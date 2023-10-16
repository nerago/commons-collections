/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.collections4.trie;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.collections4.IterableSortedMap;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.collections4.MutableBoolean;
import org.apache.commons.collections4.Reference;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.SortedMapRange;
import org.apache.commons.collections4.iterators.SingletonIterator;

/**
 * This class implements the base PATRICIA algorithm and everything that
 * is related to the {@link Map} interface.
 *
 * @param <K> the type of the keys in this map
 * @param <V> the type of the values in this map
 * @since 4.0
 */
public abstract class AbstractPatriciaTrie<K, V, SubMap extends IterableSortedMap<K, V, ?>>
        extends AbstractBitwiseTrie<K, V, SubMap> {

    private static final long serialVersionUID = 5155253417231339498L;

    /** The root node of the {@link org.apache.commons.collections4.Trie}. */
    private transient TrieEntry<K, V> root = new TrieEntry<>(null, null, -1);

    /**
     * Each of these fields are initialized to contain an instance of the
     * appropriate view the first time this view is requested. The views are
     * stateless, so there's no reason to create more than one of each.
     */
    private transient volatile Set<K> keySet;
    private transient volatile Collection<V> values;
    private transient volatile Set<Map.Entry<K, V>> entrySet;

    /** The current size of the {@link org.apache.commons.collections4.Trie}. */
    private transient int size;

    /**
     * The number of times this {@link org.apache.commons.collections4.Trie} has been modified.
     * It's used to detect concurrent modifications and fail-fast the {@link Iterator}s.
     */
    protected transient int modCount;

    /**
     * Constructs a new {@link Trie} using the given {@link KeyAnalyzer}.
     *
     * @param keyAnalyzer  the {@link KeyAnalyzer} to use
     */
    protected AbstractPatriciaTrie(final KeyAnalyzer<? super K> keyAnalyzer) {
        super(keyAnalyzer, SortedMapRange.full(keyAnalyzer));
    }

    /**
     * Constructs a new {@link org.apache.commons.collections4.Trie}
     * using the given {@link KeyAnalyzer} and initializes the {@link org.apache.commons.collections4.Trie}
     * with the values from the provided {@link Map}.
     */
    protected AbstractPatriciaTrie(final KeyAnalyzer<? super K> keyAnalyzer,
                                   final Map<? extends K, ? extends V> map) {
        super(keyAnalyzer, SortedMapRange.full(keyAnalyzer));
        putAll(map);
    }

    @Override
    public void clear() {
        root.key = null;
        root.bitIndex = -1;
        root.value = null;

        root.parent = null;
        root.left = root;
        root.right = null;
        root.predecessor = root;

        size = 0;
        incrementModCount();
    }

    @Override
    public int size() {
        return size;
    }

    /**
     * A helper method to increment the {@link org.apache.commons.collections4.Trie} size and the modification counter.
     */
    void incrementSize() {
        size++;
        incrementModCount();
    }

    /**
     * A helper method to decrement the {@link org.apache.commons.collections4.Trie} size and increment the modification counter.
     */
    void decrementSize() {
        size--;
        incrementModCount();
    }

    /**
     * A helper method to increment the modification counter.
     */
    private void incrementModCount() {
        ++modCount;
    }

    @Override
    public V put(final K key, final V value) {
        return doPut(key, value, true, true);
    }

    @Override
    public V putIfAbsent(final K key, final V value) {
        return doPut(key, value, true, false);
    }

    @Override
    public V replace(final K key, final V value) {
        return doPut(key, value, false, true);
    }

    private V doPut(final K key, final V value, final boolean addIfAbsent, final boolean updateIfPresent) {
        Objects.requireNonNull(key, "key");

        // empty key maps to root
        final int lengthInBits = lengthInBits(key);
        if (lengthInBits == 0) {
            return doPutEntry(root, key, value, addIfAbsent, updateIfPresent);
        }

        // find matching entry node
        final TrieEntry<K, V> found = getNearestEntryForKey(key, lengthInBits);
        if (equalKeys(key, found.key)) {
            return doPutEntry(found, key, value, addIfAbsent, updateIfPresent);
        }

        // find difference bit
        final int bitIndex = bitIndex(key, found.key);
        if (KeyAnalyzer.isValidBitIndex(bitIndex)) {
            if (addIfAbsent) {
                final TrieEntry<K, V> t = new TrieEntry<>(key, value, bitIndex);
                addEntry(t, lengthInBits);
                incrementSize();
            }
            return null;
        } else if (KeyAnalyzer.isNullBitKey(bitIndex)) {
            return doPutEntry(root, key, value, addIfAbsent, updateIfPresent);
        } else if (KeyAnalyzer.isEqualBitKey(bitIndex)) {
            return doPutEntry(found, key, value, addIfAbsent, updateIfPresent);
        } else {
            throw new IllegalArgumentException("key");
        }
    }

    private V doPut(final K key,
                    final Function<? super K, ? extends V> absentFunc,
                    final BiFunction<? super K, ? super V, ? extends V> presentFunc,
                    final boolean saveNulls) {
        Objects.requireNonNull(key, "key");
        final int expectedModCount = this.modCount;

        // empty key maps to root
        final int lengthInBits = lengthInBits(key);
        if (lengthInBits == 0) {
            return doPutEntry(root, key, absentFunc, presentFunc, saveNulls, expectedModCount);
        }

        // find matching entry node
        final TrieEntry<K, V> found = getNearestEntryForKey(key, lengthInBits);
        if (equalKeys(key, found.key)) {
            return doPutEntry(found, key, absentFunc, presentFunc, saveNulls, expectedModCount);
        }

        // find difference bit
        final int bitIndex = bitIndex(key, found.key);
        if (KeyAnalyzer.isValidBitIndex(bitIndex)) {
            if (absentFunc != null) {
                final V value = absentFunc.apply(key);
                if (value != null || saveNulls) {
                    final TrieEntry<K, V> t = new TrieEntry<>(key, value, bitIndex);
                    addEntry(t, lengthInBits);
                    incrementSize();
                }
            }
            return null;
        } else if (KeyAnalyzer.isNullBitKey(bitIndex)) {
            return doPutEntry(root, key, absentFunc, presentFunc, saveNulls, expectedModCount);
        } else if (KeyAnalyzer.isEqualBitKey(bitIndex)) {
            return doPutEntry(found, key, absentFunc, presentFunc, saveNulls, expectedModCount);
        } else {
            throw new IllegalArgumentException("key");
        }
    }

    private V doPutEntry(final TrieEntry<K, V> found, final K key, final V value, final boolean addIfAbsent, final boolean updateIfPresent) {
        if (found.isEmptyRoot()) {
            if (addIfAbsent) {
                incrementSize();
                found.setKeyValue(key, value);
            }
            return null;
        } else {
            if (updateIfPresent) {
                incrementModCount();
                return found.setKeyValue(key, value);
            }
            return found.value;
        }
    }

    private V doPutEntry(final TrieEntry<K,V> found, final K key,
                          final Function<? super K,? extends V> absentFunc,
                          final BiFunction<? super K,? super V,? extends V> presentFunc,
                          final boolean saveNulls,
                          final int expectedModCount) {
        if (found.isEmptyRoot()) {
            if (absentFunc != null) {
                final V value = absentFunc.apply(key);
                if (expectedModCount != modCount) {
                    throw new ConcurrentModificationException();
                }
                if (value != null || saveNulls) {
                    incrementSize();
                    found.setKeyValue(key, value);
                    return value;
                }
            }
            return null;
        } else {
            if (presentFunc != null) {
                final V value = presentFunc.apply(key, found.value);
                if (expectedModCount != modCount) {
                    throw new ConcurrentModificationException();
                }
                if (value != null || saveNulls) {
                    incrementModCount();
                    found.setKeyValue(key, value);
                    return value;
                } else {
                    removeEntry(found);
                    return null;
                }
            }
            return found.value;
        }
    }

    /**
     * Adds the given {@link TrieEntry} to the {@link org.apache.commons.collections4.Trie}.
     */
    TrieEntry<K, V> addEntry(final TrieEntry<K, V> entry, final int lengthInBits) {
        TrieEntry<K, V> current = root.left;
        TrieEntry<K, V> path = root;
        while (true) {
            // find a point where entry goes under
            // or a point where we've hit a loopback
            if (current.bitIndex >= entry.bitIndex || current.bitIndex <= path.bitIndex) {
                entry.predecessor = entry;

                // current always gets pushed down "under" new entry
                if (!isBitSet(entry.key, entry.bitIndex, lengthInBits)) {
                    entry.left = entry;
                    entry.right = current;
                } else {
                    entry.left = current;
                    entry.right = entry;
                }

                // parent is set to node we navigated via
                entry.parent = path;

                // if current actually belongs deeper than new entry set its parent
                if (current.bitIndex >= entry.bitIndex) {
                    current.parent = entry;
                }

                // [[if we inserted an uplink, set the predecessor on it]]
                // if we've hit a loop
                if (current.bitIndex <= path.bitIndex) {
                    current.predecessor = entry;
                }

                if (path == root || !isBitSet(entry.key, path.bitIndex, lengthInBits)) {
                    path.left = entry;
                } else {
                    path.right = entry;
                }

                return entry;
            }

            path = current;

            if (!isBitSet(entry.key, current.bitIndex, lengthInBits)) {
                current = current.left;
            } else {
                current = current.right;
            }
        }
    }

    @Override
    public V get(final Object k) {
        final TrieEntry<K, V> entry = getEntry(k);
        return entry != null ? entry.getValue() : null;
    }

    @Override
    public V getOrDefault(final Object key, final V defaultValue) {
        final TrieEntry<K, V> entry = getEntry(key);
        return entry != null ? entry.getValue() : defaultValue;
    }

    /**
     * Returns the entry associated with the specified key in the
     * PatriciaTrieBase.  Returns null if the map contains no mapping
     * for this key.
     * <p>
     * This may throw ClassCastException if the object is not of type K.
     */
     protected TrieEntry<K, V> getEntry(final Object k) {
        final K key = castKey(k);
        if (key == null) {
            return null;
        }

        final int lengthInBits = lengthInBits(key);
        final TrieEntry<K, V> entry = getNearestEntryForKey(key, lengthInBits);
        return !entry.isEmptyRoot() && equalKeys(key, entry.key) ? entry : null;
    }

    /**
     * Returns the {@link java.util.Map.Entry} whose key is closest in a bitwise XOR
     * metric to the given key. This is NOT lexicographic closeness.
     * For example, given the keys:
     *
     * <ol>
     * <li>D = 1000100
     * <li>H = 1001000
     * <li>L = 1001100
     * </ol>
     *
     * If the {@link org.apache.commons.collections4.Trie} contained 'H' and 'L', a lookup of 'D' would
     * return 'L', because the XOR distance between D &amp; L is smaller
     * than the XOR distance between D &amp; H.
     *
     * @param key  the key to use in the search
     * @return the {@link java.util.Map.Entry} whose key is closest in a bitwise XOR metric
     *   to the provided key
     */
    public Map.Entry<K, V> select(final K key) {
        final int lengthInBits = lengthInBits(key);
        final Reference<Map.Entry<K, V>> reference = new Reference<>();
        if (!selectR(root.left, -1, key, lengthInBits, reference)) {
            return reference.get();
        }
        return null;
    }

    /**
     * Returns the key that is closest in a bitwise XOR metric to the
     * provided key. This is NOT lexicographic closeness!
     *
     * For example, given the keys:
     *
     * <ol>
     * <li>D = 1000100
     * <li>H = 1001000
     * <li>L = 1001100
     * </ol>
     *
     * If the {@link org.apache.commons.collections4.Trie} contained 'H' and 'L', a lookup of 'D' would
     * return 'L', because the XOR distance between D &amp; L is smaller
     * than the XOR distance between D &amp; H.
     *
     * @param key  the key to use in the search
     * @return the key that is closest in a bitwise XOR metric to the provided key
     */
    public K selectKey(final K key) {
        final Map.Entry<K, V> entry = select(key);
        if (entry == null) {
            return null;
        }
        return entry.getKey();
    }

    /**
     * Returns the value whose key is closest in a bitwise XOR metric to
     * the provided key. This is NOT lexicographic closeness!
     *
     * For example, given the keys:
     *
     * <ol>
     * <li>D = 1000100
     * <li>H = 1001000
     * <li>L = 1001100
     * </ol>
     *
     * If the {@link org.apache.commons.collections4.Trie} contained 'H' and 'L', a lookup of 'D' would
     * return 'L', because the XOR distance between D &amp; L is smaller
     * than the XOR distance between D &amp; H.
     *
     * @param key  the key to use in the search
     * @return the value whose key is closest in a bitwise XOR metric
     * to the provided key
     */
    public V selectValue(final K key) {
        final Map.Entry<K, V> entry = select(key);
        if (entry == null) {
            return null;
        }
        return entry.getValue();
    }

    private boolean selectR(final TrieEntry<K, V> h, final int bitIndex,
                            final K key, final int lengthInBits,
                            final Reference<Map.Entry<K, V>> reference) {

        if (h.bitIndex <= bitIndex) {
            // If we hit the root Node and it is empty
            // we have to look for an alternative best
            // matching node.
            if (!h.isEmptyRoot()) {
                reference.set(h);
                return false;
            }
            return true;
        }

        if (!isBitSet(key, h.bitIndex, lengthInBits)) {
            if (selectR(h.left, h.bitIndex, key, lengthInBits, reference)) {
                return selectR(h.right, h.bitIndex, key, lengthInBits, reference);
            }
        } else {
            if (selectR(h.right, h.bitIndex, key, lengthInBits, reference)) {
                return selectR(h.left, h.bitIndex, key, lengthInBits, reference);
            }
        }
        return false;
    }

    @Override
    public boolean containsKey(final Object k) {
        if (k == null) {
            return false;
        }

        final K key = castKey(k);
        final int lengthInBits = lengthInBits(key);
        final TrieEntry<K, V> entry = getNearestEntryForKey(key, lengthInBits);
        return !entry.isEmptyRoot() && equalKeys(key, entry.key);
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        if (entrySet == null) {
            entrySet = new EntrySet();
        }
        return entrySet;
    }

    @Override
    public Set<K> keySet() {
        if (keySet == null) {
            keySet = new KeySet();
        }
        return keySet;
    }

    @Override
    public Collection<V> values() {
        if (values == null) {
            values = new Values();
        }
        return values;
    }

    @Override
    public V remove(final Object k) {
        if (k == null) {
            // TODO apply to root?
            return null;
        }

        final K key = castKey(k);
        final int lengthInBits = lengthInBits(key);
        TrieEntry<K, V> current = root.left;
        TrieEntry<K, V> path = root;
        while (true) {
            if (current.bitIndex <= path.bitIndex) {
                if (!current.isEmptyRoot() && equalKeys(key, current.key)) {
                    return removeEntry(current);
                }
                return null;
            }

            path = current;

            if (!isBitSet(key, current.bitIndex, lengthInBits)) {
                current = current.left;
            } else {
                current = current.right;
            }
        }
    }

    @Override
    public boolean remove(final Object k, final Object value) {
        if (k == null) {
            // TODO apply to root?
            return false;
        }

        final K key = castKey(k);
        final int lengthInBits = lengthInBits(key);
        TrieEntry<K, V> current = root.left;
        TrieEntry<K, V> path = root;
        while (true) {
            if (current.bitIndex <= path.bitIndex) {
                if (!current.isEmptyRoot() && equalKeys(key, current.key) && Objects.equals(value, current.value)) {
                    removeEntry(current);
                    return true;
                }
                return false;
            }

            path = current;

            if (!isBitSet(key, current.bitIndex, lengthInBits)) {
                current = current.left;
            } else {
                current = current.right;
            }
        }
    }

    @Override
    public boolean replace(final K key, final V oldValue, final V newValue) {
        final MutableBoolean didUpdate = new MutableBoolean();
        doPut(key, null, (k, v) -> {
            if (Objects.equals(v, oldValue)) {
                didUpdate.flag = true;
                return newValue;
            } else {
                return v;
            }
        }, true);
        return didUpdate.flag;
    }

    @Override
    public V computeIfAbsent(final K key, final Function<? super K, ? extends V> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        return doPut(key, mappingFunction, null, false);
    }

    @Override
    public V computeIfPresent(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        return doPut(key, null, remappingFunction, false);
    }

    @Override
    public V compute(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        return doPut(key,
                v -> remappingFunction.apply(v, null),
                remappingFunction, false);
    }

    @Override
    public V merge(final K key, final V value, final BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(value);
        Objects.requireNonNull(remappingFunction);
        return doPut(key,
                k -> value,
                (k, v) -> remappingFunction.apply(v, value), false);
    }

    /**
     * Returns the nearest entry for a given key.  This is useful
     * for finding knowing if a given key exists (and finding the value
     * for it), or for inserting the key.
     *
     * The actual get implementation. This is very similar to
     * selectR but with the exception that it might return the
     * root Entry even if it's empty.
     */
    TrieEntry<K, V> getNearestEntryForKey(final K key, final int lengthInBits) {
        TrieEntry<K, V> current = root.left;
        TrieEntry<K, V> path = root;
        while (true) {
            if (current.bitIndex <= path.bitIndex) {
                return current;
            }

            path = current;
            if (!isBitSet(key, current.bitIndex, lengthInBits)) {
                current = current.left;
            } else {
                current = current.right;
            }
        }
    }

    /**
     * Removes a single entry from the {@link org.apache.commons.collections4.Trie}.
     *
     * If we found a Key (Entry h) then figure out if it's
     * an internal (hard to remove) or external Entry (easy
     * to remove)
     */
    V removeEntry(final TrieEntry<K, V> h) {
        if (h != root) {
            if (h.isInternalNode()) {
                removeInternalEntry(h);
            } else {
                removeExternalEntry(h);
            }
        }

        decrementSize();
        return h.setKeyValue(null, null);
    }

    /**
     * Removes an external entry from the {@link org.apache.commons.collections4.Trie}.
     *
     * If it's an external Entry then just remove it.
     * This is very easy and straight forward.
     */
    private void removeExternalEntry(final TrieEntry<K, V> h) {
        if (h == root) {
            throw new IllegalArgumentException("Cannot delete root Entry!");
        }
        if (!h.isExternalNode()) {
            throw new IllegalArgumentException(h + " is not an external Entry!");
        }

        final TrieEntry<K, V> parent = h.parent;
        final TrieEntry<K, V> child = h.left == h ? h.right : h.left;

        if (parent.left == h) {
            parent.left = child;
        } else {
            parent.right = child;
        }

        // either the parent is changing, or the predecessor is changing.
        if (child.bitIndex > parent.bitIndex) {
            child.parent = parent;
        } else {
            child.predecessor = parent;
        }

    }

    /**
     * Removes an internal entry from the {@link org.apache.commons.collections4.Trie}.
     *
     * If it's an internal Entry then "good luck" with understanding
     * this code. The Idea is essentially that Entry p takes Entry h's
     * place in the trie which requires some re-wiring.
     */
    private void removeInternalEntry(final TrieEntry<K, V> h) {
        if (h == root) {
            throw new IllegalArgumentException("Cannot delete root Entry!");
        }
        if (!h.isInternalNode()) {
            throw new IllegalArgumentException(h + " is not an internal Entry!");
        }

        final TrieEntry<K, V> p = h.predecessor;

        // Set P's bitIndex
        p.bitIndex = h.bitIndex;

        // Fix P's parent, predecessor and child Nodes
        {
            final TrieEntry<K, V> parent = p.parent;
            final TrieEntry<K, V> child = p.left == h ? p.right : p.left;

            // if it was looping to itself previously,
            // it will now be pointed from its parent
            // (if we aren't removing its parent --
            //  in that case, it remains looping to itself).
            // otherwise, it will continue to have the same
            // predecessor.
            if (p.predecessor == p && p.parent != h) {
                p.predecessor = p.parent;
            }

            if (parent.left == p) {
                parent.left = child;
            } else {
                parent.right = child;
            }

            if (child.bitIndex > parent.bitIndex) {
                child.parent = parent;
            }
        }

        // Fix H's parent and child Nodes
        {
            // If H is a parent of its left and right child
            // then change them to P
            if (h.left.parent == h) {
                h.left.parent = p;
            }

            if (h.right.parent == h) {
                h.right.parent = p;
            }

            // Change H's parent
            if (h.parent.left == h) {
                h.parent.left = p;
            } else {
                h.parent.right = p;
            }
        }

        // Copy the remaining fields from H to P
        //p.bitIndex = h.bitIndex;
        p.parent = h.parent;
        p.left = h.left;
        p.right = h.right;

        // Make sure that if h was pointing to any uplinks,
        // p now points to them.
        if (isValidUplink(p.left, p)) {
            p.left.predecessor = p;
        }

        if (isValidUplink(p.right, p)) {
            p.right.predecessor = p;
        }
    }

    /**
     * Returns the entry lexicographically after the given entry.
     * If the given entry is null, returns the first node.
     */
    TrieEntry<K, V> nextEntry(final TrieEntry<K, V> node) {
        if (node == null) {
            return firstEntry();
        }
        return nextEntryImpl(node.predecessor, node, null);
    }

    /**
     * Scans for the next node, starting at the specified point, and using 'previous'
     * as a hint that the last node we returned was 'previous' (so we know not to return
     * it again).  If 'tree' is non-null, this will limit the search to the given tree.
     *
     * The basic premise is that each iteration can follow the following steps:
     *
     * 1) Scan all the way to the left.
     *   a) If we already started from this node last time, proceed to Step 2.
     *   b) If a valid uplink is found, use it.
     *   c) If the result is an empty node (root not set), break the scan.
     *   d) If we already returned the left node, break the scan.
     *
     * 2) Check the right.
     *   a) If we already returned the right node, proceed to Step 3.
     *   b) If it is a valid uplink, use it.
     *   c) Do Step 1 from the right node.
     *
     * 3) Back up through the parents until we encounter find a parent
     *    that we're not the right child of.
     *
     * 4) If there's no right child of that parent, the iteration is finished.
     *    Otherwise continue to Step 5.
     *
     * 5) Check to see if the right child is a valid uplink.
     *    a) If we already returned that child, proceed to Step 6.
     *       Otherwise, use it.
     *
     * 6) If the right child of the parent is the parent itself, we've
     *    already found &amp; returned the end of the Trie, so exit.
     *
     * 7) Do Step 1 on the parent's right child.
     */
    TrieEntry<K, V> nextEntryImpl(final TrieEntry<K, V> start,
            final TrieEntry<K, V> previous, final TrieEntry<K, V> tree) {

        TrieEntry<K, V> current = start;

        // Only look at the left if this was a recursive or
        // the first check, otherwise we know we've already looked
        // at the left.
        if (previous == null || start != previous.predecessor) {
            while (!current.left.isEmptyRoot()) {
                // stop traversing if we've already
                // returned the left of this node.
                if (previous == current.left) {
                    break;
                }

                if (isValidUplink(current.left, current)) {
                    return current.left;
                }

                current = current.left;
            }
        }

        // If there's no data at all, exit.
        if (current.isEmptyRoot()) {
            return null;
        }

        // If we've already returned the left,
        // and the immediate right is null,
        // there's only one entry in the Trie
        // which is stored at the root.
        //
        //  / ("")   <-- root
        //  \_/  \
        //       null <-- 'current'
        //
        if (current.right == null) {
            return null;
        }

        // If nothing valid on the left, try the right.
        if (previous != current.right) {
            // See if it immediately is valid.
            if (isValidUplink(current.right, current)) {
                return current.right;
            }

            // Must search on the right's side if it wasn't initially valid.
            return nextEntryImpl(current.right, previous, tree);
        }

        // Neither left nor right are valid, find the first parent
        // whose child did not come from the right & traverse it.
        while (current == current.parent.right) {
            // If we're going to traverse to above the subtree, stop.
            if (current == tree) {
                return null;
            }

            current = current.parent;
        }

        // If we're on the top of the subtree, we can't go any higher.
        if (current == tree) {
            return null;
        }

        // If there's no right, the parent must be root, so we're done.
        if (current.parent.right == null) {
            return null;
        }

        // If the parent's right points to itself, we've found one.
        if (previous != current.parent.right
                && isValidUplink(current.parent.right, current.parent)) {
            return current.parent.right;
        }

        // If the parent's right is itself, there can't be any more nodes.
        if (current.parent.right == current.parent) {
            return null;
        }

        // We need to traverse down the parent's right's path.
        return nextEntryImpl(current.parent.right, previous, tree);
    }

    /**
     * Returns the first entry the {@link org.apache.commons.collections4.Trie} is storing.
     * <p>
     * This is implemented by going always to the left until
     * we encounter a valid uplink. That uplink is the first key.
     */
    TrieEntry<K, V> firstEntry() {
        // if Trie is empty, no first node.
        if (isEmpty()) {
            return null;
        }

        return followLeft(root);
    }

    /**
     * Goes left through the tree until it finds a valid node.
     */
    TrieEntry<K, V> followLeft(TrieEntry<K, V> node) {
        while (true) {
            TrieEntry<K, V> child = node.left;
            // if we hit root and it didn't have a node, go right instead.
            if (child.isEmptyRoot()) {
                child = node.right;
            }

            if (child.bitIndex <= node.bitIndex) {
                return child;
            }

            node = child;
        }
    }


    @Override
    public Comparator<? super K> comparator() {
        return getKeyAnalyzer();
    }

    @Override
    public K firstKey() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return firstEntry().getKey();
    }

    @Override
    public K lastKey() {
        final TrieEntry<K, V> entry = lastEntry();
        if (entry != null) {
            return entry.getKey();
        }
        throw new NoSuchElementException();
    }

    @Override
    public K nextKey(final K key) {
        Objects.requireNonNull(key, "key");
        final TrieEntry<K, V> entry = getEntry(key);
        if (entry != null) {
            final TrieEntry<K, V> nextEntry = nextEntry(entry);
            return nextEntry != null ? nextEntry.getKey() : null;
        }
        return null;
    }

    @Override
    public K previousKey(final K key) {
        Objects.requireNonNull(key, "key");
        final TrieEntry<K, V> entry = getEntry(key);
        if (entry != null) {
            final TrieEntry<K, V> prevEntry = previousEntry(entry);
            return prevEntry != null ? prevEntry.getKey() : null;
        }
        return null;
    }

    @Override
    public OrderedMapIterator<K, V> mapIterator() {
        return new TrieMapIterator();
    }

    @Override
    public SubMap prefixMap(final K key) {
        return getPrefixMapByBits(key, 0, lengthInBits(key));
    }

    /**
     * Returns a view of this {@link org.apache.commons.collections4.Trie} of all elements that are prefixed
     * by the number of bits in the given Key.
     * <p>
     * The view that this returns is optimized to have a very efficient
     * {@link Iterator}. The {@link SortedMap#firstKey()},
     * {@link SortedMap#lastKey()} &amp; {@link Map#size()} methods must
     * iterate over all possible values in order to determine the results.
     * This information is cached until the PATRICIA {@link org.apache.commons.collections4.Trie} changes.
     * All other methods (except {@link Iterator}) must compare the given
     * key to the prefix to ensure that it is within the range of the view.
     * The {@link Iterator}'s remove method must also relocate the subtree
     * that contains the prefixes if the entry holding the subtree is
     * removed or changes. Changing the subtree takes O(K) time.
     *
     * @param key          the key to use in the search
     * @param offsetInBits the prefix offset
     * @param lengthInBits the number of significant prefix bits
     * @return a {@link SortedMap} view of this {@link org.apache.commons.collections4.Trie} with all elements whose
     * key is prefixed by the search key
     */
    private SubMap getPrefixMapByBits(final K key, final int offsetInBits, final int lengthInBits) {

        final int offsetLength = offsetInBits + lengthInBits;
        if (offsetLength > lengthInBits(key)) {
            throw new IllegalArgumentException(offsetInBits + " + "
                    + lengthInBits + " > " + lengthInBits(key));
        }

        if (offsetLength == 0) {
            return (SubMap) this;
        }

        return (SubMap) new PrefixRangeMap(key, offsetInBits, lengthInBits);
    }

    @Override
    public SubMap subMap(final SortedMapRange<K> range) {
        return (SubMap) new BoundedRangeMap(range);
    }

    /**
     * Returns an entry strictly higher than the given key,
     * or null if no such entry exists.
     */
    TrieEntry<K, V> higherEntry(final K key) {
        // TODO: Cleanup so that we don't actually have to add/remove from the
        //       tree.  (We do it here because there are other well-defined
        //       functions to perform the search.)
        final int lengthInBits = lengthInBits(key);

        if (lengthInBits == 0) {
            if (!root.isEmptyRoot()) {
                // If data in root, and more after -- return it.
                if (size() > 1) {
                    return nextEntry(root);
                }
                // If no more after, no higher entry.
                return null;
            }
            // Root is empty & we want something after empty, return first.
            return firstEntry();
        }

        final TrieEntry<K, V> found = getNearestEntryForKey(key, lengthInBits);
        if (equalKeys(key, found.key)) {
            return nextEntry(found);
        }

        final int bitIndex = bitIndex(key, found.key);
        if (KeyAnalyzer.isValidBitIndex(bitIndex)) {
            final TrieEntry<K, V> added = new TrieEntry<>(key, null, bitIndex);
            addEntry(added, lengthInBits);
            incrementSize(); // must increment because remove will decrement
            final TrieEntry<K, V> ceil = nextEntry(added);
            removeEntry(added);
            modCount -= 2; // we didn't really modify it.
            return ceil;
        }
        if (KeyAnalyzer.isNullBitKey(bitIndex)) {
            if (!root.isEmptyRoot()) {
                return firstEntry();
            }
            if (size() > 1) {
                return nextEntry(firstEntry());
            }
            return null;
        }
        if (KeyAnalyzer.isEqualBitKey(bitIndex)) {
            return nextEntry(found);
        }

        // we should have exited above.
        throw new IllegalStateException("invalid lookup: " + key);
    }

    /**
     * Returns a key-value mapping associated with the least key greater
     * than or equal to the given key, or null if there is no such key.
     */
    TrieEntry<K, V> ceilingEntry(final K key) {
        TrieEntry<K, V> b = ceilingEntry5(key);
        TrieEntry<K, V> a = ceilingEntry0(key);
        System.out.println("ceilingEntry " + a.key + " " + b.key);
        assert a == b;
//        TrieEntry<K, V> c = ceilingEntry4(key);
        return a == b ? a : b;
//        return c;
//        return ceilingEntry3(key);
    }

    TrieEntry<K, V> ceilingEntry0(final K key) {
        // Basically:
        // Follow the steps of adding an entry, but instead...
        //
        // - If we ever encounter a situation where we found an equal
        //   key, we return it immediately.
        //
        // - If we hit an empty root, return the first iterable item.
        //
        // - If we have to add a new item, we temporarily add it,
        //   find the successor to it, then remove the added item.
        //
        // These steps ensure that the returned value is either the
        // entry for the key itself, or the first entry directly after
        // the key.

        // TODO: Cleanup so that we don't actually have to add/remove from the
        //       tree.  (We do it here because there are other well-defined
        //       functions to perform the search.)
        final int lengthInBits = lengthInBits(key);

        if (lengthInBits == 0) {
            if (!root.isEmptyRoot()) {
                return root;
            }
            return firstEntry();
        }

        final TrieEntry<K, V> found = getNearestEntryForKey(key, lengthInBits);
        if (equalKeys(key, found.key)) {
            return found;
        }

        final int bitIndex = bitIndex(key, found.key);
        if (KeyAnalyzer.isValidBitIndex(bitIndex)) {
            final TrieEntry<K, V> added = new TrieEntry<>(key, null, bitIndex);
            addEntry(added, lengthInBits);
            incrementSize(); // must increment because remove will decrement
            final TrieEntry<K, V> ceil = nextEntry(added);
            removeEntry(added);
            modCount -= 2; // we didn't really modify it.
            return ceil;
        }
        if (KeyAnalyzer.isNullBitKey(bitIndex)) {
            if (!root.isEmptyRoot()) {
                return root;
            }
            return firstEntry();
        }
        if (KeyAnalyzer.isEqualBitKey(bitIndex)) {
            return found;
        }

        // we should have exited above.
        throw new IllegalStateException("invalid lookup: " + key);
    }

    /**
     * Returns a key-value mapping associated with the least key greater
     * than or equal to the given key, or null if there is no such key.
     */
    TrieEntry<K, V> ceilingEntry5(final K key) {
        final int keyLengthInBits = lengthInBits(key);


        if (keyLengthInBits == 0) {
            System.out.println("ceilingEntry5 null");
            if (!root.isEmptyRoot()) {
                return root;
            }
            return firstEntry();
        }

        final KeyAnalyzer<? super K> keyAnalyzer = getKeyAnalyzer();
        final int bitsPerElement = keyAnalyzer.bitsPerElement();

        TrieEntry<K, V> current = root.left;
        TrieEntry<K, V> path = root;
        TrieEntry<K, V> low = root;
        TrieEntry<K, V> high = null;

        // wait until key plausibly matches
        int bitIndex = keyAnalyzer.bitIndex(key, 0, keyLengthInBits, current.key, 0, keyAnalyzer.lengthInBits(current.key));
        while (bitIndex < current.bitIndex || current.bitIndex > path.bitIndex) {
            System.out.println("ceilingEntry5 " + bitIndex + " " + current.bitIndex);
            if (KeyAnalyzer.isEqualBitKey(bitIndex)) {
                System.out.println("ceilingEntry5 equal " + current.key);
                return current;
            }
            path = current;
            if (bitIndex < current.bitIndex) {
                // differs earlier bit position means it can't be in tree
                // we can assume key[bitIndex] != current[bitIndex]
                if (!isBitSet(key, bitIndex, keyLengthInBits)) {
                    // zero bit in key means one in current, thus key < current
                    high = current;
                    System.out.println("ceilingEntry5 A " + current.key);
                } else {
                    // one bit in key means zero in current, thus key > current
                    low = current;
                    System.out.println("ceilingEntry5 B " + current.key);
                }
                break;
            } else {
                // lets consider bitIndex split >>> current.bitIndex
                // assume key[current.bitIndex] == current[current.bitIndex]
                if (!isBitSet(key, current.bitIndex, keyLengthInBits)) {
                    System.out.println("ceilingEntry5 C " + current.key);
                    high = current;
                    current = current.left;
                } else {
                    System.out.println("ceilingEntry5 D " + current.key);
                    low = current;
                    current = current.right;
                }
            }
            int offset = (path.bitIndex / bitsPerElement) * bitsPerElement;
            bitIndex = keyAnalyzer.bitIndex(
                    key, offset, keyLengthInBits - offset,
                    current.key, offset, keyAnalyzer.lengthInBits(current.key) - offset
            );
        }

        if (high != null)
            return high;
        else
            return nextEntry(low);
    }

    /**
     * Returns a key-value mapping associated with the least key greater
     * than or equal to the given key, or null if there is no such key.
     */
    TrieEntry<K, V> ceilingEntry4(final K key) {
        final int keyLengthInBits = lengthInBits(key);

        if (keyLengthInBits == 0) {
            System.out.println("ceilingEntry4 null");
            if (!root.isEmptyRoot()) {
                return root;
            }
            return firstEntry();
        }

        KeyAnalyzer<? super K> keyAnalyzer = getKeyAnalyzer();
        boolean followedLeft = true;
        TrieEntry<K, V> current = root.left;
        TrieEntry<K, V> path = root;

        // wait until key plausibly matches
        int bitIndex = keyAnalyzer.bitIndex(key, 0, keyLengthInBits, current.key, 0, keyAnalyzer.lengthInBits(current.key));
        while (bitIndex < current.bitIndex) {
            if (KeyAnalyzer.isEqualBitKey(bitIndex)) {
                System.out.println("ceilingEntry4 equal");
                return current;
            }
            path = current;
            if (!isBitSet(key, bitIndex, keyLengthInBits)) {
                // zero bit means key < current
                current = current.left;
                followedLeft = true;
            } else {
                // one bit means key > current
                current = current.right;
                followedLeft = false;
            }
            bitIndex = keyAnalyzer.bitIndex(key, 0, keyLengthInBits, current.key, 0, keyAnalyzer.lengthInBits(current.key));
        }

        while (true) {
            if (KeyAnalyzer.isEqualBitKey(bitIndex)) {
                System.out.println("ceilingEntry4 equal");
                return current;
            } else if (current.bitIndex <= path.bitIndex) {
                // hit an uplink (terminal path)
                if (followedLeft)
                    return path;
                else
                    return nextEntry(current);
            } else if (bitIndex > current.bitIndex) {
                // differs further down the path
                if (!isBitSet(key, bitIndex, keyLengthInBits)) {
                    // zero bit means key < current
                    if (followedLeft)
                        return path;
                }
            }
            path = current;
        }

//        return keyAnalyzer.bitIndex(key, 0, lengthInBits(key), foundKey, 0, lengthInBits(foundKey));

//
//        while (true) {
//
//            // end of the road
//            if (current.bitIndex <= path.bitIndex) {
//                if (followedLeft)
//                    return current;
//            }
//
//            path = current;
//            if (!isBitSet(key, current.bitIndex, lengthInBits)) {
//                current = current.left;
//                followedLeft = true;
//            } else {
//                current = current.right;
//                followedLeft = false;
//            }
//        }
    }

    TrieEntry<K, V> ceilingEntry3(final K key) {
        final int lengthInBits = lengthInBits(key);

        if (lengthInBits == 0) {
            System.out.println("ceilingEntry2 null");
            if (!root.isEmptyRoot()) {
                return root;
            }
            return firstEntry();
        }

        TrieEntry<K, V> current = root.left;
        TrieEntry<K, V> path = root;
        while (true) {
            int currentBitIndex = bitIndex(key, current.key);
            if (KeyAnalyzer.isEqualBitKey(currentBitIndex)) {
                System.out.println("ceilingEntry2 equal");
                return current;
            }

            if (currentBitIndex == current.bitIndex) {
                System.out.println("ceilingEntry2 bitindex ==");
                boolean keyXBit = isBitSet(key, current.bitIndex, lengthInBits);
                boolean curXBit = isBitSet(current.key, current.bitIndex, lengthInBits(current.key));
                assert keyXBit != curXBit;

                boolean keyDBit = isBitSet(key, currentBitIndex, lengthInBits);
                boolean curDBit = isBitSet(current.key, currentBitIndex, lengthInBits(current.key));
                assert keyDBit != curDBit;
                assert keyDBit == keyXBit;
                // if the difference is at our current index then we know this bit specifically is different
                if (!keyDBit && isValidUplink(current.left, current)) {
                    // zero bit on key means one on current and thus current>key
                    System.out.println("ceilingEntry2 bitindex ==, current>key, validUplink");
                    // no exact match exists
                    // our current is slightly greater than the target key
                    return current;
                } else if (keyDBit && isValidUplink(current.right, current)) {
                    // one bit on key means zero on current and thus current<key
                    System.out.println("ceilingEntry2 bitindex ==, current<key, validUplink");
                    // no exact match exists
                    // our current is slightly lower than the target
                    path = current;
                    current = current.right;
//                    return nextEntry(current);
                } else if (!keyXBit) {
                    System.out.println("ceilingEntry2 bitindex ==, follow zero path");
                    path = current;
                    current = current.left;
                } else {
                    System.out.println("ceilingEntry2 bitindex ==, follow one path");
                    path = current;
                    current = current.right;
                }
            } else if (currentBitIndex > current.bitIndex) {
                System.out.println("ceilingEntry2 bitindex > current");
                boolean keyXBit = isBitSet(key, current.bitIndex, lengthInBits);
                boolean curXBit = isBitSet(current.key, current.bitIndex, lengthInBits(current.key));
                assert keyXBit == curXBit;

                boolean keyDBit = isBitSet(key, currentBitIndex, lengthInBits);
                boolean curDBit = isBitSet(current.key, currentBitIndex, lengthInBits(current.key));
                assert keyDBit != curDBit;

                if (!keyDBit && isValidUplink(current.left, current)) {
                    System.out.println("ceilingEntry2 bitindex > current, target < current, uplink");
                    // no exact match exists
                    // our current is slightly greater than the target key
                    return current;
                } else if (keyDBit && isValidUplink(current.right, current)) {
                    System.out.println("ceilingEntry2 bitindex > current, target > current, uplink");
                    // no exact match exists
                    // our current is slightly lower than the target
                    return nextEntry(current);
                } else if (!keyXBit) {
                    System.out.println("ceilingEntry2 bitindex > current, follow zero path");
                    path = current;
                    current = current.left;
                } else {
                    System.out.println("ceilingEntry2 bitindex > current, follow one path");
                    path = current;
                    current = current.right;
                }
            } else { // (currentBitIndex < current.bitIndex)
                // this key would belong in tree before here, and we've gone too far
                System.out.println("ceilingEntry2 bitindex < current");
                assert current != path;
                assert current == path.left || current == path.right;
                if (!isBitSet(key, currentBitIndex, lengthInBits)) {
                    System.out.println("ceilingEntry2 bitindex < current, zero bit");
                    // target key has a zero, implying current's key is a one
                    // thus current is higher than target
                    return current;
                } else {
                    System.out.println("ceilingEntry2 bitindex < current, one bit");
                    // target key has a one, implying current's key is a zero
                    // thus current is lower than target
//                    return nextEntry(current);
                    if (isValidUplink(current.right, current)) {
                        System.out.println("ceilingEntry2 bitindex < current, one bit, valid uplink");
                        // no exact match exists
                        // our current is slightly lower than the target
                        return nextEntry(current);
                    }
                    path = current;
                    current = current.right;
                }
            }
        }
    }

    TrieEntry<K, V> ceilingEntry2(final K key) {
        final int lengthInBits = lengthInBits(key);

        if (lengthInBits == 0) {
            System.out.println("ceilingEntry2 null");
            if (!root.isEmptyRoot()) {
                return root;
            }
            return firstEntry();
        }

        TrieEntry<K, V> current = root.left;
        TrieEntry<K, V> path = root;
        while (true) {
            int currentBitIndex = bitIndex(key, current.key);
            if (KeyAnalyzer.isEqualBitKey(currentBitIndex)) {
                System.out.println("ceilingEntry2 equal");
                return current;
            }

            if (currentBitIndex == current.bitIndex) {
                System.out.println("ceilingEntry2 bitindex ==");
                boolean keyBit = isBitSet(key, current.bitIndex, lengthInBits);
                boolean curBit = isBitSet(current.key, current.bitIndex, lengthInBits(current.key));
                assert curBit != keyBit;
                // if the difference is at our current index then we know this bit specifically is different
                if (!isBitSet(key, currentBitIndex, lengthInBits)) {
                    System.out.println("ceilingEntry2 bitindex ==, current>key");
                    // zero bit on key means one on current and thus current>key
                    if (isValidUplink(current.left, current)) {
                        System.out.println("ceilingEntry2 bitindex ==, current>key, validUplink");
                        // no exact match exists
                        // our current is slightly greater than the target key
                        return current;
                    }
                    path = current;
                    current = current.left;
                } else {
                    System.out.println("ceilingEntry2 bitindex ==, current<key");
                    if (isValidUplink(current.right, current)) {
                        System.out.println("ceilingEntry2 bitindex ==, current<key, validUplink");
                        // no exact match exists
                        // our current is slightly lower than the target
                        return nextEntry(current);
                    }
                    path = current;
                    current = current.right;
                }
            } else if (currentBitIndex > current.bitIndex) {
                System.out.println("ceilingEntry2 bitindex > current");
                boolean keyXBit = isBitSet(key, current.bitIndex, lengthInBits);
                boolean curXBit = isBitSet(current.key, current.bitIndex, lengthInBits(current.key));
                assert keyXBit == curXBit;

                boolean keyDBit = isBitSet(key, currentBitIndex, lengthInBits);
                boolean curDBit = isBitSet(current.key, currentBitIndex, lengthInBits(current.key));
                assert keyDBit != curDBit;

                if (!keyDBit) {
                    System.out.println("ceilingEntry2 bitindex > current, zero bit");
                    // key is lower than current
                    if (isValidUplink(current.left, current)) {
                        System.out.println("ceilingEntry2 bitindex > current, zero bit, valid uplink");
                        // no exact match exists
                        // our current is slightly greater than the target key
                        return current;
                    }
                    path = current;
                    current = current.left;
                } else {
                    System.out.println("ceilingEntry2 bitindex > current, one bit");
                    if (isValidUplink(current.right, current)) {
                        System.out.println("ceilingEntry2 bitindex > current, one bit, valid uplink");
                        // no exact match exists
                        // our current is slightly lower than the target
                        return nextEntry(current);
                    }
                    path = current;
                    current = current.right;
                }
            } else { // (currentBitIndex < current.bitIndex)
                // this key would belong in tree before here, and we've gone too far
                System.out.println("ceilingEntry2 bitindex < current");
                assert current != path;
                assert current == path.left || current == path.right;
                if (!isBitSet(key, currentBitIndex, lengthInBits)) {
                    System.out.println("ceilingEntry2 bitindex < current, zero bit");
                    // target key has a zero, implying current's key is a one
                    // thus current is higher than target
                    return current;
                } else {
                    System.out.println("ceilingEntry2 bitindex < current, one bit");
                    // target key has a one, implying current's key is a zero
                    // thus current is lower than target
//                    return nextEntry(current);
                    if (isValidUplink(current.right, current)) {
                        System.out.println("ceilingEntry2 bitindex < current, one bit, valid uplink");
                        // no exact match exists
                        // our current is slightly lower than the target
                        return nextEntry(current);
                    }
                    path = current;
                    current = current.right;
                }
            }
        }
    }

    /**
     * Returns a key-value mapping associated with the greatest key
     * strictly less than the given key, or null if there is no such key.
     */
    TrieEntry<K, V> lowerEntry(final K key) {
        // Basically:
        // Follow the steps of adding an entry, but instead...
        //
        // - If we ever encounter a situation where we found an equal
        //   key, we return it's previousEntry immediately.
        //
        // - If we hit root (empty or not), return null.
        //
        // - If we have to add a new item, we temporarily add it,
        //   find the previousEntry to it, then remove the added item.
        //
        // These steps ensure that the returned value is always just before
        // the key or null (if there was nothing before it).

        // TODO: Cleanup so that we don't actually have to add/remove from the
        //       tree.  (We do it here because there are other well-defined
        //       functions to perform the search.)
        final int lengthInBits = lengthInBits(key);

//        assert false;

        if (lengthInBits == 0) {
            return null; // there can never be anything before root.
        }

        final TrieEntry<K, V> found = getNearestEntryForKey(key, lengthInBits);
        if (equalKeys(key, found.key)) {
            return previousEntry(found);
        }

        final int bitIndex = bitIndex(key, found.key);
        if (KeyAnalyzer.isValidBitIndex(bitIndex)) {
            final TrieEntry<K, V> added = new TrieEntry<>(key, null, bitIndex);
            addEntry(added, lengthInBits);
            incrementSize(); // must increment because remove will decrement
            final TrieEntry<K, V> prior = previousEntry(added);
            removeEntry(added);
            modCount -= 2; // we didn't really modify it.
            return prior;
        }
        if (KeyAnalyzer.isNullBitKey(bitIndex)) {
            return null;
        }
        if (KeyAnalyzer.isEqualBitKey(bitIndex)) {
            return previousEntry(found);
        }

        // we should have exited above.
        throw new IllegalStateException("invalid lookup: " + key);
    }

    /**
     * Returns a key-value mapping associated with the greatest key
     * less than or equal to the given key, or null if there is no such key.
     */
    TrieEntry<K, V> floorEntry(final K key) {
        // TODO: Cleanup so that we don't actually have to add/remove from the
        //       tree.  (We do it here because there are other well-defined
        //       functions to perform the search.)
        final int lengthInBits = lengthInBits(key);

        if (lengthInBits == 0) {
            if (!root.isEmptyRoot()) {
                return root;
            }
            return null;
        }

        final TrieEntry<K, V> found = getNearestEntryForKey(key, lengthInBits);
        if (equalKeys(key, found.key)) {
            return found;
        }

        final int bitIndex = bitIndex(key, found.key);
        if (KeyAnalyzer.isValidBitIndex(bitIndex)) {
            final TrieEntry<K, V> added = new TrieEntry<>(key, null, bitIndex);
            addEntry(added, lengthInBits);
            incrementSize(); // must increment because remove will decrement
            final TrieEntry<K, V> floor = previousEntry(added);
            removeEntry(added);
            modCount -= 2; // we didn't really modify it.
            return floor;
        }
        if (KeyAnalyzer.isNullBitKey(bitIndex)) {
            if (!root.isEmptyRoot()) {
                return root;
            }
            return null;
        }
        if (KeyAnalyzer.isEqualBitKey(bitIndex)) {
            return found;
        }

        // we should have exited above.
        throw new IllegalStateException("invalid lookup: " + key);
    }

    /**
     * Finds the subtree that contains the prefix.
     *
     * This is very similar to getR but with the difference that
     * we stop the lookup if h.bitIndex > lengthInBits.
     */
    TrieEntry<K, V> subtree(final K prefix, final int offsetInBits, final int lengthInBits) {
        TrieEntry<K, V> current = root.left;
        TrieEntry<K, V> path = root;
        while (true) {
            if (current.bitIndex <= path.bitIndex || lengthInBits <= current.bitIndex) {
                break;
            }

            path = current;
            if (!isBitSet(prefix, offsetInBits + current.bitIndex, offsetInBits + lengthInBits)) {
                current = current.left;
            } else {
                current = current.right;
            }
        }

        // Make sure the entry is valid for a subtree.
        final TrieEntry<K, V> entry = current.isEmptyRoot() ? path : current;

        // If entry is root, it can't be empty.
        if (entry.isEmptyRoot()) {
            return null;
        }

        final int endIndexInBits = offsetInBits + lengthInBits;

        // if root && length of root is less than length of lookup,
        // there's nothing.
        // (this prevents returning the whole subtree if root has an empty
        //  string and we want to lookup things with "\0")
        if (entry == root && lengthInBits(entry.getKey()) < endIndexInBits) {
            return null;
        }

        // Found key's length-th bit differs from our key
        // which means it cannot be the prefix...
        if (isBitSet(prefix, endIndexInBits - 1, endIndexInBits)
                != isBitSet(entry.key, lengthInBits - 1, lengthInBits(entry.key))) {
            return null;
        }

        // ... or there are less than 'length' equal bits
        final int bitIndex = getKeyAnalyzer().bitIndex(prefix, offsetInBits, lengthInBits,
                                                       entry.key, 0, lengthInBits(entry.getKey()));

        if (bitIndex >= 0 && bitIndex < lengthInBits) {
            return null;
        }

        return entry;
    }

    /**
     * Returns the last entry the {@link org.apache.commons.collections4.Trie} is storing.
     *
     * <p>This is implemented by going always to the right until
     * we encounter a valid uplink. That uplink is the last key.
     */
    TrieEntry<K, V> lastEntry() {
        return followRight(root.left);
    }

    /**
     * Traverses down the right path until it finds an uplink.
     */
    TrieEntry<K, V> followRight(TrieEntry<K, V> node) {
        // if Trie is empty, no last entry.
        if (node.right == null) {
            return null;
        }

        // Go as far right as possible, until we encounter an uplink.
        while (node.right.bitIndex > node.bitIndex) {
            node = node.right;
        }

        return node.right;
    }

    /**
     * Returns the node lexicographically before the given node (or null if none).
     *
     * This follows four simple branches:
     *  - If the uplink that returned us was a right uplink:
     *      - If predecessor's left is a valid uplink from predecessor, return it.
     *      - Else, follow the right path from the predecessor's left.
     *  - If the uplink that returned us was a left uplink:
     *      - Loop back through parents until we encounter a node where
     *        node != node.parent.left.
     *          - If node.parent.left is uplink from node.parent:
     *              - If node.parent.left is not root, return it.
     *              - If it is root &amp; root isEmptyRoot, return null.
     *              - If it is root &amp; root !isEmptyRoot, return root.
     *          - If node.parent.left is not uplink from node.parent:
     *              - Follow right path for first right child from node.parent.left
     *
     * @param start  the start entry
     */
    TrieEntry<K, V> previousEntry(final TrieEntry<K, V> start) {
        if (start.predecessor == null) {
            throw new IllegalArgumentException("must have come from somewhere!");
        }

        if (start.predecessor.right == start) {
            if (isValidUplink(start.predecessor.left, start.predecessor)) {
                return start.predecessor.left;
            }
            return followRight(start.predecessor.left);
        }
        TrieEntry<K, V> node = start.predecessor;
        while (node.parent != null && node == node.parent.left) {
            node = node.parent;
        }

        if (node.parent == null) { // can be null if we're looking up root.
            return null;
        }

        if (isValidUplink(node.parent.left, node.parent)) {
            if (node.parent.left == root) {
                if (root.isEmptyRoot()) {
                    return null;
                }
                return root;

            }
            return node.parent.left;
        }
        return followRight(node.parent.left);
    }

    /**
     * Returns the entry lexicographically after the given entry.
     * If the given entry is null, returns the first node.
     *
     * This will traverse only within the subtree.  If the given node
     * is not within the subtree, this will have undefined results.
     */
    TrieEntry<K, V> nextEntryInSubtree(final TrieEntry<K, V> node,
            final TrieEntry<K, V> parentOfSubtree) {
        if (node == null) {
            return firstEntry();
        }
        return nextEntryImpl(node.predecessor, node, parentOfSubtree);
    }

    /**
     * Returns true if 'next' is a valid uplink coming from 'from'.
     */
    static boolean isValidUplink(final TrieEntry<?, ?> next, final TrieEntry<?, ?> from) {
        return next != null && next.bitIndex <= from.bitIndex && !next.isEmptyRoot();
    }

    /**
     *  A {@link org.apache.commons.collections4.Trie} is a set of {@link TrieEntry} nodes.
     */
    protected static class TrieEntry<K, V> extends BasicEntry<K, V> {

        private static final long serialVersionUID = 4596023148184140013L;

        /** The index this entry is comparing. */
        protected int bitIndex;

        /** The parent of this entry. */
        protected TrieEntry<K, V> parent;

        /** The left child of this entry.
         * Defaults to self link. */
        protected TrieEntry<K, V> left;

        /** The right child of this entry. */
        protected TrieEntry<K, V> right;

        /** The entry who uplinks to this entry.
         * What do you mean by that.
         * Defaults to self link.
         * Generally points to node that this node replaced in the tree.
         * */
        protected TrieEntry<K, V> predecessor;

        public TrieEntry(final K key, final V value, final int bitIndex) {
            super(key, value);

            this.bitIndex = bitIndex;

            this.parent = null;
            this.left = this;
            this.right = null;
            this.predecessor = this;
        }

        /**
         * Whether or not the entry is storing a key.
         * Only the root can potentially be empty, all other
         * nodes must have a key.
         */
        public boolean isEmptyRoot() {
            return key == null;
        }

        /**
         * Neither the left nor right child is a loopback.
         */
        public boolean isInternalNode() {
            return left != this && right != this;
        }

        /**
         * Either the left or right child is a loopback.
         */
        public boolean isExternalNode() {
            return !isInternalNode();
        }

        @Override
        public String toString() {
            final StringBuilder buffer = new StringBuilder();

            if (bitIndex == -1) {
                buffer.append("RootEntry(");
            } else {
                buffer.append("Entry(");
            }

            buffer.append("key=").append(getKey()).append(" [").append(bitIndex).append("], ");
            buffer.append("value=").append(getValue()).append(", ");
            //buffer.append("bitIndex=").append(bitIndex).append(", ");

            if (parent != null) {
                if (parent.bitIndex == -1) {
                    buffer.append("parent=").append("ROOT");
                } else {
                    buffer.append("parent=").append(parent.getKey()).append(" [").append(parent.bitIndex).append("]");
                }
            } else {
                buffer.append("parent=").append("null");
            }
            buffer.append(", ");

            if (left != null) {
                if (left.bitIndex == -1) {
                    buffer.append("left=").append("ROOT");
                } else {
                    buffer.append("left=").append(left.getKey()).append(" [").append(left.bitIndex).append("]");
                }
            } else {
                buffer.append("left=").append("null");
            }
            buffer.append(", ");

            if (right != null) {
                if (right.bitIndex == -1) {
                    buffer.append("right=").append("ROOT");
                } else {
                    buffer.append("right=").append(right.getKey()).append(" [").append(right.bitIndex).append("]");
                }
            } else {
                buffer.append("right=").append("null");
            }
            buffer.append(", ");

            if (predecessor != null) {
                if (predecessor.bitIndex == -1) {
                    buffer.append("predecessor=").append("ROOT");
                } else {
                    buffer.append("predecessor=").append(predecessor.getKey()).append(" [").
                           append(predecessor.bitIndex).append("]");
                }
            }

            buffer.append(")");
            return buffer.toString();
        }
    }


    /**
     * This is an entry set view of the {@link org.apache.commons.collections4.Trie} as returned by {@link Map#entrySet()}.
     */
    private class EntrySet extends AbstractSet<Map.Entry<K, V>> {

        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return new EntryIterator();
        }

        @Override
        public Spliterator<Entry<K, V>> spliterator() {
            return super.spliterator();
        }

        @Override
        public boolean contains(final Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }

            final TrieEntry<K, V> candidate = getEntry(((Map.Entry<?, ?>) o).getKey());
            return candidate != null && candidate.equals(o);
        }

        @Override
        public boolean remove(final Object obj) {
            if (!(obj instanceof Map.Entry)) {
                return false;
            }
            if (!contains(obj)) {
                return false;
            }
            final Map.Entry<?, ?> entry = (Map.Entry<?, ?>) obj;
            AbstractPatriciaTrie.this.remove(entry.getKey());
            return true;
        }

        @Override
        public int size() {
            return AbstractPatriciaTrie.this.size();
        }

        @Override
        public void clear() {
            AbstractPatriciaTrie.this.clear();
        }

        /**
         * An {@link Iterator} that returns {@link Entry} Objects.
         */
        private class EntryIterator extends TrieIterator<Map.Entry<K, V>> {
            @Override
            public Map.Entry<K, V> next() {
                return nextEntry();
            }
        }
    }

    /**
     * This is a key set view of the {@link org.apache.commons.collections4.Trie} as returned by {@link Map#keySet()}.
     */
    private class KeySet extends AbstractSet<K> {

        @Override
        public Iterator<K> iterator() {
            return new KeyIterator();
        }

        @Override
        public int size() {
            return AbstractPatriciaTrie.this.size();
        }

        @Override
        public boolean contains(final Object o) {
            return containsKey(o);
        }

        @Override
        public boolean remove(final Object o) {
            final int size = size();
            AbstractPatriciaTrie.this.remove(o);
            return size != size();
        }

        @Override
        public void clear() {
            AbstractPatriciaTrie.this.clear();
        }

        /**
         * An {@link Iterator} that returns Key Objects.
         */
        private class KeyIterator extends TrieIterator<K> {
            @Override
            public K next() {
                return nextEntry().getKey();
            }
        }
    }

    /**
     * This is a value view of the {@link org.apache.commons.collections4.Trie} as returned by {@link Map#values()}.
     */
    private class Values extends AbstractCollection<V> {

        @Override
        public Iterator<V> iterator() {
            return new ValueIterator();
        }

        @Override
        public int size() {
            return AbstractPatriciaTrie.this.size();
        }

        @Override
        public boolean contains(final Object o) {
            return containsValue(o);
        }

        @Override
        public void clear() {
            AbstractPatriciaTrie.this.clear();
        }

        @Override
        public boolean remove(final Object o) {
            for (final Iterator<V> it = iterator(); it.hasNext(); ) {
                final V value = it.next();
                if (Objects.equals(value, o)) {
                    it.remove();
                    return true;
                }
            }
            return false;
        }

        /**
         * An {@link Iterator} that returns Value Objects.
         */
        private class ValueIterator extends TrieIterator<V> {
            @Override
            public V next() {
                return nextEntry().getValue();
            }
        }
    }

    /**
     * An iterator for the entries.
     */
    abstract class TrieIterator<E> implements Iterator<E> {

        /** For fast-fail. */
        protected int expectedModCount = AbstractPatriciaTrie.this.modCount;

        protected TrieEntry<K, V> next; // the next node to return
        protected TrieEntry<K, V> current; // the current entry we're on

        /**
         * Starts iteration from the root.
         */
        protected TrieIterator() {
            next = AbstractPatriciaTrie.this.nextEntry(null);
        }

        /**
         * Starts iteration at the given entry.
         */
        protected TrieIterator(final TrieEntry<K, V> firstEntry) {
            next = firstEntry;
        }

        /**
         * Returns the next {@link TrieEntry}.
         */
        protected TrieEntry<K, V> nextEntry() {
            if (expectedModCount != AbstractPatriciaTrie.this.modCount) {
                throw new ConcurrentModificationException();
            }

            final TrieEntry<K, V> e = next;
            if (e == null) {
                throw new NoSuchElementException();
            }

            next = findNext(e);
            current = e;
            return e;
        }

        /**
         * @see PatriciaTrie#nextEntry(TrieEntry)
         */
        protected TrieEntry<K, V> findNext(final TrieEntry<K, V> prior) {
            return AbstractPatriciaTrie.this.nextEntry(prior);
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public void remove() {
            if (current == null) {
                throw new IllegalStateException();
            }

            if (expectedModCount != AbstractPatriciaTrie.this.modCount) {
                throw new ConcurrentModificationException();
            }

            final TrieEntry<K, V> node = current;
            current = null;
            AbstractPatriciaTrie.this.removeEntry(node);

            expectedModCount = AbstractPatriciaTrie.this.modCount;
        }
    }

    /**
     * An {@link OrderedMapIterator} for a {@link org.apache.commons.collections4.Trie}.
     */
    private class TrieMapIterator extends TrieIterator<K> implements OrderedMapIterator<K, V> {

        protected TrieEntry<K, V> previous; // the previous node to return

        protected TrieMapIterator() { }

        protected TrieMapIterator(final TrieEntry<K, V> first) {
            super(first);
        }

        @Override
        public K next() {
            return nextEntry().getKey();
        }

        @Override
        public K getKey() {
            if (current == null) {
                throw new IllegalStateException();
            }
            return current.getKey();
        }

        @Override
        public V getValue() {
            if (current == null) {
                throw new IllegalStateException();
            }
            return current.getValue();
        }

        @Override
        public V setValue(final V value) {
            if (current == null) {
                throw new IllegalStateException();
            }
            return current.setValue(value);
        }

        @Override
        public boolean hasPrevious() {
            return previous != null;
        }

        @Override
        public K previous() {
            return previousEntry().getKey();
        }

        @Override
        protected TrieEntry<K, V> nextEntry() {
            final TrieEntry<K, V> nextEntry = super.nextEntry();
            previous = nextEntry;
            return nextEntry;
        }

        protected TrieEntry<K, V> previousEntry() {
            if (expectedModCount != AbstractPatriciaTrie.this.modCount) {
                throw new ConcurrentModificationException();
            }

            final TrieEntry<K, V> e = previous;
            if (e == null) {
                throw new NoSuchElementException();
            }

            previous = findPrevious(e);
            next = current;
            current = e;
            return current;
        }

        protected TrieEntry<K, V> findPrevious(final TrieEntry<K, V> prior) {
            return AbstractPatriciaTrie.this.previousEntry(prior);
        }
    }

    /**
     * A range view of the {@link org.apache.commons.collections4.Trie}.
     */
    private abstract class RangeMap extends AbstractMap<K, V>
            implements IterableSortedMap<K, V, SubMap> {

        private static final long serialVersionUID = 5837026185237818383L;
        protected SortedMapRange<K> keyRange;

        /** The {@link #entrySet()} view. */
        private transient volatile Set<Map.Entry<K, V>> entrySet;

        private RangeMap(final SortedMapRange<K> keyRange) {
            this.keyRange = keyRange;
        }

        public SortedMapRange<K> getKeyRange() {
            return keyRange;
        }

        protected abstract boolean inRange(K key);

        /**
         * Creates and returns an {@link #entrySet()} view of the {@link RangeMap}.
         */
        protected abstract Set<Map.Entry<K, V>> createEntrySet();

        abstract Iterator<Entry<K,V>> createEntryIterator();

        abstract TrieEntry<K, V> firstEntry();

        abstract TrieEntry<K, V> lastEntry();

        @Override
        public Comparator<? super K> comparator() {
            return AbstractPatriciaTrie.this.comparator();
        }

        @Override
        public boolean containsKey(final Object key) {
            if (!keyRange.inRange(castKey(key))) {
                return false;
            }

            return AbstractPatriciaTrie.this.containsKey(key);
        }

        @Override
        public V remove(final Object key) {
            if (!keyRange.inRange(castKey(key))) {
                return null;
            }

            return AbstractPatriciaTrie.this.remove(key);
        }

        @Override
        public V get(final Object key) {
            if (!keyRange.inRange(castKey(key))) {
                return null;
            }

            return AbstractPatriciaTrie.this.get(key);
        }

        @Override
        public V put(final K key, final V value) {
            if (!keyRange.inRange(key)) {
                throw new IllegalArgumentException("Key is out of range: " + key);
            }
            return AbstractPatriciaTrie.this.put(key, value);
        }

        @Override
        public Set<Map.Entry<K, V>> entrySet() {
            if (entrySet == null) {
                entrySet = createEntrySet();
            }
            return entrySet;
        }

        @Override
        public SubMap subMap(final SortedMapRange<K> range) {
            return createRangeMap(range);
        }

        protected abstract SubMap createRangeMap(SortedMapRange<K> range);
    }

    /**
     * A {@link RangeMap} that deals with {@link Entry}s.
     */
    private class BoundedRangeMap extends RangeMap {

        /**
         * Creates a {@link BoundedRangeMap}.
         */
        protected BoundedRangeMap(final SortedMapRange<K> keyRange) {
            super(keyRange);
        }

        @Override
        TrieEntry<K,V> firstEntry() {
            if (!keyRange.hasFrom()) {
                return AbstractPatriciaTrie.this.firstEntry();
            } else {
                if (keyRange.isFromInclusive()) {
                    return AbstractPatriciaTrie.this.ceilingEntry(keyRange.getFromKey());
                } else {
                    return AbstractPatriciaTrie.this.higherEntry(keyRange.getFromKey());
                }
            }
        }

        @Override
        TrieEntry<K, V> lastEntry() {
            if (!keyRange.hasTo()) {
                return AbstractPatriciaTrie.this.lastEntry();
            } else {
                if (keyRange.isToInclusive()) {
                    return AbstractPatriciaTrie.this.floorEntry(keyRange.getToKey());
                } else {
                    return AbstractPatriciaTrie.this.lowerEntry(keyRange.getToKey());
                }
            }
        }

        TrieEntry<K, V> lastEntryFollowing() {
            if (!keyRange.hasTo()) {
                return null;
            } else {
                if (keyRange.isToInclusive()) {
                    return AbstractPatriciaTrie.this.higherEntry(keyRange.getToKey());
                } else {
                    return AbstractPatriciaTrie.this.ceilingEntry(keyRange.getToKey());
                }
            }
        }

        K lastKeyFollowing() {
            final Map.Entry<K, V> entry = this.lastEntryFollowing();
            if (entry != null)
                return entry.getKey();
            else
                return null;
        }

        @Override
        public K firstKey() {
            final Map.Entry<K, V> entry = this.firstEntry();
            if (entry == null) {
                throw new NoSuchElementException();
            }
            final K firstKey = entry.getKey();
            if (!keyRange.inRange(firstKey)) {
                throw new NoSuchElementException();
            }
            return firstKey;
        }

        @Override
        public K lastKey() {
            final Entry<K, V> entry = this.lastEntry();
            if (entry == null) {
                throw new NoSuchElementException();
            }
            final K lastKey = entry.getKey();
            if (!keyRange.inRange(lastKey)) {
                throw new NoSuchElementException();
            }
            return lastKey;
        }

        @Override
        public K nextKey(final K key) {
            final TrieEntry<K, V> entry = higherEntry(key);
            if (entry != null)
                return entry.getKey();
            else
                return null;
        }

        @Override
        public K previousKey(final K key) {
            final TrieEntry<K, V> entry = lowerEntry(key);
            if (entry != null)
                return entry.getKey();
            else
                return null;
        }

        @Override
        protected Set<Entry<K, V>> createEntrySet() {
            return new BoundedRangeEntrySet(this);
        }

        @Override
        Iterator<Entry<K, V>> createEntryIterator() {
            return new BoundedRangeEntryIterator();
        }

        @Override
        public OrderedMapIterator<K, V> mapIterator() {
            return new RangeMapIterator(this.firstEntry());
        }

        @Override
        protected SubMap createRangeMap(final SortedMapRange<K> keyRange) {
            return (SubMap) new BoundedRangeMap(keyRange);
        }

        @Override
        protected boolean inRange(final K key) {
            return keyRange.inRange(key);
        }

        private final class RangeMapIterator extends TrieMapIterator {
            private RangeMapIterator(final TrieEntry<K, V> first) {
                super(first);
            }

            @Override
            public boolean hasNext() {
                return super.hasNext();
            }

            @Override
            public boolean hasPrevious() {
                return super.hasPrevious();
            }

            @Override
            public K next() {
                return super.next();
            }

            @Override
            public K previous() {
                return super.previous();
            }

            // TODO
        }

        /**
         * An {@link Iterator} for {@link BoundedRangeEntrySet}s.
         */
        private final class BoundedRangeEntryIterator extends TrieIterator<Map.Entry<K, V>> {
            private final K afterLast;

            /**
             * Creates a {@link BoundedRangeEntryIterator}.
             */
            private BoundedRangeEntryIterator() {
                super(BoundedRangeMap.this.firstEntry());
                this.afterLast = BoundedRangeMap.this.lastKeyFollowing();
            }

            @Override
            protected TrieEntry<K, V> findNext(final TrieEntry<K, V> prior) {
                final TrieEntry<K, V> candidate = super.findNext(prior);
                if (equalKeys(candidate.key, afterLast)) {
                    return null;
                } else {
                    return candidate;
                }
            }

            @Override
            public Entry<K, V> next() {
                return nextEntry();
            }
        }
    }

    /**
     * A {@link Set} view of a {@link RangeMap}.
     */
    private class BoundedRangeEntrySet extends AbstractSet<Map.Entry<K, V>> {

        private final RangeMap delegate;

        private transient int rangeSize = -1;

        private transient int expectedModCount;

        /**
         * Creates a {@link BoundedRangeEntrySet}.
         */
        BoundedRangeEntrySet(final RangeMap delegate) {
            this.delegate = Objects.requireNonNull(delegate, "delegate");
        }

        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return delegate.createEntryIterator();
        }

        @Override
        public int size() {
            if (rangeSize == -1 || expectedModCount != AbstractPatriciaTrie.this.modCount) {
                rangeSize = IteratorUtils.size(iterator());
                expectedModCount = AbstractPatriciaTrie.this.modCount;
            }
            return rangeSize;
        }

        @Override
        public boolean isEmpty() {
            return !iterator().hasNext();
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean contains(final Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }

            final Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
            final K key = entry.getKey();
            if (!delegate.keyRange.inRange(key)) {
                return false;
            }

            final TrieEntry<K, V> node = getEntry(key);
            if (node == null) return false;
            return Objects.equals(node.getValue(), entry.getValue());
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean remove(final Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }

            final Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
            final K key = entry.getKey();
            if (!delegate.keyRange.inRange(key)) {
                return false;
            }

            final TrieEntry<K, V> node = getEntry(key);
            if (node != null) {
                if (Objects.equals(node.getValue(), entry.getValue())) {
                    removeEntry(node);
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * A submap used for prefix views over the {@link org.apache.commons.collections4.Trie}.
     */
    private final class PrefixRangeMap extends RangeMap {

        private final K prefix;

        private final int offsetInBits;

        private final int lengthInBits;

        private transient int expectedModCount;

        private TrieEntry<K, V> prefixSubTree;

        private int rangeSize = -1;


        /**
         * Creates a {@link PrefixRangeMap}.
         */
        private PrefixRangeMap(final K prefix, final int offsetInBits, final int lengthInBits) {
            super(null);
            this.prefix = prefix;
            this.offsetInBits = offsetInBits;
            this.lengthInBits = lengthInBits;
            updateRange();
        }

        @Override
        protected SubMap createRangeMap(SortedMapRange<K> range) {
            return null;
        }

        private void updateRangeIfNeeded() {
            // The trie has changed since we last found our toKey / fromKey
            if (rangeSize == -1 || AbstractPatriciaTrie.this.modCount != expectedModCount) {
                updateRange();
            }
        }

        /**
         * This method does two things. It determines the FROM
         * and TO range of the {@link PrefixRangeMap} and the number
         * of elements in the range. This method must be called every
         * time the {@link org.apache.commons.collections4.Trie} has changed.
         */
        private void updateRange() {
            final SortedMapRange<K> parentRange = AbstractPatriciaTrie.this.getKeyRange();

            prefixSubTree = subtree(prefix, offsetInBits, lengthInBits);

            if (prefixSubTree == null) {
                rangeSize = 0;
                keyRange = SortedMapRange.empty();
            } else if (lengthInBits > prefixSubTree.bitIndex) {
                rangeSize = 0;
                keyRange = parentRange.subRange(prefixSubTree.key, true, prefixSubTree.key, true);
            } else {
                final TrieEntry<K, V> firstEntry = followLeft(prefixSubTree);
                final PrefixEntryIterator iterator = new PrefixEntryIterator(firstEntry);
                rangeSize = IteratorUtils.size(iterator);
                keyRange = parentRange.subRange(firstEntry.key, true, prefixSubTree.key, true);
            }

            expectedModCount = AbstractPatriciaTrie.this.modCount;
        }

        @Override
        public int size() {
            updateRangeIfNeeded();
            return rangeSize;
        }

        @Override
        TrieEntry<K, V> firstEntry() {
            updateRangeIfNeeded();
            return followLeft(prefixSubTree);
        }

        @Override
        TrieEntry<K, V> lastEntry() {
            updateRangeIfNeeded();
            return prefixSubTree;
        }

        @Override
        public K firstKey() {
            final TrieEntry<K, V> e = firstEntry();
            final K first = e != null ? e.getKey() : null;
            if (e == null || !getKeyAnalyzer().isPrefix(prefix, offsetInBits, lengthInBits, first)) {
                throw new NoSuchElementException();
            }
            return first;
        }

        @Override
        public K lastKey() {
            final Map.Entry<K, V> e = lastEntry();
            final K last = e != null ? e.getKey() : null;
            if (e == null || !getKeyAnalyzer().isPrefix(prefix, offsetInBits, lengthInBits, last)) {
                throw new NoSuchElementException();
            }
            return last;
        }

        @Override
        public K nextKey(final K key) {
            updateRangeIfNeeded();
            final TrieEntry<K, V> entry = getEntry(key);
            if (entry != null) {
                final TrieEntry<K, V> next = nextEntryInSubtree(entry, prefixSubTree);
                if (next != null) {
                    return next.getKey();
                }
            }
            return null;
        }

        @Override
        public K previousKey(K key) {

            return null;
        }

        /**
         * Returns true if this {@link PrefixRangeMap}'s key is a prefix of the provided key.
         */
        @Override
        protected boolean inRange(final K key) {
            updateRangeIfNeeded();
            return getKeyAnalyzer().isPrefix(prefix, offsetInBits, lengthInBits, key);
        }

        @Override
        protected Set<Map.Entry<K, V>> createEntrySet() {
            updateRangeIfNeeded();
            return new PrefixRangeEntrySet(this);
        }

        @Override
        Iterator<Entry<K, V>> createEntryIterator() {
            updateRangeIfNeeded();
            if (prefixSubTree == null) {
                return IteratorUtils.emptyIterator();
            } else if (lengthInBits > prefixSubTree.bitIndex) {
                return new PrefixSingletonIterator(prefixSubTree);
            } else {
                return new PrefixEntryIterator(followLeft(prefixSubTree));
            }
        }

        @Override
        public OrderedMapIterator<K, V> mapIterator() {
            return null;
        }

        @Override
        public void clear() {
            updateRangeIfNeeded();
            AbstractPatriciaTrie.this.keySet().removeIf(this::inRange);
        }

        /**
         * An {@link Iterator} that holds a single {@link TrieEntry}.
         */
        private final class PrefixSingletonIterator extends SingletonIterator<Map.Entry<K, V>> {
            PrefixSingletonIterator(final TrieEntry<K, V> entry) {
                super(entry, true);
            }

            @Override
            public void remove() {
                final Entry<K, V> entry = super.getObject();
                super.remove();
                AbstractPatriciaTrie.this.removeEntry((TrieEntry<K, V>) entry);
            }
        }

        /**
         * An {@link Iterator} for iterating over a prefix search.
         */
        private final class PrefixEntryIterator extends TrieIterator<Map.Entry<K, V>> {
            private boolean lastOne;

            /**
             * Starts iteration at the given entry &amp; search only
             * within the given subtree.
             */
            PrefixEntryIterator(final TrieEntry<K, V> first) {
                next = first;
            }

            @Override
            public Map.Entry<K, V> next() {
                final Map.Entry<K, V> entry = nextEntry();
                if (lastOne) {
                    next = null;
                }
                return entry;
            }

            @Override
            protected TrieEntry<K, V> findNext(final TrieEntry<K, V> prior) {
                return nextEntryInSubtree(prior, prefixSubTree);
            }

            @Override
            public void remove() {
                // If the current entry we're removing is the subtree
                // then we need to find a new subtree parent.
                boolean needsFixing = false;
                final int bitIdx = prefixSubTree.bitIndex;
                if (current == prefixSubTree) {
                    needsFixing = true;
                }

                super.remove();

                // If the subtree changed its bitIndex or we
                // removed the old subtree, get a new one.
                if (bitIdx != prefixSubTree.bitIndex || needsFixing) {
                    updateRange();
                }

                // If the subtree's bitIndex is less than the
                // length of our prefix, it's the last item
                // in the prefix tree.
                if (lengthInBits >= prefixSubTree.bitIndex) {
                    lastOne = true;
                }
            }
        }


        /**
         * A prefix {@link BoundedRangeEntrySet} view of the {@link org.apache.commons.collections4.Trie}.
         */
        private final class PrefixRangeEntrySet extends BoundedRangeEntrySet {
            PrefixRangeEntrySet(final PrefixRangeMap delegate) {
                super(delegate);
            }

            @Override
            public int size() {
                return PrefixRangeMap.this.size();
            }

            @Override
            public Iterator<Map.Entry<K, V>> iterator() {
                return PrefixRangeMap.this.createEntryIterator();
            }
        }
    }



    /**
     * Reads the content of the stream.
     */
    @SuppressWarnings("unchecked") // This will fail at runtime if the stream is incorrect
    private void readObject(final ObjectInputStream stream) throws IOException, ClassNotFoundException{
        stream.defaultReadObject();
        root = new TrieEntry<>(null, null, -1);
        final int size = stream.readInt();
        for (int i = 0; i < size; i++){
            final K k = (K) stream.readObject();
            final V v = (V) stream.readObject();
            put(k, v);
        }
    }

    /**
     * Writes the content to the stream for serialization.
     */
    private void writeObject(final ObjectOutputStream stream) throws IOException{
        stream.defaultWriteObject();
        stream.writeInt(this.size());
        for (final Entry<K, V> entry : entrySet()) {
            stream.writeObject(entry.getKey());
            stream.writeObject(entry.getValue());
        }
    }

}
