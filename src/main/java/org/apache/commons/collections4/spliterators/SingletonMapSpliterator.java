package org.apache.commons.collections4.spliterators;

import org.apache.commons.collections4.keyvalue.UnmodifiableMapEntry;

import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SingletonMapSpliterator<K, V> implements MapSpliterator<K, V> {
    private final K key;
    private final V value;
    private boolean isComplete = false;

    public SingletonMapSpliterator(final K key, final V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public boolean tryAdvance(BiConsumer<? super K, ? super V> action) {
        if (!isComplete) {
            action.accept(key, value);
            isComplete = true;
        }
        return false;
    }

    @Override
    public void forEachRemaining(BiConsumer<? super K, ? super V> action) {
        if (!isComplete) {
            action.accept(key, value);
            isComplete = true;
        }
    }

    @Override
    public boolean tryAdvance(Consumer<? super Map.Entry<K, V>> action) {
        if (!isComplete) {
            action.accept(new UnmodifiableMapEntry<>(key, value));
            isComplete = true;
        }
        return false;
    }

    @Override
    public void forEachRemaining(Consumer<? super Map.Entry<K, V>> action) {
        if (!isComplete) {
            action.accept(new UnmodifiableMapEntry<>(key, value));
            isComplete = true;
        }
    }

    @Override
    public MapSpliterator<K, V> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        return 1;
    }

    @Override
    public int characteristics() {
        return Spliterator.SIZED | Spliterator.NONNULL;
    }
}
