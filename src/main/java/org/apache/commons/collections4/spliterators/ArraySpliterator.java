package org.apache.commons.collections4.spliterators;

import java.util.Spliterator;
import java.util.function.Consumer;

public class ArraySpliterator<E> implements Spliterator<E> {
    private final E[] array;
    private int currentIndex;
    private final int endIndex;

    public ArraySpliterator(final E[] array) {
        this.array = array;
        this.currentIndex = 0;
        this.endIndex = array.length - 1;
    }

    private ArraySpliterator(final E[] array, final int currentIndex, final int endIndex) {
        this.array = array;
        this.currentIndex = currentIndex;
        this.endIndex = endIndex;
    }

    @Override
    public boolean tryAdvance(final Consumer<? super E> action) {
        if (currentIndex <= endIndex) {
            final E value = array[currentIndex];
            action.accept(value);
            currentIndex++;
            return true;
        }
        return false;
    }

    @Override
    public Spliterator<E> trySplit() {
        final int mid = currentIndex + (endIndex - currentIndex) / 2;
        if (currentIndex < mid && mid < endIndex) {
            final Spliterator<E> split = new ArraySpliterator<>(array, currentIndex, mid);
            currentIndex = mid + 1;
            return split;
        }
        return null;
    }

    @Override
    public long estimateSize() {
        return endIndex - currentIndex + 1;
    }

    @Override
    public int characteristics() {
        return Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.ORDERED;
    }
}
