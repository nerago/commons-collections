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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.collections4.KeyValue;
import org.apache.commons.collections4.OrderedIterator;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.ResettableIterator;
import org.apache.commons.collections4.SortedMapRange;
import org.apache.commons.collections4.iterators.EmptyIterator;
import org.apache.commons.collections4.iterators.EmptyOrderedMapIterator;
import org.apache.commons.collections4.spliterators.AbstractTreeRangeSpliterator;
import org.apache.commons.collections4.spliterators.AbstractTreeSpliterator;
import org.apache.commons.collections4.spliterators.EmptyMapSpliterator;
import org.apache.commons.collections4.spliterators.MapSpliterator;



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
@SuppressWarnings({"OverlyComplexMethod", "OverlyLongMethod", "OverlyComplexClass"})
public final class BinaryTreeMap<K extends Comparable<K>, V>
        extends AbstractIterableSortedMap<K, V>
        implements Externalizable, Cloneable {

    private static final long serialVersionUID = 5398917388048351226L;
    
    private transient Node<K, V> rootNode;
    private transient int nodeCount;
    private transient int modifications;

    /**
     * Constructs a new empty TreeBidiMap.
     */
    @SuppressWarnings("WeakerAccess")
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
    public boolean containsMapping(final Object key, final Object value) {
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
        checkKey(key);

        Node<K, V> node = rootNode;
        if (node == null) {
            if (addIfAbsent) {
                addAsRoot(key, value);
            }
            return null;
        }

        while (true) {
            final int cmp = key.compareTo(node.getKey());
            if (cmp == 0) {
                final V oldValue = node.value;
                if (oldValue == null) {
                    if (addIfAbsent) {
                        updateValue(node, value);
                    }
                } else if (!Objects.equals(oldValue, value)) {
                    if (updateIfPresent) {
                        updateValue(node, value);
                    }
                }
                return oldValue;
            } else if (cmp < 0) {
                if (node.leftNode == null) {
                    if (addIfAbsent) {
                        addOnLeft(key, value, node);
                    }
                    return null;
                }
                node = node.leftNode;
            } else { // cmp > 0
                if (node.rightNode == null) {
                    if (addIfAbsent) {
                        addOnRight(key, value, node);
                    }
                    return null;
                }
                node = node.rightNode;
            }
        }
    }

    @Override
    protected V doPut(final K key, final Function<? super K, ? extends V> absentFunc,
                                   final BiFunction<? super K, ? super V, ? extends V> presentFunc,
                                   final boolean saveNulls) {
        checkKey(key);
        final int expectedModifications = modifications;

        Node<K, V> node = rootNode;
        if (node == null) {
            // map is empty
            if (absentFunc != null) {
                final V newValue = absentFunc.apply(key);
                if (expectedModifications != modifications) {
                    throw new ConcurrentModificationException();
                } else if (newValue != null || saveNulls) {
                    addAsRoot(key, newValue);
                    return newValue;
                }
            }
            return null;
        }

        while (true) {
            final int cmp = key.compareTo(node.getKey());
            if (cmp == 0) {
                final V oldValue = node.getValue();
                if (oldValue != null && presentFunc != null) {
                    final V newValue = presentFunc.apply(key, oldValue);
                    if (expectedModifications != modifications) {
                        throw new ConcurrentModificationException();
                    } else if (newValue == null && !saveNulls) {
                        doRedBlackDelete(node);
                        return null;
                    } else if (Objects.equals(oldValue, newValue)) {
                        return oldValue;
                    } else {
                        updateValue(node, newValue);
                        return newValue;
                    }
                } else if (oldValue == null && absentFunc != null) {
                    final V newValue = absentFunc.apply(key);
                    if (expectedModifications != modifications) {
                        throw new ConcurrentModificationException();
                    } else if (newValue != null || saveNulls) {
                        updateValue(node, newValue);
                        return newValue;
                    }
                } else {
                    return oldValue;
                }
            } else if (cmp < 0) {
                if (node.leftNode == null) {
                    if (absentFunc != null) {
                        final V newValue = absentFunc.apply(key);
                        if (expectedModifications != modifications) {
                            throw new ConcurrentModificationException();
                        } else if (newValue != null || saveNulls) {
                            addOnLeft(key, newValue, node);
                            return newValue;
                        }
                    }
                    return null;
                }
                node = node.leftNode;
            } else { // cmp > 0
                if (node.rightNode == null) {
                    if (absentFunc != null) {
                        final V newValue = absentFunc.apply(key);
                        if (expectedModifications != modifications) {
                            throw new ConcurrentModificationException();
                        } else if (newValue != null || saveNulls) {
                            addOnRight(key, newValue, node);
                            return newValue;
                        }
                    }
                    return null;
                }
                node = node.rightNode;
            }
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
    public AbstractIterableSortedMap<K, V> subMap(final SortedMapRange<K> range) {
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
        nodeCount = 0;
        rootNode = null;
        modify();
    }

    /**
     * Gets the first (lowest) key currently in this map.
     *
     * @return the first (lowest) key currently in this sorted map
     * @throws NoSuchElementException if this map is empty
     */
    @Override
    public K firstKey() {
        final Node<K, V> node = rootNode;
        if (node == null) {
            throw new NoSuchElementException("Map is empty");
        }
        return leastNode(node).getKey();
    }

    /**
     * Gets the last (highest) key currently in this map.
     *
     * @return the last (highest) key currently in this sorted map
     * @throws NoSuchElementException if this map is empty
     */
    @Override
    public K lastKey() {
        final Node<K, V> node = rootNode;
        if (node == null) {
            throw new NoSuchElementException("Map is empty");
        }
        return greatestNode(node).getKey();
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
        final Node<K, V> node = lookupHigher(key, false);
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
        final Node<K, V> node = lookupLower(key, false);
        return node == null ? null : node.getKey();
    }

    @Override
    public OrderedMapIterator<K, V> mapIterator() {
        return isEmpty() ? EmptyOrderedMapIterator.emptyOrderedMapIterator()
                         : new TreeMapIterator();
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

    private void addAsRoot(final K key, final V value) {
        rootNode = new Node<>(key, value);
        grow();
    }

    private void addOnLeft(final K key, final V value, final Node<K, V> parent) {
        final Node<K, V> node = new Node<>(key, value);
        parent.leftNode = node;
        node.parentNode = parent;
        doRedBlackInsert(node);
        grow();
    }

    private void addOnRight(final K key, final V value, final Node<K, V> parent) {
        final Node<K, V> node = new Node<>(key, value);
        parent.rightNode = node;
        node.parentNode = parent;
        doRedBlackInsert(node);
        grow();
    }

    private void updateValue(final Node<K, V> node, final V value) {
        // update value
        node.value = value;
        node.calculatedHashCode = false;
        modify();
    }

    private Node<K, V> lookupKey(final K key) {
        Node<K, V> node = rootNode;

        while (node != null) {
            final K result = node.getKey();
            final int cmp = key.compareTo(result);
            if (cmp == 0) {
                return node;
            } else if (cmp < 0) {
                node = node.leftNode;
            } else {
                node = node.rightNode;
            }
        }

        return null;
    }

    private Node<K, V> lookupHigher(final K key, final boolean includeEqual) {
        Node<K, V> node = rootNode, higher = null;

        while (node != null) {
            final int cmp = node.getKey().compareTo(key);
            if (cmp == 0 && includeEqual) {
                return node;
            } else if (cmp > 0) {
                higher = node;
                node = node.leftNode;
            } else {
                node = node.rightNode;
            }
        }

        return higher;
    }

    private Node<K, V> lookupLower(final K key, final boolean includeEqual) {
        Node<K, V> node = rootNode, lower = null;

        while (node != null) {
            final int cmp = node.getKey().compareTo(key);
            if (cmp == 0 && includeEqual) {
                return node;
            } else if (cmp < 0) {
                lower = node;
                node = node.rightNode;
            } else {
                node = node.leftNode;
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
    private Node<K, V> nextSmaller(final Node<K, V> node) {
        if (node == null) {
            return null;
        } else if (node.leftNode != null) {
            return greatestNode(node.leftNode);
        } else {
            Node<K, V> parent = node.parentNode;
            Node<K, V> child = node;

            while (parent != null) {
                if (child != parent.leftNode) {
                    break;
                } else {
                    child = parent;
                    parent = parent.parentNode;
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
    private Node<K, V> nextGreater(final Node<K, V> node) {
        if (node == null) {
            return null;
        } else if (node.rightNode != null) {
            return leastNode(node.rightNode);
        } else {
            Node<K, V> parent = node.parentNode;
            Node<K, V> child = node;

            while (parent != null) {
                if (child != parent.rightNode)
                    break;
                child = parent;
                parent = parent.parentNode;
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
    private Node<K, V> leastNode(final Node<K, V> node) {
        Node<K, V> rval = node;
        if (rval != null) {
            while (rval.leftNode != null) {
                rval = rval.leftNode;
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
    private Node<K, V> greatestNode(final Node<K, V> node) {
        Node<K, V> rval = node;
        if (rval != null) {
            while (rval.rightNode != null) {
                rval = rval.rightNode;
            }
        }
        return rval;
    }

    /**
     * Does a rotate left. standard fare in the world of balanced trees.
     *
     * @param node the node to be rotated
     */
    private void rotateLeft(final Node<K, V> node) {
        final Node<K, V> rightChild = node.rightNode;
        node.rightNode = rightChild.leftNode;

        if (rightChild.leftNode != null) {
            rightChild.leftNode.parentNode = node;
        }
        rightChild.parentNode = node.parentNode;

        final Node<K, V> parent = node.parentNode;
        if (parent == null) {
            // node was the root ... now its right child is the root
            rootNode = rightChild;
        } else {
            if (parent.leftNode == node) {
                parent.leftNode = rightChild;
            } else {
                parent.rightNode = rightChild;
            }
        }

        rightChild.leftNode = node;
        node.parentNode = rightChild;
    }

    /**
     * Does a rotate right. standard fare in the world of balanced trees.
     *
     * @param node the node to be rotated
     */
    private void rotateRight(final Node<K, V> node) {
        final Node<K, V> leftChild = node.leftNode;
        node.leftNode = leftChild.rightNode;

        if (leftChild.rightNode != null) {
            final Node<K, V> kvNode = leftChild.rightNode;
            kvNode.parentNode = node;
        }
        leftChild.parentNode = node.parentNode;

        final Node<K, V> parent = node.parentNode;
        if (parent == null) {
            // node was the root ... now its left child is the root
            rootNode = leftChild;
        } else {
            if (parent.rightNode == node) {
                parent.rightNode = leftChild;
            } else {
                parent.leftNode = leftChild;
            }
        }

        leftChild.rightNode = node;
        node.parentNode = leftChild;
    }

    /**
     * Complicated red-black insert stuff. Based on Sun's TreeMap
     * implementation, though it's barely recognizable anymore.
     *
     * @param insertedNode the node to be inserted
     */
    private void doRedBlackInsert(final Node<K, V> insertedNode) {
        Node<K, V> currentNode = insertedNode;
        if (currentNode != null) {
            currentNode.setRed();
        }

        while (currentNode != null && currentNode != rootNode && currentNode.parentNode != null && currentNode.parentNode.isRed()) {
            final Node<K, V> parent = currentNode.parentNode;
            final Node<K, V> grandParent = parent.parentNode;
            if (parent.leftNode == currentNode) {
                final Node<K, V> grandParentRight = grandParent != null ? grandParent.rightNode : null;

                if (grandParentRight != null && grandParentRight.isRed()) {
                    parent.setBlack();
                    grandParentRight.setBlack();
                    grandParent.setRed();
                    currentNode = grandParent;
                } else {
                    parent.setBlack();
                    if (grandParent != null) {
                        grandParent.setRed();
                        rotateRight(grandParent);
                    }
                }
            } else {
                // just like clause above, except swap left for right
                final Node<K, V> grandParentLeft = grandParent != null ? grandParent.leftNode : null;

                if (grandParentLeft != null && grandParentLeft.isRed()) {
                    parent.setBlack();
                    grandParentLeft.setBlack();
                    grandParent.setRed();
                    currentNode = grandParent;
                } else {
                    parent.setBlack();
                    if (grandParent != null) {
                        grandParent.setRed();
                        rotateLeft(grandParent);
                    }
                }
            }
        }

        if (rootNode != null) {
            rootNode.setBlack();
        }
    }

    /**
     * Complicated red-black delete stuff. Based on Sun's TreeMap
     * implementation, though it's barely recognizable anymore.
     *
     * @param deletedNode the node to be deleted
     */
    private void doRedBlackDelete(final Node<K, V> deletedNode) {
        // if deleted node has both left and children, swap with
        // the next greater node
        if (deletedNode.leftNode != null && deletedNode.rightNode != null) {
            swapPosition(nextGreater(deletedNode), deletedNode);
        }

        final Node<K, V> replacement;
        if (deletedNode.leftNode != null) {
            replacement = deletedNode.leftNode;
        } else {
            replacement = deletedNode.rightNode;
        }

        if (replacement != null) {
            replaceNode(deletedNode, replacement);
        } else {
            // replacement is null
            if (deletedNode.parentNode == null) {
                // empty tree
                rootNode = null;
            } else {
                // deleted node had no children
                if (deletedNode.isBlack()) {
                    doRedBlackDeleteFixup(deletedNode);
                }

                if (deletedNode.parentNode != null) {
                    final Node<K, V> parentNode = deletedNode.parentNode;
                    if (deletedNode == parentNode.leftNode) {
                        parentNode.leftNode = null;
                    } else {
                        parentNode.rightNode = null;
                    }

                    deletedNode.parentNode = null;
                }
            }
        }

        shrink();
    }

    /**
     * Complicated red-black delete stuff. Based on Sun's TreeMap
     * implementation, though it's barely recognizable anymore. This
     * rebalances the tree (somewhat, as red-black trees are not
     * perfectly balanced -- perfect balancing takes longer)
     *
     * @param replacementNode the node being replaced
     */
    private void doRedBlackDeleteFixup(final Node<K, V> replacementNode) {
        Node<K, V> currentNode = replacementNode;

        while (currentNode != rootNode && currentNode != null && currentNode.parentNode != null) {
            if (!currentNode.isBlack()) break;
            Node<K, V> parent = currentNode.parentNode;
            final boolean isLeftChild = parent.leftNode == currentNode;
            if (isLeftChild) {
                Node<K, V> siblingNode = parent.rightNode;

                if (siblingNode == null) {
                    currentNode = parent;
                    continue;
                }

                if (siblingNode.isRed()) {
                    siblingNode.setBlack();
                    parent.setRed();
                    rotateLeft(parent);
                    parent = currentNode.parentNode;
                    siblingNode = parent.rightNode;
                }

                if (siblingNode == null) {
                    currentNode = parent;
                    continue;
                }

                final Node<K, V> siblingLeft = siblingNode.leftNode;
                final Node<K, V> siblingRight = siblingNode.rightNode;
                if ((siblingLeft == null || siblingLeft.isBlack()) && (siblingRight == null || siblingRight.isBlack())) {
                    siblingNode.setRed();
                    currentNode = parent;
                } else {
                    if (siblingRight == null || siblingRight.isBlack()) {
                        if (siblingLeft != null) {
                            siblingLeft.setBlack();
                        }
                        siblingNode.setRed();
                        rotateRight(siblingNode);
                        parent = currentNode.parentNode;
                        siblingNode = parent.rightNode;
                    }

                    if (siblingNode != null) {
                        siblingNode.copyColor(parent);
                    }
                    parent.setBlack();
                    if (siblingRight != null) {
                        siblingRight.setBlack();
                    }
                    rotateLeft(parent);
                    break;
                }
            } else {
                Node<K, V> siblingNode = parent.leftNode;

                if (siblingNode == null) {
                    currentNode = parent;
                    continue;
                }

                if (siblingNode.isRed()) {
                    siblingNode.setBlack();
                    parent.setRed();
                    rotateRight(parent);

                    final Node<K, V> result;
                    result = parent.leftNode;
                    siblingNode = result;
                }

                if (siblingNode == null) {
                    currentNode = parent;
                    continue;
                }

                final Node<K, V> siblingLeft = siblingNode.leftNode;
                final Node<K, V> siblingRight = siblingNode.rightNode;
                if ((siblingLeft == null || siblingLeft.isBlack()) && (siblingRight == null || siblingRight.isBlack())) {
                    siblingNode.setRed();
                    currentNode = parent;
                } else {
                    if (siblingLeft == null || siblingLeft.isBlack()) {
                        if (siblingRight != null) {
                            siblingRight.setBlack();
                        }
                        siblingNode.setRed();
                        rotateLeft(siblingNode);
                        parent = currentNode.parentNode;
                        siblingNode = parent.leftNode;
                    }

                    if (siblingNode != null) {
                        siblingNode.copyColor(parent);
                    }
                    parent.setBlack();
                    if (siblingLeft != null) {
                        siblingLeft.setBlack();
                    }
                    rotateRight(parent);
                    break;
                }
            }
        }

        if (currentNode != null) {
            currentNode.setBlack();
        }
    }

    private void replaceNode(final Node<K, V> previous, final Node<K, V> replacement) {
        final Node<K, V> parentNode = previous.parentNode;
        replacement.parentNode = parentNode;

        if (parentNode == null) {
            rootNode = replacement;
        } else {
            if (previous == parentNode.leftNode) {
                parentNode.leftNode = replacement;
            } else {
                parentNode.rightNode = replacement;
            }
        }

        previous.leftNode = null;
        previous.rightNode = null;
        previous.parentNode = null;

        if (previous.isBlack()) {
            doRedBlackDeleteFixup(replacement);
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
    private void swapPosition(final Node<K, V> a, final Node<K, V> b) {
        // Save initial values.
        final Node<K, V> aParent = a.parentNode;
        final Node<K, V> aLeftChild = a.leftNode;
        final Node<K, V> aRightChild = a.rightNode;
        final Node<K, V> bParent = b.parentNode;
        final Node<K, V> bLeftChild = b.leftNode;
        final Node<K, V> bRightChild = b.rightNode;

        if (a == bParent) {
            a.parentNode = b;
            b.parentNode = aParent;
            if (aParent != null) {
                if (a == aParent.leftNode)
                    aParent.leftNode = b;
                else
                    aParent.rightNode = b;
            }
            a.leftNode = bLeftChild;
            a.rightNode = bRightChild;
            if (b == aLeftChild) {
                b.leftNode = a;
                b.rightNode = aRightChild;
            } else {
                b.leftNode = aLeftChild;
                b.rightNode = a;
            }
        } else if (b == aParent) {
            a.parentNode = bParent;
            b.parentNode = a;
            if (bParent != null) {
                if (b == bParent.leftNode)
                    bParent.leftNode = a;
                else
                    bParent.rightNode = a;
            }
            if (a == bLeftChild) {
                a.leftNode = b;
                a.rightNode = bRightChild;
            } else {
                a.rightNode = b;
                a.leftNode = bLeftChild;
            }
            b.leftNode = aLeftChild;
            b.rightNode = aRightChild;
        } else if (aParent != null && bParent != null) {
            a.parentNode = bParent;
            b.parentNode = aParent;
            if (a == aParent.leftNode)
                aParent.leftNode = b;
            else
                aParent.rightNode = b;
            if (b == bParent.leftNode)
                bParent.leftNode = a;
            else
                bParent.rightNode = a;
            a.leftNode = bLeftChild;
            a.rightNode = bRightChild;
            b.leftNode = aLeftChild;
            b.rightNode = aRightChild;
        } else if (aParent != null) {
            a.parentNode = null;
            b.parentNode = aParent;
            if (a == aParent.leftNode)
                aParent.leftNode = b;
            else
                aParent.rightNode = b;
            a.leftNode = bLeftChild;
            a.rightNode = bRightChild;
            b.leftNode = aLeftChild;
            b.rightNode = aRightChild;
        } else if (bParent != null) {
            a.parentNode = bParent;
            b.parentNode = null;
            if (b == bParent.leftNode) {
                bParent.leftNode = a;
            } else {
                bParent.rightNode = a;
            }
            a.leftNode = bLeftChild;
            a.rightNode = bRightChild;
            b.leftNode = aLeftChild;
            b.rightNode = aRightChild;
        } else {
            a.leftNode = bLeftChild;
            a.rightNode = bRightChild;
            b.leftNode = aLeftChild;
            b.rightNode = aRightChild;
        }

        // Fix children's parent pointers
        if (a.leftNode != null)
            a.leftNode.parentNode = a;
        if (a.rightNode != null)
            a.rightNode.parentNode = a;
        if (b.leftNode != null)
            b.leftNode.parentNode = b;
        if (b.rightNode != null)
            b.rightNode.parentNode = b;

        a.swapColors(b);

        // Check if root changed
        if (rootNode == a)
            rootNode = b;
        else if (rootNode == b)
            rootNode = a;
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
     * @param in the input data
     * @throws IOException            if an error occurs while reading from the stream
     * @throws ClassNotFoundException if an object read from the stream can not be loaded
     */
    @SuppressWarnings("unchecked")  // This will fail at runtime if the stream is incorrect
    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        final int size = in.readInt();
        for (int i = 0; i < size; i++) {
            final K k = (K) in.readObject();
            final V v = (V) in.readObject();
            put(k, v);
        }
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeInt(this.size());

        final OrderedMapIterator<K, V> it = mapIterator();
        while (it.hasNext()) {
            out.writeObject(it.next());
            out.writeObject(it.getValue());
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
        final Node<K, V> candidate = keyRange.hasFrom() ? lookupHigher(keyRange.getFromKey(), keyRange.isFromInclusive())
                : leastNode(rootNode);
        if (keyRange.contains(candidate.getKey())) {
            return candidate;
        } else {
            return null;
        }
    }

    private Node<K, V> lastEntryInRange(final SortedMapRange<K> keyRange) {
        final Node<K, V> candidate = keyRange.hasTo() ? lookupLower(keyRange.getToKey(), keyRange.isToInclusive())
                : greatestNode(rootNode);
        if (keyRange.contains(candidate.getKey())) {
            return candidate;
        } else {
            return null;
        }
    }

    /**
     * Base class for all iterators.
     */
    abstract class IteratorBase {
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
        IteratorBase() {
            expectedModifications = modifications;
            reset();
        }

        public final void reset() {
            nextNode = leastNode(rootNode);
            lastReturnedNode = null;
            previousNode = null;
        }

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

        final Node<K, V> navigateNext() {
            if (nextNode == null) {
                throw new NoSuchElementException();
            }
            if (modifications != expectedModifications) {
                throw new ConcurrentModificationException();
            }
            lastReturnedNode = nextNode;
            previousNode = nextNode;
            nextNode = nextGreater(nextNode);
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
            previousNode = nextSmaller(lastReturnedNode);
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
                    previousNode = greatestNode(rootNode);
                } else {
                    previousNode = nextSmaller(nextNode);
                }
            } else {
                // most recent was navigatePrevious
                if (previousNode == null) {
                    nextNode = leastNode(rootNode);
                } else {
                    nextNode = nextGreater(previousNode);
                }
            }
            lastReturnedNode = null;
        }
    }

    /**
     * An iterator over the map.
     */
    private final class TreeMapIterator extends IteratorBase implements OrderedMapIterator<K, V>, ResettableIterator<K> {
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
        public V setValue(final V value) {
            if (lastReturnedNode == null) {
                throw new IllegalStateException();
            }
            return lastReturnedNode.setValue(value);
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
     * An iterator over the map entries.
     */
    private final class EntryIteratorStandardByKey extends IteratorBase implements OrderedIterator<Entry<K, V>>, ResettableIterator<Entry<K, V>> {
        @Override
        public Entry<K, V> next() {
            return navigateNext();
        }

        @Override
        public Entry<K, V> previous() {
            return navigatePrevious();
        }
    }

    /**
     * A node used to store the data.
     */
    private static final class Node<K extends Comparable<K>, V> implements Entry<K, V>, KeyValue<K, V> {
        private final K key;
        private V value;
        private Node<K, V> leftNode;
        private Node<K, V> rightNode;
        private Node<K, V> parentNode;
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
         * Sets the value.
         *
         * @param newValue new value replacing contents
         * @return old value
         */
        @Override
        public V setValue(final V newValue) throws UnsupportedOperationException {
            final V oldValue = value;
            value = newValue;
            return oldValue;
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
            return Objects.equals(key, e.getKey()) && Objects.equals(value, e.getValue());
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

    private class TreeSubMap extends AbstractIterableSortedMap<K, V> {
        private static final long serialVersionUID = 7793720431038658603L;
        private SortedMapRange<K> keyRange;

        TreeSubMap(final SortedMapRange<K> keyRange) {
            this.keyRange = keyRange;
        }

        private void verifyRange(final K key) {
            if (!keyRange.contains(key)) {
                throw new IllegalArgumentException("key");
            }
        }

        @Override
        public boolean containsKey(final Object keyObject) {
            final K key = checkKey(keyObject);
            if (keyRange.contains(key)) {
                return lookupKey(key) != null;
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
            return new TreeMapIterator();
        }

        @Override
        public AbstractIterableSortedMap<K, V> subMap(final SortedMapRange<K> range) {
            return new TreeSubMap(range);
        }

        @Override
        protected AbstractIterableSortedMap<K, V> createReversed() {
            return new TreeSubMapReverse(keyRange.reversed());
        }

        @Override
        public MapSpliterator<K, V> mapSpliterator() {
            return new KeyRangeMapSpliterator(keyRange);
        }

        @Override
        public V getOrDefault(final Object keyObject, final V defaultValue) {
            final K key = checkKey(keyObject);
            if (keyRange.contains(key)) {
                final Node<K, V> node = lookupKey(key);
                if (node != null) {
                    return node.value;
                }
            }
            return defaultValue;
        }

        @Override
        public boolean containsMapping(final Object keyObject, final Object value) {
            final K key = checkKey(keyObject);
            if (keyRange.contains(key)) {
                final Node<K, V> node = lookupKey(key);
                if (node != null) {
                    return Objects.equals(node.value, value);
                }
            }
            return false;
        }

        @Override
        public V remove(final Object keyObject) {
            final K key = checkKey(keyObject);
            if (keyRange.contains(key)) {
                return BinaryTreeMap.this.remove(key);
            }
            return null;
        }

        @Override
        public boolean remove(final Object keyObject, final Object value) {
            final K key = checkKey(keyObject);
            if (keyRange.contains(key)) {
                return BinaryTreeMap.this.remove(key,  value);
            }
            return false;
        }

        @Override
        public boolean removeAsBoolean(final Object keyObject) {
            final K key = checkKey(keyObject);
            if (keyRange.contains(key)) {
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
            return getKeyNullSafe(lookupHigher(key, false));
        }

        @Override
        public K previousKey(final K key) {
            return getKeyNullSafe(lookupLower(key, false));
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
        public void writeExternal(final ObjectOutput out) throws IOException {
            out.writeObject(keyRange);
            BinaryTreeMap.this.writeExternal(out);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
            keyRange = (SortedMapRange<K>) in.readObject();
            BinaryTreeMap.this.readExternal(in);
        }
    }

    private class TreeSubMapReverse extends TreeSubMap {
        TreeSubMapReverse(final SortedMapRange<K> keyRange) {
            super(keyRange);
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
            return rootNode;
        }

        @Override
        protected Node<K, V> getLeft(final Node<K, V> node) {
            return node.leftNode;
        }

        @Override
        protected Node<K, V> getRight(final Node<K, V> node) {
            return node.rightNode;
        }

        @Override
        protected Node<K, V> nextLower(final Node<K, V> node) {
            return nextSmaller(node);
        }

        @Override
        protected Node<K, V> nextGreater(final Node<K, V> node) {
            return BinaryTreeMap.this.nextGreater(node);
        }

        @Override
        protected Node<K, V> subTreeLowest(final Node<K, V> node) {
            return leastNode(node);
        }

        @Override
        protected Node<K, V> subTreeGreatest(final Node<K, V> node) {
            return greatestNode(node);
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
            return rootNode;
        }

        @Override
        protected Node<K, V> getLeft(final Node<K, V> node) {
            return node.leftNode;
        }

        @Override
        protected Node<K, V> getRight(final Node<K, V> node) {
            return node.rightNode;
        }

        @Override
        protected Node<K, V> nextLower(final Node<K, V> node) {
            return nextSmaller(node);
        }

        @Override
        protected Node<K, V> nextGreater(final Node<K, V> node) {
            return BinaryTreeMap.this.nextGreater(node);
        }

        @Override
        protected Node<K, V> subTreeLowest(final Node<K, V> node) {
            return leastNode(node);
        }

        @Override
        protected Node<K, V> subTreeGreatest(final Node<K, V> node) {
            return greatestNode(node);
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
