package org.apache.commons.collections4.collection;

import java.io.Serializable;
import java.util.Collection;

public interface IndexedCollectionInterface<K, C> extends Collection<C>, Serializable {

    C get(K key);

    Collection<C> values(K key);

    void reindex();

}
