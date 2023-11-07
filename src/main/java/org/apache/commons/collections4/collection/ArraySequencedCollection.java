package org.apache.commons.collections4.collection;

import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.SequencedCollection;
import java.util.Spliterator;
import java.util.function.Predicate;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SequencedCommonsCollection;
import org.apache.commons.collections4.ToArrayUtils;
import org.apache.commons.collections4.Unmodifiable;
import org.apache.commons.collections4.iterators.ObjectArrayIterator;
import org.apache.commons.collections4.iterators.ObjectArrayReverseIterator;
import org.apache.commons.collections4.set.EmptySet;
import org.apache.commons.collections4.spliterators.ArraySpliterator;


// similar to a lightweight Unmodifiable ArrayList
public final class ArraySequencedCollection<E> extends AbstractCollection<E> implements SequencedCommonsCollection<E>, Unmodifiable {
    private final E[] array;

    @SuppressWarnings("unchecked")
    public static <E> SequencedCommonsCollection<E> sequencedCollection(final Collection<E> collection) {
        if (collection instanceof SequencedCommonsCollection<E>) {
            return (SequencedCommonsCollection<E>) collection;
        } else if (collection instanceof SequencedCollection<E>) {
            // TODO adapter
        } else if (collection.isEmpty()) {
            return EmptySet.emptySet();
        } else {
            return new ArraySequencedCollection<>((E[]) collection.toArray());
        }
    }

    @SuppressWarnings("unchecked")
    public static <E> SequencedCommonsCollection<E> reverseSequencedCollection(final Collection<E> collection) {
        if (collection.isEmpty()) {
            return EmptySet.emptySet();
        } else {
            final E[] array = (E[]) collection.toArray();
            CollectionUtils.reverseArray(array);
            return new ArraySequencedCollection<>(array);
        }
    }

    private ArraySequencedCollection(final E[] array) {
        this.array = array;
    }

    @Override
    public int size() {
        return array.length;
    }

    @Override
    public boolean contains(final Object obj) {
        for (final E element : array) {
            if (Objects.equals(element, obj)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public E getFirst() {
        if (array.length > 0) {
            return array[array.length - 1];
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public E getLast() {
        if (array.length > 0) {
            return array[0];
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public SequencedCommonsCollection<E> reversed() {
        final E[] reverse = CollectionUtils.reverseArrayCopy(array);
        return new ArraySequencedCollection<>(reverse);
    }

    @Override
    public Iterator<E> iterator() {
        return new ObjectArrayIterator<>(array);
    }

    @Override
    public Iterator<E> descendingIterator() {
        return new ObjectArrayReverseIterator<>(array);
    }

    @Override
    public Spliterator<E> spliterator() {
        return new ArraySpliterator<>(array);
    }

    @Override
    public Object[] toArray() {
        return Arrays.copyOf(array, array.length);
    }

    @Override
    public <T> T[] toArray(final T[] param) {
        if (param.length >= array.length) {
            System.arraycopy(array, 0, param, 0, array.length);
            Arrays.fill(param, array.length, param.length, null);
            return param;
        }
        return ToArrayUtils.fromIteratorAndSize(iterator(), size(), param);
    }

    @Override
    public boolean remove(final Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(final Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addFirst(final E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addLast(final E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E removeFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public E removeLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeIf(final Predicate<? super E> filter) {
        throw new UnsupportedOperationException();
    }
}
