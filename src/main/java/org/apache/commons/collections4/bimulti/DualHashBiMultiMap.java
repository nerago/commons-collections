package org.apache.commons.collections4.bimulti;

import org.apache.commons.collections4.BiMultiMap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableMap;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.MultiSet;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.ResettableIterator;
import org.apache.commons.collections4.iterators.TransformIterator;
import org.apache.commons.collections4.keyvalue.UnmodifiableMapEntry;
import org.apache.commons.collections4.map.EntrySetUtil;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.collections4.multiset.AbstractMultiSet;
import org.apache.commons.collections4.set.AbstractSetDecorator;
import org.apache.commons.collections4.set.HashedSet;
import org.apache.commons.collections4.set.UnmodifiableSet;
import org.apache.commons.collections4.spliterators.MapSpliterator;
import org.apache.commons.collections4.spliterators.TransformSpliterator;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
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
    private static <S> S checkParam(final Object object) {
        return (S) Objects.requireNonNull(object);
    }

    protected void checkKeyValue(final K key, final V value) {
        checkParam(key);
        checkParam(value);
    }

    @Override
    public boolean containsKey(final K key) {
        return keyMap.containsKey(DualHashBiMultiMap.<K>checkParam(key));
    }

    @Override
    public boolean containsValue(final V value) {
        return valueMap.containsValue(DualHashBiMultiMap.<V>checkParam(value));
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
        final SlotSet<V> valueSet = keyMap.get(DualHashBiMultiMap.<K>checkParam(key));
        if (valueSet != null) {
            return valueSet.wrapped();
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    public Set<K> getKeys(final V value) {
        final SlotSet<K> keySet = valueMap.get(DualHashBiMultiMap.<V>checkParam(value));
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
        return addAllOneSlot(keyMap, valueMap, checkParam(key), values);
    }

    @Override
    public boolean addAll(final Iterable<? extends K> keys, final V value) {
        return addAllOneSlot(valueMap, keyMap, checkParam(value), keys);
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

    private <S, O> void fixupPostRemove(final Map<O, SlotSet<S>> otherMap, final S slot, final Collection<O> itemCollection) {
        for (final O item : itemCollection) {
            fixupPostRemove(otherMap, slot, item);
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
        return new EntrySetView<>(this);
    }

    @Override
    protected Set<K> createKeySet() {
        return new SetView<>(this, keyMap, valueMap);
    }

    @Override
    protected Set<V> createValueSet() {
        return new SetView<>(this, valueMap, keyMap);
    }

    @Override
    protected MultiSet<K> createKeyMultiSet() {
        return new MultiSetView<>(this, keyMap, valueMap);
    }

    @Override
    protected MultiSet<V> createValueMultiSet() {
        return new MultiSetView<>(this, valueMap, keyMap);
    }

    @Override
    protected MultiValuedMap<V, K> createKeyMultiMap() {
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
        return new EntryMapIterator<>(this);
    }

    @Override
    public MapSpliterator<K, V> mapSpliteratorEntries() {
        return new EntryMapSpliterator<>(this);
    }

    @Override
    public MapIterator<K, Set<V>> mapIteratorKeys() {
        return new MapIteratorForSets<>(this, keyMap, valueMap);
    }

    @Override
    public MapIterator<V, Set<K>> mapIteratorValues() {
        return new MapIteratorForSets<>(this, valueMap, keyMap);
    }

    private static final class SetView<S, O> extends AbstractSetDecorator<S> {
        private static final long serialVersionUID = -994466265223876023L;
        private final DualHashBiMultiMap<?, ?> parent;
        private final Map<S, SlotSet<O>> primaryMap;
        private final Map<O, SlotSet<S>> otherMap;

        private SetView(final DualHashBiMultiMap<?, ?> parent, final Map<S, SlotSet<O>> primaryMap, final Map<O, SlotSet<S>> otherMap) {
            super(primaryMap.keySet());
            this.parent = parent;
            this.primaryMap = primaryMap;
            this.otherMap = otherMap;
        }

        @Override
        public void clear() {
            parent.clear();
        }

        @Override
        public boolean remove(final Object object) {
            return parent.removeSlot(checkParam(object), primaryMap, otherMap);
        }

        @Override
        public boolean removeIf(final Predicate<? super S> filter) {
            return IteratorUtils.removeIf(iterator(), filter);
        }

        @Override
        public boolean retainAll(final Collection<?> coll) {
            return removeIf(e -> !coll.contains(e));
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean removeAll(final Collection<?> coll) {
            return parent.removeAllGenericByItem((Collection<S>) coll, primaryMap, otherMap);
        }

        @Override
        public Iterator<S> iterator() {
            return new MapIteratorForSets<>(parent, primaryMap, otherMap);
        }
    }

    private static final class MultiSetView<S, O> extends AbstractMultiSet<S> {
        private final DualHashBiMultiMap<?, ?> parent;
        private final Map<S, SlotSet<O>> primaryMap;
        private final Map<O, SlotSet<S>> otherMap;

        private MultiSetView(final DualHashBiMultiMap<?, ?> parent, final Map<S, SlotSet<O>> primaryMap, final Map<O, SlotSet<S>> otherMap) {
            this.parent = parent;
            this.primaryMap = primaryMap;
            this.otherMap = otherMap;
        }

        @Override
        public int size() {
            return parent.entryCount;
        }

        @Override
        protected int uniqueElements() {
            return primaryMap.size();
        }

        @Override
        public boolean isEmpty() {
            return parent.entryCount == 0;
        }

        @Override
        public void clear() {
            parent.clear();
        }

        @Override
        public int getCount(final Object object) {
            final SlotSet<?> slot = primaryMap.get(checkParam(object));
            if (slot != null) {
                return slot.size();
            } else {
                return 0;
            }
        }

        @Override
        protected Set<S> createUniqueSet() {
            return new SetView<>(parent, primaryMap, otherMap);
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean removeAll(final Collection<?> coll) {
            return parent.removeAllGeneric((Collection<S>) coll, primaryMap, otherMap);
        }

        @Override
        protected Iterator<Entry<S>> createEntrySetIterator() {
            return new TransformIterator<>(primaryMap.entrySet().iterator(),
                    e -> new BiMultiSetEntry<>(e.getKey(), e.getValue().size()));
        }

        @Override
        protected Spliterator<Entry<S>> createEntrySetSpliterator() {
            return new TransformSpliterator<>(primaryMap.entrySet().spliterator(),
                    e -> new BiMultiSetEntry<>(e.getKey(), e.getValue().size()));
        }

        @Override
        public boolean contains(final Object object) {
            return primaryMap.containsKey(object);
        }

        private static final class BiMultiSetEntry<E> extends AbstractMultiSet.AbstractEntry<E> {
            private final E element;
            private final int count;

            private BiMultiSetEntry(final E element, final int count) {
                this.element = element;
                this.count = count;
            }

            @Override
            public E getElement() {
                return element;
            }

            @Override
            public int getCount() {
                return count;
            }
        }
    }

    private static final class EntrySetView<K, V> extends AbstractSet<Map.Entry<K, V>> {
        private final DualHashBiMultiMap<K, V> parent;

        private EntrySetView(final DualHashBiMultiMap<K, V> parent) {
            this.parent = parent;
        }

        @Override
        public int size() {
            return parent.entryCount;
        }

        @Override
        public boolean isEmpty() {
            return parent.entryCount != 0;
        }

        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return new EntryIterator<>(parent);
        }

        @Override
        public void clear() {
            parent.clear();
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean contains(final Object obj) {
            if (obj instanceof Map.Entry) {
                final Map.Entry<K, V> entry = (Map.Entry<K, V>) obj;
                return parent.containsMapping(entry.getKey(), entry.getValue());
            }
            return false;
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean remove(final Object obj) {
            if (obj instanceof Map.Entry) {
                final Map.Entry<K, V> entry = (Map.Entry<K, V>) obj;
                return parent.removeMapping(entry.getKey(), entry.getValue());
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
            return EntrySetUtil.toArrayUnmodifiable(parent.mapIteratorEntries(), size());
        }

        @Override
        public <T> T[] toArray(final T[] array) {
            return EntrySetUtil.toArrayUnmodifiable(parent.mapIteratorEntries(), size(), array);
        }
    }

    protected abstract static class AbstractDistinctEntryIterator<K, V> {
        private final DualHashBiMultiMap<K, V> parent;
        private Iterator<Map.Entry<K, SlotSet<V>>> slotIterator;
        private Iterator<V> itemIterator;
        private boolean shouldDeleteKeySlot;
        protected boolean validEntry;
        protected K key;
        protected V value;

        protected AbstractDistinctEntryIterator(final DualHashBiMultiMap<K, V> parent) {
            this.parent = parent;
            reset();
        }

        public void reset() {
            slotIterator = parent.keyMap.entrySet().iterator();
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
            fixupPostRemove(parent.valueMap, key, value);
            validEntry = false;
        }
    }

    protected static class EntryIterator<K, V> extends AbstractDistinctEntryIterator<K, V> implements ResettableIterator<Map.Entry<K, V>> {
        protected EntryIterator(final DualHashBiMultiMap<K, V> parent) {
            super(parent);
        }

        @Override
        public Map.Entry<K, V> next() throws NoSuchElementException {
            nextItem();
            return new UnmodifiableMapEntry<>(key, value);
        }
    }

    protected static class EntryMapIterator<K, V> extends AbstractDistinctEntryIterator<K, V> implements MapIterator<K, V>, ResettableIterator<K> {
        protected EntryMapIterator(final DualHashBiMultiMap<K, V> parent) {
            super(parent);
        }

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

    private static class EntryMapSpliterator<K, V> implements MapSpliterator<K, V> {
//        private final DualHashBiMultiMap<K, V> parent;
        private final Spliterator<Map.Entry<K, SlotSet<V>>> mainSpliterator;
        private Iterator<V> itemIterator;
        private long estimateSize;
        private K key;

        private EntryMapSpliterator(final DualHashBiMultiMap<K, V> parent) {
//            this.parent = parent;
            mainSpliterator = parent.keyMap.entrySet().spliterator();
            estimateSize = parent.entryCount;
        }

        private EntryMapSpliterator(final Spliterator<Map.Entry<K, SlotSet<V>>> split, final long size) {
            mainSpliterator = split;
            estimateSize = size;
        }

        @Override
        public boolean tryAdvance(final BiConsumer<? super K, ? super V> action) {
            if ((itemIterator != null && itemIterator.hasNext()) || mainSpliterator.tryAdvance(this::advanceSlot)) {
                final V value = itemIterator.next();
                action.accept(key, value);
                return true;
            }
            return false;
        }

        private void advanceSlot(final Map.Entry<K, SlotSet<V>> entry) {
            key = entry.getKey();
            itemIterator = entry.getValue().iterator();
        }

        @Override
        public MapSpliterator<K, V> trySplit() {
            final Spliterator<Map.Entry<K, SlotSet<V>>> split = mainSpliterator.trySplit();
            if (split != null) {
                return new EntryMapSpliterator<>(split, estimateSize >>>= 1L);
            } else {
                return null;
            }
        }

        @Override
        public long estimateSize() {
            return estimateSize;
        }

        @Override
        public int characteristics() {
            return mainSpliterator.characteristics();
        }
    }

    private static final class MapIteratorForSets<S, O> implements MapIterator<S, Set<O>>, ResettableIterator<S> {
        private final DualHashBiMultiMap<?, ?> parent;
        private final Map<S, SlotSet<O>> primaryMap;
        private final Map<O, SlotSet<S>> otherMap;
        private Iterator<Map.Entry<S, SlotSet<O>>> entryIterator;
        private Map.Entry<S, SlotSet<O>> entry;

        private MapIteratorForSets(final DualHashBiMultiMap<?, ?> parent, final Map<S, SlotSet<O>> primaryMap, final Map<O, SlotSet<S>> otherMap) {
            this.parent = parent;
            this.primaryMap = primaryMap;
            this.otherMap = otherMap;
        }

        @Override
        public void reset() {
            entryIterator = primaryMap.entrySet().iterator();
        }

        @Override
        public boolean hasNext() {
            return entryIterator.hasNext();
        }

        @Override
        public S next() {
            entry = entryIterator.next();
            return entry.getKey();
        }

        @Override
        public S getKey() {
            return entry.getKey();
        }

        @Override
        public Set<O> getValue() {
            return entry.getValue().wrapped();
        }

        @Override
        public void remove() {
            entryIterator.remove();
            parent.fixupPostRemove(otherMap, entry.getKey(), entry.getValue());
            entry = null;
        }

        @Override
        public Set<O> setValue(final Set<O> value) {
            throw new UnsupportedOperationException();
        }
    }
}
