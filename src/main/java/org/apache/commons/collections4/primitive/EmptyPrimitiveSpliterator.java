package org.apache.commons.collections4.primitive;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.LongConsumer;

public class EmptyPrimitiveSpliterator implements Spliterator.OfLong {
    private static final Spliterator.OfLong instance = new EmptyPrimitiveSpliterator();

    public static Spliterator.OfLong emptySpliterator() {
        return instance;
    }

    @Override
    public boolean tryAdvance(final LongConsumer action) {
        return false;
    }

    @Override
    public Spliterator.OfLong trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        return 0;
    }

    @Override
    public int characteristics() {
        return Spliterator.SIZED | Spliterator.IMMUTABLE;
    }
}
