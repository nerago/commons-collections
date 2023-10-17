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
package org.apache.commons.collections4.bidimap;

import org.apache.commons.collections4.KeyValue;
import org.apache.commons.collections4.OrderedIterator;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.SortedMapRange;
import org.apache.commons.collections4.iterators.EmptyOrderedMapIterator;
import org.apache.commons.collections4.keyvalue.UnmodifiableMapEntry;
import org.apache.commons.collections4.spliterators.AbstractTreeRangeSpliterator;
import org.apache.commons.collections4.spliterators.AbstractTreeSpliterator;
import org.apache.commons.collections4.spliterators.MapSpliterator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;



/**
 * Red-Black tree-based implementation of BidiMap where all objects added
 * implement the {@code Comparable} interface.
 * <p>
 * This class guarantees that the map will be in both ascending key order
 * and ascending value order, sorted according to the natural order for
 * the key's and value's classes.
 * </p>
 * <p>
 * This Map is intended for applications that need to be able to look
 * up a key-value pairing by either key or value, and need to do so
 * with equal efficiency.
 * </p>
 * <p>
 * While that goal could be accomplished by taking a pair of TreeMaps
 * and redirecting requests to the appropriate TreeMap (e.g.,
 * containsKey would be directed to the TreeMap that maps values to
 * keys, containsValue would be directed to the TreeMap that maps keys
 * to values), there are problems with that implementation.
 * If the data contained in the TreeMaps is large, the cost of redundant
 * storage becomes significant. The {@link DualTreeBidiMap} and
 * {@link DualHashBidiMap} implementations use this approach.
 * </p>
 * <p>
 * This solution keeps minimizes the data storage by holding data only once.
 * The red-black algorithm is based on {@link java.util.TreeMap}, but has been modified
 * to simultaneously map a tree node by key and by value. This doubles the
 * cost of put operations (but so does using two TreeMaps), and nearly doubles
 * the cost of remove operations (there is a savings in that the lookup of the
 * node to be removed only has to be performed once). And since only one node
 * contains the key and value, storage is significantly less than that
 * required by two TreeMaps.
 * </p>
 * <p>
 * The Map.Entry instances returned by the appropriate methods will
 * not allow setValue() and will throw an
 * UnsupportedOperationException on attempts to call that method.
 * </p>
 *
 * @param <K> the type of the keys in this map
 * @param <V> the type of the values in this map
 * @since 3.0 (previously DoubleOrderedMap v2.0)
 */
@SuppressWarnings({"OverlyComplexMethod", "OverlyLongMethod", "InstanceVariableMayNotBeInitializedByReadObject"})
public final class TreeBidiMapHard<K extends Comparable<K>, V extends Comparable<V>>
        extends AbstractExtendedBidiMap<K, V, TreeBidiMapHard.TreeBidiSubMap<K, V>, TreeBidiMapHard.Inverse<K, V>> {

    private static final long serialVersionUID = -1641463319195982234L;

    private transient Node<K, V> rootNodeKey;
    private transient Node<K, V> rootNodeValue;
    private transient int nodeCount;
    private transient int modifications;

    private transient Inverse<K, V> inverse;

    /**
     * Constructs a new empty TreeBidiMap.
     */
    public TreeBidiMapHard() {
    }

    /**
     * Constructs a new TreeBidiMap by copying an existing Map.
     *
     * @param map the map to copy
     * @throws ClassCastException   if the keys/values in the map are
     *                              not Comparable or are not mutually comparable
     * @throws NullPointerException if any key or value in the map is null
     */
    public TreeBidiMapHard(final Map<? extends K, ? extends V> map) {
        this();
        putAll(map);
    }

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map
     */
    @Override
    public int size() {
        return nodeCount;
    }

    /**
     * Checks whether the map is empty or not.
     *
     * @return true if the map is empty
     */
    @Override
    public boolean isEmpty() {
        return nodeCount == 0;
    }

    /**
     * Checks whether this map contains a mapping for the specified key.
     * <p>
     * The key must implement {@code Comparable}.
     *
     * @param key key whose presence in this map is to be tested
     * @return true if this map contains a mapping for the specified key
     * @throws ClassCastException   if the key is of an inappropriate type
     * @throws NullPointerException if the key is null
     */
    @Override
    public boolean containsKey(final Object key) {
        return lookupKey(checkKey(key)) != null;
    }

    /**
     * Checks whether this map contains a mapping between the specified key and value.
     * <p>
     * The key and value must implement {@code Comparable}.
     *
     * @param key key whose presence in this map is to be tested
     * @param value value whose presence in this map is to be tested
     * @return true if this map contains the specified mapping
     * @throws ClassCastException   if the key or value is of an inappropriate type
     * @throws NullPointerException if the key is null
     */
    @Override
    public boolean containsEntry(final Object key, final Object value) {
        checkValue(value);
        final Node<K, V> entry = lookupKey(checkKey(key));
        if (entry != null) {
            return Objects.equals(entry.getValue(), value);
        }
        return false;
    }

    @Override
    public Iterator<Entry<K, V>> entryIterator() {
        return new EntryIteratorStandardByKey<>(this);
    }

    @Override
    public MapSpliterator<K, V> mapSpliterator() {
        return new KeyMapSpliterator<>(this);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The key must implement {@code Comparable}.
     */
    @Override
    public V getOrDefault(final Object key, final V defaultValue) {
        final Node<K, V> node = lookupKey(checkKey(key));
        return node == null ? defaultValue : node.getValue();
    }

    @Override
    protected V doPut(final K key, final V value, final boolean addIfAbsent, final boolean updateIfPresent) {
        checkKeyAndValue(key, value);
        return doPutKeyFirst(key, value, addIfAbsent, updateIfPresent);
    }

    @Override
    protected V doPut(final K key, final Function<? super K, ? extends V> absentFunc, final BiFunction<? super K, ? super V, ? extends V> presentFunc, final boolean saveNulls) {
        return doPutKeyFirst(key, absentFunc, presentFunc);
    }

    /**
     * Puts all the mappings from the specified map into this map.
     * <p>
     * All keys and values must implement {@code Comparable}.
     *
     * @param map the map to copy from
     */
    @Override
    public void putAll(final Map<? extends K, ? extends V> map) {
        for (final Entry<? extends K, ? extends V> e : map.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    @Override
    public void forEach(final BiConsumer<? super K, ? super V> action) {
        super.forEach(action);
    }

    @Override
    public void replaceAll(final BiFunction<? super K, ? super V, ? extends V> function) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Comparator<? super K> comparator() {
        return null; // natural order
    }

    @Override
    public Comparator<? super V> valueComparator() {
        return null; // natural order
    }

    @Override
    public SortedMapRange<K> getKeyRange() {
        return SortedMapRange.full(null);
    }

    @Override
    public SortedMapRange<V> getValueRange() {
        return SortedMapRange.full(null);
    }

    @Override
    public TreeBidiSubMap<K, V> subMap(final SortedMapRange<K> range) {
        return new TreeBidiSubMap<>(this, range);
    }

    /**
     * Removes the mapping for this key from this map if present.
     * <p>
     * The key must implement {@code Comparable}.
     *
     * @param key key whose mapping is to be removed from the map.
     * @return previous value associated with specified key,
     * or null if there was no mapping for key.
     * @throws ClassCastException   if the key is of an inappropriate type
     * @throws NullPointerException if the key is null
     */
    @Override
    public V remove(final Object key) {
        final Node<K, V> node = lookupKey(checkKey(key));
        if (node == null) {
            return null;
        }
        doRedBlackDelete(node);
        return node.getValue();
    }

    @Override
    public boolean removeAsBoolean(final Object key) {
        final Node<K, V> node = lookupKey(checkKey(key));
        if (node == null) {
            return false;
        }
        doRedBlackDelete(node);
        return true;
    }

    @Override
    public boolean remove(final Object key, final Object value) {
        final Node<K, V> node = lookupKey(checkKey(key));
        if (node == null || !Objects.equals(node.value, value)) {
            return false;
        }
        doRedBlackDelete(node);
        return true;
    }

    /**
     * Removes all mappings from this map.
     */
    @Override
    public void clear() {
        modify();

        nodeCount = 0;
        rootNodeKey = null;
        rootNodeValue = null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The key must implement {@code Comparable}.
     */
    @Override
    public K getKeyOrDefault(final Object value, final K defaultKey) {
        final Node<K, V> node = lookupValue(checkValue(value));
        return node == null ? defaultKey : node.getKey();
    }

    /**
     * Removes the mapping for this value from this map if present.
     * <p>
     * The value must implement {@code Comparable}.
     *
     * @param value value whose mapping is to be removed from the map
     * @return previous key associated with specified value,
     * or null if there was no mapping for value.
     * @throws ClassCastException   if the value is of an inappropriate type
     * @throws NullPointerException if the value is null
     */
    @Override
    public K removeValue(final Object value) {
        final Node<K, V> node = lookupValue(checkValue(value));
        if (node == null) {
            return null;
        }
        doRedBlackDelete(node);
        return node.getKey();
    }

    @Override
    public boolean removeValueAsBoolean(final Object value) {
        final Node<K, V> node = lookupValue(checkValue(value));
        if (node == null) {
            return false;
        }
        doRedBlackDelete(node);
        return true;
    }

    /**
     * Gets the first (lowest) key currently in this map.
     *
     * @return the first (lowest) key currently in this sorted map
     * @throws NoSuchElementException if this map is empty
     */
    @Override
    public K firstKey() {
        if (nodeCount == 0) {
            throw new NoSuchElementException("Map is empty");
        }
        return leastNodeKey(rootNodeKey).getKey();
    }

    /**
     * Gets the last (highest) key currently in this map.
     *
     * @return the last (highest) key currently in this sorted map
     * @throws NoSuchElementException if this map is empty
     */
    @Override
    public K lastKey() {
        if (nodeCount == 0) {
            throw new NoSuchElementException("Map is empty");
        }
        return greatestNodeKey(rootNodeKey).getKey();
    }

    /**
     * Gets the next key after the one specified.
     * <p>
     * The key must implement {@code Comparable}.
     *
     * @param key the key to search for next from
     * @return the next key, null if no match or at end
     */
    @Override
    public K nextKey(final K key) {
        checkKey(key);
        final Node<K, V> node = lookupKeyHigher(key, false);
        return node == null ? null : node.getKey();
    }

    /**
     * Gets the previous key before the one specified.
     * <p>
     * The key must implement {@code Comparable}.
     *
     * @param key the key to search for previous from
     * @return the previous key, null if no match or at start
     */
    @Override
    public K previousKey(final K key) {
        checkKey(key);
        final Node<K, V> node = lookupKeyLower(key, false);
        return node == null ? null : node.getKey();
    }

    @Override
    public OrderedMapIterator<K, V> mapIterator() {
        if (isEmpty()) {
            return EmptyOrderedMapIterator.emptyOrderedMapIterator();
        }
        return new MapIteratorKeyByKey<>(this);
    }

    /**
     * Gets the inverse map for comparison.
     *
     * @return the inverse map
     */
    @Override
    public Inverse<K, V> inverseBidiMap() {
        if (inverse == null) {
            inverse = new Inverse<>(this);
        }
        return inverse;
    }

    /**
     * Puts logic.
     *
     * @param key   the key, always the main map key
     * @param value the value, always the main map value
     */
    private V doPutKeyFirst(final K key, final V value, final boolean addIfAbsent, final boolean updateIfPresent) {
        Node<K, V> node = rootNodeKey;
        if (node == null) {
            if (addIfAbsent) {
                addAsRoot(key, value);
            }
            return null;
        }

        // find key position
        final Node<K, V> keyNode;
        V oldValue = null;
        while (true) {
            final int cmp = key.compareTo(node.getKey());
            if (cmp == 0) {
                oldValue = node.value;
                if (!updateIfPresent || Objects.equals(oldValue, value))
                    return oldValue;
                updateValue(node, value);
                keyNode = node;
                break;
            } else if (cmp < 0) {
                if (node.keyLeftNode == null) {
                    if (!addIfAbsent)
                        return null;
                    keyNode = addOnLeftKey(key, value, node);
                    break;
                }
                node = node.keyLeftNode;
            } else { // cmp > 0
                if (node.keyRightNode == null) {
                    if (!addIfAbsent)
                        return null;
                    keyNode = addOnRightKey(key, value, node);
                    break;
                }
                node = node.keyRightNode;
            }
        }

        finishPutKeyFirst(key, value, keyNode);

        return oldValue;
    }

    private V doPutKeyFirst(final K key,
                            final Function<? super K, ? extends V> absentFunc,
                            final BiFunction<? super K, ? super V, ? extends V> presentFunc) {
        final int expectedModifications = modifications;

        Node<K, V> node = rootNodeKey;
        if (node == null) {
            // map is empty
            if (absentFunc != null) {
                final V value = absentFunc.apply(key);
                if (expectedModifications != modifications) {
                    throw new ConcurrentModificationException();
                }
                if (value != null) {
                    checkValue(value);
                    addAsRoot(key, value);
                    return value;
                }
            }
            return null;
        }

        // find key position
        final Node<K, V> keyNode;
        final V newValue;
        while (true) {
            final int cmp = key.compareTo(node.getKey());
            if (cmp == 0) {
                final V oldValue = node.getValue();
                if (oldValue != null && presentFunc != null) {
                    newValue = presentFunc.apply(key, oldValue);
                    if (expectedModifications != modifications) {
                        throw new ConcurrentModificationException();
                    } else if (newValue == null) {
                        doRedBlackDelete(node);
                        return null;
                    } else if (Objects.equals(oldValue, newValue)) {
                        return oldValue;
                    } else {
                        checkValue(newValue);
                        updateValue(node, newValue);
                        keyNode = node;
                        break;
                    }
                }
                return oldValue;
            } else if (cmp < 0) {
                if (node.keyLeftNode == null) {
                    if (absentFunc != null) {
                        newValue = absentFunc.apply(key);
                        if (expectedModifications != modifications) {
                            throw new ConcurrentModificationException();
                        } else if (newValue != null) {
                            checkValue(newValue);
                            keyNode = addOnLeftKey(key, newValue, node);
                            break;
                        }
                    }
                    return null;
                }
                node = node.keyLeftNode;
            } else { // cmp > 0
                if (node.keyRightNode == null) {
                    if (absentFunc != null) {
                        newValue = absentFunc.apply(key);
                        if (expectedModifications != modifications) {
                            throw new ConcurrentModificationException();
                        } else if (newValue != null) {
                            checkValue(newValue);
                            keyNode = addOnRightKey(key, newValue, node);
                            break;
                        }
                    }
                    return null;
                }
                node = node.keyRightNode;
            }
        }

        finishPutKeyFirst(key, newValue, keyNode);

        return newValue;
    }

    private void finishPutKeyFirst(final K key, final V value, final Node<K, V> keyNode) {
        Node<K, V> node = rootNodeValue;
        if (node == null) {
            rootNodeValue = keyNode;
            keyNode.valueParentNode = null;
            return;
        }

        while (true) {
            final int cmp = value.compareTo(node.getValue());

            if (cmp == 0) {
                // replace existing value node (assume different key)
                assert !Objects.equals(node.key, key);
                keyNode.copyColorValue(node);
                replaceNodeValue(node, keyNode, true);
                doRedBlackDeleteKey(node);
                shrink();
                break;
            } else if (cmp < 0) {
                if (node.valueLeftNode == null) {
                    insertOnLeftValue(node, keyNode);
                    break;
                }
                node = node.valueLeftNode;
            } else { // cmp > 0
                if (node.valueRightNode == null) {
                    insertOnRightValue(node, keyNode);
                    break;
                }
                node = node.valueRightNode;
            }
        }
    }

    private K doPutValueFirst(final V value, final K key, final boolean addIfAbsent, final boolean updateIfPresent) {
        checkKeyAndValue(key, value);

        Node<K, V> node = rootNodeValue;
        if (node == null) {
            if (addIfAbsent) {
                addAsRoot(key, value);
            }
            return null;
        }

        // find key position
        final Node<K, V> valueNode;
        K oldKey = null;
        while (true) {
            final int cmp = value.compareTo(node.getValue());
            if (cmp == 0) {
                oldKey = node.key;
                if (!updateIfPresent || Objects.equals(oldKey, key))
                    return oldKey;
                this.updateKey(node, key);
                valueNode = node;
                break;
            } else if (cmp < 0) {
                if (node.valueLeftNode == null) {
                    if (!addIfAbsent)
                        return null;
                    valueNode = addOnLeftValue(key, value, node);
                    break;
                }
                node = node.valueLeftNode;
            } else { // cmp > 0
                if (node.valueRightNode == null) {
                    if (!addIfAbsent)
                        return null;
                    valueNode = addOnRightValue(key, value, node);
                    break;
                }
                node = node.valueRightNode;
            }
        }

        finishPutValueFirst(key, value, valueNode);

        return oldKey;
    }

    private K doPutValueFirst(final V value,
                              final Function<? super V, ? extends K> absentFunc,
                              final BiFunction<? super V, ? super K, ? extends K> presentFunc) {
        final int expectedModifications = modifications;

        Node<K, V> node = rootNodeValue;
        if (node == null) {
            // map is empty
            if (absentFunc != null) {
                final K key = absentFunc.apply(value);
                if (expectedModifications != modifications) {
                    throw new ConcurrentModificationException();
                }
                if (key != null) {
                    checkKey(key);
                    addAsRoot(key, value);
                    return key;
                }
            }
            return null;
        }

        // find value position
        final Node<K, V> valueNode;
        final K newKey;
        while (true) {
            final int cmp = value.compareTo(node.getValue());
            if (cmp == 0) {
                final K oldKey = node.getKey();
                if (presentFunc != null) {
                    newKey = presentFunc.apply(value, oldKey);
                    if (expectedModifications != modifications) {
                        throw new ConcurrentModificationException();
                    } else if (newKey == null) {
                        doRedBlackDelete(node);
                        return null;
                    } else if (Objects.equals(oldKey, newKey)) {
                        return oldKey;
                    } else {
                        checkKey(newKey);
                        this.updateKey(node, newKey);
                        valueNode = node;
                        break;
                    }
                }
                return oldKey;
            } else if (cmp < 0) {
                if (node.valueLeftNode == null) {
                    if (absentFunc != null) {
                        newKey = absentFunc.apply(value);
                        if (expectedModifications != modifications) {
                            throw new ConcurrentModificationException();
                        } else if (newKey != null) {
                            checkKey(newKey);
                            valueNode = addOnLeftValue(newKey, value, node);
                            break;
                        }
                    }
                    return null;
                }
                node = node.valueLeftNode;
            } else { // cmp > 0
                if (node.valueRightNode == null) {
                    if (absentFunc != null) {
                        newKey = absentFunc.apply(value);
                        if (expectedModifications != modifications) {
                            throw new ConcurrentModificationException();
                        } else if (newKey != null) {
                            checkKey(newKey);
                            valueNode = addOnRightValue(newKey, value, node);
                            break;
                        }
                    }
                    return null;
                }
                node = node.valueRightNode;
            }
        }

        finishPutValueFirst(newKey, value, valueNode);

        return newKey;
    }

    private void finishPutValueFirst(final K key, final V value, final Node<K, V> valueNode) {
        Node<K, V> node = rootNodeKey;
        if (node == null) {
            rootNodeKey = valueNode;
            valueNode.keyParentNode = null;
            return;
        }

        while (true) {
            final int cmp = key.compareTo(node.getKey());

            if (cmp == 0) {
                // replace existing key node (assume different value)
                assert !Objects.equals(node.value, value);
                valueNode.copyColorKey(node);
                replaceNodeKey(node, valueNode, true);
                doRedBlackDeleteValue(node);
                shrink();
                break;
            } else if (cmp < 0) {
                if (node.keyLeftNode == null) {
                    insertOnLeftKey(node, valueNode);
                    break;
                }
                node = node.keyLeftNode;
            } else { // cmp > 0
                if (node.keyRightNode == null) {
                    insertOnRightKey(node, valueNode);
                    break;
                }
                node = node.keyRightNode;
            }
        }
    }

    private void insertOnLeftKey(final Node<K, V> parentNode, final Node<K, V> node) {
        parentNode.keyLeftNode = node;
        node.keyParentNode = parentNode;
        doRedBlackInsertKey(node);
    }

    private void insertOnRightKey(final Node<K, V> parentNode, final Node<K, V> node) {
        parentNode.keyRightNode = node;
        node.keyParentNode = parentNode;
        doRedBlackInsertKey(node);
    }

    private void insertOnLeftValue(final Node<K, V> parent, final Node<K, V> node) {
        parent.valueLeftNode = node;
        node.valueParentNode = parent;
        doRedBlackInsertValue(node);
    }

    private void insertOnRightValue(final Node<K, V> parent, final Node<K, V> node) {
        parent.valueRightNode = node;
        node.valueParentNode = parent;
        doRedBlackInsertValue(node);
    }

    private void addAsRoot(final K key, final V value) {
        final Node<K, V> root = new Node<>(key, value);
        rootNodeKey = root;
        rootNodeValue = root;
        grow();
    }

    private Node<K, V> addOnLeftKey(final K key, final V value, final Node<K, V> parent) {
        final Node<K, V> node = new Node<>(key, value);
        parent.keyLeftNode = node;
        node.keyParentNode = parent;
        doRedBlackInsertKey(node);
        grow();
        return node;
    }

    private Node<K, V> addOnRightKey(final K key, final V value, final Node<K, V> parent) {
        final Node<K, V> node = new Node<>(key, value);
        parent.keyRightNode = node;
        node.keyParentNode = parent;
        doRedBlackInsertKey(node);
        grow();
        return node;
    }

    private Node<K, V> addOnLeftValue(final K key, final V value, final Node<K, V> parent) {
        final Node<K, V> node = new Node<>(key, value);
        parent.valueLeftNode = node;
        node.valueParentNode = parent;
        doRedBlackInsertValue(node);
        grow();
        return node;
    }

    private Node<K, V> addOnRightValue(final K key, final V value, final Node<K, V> parent) {
        final Node<K, V> node = new Node<>(key, value);
        parent.valueRightNode = node;
        node.valueParentNode = parent;
        doRedBlackInsertValue(node);
        grow();
        return node;
    }

    private void updateValue(final Node<K, V> node, final V value) {
        // remove from value tree
        doRedBlackDeleteValue(node);

        // update value
        node.value = value;
        node.calculatedHashCode = false;
        modify();
    }

    private void updateKey(final Node<K, V> node, final K key) {
        // remove from value tree
        doRedBlackDeleteKey(node);

        // update value
        node.key = key;
        node.calculatedHashCode = false;
        modify();
    }

    private Node<K, V> lookupKey(final K key) {
        Node<K, V> node = rootNodeKey;

        while (node != null) {
            final K result = node.getKey();
            final int cmp = key.compareTo(result);
            if (cmp == 0) {
                return node;
            } else if (cmp < 0) {
                node = node.keyLeftNode;
            } else {
                node = node.keyRightNode;
            }
        }

        return null;
    }

    private Node<K, V> lookupKeyHigher(final K key, final boolean includeEqual) {
        Node<K, V> node = rootNodeKey, higher = null;

        while (node != null) {
            final int cmp = node.getKey().compareTo(key);
            if (cmp == 0 && includeEqual) {
                return node;
            } else if (cmp > 0) {
                higher = node;
                node = node.keyLeftNode;
            } else {
                node = node.keyRightNode;
            }
        }

        return higher;
    }

    private Node<K, V> lookupKeyLower(final K key, final boolean includeEqual) {
        Node<K, V> node = rootNodeKey, lower = null;

        while (node != null) {
            final int cmp = node.getKey().compareTo(key);
            if (cmp == 0 && includeEqual) {
                return node;
            } else if (cmp < 0) {
                lower = node;
                node = node.keyRightNode;
            } else {
                node = node.keyLeftNode;
            }
        }

        return lower;
    }

    private Node<K, V> lookupValue(final V value) {
        Node<K, V> node = rootNodeValue;

        while (node != null) {
            final int cmp = value.compareTo(node.getValue());
            if (cmp == 0) {
                return node;
            } else if (cmp < 0) {
                node = node.valueLeftNode;
            } else {
                node = node.valueRightNode;
            }
        }

        return null;
    }

    private Node<K, V> lookupValueLower(final V value) {
        Node<K, V> node = rootNodeValue, lower = null;

        while (node != null) {
            final int cmp = node.getValue().compareTo(value);
            if (cmp < 0) {
                lower = node;
                node = node.valueRightNode;
            } else {
                node = node.valueLeftNode;
            }
        }

        return lower;
    }

    private Node<K, V> lookupValueHigher(final V value) {
        Node<K, V> node = rootNodeValue, higher = null;

        while (node != null) {
            final int cmp = node.getValue().compareTo(value);
            if (cmp > 0) {
                higher = node;
                node = node.valueLeftNode;
            } else {
                node = node.valueRightNode;
            }
        }

        return higher;
    }

    /**
     * Gets the next smaller node from the specified node.
     *
     * @param node the node to be searched from
     * @return the specified node
     */
    private Node<K, V> nextSmallerKey(final Node<K, V> node) {
        if (node == null) {
            return null;
        } else if (node.keyLeftNode != null) {
            return greatestNodeKey(node.keyLeftNode);
        } else {
            Node<K, V> parent = node.keyParentNode;
            Node<K, V> child = node;

            while (parent != null) {
                if (child != parent.keyLeftNode) {
                    break;
                } else {
                    child = parent;
                    parent = parent.keyParentNode;
                }
            }
            return parent;
        }
    }

    /**
     * Gets the next larger node from the specified node.
     *
     * @param node the node to be searched from
     * @return the specified node
     */
    private Node<K, V> nextGreaterKey(final Node<K, V> node) {
        if (node == null) {
            return null;
        } else if (node.keyRightNode != null) {
            return leastNodeKey(node.keyRightNode);
        } else {
            Node<K, V> parent = node.keyParentNode;
            Node<K, V> child = node;

            while (parent != null) {
                if (child != parent.keyRightNode)
                    break;
                child = parent;
                parent = parent.keyParentNode;
            }

            return parent;
        }

    }

    /**
     * Gets the next smaller node from the specified node.
     *
     * @param node the node to be searched from
     * @return the specified node
     */
    private Node<K, V> nextSmallerValue(final Node<K, V> node) {
        if (node == null) {
            return null;
        } else if (node.valueLeftNode != null) {
            return greatestNodeValue(node.valueLeftNode);
        } else {
            Node<K, V> parent = node.valueParentNode;
            Node<K, V> child = node;

            while (parent != null) {
                if (child != parent.valueLeftNode) {
                    break;
                } else {
                    child = parent;
                    parent = parent.valueParentNode;
                }
            }
            return parent;
        }
    }

    /**
     * Gets the next larger node from the specified node.
     *
     * @param node the node to be searched from
     * @return the specified node
     */
    private Node<K, V> nextGreaterValue(final Node<K, V> node) {
        if (node == null) {
            return null;
        } else if (node.valueRightNode != null) {
            return leastNodeValue(node.valueRightNode);
        } else {
            Node<K, V> parent = node.valueParentNode;
            Node<K, V> child = node;

            while (parent != null) {
                if (child != parent.valueRightNode)
                    break;
                child = parent;
                parent = parent.valueParentNode;
            }

            return parent;

        }
    }

    /**
     * Finds the least node from a given node.
     *
     * @param node the node from which we will start searching
     * @return the smallest node, from the specified node, in the
     * specified mapping
     */
    private Node<K, V> leastNodeKey(final Node<K, V> node) {
        Node<K, V> rval = node;
        if (rval != null) {
            while (rval.keyLeftNode != null) {
                rval = rval.keyLeftNode;
            }
        }
        return rval;
    }

    /**
     * Finds the least node from a given node.
     *
     * @param node the node from which we will start searching
     * @return the smallest node, from the specified node, in the
     * specified mapping
     */
    private Node<K, V> leastNodeValue(final Node<K, V> node) {
        Node<K, V> rval = node;
        if (rval != null) {
            while (rval.valueLeftNode != null) {
                rval = rval.valueLeftNode;
            }
        }
        return rval;
    }

    /**
     * Finds the greatest node from a given node.
     *
     * @param node the node from which we will start searching
     * @return the greatest node, from the specified node
     */
    private Node<K, V> greatestNodeKey(final Node<K, V> node) {
        Node<K, V> rval = node;
        if (rval != null) {
            while (rval.keyRightNode != null) {
                rval = rval.keyRightNode;
            }
        }
        return rval;
    }

    /**
     * Finds the greatest node from a given node.
     *
     * @param node the node from which we will start searching
     * @return the greatest node, from the specified node
     */
    private Node<K, V> greatestNodeValue(final Node<K, V> node) {
        Node<K, V> rval = node;
        if (rval != null) {
            while (rval.valueRightNode != null) {
                rval = rval.valueRightNode;
            }
        }
        return rval;
    }

    /**
     * Does a rotate left. standard fare in the world of balanced trees.
     *
     * @param node the node to be rotated
     */
    private void rotateLeftKey(final Node<K, V> node) {
        final Node<K, V> rightChild = node.keyRightNode;
        node.keyRightNode = rightChild.keyLeftNode;

        if (rightChild.keyLeftNode != null) {
            rightChild.keyLeftNode.keyParentNode = node;
        }
        rightChild.keyParentNode = node.keyParentNode;

        final Node<K, V> parent = node.keyParentNode;
        if (parent == null) {
            // node was the root ... now its right child is the root
            rootNodeKey = rightChild;
        } else {
            if (parent.keyLeftNode == node) {
                parent.keyLeftNode = rightChild;
            } else {
                parent.keyRightNode = rightChild;
            }
        }

        rightChild.keyLeftNode = node;
        node.keyParentNode = rightChild;
    }

    private void rotateLeftValue(final Node<K, V> node) {
        final Node<K, V> rightChild = node.valueRightNode;
        node.valueRightNode = rightChild.valueLeftNode;

        if (rightChild.valueLeftNode != null) {
            rightChild.valueLeftNode.valueParentNode = node;
        }
        rightChild.valueParentNode = node.valueParentNode;

        final Node<K, V> parent = node.valueParentNode;
        if (parent == null) {
            // node was the root ... now its right child is the root
            rootNodeValue = rightChild;
        } else {
            if (parent.valueLeftNode == node) {
                parent.valueLeftNode = rightChild;
            } else {
                parent.valueRightNode = rightChild;
            }
        }

        rightChild.valueLeftNode = node;
        node.valueParentNode = rightChild;
    }


    /**
     * Does a rotate right. standard fare in the world of balanced trees.
     *
     * @param node the node to be rotated
     */
    private void rotateRightKey(final Node<K, V> node) {
        final Node<K, V> leftChild = node.keyLeftNode;
        node.keyLeftNode = leftChild.keyRightNode;
        if (leftChild.keyRightNode != null) {
            final Node<K, V> kvNode = leftChild.keyRightNode;
            kvNode.keyParentNode = node;
        }
        leftChild.keyParentNode = node.keyParentNode;

        final Node<K, V> parent = node.keyParentNode;
        if (parent == null) {
            // node was the root ... now its left child is the root
            rootNodeKey = leftChild;
        } else {
            if (parent.keyRightNode == node) {
                parent.keyRightNode = leftChild;
            } else {
                parent.keyLeftNode = leftChild;
            }
        }

        leftChild.keyRightNode = node;
        node.keyParentNode = leftChild;
    }

    private void rotateRightValue(final Node<K, V> node) {
        final Node<K, V> leftChild = node.valueLeftNode;
        node.valueLeftNode = leftChild.valueRightNode;
        if (leftChild.valueRightNode != null) {
            final Node<K, V> kvNode = leftChild.valueRightNode;
            kvNode.valueParentNode = node;
        }
        leftChild.valueParentNode = node.valueParentNode;

        final Node<K, V> parent = node.valueParentNode;
        if (node.valueParentNode == null) {
            // node was the root ... now its left child is the root
            rootNodeValue = leftChild;
        } else {
            if (parent.valueRightNode == node) {
                parent.valueRightNode = leftChild;
            } else {
                parent.valueLeftNode = leftChild;
            }
        }

        leftChild.valueRightNode = node;
        node.valueParentNode = leftChild;
    }

    /**
     * Complicated red-black insert stuff. Based on Sun's TreeMap
     * implementation, though it's barely recognizable anymore.
     *
     * @param insertedNode the node to be inserted
     */
    private void doRedBlackInsertKey(final Node<K, V> insertedNode) {
        Node<K, V> currentNode = insertedNode;
        if (currentNode != null) {
            currentNode.setRedKey();
        }

        while (currentNode != null && currentNode != rootNodeKey && currentNode.keyParentNode != null && currentNode.keyParentNode.isRedKey()) {
            final Node<K, V> parent = currentNode.keyParentNode;
            final Node<K, V> grandParent = parent.keyParentNode;
            if (parent.keyLeftNode == currentNode) {
                final Node<K, V> grandParentRight = grandParent != null ? grandParent.keyRightNode : null;

                if (grandParentRight != null && grandParentRight.isRedKey()) {
                    parent.setBlackKey();
                    grandParentRight.setBlackKey();
                    grandParent.setRedKey();
                    currentNode = grandParent;
                } else {
                    parent.setBlackKey();
                    if (grandParent != null) {
                        grandParent.setRedKey();
                        rotateRightKey(grandParent);
                    }
                }
            } else {
                // just like clause above, except swap left for right
                final Node<K, V> grandParentLeft = grandParent != null ? grandParent.keyLeftNode : null;

                if (grandParentLeft != null && grandParentLeft.isRedKey()) {
                    parent.setBlackKey();
                    grandParentLeft.setBlackKey();
                    grandParent.setRedKey();
                    currentNode = grandParent;
                } else {
                    parent.setBlackKey();
                    if (grandParent != null) {
                        grandParent.setRedKey();
                        rotateLeftKey(grandParent);
                    }
                }
            }
        }

        if (rootNodeKey != null) {
            rootNodeKey.setBlackKey();
        }
    }

    private void doRedBlackInsertValue(final Node<K, V> insertedNode) {
        Node<K, V> currentNode = insertedNode;
        if (currentNode != null) {
            currentNode.setRedValue();
        }

        while (currentNode != null && currentNode != rootNodeValue && currentNode.valueParentNode != null && currentNode.valueParentNode.isRedValue()) {
            final Node<K, V> parent = currentNode.valueParentNode;
            final Node<K, V> grandParent = parent.valueParentNode;
            if (parent.valueLeftNode == currentNode) {
                final Node<K, V> grandParentRight = grandParent != null ? grandParent.valueRightNode : null;

                if (grandParentRight != null && grandParentRight.isRedValue()) {
                    parent.setBlackValue();
                    grandParentRight.setBlackValue();
                    grandParent.setRedValue();
                    currentNode = grandParent;
                } else {
                    parent.setBlackValue();
                    if (grandParent != null) {
                        grandParent.setRedValue();
                        rotateRightValue(grandParent);
                    }
                }
            } else {
                // just like clause above, except swap left for right
                final Node<K, V> grandParentLeft = grandParent != null ? grandParent.valueLeftNode : null;

                if (grandParentLeft != null && grandParentLeft.isRedValue()) {
                    parent.setBlackValue();
                    grandParentLeft.setBlackValue();
                    grandParent.setRedValue();
                    currentNode = grandParent;
                } else {
                    parent.setBlackValue();
                    if (grandParent != null) {
                        grandParent.setRedValue();
                        rotateLeftValue(grandParent);
                    }
                }
            }
        }

        if (rootNodeValue != null) {
            rootNodeValue.setBlackValue();
        }
    }

    /**
     * Complicated red-black delete stuff. Based on Sun's TreeMap
     * implementation, though it's barely recognizable anymore.
     *
     * @param deletedNode the node to be deleted
     */
    private void doRedBlackDelete(final Node<K, V> deletedNode) {
        doRedBlackDeleteKey(deletedNode);
        doRedBlackDeleteValue(deletedNode);
        shrink();
    }

    private void doRedBlackDeleteKey(final Node<K, V> deletedNode) {
        // if deleted node has both left and children, swap with
        // the next greater node
        if (deletedNode.keyLeftNode != null && deletedNode.keyRightNode != null) {
            swapPositionKey(nextGreaterKey(deletedNode), deletedNode);
        }

        final Node<K, V> replacement;
        if (deletedNode.keyLeftNode != null) {
            replacement = deletedNode.keyLeftNode;
        } else {
            replacement = deletedNode.keyRightNode;
        }

        if (replacement != null) {
            replaceNodeKey(deletedNode, replacement, false);
        } else {
            // replacement is null
            if (deletedNode.keyParentNode == null) {
                // empty tree
                rootNodeKey = null;
            } else {
                // deleted node had no children
                if (deletedNode.isBlackKey()) {
                    doRedBlackDeleteFixupKey(deletedNode);
                }

                if (deletedNode.keyParentNode != null) {
                    final Node<K, V> parentNode = deletedNode.keyParentNode;
                    if (deletedNode == parentNode.keyLeftNode) {
                        parentNode.keyLeftNode = null;
                    } else {
                        parentNode.keyRightNode = null;
                    }

                    deletedNode.keyParentNode = null;
                }
            }
        }
    }

    private void doRedBlackDeleteValue(final Node<K, V> deletedNode) {
        // if deleted node has both left and children, swap with
        // the next greater node
        if (deletedNode.valueLeftNode != null && deletedNode.valueRightNode != null) {
            swapPositionValue(nextGreaterValue(deletedNode), deletedNode);
        }

        final Node<K, V> replacement;
        if (deletedNode.valueLeftNode != null) {
            replacement = deletedNode.valueLeftNode;
        } else {
            replacement = deletedNode.valueRightNode;
        }

        if (replacement != null) {
            replaceNodeValue(deletedNode, replacement, false);
        } else {
            // replacement is null
            if (deletedNode.valueParentNode == null) {
                // empty tree
                rootNodeValue = null;
            } else {
                // deleted node had no children
                if (deletedNode.isBlackValue()) {
                    doRedBlackDeleteFixupValue(deletedNode);
                }

                if (deletedNode.valueParentNode != null) {
                    final Node<K, V> parentNode = deletedNode.valueParentNode;
                    if (deletedNode == parentNode.valueLeftNode) {
                        parentNode.valueLeftNode = null;
                    } else {
                        parentNode.valueRightNode = null;
                    }

                    deletedNode.valueParentNode = null;
                }
            }
        }
    }

    /**
     * Complicated red-black delete stuff. Based on Sun's TreeMap
     * implementation, though it's barely recognizable anymore. This
     * rebalances the tree (somewhat, as red-black trees are not
     * perfectly balanced -- perfect balancing takes longer)
     *
     * @param replacementNode the node being replaced
     */
    private void doRedBlackDeleteFixupKey(final Node<K, V> replacementNode) {
        Node<K, V> currentNode = replacementNode;

        while (currentNode != rootNodeKey && currentNode != null && currentNode.keyParentNode != null) {
            if (!currentNode.isBlackKey()) break;
            Node<K, V> parent = currentNode.keyParentNode;
            final boolean isLeftChild = parent.keyLeftNode == currentNode;
            if (isLeftChild) {
                Node<K, V> siblingNode = parent.keyRightNode;

                if (siblingNode == null) {
                    currentNode = parent;
                    continue;
                }

                if (siblingNode.isRedKey()) {
                    siblingNode.setBlackKey();
                    parent.setRedKey();
                    rotateLeftKey(parent);
                    parent = currentNode.keyParentNode;
                    siblingNode = parent.keyRightNode;
                }

                if (siblingNode == null) {
                    currentNode = parent;
                    continue;
                }

                final Node<K, V> siblingLeft = siblingNode.keyLeftNode;
                final Node<K, V> siblingRight = siblingNode.keyRightNode;
                if ((siblingLeft == null || siblingLeft.isBlackKey()) && (siblingRight == null || siblingRight.isBlackKey())) {
                    siblingNode.setRedKey();
                    currentNode = parent;
                } else {
                    if (siblingRight == null || siblingRight.isBlackKey()) {
                        if (siblingLeft != null) {
                            siblingLeft.setBlackKey();
                        }
                        siblingNode.setRedKey();
                        rotateRightKey(siblingNode);
                        parent = currentNode.keyParentNode;
                        siblingNode = parent.keyRightNode;
                    }

                    if (siblingNode != null) {
                        siblingNode.copyColorKey(parent);
                    }
                    parent.setBlackKey();
                    if (siblingRight != null) {
                        siblingRight.setBlackKey();
                    }
                    rotateLeftKey(parent);
                    break;
                }
            } else {
                Node<K, V> siblingNode = parent.keyLeftNode;

                if (siblingNode == null) {
                    currentNode = parent;
                    continue;
                }

                if (siblingNode.isRedKey()) {
                    siblingNode.setBlackKey();
                    parent.setRedKey();
                    rotateRightKey(parent);

                    final Node<K, V> result;
                    result = parent.keyLeftNode;
                    siblingNode = result;
                }

                if (siblingNode == null) {
                    currentNode = parent;
                    continue;
                }

                final Node<K, V> siblingLeft = siblingNode.keyLeftNode;
                final Node<K, V> siblingRight = siblingNode.keyRightNode;
                if ((siblingLeft == null || siblingLeft.isBlackKey()) && (siblingRight == null || siblingRight.isBlackKey())) {
                    siblingNode.setRedKey();
                    currentNode = parent;
                } else {
                    if (siblingLeft == null || siblingLeft.isBlackKey()) {
                        if (siblingRight != null) {
                            siblingRight.setBlackKey();
                        }
                        siblingNode.setRedKey();
                        rotateLeftKey(siblingNode);
                        parent = currentNode.keyParentNode;
                        siblingNode = parent.keyLeftNode;
                    }

                    if (siblingNode != null) {
                        siblingNode.copyColorKey(parent);
                    }
                    parent.setBlackKey();
                    if (siblingLeft != null) {
                        siblingLeft.setBlackKey();
                    }
                    rotateRightKey(parent);
                    break;
                }
            }
        }

        if (currentNode != null) {
            currentNode.setBlackKey();
        }
    }

    private void doRedBlackDeleteFixupValue(final Node<K, V> replacementNode) {
        Node<K, V> currentNode = replacementNode;

        while (currentNode != rootNodeValue && currentNode != null && currentNode.valueParentNode != null) {
            if (!currentNode.isBlackValue()) break;
            Node<K, V> parent = currentNode.valueParentNode;
            final boolean isLeftChild = parent.valueLeftNode == currentNode;
            if (isLeftChild) {
                Node<K, V> siblingNode = parent.valueRightNode;

                if (siblingNode == null) {
                    currentNode = parent;
                    continue;
                }

                if (siblingNode.isRedValue()) {
                    siblingNode.setBlackValue();
                    parent.setRedValue();
                    rotateLeftValue(parent);
                    parent = currentNode.valueParentNode;
                    siblingNode = parent.valueRightNode;
                }

                if (siblingNode == null) {
                    currentNode = parent;
                    continue;
                }

                final Node<K, V> siblingLeft = siblingNode.valueLeftNode;
                final Node<K, V> siblingRight = siblingNode.valueRightNode;
                if ((siblingLeft == null || siblingLeft.isBlackValue()) && (siblingRight == null || siblingRight.isBlackValue())) {
                    siblingNode.setRedValue();
                    currentNode = parent;
                } else {
                    if (siblingRight == null || siblingRight.isBlackValue()) {
                        if (siblingLeft != null) {
                            siblingLeft.setBlackValue();
                        }
                        siblingNode.setRedValue();
                        rotateRightValue(siblingNode);
                        parent = currentNode.valueParentNode;
                        siblingNode = parent.valueRightNode;
                    }

                    if (siblingNode != null) {
                        siblingNode.copyColorValue(parent);
                    }
                    parent.setBlackValue();
                    if (siblingRight != null) {
                        siblingRight.setBlackValue();
                    }
                    rotateLeftValue(parent);
                    break;
                }
            } else {
                Node<K, V> siblingNode = parent.valueLeftNode;

                if (siblingNode == null) {
                    currentNode = parent;
                    continue;
                }

                if (siblingNode.isRedValue()) {
                    siblingNode.setBlackValue();
                    parent.setRedValue();
                    rotateRightValue(parent);

                    final Node<K, V> result;
                    result = parent.valueLeftNode;
                    siblingNode = result;
                }

                if (siblingNode == null) {
                    currentNode = parent;
                    continue;
                }

                final Node<K, V> siblingLeft = siblingNode.valueLeftNode;
                final Node<K, V> siblingRight = siblingNode.valueRightNode;
                if ((siblingLeft == null || siblingLeft.isBlackValue()) && (siblingRight == null || siblingRight.isBlackValue())) {
                    siblingNode.setRedValue();
                    currentNode = parent;
                } else {
                    if (siblingLeft == null || siblingLeft.isBlackValue()) {
                        if (siblingRight != null) {
                            siblingRight.setBlackValue();
                        }
                        siblingNode.setRedValue();
                        rotateLeftValue(siblingNode);
                        parent = currentNode.valueParentNode;
                        siblingNode = parent.valueLeftNode;
                    }

                    if (siblingNode != null) {
                        siblingNode.copyColorValue(parent);
                    }
                    parent.setBlackValue();
                    if (siblingLeft != null) {
                        siblingLeft.setBlackValue();
                    }
                    rotateRightValue(parent);
                    break;
                }
            }
        }

        if (currentNode != null) {
            currentNode.setBlackValue();
        }
    }

    private void replaceNodeKey(final Node<K, V> previous, final Node<K, V> replacement, final boolean keepChildren) {
        final Node<K, V> parentNode = previous.keyParentNode;
        replacement.keyParentNode = parentNode;

        if (parentNode == null) {
            rootNodeKey = replacement;
        } else {
            if (previous == parentNode.keyLeftNode) {
                parentNode.keyLeftNode = replacement;
            } else {
                parentNode.keyRightNode = replacement;
            }
        }

        if (keepChildren) {
            if (previous.keyLeftNode != null) {
                replacement.keyLeftNode = previous.keyLeftNode;
                replacement.keyLeftNode.keyParentNode = replacement;
            }

            if (previous.keyRightNode != null) {
                replacement.keyRightNode = previous.keyRightNode;
                replacement.keyRightNode.keyParentNode = replacement;
            }
        }

        previous.keyLeftNode = null;
        previous.keyRightNode = null;
        previous.keyParentNode = null;

        if (previous.isBlackKey()) {
            doRedBlackDeleteFixupKey(replacement);
        }
    }

    private void replaceNodeValue(final Node<K, V> previous, final Node<K, V> replacement, final boolean keepChildren) {
        final Node<K, V> parentNode = previous.valueParentNode;
        replacement.valueParentNode = parentNode;

        if (parentNode == null) {
            rootNodeValue = replacement;
        } else {
            if (previous == parentNode.valueLeftNode) {
                parentNode.valueLeftNode = replacement;
            } else {
                parentNode.valueRightNode = replacement;
            }
        }

        if (keepChildren) {
            if (previous.valueLeftNode != null) {
                replacement.valueLeftNode = previous.valueLeftNode;
                replacement.valueLeftNode.valueParentNode = replacement;
            }

            if (previous.valueRightNode != null) {
                replacement.valueRightNode = previous.valueRightNode;
                replacement.valueRightNode.valueParentNode = replacement;
            }
        }

        previous.valueLeftNode = null;
        previous.valueRightNode = null;
        previous.valueParentNode = null;

        if (previous.isBlackValue()) {
            doRedBlackDeleteFixupValue(replacement);
        }
    }


    /**
     * Swaps two nodes (except for their content), taking care of
     * special cases where one is the other's parent ... hey, it
     * happens.
     *
     * @param a one node
     * @param b another node
     */
    private void swapPositionKey(final Node<K, V> a, final Node<K, V> b) {
        // Save initial values.
        final Node<K, V> aParent = a.keyParentNode;
        final Node<K, V> aLeftChild = a.keyLeftNode;
        final Node<K, V> aRightChild = a.keyRightNode;
        final Node<K, V> bParent = b.keyParentNode;
        final Node<K, V> bLeftChild = b.keyLeftNode;
        final Node<K, V> bRightChild = b.keyRightNode;

        if (a == bParent) {
            a.keyParentNode = b;
            b.keyParentNode = aParent;
            if (aParent != null) {
                if (a == aParent.keyLeftNode)
                    aParent.keyLeftNode = b;
                else
                    aParent.keyRightNode = b;
            }
            a.keyLeftNode = bLeftChild;
            a.keyRightNode = bRightChild;
            if (b == aLeftChild) {
                b.keyLeftNode = a;
                b.keyRightNode = aRightChild;
            } else {
                b.keyLeftNode = aLeftChild;
                b.keyRightNode = a;
            }
        } else if (b == aParent) {
            a.keyParentNode = bParent;
            b.keyParentNode = a;
            if (bParent != null) {
                if (b == bParent.keyLeftNode)
                    bParent.keyLeftNode = a;
                else
                    bParent.keyRightNode = a;
            }
            if (a == bLeftChild) {
                a.keyLeftNode = b;
                a.keyRightNode = bRightChild;
            } else {
                a.keyRightNode = b;
                a.keyLeftNode = bLeftChild;
            }
            b.keyLeftNode = aLeftChild;
            b.keyRightNode = aRightChild;
        } else if (aParent != null && bParent != null) {
            a.keyParentNode = bParent;
            b.keyParentNode = aParent;
            if (a == aParent.keyLeftNode)
                aParent.keyLeftNode = b;
            else
                aParent.keyRightNode = b;
            if (b == bParent.keyLeftNode)
                bParent.keyLeftNode = a;
            else
                bParent.keyRightNode = a;
            a.keyLeftNode = bLeftChild;
            a.keyRightNode = bRightChild;
            b.keyLeftNode = aLeftChild;
            b.keyRightNode = aRightChild;
        } else if (aParent != null) {
            a.keyParentNode = null;
            b.keyParentNode = aParent;
            if (a == aParent.keyLeftNode)
                aParent.keyLeftNode = b;
            else
                aParent.keyRightNode = b;
            a.keyLeftNode = bLeftChild;
            a.keyRightNode = bRightChild;
            b.keyLeftNode = aLeftChild;
            b.keyRightNode = aRightChild;
        } else if (bParent != null) {
            a.keyParentNode = bParent;
            b.keyParentNode = null;
            if (b == bParent.keyLeftNode) {
                bParent.keyLeftNode = a;
            } else {
                bParent.keyRightNode = a;
            }
            a.keyLeftNode = bLeftChild;
            a.keyRightNode = bRightChild;
            b.keyLeftNode = aLeftChild;
            b.keyRightNode = aRightChild;
        } else {
            a.keyLeftNode = bLeftChild;
            a.keyRightNode = bRightChild;
            b.keyLeftNode = aLeftChild;
            b.keyRightNode = aRightChild;
        }

        // Fix children's parent pointers
        if (a.keyLeftNode != null)
            a.keyLeftNode.keyParentNode = a;
        if (a.keyRightNode != null)
            a.keyRightNode.keyParentNode = a;
        if (b.keyLeftNode != null)
            b.keyLeftNode.keyParentNode = b;
        if (b.keyRightNode != null)
            b.keyRightNode.keyParentNode = b;

        a.swapColorsKey(b);

        // Check if root changed
        if (rootNodeKey == a)
            rootNodeKey = b;
        else if (rootNodeKey == b)
            rootNodeKey = a;
    }

    private void swapPositionValue(final Node<K, V> a, final Node<K, V> b) {
        // Save initial values.
        final Node<K, V> aParent = a.valueParentNode;
        final Node<K, V> aLeftChild = a.valueLeftNode;
        final Node<K, V> aRightChild = a.valueRightNode;
        final Node<K, V> bParent = b.valueParentNode;
        final Node<K, V> bLeftChild = b.valueLeftNode;
        final Node<K, V> bRightChild = b.valueRightNode;

        if (a == bParent) {
            a.valueParentNode = b;
            b.valueParentNode = aParent;
            if (aParent != null) {
                if (a == aParent.valueLeftNode)
                    aParent.valueLeftNode = b;
                else
                    aParent.valueRightNode = b;
            }
            a.valueLeftNode = bLeftChild;
            a.valueRightNode = bRightChild;
            if (b == aLeftChild) {
                b.valueLeftNode = a;
                b.valueRightNode = aRightChild;
            } else {
                b.valueLeftNode = aLeftChild;
                b.valueRightNode = a;
            }
        } else if (b == aParent) {
            a.valueParentNode = bParent;
            b.valueParentNode = a;
            if (bParent != null) {
                if (b == bParent.valueLeftNode)
                    bParent.valueLeftNode = a;
                else
                    bParent.valueRightNode = a;
            }
            if (a == bLeftChild) {
                a.valueLeftNode = b;
                a.valueRightNode = bRightChild;
            } else {
                a.valueRightNode = b;
                a.valueLeftNode = bLeftChild;
            }
            b.valueLeftNode = aLeftChild;
            b.valueRightNode = aRightChild;
        } else if (aParent != null && bParent != null) {
            a.valueParentNode = bParent;
            b.valueParentNode = aParent;
            if (a == aParent.valueLeftNode)
                aParent.valueLeftNode = b;
            else
                aParent.valueRightNode = b;
            if (b == bParent.valueLeftNode)
                bParent.valueLeftNode = a;
            else
                bParent.valueRightNode = a;
            a.valueLeftNode = bLeftChild;
            a.valueRightNode = bRightChild;
            b.valueLeftNode = aLeftChild;
            b.valueRightNode = aRightChild;
        } else if (aParent != null) {
            a.valueParentNode = null;
            b.valueParentNode = aParent;
            if (a == aParent.valueLeftNode)
                aParent.valueLeftNode = b;
            else
                aParent.valueRightNode = b;
            a.valueLeftNode = bLeftChild;
            a.valueRightNode = bRightChild;
            b.valueLeftNode = aLeftChild;
            b.valueRightNode = aRightChild;
        } else if (bParent != null) {
            a.valueParentNode = bParent;
            b.valueParentNode = null;
            if (b == bParent.valueLeftNode) {
                bParent.valueLeftNode = a;
            } else {
                bParent.valueRightNode = a;
            }
            a.valueLeftNode = bLeftChild;
            a.valueRightNode = bRightChild;
            b.valueLeftNode = aLeftChild;
            b.valueRightNode = aRightChild;
        } else {
            a.valueLeftNode = bLeftChild;
            a.valueRightNode = bRightChild;
            b.valueLeftNode = aLeftChild;
            b.valueRightNode = aRightChild;
        }

        // Fix children's parent pointers
        if (a.valueLeftNode != null)
            a.valueLeftNode.valueParentNode = a;
        if (a.valueRightNode != null)
            a.valueRightNode.valueParentNode = a;
        if (b.valueLeftNode != null)
            b.valueLeftNode.valueParentNode = b;
        if (b.valueRightNode != null)
            b.valueRightNode.valueParentNode = b;

        a.swapColorsValue(b);

        // Check if root changed
        if (rootNodeValue == a)
            rootNodeValue = b;
        else if (rootNodeValue == b)
            rootNodeValue = a;
    }

    /**
     * Checks a key for validity (non-null and implements Comparable)
     *
     * @param key the key to be checked
     * @return key cast as type K
     * @throws NullPointerException if key is null
     * @throws ClassCastException   if key is not Comparable or castable to K
     */
    @SuppressWarnings("unchecked")
    private K checkKey(final Object key) {
        Objects.requireNonNull(key, "key");
        if (!(key instanceof Comparable)) {
            throw new ClassCastException("key must be Comparable");
        }
        return (K) key;
    }

    /**
     * Checks a value for validity (non-null and implements Comparable)
     *
     * @param value the value to be checked
     * @return value cast as type V
     * @throws NullPointerException if value is null
     * @throws ClassCastException   if value is not Comparable or castable to V
     */
    @SuppressWarnings("unchecked")
    private V checkValue(final Object value) {
        Objects.requireNonNull(value, "value");
        if (!(value instanceof Comparable)) {
            throw new ClassCastException("key must be Comparable");
        }
        return (V) value;
    }

    /**
     * Checks a key and a value for validity (non-null and implements
     * Comparable)
     *
     * @param key   the key to be checked
     * @param value the value to be checked
     * @throws NullPointerException if key or value is null
     * @throws ClassCastException   if key or value is not Comparable
     */
    private void checkKeyAndValue(final Object key, final Object value) {
        checkKey(key);
        checkValue(value);
    }

    /**
     * Increments the modification count -- used to check for
     * concurrent modification of the map through the map and through
     * an Iterator from one of its Set or Collection views.
     */
    private void modify() {
        modifications++;
    }

    /**
     * Bumps up the size and note that the map has changed.
     */
    private void grow() {
        modify();
        nodeCount++;
    }

    /**
     * Decrements the size and note that the map has changed.
     */
    private void shrink() {
        modify();
        nodeCount--;
    }

    /**
     * Reads the content of the stream.
     *
     * @param stream the input stream
     * @throws IOException            if an error occurs while reading from the stream
     * @throws ClassNotFoundException if an object read from the stream can not be loaded
     */
    @SuppressWarnings("unchecked") // This will fail at runtime if the stream is incorrect
    private void readObject(final ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        final int size = stream.readInt();
        for (int i = 0; i < size; i++) {
            final K k = (K) stream.readObject();
            final V v = (V) stream.readObject();
            put(k, v);
        }
    }

    /**
     * Writes the content to the stream for serialization.
     *
     * @param stream the output stream
     * @throws IOException if an error occurs while writing to the stream
     */
    private void writeObject(final ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        stream.writeInt(this.size());
        for (final Entry<K, V> entry : entrySet()) {
            stream.writeObject(entry.getKey());
            stream.writeObject(entry.getValue());
        }
    }

    private static <K, V> K getKeyNullSafe(final Entry<K, V> entry) {
        if (entry != null) {
            return entry.getKey();
        } else {
            return null;
        }
    }

    private Node<K, V> firstEntryInRange(final SortedMapRange<K> keyRange) {
        final Node<K, V> candidate = keyRange.hasFrom() ? lookupKeyHigher(keyRange.getFromKey(), keyRange.isFromInclusive())
                                                        : leastNodeKey(rootNodeKey);
        if (keyRange.inRange(candidate.getKey())) {
            return candidate;
        } else {
            return null;
        }
    }

    private Node<K, V> lastEntryInRange(final SortedMapRange<K> keyRange) {
        final Node<K, V> candidate = keyRange.hasTo() ? lookupKeyLower(keyRange.getToKey(), keyRange.isToInclusive())
                                                      : greatestNodeKey(rootNodeKey);
        if (keyRange.inRange(candidate.getKey())) {
            return candidate;
        } else {
            return null;
        }
    }

    /**
     * Base class for all iterators.
     */
    abstract static class BaseIterator<K extends Comparable<K>, V extends Comparable<V>> {
        final TreeBidiMapHard<K, V> parent;

        /**
         * The last node returned by the iterator.
         */
        Node<K, V> lastReturnedNode;
        /**
         * The next node to be returned by the iterator.
         */
        Node<K, V> nextNode;
        /**
         * The previous node in the sequence returned by the iterator.
         */
        Node<K, V> previousNode;
        /**
         * The modification count.
         */
        int expectedModifications;

        /**
         * Constructor.
         */
        BaseIterator(final TreeBidiMapHard<K, V> parent) {
            this.parent = parent;
            this.expectedModifications = parent.modifications;
            reset();
        }

        protected abstract void reset();

        public final boolean hasNext() {
            return nextNode != null;
        }

        public final boolean hasPrevious() {
            return previousNode != null;
        }

        final void checkCanGetKey() {
            if (lastReturnedNode == null) {
                throw new IllegalStateException(
                        "Iterator getKey() can only be called after next() and before remove()");
            }
        }

        final void checkCanGetValue() {
            if (lastReturnedNode == null) {
                throw new IllegalStateException(
                        "Iterator getValue() can only be called after next() and before remove()");
            }
        }
    }

    /**
     * Intermediate iterator class ordering results by key.
     */
    abstract static class IteratorByKey<K extends Comparable<K>, V extends Comparable<V>> extends BaseIterator<K, V> {
        IteratorByKey(final TreeBidiMapHard<K, V> parent) {
            super(parent);
        }

        public final void reset() {
            nextNode = parent.leastNodeKey(parent.rootNodeKey);
            lastReturnedNode = null;
            previousNode = null;
        }

        final Node<K, V> navigateNext() {
            if (nextNode == null) {
                throw new NoSuchElementException();
            }
            if (parent.modifications != expectedModifications) {
                throw new ConcurrentModificationException();
            }
            lastReturnedNode = nextNode;
            previousNode = nextNode;
            nextNode = parent.nextGreaterKey(nextNode);
            return lastReturnedNode;
        }

        final Node<K, V> navigatePrevious() {
            if (previousNode == null) {
                throw new NoSuchElementException();
            }
            if (parent.modifications != expectedModifications) {
                throw new ConcurrentModificationException();
            }
            lastReturnedNode = previousNode;
            nextNode = previousNode;
            previousNode = parent.nextSmallerKey(lastReturnedNode);
            return lastReturnedNode;
        }

        public final void remove() {
            if (lastReturnedNode == null) {
                throw new IllegalStateException();
            }
            if (parent.modifications != expectedModifications) {
                throw new ConcurrentModificationException();
            }
            parent.doRedBlackDelete(lastReturnedNode);
            expectedModifications++;
            if (lastReturnedNode == previousNode) {
                // most recent was navigateNext
                if (nextNode == null) {
                    previousNode = parent.greatestNodeKey(parent.rootNodeKey);
                } else {
                    previousNode = parent.nextSmallerKey(nextNode);
                }
            } else {
                // most recent was navigatePrevious
                if (previousNode == null) {
                    nextNode = parent.leastNodeKey(parent.rootNodeKey);
                } else {
                    nextNode = parent.nextGreaterKey(previousNode);
                }
            }
            lastReturnedNode = null;
        }
    }

    /**
     * Intermediate iterator class ordering results by value.
     */
    abstract static class IteratorByValue<K extends Comparable<K>, V extends Comparable<V>> extends BaseIterator<K, V> {
        IteratorByValue(final TreeBidiMapHard<K, V> parent) {
            super(parent);
        }

        public final void reset() {
            nextNode = parent.leastNodeValue(parent.rootNodeValue);
            lastReturnedNode = null;
            previousNode = null;
        }

        final Node<K, V> navigateNext() {
            if (nextNode == null) {
                throw new NoSuchElementException();
            }
            if (parent.modifications != expectedModifications) {
                throw new ConcurrentModificationException();
            }
            lastReturnedNode = nextNode;
            previousNode = nextNode;
            nextNode = parent.nextGreaterValue(nextNode);
            return lastReturnedNode;
        }

        final Node<K, V> navigatePrevious() {
            if (previousNode == null) {
                throw new NoSuchElementException();
            }
            if (parent.modifications != expectedModifications) {
                throw new ConcurrentModificationException();
            }
            lastReturnedNode = previousNode;
            nextNode = previousNode;
            previousNode = parent.nextSmallerValue(lastReturnedNode);
            return lastReturnedNode;
        }

        public final void remove() {
            if (lastReturnedNode == null) {
                throw new IllegalStateException();
            }
            if (parent.modifications != expectedModifications) {
                throw new ConcurrentModificationException();
            }
            parent.doRedBlackDelete(lastReturnedNode);
            expectedModifications++;
            if (lastReturnedNode == previousNode) {
                // most recent was navigateNext
                if (nextNode == null) {
                    previousNode = parent.greatestNodeValue(parent.rootNodeValue);
                } else {
                    previousNode = parent.nextSmallerValue(nextNode);
                }
            } else {
                // most recent was navigatePrevious
                if (previousNode == null) {
                    nextNode = parent.leastNodeValue(parent.rootNodeValue);
                } else {
                    nextNode = parent.nextGreaterValue(previousNode);
                }
            }
            lastReturnedNode = null;
        }
    }

    /**
     * An iterator over the map.
     */
    private static final class MapIteratorKeyByKey<K extends Comparable<K>, V extends Comparable<V>>
            extends IteratorByKey<K, V>
            implements OrderedMapIterator<K, V> {
        private MapIteratorKeyByKey(final TreeBidiMapHard<K, V> parent) {
            super(parent);
        }

        @Override
        public K getKey() {
            checkCanGetKey();
            return lastReturnedNode.getKey();
        }

        @Override
        public V getValue() {
            checkCanGetValue();
            return lastReturnedNode.getValue();
        }

        @Override
        public V setValue(final V obj) {
            throw new UnsupportedOperationException();
        }

        @Override
        public K next() {
            return navigateNext().getKey();
        }

        @Override
        public K previous() {
            return navigatePrevious().getKey();
        }
    }

    /**
     * An iterator over the map.
     */
    private static final class MapIteratorKeyByValue<K extends Comparable<K>, V extends Comparable<V>>
            extends IteratorByValue<K, V>
            implements OrderedMapIterator<K, V> {
        private MapIteratorKeyByValue(final TreeBidiMapHard<K, V> parent) {
            super(parent);
        }

        @Override
        public K getKey() {
            checkCanGetKey();
            return lastReturnedNode.getKey();
        }

        @Override
        public V getValue() {
            checkCanGetValue();
            return lastReturnedNode.getValue();
        }

        @Override
        public V setValue(final V obj) {
            throw new UnsupportedOperationException();
        }

        @Override
        public K next() {
            return navigateNext().getKey();
        }

        @Override
        public K previous() {
            return navigatePrevious().getKey();
        }
    }

    /**
     * An iterator over the map.
     */
    private static final class MapIteratorValueByKey<K extends Comparable<K>, V extends Comparable<V>>
            extends IteratorByKey<K, V>
            implements OrderedMapIterator<V, K> {
        private MapIteratorValueByKey(final TreeBidiMapHard<K, V> parent) {
            super(parent);
        }

        @Override
        public V getKey() {
            checkCanGetKey();
            return lastReturnedNode.getValue();
        }

        @Override
        public K getValue() {
            checkCanGetValue();
            return lastReturnedNode.getKey();
        }

        @Override
        public K setValue(final K obj) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V next() {
            return navigateNext().getValue();
        }

        @Override
        public V previous() {
            return navigatePrevious().getValue();
        }
    }

    /**
     * An iterator over the map.
     */
    private static final class MapIteratorValueByValue<K extends Comparable<K>, V extends Comparable<V>>
            extends IteratorByValue<K, V>
            implements OrderedMapIterator<V, K> {
        private MapIteratorValueByValue(final TreeBidiMapHard<K, V> parent) {
            super(parent);
        }

        @Override
        public V getKey() {
            checkCanGetKey();
            return lastReturnedNode.getValue();
        }

        @Override
        public K getValue() {
            checkCanGetValue();
            return lastReturnedNode.getKey();
        }

        @Override
        public K setValue(final K obj) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V next() {
            return navigateNext().getValue();
        }

        @Override
        public V previous() {
            return navigatePrevious().getValue();
        }
    }


    /**
     * An iterator over the map entries.
     */
    private static final class EntryIteratorStandardByKey<K extends Comparable<K>, V extends Comparable<V>>
            extends IteratorByKey<K, V>
            implements OrderedIterator<Entry<K, V>> {
        private EntryIteratorStandardByKey(final TreeBidiMapHard<K, V> parent) {
            super(parent);
        }

        @Override
        public Entry<K, V> next() {
            return navigateNext().copyEntryStandard();
        }

        @Override
        public Entry<K, V> previous() {
            return navigatePrevious().copyEntryStandard();
        }
    }

    private static final class EntryIteratorInvertedByValue<K extends Comparable<K>, V extends Comparable<V>>
            extends IteratorByValue<K, V>
            implements OrderedIterator<Entry<V, K>> {
        private EntryIteratorInvertedByValue(final TreeBidiMapHard<K, V> parent) {
            super(parent);
        }

        @Override
        public Entry<V, K> next() {
            return navigateNext().copyEntryInverted();
        }

        @Override
        public Entry<V, K> previous() {
            return navigatePrevious().copyEntryInverted();
        }
    }

    /**
     * A node used to store the data.
     */
    @SuppressWarnings({"MagicNumber", "ImplicitNumericConversion", "ClassWithTooManyFields"})
    private static final class Node<K extends Comparable<K>, V extends Comparable<V>> implements Entry<K, V>, KeyValue<K, V> {

        // TODO make finals (replacing)

        private K key;
        private V value;
        private Node<K, V> keyLeftNode;
        private Node<K, V> keyRightNode;
        private Node<K, V> keyParentNode;
        private Node<K, V> valueLeftNode;
        private Node<K, V> valueRightNode;
        private Node<K, V> valueParentNode;
        private int hashCodeValue;
        private boolean calculatedHashCode;
        private short colorFlags;

        /**
         * Makes a new cell with given key and value, and with null
         * links, and black (true) colors.
         *
         * @param key   the key of this node
         * @param value the value of this node
         */
        Node(final K key, final V value) {
            this.key = key;
            this.value = value;
            colorFlags = 0x3;
            calculatedHashCode = false;
        }

        private void swapColorsValue(final Node<K, V> node) {
            final int tmp = node.colorFlags & 0x2;
            node.colorFlags = (short) ((node.colorFlags & 0x1) | (colorFlags & 0x2));
            colorFlags = (short) ((colorFlags & 0x1) | tmp);
        }

        private void swapColorsKey(final Node<K, V> node) {
            final int tmp = node.colorFlags & 0x1;
            node.colorFlags = (short) ((node.colorFlags & 0x2) | (colorFlags & 0x1));
            colorFlags = (short) ((colorFlags & 0x2) | tmp);
        }

        private boolean isBlackValue() {
            return (colorFlags & 0x2) != 0;
        }

        private boolean isBlackKey() {
            return (colorFlags & 0x1) != 0;
        }

        private boolean isRedValue() {
            return (colorFlags & 0x2) == 0;
        }

        private boolean isRedKey() {
            return (colorFlags & 0x1) == 0;
        }

        private void setBlackValue() {
            colorFlags |= 0x2;
        }

        private void setBlackKey() {
            colorFlags |= 0x1;
        }

        private void setRedValue() {
            colorFlags &= 0xFFFD;
        }

        private void setRedKey() {
            colorFlags &= 0xFFFE;
        }

        private void copyColorValue(final Node<K, V> node) {
            colorFlags = (short) ((colorFlags & 0x1) | (node.colorFlags & 0x2));
        }

        private void copyColorKey(final Node<K, V> node) {
            colorFlags = (short) ((colorFlags & 0x2) | (node.colorFlags & 0x1));
        }

        private Entry<V, K> copyEntryInverted() {
            return new UnmodifiableMapEntry<>(value, key);
        }

        private Entry<K, V> copyEntryStandard() {
            return new UnmodifiableMapEntry<>(key, value);
        }

        /**
         * Gets the key.
         *
         * @return the key corresponding to this entry.
         */
        @Override
        public K getKey() {
            return key;
        }

        /**
         * Gets the value.
         *
         * @return the value corresponding to this entry.
         */
        @Override
        public V getValue() {
            return value;
        }

        /**
         * Optional operation that is not permitted in this implementation.
         *
         * @param ignored this parameter is ignored.
         * @return does not return
         * @throws UnsupportedOperationException always
         */
        @Override
        public V setValue(final V ignored) throws UnsupportedOperationException {
            throw new UnsupportedOperationException("Map.Entry.setValue is not supported");
        }

        boolean isKeyLessThanOrEqual(final Node<K, V> other) {
            return key.compareTo(other.key) <= 0;
        }

        boolean isKeyLessThan(final Node<K, V> other) {
            return key.compareTo(other.key) < 0;
        }

        boolean isValueLessThanOrEqual(final Node<K, V> other) {
            return value.compareTo(other.value) <= 0;
        }

        /**
         * Compares the specified object with this entry for equality.
         * Returns true if the given object is also a map entry and
         * the two entries represent the same mapping.
         *
         * @param obj the object to be compared for equality with this entry.
         * @return true if the specified object is equal to this entry.
         */
        @Override
        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof Map.Entry)) {
                return false;
            }
            final Entry<?, ?> e = (Entry<?, ?>) obj;
            return key.equals(e.getKey()) && value.equals(e.getValue());
        }

        /**
         * @return the hash code value for this map entry.
         */
        @Override
        public int hashCode() {
            if (!calculatedHashCode) {
                hashCodeValue = key.hashCode() ^ value.hashCode();
                calculatedHashCode = true;
            }
            return hashCodeValue;
        }
    }

    /**
     * The inverse map implementation.
     */
    public static final class Inverse<K extends Comparable<K>, V extends Comparable<V>>
            extends AbstractExtendedBidiMap<V, K, TreeBidiSubMap<V, K>, TreeBidiMapHard<K, V>> {

        private static final long serialVersionUID = -5940400507869486450L;
        private final TreeBidiMapHard<K, V> parent;

        Inverse(final TreeBidiMapHard<K, V> parent) {
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
        public K getOrDefault(final Object value, final K defaultKey) {
            return parent.getKeyOrDefault(value, defaultKey);
        }

        @Override
        public V getKeyOrDefault(final Object value, final V defaultKey) {
            return parent.getOrDefault(value, defaultKey);
        }

        @Override
        public boolean containsKey(final Object key) {
            return parent.containsValue(key);
        }

        @Override
        public boolean containsEntry(final Object key, final Object value) {
            return parent.containsEntry(value, key);
        }

        @Override
        public V firstKey() {
            if (parent.nodeCount == 0) {
                throw new NoSuchElementException("Map is empty");
            }
            return parent.leastNodeValue(parent.rootNodeValue).getValue();
        }

        @Override
        public V lastKey() {
            if (parent.nodeCount == 0) {
                throw new NoSuchElementException("Map is empty");
            }
            return parent.greatestNodeValue(parent.rootNodeValue).getValue();
        }

        @Override
        public V nextKey(final V key) {
            final Node<K, V> rval = parent.lookupValueHigher(parent.checkValue(key));
            return rval == null ? null : rval.getValue();
        }

        @Override
        public V previousKey(final V key) {
            final Node<K, V> rval = parent.lookupValueLower(parent.checkValue(key));
            return rval == null ? null : rval.getValue();
        }

        @Override
        protected K doPut(final V value, final K key, final boolean addIfAbsent, final boolean updateIfPresent) {
            parent.checkKeyAndValue(key, value);
            return parent.doPutValueFirst(value, key, addIfAbsent,  updateIfPresent);
        }

        @Override
        protected K doPut(final V key, final Function<? super V, ? extends K> absentFunc, final BiFunction<? super V, ? super K, ? extends K> presentFunc, final boolean saveNulls) {
            return parent.doPutValueFirst(key, absentFunc, presentFunc);
        }

        @Override
        public K remove(final Object key) {
            return parent.removeValue(key);
        }

        @Override
        public boolean remove(final Object key, final Object value) {
            return parent.remove(value, key);
        }

        @Override
        public boolean removeAsBoolean(final Object value) {
            return parent.removeValueAsBoolean(value);
        }

        @Override
        public V removeValue(final Object value) {
            return parent.remove(value);
        }

        @Override
        public boolean removeValueAsBoolean(final Object value) {
            return parent.removeAsBoolean(value);
        }

        @Override
        public void clear() {
            parent.clear();
        }

        @Override
        public OrderedMapIterator<V, K> mapIterator() {
            if (isEmpty()) {
                return EmptyOrderedMapIterator.emptyOrderedMapIterator();
            }
            return new MapIteratorValueByValue<>(parent);
        }

        @Override
        public MapSpliterator<V, K> mapSpliterator() {
            // TODO
            return null;
        }

        @Override
        public Iterator<Entry<V, K>> entryIterator() {
            return super.entryIterator(); // TODO
        }

        @Override
        public TreeBidiMapHard<K, V> inverseBidiMap() {
            return parent;
        }

        @Override
        public Comparator<? super V> comparator() {
            return null; // natural order
        }

        @Override
        public Comparator<? super K> valueComparator() {
            return null; // natural order
        }

        @Override
        public SortedMapRange<V> getKeyRange() {
            return SortedMapRange.full(null);
        }

        @Override
        public SortedMapRange<K> getValueRange() {
            return SortedMapRange.full(null);
        }

        @Override
        public TreeBidiSubMap<V, K> subMap(final SortedMapRange<V> range) {
            throw new UnsupportedOperationException("TreeBidiMap can't combine inverse and sub map operations");
        }
    }

    public static final class TreeBidiSubMap<K extends Comparable<K>, V extends Comparable<V>>
            extends AbstractExtendedBidiMap<K, V, TreeBidiSubMap<K, V>, TreeBidiSubMap<V, K>> {

        private static final long serialVersionUID = 7793720431038658603L;
        private final TreeBidiMapHard<K, V> parent;
        private final SortedMapRange<K> keyRange;

        TreeBidiSubMap(final TreeBidiMapHard<K, V> parent, final SortedMapRange<K> keyRange) {
            this.parent = parent;
            this.keyRange = keyRange;
        }

        private void verifyRange(final K key) {
            if (!keyRange.inRange(key)) {
                throw new IllegalArgumentException("key");
            }
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean containsKey(final Object keyObject) {
            final K key = parent.checkKey(keyObject);
            if (keyRange.inRange(key)) {
                return parent.lookupKey(key) != null;
            } else {
                return false;
            }
        }

        @Override
        public Iterator<Entry<K, V>> entryIterator() {
            return null; // TODO
        }

        @Override
        public OrderedMapIterator<K, V> mapIterator() {
            // TODO
            return new MapIteratorKeyByKey<>(parent);
        }

        @Override
        public MapSpliterator<K, V> mapSpliterator() {
            return new KeyRangeMapSpliterator<>(parent, keyRange);
        }

        @Override
        public V getOrDefault(final Object keyObject, final V defaultValue) {
            final K key = parent.checkKey(keyObject);
            if (keyRange.inRange(key)) {
                final Node<K, V> node = parent.lookupKey(key);
                if (node != null) {
                    return node.value;
                }
            }
            return defaultValue;
        }

        @Override
        public boolean containsEntry(final Object keyObject, final Object valueObject) {
            final K key = parent.checkKey(keyObject);
            final V value = parent.checkValue(valueObject);
            if (keyRange.inRange(key)) {
                final Node<K, V> node = parent.lookupKey(key);
                if (node != null) {
                    return Objects.equals(node.value, value);
                }
            }
            return false;
        }

        @Override
        public K getKeyOrDefault(final Object valueObject, final K defaultKey) {
            final V value = parent.checkValue(valueObject);
            final Node<K, V> node = parent.lookupValue(value);
            if (node != null && keyRange.inRange(node.key)) {
                return node.key;
            }
            return defaultKey;
        }

        @Override
        public K removeValue(final Object valueObject) {
            final V value = parent.checkValue(valueObject);
            final Node<K, V> node = parent.lookupValue(value);
            if (node != null && keyRange.inRange(node.key)) {
                parent.doRedBlackDelete(node);
                return node.key;
            }
            return null;
        }

        @Override
        public V remove(final Object keyObject) {
            final K key = parent.checkKey(keyObject);
            if (keyRange.inRange(key)) {
                return parent.remove(key);
            }
            return null;
        }

        @Override
        public boolean remove(final Object keyObject, final Object value) {
            final K key = parent.checkKey(keyObject);
            if (keyRange.inRange(key)) {
                return parent.remove(key,  value);
            }
            return false;
        }

        @Override
        public boolean removeAsBoolean(final Object keyObject) {
            final K key = parent.checkKey(keyObject);
            if (keyRange.inRange(key)) {
                return parent.removeAsBoolean(keyObject);
            }
            return false;
        }

        @Override
        protected V doPut(final K key, final V value, final boolean addIfAbsent, final boolean updateIfPresent) {
            verifyRange(key);
            return parent.doPut(key, value, addIfAbsent, updateIfPresent);
        }

        @Override
        protected V doPut(final K key, final Function<? super K, ? extends V> absentFunc, final BiFunction<? super K, ? super V, ? extends V> presentFunc, final boolean saveNulls) {
            verifyRange(key);
            return parent.doPut(key, absentFunc, presentFunc, saveNulls);
        }

        @Override
        public void putAll(final Map<? extends K, ? extends V> m) {
            for (final K key : m.keySet()) {
                verifyRange(key);
            }
            parent.putAll(m);
        }

        @Override
        public void clear() {

        }

        private Entry<K, V> nextEntry(final K key) {
            return parent.lookupKeyHigher(key, false);
        }

        private Entry<K, V> previousEntry(final K key) {
            return parent.lookupKeyLower(key, false);
        }

        @Override
        public K firstKey() {
            return getKeyNullSafe(parent.firstEntryInRange(keyRange));
        }

        @Override
        public K lastKey() {
            return getKeyNullSafe(parent.lastEntryInRange(keyRange));
        }

        @Override
        public K nextKey(final K key) {
            return getKeyNullSafe(nextEntry(key));
        }

        @Override
        public K previousKey(final K key) {
            return getKeyNullSafe(previousEntry(key));
        }

        @Override
        public Comparator<? super V> valueComparator() {
            return null; // natural order
        }

        @Override
        public Comparator<? super K> comparator() {
            return null; // natural order
        }

        @Override
        public SortedMapRange<K> getKeyRange() {
            return keyRange;
        }

        @Override
        public SortedMapRange<V> getValueRange() {
            return SortedMapRange.full(null);
        }

        @Override
        public TreeBidiSubMap<K, V> subMap(final SortedMapRange<K> range) {
            return new TreeBidiSubMap<>(parent, range);
        }

        @Override
        public TreeBidiSubMap<V, K> inverseBidiMap() {
            throw new UnsupportedOperationException("TreeBidiMap can't combine inverse and sub map operations");
        }
    }

    private static final class KeyMapSpliterator<K extends Comparable<K>, V extends Comparable<V>>
            extends AbstractTreeSpliterator<K, V, Node<K, V>> {
        private final TreeBidiMapHard<K, V> parent;

        private KeyMapSpliterator(final TreeBidiMapHard<K, V> parent) {
            super();
            this.parent = parent;
        }

        private KeyMapSpliterator(final TreeBidiMapHard<K, V> parent, final SplitState state,
                                  final Node<K, V> currentNode, final Node<K, V> lastNode, final long estimatedSize) {
            super(state, currentNode, lastNode, estimatedSize);
            this.parent = parent;
        }

        @Override
        protected AbstractTreeSpliterator<K, V, Node<K, V>> makeSplit(final SplitState state,
                                                                      final Node<K, V> currentNode, final Node<K, V> lastNode, final long estimatedSize) {
            return new KeyMapSpliterator<>(parent, state, currentNode, lastNode, estimatedSize);
        }

        @Override
        protected int modCount() {
            return parent.modifications;
        }

        @Override
        protected Node<K, V> rootNode() {
            return parent.rootNodeKey;
        }

        @Override
        protected Node<K, V> getLeft(final Node<K, V> node) {
            return node.keyLeftNode;
        }

        @Override
        protected Node<K, V> getRight(final Node<K, V> node) {
            return node.keyRightNode;
        }

        @Override
        protected Node<K, V> nextLower(final Node<K, V> node) {
            return parent.nextSmallerKey(node);
        }

        @Override
        protected Node<K, V> nextGreater(final Node<K, V> node) {
            return parent.nextGreaterKey(node);
        }

        @Override
        protected Node<K, V> subTreeLowest(final Node<K, V> node) {
            return parent.leastNodeKey(node);
        }

        @Override
        protected Node<K, V> subTreeGreatest(final Node<K, V> node) {
            return parent.greatestNodeKey(node);
        }

        @Override
        protected boolean isLowerThanOrEqual(final Node<K, V> node, final Node<K, V> other) {
            return node.isKeyLessThanOrEqual(other);
        }

        @Override
        protected boolean isLowerThan(final Node<K, V> node, final Node<K, V> other) {
            return node.isKeyLessThan(other);
        }
    }

    private static final class KeyRangeMapSpliterator<K extends Comparable<K>, V extends Comparable<V>>
            extends AbstractTreeRangeSpliterator<K, V, Node<K, V>> {
        private final TreeBidiMapHard<K, V> parent;

        private KeyRangeMapSpliterator(final TreeBidiMapHard<K, V> parent, final SortedMapRange<K> range) {
            super(range);
            this.parent = parent;
        }

        private KeyRangeMapSpliterator(final TreeBidiMapHard<K, V> parent, final SortedMapRange<K> range, final SplitState state,
                                       final Node<K, V> currentNode, final Node<K, V> lastNode, final long estimatedSize) {
            super(range, state, currentNode, lastNode, estimatedSize);
            this.parent = parent;
        }

        @Override
        protected AbstractTreeSpliterator<K, V, Node<K, V>> makeSplit(final SplitState state,
                                                                      final Node<K, V> currentNode, final Node<K, V> lastNode, final long estimatedSize) {
            return new KeyRangeMapSpliterator<>(parent, keyRange, state, currentNode, lastNode, estimatedSize);
        }

        @Override
        protected int modCount() {
            return parent.modifications;
        }

        @Override
        protected Node<K, V> rootNode() {
            return parent.rootNodeKey;
        }

        @Override
        protected Node<K, V> getLeft(final Node<K, V> node) {
            return node.keyLeftNode;
        }

        @Override
        protected Node<K, V> getRight(final Node<K, V> node) {
            return node.keyRightNode;
        }

        @Override
        protected Node<K, V> nextLower(final Node<K, V> node) {
            return parent.nextSmallerKey(node);
        }

        @Override
        protected Node<K, V> nextGreater(final Node<K, V> node) {
            return parent.nextGreaterKey(node);
        }

        @Override
        protected Node<K, V> subTreeLowest(final Node<K, V> node) {
            return parent.leastNodeKey(node);
        }

        @Override
        protected Node<K, V> subTreeGreatest(final Node<K, V> node) {
            return parent.greatestNodeKey(node);
        }

        @Override
        protected boolean isLowerThan(final Node<K, V> node, final Node<K, V> other) {
            return node.isKeyLessThan(other);
        }

        @Override
        protected boolean isLowerThanOrEqual(final Node<K, V> node, final Node<K, V> other) {
            return node.isKeyLessThanOrEqual(other);
        }

        @Override
        protected Node<K, V> findFirstNode() {
            return parent.firstEntryInRange(keyRange);
        }

        @Override
        protected Node<K, V> findLastNode() {
            return parent.lastEntryInRange(keyRange);
        }
    }
}
