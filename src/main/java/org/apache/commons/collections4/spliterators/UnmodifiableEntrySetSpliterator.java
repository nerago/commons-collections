package org.apache.commons.collections4.spliterators;

import org.apache.commons.collections4.keyvalue.UnmodifiableMapEntry;

import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;

public class UnmodifiableEntrySetSpliterator<K, V>
        extends AbstractSpliteratorDecorator<Map.Entry<K, V>, UnmodifiableEntrySetSpliterator<K, V>> {

    public UnmodifiableEntrySetSpliterator(Spliterator<Map.Entry<K, V>> spliterator) {
        super(spliterator);
    }

    protected Consumer<? super Map.Entry<K, V>> wrapAction(Consumer<? super Map.Entry<K, V>> action) {
        return entry -> action.accept(new UnmodifiableMapEntry<>(entry));
    }

    @Override
    public boolean tryAdvance(Consumer<? super Map.Entry<K, V>> action) {
        return decorated().tryAdvance(wrapAction(action));
    }

    @Override
    public void forEachRemaining(Consumer<? super Map.Entry<K, V>> action) {
        decorated().forEachRemaining(wrapAction(action));
    }

    @Override
    protected UnmodifiableEntrySetSpliterator<K, V> decorateSplit(Spliterator<Map.Entry<K, V>> split) {
        return new UnmodifiableEntrySetSpliterator<>(split);
    }

}
