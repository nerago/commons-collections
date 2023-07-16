package org.apache.commons.collections4.spliterators;


import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class EmptyMapSpliterator<K, V> implements MapSpliterator<K, V> {
    @SuppressWarnings("rawtypes")
    public static final MapSpliterator INSTANCE = new EmptyMapSpliterator();

    @SuppressWarnings("unchecked")
    public static <V, K> MapSpliterator<K,V> emptyMapSpliterator() {
        return INSTANCE;
    }

    @Override
    public boolean tryAdvance(BiConsumer<? super K, ? super V> action) {
        Objects.requireNonNull(action);
        return false;
    }

    @Override
    public boolean tryAdvance(Consumer<? super Map.Entry<K, V>> action) {
        Objects.requireNonNull(action);
        return false;
    }

    @Override
    public void forEachRemaining(BiConsumer<? super K, ? super V> action) {
        Objects.requireNonNull(action);
    }

    @Override
    public void forEachRemaining(Consumer<? super Map.Entry<K, V>> action) {
        Objects.requireNonNull(action);
    }

    @Override
    public MapSpliterator<K, V> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        return 0;
    }

    @Override
    public int characteristics() {
        return Spliterator.SIZED | Spliterator.SUBSIZED;
    }
}
