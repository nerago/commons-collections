package org.apache.commons.collections4.list;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class SplittableArrayList<E> extends AbstractList<E> {

    public static final int DEFAULT_NODE_SIZE = 4;

    private static class Node<E> {
        Node<E> left;
        int firstIndex;
        int contentStart, contentEnd, contentStartLimit, contentEndLimit;
        E[] content;
        Node<E> right;
        Node<E> parent;

        @SuppressWarnings("unchecked")
        public Node(int size, E value) {
            int index = size / 3;
            content = (E[]) new Object[size];
            content[index] = value;
            contentStart = index;
            contentEnd = index;
            contentStartLimit = 0;
            contentEndLimit = size - 1;
        }

        public Node(int headSize, Node<E> original) {
            content = original.content;
            contentStart = original.contentStart;
            contentStartLimit = original.contentStartLimit;
            contentEnd = original.contentEnd - original.count() + headSize;
            contentEndLimit = contentEnd;
        }

        public Node(Node<E> original, int tailSize) {
            content = original.content;
            contentStart = original.contentStart + original.count() - tailSize;
            contentStartLimit = contentStart;
            contentEnd = original.contentEnd;
            contentEndLimit = original.contentEndLimit;
        }

        int lastIndex() {
            return firstIndex + contentEnd - contentStart;
        }

        int count() {
            return contentEnd - contentStart + 1;
        }

        Node<E> successor() {
            if (right != null)
                return right;
            Node<E> curr = this, parent = this.parent;
            while (parent != null) {
                if (curr == parent.left)
                    return parent;
                curr = parent;
                parent = curr.parent;
            }
            return null;
        }

        void addToIndexAndFollowing(int delta) {
            // TODO better methods if this my making firstIndexRelative then only need to set on relevant parents?
            Node<E> curr = this;
            do {
                curr.firstIndex += delta;
                curr = curr.successor();
            } while (curr != null);
        }

        public void addToIndexFollowingOnly(int delta) {
            Node<E> curr = successor();
            while (curr != null) {
                curr.firstIndex += delta;
                curr = curr.successor();
            }
        }

        public void replaceLink(Node<E> prev, Node<E> replace) {
            if (left == prev)
                left = replace;
            else if (right == prev)
                right = replace;
        }
    }

    private Node<E> root;

    private Node<E> firstNode() {
        Node<E> node = root;
        if (node == null)
            return null;
        while (true) {
            Node<E> left = node.left;
            if (left == null)
                return node;
            else
                node = left;
        }
    }

    private Node<E> lastNode() {
        Node<E> node = root;
        if (node == null)
            return null;
        while (true) {
            Node<E> right = node.right;
            if (right == null)
                return node;
            else
                node = right;
        }
    }

    @Override
    public E get(int index) {
        Node<E> node = root;
        while (node != null) {
            if (index < node.firstIndex) {
                node = node.left;
            } else if (index > node.lastIndex()) {
                node = node.right;
            } else {
                return node.content[node.contentStart + index - node.firstIndex];
            }
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int size() {
        Node<E> node = firstNode();
        int count = 0;
        while (node != null) {
            count += node.count();
            node = node.successor();
        }
        return count;
    }

    @Override
    public boolean add(E e) {
        Node<E> last = lastNode();
        if (last == null) {
            Node<E> add = new Node<>(DEFAULT_NODE_SIZE, e);
            root = add;
            add.firstIndex = 0;
        } else if (last.contentEnd < last.contentEndLimit) {
            last.contentEnd++;
            last.content[last.contentEnd] = e;
        } else {
            Node<E> add = new Node<>(last.content.length * 2, e);
            last.right = add;
            add.parent = last;
            add.firstIndex = last.lastIndex() + 1;
        }
        return true;
    }

    @Override
    public E set(int index, E element) {
        Node<E> node = root;
        while (node != null) {
            if (index < node.firstIndex) {
                node = node.left;
            } else if (index > node.lastIndex()) {
                node = node.right;
            } else {
                int contentIndex = node.contentStart + index - node.firstIndex;
                E oldValue = node.content[contentIndex];
                node.content[contentIndex] = element;
                return oldValue;
            }
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void add(int index, E element) {
        Node<E> node = root;
        if (node == null) {
            Node<E> add = new Node<>(DEFAULT_NODE_SIZE, element);
            root = add;
            add.firstIndex = 0;
            return;
        }
        while (true) {
            if (index <= node.firstIndex) {
                if (node.contentStartLimit < node.contentStart && index == node.firstIndex - 1) {
                    node.contentStart--;
                    node.content[node.contentStart] = element;
                    node.firstIndex--;
                    node.addToIndexFollowingOnly(1);
                    return;
                } else if (node.contentStartLimit < node.contentStart && index == node.firstIndex) {
                    node.contentStart--;
                    node.content[node.contentStart] = element;
                    node.addToIndexFollowingOnly(1);
                    return;
                } else if (node.left != null) {
                    node = node.left;
                } else if (node.firstIndex == 0 && index == 0) {
                    Node<E> add = new Node<>(node.content.length, element);
                    node.left = add;
                    add.parent = node;
                    add.firstIndex = index;
                    node.addToIndexAndFollowing(1);
                    return;
                } else {
                    throw new IndexOutOfBoundsException();
                }
            } else if (index >= node.lastIndex()) {
                if (node.contentEnd < node.contentEndLimit && index == node.lastIndex() + 1) {
                    node.contentEnd++;
                    node.content[node.contentEnd] = element;
                    node.addToIndexFollowingOnly(1);
                    return;
                } else if (node.right != null) {
                    node = node.right;
                } else if (index == node.lastIndex() + 1) {
                    Node<E> add = new Node<>(node.content.length, element);
                    node.right = add;
                    add.parent = node;
                    add.firstIndex = index;
                    return;
                } else {
                    throw new IndexOutOfBoundsException();
                }
            } else {
                // split node
                Node<E> add = new Node<>(DEFAULT_NODE_SIZE, element);
                add.parent = node.parent;
                if (index > node.firstIndex) {
                    int splitSize = index - node.firstIndex;
                    Node<E> left = new Node<>(splitSize, node);
                    add.left = left;
                    left.parent = add;
                    left.firstIndex = node.firstIndex;
                    add.firstIndex = node.firstIndex + splitSize;
                    if (node.left != null) {
                        left.left = node.left;
                        left.left.parent = left;
                    }
                } else {
                    add.firstIndex = node.firstIndex;
                    if (node.left != null) {
                        add.left = node.left;
                        add.left.parent = add;
                    }
                }
                if (index < node.lastIndex()) {
                    int splitSize = node.lastIndex() - index;
                    Node<E> right = new Node<>(node, splitSize);
                    add.right = right;
                    right.parent = add;
                    right.firstIndex = node.lastIndex() - splitSize + 1;
                    if (node.right != null) {
                        right.right = node.right;
                        right.right.parent = right;
                    }
                    right.addToIndexFollowingOnly(1);
                } else {
                    add.addToIndexFollowingOnly(1);
                }
            }
        }
    }

    @Override
    public E remove(int index) {
        Node<E> node = root;
        while (node != null) {
            if (index < node.firstIndex) {
                node = node.left;
            } else if (index == node.firstIndex && node.contentStart == node.contentEnd) {
                E oldValue = node.content[node.contentStart];
                Node<E> successor = node.successor();
                removeNode(node);
                if (successor != null)
                    successor.addToIndexAndFollowing(-1);
                return oldValue;
            } else if (index == node.firstIndex) {
                E oldValue = node.content[node.contentStart];
                node.content[node.contentStart] = null;
                node.contentStart++;
                node.addToIndexFollowingOnly(-1);
                return oldValue;
            } else if (index > node.lastIndex()) {
                node = node.right;
            } else if (index == node.lastIndex()) {
                E oldValue = node.content[node.contentEnd];
                node.content[node.contentEnd] = null;
                node.contentEnd--;
                node.addToIndexFollowingOnly(-1);
                return oldValue;
            } else {
                int splitSizeLeft = index - node.firstIndex;
                Node<E> left = new Node<>(splitSizeLeft, node);
                if (node.left != null) {
                    left.left = node.left;
                    left.left.parent = left;
                }
                node.left = left;
                left.parent = node;

                int splitSizeRight = node.lastIndex() - index;
                Node<E> right = new Node<>(node, splitSizeRight);
                if (node.right != null) {
                    right.right = node.right;
                    right.right.parent = right;
                }
                node.right = right;
                right.parent = node;
            }
        }
    }

    private void removeNode(Node<E> node) {
        if (node.parent == null) {
            if (node.left == null) {
                if (node.right == null) {
                    root = null;
                } else {
                    root = node.right;
                    root.parent = null;
                }
            } else {
                if (node.right == null) {
                    root = node.left;
                    root.parent = null;
                } else {
                    root = node.left;
                    root.parent = null;
                    Node<E> right = node.right;
                    Node<E> placement = root;
                    while (placement.right != null) {
                        placement = placement.right;
                    }
                    placement.right = right;
                    right.parent = placement;
                }
            }
        } else {
            if (node.left == null) {
                if (node.right == null) {

                } else {

                }
            } else {
                if (node.right == null) {

                } else {

                }
            }
        }
    }

    @Override
    public int indexOf(Object o) {
        return super.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return super.lastIndexOf(o);
    }

    @Override
    public void clear() {
        super.clear();
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        return super.addAll(index, c);
    }

    @Override
    public Iterator<E> iterator() {
        return super.iterator();
    }

    @Override
    public ListIterator<E> listIterator() {
        return super.listIterator();
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return super.listIterator(index);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return super.subList(fromIndex, toIndex);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        super.removeRange(fromIndex, toIndex);
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return super.contains(o);
    }

    @Override
    public Object[] toArray() {
        return super.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return super.toArray(a);
    }

    @Override
    public boolean remove(Object o) {
        return super.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return super.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return super.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return super.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return super.retainAll(c);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        super.forEach(action);
    }

    @Override
    public Spliterator<E> spliterator() {
        return super.spliterator();
    }

    @Override
    public Stream<E> stream() {
        return super.stream();
    }

    @Override
    public Stream<E> parallelStream() {
        return super.parallelStream();
    }


    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        return super.removeIf(filter);
    }

    @Override
    public void replaceAll(UnaryOperator<E> operator) {
        super.replaceAll(operator);
    }

    @Override
    public void sort(Comparator<? super E> c) {
        super.sort(c);
    }

}
