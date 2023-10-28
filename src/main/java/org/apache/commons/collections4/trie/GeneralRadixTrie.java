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
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.commons.collections4.AbstractCommonsCollection;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableSortedMap;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.ResettableIterator;
import org.apache.commons.collections4.SortedMapRange;
import org.apache.commons.collections4.ToArrayUtils;
import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.iterators.TransformIterator;
import org.apache.commons.collections4.keyvalue.AbstractMapEntry;
import org.apache.commons.collections4.map.AbstractIterableMapAlternate;
import org.apache.commons.collections4.set.AbstractMapViewSortedSet;
import org.apache.commons.collections4.spliterators.AbstractTreeSpliterator;
import org.apache.commons.collections4.spliterators.MapSpliterator;
import org.apache.commons.collections4.spliterators.TransformSpliterator;

// https://en.wikipedia.org/wiki/Radix_tree
public class GeneralRadixTrie<K extends Comparable<K>, V extends Comparable<V>>
        extends AbstractIterableMapAlternate<K, V>
        implements Trie<K, V, IterableSortedMap<K, V, ?>> {
    private static final long serialVersionUID = -1993317552691676845L;

    private transient SortedMapRange<K> keyRange;
    private transient KeyAnalyzer<K> keyAnalyzer;
    private transient final TEntry<K, V> root;
    private transient int size;
    private transient int modCount;

    private transient Set<K> keySet;
    private transient Collection<V> values;
    private transient Set<Map.Entry<K, V>> entrySet;

    public GeneralRadixTrie(final KeyAnalyzer<K> keyAnalyzer) {
        this.keyAnalyzer = keyAnalyzer;
        this.keyRange = SortedMapRange.full(keyAnalyzer);
        this.root = TEntry.makeRoot();
        this.size = 0;
        this.modCount = 0;
    }

    @SuppressWarnings("unchecked")
    protected final K castKey(final Object key) {
        return (K) Objects.requireNonNull(key);
    }

    @SuppressWarnings("unchecked")
    protected final V castValue(final Object value) {
        return (V) value;
    }

    protected boolean valueEquals(final V a, final V b) {
        return Objects.equals(a, b);
    }

    protected TEntry<K, V> getEffectiveRoot() {
        if (size == 0 || !root.isEmptyRoot())
            return root;
        else
            return root.right;
    }

    /**
     * A utility method for calling {@link KeyAnalyzer#compare(Comparable, Comparable)}
     */
    protected boolean equalKeys(final K key, final K other) {
        if (key == null) {
            return other == null;
        }
        if (other == null) {
            return false;
        }

        return keyAnalyzer.compare(key, other) == 0;
    }

    @Override
    public int size() {
        return size;
    }

    /**
     * A helper method to increment the {@link org.apache.commons.collections4.Trie} size and the modification counter.
     */
    protected void incrementSize() {
        size++;
        incrementModCount();
    }

    /**
     * A helper method to decrement the {@link org.apache.commons.collections4.Trie} size and increment the modification counter.
     */
    protected void decrementSize() {
        size--;
        incrementModCount();
    }

    /**
     * A helper method to increment the modification counter.
     */
    protected void incrementModCount() {
        ++modCount;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public V getOrDefault(final Object key, final V defaultValue) {
        if (!(key instanceof Comparable<?>))
            return null;
        final TEntry<K, V> entry = getEntry(castKey(key));
        if (entry != null)
            return entry.getValue();
        else
            return defaultValue;
    }

    @Override
    public boolean containsKey(final Object key) {
        if (!(key instanceof Comparable<?>))
            return false;
        final TEntry<K, V> entry = getEntry(castKey(key));
        return entry != null;
    }

    @Override
    public boolean containsEntry(final Object key, final Object valueObject) {
        if (!(key instanceof Comparable<?>))
            return false;
        final V value = castValue(valueObject);
        final TEntry<K, V> entry = getEntry(castKey(key));
        return entry != null && valueEquals(value, entry.getValue());
    }

    @Override
    public K firstKey() {
        final TEntry<K, V> first = _firstEntry();
        return first != null ? first.getKey() : null;
    }

    @Override
    public K lastKey() {
        final TEntry<K, V> last = _lastEntry();
        return last != null ? last.getKey() : null;
    }

    private TEntry<K, V> _firstEntry() {
        return getEffectiveRoot().lowestGrandchild();
    }

    private TEntry<K, V> _lastEntry() {
        return getEffectiveRoot().highestGrandchild();
    }

    @Override
    public Entry<K, V> firstEntry() {
        final TEntry<K, V> entry = _firstEntry();
        if (entry != null) {
            return entry.toUnmodifiableEntry();
        } else {
            return null;
        }
    }

    @Override
    public Entry<K, V> lastEntry() {
        final TEntry<K, V> entry = _lastEntry();
        if (entry != null) {
            return entry.toUnmodifiableEntry();
        } else {
            return null;
        }
    }

    @Override
    public K nextKey(final K key) {
        final TEntry<K, V> entry = getRelativeEntry(key, true, false);
        return entry != null ? entry.getKey() : null;
    }

    @Override
    public K previousKey(final K key) {
        final TEntry<K, V> entry = getRelativeEntry(key, false, false);
        return entry != null ? entry.getKey() : null;
    }

    @Override
    protected V doPut(final K key, final V value, final boolean addIfAbsent, final boolean updateIfPresent) {
        final int keyLengthInBits = keyAnalyzer.lengthInBits(key);
        if (keyLengthInBits == 0) {
            return doPutRoot(key, value, addIfAbsent, updateIfPresent);
        }

        TEntry<K, V> node = root.right, path = root;
        boolean lastWasRight = true;
        int lastDiffBit = -1;
        int startIndex = 0;
        while (true) {
            final K nodeKey = node.getKey();
            final int diffBit = keyAnalyzer.bitIndex(
                    key, startIndex, keyLengthInBits - startIndex,
                    nodeKey, startIndex, keyAnalyzer.lengthInBits(nodeKey) - startIndex);

            if (KeyAnalyzer.isEqualBitKey(diffBit)) {
                if (updateIfPresent) {
                    incrementModCount();
                    return node.setValue(value);
                } else {
                    return node.getValue();
                }
            } else if (KeyAnalyzer.isNullBitKey(diffBit)) {
                assert node == root;
                return doPutRoot(key, value, addIfAbsent, updateIfPresent);
            } else if (size == 0 || (size == 1 && !root.isEmptyRoot())) {
                if (addIfAbsent) {
                    root.right = new TEntry<>(key, value, diffBit, root, root, root);
                    incrementSize();
                }
                return null;
            } else if (diffBit < node.diffBit) {
                // need to split before we get here, attached to path
                if (addIfAbsent) {
                    doPutOnPath(key, value, keyLengthInBits, node, path, diffBit, lastWasRight);
                }
                return null;
            } else if (node == path) {
                throw new IllegalStateException("not doing self links anymore?");
//                if (addIfAbsent) {
//                    doPutLeaf(key, value, node, path, diffBit);
//                }
//                return null;
            } else if (node.diffBit <= path.diffBit) {
                // we've hit a self-link or uplink
                if (addIfAbsent) {
                    assert lastDiffBit != -1;
                    doPutLeaf(key, value, node, path, lastDiffBit, lastWasRight);
                }
                return null;
//            } else if (diffBit == node.diffBit) {
//                // may be able to keep going down
//                return null;
            } else if (!keyAnalyzer.isBitSet(key, diffBit, keyLengthInBits)) {
                // key < node
                path = node;
                node = node.left;
                lastWasRight = false;
            } else {
                // key > node
                path = node;
                node = node.right;
                lastWasRight = true;
            }

            lastDiffBit = diffBit;
            startIndex = diffBit + 1;
        }
    }

    private V doPutRoot(final K key, final V value, final boolean addIfAbsent, final boolean updateIfPresent) {
        if (root.isEmptyRoot() && addIfAbsent) {
            incrementSize();
            return root.setKeyValue(key, value);
        } else if (!root.isEmptyRoot() && updateIfPresent) {
            incrementModCount();
            return root.setKeyValue(key, value);
        } else {
            return root.getValue();
        }
    }

    private void doPutOnPath(final K key, final V value, final int keyLengthInBits, final TEntry<K, V> node, final TEntry<K, V> path, final int diffBit, final boolean lastWasRight) {
        final TEntry<K, V> add = new TEntry<>(key, value, diffBit, path, null, null);

        if (lastWasRight) {
            path.right = add;
        } else {
            path.left = add;
        }

        if (keyAnalyzer.isBitSet(key, diffBit, keyLengthInBits)) {
            // node < add
            add.left = node;
            add.right = node.right;
            node.right = add;
        } else {
            // node > add
            add.right = node;
            add.left = node.left;
            node.left = add;
        }

        // might we want to copy an uplink too?
        node.parent = add;
        incrementSize();
    }

    private void doPutLeaf(final K key, final V value, final TEntry<K, V> node, final TEntry<K, V> path, final int diffBit, final boolean lastWasRight) {
        final TEntry<K, V> add = new TEntry<>(key, value, diffBit, path, null, null);
        //assert path.left != node || path.right != node; // not sure if true
        if (lastWasRight) {
            path.right = add;
            add.left = path;
            add.right = node;
        } else {
            path.left = add;
            add.right = path;
            add.left = node;
        }
        incrementSize();
    }

    @Override
    protected V doPut(K key, Function<? super K, ? extends V> absentFunc, BiFunction<? super K, ? super V, ? extends V> presentFunc, boolean saveNulls) {
        // TODO
        return null;
    }

    @Override
    public boolean removeAsBoolean(Object key) {
        // TODO
        return false;
    }

    @Override
    public boolean remove(Object key, Object value) {
        // TODO
        return false;
    }

    protected void removeEntry(final TEntry<K,V> entry) {
        final TEntry<K, V> parent = entry.parent, left = entry.left, right = entry.right;
        assert parent.left == entry || parent.right == entry;
        assert parent.left != entry || parent.right != entry;
        assert left != right;
        assert entry != root;

//        final TEntry<K, V> leftOuterLink = left.lowestUplink();
//        final TEntry<K, V> leftInnerLink = left.highestUplink();
//        final TEntry<K, V> rightInnerLink = right.lowestUplink();
//        final TEntry<K, V> rightOuterLink = right.highestUplink();

        final TEntry<K, V> leftOuterNode = left.lowestGrandchild();
        final TEntry<K, V> leftInnerNode = left.highestGrandchild();
        final TEntry<K, V> rightInnerNode = right.lowestGrandchild();
        final TEntry<K, V> rightOuterNode = right.highestGrandchild();

        assert leftInnerNode.right == entry;
        assert rightInnerNode.left == entry;

        TEntry<K, V> replacement = null;
        if (entry == entry.left) {
            replacement = entry.right;
        } else if (entry == entry.right) {
            replacement = entry.left;
        }

        if (parent.left == entry) {
            parent.left = replacement;
        } else {
            parent.right = replacement;
        }
        // TODO recalculate replacement's indexes


        entry.setKeyValue(null, null);
        entry.left = null;
        entry.right = null;
        entry.parent = null;

        decrementSize();
    }

    @Override
    public V remove(final Object keyObject) {
        if (!(keyObject instanceof Comparable<?>))
            return null;
        final TEntry<K, V> entry = getEntry(castKey(keyObject));
        if (entry != null) {
            removeEntry(entry);
            return entry.getValue();
        } else {
            return null;
        }
    }

    @Override
    public void clear() {
        root.left = root;
        root.right = root;
        root.setKeyValue(null, null);
        size = 0;
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        // TODO
        Trie.super.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        // TODO
        Trie.super.replaceAll(function);
    }

    @Override
    public SortedMapRange<K> getKeyRange() {
        return keyRange;
    }

    @Override
    public IterableSortedMap<K, V, ?> prefixMap(K key) {
        return null;
    }

    @Override
    public IterableSortedMap<K, V, ?> subMap(final SortedMapRange<K> range) {
        return null;
    }

    @Override
    protected Set<K> createKeySet() {
        return new TrieKeySet<>(this);
    }

    @Override
    protected Set<Entry<K, V>> createEntrySet() {
        return new TrieEntrySet<>(this);
    }

    @Override
    protected Collection<V> createValuesCollection() {
        return new TrieValues<>(this);
    }

    @Override
    public OrderedMapIterator<K, V> mapIterator() {
        return new TrieMapIterator<>(this);
    }

    @Override
    public MapSpliterator<K, V> mapSpliterator() {
        return null;
    }

    @Override
    public Comparator<? super K> comparator() {
        return keyAnalyzer;
    }

    private TEntry<K, V> getEntry(K key) {
        final int keyLengthInBits = keyAnalyzer.lengthInBits(key);
        if (keyLengthInBits == 0) {
            return root.isEmptyRoot() ? null : root;
        }

        TEntry<K, V> node = getEffectiveRoot(), path = null;
        int startIndex = 0;
        while (true) {
            final K nodeKey = node.getKey();
            final int diffBit = keyAnalyzer.bitIndex(
                    key, startIndex, keyLengthInBits - startIndex,
                    nodeKey, startIndex, keyAnalyzer.lengthInBits(nodeKey) - startIndex);

            if (KeyAnalyzer.isEqualBitKey(diffBit)) {
                return node;
            } else if (KeyAnalyzer.isNullBitKey(diffBit)) {
                return node.isEmptyRoot() ? null : node;
            } else if (diffBit <= node.diffBit) {
                // if we aren't equal by this point we've gone too far
                return null;
            } else if (path != null && node.diffBit <= path.diffBit) {
                // we've hit a self-link or uplink
                return null;
            } else if (!keyAnalyzer.isBitSet(key, diffBit, keyLengthInBits)) {
                // zero bit means key < node
                path = node;
                node = node.left;
            } else {
                path = node;
                node = node.right;
            }

            startIndex = diffBit + 1;
        }
    }

    private TEntry<K, V> getRelativeEntry(K key, boolean higher, boolean includeEqual) {
        if (size == 0)
            return null;

        final int keyLengthInBits = keyAnalyzer.lengthInBits(key);
        if (keyLengthInBits == 0 && includeEqual) {
            if (!root.isEmptyRoot())
                return root;
        }

        TEntry<K, V> node = getEffectiveRoot(), path = null, nearest = null;
        int startIndex = 0;
        while (true) {
            final K nodeKey = node.getKey();
            final int diffBit = keyAnalyzer.bitIndex(
                    key, startIndex, keyLengthInBits - startIndex,
                    nodeKey, startIndex, keyAnalyzer.lengthInBits(nodeKey) - startIndex);

            if (KeyAnalyzer.isEqualBitKey(diffBit) && includeEqual) {
                return node;
            } else if (KeyAnalyzer.isNullBitKey(diffBit) && !node.isEmptyRoot() && includeEqual) {
                return node;
            } else if (diffBit <= node.diffBit) {
                return nearest;
            } else if (path != null && node.diffBit >= path.diffBit) {
                return nearest;
            } else if (!keyAnalyzer.isBitSet(key, diffBit, keyLengthInBits)) {
                // node > key
                if (higher) {
                    nearest = node;
                }
                path = node;
                node = node.left;
            } else {
                // node < key
                if (!higher) {
                    nearest = node;
                }
                path = node;
                node = node.right;
            }

            startIndex = diffBit + 1;
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(keyAnalyzer);
        out.writeObject(keyRange);
        MapUtils.writeExternal(out, size, mapIterator());
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        keyAnalyzer = (KeyAnalyzer<K>) in.readObject();
        keyRange = (SortedMapRange<K>) in.readObject();
        putAll(MapUtils.readExternal(in));
    }

    private static final class TEntry<K, V> extends AbstractMapEntry<K, V> {
        private int diffBit;
        private TEntry<K, V> parent;
        private TEntry<K, V> left;
        private TEntry<K, V> right;

        private TEntry(K key, V value, int diffBit, TEntry<K, V> parent, TEntry<K, V> left, TEntry<K, V> right) {
            super(key, value);
            this.diffBit = diffBit;
            this.parent = parent != null ? parent : this;
            this.left = left != null ? left : this;
            this.right = right != null ? right : this;
        }

        public static <K, V> TEntry<K,V> makeRoot() {
            return new TEntry<>(null, null, -1, null, null, null);
        }

        public boolean isRoot() {
            return parent == this;
        }

        public boolean isEmptyRoot() {
            return getKey() == null;
        }

        public V setKeyValue(K key, V value) {
            setKey(key);
            return setValue(value);
        }

        public TEntry<K, V> lowestGrandchild() {
            TEntry<K, V> path = this, child = path.left;
            while (path.diffBit < child.diffBit) {
                path = child;
                child = child.left;
            }
            return path;
        }

        public TEntry<K, V> lowestUplink() {
            TEntry<K, V> path = this, child = path.left;
            while (path.diffBit < child.diffBit) {
                path = child;
                child = child.left;
            }
            return child;
        }

        public TEntry<K,V> highestGrandchild() {
            TEntry<K, V> path = this, child = path.right;
            while (path.diffBit < child.diffBit) {
                path = child;
                child = child.right;
            }
            return path;
        }

        public TEntry<K, V> highestUplink() {
            TEntry<K, V> path = this, child = path.right;
            while (path.diffBit < child.diffBit) {
                path = child;
                child = child.right;
            }
            return child;
        }

        private TEntry<K,V> next() {
            //assert right.diffBit != diffBit;
            if (right.isRoot()) {
                return null;
            } else if (right.diffBit > diffBit) { // is deeper
                return right.lowestGrandchild();
            } else { // uplink into higher in the tree
                return right;
            }
        }

        private TEntry<K,V> prev() {
            // assert left.diffBit != diffBit;
            if (isRoot() || left.isEmptyRoot()) {
                return null;
            } else if (left.diffBit > diffBit) { // is deeper
                return left.highestGrandchild();
            } else { // uplink into higher in the tree
                return left;
            }
        }
    }

    private static class TrieMapIterator<K extends Comparable<K>, V extends Comparable<V>>
            implements OrderedMapIterator<K, V>, ResettableIterator<K> {
        private final GeneralRadixTrie<K, V> parent;
        private TEntry<K, V> current;
        private TEntry<K, V> nextEntry;
        private TEntry<K, V> previousEntry;

        public TrieMapIterator(final GeneralRadixTrie<K, V> parent) {
            this.parent = parent;
            reset();
        }

        @Override
        public void reset() {
            nextEntry = parent._firstEntry();
            previousEntry = null;
        }

        @Override
        public boolean hasNext() {
            return nextEntry != null;
        }

        @Override
        public boolean hasPrevious() {
            return previousEntry != null;
        }

        @Override
        public K next() {
            if (nextEntry == null)
                throw new NoSuchElementException();
            current = nextEntry;
            nextEntry = nextEntry.next();
            previousEntry = current;
            return current.getKey();
        }

        @Override
        public K previous() {
            if (previousEntry == null)
                throw new NoSuchElementException();
            current = previousEntry;
            previousEntry = previousEntry.prev();
            nextEntry = current;
            return current.getKey();
        }

        @Override
        public K getKey() {
            if (current == null)
                throw new IllegalStateException();
            return current.getKey();
        }

        @Override
        public V getValue() {
            if (current == null)
                throw new IllegalStateException();
            return current.getValue();
        }

        @Override
        public void remove() {
            if (current == null)
                throw new IllegalStateException();
            parent.removeEntry(current);
            current = null;
        }

        @Override
        public V setValue(V value) {
            parent.incrementModCount();
            return current.setValue(value);
        }
    }

    private static final class TrieEntryIterator<K extends Comparable<K>, V extends Comparable<V>>
            implements ResettableIterator<Entry<K, V>> {
        private final GeneralRadixTrie<K, V> parent;
        private TEntry<K, V> current;
        private TEntry<K, V> nextEntry;

        private TrieEntryIterator(final GeneralRadixTrie<K, V> parent, SortedMapRange<Entry<K,V>> range) {
            this.parent = parent;
            if (!range.isFull())
                throw new IllegalStateException();
            reset();
        }

        @Override
        public void reset() {
            nextEntry = parent._firstEntry();
        }

        @Override
        public boolean hasNext() {
            return nextEntry != null;
        }

        @Override
        public Entry<K, V> next() {
            if (nextEntry == null)
                throw new NoSuchElementException();
            current = nextEntry;
            nextEntry = nextEntry.next();
            return current;
        }

        @Override
        public void remove() {
            ResettableIterator.super.remove();
        }

        @Override
        public void forEachRemaining(Consumer<? super Entry<K, V>> action) {
            ResettableIterator.super.forEachRemaining(action);
        }
    }

    private static class TrieEntrySpliterator<K extends Comparable<K>, V extends Comparable<V>>
            extends AbstractTreeSpliterator<K, V, TEntry<K, V>> {
        private final GeneralRadixTrie<K, V> parent;

        TrieEntrySpliterator(final GeneralRadixTrie<K, V> parent, final SortedMapRange<Entry<K,V>> range) {
            this.parent = parent;
            if (!range.isFull())
                throw new IllegalArgumentException();
        }

        TrieEntrySpliterator(final GeneralRadixTrie<K, V> parent, final SplitState state, final TEntry<K, V> currentNode, final TEntry<K, V> lastNode, final long estimatedSize) {
            super(state, currentNode, lastNode, estimatedSize);
            this.parent = parent;
        }

        @Override
        protected AbstractTreeSpliterator<K, V, TEntry<K, V>> makeSplit(final SplitState state, final TEntry<K, V> currentNode, final TEntry<K, V> lastNode, final long estimatedSize) {
            return new TrieEntrySpliterator<>(parent, state, currentNode, lastNode, estimatedSize);
        }

        @Override
        protected int modCount() {
            return parent.modCount;
        }

        @Override
        protected TEntry<K, V> rootNode() {
            if (parent.isEmpty())
                return null;
            else
                return parent.getEffectiveRoot();
        }

        @Override
        protected TEntry<K, V> getLeft(final TEntry<K, V> node) {
            return node.left;
        }

        @Override
        protected TEntry<K, V> getRight(final TEntry<K, V> node) {
            return node.right;
        }

        @Override
        protected TEntry<K, V> nextLower(final TEntry<K, V> node) {
            return node.prev();
        }

        @Override
        protected TEntry<K, V> nextGreater(final TEntry<K, V> node) {
            return node.next();
        }

        @Override
        protected TEntry<K, V> subTreeLowest(final TEntry<K, V> node) {
            return node.lowestGrandchild();
        }

        @Override
        protected TEntry<K, V> subTreeGreatest(final TEntry<K, V> node) {
            return node.highestGrandchild();
        }

        @Override
        protected boolean isLowerThan(final TEntry<K, V> node, final TEntry<K, V> other) {
            return parent.comparator().compare(node.getKey(), other.getKey()) < 0;
        }

        @Override
        protected boolean isLowerThanOrEqual(final TEntry<K, V> node, final TEntry<K, V> other) {
            return parent.comparator().compare(node.getKey(), other.getKey()) <= 0;
        }
    }

    private abstract static class TrieSetView<E, K extends Comparable<K>, V extends Comparable<V>>
            extends AbstractMapViewSortedSet<E, TrieSetView<E, K, V>> {
        protected final GeneralRadixTrie<K, V> parent;

        protected TrieSetView(final GeneralRadixTrie<K, V> parent, final SortedMapRange<E> range) {
            super(range);
            this.parent = parent;
        }

        @Override
        public int size() {
            return parent.size();
        }

        @Override
        public boolean isEmpty() {
            return parent.isEmpty();
        }

        @Override
        public void clear() {
            parent.clear();
        }
    }

    private static class TrieEntrySet<K extends Comparable<K>, V extends Comparable<V>>
            extends TrieSetView<Entry<K, V>, K, V>
            implements Set<Entry<K, V>> {
        private TrieEntrySet(final GeneralRadixTrie<K, V> parent, final SortedMapRange<Entry<K, V>> range) {
            super(parent, range);
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new TrieEntryIterator<>(parent, range);
        }

        @Override
        public Spliterator<Entry<K, V>> spliterator() {
            return new TrieEntrySpliterator<>(parent, range);
        }

        protected TEntry<K, V> findMatchingEntry(final Object obj) {
            if (!(obj instanceof Map.Entry)) {
                return null;
            }

            final Map.Entry<?, ?> entry = (Entry<?, ?>) obj;
            final Object keyObject = entry.getKey();
            if (!(keyObject instanceof Comparable)) {
                return null;
            }

            final K key = parent.castKey(keyObject);
            final TEntry<K, V> mapEntry = parent.getEntry(key);
            if (mapEntry != null && Objects.equals(mapEntry.getValue(), entry.getValue())) {
                return mapEntry;
            } else {
                return null;
            }
        }

        @Override
        public boolean contains(final Object obj) {
            final TEntry<K, V> entry = findMatchingEntry(obj);
            return entry != null;
        }

        @Override
        public boolean remove(final Object obj) {
            final TEntry<K, V> entry = findMatchingEntry(obj);
            if (entry != null) {
                parent.removeEntry(entry);
                return true;
            }
            return false;
        }

        @Override
        public Object[] toArray() {
            return ToArrayUtils.fromEntryCollectionUnmodifiable(this);
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return ToArrayUtils.fromEntryCollectionUnmodifiable(this, a);
        }
    }

    private static class TrieKeySet<K extends Comparable<K>, V extends Comparable<V>>
            extends TrieSetView<K, K, V>
            implements Set<K> {
        public TrieKeySet(final GeneralRadixTrie<K, V> parent, final SortedMapRange<K> range) {
            super(parent, range);
        }

        @Override
        public TrieSetView<K, K, V> subSet(final SortedMapRange<K> range) {
            return null;
        }

        @Override
        public Iterator<K> iterator() {
            return new TransformIterator<>(new TrieEntryIterator<>(parent), Entry::getKey);
        }

        @Override
        public Spliterator<K> spliterator() {
            return new TransformSpliterator<>(new TrieEntrySpliterator<>(parent), Entry::getKey);
        }

        @Override
        public boolean contains(Object o) {
            return parent.containsKey(o);
        }

        @Override
        public boolean remove(Object keyObject) {
            if (keyObject instanceof Comparable<?>) {
                final TEntry<K, V> entry = parent.getEntry(parent.castKey(keyObject));
                if (entry != null) {
                    parent.removeEntry(entry);
                    return true;
                }
            }
            return false;
        }
    }

    private static class TrieValues<K extends Comparable<K>, V extends Comparable<V>>
            extends AbstractCommonsCollection<V> {
        private final GeneralRadixTrie<K, V> parent;

        private TrieValues(final GeneralRadixTrie<K, V> parent) {
            this.parent = parent;
        }

        @Override
        public Iterator<V> iterator() {
            return new TransformIterator<>(new TrieEntryIterator<>(parent), Entry::getValue);
        }

        @Override
        public Spliterator<V> spliterator() {
            return new TransformSpliterator<>(new TrieEntrySpliterator<>(parent), Entry::getValue);
        }

        @Override
        public boolean contains(final Object value) {
            return parent.containsValue(value);
        }

        @Override
        public boolean remove(final Object value) {
            if (!(value instanceof Comparable<?>))
                return false;
            final V value1 = parent.castValue(value);
            TEntry<K, V> entry = parent._firstEntry();
            while (entry != null) {
                if (parent.valueEquals(value1, entry.getValue())) {
                    parent.removeEntry(entry);
                    return true;
                }
                entry = entry.next();
            }
            return false;
        }

        @Override
        public boolean equals(final Object other) {
            if (this == other) return true;
            if (other instanceof Collection) {
                final Collection<?> otherCollection = (Collection<?>) other;
                return CollectionUtils.isEqualCollection(this, otherCollection);
            } else {
                return false;
            }
        }
    }
}
