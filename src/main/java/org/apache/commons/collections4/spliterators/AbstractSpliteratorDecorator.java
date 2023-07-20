package org.apache.commons.collections4.spliterators;

import java.util.Comparator;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;

public abstract class AbstractSpliteratorDecorator<E, T extends Spliterator<E>> implements Spliterator<E> {
    private final Spliterator<E> spliterator;

    protected AbstractSpliteratorDecorator(Spliterator<E> spliterator) {
        this.spliterator = spliterator;
    }

    protected Spliterator<E> decorated() { return spliterator; }

    @Override
    public boolean tryAdvance(Consumer<? super E> action) {
        return spliterator.tryAdvance(action);
    }

    @Override
    public void forEachRemaining(Consumer<? super E> action) {
        spliterator.forEachRemaining(action);
    }

    @Override
    public T trySplit() {
        Spliterator<E> split = spliterator.trySplit();
        if (split != null)
            return decorateSplit(split);
        else
            return null;
    }

    protected abstract T decorateSplit(Spliterator<E> split);

    @Override
    public long estimateSize() {
        return spliterator.estimateSize();
    }

    @Override
    public long getExactSizeIfKnown() {
        return spliterator.getExactSizeIfKnown();
    }

    @Override
    public int characteristics() {
        return spliterator.characteristics();
    }

    @Override
    public boolean hasCharacteristics(int characteristics) {
        return spliterator.hasCharacteristics(characteristics);
    }

    @Override
    public Comparator<? super E> getComparator() {
        return spliterator.getComparator();
    }
}
