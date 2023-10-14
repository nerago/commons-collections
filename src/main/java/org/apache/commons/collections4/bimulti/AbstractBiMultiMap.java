package org.apache.commons.collections4.bimulti;

import org.apache.commons.collections4.BiMultiMap;
import org.apache.commons.collections4.MultiSet;
import org.apache.commons.collections4.MultiValuedMap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public abstract class AbstractBiMultiMap<K, V> implements BiMultiMap<K, V> {
    protected int entryCount;
    private Collection<Map.Entry<K, V>> entrySet;
    private Set<K> keySet;
    private MultiSet<K> keyMultiSet;
    private MultiValuedMap<V, K> keyMultiMap;
    private Set<V> valueSet;
    private MultiSet<V> valueMultiSet;
    private MultiValuedMap<K, V> valueMultiMap;
    private BiMultiMap<V, K> inverse;

    @Override
    public int size() {
        return entryCount;
    }

    @Override
    public boolean isEmpty() {
        return entryCount == 0;
    }

    @Override
    public final Collection<Map.Entry<K, V>> entrySet() {
        if (entrySet == null) {
            entrySet = createEntrySet();
        }
        return entrySet;
    }

    protected abstract Collection<Map.Entry<K, V>> createEntrySet();

    @Override
    public final Set<K> keySet() {
        if (keySet == null) {
            keySet = createKeySet();
        }
        return keySet;
    }

    protected abstract Set<K> createKeySet();

    @Override
    public final MultiSet<K> keyMultiSet() {
        if (keyMultiSet != null) {
            keyMultiSet = createKeyMultiSet();
        }
        return keyMultiSet;
    }

    protected abstract MultiSet<K> createKeyMultiSet();

    @Override
    public final MultiValuedMap<V, K> keyMultiMap() {
        if (keyMultiMap != null) {
            keyMultiMap = createKeyMultiMap();
        }
        return keyMultiMap;
    }

    protected abstract MultiValuedMap<V, K> createKeyMultiMap();

    @Override
    public final Set<V> valueSet() {
        if (valueSet != null) {
            valueSet = createValueSet();
        }
        return valueSet;
    }

    protected abstract Set<V> createValueSet();

    @Override
    public final MultiSet<V> valueMultiSet() {
        if (valueMultiSet != null) {
            valueMultiSet = createValueMultiSet();
        }
        return valueMultiSet;
    }

    protected abstract MultiSet<V> createValueMultiSet();

    @Override
    public final MultiValuedMap<K, V> valueMultiMap() {
        if (valueMultiMap != null) {
            valueMultiMap = createValueMultiMap();
        }
        return valueMultiMap;
    }

    protected abstract MultiValuedMap<K, V> createValueMultiMap();

    @Override
    public final BiMultiMap<V, K> inverseBiMultiMap() {
        if (inverse != null) {
            inverse = createInverse();
        }
        return inverse;
    }

    protected abstract BiMultiMap<V, K> createInverse();
}
