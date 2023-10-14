package org.apache.commons.collections4.bimulti;

import org.apache.commons.collections4.BiMultiMap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableMap;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.MultiSet;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.ResettableIterator;
import org.apache.commons.collections4.keyvalue.UnmodifiableMapEntry;
import org.apache.commons.collections4.map.EntrySetUtil;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.collections4.set.AbstractSetDecorator;
import org.apache.commons.collections4.set.HashedSet;
import org.apache.commons.collections4.set.UnmodifiableSet;
import org.apache.commons.collections4.spliterators.MapSpliterator;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

@SuppressWarnings("CollectionDeclaredAsConcreteClass")
public class DualHashBiMultiMap<K, V> extends AbstractBiMultiMap<K, V> {

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

    private final Map<K, SlotSet<V>> keyMap = new HashedMap<>();
    private final Map<V, SlotSet<K>> valueMap = new HashedMap<>();

    @SuppressWarnings("unchecked")
    protected K checkKey(final Object object) {
        return (K) Objects.requireNonNull(object);
    }

    @SuppressWarnings("unchecked")
    protected V checkValue(final Object object) {
        return (V) Objects.requireNonNull(object);
    }

    protected void checkKeyValue(final K key, final V value) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
    }

    @Override
    public boolean containsKey(final K key) {
        return keyMap.containsKey(checkKey(key));
    }

    @Override
    public boolean containsValue(final V value) {
        return valueMap.containsValue(checkValue(value));
    }

    @Override
    public boolean containsMapping(final K key, final V value) {
        checkKeyValue(key, value);
        final SlotSet<V> valueSet = keyMap.get(key);
        if (valueSet != null) {
            return valueSet.contains(value);
        }
        return false;
    }

    @Override
    public Set<V> getValues(final K key) {
        final SlotSet<V> valueSet = keyMap.get(checkKey(key));
        if (valueSet != null) {
            return valueSet.wrapped();
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    public Set<K> getKeys(final V value) {
        final SlotSet<K> keySet = valueMap.get(checkValue(value));
        if (keySet != null) {
            return keySet.wrapped();
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    public boolean add(final K key, final V value) {
        checkKeyValue(key, value);

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
        return addAllOneSlot(keyMap, valueMap, checkKey(key), values);
    }

    @Override
    public boolean addAll(final Iterable<? extends K> keys, final V value) {
        return addAllOneSlot(valueMap, keyMap, checkValue(value), keys);
    }

    private <S, O> boolean addAllOneSlot(final Map<S, SlotSet<O>> slotMap, final Map<O, SlotSet<S>> otherMap,
                                                final S slot, final Iterable<? extends O> others) {
        for (final O other : others) {
            Objects.requireNonNull(other);
        }

        SlotSet<O> slotSet = slotMap.get(slot);
        final int startSize;
        if (slotSet == null) {
            slotSet = makeSet(null);
            startSize = 0;
        } else {
            startSize = slotSet.size();
        }

        final boolean changed = CollectionUtils.addAll(slotSet, others);
        if (changed) {
            for (final O other : others) {
                otherMap.computeIfAbsent(other, DualHashBiMultiMap::makeSet).add(slot);
            }
            entryCount += slotSet.size() - startSize;
            if (startSize == 0) {
                slotMap.put(slot, slotSet);
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
        return removeSlotReturningOld(key, keyMap, valueMap);
    }

    @Override
    public Set<K> removeValue(final V value) {
        return removeSlotReturningOld(value, valueMap, keyMap);
    }

    private <S, O> Set<O> removeSlotReturningOld(final S slot, final Map<S, SlotSet<O>> slotMap, final Map<O, SlotSet<S>> otherMap) {
        final SlotSet<O> removedSet = slotMap.remove(slot);
        if (removedSet != null) {
            if (removedSet.isEmpty()) {
                throw new IllegalStateException();
            }
            for (final O item : removedSet) {
                fixupPostRemove(otherMap, slot, item);
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
            for (final O item : removedSet) {
                fixupPostRemove(otherMap, slot, item);
            }
            entryCount -= removedSet.size();
            return true;
        }
        return false;
    }

    private static <S, O> void fixupPostRemove(final Map<O, SlotSet<S>> otherMap, final S slot, final O item) {
        final SlotSet<S> slotSet = otherMap.get(item);
        if (slotSet == null || !slotSet.remove(slot)) {
            throw new IllegalStateException();
        }
        if (slotSet.isEmpty()) {
            otherMap.remove(item);
        }
    }

    @Override
    public boolean removeAllKeys(final Collection<K> collection) {
        return removeAllGenericByItem(collection, keyMap, valueMap);
    }

    @Override
    public boolean removeAllValues(final Collection<V> collection) {
        return removeAllGenericByItem(collection, valueMap, keyMap);
    }

    private <S, O> boolean removeAllGeneric(final Collection<S> collection, final Map<S, SlotSet<O>> slotMap, final Map<O, SlotSet<S>> otherMap) {
        if (collection.size() < slotMap.size()) {
            return removeAllGenericByItem(collection, slotMap, otherMap);
        } else {
            return removeAllGenericBulk(collection, slotMap, otherMap);
        }
    }

    private <S, O> boolean removeAllGenericByItem(final Collection<S> collection, final Map<S, SlotSet<O>> slotMap, final Map<O, SlotSet<S>> otherMap) {
        boolean changed = false;
        for (final S item : collection) {
            changed |= removeSlot(item, slotMap, otherMap);
        }
        return changed;
    }

    private <S, O> boolean removeAllGenericBulk(final Collection<S> collection, final Map<S, SlotSet<O>> slotMap, final Map<O, SlotSet<S>> otherMap) {
        final boolean changed = slotMap.keySet().removeAll(collection);
        if (changed) {
            final Iterator<SlotSet<S>> valueIterator = otherMap.values().iterator();
            while (valueIterator.hasNext()) {
                final SlotSet<S> slotSet = valueIterator.next();
                final int startSize = slotSet.size();
                if (slotSet.removeAll(collection)) {
                    entryCount -= startSize - slotSet.size();
                    if (slotSet.isEmpty()) {
                        valueIterator.remove();
                    }
                }
            }
        }
        return changed;
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
    protected Collection<Map.Entry<K, V>> createEntrySet() {
        return new EntrySetView();
    }

    @Override
    protected Set<K> createKeySet() {
        return new KeySetView();
    }

    @Override
    protected MultiSet<K> createKeyMultiSet() {
        return null; // TODO
    }

    @Override
    protected MultiValuedMap<V, K> createKeyMultiMap() {
        return null; // TODO
    }

    @Override
    protected Set<V> createValueSet() {
        return new ValueSetView();
    }

    @Override
    protected MultiSet<V> createValueMultiSet() {
        return null; // TODO
    }

    @Override
    protected MultiValuedMap<K, V> createValueMultiMap() {
        return null; // TODO
    }

    @Override
    protected BiMultiMap<V, K> createInverse() {
        return null; // TODO
    }

    @Override
    public MapIterator<K, V> mapIteratorEntries() {
        return new EntryMapIterator();
    }

    @Override
    public MapIterator<K, Set<V>> mapIteratorKeys() {
        return null; // TODO
    }

    @Override
    public MapIterator<V, Set<K>> mapIteratorValues() {
        return null; // TODO
    }

    @Override
    public MapSpliterator<K, V> mapSpliteratorEntries() {
        return null; // TODO
    }

    private abstract class SetView<E> extends AbstractSetDecorator<E> {
        protected SetView(final Set<E> set) {
            super(set);
        }

        @Override
        public void clear() {
            DualHashBiMultiMap.this.clear();
        }

        @Override
        public boolean removeIf(final Predicate<? super E> filter) {
            return IteratorUtils.removeIf(iterator(), filter);
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean removeAll(final Collection<?> coll) {
            return removeAllValues((Collection<V>) coll);
        }

        @Override
        public boolean retainAll(final Collection<?> coll) {
            return removeIf(e -> !coll.contains(e));
        }
    }

    private final class KeySetView extends SetView<K> {
        private static final long serialVersionUID = 4363160454265549798L;

        private KeySetView() {
            super(keyMap.keySet());
        }

        @Override
        public Iterator<K> iterator() {
            return mapIteratorKeys();
        }

        @Override
        public boolean remove(final Object object) {
            return removeSlot(checkKey(object), keyMap, valueMap);
        }

    }

    private final class ValueSetView extends SetView<V> {
        private static final long serialVersionUID = -6810547806861188320L;

        private ValueSetView() {
            super(valueMap.keySet());
        }

        @Override
        public Iterator<V> iterator() {
            return mapIteratorValues();
        }

        @Override
        public boolean remove(final Object object) {
            return removeSlot(checkValue(object), valueMap, keyMap);
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean removeAll(final Collection<?> coll) {
            return removeAllKeys((Collection<K>) coll);
        }
    }

    private class EntrySetView extends AbstractSet<Map.Entry<K, V>> {
        @Override
        public int size() {
            return entryCount;
        }

        @Override
        public boolean isEmpty() {
            return entryCount != 0;
        }

        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return new EntryIterator();
        }

        @Override
        public void clear() {
            DualHashBiMultiMap.this.clear();
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean contains(final Object obj) {
            if (obj instanceof Map.Entry) {
                final Map.Entry<K, V> entry = (Map.Entry<K, V>) obj;
                return containsMapping(entry.getKey(), entry.getValue());
            }
            return false;
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean remove(final Object obj) {
            if (obj instanceof Map.Entry) {
                final Map.Entry<K, V> entry = (Map.Entry<K, V>) obj;
                return removeMapping(entry.getKey(), entry.getValue());
            }
            return false;
        }

        @Override
        public boolean retainAll(final Collection<?> coll) {
            return removeIf(e -> !coll.contains(e));
        }

        @Override
        public boolean removeAll(final Collection<?> coll) {
            return removeIf(coll::contains);
        }

        @Override
        public Object[] toArray() {
            return EntrySetUtil.toArrayUnmodifiable(mapIteratorEntries(), size());
        }

        @Override
        public <T> T[] toArray(final T[] array) {
            return EntrySetUtil.toArrayUnmodifiable(mapIteratorEntries(), size(), array);
        }
    }

    protected abstract class AbstractEntryIterator {
        private Iterator<Map.Entry<K, SlotSet<V>>> slotIterator;
        private Iterator<V> itemIterator;
        private boolean shouldDeleteKeySlot;
        protected boolean validEntry;
        protected K key;
        protected V value;

        protected AbstractEntryIterator() {
            reset();
        }

        public void reset() {
            slotIterator = keyMap.entrySet().iterator();
            itemIterator = null;
            shouldDeleteKeySlot = false;
            validEntry = false;
            key = null;
            value = null;
        }

        public boolean hasNext() {
            return slotIterator.hasNext() || (itemIterator != null && itemIterator.hasNext());
        }

        protected void nextItem() throws NoSuchElementException {
            // if we haven't removed last entry then know we need to keep the current key slot
            if (validEntry) {
                shouldDeleteKeySlot = false;
            }

            // move onto next slot if we don't have values
            if (itemIterator == null || !itemIterator.hasNext()) {
                nextSlot();
            }

            // common case, move down the slot
            value = itemIterator.next();
            validEntry = true;
        }

        protected void nextSlot() throws NoSuchElementException {
            // if we haven't kept any elements then can remove the slot
            if (shouldDeleteKeySlot) {
                slotIterator.remove();
            }

            final Map.Entry<K, SlotSet<V>> slotEntry = slotIterator.next();
            final SlotSet<V> content = slotEntry.getValue();
            if (content.isEmpty()) {
                throw new IllegalStateException();
            }

            key = slotEntry.getKey();
            shouldDeleteKeySlot = true;
            itemIterator = content.iterator();
        }

        public void remove() {
            if (!validEntry) {
                throw new IllegalStateException();
            }
            itemIterator.remove();
            fixupPostRemove(valueMap, key, value);
            validEntry = false;
        }
    }

    protected class EntryMapIterator extends AbstractEntryIterator implements MapIterator<K, V>, ResettableIterator<K> {
        @Override
        public K next() throws NoSuchElementException {
            nextItem();
            return key;
        }

        @Override
        public K getKey() {
            if (!validEntry) {
                throw new IllegalStateException();
            }
            return key;
        }

        @Override
        public V getValue() {
            if (!validEntry) {
                throw new IllegalStateException();
            }
            return value;
        }

        @Override
        public V setValue(final V value) {
            throw new UnsupportedOperationException();
        }
    }

    protected class EntryIterator extends AbstractEntryIterator implements ResettableIterator<Map.Entry<K, V>> {
        @Override
        public Map.Entry<K, V> next() throws NoSuchElementException {
            nextItem();
            return new UnmodifiableMapEntry<>(key, value);
        }
    }
}
