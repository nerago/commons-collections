package org.apache.commons.collections4.spliterators;

import java.util.Comparator;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface MapSpliterator<K, V> extends Spliterator<Map.Entry<K, V>> {
    boolean tryAdvance(BiConsumer<? super K, ? super V> action);

    default void forEachRemaining(BiConsumer<? super K, ? super V> action) {
        do { } while (tryAdvance(action));
    }

    @Override
    MapSpliterator<K, V> trySplit();
}
