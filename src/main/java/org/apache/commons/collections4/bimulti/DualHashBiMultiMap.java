package org.apache.commons.collections4.bimulti;

import org.apache.commons.collections4.BiMultiMap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableMap;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.MultiSet;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.set.HashedSet;
import org.apache.commons.collections4.set.UnmodifiableSet;
import org.apache.commons.collections4.spliterators.MapSpliterator;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("CollectionDeclaredAsConcreteClass")
public class DualHashBiMultiMap<K, V> implements BiMultiMap<K, V> {

    protected static class DHBSet<E> extends HashedSet<E> {
        private static final long serialVersionUID = -3193048174884758097L;

        private transient Set<E> wrapper;

        DHBSet() {

        }

        public Set<E> wrapped() {
            if (wrapper == null) {
                wrapper = UnmodifiableSet.unmodifiableSet(this);
            }
            return wrapper;
        }
    }

    private static <E> DHBSet<E> makeSet(final Object ignored) {
        return new DHBSet<>();
    }

    private int entryCount;

    private HashMap<K, DHBSet<V>> keyMap;
    private HashMap<V, DHBSet<K>> valueMap;

    @Override
    public int size() {
        return entryCount;
    }

    @Override
    public boolean isEmpty() {
        return entryCount == 0;
    }

    @Override
    public boolean containsKey(final K key) {
        return keyMap.containsKey(key);
    }

    @Override
    public boolean containsValue(final V value) {
        return valueMap.containsValue(value);
    }

    @Override
    public boolean containsMapping(final K key, final V value) {
        final DHBSet<V> valueSet = keyMap.get(key);
        if (valueSet != null) {
            return valueSet.contains(value);
        }
        return false;
    }

    @Override
    public Set<V> getValues(final K key) {
        final DHBSet<V> valueSet = keyMap.get(key);
        if (valueSet != null) {
            return valueSet.wrapped();
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    public Set<K> getKeys(final V value) {
        final DHBSet<K> keySet = valueMap.get(value);
        if (keySet != null) {
            return keySet.wrapped();
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    public boolean add(final K key, final V value) {
        final DHBSet<V> valueSet = keyMap.computeIfAbsent(key, DualHashBiMultiMap::makeSet);
        valueSet.add(value);

        final DHBSet<K> keySet = valueMap.computeIfAbsent(value, DualHashBiMultiMap::makeSet);
        return keySet.add(key);
    }

    @Override
    public boolean addAll(final K key, final Iterable<? extends V> values) {
        return addAllOneSlot(keyMap, valueMap, key, values);
    }

    private static <S, O> boolean addAllOneSlot(final HashMap<S, DHBSet<O>> slotMap, final HashMap<O, DHBSet<S>> otherMap,
                                                final S slot, final Iterable<? extends O> others) {
        final DHBSet<O> mapSlot = slotMap.get(slot);
        if (mapSlot == null) {
            final DHBSet<O> slotSet = makeSet(null);
            final boolean changed = CollectionUtils.addAll(slotSet, others);
            if (changed) {
                slotMap.putIfAbsent(slot, slotSet);
                for (final O other : others) {
                    otherMap.computeIfAbsent(other, DualHashBiMultiMap::makeSet).add(slot);
                }
                return true;
            }
        } else {
            final boolean changed = CollectionUtils.addAll(mapSlot, others);
            if (changed) {
                for (final O other : others) {
                    otherMap.computeIfAbsent(other, DualHashBiMultiMap::makeSet).add(slot);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean addAll(final Iterable<? extends K> keys, final V value) {
        final DHBSet<K> mapKeySet = valueMap.get(value);
        final DHBSet<K> keySet = mapKeySet != null ? mapKeySet : makeSet(value);
        final boolean changed = CollectionUtils.addAll(keySet, keys);
        if (changed) {
            for (final K key : keys) {
                keyMap.computeIfAbsent(key, DualHashBiMultiMap::makeSet).add(value);
            }
            if (mapKeySet == null) {
                valueMap.put(value, keySet);
            }
        }
        return changed;
    }

    @Override
    public boolean addAll(final Map<? extends K, ? extends V> map) {
        final Iterator<? extends Map.Entry<? extends K, ? extends V>> it = map.entrySet().iterator();
        boolean changed = false;
        while (it.hasNext()) {
            final Map.Entry<? extends K, ? extends V> entry = it.next();
            changed |= add(entry.getKey(), entry.getValue());
        }
        return changed;
    }

    @Override
    public boolean addAll(final IterableMap<? extends K, ? extends V> map) {
        final MapIterator<? extends K, ? extends V> it = map.mapIterator();
        boolean changed = false;
        while (it.hasNext()) {
            final K key = it.next();
            final V val = it.getValue();
            changed |= add(key, val);
        }
        return changed;
    }

    @Override
    public boolean addAll(final BiMultiMap<? extends K, ? extends V> map) {
        boolean changed = false;
        if (map.keySet().size() < map.valueSet().size()) {
            final MapIterator<? extends K, ? extends Set<? extends V>> it = map.mapIteratorKeys();
            while (it.hasNext()) {
                final K key = it.next();
                changed |= addAll(key, it.getValue());
            }
        } else {
            final MapIterator<? extends V, ? extends Set<? extends K>> it = map.mapIteratorValues();
            while (it.hasNext()) {
                final V value = it.next();
                changed |= addAll(it.getValue(), value);
            }
        }
        return changed;
    }

    @Override
    public boolean addAll(final MultiValuedMap<? extends K, ? extends V> mvm) {
        for (final Map.Entry<? extends K, ? extends Collection<? extends V>> entry : mvm.asMap().entrySet()) {

        }
        return false;
    }

    @Override
    public Set<V> removeKey(final K key) {
        return null;
    }

    @Override
    public Set<K> removeValue(final V value) {
        return null;
    }

    @Override
    public boolean removeAllKeys(final K key) {
        return false;
    }

    @Override
    public Set<V> removeAllValues(final V value) {
        return null;
    }

    @Override
    public boolean removeMapping(final K key, final V value) {
        return false;
    }

    @Override
    public void clear() {

    }

    @Override
    public Collection<Map.Entry<K, V>> allEntries() {
        return null;
    }

//    @Override
//    public Collection<Map.Entry<K, V>> entries() {
//        return null;
//    }

    @Override
    public MultiSet<K> keys() {
        return null;
    }

    @Override
    public Set<K> keySet() {
        return null;
    }

    @Override
    public MultiSet<K> values() {
        return null;
    }

    @Override
    public Set<K> valueSet() {
        return null;
    }

    @Override
    public MultiValuedMap<V, K> asMultiKeyMap() {
        return null;
    }

    @Override
    public MultiValuedMap<K, V> asMultiValueMap() {
        return null;
    }

    @Override
    public BiMultiMap<V, K> inverseBiMultiMap() {
        return null;
    }

    @Override
    public MapIterator<K, V> mapIteratorFull() {
        return null;
    }

    @Override
    public MapIterator<K, Set<V>> mapIteratorKeys() {
        return null;
    }

    @Override
    public MapIterator<V, Set<K>> mapIteratorValues() {
        return null;
    }

    @Override
    public MapSpliterator<K, V> mapSpliterator() {
        return null;
    }
}
