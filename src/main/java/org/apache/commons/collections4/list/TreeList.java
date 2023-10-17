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
package org.apache.commons.collections4.list;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MutableBoolean;
import org.apache.commons.collections4.MutableInteger;
import org.apache.commons.collections4.OrderedIterator;

/**
 * A {@code List} implementation that is optimised for fast insertions and
 * removals at any index in the list.
 * <p>
 * This list implementation utilises a tree structure internally to ensure that
 * all insertions and removals are O(log n). This provides much faster performance
 * than both an {@code ArrayList} and a {@code LinkedList} where elements
 * are inserted and removed repeatedly from anywhere in the list.
 * </p>
 * <p>
 * The following relative performance statistics are indicative of this class:
 * </p>
 * <pre>
 *              get  add  insert  iterate  remove
 * TreeList       3    5       1       2       1
 * ArrayList      1    1      40       1      40
 * LinkedList  5800    1     350       2     325
 * </pre>
 * <p>
 * {@code ArrayList} is a good general purpose list implementation.
 * It is faster than {@code TreeList} for most operations except inserting
 * and removing in the middle of the list. {@code ArrayList} also uses less
 * memory as {@code TreeList} uses one object per entry.
 * </p>
 * <p>
 * {@code LinkedList} is rarely a good choice of implementation.
 * {@code TreeList} is almost always a good replacement for it, although it
 * does use slightly more memory.
 * </p>
 *
 * @since 3.1
 */
public class TreeList<E> extends AbstractList<E> implements Externalizable {
//    add; toArray; iterator; insert; get; indexOf; remove
//    TreeList = 1260;7360;3080;  160;   170;3400;  170;
//   ArrayList =  220;1480;1760; 6870;    50;1540; 7200;
//  LinkedList =  270;7360;3350;55860;290720;2910;55200;

    /** Serialization version */
    private static final long serialVersionUID = 3228691446211358574L;

    /** The root node in the AVL tree */
    private transient AVLNode<E> root;

    /** The current size of the list */
    private transient int size;

    /**
     * Constructs a new empty list.
     */
    public TreeList() {
    }

    /**
     * Constructs a new empty list that copies the specified collection.
     *
     * @param coll  the collection to copy
     * @throws NullPointerException if the collection is null
     */
    public TreeList(final Collection<? extends E> coll) {
        if (!coll.isEmpty()) {
            root = new AVLNode<>(coll);
            size = coll.size();
        }
    }

    /**
     * Gets the element at the specified index.
     *
     * @param index  the index to retrieve
     * @return the element at the specified index
     */
    @Override
    public E get(final int index) {
        checkInterval(index, 0, size() - 1);
        return root.get(index).getValue();
    }

    /**
     * Gets the current size of the list.
     *
     * @return the current size
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * Returns true if this collection contains no elements.
     */
    @Override
    public boolean isEmpty() {
        return root == null;
    }

    /**
     * Gets an iterator over the list.
     *
     * @return an iterator over the list
     */
    @Override
    public Iterator<E> iterator() {
        // override to go 75% faster
        return listIterator(0);
    }

    /**
     * Gets a ListIterator over the list.
     *
     * @return the new iterator
     */
    @Override
    public ListIterator<E> listIterator() {
        // override to go 75% faster
        return listIterator(0);
    }

    /**
     * Gets a ListIterator over the list.
     *
     * @param fromIndex  the index to start from
     * @return the new iterator
     */
    @Override
    public ListIterator<E> listIterator(final int fromIndex) {
        // override to go 75% faster
        // cannot use EmptyIterator as iterator.add() must work
        checkInterval(fromIndex, 0, size());
        return new TreeListIterator<>(this, fromIndex);
    }

    @Override
    public Spliterator<E> spliterator() {
        if (root != null)
            return new TreeListSpliterator<>(this);
        else
            return Spliterators.emptySpliterator();
    }

    /**
     * Searches for the index of an object in the list.
     *
     * @param object  the object to search
     * @return the index of the object, -1 if not found
     */
    @Override
    public int indexOf(final Object object) {
        // override to go 75% faster
        if (root == null) {
            return -1;
        }
        return root.indexOf(object, root.relativePosition);
    }

    @Override
    public int lastIndexOf(final Object object) {
        if (root == null) {
            return -1;
        }
        return root.lastIndexOf(object, root.relativePosition);
    }

    /**
     * Searches for the presence of an object in the list.
     *
     * @param object  the object to check
     * @return true if the object is found
     */
    @Override
    public boolean contains(final Object object) {
        return indexOf(object) >= 0;
    }

    /**
     * Converts the list into an array.
     *
     * @return the list as an array
     */
    @Override
    public Object[] toArray() {
        // override to go 20% faster
        final Object[] array = new Object[size()];
        if (root != null) {
            root.toArray(array, root.relativePosition);
        }
        return array;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] array) {
        if (root != null) {
            if (array.length < size)
                array = (T[]) Array.newInstance(array.getClass().getComponentType(), size);
            root.toArray(array, root.relativePosition);
            if (array.length > size)
                array[size] = null;
        } else if (array.length > 0) {
            array[0] = null;
        }
        return array;
    }

    /**
     * Adds a new element to the list.
     *
     * @param index  the index to add before
     * @param obj  the element to add
     */
    @Override
    public void add(final int index, final E obj) {
        modCount++;
        checkInterval(index, 0, size());
        if (root == null) {
            root = new AVLNode<>(index, obj, null, null);
        } else {
            root = root.insert(index, obj);
        }
        size++;
    }

    /**
     * Appends all the elements in the specified collection to the end of this list,
     * in the order that they are returned by the specified collection's Iterator.
     * <p>
     * This method runs in O(n + log m) time, where m is
     * the size of this list and n is the size of {@code c}.
     *
     * @param c  the collection to be added to this list
     * @return {@code true} if this list changed as a result of the call
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public boolean addAll(final Collection<? extends E> c) {
        if (c.isEmpty()) {
            return false;
        }
        modCount += c.size();
        final AVLNode<E> cTree = new AVLNode<>(c);
        root = root == null ? cTree : root.addAll(cTree, size);
        size += c.size();
        return true;
    }

    /**
     * Sets the element at the specified index.
     *
     * @param index  the index to set
     * @param obj  the object to store at the specified index
     * @return the previous object at that index
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    @Override
    public E set(final int index, final E obj) {
        checkInterval(index, 0, size() - 1);
        final AVLNode<E> node = root.get(index);
        final E result = node.value;
        node.setValue(obj);
        return result;
    }

    /**
     * Removes the element at the specified index.
     *
     * @param index  the index to remove
     * @return the previous object at that index
     */
    @Override
    public E remove(final int index) {
        modCount++;
        checkInterval(index, 0, size() - 1);
        final E result = get(index);
        root = root.remove(index);
        size--;
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(Object object) {
        if (root != null) {
            MutableBoolean isRemoved = new MutableBoolean();
            root = root.remove((E) object, isRemoved);
            if (isRemoved.flag) {
                size--;
                modCount++;
                return true;
            }
        }
        return false;
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        if (root != null) {
            AVLNode<E> node = root.get(0);
            do {
                action.accept(node.value);
                node = node.next();
            } while (node != null);
        }
    }

    /**
     * Clears the list, removing all entries.
     */
    @Override
    public void clear() {
        modCount++;
        root = null;
        size = 0;
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        root = root.removeRange(fromIndex, toIndex, new MutableInteger());
        size -= toIndex - fromIndex + 1;
    }

    /**
     * Returns a view of the portion of this list between the specified
     * {@code fromIndex}, inclusive, and {@code toIndex}, exclusive.  (If
     * {@code fromIndex} and {@code toIndex} are equal, the returned list is
     * empty.)  The returned list is backed by this list, so non-structural
     * changes in the returned list are reflected in this list, and vice-versa.
     * The returned list supports all of the optional list operations supported
     * by this list.<p>
     *
     * The semantics of the list returned by this method become undefined if
     * the backing list (i.e., this list) is <i>structurally modified</i> in
     * any way other than via the returned list.  (Structural modifications are
     * those that change the size of this list, or otherwise perturb it in such
     * a fashion that iterations in progress may yield incorrect results.)
     *
     * @param fromIndex low endpoint (inclusive) of the subList
     * @param toIndex high endpoint (exclusive) of the subList
     * @return a view of the specified range within this list
     * @throws IndexOutOfBoundsException for an illegal endpoint index value
     *         ({@code fromIndex < 0 || toIndex > size ||
     *         fromIndex > toIndex})
     */
    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex > size || fromIndex > toIndex)
            throw new IndexOutOfBoundsException();
        return new TreeSubList<>(this, fromIndex, toIndex - 1);
    }

    /**
     * Checks whether the index is valid.
     *
     * @param index  the index to check
     * @param startIndex  the first allowed index
     * @param endIndex  the last allowed index
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    private void checkInterval(final int index, final int startIndex, final int endIndex) {
        if (index < startIndex || index > endIndex) {
            throw new IndexOutOfBoundsException("Invalid index:" + index + ", size=" + size());
        }
    }

    /**
     * Implements an AVLNode which keeps the offset updated.
     * <p>
     * This node contains the real work.
     * TreeList is just there to implement {@link java.util.List}.
     * The nodes don't know the index of the object they are holding.  They
     * do know however their position relative to their parent node.
     * This allows to calculate the index of a node while traversing the tree.
     * <p>
     * The Faedelung calculation stores a flag for both the left and right child
     * to indicate if they are a child (false) or a link as in linked list (true).
     */
    static class AVLNode<E> {
        /** The left child node or the predecessor if {@link #leftIsPrevious}.*/
        private AVLNode<E> left;
        /** Flag indicating that left reference is not a subtree but the predecessor. */
        private boolean leftIsPrevious;
        /** The right child node or the successor if {@link #rightIsNext}. */
        private AVLNode<E> right;
        /** Flag indicating that right reference is not a subtree but the successor. */
        private boolean rightIsNext;
        /** How many levels of left/right are below this one. */
        private int height;
        /** The relative position, root holds absolute position. */
        private int relativePosition;
        /** The stored element. */
        private E value;

        /**
         * Constructs a new node with a relative position.
         *
         * @param relativePosition  the relative position of the node
         * @param obj  the value for the node
         * @param rightFollower the node with the value following this one
         * @param leftFollower the node with the value leading this one
         */
        private AVLNode(final int relativePosition, final E obj,
                        final AVLNode<E> rightFollower, final AVLNode<E> leftFollower) {
            this.relativePosition = relativePosition;
            value = obj;
            rightIsNext = true;
            leftIsPrevious = true;
            right = rightFollower;
            left = leftFollower;
        }

        /**
         * Constructs a new AVL tree from a collection.
         * <p>
         * The collection must be nonempty.
         *
         * @param coll  a nonempty collection
         */
        private AVLNode(final Collection<? extends E> coll) {
            this(coll.iterator(), 0, coll.size() - 1, 0, null, null);
        }

        /**
         * Constructs a new AVL tree from a collection.
         * <p>
         * This is a recursive helper for {@link #AVLNode(Collection)}. A call
         * to this method will construct the subtree for elements {@code start}
         * through {@code end} of the collection, assuming the iterator
         * {@code e} already points at element {@code start}.
         *
         * @param iterator  an iterator over the collection, which should already point
         *          to the element at index {@code start} within the collection
         * @param start  the index of the first element in the collection that
         *          should be in this subtree
         * @param end  the index of the last element in the collection that
         *          should be in this subtree
         * @param absolutePositionOfParent  absolute position of this node's
         *          parent, or 0 if this node is the root
         * @param prev  the {@code AVLNode} corresponding to element (start - 1)
         *          of the collection, or null if start is 0
         * @param next  the {@code AVLNode} corresponding to element (end + 1)
         *          of the collection, or null if end is the last element of the collection
         */
        private AVLNode(final Iterator<? extends E> iterator, final int start, final int end,
                        final int absolutePositionOfParent, final AVLNode<E> prev, final AVLNode<E> next) {
            final int mid = start + (end - start) / 2;
            if (start < mid) {
                left = new AVLNode<>(iterator, start, mid - 1, mid, prev, this);
            } else {
                leftIsPrevious = true;
                left = prev;
            }
            value = iterator.next();
            relativePosition = mid - absolutePositionOfParent;
            if (mid < end) {
                right = new AVLNode<>(iterator, mid + 1, end, mid, this, next);
            } else {
                rightIsNext = true;
                right = next;
            }
            recalcHeight();
        }

        /**
         * Gets the value.
         *
         * @return the value of this node
         */
        E getValue() {
            return value;
        }

        /**
         * Sets the value.
         *
         * @param obj  the value to store
         */
        void setValue(final E obj) {
            this.value = obj;
        }

        /**
         * Locate the element with the given index relative to the
         * offset of the parent of this node.
         */
        AVLNode<E> get(final int index) {
            final int indexRelativeToMe = index - relativePosition;

            if (indexRelativeToMe == 0) {
                return this;
            }

            final AVLNode<E> nextNode = indexRelativeToMe < 0 ? getLeftSubTree() : getRightSubTree();
            if (nextNode == null) {
                return null;
            }
            return nextNode.get(indexRelativeToMe);
        }

        /**
         * Locate the index that contains the specified object.
         */
        int indexOf(final Object object, final int index) {
            if (getLeftSubTree() != null) {
                final int result = left.indexOf(object, index + left.relativePosition);
                if (result != -1) {
                    return result;
                }
            }
            if (Objects.equals(value, object)) {
                return index;
            }
            if (getRightSubTree() != null) {
                return right.indexOf(object, index + right.relativePosition);
            }
            return -1;
        }

        /**
         * Locate the index that contains the specified object.
         */
        int lastIndexOf(final Object object, final int index) {
            if (getRightSubTree() != null) {
                final int result = right.lastIndexOf(object, index + right.relativePosition);
                if (result != -1) {
                    return result;
                }
            }
            if (Objects.equals(value, object)) {
                return index;
            }
            if (getLeftSubTree() != null) {
                return left.lastIndexOf(object, index + left.relativePosition);
            }
            return -1;
        }

        /**
         * Stores the node and its children into the array specified.
         *
         * @param array the array to be filled
         * @param index the index of this node
         */
        void toArray(final Object[] array, final int index) {
            array[index] = value;
            if (getLeftSubTree() != null) {
                left.toArray(array, index + left.relativePosition);
            }
            if (getRightSubTree() != null) {
                right.toArray(array, index + right.relativePosition);
            }
        }

        /**
         * Gets the next node in the list after this one.
         *
         * @return the next node
         */
        AVLNode<E> next() {
            if (rightIsNext || right == null) {
                return right;
            }
            return right.min();
        }

        /**
         * Gets the node in the list before this one.
         *
         * @return the previous node
         */
        AVLNode<E> previous() {
            if (leftIsPrevious || left == null) {
                return left;
            }
            return left.max();
        }

        /**
         * Inserts a node at the position index.
         *
         * @param index is the index of the position relative to the position of
         * the parent node.
         * @param obj is the object to be stored in the position.
         */
        AVLNode<E> insert(final int index, final E obj) {
            final int indexRelativeToMe = index - relativePosition;

            if (indexRelativeToMe <= 0) {
                return insertOnLeft(indexRelativeToMe, obj);
            }
            return insertOnRight(indexRelativeToMe, obj);
        }

        private AVLNode<E> insertOnLeft(final int indexRelativeToMe, final E obj) {
            if (getLeftSubTree() == null) {
                setLeft(new AVLNode<>(-1, obj, this, left), null);
            } else {
                setLeft(left.insert(indexRelativeToMe, obj), null);
            }

            if (relativePosition >= 0) {
                relativePosition++;
            }
            final AVLNode<E> ret = balance();
            recalcHeight();
            return ret;
        }

        private AVLNode<E> insertOnRight(final int indexRelativeToMe, final E obj) {
            if (getRightSubTree() == null) {
                setRight(new AVLNode<>(+1, obj, right, this), null);
            } else {
                setRight(right.insert(indexRelativeToMe, obj), null);
            }
            if (relativePosition < 0) {
                relativePosition--;
            }
            final AVLNode<E> ret = balance();
            recalcHeight();
            return ret;
        }

        /**
         * Gets the left node, returning null if it's a faedelung.
         */
        private AVLNode<E> getLeftSubTree() {
            return leftIsPrevious ? null : left;
        }

        /**
         * Gets the right node, returning null if it's a faedelung.
         */
        private AVLNode<E> getRightSubTree() {
            return rightIsNext ? null : right;
        }

        /**
         * Gets the rightmost child of this node.
         *
         * @return the rightmost child (greatest index)
         */
        private AVLNode<E> max() {
            return getRightSubTree() == null ? this : right.max();
        }

        /**
         * Gets the leftmost child of this node.
         *
         * @return the leftmost child (smallest index)
         */
        private AVLNode<E> min() {
            return getLeftSubTree() == null ? this : left.min();
        }

        /**
         * Removes the node at a given position.
         *
         * @param index is the index of the element to be removed relative to the position of
         * the parent node of the current node.
         */
        AVLNode<E> remove(final int index) {
            final int indexRelativeToMe = index - relativePosition;

            if (indexRelativeToMe == 0) {
                return removeSelf();
            }
            if (indexRelativeToMe > 0) {
                setRight(right.remove(indexRelativeToMe), right.right);
                if (relativePosition < 0) {
                    relativePosition++;
                }
            } else {
                setLeft(left.remove(indexRelativeToMe), left.left);
                if (relativePosition > 0) {
                    relativePosition--;
                }
            }
            recalcHeight();
            return balance();
        }

        /**
         * Removes the first node with a given value.
         *
         * @param removeValue the value to be found and removed.
         */
        AVLNode<E> remove(final E removeValue, MutableBoolean isRemoved) {
            AVLNode<E> leftNode = getLeftSubTree();
            if (leftNode != null) {
                setLeft(leftNode.remove(removeValue, isRemoved), leftNode.left);
                if (isRemoved.flag) {
                    if (relativePosition > 0)
                        relativePosition--;
                    recalcHeight();
                    return balance();
                }
            }

            if (Objects.equals(value, removeValue)) {
                isRemoved.flag = true;
                return removeSelf();
            }

            AVLNode<E> rightNode = getRightSubTree();
            if (rightNode != null) {
                setRight(rightNode.remove(removeValue, isRemoved), rightNode.right);
                if (isRemoved.flag) {
                    if (relativePosition < 0)
                        relativePosition++;
                    recalcHeight();
                    return balance();
                }
            }

            return this;
        }

        public AVLNode<E> removeRange(final int fromIndex, final  int toIndex, final MutableInteger parentPositionChange) {
            final int fromRelativeToMe = fromIndex - relativePosition;
            final int toRelativeToMe = toIndex - relativePosition;

            if (fromRelativeToMe < 0 && !leftIsPrevious) {
                final MutableInteger positionChange = new MutableInteger();
                setLeft(left.removeRange(fromRelativeToMe, toRelativeToMe, positionChange), left.left);
                if (relativePosition > 0) {
                    relativePosition -= positionChange.value;
                }
                parentPositionChange.value += positionChange.value;
            }

            if (toRelativeToMe > 0 && !rightIsNext) {
                final MutableInteger positionChange = new MutableInteger();
                setRight(right.removeRange(fromRelativeToMe, toRelativeToMe, positionChange), right.right);
                if (relativePosition < 0) {
                    relativePosition += positionChange.value;
                }
                parentPositionChange.value += positionChange.value;
            }

            if (fromRelativeToMe <= 0 && 0 <= toRelativeToMe) {
                final AVLNode<E> selfReplacement = removeSelf();
                parentPositionChange.value++;
                return selfReplacement;
            }

            recalcHeight();
            return superBalance();
        }

        private AVLNode<E> removeMax() {
            if (getRightSubTree() == null) {
                return removeSelf();
            }
            setRight(right.removeMax(), right.right);
            if (relativePosition < 0) {
                relativePosition++;
            }
            recalcHeight();
            return balance();
        }

        private AVLNode<E> removeMin() {
            if (getLeftSubTree() == null) {
                return removeSelf();
            }
            setLeft(left.removeMin(), left.left);
            if (relativePosition > 0) {
                relativePosition--;
            }
            recalcHeight();
            return balance();
        }

        /**
         * Removes this node from the tree.
         *
         * @return the node that replaces this one in the parent
         */
        private AVLNode<E> removeSelf() {
            if (getRightSubTree() == null && getLeftSubTree() == null) {
                return null;
            }
            if (getRightSubTree() == null) {
                if (relativePosition > 0) {
                    left.relativePosition += relativePosition;
                }
                left.max().setRight(null, right);
                return left;
            }
            if (getLeftSubTree() == null) {
                right.relativePosition += relativePosition - (relativePosition < 0 ? 0 : 1);
                right.min().setLeft(null, left);
                return right;
            }

            if (heightRightMinusLeft() > 0) {
                // more on the right, so delete from the right
                final AVLNode<E> rightMin = right.min();
                value = rightMin.value;
                if (leftIsPrevious) {
                    left = rightMin.left;
                }
                right = right.removeMin();
                if (relativePosition < 0) {
                    relativePosition++;
                }
            } else {
                // more on the left or equal, so delete from the left
                final AVLNode<E> leftMax = left.max();
                value = leftMax.value;
                if (rightIsNext) {
                    right = leftMax.right;
                }
                final AVLNode<E> leftPrevious = left.left;
                left = left.removeMax();
                if (left == null) {
                    // special case where left that was deleted was a double link
                    // only occurs when height difference is equal
                    left = leftPrevious;
                    leftIsPrevious = true;
                }
                if (relativePosition > 0) {
                    relativePosition--;
                }
            }
            recalcHeight();
            return this;
        }

        /**
         * Balances according to the AVL algorithm.
         */
        private AVLNode<E> balance() {
            switch (heightRightMinusLeft()) {
            case 1 :
            case 0 :
            case -1 :
                return this;
            case -2 :
                if (left.heightRightMinusLeft() > 0) {
                    setLeft(left.rotateLeft(), null);
                }
                return rotateRight();
            case 2 :
                if (right.heightRightMinusLeft() < 0) {
                    setRight(right.rotateRight(), null);
                }
                return rotateLeft();
            default :
                throw new IllegalStateException("tree inconsistent!");
            }
        }

        /**
         * Recursively balance extension to standard AVL algorithm for bigger discrepancies.
         */
        private AVLNode<E> superBalance() {
            final int diff = heightRightMinusLeft();
            if (diff <= -2) {
                if (left.heightRightMinusLeft() > 0) {
                    setLeft(left.rotateLeft(), null);
                }
                return rotateRight().superBalance();
            } else if (diff >= 2) {
                if (right.heightRightMinusLeft() < 0) {
                    setRight(right.rotateRight(), null);
                }
                return rotateLeft().superBalance();
            } else {
                return this;
            }
        }

        /**
         * Gets the relative position.
         */
        private int getOffset(final AVLNode<E> node) {
            if (node == null) {
                return 0;
            }
            return node.relativePosition;
        }

        /**
         * Sets the relative position.
         */
        private void setOffset(final AVLNode<E> node, final int newOffset) {
            if (node == null) {
                return;
            }
            node.relativePosition = newOffset;
        }

        /**
         * Sets the height by calculation.
         */
        private void recalcHeight() {
            height = Math.max(
                getLeftSubTree() == null ? -1 : getLeftSubTree().height,
                getRightSubTree() == null ? -1 : getRightSubTree().height) + 1;
        }

        /**
         * Returns the height of the node or -1 if the node is null.
         */
        private int getHeight(final AVLNode<E> node) {
            return node == null ? -1 : node.height;
        }

        /**
         * Returns the height difference right - left
         */
        private int heightRightMinusLeft() {
            return getHeight(getRightSubTree()) - getHeight(getLeftSubTree());
        }

        private AVLNode<E> rotateLeft() {
            final AVLNode<E> newTop = right; // can't be faedelung!
            final AVLNode<E> movedNode = getRightSubTree().getLeftSubTree();

            final int newTopPosition = relativePosition + getOffset(newTop);
            final int myNewPosition = -newTop.relativePosition;
            final int movedPosition = getOffset(newTop) + getOffset(movedNode);

            setRight(movedNode, newTop);
            newTop.setLeft(this, null);

            setOffset(newTop, newTopPosition);
            setOffset(this, myNewPosition);
            setOffset(movedNode, movedPosition);
            return newTop;
        }

        private AVLNode<E> rotateRight() {
            final AVLNode<E> newTop = left; // can't be faedelung
            final AVLNode<E> movedNode = getLeftSubTree().getRightSubTree();

            final int newTopPosition = relativePosition + getOffset(newTop);
            final int myNewPosition = -newTop.relativePosition;
            final int movedPosition = getOffset(newTop) + getOffset(movedNode);

            setLeft(movedNode, newTop);
            newTop.setRight(this, null);

            setOffset(newTop, newTopPosition);
            setOffset(this, myNewPosition);
            setOffset(movedNode, movedPosition);
            return newTop;
        }

        /**
         * Sets the left field to the node, or the previous node if that is null
         *
         * @param node  the new left subtree node
         * @param previous  the previous node in the linked list
         */
        private void setLeft(final AVLNode<E> node, final AVLNode<E> previous) {
            leftIsPrevious = node == null;
            left = leftIsPrevious ? previous : node;
            recalcHeight();
        }

        /**
         * Sets the right field to the node, or the next node if that is null
         *
         * @param node  the new left subtree node
         * @param next  the next node in the linked list
         */
        private void setRight(final AVLNode<E> node, final AVLNode<E> next) {
            rightIsNext = node == null;
            right = rightIsNext ? next : node;
            recalcHeight();
        }

        /**
         * Appends the elements of another tree list to this tree list by efficiently
         * merging the two AVL trees. This operation is destructive to both trees and
         * runs in O(log(m + n)) time.
         *
         * @param otherTree
         *            the root of the AVL tree to merge with this one
         * @param currentSize
         *            the number of elements in this AVL tree
         * @return the root of the new, merged AVL tree
         */
        private AVLNode<E> addAll(AVLNode<E> otherTree, final int currentSize) {
            final AVLNode<E> maxNode = max();
            final AVLNode<E> otherTreeMin = otherTree.min();

            // We need to efficiently merge the two AVL trees while keeping them
            // balanced (or nearly balanced). To do this, we take the shorter
            // tree and combine it with a similar-height subtree of the taller
            // tree. There are two symmetric cases:
            //   * this tree is taller, or
            //   * otherTree is taller.
            if (otherTree.height > height) {
                // CASE 1: The other tree is taller than this one. We will thus
                // merge this tree into otherTree.

                // STEP 1: Remove the maximum element from this tree.
                final AVLNode<E> leftSubTree = removeMax();

                // STEP 2: Navigate left from the root of otherTree until we
                // find a subtree, s, that is no taller than me. (While we are
                // navigating left, we store the nodes we encounter in a stack
                // so that we can re-balance them in step 4.)
                final Deque<AVLNode<E>> sAncestors = new ArrayDeque<>();
                AVLNode<E> s = otherTree;
                int sAbsolutePosition = s.relativePosition + currentSize;
                int sParentAbsolutePosition = 0;
                while (s != null && s.height > getHeight(leftSubTree)) {
                    sParentAbsolutePosition = sAbsolutePosition;
                    sAncestors.push(s);
                    s = s.left;
                    if (s != null) {
                        sAbsolutePosition += s.relativePosition;
                    }
                }

                // STEP 3: Replace s with a newly constructed subtree whose root
                // is maxNode, whose left subtree is leftSubTree, and whose right
                // subtree is s.
                maxNode.setLeft(leftSubTree, null);
                maxNode.setRight(s, otherTreeMin);
                if (leftSubTree != null) {
                    leftSubTree.max().setRight(null, maxNode);
                    leftSubTree.relativePosition -= currentSize - 1;
                }
                if (s != null) {
                    s.min().setLeft(null, maxNode);
                    s.relativePosition = sAbsolutePosition - currentSize + 1;
                }
                maxNode.relativePosition = currentSize - 1 - sParentAbsolutePosition;
                otherTree.relativePosition += currentSize;

                // STEP 4: Re-balance the tree and recalculate the heights of s's ancestors.
                s = maxNode;
                while (!sAncestors.isEmpty()) {
                    final AVLNode<E> sAncestor = sAncestors.pop();
                    sAncestor.setLeft(s, null);
                    s = sAncestor.balance();
                }
                return s;
            }
            otherTree = otherTree.removeMin();

            final Deque<AVLNode<E>> sAncestors = new ArrayDeque<>();
            AVLNode<E> s = this;
            int sAbsolutePosition = s.relativePosition;
            int sParentAbsolutePosition = 0;
            while (s != null && s.height > getHeight(otherTree)) {
                sParentAbsolutePosition = sAbsolutePosition;
                sAncestors.push(s);
                s = s.right;
                if (s != null) {
                    sAbsolutePosition += s.relativePosition;
                }
            }

            otherTreeMin.setRight(otherTree, null);
            otherTreeMin.setLeft(s, maxNode);
            if (otherTree != null) {
                otherTree.min().setLeft(null, otherTreeMin);
                otherTree.relativePosition++;
            }
            if (s != null) {
                s.max().setRight(null, otherTreeMin);
                s.relativePosition = sAbsolutePosition - currentSize;
            }
            otherTreeMin.relativePosition = currentSize - sParentAbsolutePosition;

            s = otherTreeMin;
            while (!sAncestors.isEmpty()) {
                final AVLNode<E> sAncestor = sAncestors.pop();
                sAncestor.setRight(s, null);
                s = sAncestor.balance();
            }
            return s;
        }

//      private void checkFaedelung() {
//          AVLNode maxNode = left.max();
//          if (!maxNode.rightIsFaedelung || maxNode.right != this) {
//              throw new RuntimeException(maxNode + " should right-faedel to " + this);
//          }
//          AVLNode minNode = right.min();
//          if (!minNode.leftIsFaedelung || minNode.left != this) {
//              throw new RuntimeException(maxNode + " should left-faedel to " + this);
//          }
//      }
//
//        private int checkTreeDepth() {
//            int hright = (getRightSubTree() == null ? -1 : getRightSubTree().checkTreeDepth());
//            //          System.out.print("checkTreeDepth");
//            //          System.out.print(this);
//            //          System.out.print(" left: ");
//            //          System.out.print(_left);
//            //          System.out.print(" right: ");
//            //          System.out.println(_right);
//
//            int hleft = (left == null ? -1 : left.checkTreeDepth());
//            if (height != Math.max(hright, hleft) + 1) {
//                throw new RuntimeException(
//                    "height should be max" + hleft + "," + hright + " but is " + height);
//            }
//            return height;
//        }
//
//        private int checkLeftSubNode() {
//            if (getLeftSubTree() == null) {
//                return 0;
//            }
//            int count = 1 + left.checkRightSubNode();
//            if (left.relativePosition != -count) {
//                throw new RuntimeException();
//            }
//            return count + left.checkLeftSubNode();
//        }
//
//        private int checkRightSubNode() {
//            AVLNode right = getRightSubTree();
//            if (right == null) {
//                return 0;
//            }
//            int count = 1;
//            count += right.checkLeftSubNode();
//            if (right.relativePosition != count) {
//                throw new RuntimeException();
//            }
//            return count + right.checkRightSubNode();
//        }

        /**
         * Used for debugging.
         */
        @Override
        public String toString() {
            return new StringBuilder()
                .append("AVLNode(")
                .append(relativePosition)
                .append(CollectionUtils.COMMA)
                .append(left != null)
                .append(CollectionUtils.COMMA)
                .append(value)
                .append(CollectionUtils.COMMA)
                .append(getRightSubTree() != null)
                .append(", faedelung ")
                .append(rightIsNext)
                .append(" )")
                .toString();
        }
    }

    private static class TreeSubList<E> extends AbstractList<E> {
        private final TreeList<E> parent;
        private final int fromIndex;
        private int toIndex;

        public TreeSubList(final TreeList<E> parent, final int fromIndex, final int toIndex) {
            this.parent = parent;
            this.fromIndex = fromIndex;
            this.toIndex = toIndex;
        }

        private int checkSubListIndex(final int relative, final int toModifier) {
            int index = fromIndex + relative;
            if (index < fromIndex || index > toIndex + toModifier) {
                throw new IndexOutOfBoundsException("Invalid index:" + relative + ", size=" + size());
            } else if (index >= parent.size + toModifier) {
                throw new IndexOutOfBoundsException("Invalid index:" + relative +
                        ", refers to main list index " + index + ", size " + parent.size);
            }
            return index;
        }

        @Override
        public boolean isEmpty() {
            return toIndex < fromIndex;
        }

        @Override
        public int size() {
            return toIndex >= fromIndex ? toIndex - fromIndex + 1 : 0;
        }

        @Override
        public E get(final int relative) {
            int index = checkSubListIndex(relative, 0);
            return parent.root.get(index).getValue();
        }

        @Override
        public boolean contains(final Object o) {
            return indexOf(o) != -1;
        }

        @Override
        public int indexOf(final Object object) {
            int index = fromIndex;
            AVLNode<E> node = parent.root.get(index);
            while (index <= toIndex && node != null) {
                if (Objects.equals(node.value, object))
                    return index - fromIndex;
                node = node.next();
                index++;
            }
            return -1;
        }

        @Override
        public int lastIndexOf(final Object object) {
            int index = toIndex;
            AVLNode<E> node = parent.root.get(index);
            while (index >= fromIndex && node != null) {
                if (Objects.equals(node.value, object))
                    return index - fromIndex;
                node = node.previous();
                index--;
            }
            return -1;
        }

        @Override
        public Iterator<E> iterator() {
            return new SubListIterator<>(parent, this, fromIndex, fromIndex, toIndex);
        }

        @Override
        public ListIterator<E> listIterator() {
            return new SubListIterator<>(parent, this, fromIndex, fromIndex, toIndex);
        }

        @Override
        public ListIterator<E> listIterator(final int relative) {
            int index = checkSubListIndex(relative, 1);
            return new SubListIterator<>(parent, this, index, fromIndex, toIndex);
        }

        @Override
        public Spliterator<E> spliterator() {
            return new TreeListSpliterator<>(parent, fromIndex, toIndex);
        }

        @Override
        public boolean add(final E element) {
            parent.add(toIndex + 1, element);
            toIndex++;
            return true;
        }

        @Override
        public void add(final int relative, final E element) {
            int index = checkSubListIndex(relative, 1);
            parent.add(index, element);
            toIndex++;
        }

        @Override
        public E set(final int relative, final E element) {
            int index = checkSubListIndex(relative, 0);
            return parent.set(index, element);
        }

        @Override
        public E remove(final int relative) {
            int index = checkSubListIndex(relative, 0);
            toIndex--;
            return parent.remove(index);
        }

        @Override
        public void clear() {
            parent.removeRange(fromIndex, toIndex);
            toIndex = fromIndex - 1;
        }

        @Override
        protected void removeRange(final int fromRelative, final int toRelative) {
            int fromMain = checkSubListIndex(fromRelative, 0);
            int toMain = checkSubListIndex(toRelative, 0);
            parent.removeRange(fromMain, toMain);
            toIndex -= toRelative - fromRelative + 1;
        }

        @Override
        public List<E> subList(final int fromRelative, final int toRelative) {
            if (fromRelative == toRelative)
                return Collections.emptyList();
            int fromMain = checkSubListIndex(fromRelative, 0);
            int toMain = checkSubListIndex(toRelative, 1);
            return new TreeSubList<>(parent, fromMain, toMain - 1);
        }
    }

    /**
     * A list iterator over the linked list.
     */
    static class TreeListIterator<E> implements ListIterator<E>, OrderedIterator<E> {
        /** The parent list */
        protected final TreeList<E> parent;
        /**
         * Cache of the next node that will be returned by {@link #next()}.
         */
        protected AVLNode<E> next;
        /**
         * The index of the next node to be returned.
         */
        protected int nextIndex;
        /**
         * Cache of the last node that was returned by {@link #next()}
         * or {@link #previous()}.
         */
        protected AVLNode<E> current;
        /**
         * The index of the last node that was returned.
         */
        protected int currentIndex;
        /**
         * The modification count that the list is expected to have. If the list
         * doesn't have this count, then a
         * {@link java.util.ConcurrentModificationException} may be thrown by
         * the operations.
         */
        protected int expectedModCount;

        /**
         * Create a ListIterator for a list.
         *
         * @param parent  the parent list
         * @param fromIndex  the index to start at
         */
        protected TreeListIterator(final TreeList<E> parent, final int fromIndex) throws IndexOutOfBoundsException {
            this.parent = parent;
            this.expectedModCount = parent.modCount;
            this.next = parent.root == null ? null : parent.root.get(fromIndex);
            this.nextIndex = fromIndex;
            this.currentIndex = -1;
        }

        /**
         * Checks the modification count of the list is the value that this
         * object expects.
         *
         * @throws ConcurrentModificationException If the list's modification
         * count isn't the value that was expected.
         */
        protected void checkModCount() {
            if (parent.modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }

        @Override
        public boolean hasNext() {
            return nextIndex < parent.size();
        }

        @Override
        public E next() {
            checkModCount();
            if (!hasNext()) {
                throw new NoSuchElementException("No element at index " + nextIndex + ".");
            }
            if (next == null) {
                next = parent.root.get(nextIndex);
            }
            final E value = next.getValue();
            current = next;
            currentIndex = nextIndex++;
            next = next.next();
            return value;
        }

        @Override
        public boolean hasPrevious() {
            return nextIndex > 0;
        }

        @Override
        public E previous() {
            checkModCount();
            if (!hasPrevious()) {
                throw new NoSuchElementException("Already at start of list.");
            }
            if (next == null) {
                next = parent.root.get(nextIndex - 1);
            } else {
                next = next.previous();
            }
            final E value = next.getValue();
            current = next;
            currentIndex = --nextIndex;
            return value;
        }

        @Override
        public int nextIndex() {
            return nextIndex;
        }

        @Override
        public int previousIndex() {
            return nextIndex() - 1;
        }

        @Override
        public void remove() {
            checkModCount();
            if (currentIndex == -1) {
                throw new IllegalStateException();
            }
            parent.remove(currentIndex);
            if (nextIndex != currentIndex) {
                // remove() following next()
                nextIndex--;
            }
            // the AVL node referenced by next may have become stale after a remove
            // reset it now: will be retrieved by next call to next()/previous() via nextIndex
            next = null;
            current = null;
            currentIndex = -1;
            expectedModCount++;
        }

        @Override
        public void set(final E obj) {
            checkModCount();
            if (current == null) {
                throw new IllegalStateException();
            }
            current.setValue(obj);
        }

        @Override
        public void add(final E obj) {
            checkModCount();
            parent.add(nextIndex, obj);
            current = null;
            currentIndex = -1;
            nextIndex++;
            expectedModCount++;
        }
    }

    /**
     * A list iterator over the linked list, with limited range of indexes.
     */
    static class SubListIterator<E> extends TreeListIterator<E> {
        protected final TreeSubList<E> sublist;

        /**
         * Create a ListIterator for a list.
         *
         * @param parent  the parent list
         * @param fromIndex  the index to start at
         */
        protected SubListIterator(final TreeList<E> parent, final TreeSubList<E> sublist, final int startIndex, final int fromIndex, final int toIndex) throws IndexOutOfBoundsException {
            super(parent, startIndex);
            this.sublist = sublist;
        }

        @Override
        public boolean hasNext() {
            return nextIndex <= sublist.toIndex;
        }

        @Override
        public boolean hasPrevious() {
            return nextIndex > sublist.fromIndex;
        }

        @Override
        public int nextIndex() {
            return nextIndex - sublist.fromIndex;
        }

        @Override
        public void add(final E obj) {
            super.add(obj);
            sublist.toIndex++;
        }

        @Override
        public void remove() {
            super.remove();
            sublist.toIndex--;
        }
    }

    static class TreeListSpliterator<E> implements Spliterator<E> {
        private final TreeList<E> parent;
        /**
         * The modification count that the list is expected to have. If the list
         * doesn't have this count, then a
         * {@link java.util.ConcurrentModificationException} may be thrown by
         * the operations.
         */
        private final int expectedModCount;
        /**
         * The index of the next node to be supplied.
         */
        private int nextIndex;
        /**
         * The last index this spliterator should supply.
         */
        private final int lastIndex;
        /**
         * Cache of the next node that will be returned by {@link #tryAdvance}.
         */
        private AVLNode<E> next;

        public TreeListSpliterator(final TreeList<E> parent) {
            this(parent, 0, parent.size - 1);
        }

        public TreeListSpliterator(final TreeList<E> parent, final int firstIndex, final int lastIndex) {
            this.parent = parent;
            this.expectedModCount = parent.modCount;
            this.nextIndex = firstIndex;
            this.lastIndex = lastIndex;
        }

        /**
         * Checks the modification count of the list is the value that this
         * object expects.
         *
         * @throws ConcurrentModificationException If the list's modification
         * count isn't the value that was expected.
         */
        protected void checkModCount() {
            if (parent.modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }

        @Override
        public boolean tryAdvance(final Consumer<? super E> action) {
            checkModCount();
            if (nextIndex <= lastIndex) {
                if (next == null) {
                    next = parent.root.get(nextIndex);
                }
                action.accept(next.getValue());
                nextIndex++;
                next = next.next();
                return true;
            }
            return false;
        }

        @Override
        public void forEachRemaining(final Consumer<? super E> action) {
            checkModCount();
            int ni = nextIndex, li = lastIndex;
            if (ni > li)
                return;
            AVLNode<E> n = next;
            if (n == null) {
                n = parent.root.get(ni);
            }
            while (ni <= li) {
                action.accept(n.getValue());
                ni++;
                n = n.next();
            }
            nextIndex = ni;
            next = null;
        }

        @Override
        public Spliterator<E> trySplit() {
            checkModCount();
            int splitIndex = nextIndex + (lastIndex - nextIndex) / 2;
            if (nextIndex < splitIndex && splitIndex < lastIndex) {
                Spliterator<E> split = new TreeListSpliterator<>(parent, nextIndex, splitIndex);
                nextIndex = splitIndex + 1;
                return split;
            }
            return null;
        }

        @Override
        public long estimateSize() {
            return lastIndex - nextIndex + 1;
        }

        @Override
        public int characteristics() {
            return Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.ORDERED;
        }
    }

    /**
     * Write the list out using a custom routine.
     *
     * @param out  the output stream
     * @throws IOException if an error occurs while writing to the stream
     */
    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        // Write the size so we know how many nodes to read back
        out.writeInt(size());
        for (final E e : this) {
            out.writeObject(e);
        }
    }

    /**
     * Read the list in using a custom routine.
     *
     * @param in  the input stream
     * @throws IOException if an error occurs while reading from the stream
     * @throws ClassNotFoundException if an object read from the stream can not be loaded
     */
    @SuppressWarnings("unchecked")
    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        final int readSize = in.readInt();
        if (readSize > 0) {
            final ArrayList<E> temp = new ArrayList<>();
            for (int i = 0; i < readSize; i++) {
                temp.add((E) in.readObject());
            }
            root = new AVLNode<>(temp);
        }
        size = readSize;
    }

}
