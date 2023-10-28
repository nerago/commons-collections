package org.apache.commons.collections4.map;

import java.util.Map;
import java.util.SequencedMap;

import org.apache.commons.collections4.CollectionCommonsRole;
import org.apache.commons.collections4.collection.IterationBehaviour;

public class SequencedMapTest<K, V, TMap extends SequencedMap<K, V>>
        extends AbstractMapTest<K, V, TMap> {

    @Override
    public CollectionCommonsRole collectionRole() {
        return null;
    }

    @Override
    public TMap makeObject() {
        return null;
    }

    @Override
    protected IterationBehaviour getIterationBehaviour() {
        return null;
    }
}
