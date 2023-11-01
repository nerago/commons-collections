package org.apache.commons.collections4;

import java.util.Iterator;
import java.util.SequencedCollection;

public interface SequencedCommonsCollection<E> extends SequencedCollection<E> {
    Iterator<E> descendingIterator();

    @Override
    SequencedCommonsCollection<E> reversed();
}
