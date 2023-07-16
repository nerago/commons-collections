package org.apache.commons.collections4.spliterators;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.BiConsumer;

public class EntrySetSpliterator<K, V>
        extends AbstractSpliteratorDecorator<Map.Entry<K, V>, EntrySetSpliterator<K, V>>
        implements MapSpliterator<K, V> {
    public EntrySetSpliterator(Map<K, V> map) {
        this(map.entrySet());
    }

    public EntrySetSpliterator(Collection<Map.Entry<K,V>> entrySet) {
        this(entrySet.spliterator());
    }

    public EntrySetSpliterator(Spliterator<Map.Entry<K,V>> split) {
        super(split);
    }

    @Override
    protected EntrySetSpliterator<K, V> decorateSplit(Spliterator<Map.Entry<K, V>> split) {
        return new EntrySetSpliterator<>(split);
    }

    @Override
    public boolean tryAdvance(BiConsumer<? super K, ? super V> action) {
        return decorated().tryAdvance(entry -> action.accept(entry.getKey(), entry.getValue()));
    }

    @Override
    public void forEachRemaining(BiConsumer<? super K, ? super V> action) {
        decorated().forEachRemaining(entry -> action.accept(entry.getKey(), entry.getValue()));
    }
}
