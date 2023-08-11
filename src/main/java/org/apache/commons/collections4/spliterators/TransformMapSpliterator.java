package org.apache.commons.collections4.spliterators;

import org.apache.commons.collections4.Transformer;

import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class TransformMapSpliterator<K, V, O> implements Spliterator<O> {
    private final MapSpliterator<K, V> spliterator;
    private final BiFunction<? super K, ? super V, ? extends O> transformer;

    public TransformMapSpliterator(final MapSpliterator<K, V> spliterator, final BiFunction<? super K, ? super V, ? extends O> transformer) {
        this.spliterator = spliterator;
        this.transformer = transformer;
    }

    @Override
    public boolean tryAdvance(final Consumer<? super O> action) {
        return spliterator.tryAdvance((k, v) -> action.accept(transformer.apply(k, v)));
    }

    @Override
    public void forEachRemaining(final Consumer<? super O> action) {
        spliterator.forEachRemaining((k, v) -> action.accept(transformer.apply(k, v)));
    }

    @Override
    public Spliterator<O> trySplit() {
        final MapSpliterator<K, V> split = spliterator.trySplit();
        if (split != null)
            return new TransformMapSpliterator<>(split, transformer);
        else
            return null;
    }

    @Override
    public long estimateSize() {
        return spliterator.estimateSize();
    }

    @Override
    public int characteristics() {
        return spliterator.characteristics();
    }

    @Override
    public long getExactSizeIfKnown() {
        return spliterator.getExactSizeIfKnown();
    }

    @Override
    public boolean hasCharacteristics(final int characteristics) {
        return spliterator.hasCharacteristics(characteristics);
    }
}
