package org.apache.commons.collections4.primitive;

import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.function.LongConsumer;
import java.util.stream.LongStream;
import java.util.stream.StreamSupport;

public interface LongCollection {
    int size();

    boolean isEmpty();

    boolean contains(long v);

    boolean add(long v);

    boolean remove(long v);

    void forEach(LongConsumer action);

    long[] toLongArray();

    PrimitiveIterator.OfLong iterator();

    Spliterator.OfLong spliterator();

    default LongStream longStream() {
        return StreamSupport.longStream(spliterator(), false);
    }

    default LongStream longParallelStream() {
        return StreamSupport.longStream(spliterator(), true);
    }
}
