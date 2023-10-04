package org.apache.commons.collections4.bimulti;

import org.apache.commons.collections4.BiMultiMap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableMap;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.MultiSet;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.collections4.set.HashedSet;
import org.apache.commons.collections4.set.UnmodifiableSet;
import org.apache.commons.collections4.spliterators.MapSpliterator;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("CollectionDeclaredAsConcreteClass")
public class DualHashBiMultiMap<K, V> implements BiMultiMap<K, V> {

    protected static class SlotSet<E> extends HashedSet<E> {
        private static final long serialVersionUID = -3193048174884758097L;

        private transient Set<E> wrapper;

        public Set<E> wrapped() {
            if (wrapper == null) {
                wrapper = UnmodifiableSet.unmodifiableSet(this);
            }
            return wrapper;
        }
    }

    private static <E> SlotSet<E> makeSet(final Object ignored) {
        return new SlotSet<>();
    }

    private int entryCount;
    private final Map<K, SlotSet<V>> keyMap = new HashedMap<>();
    private final Map<V, SlotSet<K>> valueMap = new HashedMap<>();

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
        final SlotSet<V> valueSet = keyMap.get(key);
        if (valueSet != null) {
            return valueSet.contains(value);
        }
        return false;
    }

    @Override
    public Set<V> getValues(final K key) {
        final SlotSet<V> valueSet = keyMap.get(key);
        if (valueSet != null) {
            return valueSet.wrapped();
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    public Set<K> getKeys(final V value) {
        final SlotSet<K> keySet = valueMap.get(value);
        if (keySet != null) {
            return keySet.wrapped();
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    public boolean add(final K key, final V value) {
        final SlotSet<V> valueSet = keyMap.computeIfAbsent(key, DualHashBiMultiMap::makeSet);
        final boolean addedValue = valueSet.add(value);

        final SlotSet<K> keySet = valueMap.computeIfAbsent(value, DualHashBiMultiMap::makeSet);
        final boolean addedKey = keySet.add(key);

        if (addedValue != addedKey) {
            throw new IllegalStateException();
        }

        if (addedValue) {
            entryCount++;
        }
        return addedValue;
    }

    @Override
    public boolean addAll(final K key, final Iterable<? extends V> values) {
        return addAllOneSlot(keyMap, valueMap, key, values);
    }

    @Override
    public boolean addAll(final Iterable<? extends K> keys, final V value) {
        return addAllOneSlot(valueMap, keyMap, value, keys);
    }

    private <S, O> boolean addAllOneSlot(final Map<S, SlotSet<O>> slotMap, final Map<O, SlotSet<S>> otherMap,
                                                final S slot, final Iterable<? extends O> others) {
        final SlotSet<O> mapSlot = slotMap.get(slot);
        if (mapSlot == null) {
            final SlotSet<O> slotSet = makeSet(null);
            final boolean changed = CollectionUtils.addAll(slotSet, others);
            if (changed) {
                slotMap.putIfAbsent(slot, slotSet);
                for (final O other : others) {
                    otherMap.computeIfAbsent(other, DualHashBiMultiMap::makeSet).add(slot);
                }
                entryCount += slotSet.size();
                return true;
            }
        } else {
            final int startSize = mapSlot.size();
            final boolean changed = CollectionUtils.addAll(mapSlot, others);
            if (changed) {
                for (final O other : others) {
                    otherMap.computeIfAbsent(other, DualHashBiMultiMap::makeSet).add(slot);
                }
                entryCount += mapSlot.size() - startSize;
                return true;
            }
        }
        return false;
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
    public boolean addAll(final MultiValuedMap<? extends K, ? extends V> mv) {
        boolean changed = false;
        for (final Map.Entry<? extends K, ? extends Collection<? extends V>> entry : mv.asMap().entrySet()) {
            changed |= addAll(entry.getKey(), entry.getValue());
        }
        return changed;
    }

    @Override
    public Set<V> removeKey(final K key) {
        return removeSlotWithOld(key, keyMap, valueMap);
    }

    @Override
    public Set<K> removeValue(final V value) {
        return removeSlotWithOld(value, valueMap, keyMap);
    }

    @Override
    public boolean removeAllKeys(final Collection<K> collection) {
        boolean changed = false;
        for (final K key : collection) {
            changed |= removeSlot(key, keyMap, valueMap);
        }
        return changed;
    }

    @Override
    public boolean removeAllValues(final Collection<V> collection) {
        boolean changed = false;
        for (final V value : collection) {
            changed |= removeSlot(value, valueMap, keyMap);
        }
        return changed;
    }

    private <S, O> Set<O> removeSlotWithOld(final S slot, final Map<S, SlotSet<O>> slotMap, final Map<O, SlotSet<S>> otherMap) {
        final SlotSet<O> removedSet = slotMap.remove(slot);
        if (removedSet != null) {
            for (final O val : removedSet) {
                final SlotSet<S> keySet = otherMap.get(val);
                if (keySet == null || !keySet.remove(slot)) {
                    throw new IllegalStateException();
                }
                if (keySet.isEmpty()) {
                    otherMap.remove(val);
                }
            }
            entryCount -= removedSet.size();
            return removedSet.wrapped();
        } else {
            return Collections.emptySet();
        }
    }

    private <S, O> boolean removeSlot(final S slot, final Map<S, SlotSet<O>> slotMap, final Map<O, SlotSet<S>> otherMap) {
        final SlotSet<O> removedSet = slotMap.remove(slot);
        if (removedSet != null) {
            if (removedSet.isEmpty()) {
                throw new IllegalStateException();
            }
            for (final O val : removedSet) {
                final SlotSet<S> keySet = otherMap.get(val);
                if (keySet == null || !keySet.remove(slot)) {
                    throw new IllegalStateException();
                }
                if (keySet.isEmpty()) {
                    otherMap.remove(val);
                }
            }
            entryCount -= removedSet.size();
            return true;
        }
        return false;
    }

    @Override
    public boolean removeMapping(final K key, final V value) {
        final SlotSet<V> valueSet = keyMap.get(key);
        final boolean removedValue = valueSet != null && valueSet.remove(value);

        final SlotSet<K> keySet = valueMap.get(value);
        final boolean removedKey = keySet != null && keySet.remove(key);

        if (removedValue != removedKey) {
            throw new IllegalStateException();
        }

        if (removedValue) {
            if (valueSet.isEmpty()) {
                keyMap.remove(key);
            }
            if (keySet.isEmpty()) {
                valueMap.remove(value);
            }
            entryCount--;
        }

        return removedValue;
    }

    @Override
    public void clear() {
        entryCount = 0;
        keyMap.clear();
        valueMap.clear();
    }

    @Override
    public Collection<Map.Entry<K, V>> allEntries() {
        return null;
    }

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
