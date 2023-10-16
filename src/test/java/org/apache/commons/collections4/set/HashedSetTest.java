package org.apache.commons.collections4.set;

import org.apache.commons.collections4.CollectionCommonsRole;
import org.apache.commons.collections4.collection.IterationBehaviour;

import java.util.Set;

public class HashedSetTest<E> extends AbstractSetTest<E> {
    @Override
    public Set<E> makeObject() {
        return new HashedSet<>();
    }

    @Override
    public CollectionCommonsRole collectionRole() {
        return CollectionCommonsRole.CONCRETE;
    }

    @Override
    protected IterationBehaviour getIterationBehaviour() {
        return IterationBehaviour.UNORDERED;
    }
}
