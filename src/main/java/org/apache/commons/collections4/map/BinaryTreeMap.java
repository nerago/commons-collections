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
package org.apache.commons.collections4.map;

import org.apache.commons.collections4.IterableSortedMap;
import org.apache.commons.collections4.KeyValue;
import org.apache.commons.collections4.OrderedIterator;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.ResettableIterator;
import org.apache.commons.collections4.SortedMapRange;
import org.apache.commons.collections4.iterators.EmptyIterator;
import org.apache.commons.collections4.iterators.EmptyOrderedMapIterator;
import org.apache.commons.collections4.keyvalue.UnmodifiableMapEntry;
import org.apache.commons.collections4.spliterators.AbstractTreeRangeSpliterator;
import org.apache.commons.collections4.spliterators.AbstractTreeSpliterator;
import org.apache.commons.collections4.spliterators.EmptyMapSpliterator;
import org.apache.commons.collections4.spliterators.MapSpliterator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.AbstractSet;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;



/**
 * Red-Black tree-based implementation of BidiMap where all objects added
 * implement the {@code Comparable} interface.
 * <p>
 * This class guarantees that the map will be in both ascending key order
 * and ascending value order, sorted according to the natural order for
 * the key's class.
 * </p>
 * <p>
 * The Map.Entry instances returned by the appropriate methods will
 * not allow setValue() and will throw an
 * UnsupportedOperationException on attempts to call that method.
 * </p>
 *
 * @param <K> the type of the keys in this map
 * @param <V> the type of the values in this map
 * @since X.X
 */
@SuppressWarnings({"OverlyComplexMethod", "OverlyLongMethod", "InstanceVariableMayNotBeInitializedByReadObject"})
public final class BinaryTreeMap<K extends Comparable<K>, V>
        extends AbstractIterableSortedMap<K, V> {

    private static final long serialVersionUID = 5398917388048351226L;
    
    private transient Node<K, V> rootNodeKey;
    private transient int nodeCount;
    private transient int modifications;

    /**
     * Constructs a new empty TreeBidiMap.
     */
    public BinaryTreeMap() {
    }

    /**
     * Constructs a new TreeBidiMap by copying an existing Map.
     *
     * @param map the map to copy
     * @throws ClassCastException   if the keys/values in the map are
     *                              not Comparable or are not mutually comparable
     * @throws NullPointerException if any key or value in the map is null
     */
    public BinaryTreeMap(final Map<? extends K, ? extends V> map) {
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
    public Comparator<? super K> comparator() {
        return null; // natural order
    }

    @Override
    public SortedMapRange<K> getKeyRange() {
        return SortedMapRange.full(null);
    }

    @Override
    public IterableSortedMap<K, V> subMap(final SortedMapRange<K> range) {
        return new TreeSubMap(range);
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
        return isEmpty() ? EmptyOrderedMapIterator.emptyOrderedMapIterator()
                         : new MapIteratorKeyByKey();
    }

    @Override
    public Iterator<Entry<K, V>> entryIterator() {
        return isEmpty() ? EmptyIterator.emptyIterator()
                         : new EntryIteratorStandardByKey();
    }

    @Override
    public MapSpliterator<K, V> mapSpliterator() {
        return isEmpty() ? EmptyMapSpliterator.emptyMapSpliterator()
                         : new KeyMapSpliterator();
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
                if (presentFunc != null) {
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

    private void updateValue(final Node<K, V> node, final V value) {
        // remove from value tree
        doRedBlackDeleteValue(node);

        // update value
        node.value = value;
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

    /**
     * Complicated red-black insert stuff. Based on Sun's TreeMap
     * implementation, though it's barely recognizable anymore.
     *
     * @param insertedNode the node to be inserted
     */
    private void doRedBlackInsertKey(final Node<K, V> insertedNode) {
        Node<K, V> currentNode = insertedNode;
        if (currentNode != null) {
            currentNode.setRed();
        }

        while (currentNode != null && currentNode != rootNodeKey && currentNode.keyParentNode != null && currentNode.keyParentNode.isRed()) {
            final Node<K, V> parent = currentNode.keyParentNode;
            final Node<K, V> grandParent = parent.keyParentNode;
            if (parent.keyLeftNode == currentNode) {
                final Node<K, V> grandParentRight = grandParent != null ? grandParent.keyRightNode : null;

                if (grandParentRight != null && grandParentRight.isRed()) {
                    parent.setBlack();
                    grandParentRight.setBlack();
                    grandParent.setRed();
                    currentNode = grandParent;
                } else {
                    parent.setBlack();
                    if (grandParent != null) {
                        grandParent.setRed();
                        rotateRightKey(grandParent);
                    }
                }
            } else {
                // just like clause above, except swap left for right
                final Node<K, V> grandParentLeft = grandParent != null ? grandParent.keyLeftNode : null;

                if (grandParentLeft != null && grandParentLeft.isRed()) {
                    parent.setBlack();
                    grandParentLeft.setBlack();
                    grandParent.setRed();
                    currentNode = grandParent;
                } else {
                    parent.setBlack();
                    if (grandParent != null) {
                        grandParent.setRed();
                        rotateLeftKey(grandParent);
                    }
                }
            }
        }

        if (rootNodeKey != null) {
            rootNodeKey.setBlack();
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
                if (deletedNode.isBlack()) {
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
            if (!currentNode.isBlack()) break;
            Node<K, V> parent = currentNode.keyParentNode;
            final boolean isLeftChild = parent.keyLeftNode == currentNode;
            if (isLeftChild) {
                Node<K, V> siblingNode = parent.keyRightNode;

                if (siblingNode == null) {
                    currentNode = parent;
                    continue;
                }

                if (siblingNode.isRed()) {
                    siblingNode.setBlack();
                    parent.setRed();
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
                if ((siblingLeft == null || siblingLeft.isBlack()) && (siblingRight == null || siblingRight.isBlack())) {
                    siblingNode.setRed();
                    currentNode = parent;
                } else {
                    if (siblingRight == null || siblingRight.isBlack()) {
                        if (siblingLeft != null) {
                            siblingLeft.setBlack();
                        }
                        siblingNode.setRed();
                        rotateRightKey(siblingNode);
                        parent = currentNode.keyParentNode;
                        siblingNode = parent.keyRightNode;
                    }

                    if (siblingNode != null) {
                        siblingNode.copyColor(parent);
                    }
                    parent.setBlack();
                    if (siblingRight != null) {
                        siblingRight.setBlack();
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

                if (siblingNode.isRed()) {
                    siblingNode.setBlack();
                    parent.setRed();
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
                if ((siblingLeft == null || siblingLeft.isBlack()) && (siblingRight == null || siblingRight.isBlack())) {
                    siblingNode.setRed();
                    currentNode = parent;
                } else {
                    if (siblingLeft == null || siblingLeft.isBlack()) {
                        if (siblingRight != null) {
                            siblingRight.setBlack();
                        }
                        siblingNode.setRed();
                        rotateLeftKey(siblingNode);
                        parent = currentNode.keyParentNode;
                        siblingNode = parent.keyLeftNode;
                    }

                    if (siblingNode != null) {
                        siblingNode.copyColor(parent);
                    }
                    parent.setBlack();
                    if (siblingLeft != null) {
                        siblingLeft.setBlack();
                    }
                    rotateRightKey(parent);
                    break;
                }
            }
        }

        if (currentNode != null) {
            currentNode.setBlack();
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

        if (previous.isBlack()) {
            doRedBlackDeleteFixupKey(replacement);
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

        a.swapColors(b);

        // Check if root changed
        if (rootNodeKey == a)
            rootNodeKey = b;
        else if (rootNodeKey == b)
            rootNodeKey = a;
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
     * A view of this map.
     */
    private abstract class View<E> extends AbstractSet<E> {
        @Override
        public final int size() {
            return BinaryTreeMap.this.size();
        }

        @Override
        public final void clear() {
            BinaryTreeMap.this.clear();
        }
    }

    private abstract class KeyView extends View<K> {
        @Override
        public final boolean contains(final Object obj) {
            return lookupKey(checkKey(obj)) != null;
        }

        @Override
        public final boolean remove(final Object o) {
            V result = null;
            final Node<K, V> node = lookupKey(checkKey(o));
            if (node != null) {
                doRedBlackDelete(node);
                result = node.getValue();
            }
            return result != null;
        }
    }

//    private final class KeyViewByKeys extends KeyView {
//        @Override
//        public Iterator<K> iterator() {
//            return new MapIteratorKeyByKey();
//        }
//
//        @Override
//        public Spliterator<K> spliterator() {
//            return new SpliteratorKeyByKey();
//        }
//    }
//
//    private final class KeyViewByValue extends View<K> {
//        @Override
//        public Iterator<K> iterator() {
//            return new MapIteratorKeyByValue();
//        }
//
//        @Override
//        public Spliterator<K> spliterator() {
//            return new SpliteratorKeyByValue();
//        }
//    }
//
//    private abstract class ValueView extends View<V> {
//        @Override
//        public final boolean contains(final Object obj) {
//            return lookupValue(checkValue(obj)) != null;
//        }
//
//        @Override
//        public final boolean remove(final Object obj) {
//            K result = null;
//            final Node<K, V> node = lookupValue(checkValue(obj));
//            if (node != null) {
//                doRedBlackDelete(node);
//                result = node.getKey();
//            }
//            return result != null;
//        }
//    }
//
//    private final class ValueViewByKey extends ValueView {
//        @Override
//        public Iterator<V> iterator() {
//            return new MapIteratorValueByKey();
//        }
//
//        @Override
//        public Spliterator<V> spliterator() {
//            return new SpliteratorValueByKey();
//        }
//    }
//
//    private final class ValueViewByValue extends ValueView {
//        @Override
//        public Iterator<V> iterator() {
//            return new MapIteratorValueByValue();
//        }
//
//        @Override
//        public Spliterator<V> spliterator() {
//            return new SpliteratorValueByValue();
//        }
//    }

    /**
     * A view of this map.
     */
//    private final class EntryView extends View<Entry<K, V>> {
//        @Override
//        public boolean contains(final Object obj) {
//            if (!(obj instanceof Map.Entry)) {
//                return false;
//            }
//            final Entry<?, ?> entry = (Entry<?, ?>) obj;
//            final K key = checkKey(entry.getKey());
//            final V value = checkValue(entry.getValue());
//            final Node<K, V> node = lookupKey(key);
//            return node != null && node.getValue().equals(value);
//        }
//
//        @Override
//        public boolean remove(final Object obj) {
//            if (!(obj instanceof Map.Entry)) {
//                return false;
//            }
//            final Entry<?, ?> entry = (Entry<?, ?>) obj;
//            final K key = checkKey(entry.getKey());
//            final V value = checkValue(entry.getValue());
//            final Node<K, V> node = lookupKey(key);
//            if (node != null && node.getValue().equals(value)) {
//                doRedBlackDelete(node);
//                return true;
//            }
//            return false;
//        }
//
//        @Override
//        public Iterator<Entry<K, V>> iterator() {
//            return new EntryIteratorStandardByKey();
//        }
//
//        @Override
//        public Spliterator<Entry<K, V>> spliterator() {
//            return new SpliteratorEntryByKey();
//        }
//    }

    /**
     * A view of this map.
     */
//    private final class InverseEntryView extends View<Entry<V, K>> {
//        @Override
//        public boolean contains(final Object obj) {
//            if (!(obj instanceof Map.Entry)) {
//                return false;
//            }
//            final Entry<?, ?> entry = (Entry<?, ?>) obj;
//            final K key = checkKey(entry.getValue());
//            final V value = checkValue(entry.getKey());
//            final Node<K, V> node = lookupValue(value);
//            return node != null && node.getKey().equals(key);
//        }
//
//        @Override
//        public boolean remove(final Object obj) {
//            if (!(obj instanceof Map.Entry)) {
//                return false;
//            }
//            final Entry<?, ?> entry = (Entry<?, ?>) obj;
//            final K key = checkKey(entry.getValue());
//            final V value = checkValue(entry.getKey());
//            final Node<K, V> node = lookupValue(value);
//            if (node != null && node.getKey().equals(key)) {
//                doRedBlackDelete(node);
//                return true;
//            }
//            return false;
//        }
//
//        @Override
//        public Iterator<Entry<V, K>> iterator() {
//            return new EntryIteratorInvertedByValue();
//        }
//
//        @Override
//        public Spliterator<Entry<V, K>> spliterator() {
//            return new SpliteratorEntryInvertedByValue();
//        }
//    }

    /**
     * Base class for all iterators.
     */
    abstract class BaseIterator {
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
        BaseIterator() {
            expectedModifications = modifications;
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
    abstract class IteratorByKey extends BaseIterator {
        public final void reset() {
            nextNode = leastNodeKey(rootNodeKey);
            lastReturnedNode = null;
            previousNode = null;
        }

        final Node<K, V> navigateNext() {
            if (nextNode == null) {
                throw new NoSuchElementException();
            }
            if (modifications != expectedModifications) {
                throw new ConcurrentModificationException();
            }
            lastReturnedNode = nextNode;
            previousNode = nextNode;
            nextNode = nextGreaterKey(nextNode);
            return lastReturnedNode;
        }

        final Node<K, V> navigatePrevious() {
            if (previousNode == null) {
                throw new NoSuchElementException();
            }
            if (modifications != expectedModifications) {
                throw new ConcurrentModificationException();
            }
            lastReturnedNode = previousNode;
            nextNode = previousNode;
            previousNode = nextSmallerKey(lastReturnedNode);
            return lastReturnedNode;
        }

        public final void remove() {
            if (lastReturnedNode == null) {
                throw new IllegalStateException();
            }
            if (modifications != expectedModifications) {
                throw new ConcurrentModificationException();
            }
            doRedBlackDelete(lastReturnedNode);
            expectedModifications++;
            if (lastReturnedNode == previousNode) {
                // most recent was navigateNext
                if (nextNode == null) {
                    previousNode = greatestNodeKey(rootNodeKey);
                } else {
                    previousNode = nextSmallerKey(nextNode);
                }
            } else {
                // most recent was navigatePrevious
                if (previousNode == null) {
                    nextNode = leastNodeKey(rootNodeKey);
                } else {
                    nextNode = nextGreaterKey(previousNode);
                }
            }
            lastReturnedNode = null;
        }
    }

    /**
     * An iterator over the map.
     */
    private final class MapIteratorKeyByKey extends IteratorByKey implements OrderedMapIterator<K, V> {
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
    private final class MapIteratorValueByKey extends IteratorByKey implements OrderedMapIterator<V, K> {
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
    private final class EntryIteratorStandardByKey extends IteratorByKey implements OrderedIterator<Entry<K, V>>, ResettableIterator<Entry<K, V>> {
        @Override
        public Entry<K, V> next() {
            return navigateNext().copyEntryUnmodifiable();
        }

        @Override
        public Entry<K, V> previous() {
            return navigatePrevious().copyEntryUnmodifiable();
        }
    }

    /**
     * A node used to store the data.
     */
    private static final class Node<K extends Comparable<K>, V> implements Entry<K, V>, KeyValue<K, V> {

        // TODO make finals (replacing)

        private final K key;
        private V value;
        private Node<K, V> keyLeftNode;
        private Node<K, V> keyRightNode;
        private Node<K, V> keyParentNode;
        private int hashCodeValue;
        private boolean calculatedHashCode;
        private boolean colorFlag;

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
            colorFlag = true;
            calculatedHashCode = false;
        }

        private void swapColors(final Node<K, V> node) {
            final boolean tmp = node.colorFlag;
            node.colorFlag = colorFlag;
            colorFlag = tmp;
        }

        private boolean isBlack() {
            return colorFlag;
        }

        private boolean isRed() {
            return !colorFlag;
        }

        private void setBlack() {
            colorFlag = true;
        }

        private void setRed() {
            colorFlag = false;
        }

        private void copyColor(final Node<K, V> node) {
            colorFlag = node.colorFlag;
        }

        private Entry<K, V> copyEntryUnmodifiable() {
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

    private class TreeSubMap extends AbstractIterableMapAlternate<K, V> implements IterableSortedMap<K, V> {
        private static final long serialVersionUID = 7793720431038658603L;
        private final SortedMapRange<K> keyRange;

        TreeSubMap(final SortedMapRange<K> keyRange) {
            this.keyRange = keyRange;
        }

        private void verifyRange(final K key) {
            if (!keyRange.inRange(key)) {
                throw new IllegalArgumentException("key");
            }
        }

        @Override
        public boolean containsKey(final Object keyObject) {
            final K key = checkKey(keyObject);
            if (keyRange.inRange(key)) {
                return lookupKey(key) != null;
            } else {
                return false;
            }
        }

        @Override
        protected Iterator<Entry<K, V>> entryIterator() {
            return null; // TODO
        }

        @Override
        public OrderedMapIterator<K, V> mapIterator() {
            // TODO
            return new MapIteratorKeyByKey();
        }

        @Override
        protected MapSpliterator<K, V> mapSpliterator() {
            return new KeyRangeMapSpliterator(keyRange);
        }

        @Override
        public V getOrDefault(final Object keyObject, final V defaultValue) {
            final K key = checkKey(keyObject);
            if (keyRange.inRange(key)) {
                final Node<K, V> node = lookupKey(key);
                if (node != null) {
                    return node.value;
                }
            }
            return defaultValue;
        }

        @Override
        protected boolean containsEntry(final Object keyObject, final Object valueObject) {
            final K key = checkKey(keyObject);
            final V value = checkValue(valueObject);
            if (keyRange.inRange(key)) {
                final Node<K, V> node = lookupKey(key);
                if (node != null) {
                    return Objects.equals(node.value, value);
                }
            }
            return false;
        }

        @Override
        public boolean containsValue(final Object valueObject) {
            final V value = checkValue(valueObject);
            final Node<K, V> node = lookupValue(value);
            if (node != null) {
                return keyRange.inRange(node.key);
            } else {
                return false;
            }
        }

        @Override
        public K getKeyOrDefault(final Object valueObject, final K defaultKey) {
            final V value = checkValue(valueObject);
            final Node<K, V> node = lookupValue(value);
            if (node != null && keyRange.inRange(node.key)) {
                return node.key;
            }
            return defaultKey;
        }

        @Override
        public K removeValue(final Object valueObject) {
            final V value = checkValue(valueObject);
            final Node<K, V> node = lookupValue(value);
            if (node != null && keyRange.inRange(node.key)) {
                doRedBlackDelete(node);
                return node.key;
            }
            return null;
        }

        @Override
        protected boolean removeValueAsBoolean(final Object valueObject) {
            final V value = checkValue(valueObject);
            final Node<K, V> node = lookupValue(value);
            if (node != null && keyRange.inRange(node.key)) {
                doRedBlackDelete(node);
                return true;
            }
            return false;
        }

        @Override
        public V remove(final Object keyObject) {
            final K key = checkKey(keyObject);
            if (keyRange.inRange(key)) {
                return BinaryTreeMap.this.remove(key);
            }
            return null;
        }

        @Override
        public boolean remove(final Object keyObject, final Object value) {
            final K key = checkKey(keyObject);
            if (keyRange.inRange(key)) {
                return BinaryTreeMap.this.remove(key,  value);
            }
            return false;
        }

        @Override
        protected boolean removeAsBoolean(final Object keyObject) {
            final K key = checkKey(keyObject);
            if (keyRange.inRange(key)) {
                return BinaryTreeMap.this.removeAsBoolean(keyObject);
            }
            return false;
        }

        @Override
        protected V doPut(final K key, final V value, final boolean addIfAbsent, final boolean updateIfPresent) {
            verifyRange(key);
            return BinaryTreeMap.this.doPut(key, value, addIfAbsent, updateIfPresent);
        }

        @Override
        protected V doPut(final K key, final Function<? super K, ? extends V> absentFunc, final BiFunction<? super K, ? super V, ? extends V> presentFunc, final boolean saveNulls) {
            verifyRange(key);
            return BinaryTreeMap.this.doPut(key, absentFunc, presentFunc, saveNulls);
        }

        @Override
        public void putAll(final Map<? extends K, ? extends V> m) {
            for (final K key : m.keySet()) {
                verifyRange(key);
            }
            BinaryTreeMap.this.putAll(m);
        }

        private Entry<K, V> nextEntry(final K key) {
            return lookupKeyHigher(key, false);
        }

        private Entry<K, V> previousEntry(final K key) {
            return lookupKeyLower(key, false);
        }

        @Override
        public K firstKey() {
            return getKeyNullSafe(firstEntryInRange(keyRange));
        }

        @Override
        public K lastKey() {
            return getKeyNullSafe(lastEntryInRange(keyRange));
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
        public IterableSortedMap<K, V> subMap(final K fromKey, final K toKey) {
            return new TreeSubMap(keyRange.subRange(fromKey, toKey));
        }

        @Override
        public IterableSortedMap<K, V> headMap(final K toKey) {
            return new TreeSubMap(keyRange.head(toKey));
        }

        @Override
        public IterableSortedMap<K, V> tailMap(final K fromKey) {
            return new TreeSubMap(keyRange.tail(fromKey));
        }
    }

    private final class KeyMapSpliterator extends AbstractTreeSpliterator<K, V, Node<K, V>> {
        private KeyMapSpliterator() {
        }

        private KeyMapSpliterator(final SplitState state,
                                  final Node<K, V> currentNode, final Node<K, V> lastNode, final long estimatedSize) {
            super(state, currentNode, lastNode, estimatedSize);
        }

        @Override
        protected AbstractTreeSpliterator<K, V, Node<K, V>> makeSplit(final SplitState state,
                                                                      final Node<K, V> currentNode, final Node<K, V> lastNode, final long estimatedSize) {
            return new KeyMapSpliterator(state, currentNode, lastNode, estimatedSize);
        }

        @Override
        protected int modCount() {
            return modifications;
        }

        @Override
        protected Node<K, V> rootNode() {
            return rootNodeKey;
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
            return nextSmallerKey(node);
        }

        @Override
        protected Node<K, V> nextGreater(final Node<K, V> node) {
            return nextGreaterKey(node);
        }

        @Override
        protected Node<K, V> subTreeLowest(final Node<K, V> node) {
            return leastNodeKey(node);
        }

        @Override
        protected Node<K, V> subTreeGreatest(final Node<K, V> node) {
            return greatestNodeKey(node);
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

    private final class KeyRangeMapSpliterator extends AbstractTreeRangeSpliterator<K, V, Node<K, V>> {
        private KeyRangeMapSpliterator(final SortedMapRange<K> range) {
            super(range);
        }

        private KeyRangeMapSpliterator(final SortedMapRange<K> range, final SplitState state,
                                       final Node<K, V> currentNode, final Node<K, V> lastNode, final long estimatedSize) {
            super(range, state, currentNode, lastNode, estimatedSize);
        }

        @Override
        protected AbstractTreeSpliterator<K, V, Node<K, V>> makeSplit(final SplitState state,
                                                                      final Node<K, V> currentNode, final Node<K, V> lastNode, final long estimatedSize) {
            return new KeyRangeMapSpliterator(keyRange, state, currentNode, lastNode, estimatedSize);
        }

        @Override
        protected int modCount() {
            return modifications;
        }

        @Override
        protected Node<K, V> rootNode() {
            return rootNodeKey;
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
            return nextSmallerKey(node);
        }

        @Override
        protected Node<K, V> nextGreater(final Node<K, V> node) {
            return nextGreaterKey(node);
        }

        @Override
        protected Node<K, V> subTreeLowest(final Node<K, V> node) {
            return leastNodeKey(node);
        }

        @Override
        protected Node<K, V> subTreeGreatest(final Node<K, V> node) {
            return greatestNodeKey(node);
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
            return firstEntryInRange(keyRange);
        }

        @Override
        protected Node<K, V> findLastNode() {
            return lastEntryInRange(keyRange);
        }
    }
}
