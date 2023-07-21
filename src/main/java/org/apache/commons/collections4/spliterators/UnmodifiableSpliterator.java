package org.apache.commons.collections4.spliterators;

import org.apache.commons.collections4.Unmodifiable;

import java.util.Spliterator;

public class UnmodifiableSpliterator<E>
        extends AbstractSpliteratorDecorator<E, Spliterator<E>>
        implements Spliterator<E>, Unmodifiable {
    public UnmodifiableSpliterator(Spliterator<E> spliterator) {
        super(spliterator);
    }

    @Override
    protected Spliterator<E> decorateSplit(Spliterator<E> split) {
        return new UnmodifiableSpliterator<>(split);
    }

    @Override
    public int characteristics() {
        return decorated().characteristics() | Spliterator.IMMUTABLE;
    }
}
