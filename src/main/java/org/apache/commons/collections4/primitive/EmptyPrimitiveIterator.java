package org.apache.commons.collections4.primitive;

import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;

public class EmptyPrimitiveIterator implements PrimitiveIterator.OfLong {
    private static final PrimitiveIterator.OfLong instance = new EmptyPrimitiveIterator();

    public static PrimitiveIterator.OfLong emptyIterator() {
        return instance;
    }

    @Override
    public long nextLong() {
        throw new NoSuchElementException();
    }

    @Override
    public boolean hasNext() {
        return false;
    }
}
