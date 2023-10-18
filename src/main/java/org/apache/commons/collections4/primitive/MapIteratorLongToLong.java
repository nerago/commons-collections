package org.apache.commons.collections4.primitive;

import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import java.util.function.ObjLongConsumer;

import org.apache.commons.collections4.MapIterator;

public interface MapIteratorLongToLong extends MapIterator<Long, Long> {
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

    long getValueLong();
    
    @Override
    @Deprecated
    default Long getValue() {
        return getValueLong();
    }

    long setValue(long value);
    
    @Override
    @Deprecated
    default Long setValue(final Long value) {
        return setValue((long) value);
    }
    
    default void forEachRemaining(final LongBiConsumer action) {
        while (hasNext()) {
            final long key = nextLong();
            final long value = getValue();
            action.accept(key, value);
        }
    }

    @Override
    @Deprecated
    default void forEachRemaining(final BiConsumer<? super Long, ? super Long> action) {
        forEachRemaining((long key, long value) -> action.accept(key, value));
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
