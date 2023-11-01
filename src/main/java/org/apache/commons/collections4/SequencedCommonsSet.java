package org.apache.commons.collections4;

import java.util.Iterator;
import java.util.SequencedCollection;
import java.util.SequencedSet;

public interface SequencedCommonsSet<E> extends SequencedSet<E>, SequencedCommonsCollection<E> {
    @Override
    SequencedCommonsSet<E> reversed();
}
