package org.apache.commons.collections4.primitive;

import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import java.util.function.ObjLongConsumer;

import org.apache.commons.collections4.MapIterator;

public interface MapIteratorLongToObject<V> extends MapIterator<Long, V> {
    long nextLong() throws NoSuchElementException;

    @Override
    @Deprecated
    default Long next() throws NoSuchElementException {
        return nextLong();
    }

    long getKeyLong();

    @Override
    @Deprecated
    default Long getKey() {
        return getKeyLong();
    }

    default void forEachRemainingLong(final ObjLongConsumer<V> action) {
        while (hasNext()) {
            final long key = nextLong();
            final V value = getValue();
            action.accept(value, key);
        }
    }

    @Override
    @Deprecated
    default void forEachRemaining(final BiConsumer<? super Long, ? super V> action) {
        forEachRemainingLong((V value, long key) -> action.accept(key, value));
    }

    default void forEachRemainingLong(final LongConsumer action) {
        while (hasNext()) {
            action.accept(nextLong());
        }
    }

    @Override
    @Deprecated
    default void forEachRemaining(final Consumer<? super Long> action) {
        forEachRemainingLong(action::accept);
    }
}
