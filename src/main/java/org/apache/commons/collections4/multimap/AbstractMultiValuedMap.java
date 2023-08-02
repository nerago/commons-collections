/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.collections4.multimap;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.apache.commons.collections4.*;
import org.apache.commons.collections4.iterators.*;
import org.apache.commons.collections4.keyvalue.UnmodifiableMapEntry;
import org.apache.commons.collections4.multiset.AbstractMultiSet;
import org.apache.commons.collections4.multiset.UnmodifiableMultiSet;
import org.apache.commons.collections4.set.UnmodifiableSet;
import org.apache.commons.collections4.spliterators.EmptyMapSpliterator;
import org.apache.commons.collections4.spliterators.MapSpliterator;
import org.apache.commons.collections4.spliterators.TransformSpliterator;

/**
 * Abstract implementation of the {@link MultiValuedMap} interface to simplify
 * the creation of subclass implementations.
 * <p>
 * Subclasses specify a Map implementation to use as the internal storage.
 * </p>
 *
 * @param <K> the type of the keys in this map
 * @param <V> the type of the values in this map
 * @since 4.1
 */
public abstract class AbstractMultiValuedMap<K, V> implements MultiValuedMap<K, V> {

    /** The values view */
    private transient Collection<V> valuesView;

    /** The EntryValues view */
    private transient EntriesView entriesView;

    /** The KeyMultiSet view */
    private transient MultiSet<K> keysMultiSetView;

    /** The AsMap view */
    private transient AsMap asMapView;

    /** The map used to store the data */
    private transient Map<K, Collection<V>> map;

    private transient int entryCount;

    /**
     * Constructor needed for subclass serialisation.
     */
    protected AbstractMultiValuedMap() {
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param map  the map to wrap, must not be null
     * @throws NullPointerException if the map is null
     */
    @SuppressWarnings("unchecked")
    protected AbstractMultiValuedMap(final Map<K, ? extends Collection<V>> map) {
        this.map = (Map<K, Collection<V>>) Objects.requireNonNull(map, "map");
    }

    /**
     * Gets the map being wrapped.
     *
     * @return the wrapped map
     */
    protected Map<K, ? extends Collection<V>> getMap() {
        return map;
    }

    /**
     * Sets the map being wrapped.
     * <p>
     * <b>NOTE:</b> this method should only be used during deserialization
     *
     * @param map the map to wrap
     */
    @SuppressWarnings("unchecked")
    protected void setMap(final Map<K, ? extends Collection<V>> map) {
        this.map = (Map<K, Collection<V>>) map;
    }

    protected abstract Collection<V> createCollection();

    protected void updateEntryCount(int delta) {
        entryCount += delta;
    }

    @Override
    public boolean containsKey(final Object key) {
        return getMap().containsKey(key);
    }

    @Override
    public boolean containsValue(final Object value) {
        for (Collection<V> coll : getMap().values()) {
            if (coll.contains(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsMapping(final Object key, final Object value) {
        final Collection<V> coll = getMap().get(key);
        return coll != null && coll.contains(value);
    }

    @Override
    public Collection<Entry<K, V>> entries() {
        return entriesView != null ? entriesView : (entriesView = new EntriesView());
    }

    /**
     * Gets the collection of values associated with the specified key. This
     * would return an empty collection in case the mapping is not present
     *
     * @param key the key to retrieve
     * @return the {@code Collection} of values, will return an empty {@code Collection} for no mapping
     */
    @Override
    public Collection<V> get(final K key) {
        return wrappedCollection(key);
    }

    Collection<V> wrappedCollection(final K key) {
        return new WrappedCollection(key);
    }

    /**
     * Removes all values associated with the specified key.
     * <p>
     * A subsequent {@code get(Object)} would return an empty collection.
     *
     * @param key  the key to remove values from
     * @return the {@code Collection} of values removed, will return an
     *   empty, unmodifiable collection for no mapping found
     */
    @Override
    public Collection<V> remove(final Object key) {
        Collection<V> removed = CollectionUtils.emptyIfNull(getMap().remove(key));
        updateEntryCount(-removed.size());
        return removed;
    }

    /**
     * Removes a specific key/value mapping from the multivalued map.
     * <p>
     * The value is removed from the collection mapped to the specified key.
     * Other values attached to that key are unaffected.
     * <p>
     * If the last value for a key is removed, an empty collection would be
     * returned from a subsequent {@link #get(Object)}.
     *
     * @param key the key to remove from
     * @param value the value to remove
     * @return true if the mapping was removed, false otherwise
     */
    @Override
    public boolean removeMapping(final Object key, final Object value) {
        final Collection<V> coll = getMap().get(key);
        if (coll == null) {
            return false;
        }
        final boolean changed = coll.remove(value);
        if (coll.isEmpty()) {
            getMap().remove(key);
        }
        if (changed) {
            updateEntryCount(-1);
        }
        return changed;
    }

    @Override
    public boolean isEmpty() {
        return getMap().isEmpty();
    }

    @Override
    public Set<K> keySet() {
        return getMap().keySet();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation does <b>not</b> cache the total size
     * of the multivalued map, but rather calculates it by iterating
     * over the entries of the underlying map.
     */
    @Override
    public int size() {
        return entryCount;
    }

    /**
     * Gets a collection containing all the values in the map.
     * <p>
     * Returns a collection containing all the values from all keys.
     *
     * @return a collection view of the values contained in this map
     */
    @Override
    public Collection<V> values() {
        final Collection<V> vs = valuesView;
        return vs != null ? vs : (valuesView = new Values());
    }

    @Override
    public void clear() {
        getMap().clear();
        entryCount = 0;
    }

    /**
     * Adds the value to the collection associated with the specified key.
     * <p>
     * Unlike a normal {@code Map} the previous value is not replaced.
     * Instead the new value is added to the collection stored against the key.
     *
     * @param key the key to store against
     * @param value the value to add to the collection at the key
     * @return true if the map changed as a result of this operation
     */
    @Override
    public boolean put(final K key, final V value) {
        Collection<V> coll = getMap().get(key);
        if (coll == null) {
            coll = createCollection();
            if (coll.add(value)) {
                updateEntryCount(+1);
                map.put(key, coll);
                return true;
            }
            return false;
        }
        final boolean changed = coll.add(value);
        if (changed) {
            updateEntryCount(+1);
        }
        return changed;
    }

    /**
     * Copies all of the mappings from the specified map to this map. The effect
     * of this call is equivalent to that of calling {@link #put(Object,Object)
     * put(k, v)} on this map once for each mapping from key {@code k} to value
     * {@code v} in the specified map. The behavior of this operation is
     * undefined if the specified map is modified while the operation is in
     * progress.
     *
     * @param map mappings to be stored in this map, may not be null
     * @return true if the map changed as a result of this operation
     * @throws NullPointerException if map is null
     */
    @Override
    public boolean putAll(final Map<? extends K, ? extends V> map) {
        Objects.requireNonNull(map, "map");
        boolean changed = false;
        if (map instanceof IterableMap) {
            MapIterator<? extends K, ? extends V> mapIterator =
                    ((IterableMap<? extends K, ? extends V>) map).mapIterator();
            while (mapIterator.hasNext()) {
                K key = mapIterator.next();
                V value = mapIterator.getValue();
                changed |= put(key, value);
            }
        } else {
            for (final Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
                changed |= put(entry.getKey(), entry.getValue());
            }
        }
        return changed;
    }

    /**
     * Copies all of the mappings from the specified MultiValuedMap to this map.
     * The effect of this call is equivalent to that of calling
     * {@link #put(Object,Object) put(k, v)} on this map once for each mapping
     * from key {@code k} to value {@code v} in the specified map. The
     * behavior of this operation is undefined if the specified map is modified
     * while the operation is in progress.
     *
     * @param map mappings to be stored in this map, may not be null
     * @return true if the map changed as a result of this operation
     * @throws NullPointerException if map is null
     */
    @Override
    public boolean putAll(final MultiValuedMap<? extends K, ? extends V> map) {
        Objects.requireNonNull(map, "map");
        boolean changed = false;
        for (Entry<? extends K, ? extends Collection<? extends V>> entry : map.asMap().entrySet()) {
            changed |= putAll(entry.getKey(), entry.getValue());
        }
        return changed;
    }

    /**
     * Returns a {@link MultiSet} view of the key mapping contained in this map.
     * <p>
     * Returns a MultiSet of keys with its values count as the count of the MultiSet.
     * This multiset is backed by the map, so any changes in the map is reflected here.
     * Any method which modifies this multiset like {@code add}, {@code remove},
     * {@link Iterator#remove()} etc throws {@code UnsupportedOperationException}.
     *
     * @return a bag view of the key mapping contained in this map
     */
    @Override
    public MultiSet<K> keys() {
        if (keysMultiSetView == null) {
            keysMultiSetView = UnmodifiableMultiSet.unmodifiableMultiSet(new KeysMultiSet<>(this, map));
        }
        return keysMultiSetView;
    }

    @Override
    public Map<K, Collection<V>> asMap() {
        return asMapView != null ? asMapView : (asMapView = new AsMap(map));
    }

    /**
     * Adds Iterable values to the collection associated with the specified key.
     *
     * @param key the key to store against
     * @param values the values to add to the collection at the key, may not be null
     * @return true if this map changed
     * @throws NullPointerException if values is null
     */
    @Override
    public boolean putAll(final K key, final Iterable<? extends V> values) {
        Objects.requireNonNull(values, "values");

        if (values instanceof Collection<?>) {
            final Collection<? extends V> valueCollection = (Collection<? extends V>) values;
            return !valueCollection.isEmpty() && get(key).addAll(valueCollection);
        }

        final Iterator<? extends V> it = values.iterator();
        if (!it.hasNext())
            return false;
        int addCount = CollectionUtils.addAllCounted(get(key), it);
        updateEntryCount(addCount);
        return addCount > 0;
    }

    @Override
    public MapIterator<K, V> mapIterator() {
        if (isEmpty()) {
            return EmptyMapIterator.emptyMapIterator();
        }
        return new FullMapIterator<>(this);
    }

    @Override
    public MapSpliterator<K, V> mapSpliterator() {
        if (isEmpty()) {
            return EmptyMapSpliterator.emptyMapSpliterator();
        }
        return new FullMapSpliterator<>(this);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof MultiValuedMap) {
            return asMap().equals(((MultiValuedMap<?, ?>) obj).asMap());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getMap().hashCode();
    }

    @Override
    public String toString() {
        return getMap().toString();
    }

    /**
     * Wrapped collection to handle add and remove on the collection returned
     * by get(object).
     * <p>
     * Currently, the wrapped collection is not cached and has to be retrieved
     * from the underlying map. This is safe, but not very efficient and
     * should be improved in subsequent releases. For this purpose, the
     * scope of this collection is set to package private to simplify later
     * refactoring.
     */
    class WrappedCollection implements Collection<V> {

        protected final K key;

        WrappedCollection(final K key) {
            this.key = key;
        }

        protected Collection<V> getMapping() {
            return getMap().get(key);
        }

        @Override
        public boolean add(final V value) {
            Collection<V> coll = getMapping();
            if (coll == null) {
                coll = createCollection();
                map.put(key, coll);
            }
            boolean changed = coll.add(value);
            if (changed)
                updateEntryCount(+1);
            return changed;
        }

        @Override
        public boolean addAll(final Collection<? extends V> other) {
            Collection<V> coll = getMapping();
            if (coll == null) {
                coll = createCollection();
                map.put(key, coll);
            }
            final int oldSize = coll.size();
            if (coll.addAll(other)) {
                updateEntryCount(coll.size() - oldSize);
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void clear() {
            final Collection<V> coll = getMapping();
            if (coll != null) {
                coll.clear();
                getMap().remove(key);
                updateEntryCount(-coll.size());
            }
        }

        @Override
        public Iterator<V> iterator() {
            final Collection<V> coll = getMapping();
            if (coll == null) {
                return IteratorUtils.emptyIterator();
            }
            return new CollectionValuesIterator(key, coll);
        }

        @Override
        public Spliterator<V> spliterator() {
            final Collection<V> coll = getMapping();
            if (coll == null) {
                return Spliterators.emptySpliterator();
            }
            return coll.spliterator();
        }

        @Override
        public int size() {
            final Collection<V> coll = getMapping();
            return coll == null ? 0 : coll.size();
        }

        @Override
        public boolean contains(final Object obj) {
            final Collection<V> coll = getMapping();
            return coll != null && coll.contains(obj);
        }

        @Override
        public boolean containsAll(final Collection<?> other) {
            final Collection<V> coll = getMapping();
            return coll != null && coll.containsAll(other);
        }

        @Override
        public boolean isEmpty() {
            final Collection<V> coll = getMapping();
            return coll == null || coll.isEmpty();
        }

        @Override
        public boolean remove(final Object item) {
            final Collection<V> coll = getMapping();
            if (coll == null) {
                return false;
            }

            final boolean result = coll.remove(item);
            if (coll.isEmpty()) {
                getMap().remove(key);
            }
            if (result) {
                updateEntryCount(-1);
            }
            return result;
        }

        @Override
        public boolean removeAll(final Collection<?> c) {
            final Collection<V> coll = getMapping();
            if (coll == null) {
                return false;
            }

            final int oldSize = coll.size();
            final boolean result = coll.removeAll(c);
            if (coll.isEmpty()) {
                getMap().remove(key);
            }
            if (result) {
                updateEntryCount(coll.size() - oldSize);
            }
            return result;
        }

        @Override
        public boolean retainAll(final Collection<?> c) {
            final Collection<V> coll = getMapping();
            if (coll == null) {
                return false;
            }

            final int oldSize = coll.size();
            final boolean result = coll.retainAll(c);
            if (coll.isEmpty()) {
                getMap().remove(key);
            }
            if (result) {
                updateEntryCount(coll.size() - oldSize);;
            }
            return result;
        }

        @Override
        public Object[] toArray() {
            final Collection<V> coll = getMapping();
            if (coll == null) {
                return CollectionUtils.EMPTY_COLLECTION.toArray();
            }
            return coll.toArray();
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T[] toArray(final T[] a) {
            final Collection<V> coll = getMapping();
            if (coll == null) {
                return (T[]) CollectionUtils.EMPTY_COLLECTION.toArray(a);
            }
            return coll.toArray(a);
        }

        @Override
        public String toString() {
            final Collection<V> coll = getMapping();
            if (coll == null) {
                return CollectionUtils.EMPTY_COLLECTION.toString();
            }
            return coll.toString();
        }

    }

    /**
     * Inner class that provides a MultiSet<K> keys view.
     * Only used with unmodifiable wrapper.
     */
    private static class KeysMultiSet<K, V> extends AbstractMultiSet<K> {
        private AbstractMultiValuedMap<K, V> parent;
        private Map<K, Collection<V>> map;

        protected KeysMultiSet(final AbstractMultiValuedMap<K, V> parent, final Map<K, Collection<V>> map) {
            this.parent = parent;
            this.map = map;
        }

        @Override
        public boolean contains(final Object o) {
            return map.containsKey(o);
        }

        @Override
        public Iterator<K> iterator() {
            return new MultiSetIterator<>(map);
        }

        @Override
        public Spliterator<K> spliterator() {
            return new MultiSetSpliterator<>(map, parent.size());
        }

        @Override
        protected Set<K> createUniqueSet() {
            return UnmodifiableSet.unmodifiableSet(map.keySet());
        }

        @Override
        protected Iterator<K> createUniqueSetIterator() {
            return map.keySet().iterator();
        }

        @Override
        protected Spliterator<K> createUniqueSetSpliterator() {
            return map.keySet().spliterator();
        }

        @Override
        public boolean isEmpty() {
            return map.isEmpty();
        }

        @Override
        public int size() {
            return parent.size();
        }

        @Override
        protected int uniqueElements() {
            return map.size();
        }

        @Override
        public int getCount(final Object object) {
            int count = 0;
            final Collection<V> col = map.get(object);
            if (col != null) {
                count = col.size();
            }
            return count;
        }

        @Override
        protected Iterator<MultiSet.Entry<K>> createEntrySetIterator() {
            return IteratorUtils.transformedIterator(map.entrySet().iterator(), MultiSetEntry::new);
        }

        @Override
        protected Spliterator<Entry<K>> createEntrySetSpliterator() {
            return SpliteratorUtils.transformedSpliterator(map.entrySet().spliterator(), MultiSetEntry::new);
        }

        @Override
        protected Set<Entry<K>> createEntrySet() {
            return UnmodifiableSet.unmodifiableSet(super.createEntrySet());
        }

        private class MultiSetEntry extends AbstractMultiSet.AbstractEntry<K> {
            private final Map.Entry<K, Collection<V>> mapEntry;

            public MultiSetEntry(Map.Entry<K, Collection<V>> mapEntry) {
                this.mapEntry = mapEntry;
            }

            @Override
            public K getElement() {
                return mapEntry.getKey();
            }

            @Override
            public int getCount() {
                return mapEntry.getValue().size();
            }
        }
    }


    /**
     * Inner class iterator for the MultiSet view.
     */
    private static class MultiSetIterator<K, V> implements Iterator<K>, Unmodifiable {
        private final Iterator<Entry<K, Collection<V>>> entryIterator;
        private Entry<K, Collection<V>> current;
        private int itemCount;

        /**
         * Constructor.
         *
         * @param map the parent's internal map
         */
        MultiSetIterator(final Map<K, Collection<V>> map) {
            this.entryIterator = map.entrySet().iterator();
            this.current = null;
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasNext() {
            return itemCount > 0 || entryIterator.hasNext();
        }

        /** {@inheritDoc} */
        @Override
        public K next() {
            if (itemCount == 0) {
                current = entryIterator.next();
                itemCount = current.getValue().size();
            }
            itemCount--;
            return current.getKey();
        }
    }

    private static abstract class BaseEntrySpliterator<E, K, V> implements Spliterator<E> {
        protected final Spliterator<Entry<K, Collection<V>>> entrySpliterator;
        protected int estimateSize;
        protected boolean isSplit;

        protected BaseEntrySpliterator(final Map<K, Collection<V>> map, final int exactSize) {
            this.entrySpliterator = map.entrySet().spliterator();
            this.estimateSize = exactSize;
        }

        protected BaseEntrySpliterator(final Spliterator<Entry<K, Collection<V>>> entrySpliterator, final int estimateSize) {
            this.entrySpliterator = entrySpliterator;
            this.estimateSize = estimateSize;
            this.isSplit = true;
        }

        @Override
        public Spliterator<E> trySplit() {
            Spliterator<Entry<K, Collection<V>>> partitionedEntries = entrySpliterator.trySplit();
            if (partitionedEntries != null) {
                isSplit = true;
                return makeSplit(partitionedEntries, estimateSize >>>= 1);
            } else {
                return null;
            }
        }

        protected abstract Spliterator<E> makeSplit(Spliterator<Entry<K, Collection<V>>> entrySpliterator, int estimateSize);

        @Override
        public long estimateSize() {
            return estimateSize;
        }

        @Override
        public int characteristics() {
            return isSplit ? 0 : Spliterator.SIZED;
        }
    }

    private static class MultiSetSpliterator<K, V> extends BaseEntrySpliterator<K, K, V> {
        private K currentKey;
        private int itemCount;

        protected MultiSetSpliterator(final Map<K, Collection<V>> map, final int exactSize) {
            super(map, exactSize);
        }

        protected MultiSetSpliterator(final Spliterator<Entry<K, Collection<V>>> entrySpliterator, final int estimateSize) {
            super(entrySpliterator, estimateSize);
        }

        @Override
        protected Spliterator<K> makeSplit(Spliterator<Entry<K, Collection<V>>> entrySpliterator, int estimateSize) {
            return new MultiSetSpliterator<>(entrySpliterator, estimateSize);
        }

        @Override
        public boolean tryAdvance(final Consumer<? super K> action) {
            if (itemCount == 0 && !entrySpliterator.tryAdvance(this::handleNextEntry)) {
                return false;
            }

            action.accept(currentKey);
            itemCount--;
            return true;
        }

        private void handleNextEntry(final Entry<K, Collection<V>> entry) {
            currentKey = entry.getKey();
            itemCount = entry.getValue().size();
        }
    }


    /**
     * Inner class that provides the Entry<K, V> view
     */
    private class EntriesView extends AbstractCollection<Entry<K, V>> {
        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new FullEntriesIterator<>(AbstractMultiValuedMap.this);
        }

        @Override
        public Spliterator<Entry<K, V>> spliterator() {
            return new FullMapSpliterator<>(AbstractMultiValuedMap.this);
        }

        @Override
        public int size() {
            return AbstractMultiValuedMap.this.size();
        }

        @Override
        public void clear() {
            AbstractMultiValuedMap.this.clear();
        }

        @Override
        public boolean contains(Object obj) {
            if (obj instanceof Entry) {
                Entry<?, ?> entry = (Entry<?, ?>) obj;
                return containsMapping(entry.getKey(), entry.getValue());
            } else {
                return false;
            }
        }

        @Override
        public boolean remove(Object obj) {
            if (obj instanceof Entry) {
                Entry<?, ?> entry = (Entry<?, ?>) obj;
                return removeMapping(entry.getKey(), entry.getValue());
            } else {
                return false;
            }
        }
    }

    private static class BaseFullEntriesIterator<K, V> {
        private final AbstractMultiValuedMap<K, V> parent;
        private Iterator<Entry<K, Collection<V>>> entryIterator;
        protected K currentKey;
        private Collection<V> currentCollection;
        private Iterator<V> collectionIterator;
        protected V currentValue;
        protected boolean haveCurrent = false;

        BaseFullEntriesIterator(final AbstractMultiValuedMap<K, V> parent) {
            this.parent = parent;
            this.entryIterator = parent.map.entrySet().iterator();
        }

        public void reset() {
            entryIterator = parent.map.entrySet().iterator();
            currentKey = null;
            currentCollection = null;
            collectionIterator = null;
            currentValue = null;
            haveCurrent = false;
        }

        public boolean hasNext() {
            return (collectionIterator != null && collectionIterator.hasNext()) || entryIterator.hasNext();
        }

        public boolean nextEntry() {
            if (collectionIterator != null && collectionIterator.hasNext()) {
                currentValue = collectionIterator.next();
                haveCurrent = true;
            } else if (entryIterator.hasNext()) {
                Entry<K, Collection<V>> entry = entryIterator.next();
                currentKey = entry.getKey();
                currentCollection = entry.getValue();
                collectionIterator = currentCollection.iterator();
                currentValue = collectionIterator.next();
                haveCurrent = true;
            } else {
                haveCurrent = false;
            }
            return haveCurrent;
        }

        public void remove() {
            if (!haveCurrent) {
                throw new IllegalStateException();
            }
            collectionIterator.remove();
            haveCurrent = false;
            parent.updateEntryCount(-1);
            if (currentCollection.isEmpty()) {
                entryIterator.remove();
                collectionIterator = null;
            }
        }

        public V setValue(V value) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Inner class for MapIterator.
     */
    private static class FullMapIterator<K, V> extends BaseFullEntriesIterator<K, V> implements MapIterator<K, V>, ResettableIterator<K> {
        FullMapIterator(AbstractMultiValuedMap<K, V> parent) {
            super(parent);
        }

        @Override
        public K next() {
            if (nextEntry())
                return currentKey;
            else
                throw new NoSuchElementException();
        }

        @Override
        public K getKey() {
            if (!haveCurrent) {
                throw new IllegalStateException();
            }
            return currentKey;
        }

        @Override
        public V getValue() {
            if (!haveCurrent) {
                throw new IllegalStateException();
            }
            return currentValue;
        }
    }

    private static class FullEntriesIterator<K, V> extends BaseFullEntriesIterator<K, V> implements Iterator<Entry<K, V>> {
        FullEntriesIterator(AbstractMultiValuedMap<K, V> parent) {
            super(parent);
        }

        @Override
        public Entry<K, V> next() {
            if (nextEntry())
                return new UnmodifiableMapEntry<>(currentKey, currentValue);
            else
                throw new NoSuchElementException();
        }
    }

    private static class FullValuesIterator<K, V> extends BaseFullEntriesIterator<K, V> implements Iterator<V> {
        FullValuesIterator(AbstractMultiValuedMap<K, V> parent) {
            super(parent);
        }

        @Override
        public V next() {
            if (nextEntry())
                return currentValue;
            else
                throw new NoSuchElementException();
        }
    }

    private static class FullMapSpliterator<K, V> extends BaseEntrySpliterator<Entry<K, V>, K, V> implements MapSpliterator<K, V> {
        private K currentKey;
        private Iterator<V> collectionIterator;

        protected FullMapSpliterator(final AbstractMultiValuedMap<K, V> parent) {
            super(parent.map, parent.entryCount);
        }

        protected FullMapSpliterator(final Spliterator<Entry<K, Collection<V>>> entrySpliterator, final int estimateSize) {
            super(entrySpliterator, estimateSize);
        }

        @Override
        protected Spliterator<Entry<K, V>> makeSplit(final Spliterator<Entry<K, Collection<V>>> entrySpliterator, final int estimateSize) {
            return new FullMapSpliterator<>(entrySpliterator, estimateSize);
        }

        @Override
        public MapSpliterator<K, V> trySplit() {
            return (MapSpliterator<K, V>) super.trySplit();
        }

        @Override
        public boolean tryAdvance(final BiConsumer<? super K, ? super V> action) {
            if ((collectionIterator != null && collectionIterator.hasNext())
                    || entrySpliterator.tryAdvance(this::handleNextEntry)) {
                V value = collectionIterator.next();
                action.accept(currentKey, value);
                return true;
            } else {
                return false;
            }
        }

        private void handleNextEntry(final Entry<K, Collection<V>> entry) {
            currentKey = entry.getKey();
            collectionIterator = entry.getValue().iterator();
        }
    }

    private static class FullValuesSpliterator<K, V> extends BaseEntrySpliterator<V, K, V> {
        private Iterator<V> collectionIterator;

        protected FullValuesSpliterator(final AbstractMultiValuedMap<K, V> parent) {
            super(parent.map, parent.entryCount);
        }

        protected FullValuesSpliterator(final Spliterator<Entry<K, Collection<V>>> entrySpliterator, final int estimateSize) {
            super(entrySpliterator, estimateSize);
        }

        @Override
        protected Spliterator<V> makeSplit(final Spliterator<Entry<K, Collection<V>>> entrySpliterator, final int estimateSize) {
            return new FullValuesSpliterator<>(entrySpliterator, estimateSize);
        }

        @Override
        public boolean tryAdvance(final Consumer<? super V> action) {
            if ((collectionIterator != null && collectionIterator.hasNext())
                    || entrySpliterator.tryAdvance(this::handleNextEntry)) {
                action.accept(collectionIterator.next());
                return true;
            } else {
                return false;
            }
        }

        private void handleNextEntry(final Entry<K, Collection<V>> entry) {
            collectionIterator = entry.getValue().iterator();
        }
    }

    /**
     * Inner class that provides the values view.
     */
    private class Values extends AbstractCollection<V> {
        @Override
        public Iterator<V> iterator() {
            return new FullValuesIterator<>(AbstractMultiValuedMap.this);
        }

        @Override
        public Spliterator<V> spliterator() {
            return new FullValuesSpliterator<>(AbstractMultiValuedMap.this);
        }

        @Override
        public int size() {
            return AbstractMultiValuedMap.this.size();
        }

        @Override
        public void clear() {
            AbstractMultiValuedMap.this.clear();
        }

        @Override
        public boolean contains(Object obj) {
            return containsValue(obj);
        }

        @Override
        public boolean remove(Object obj) {
            final Iterator<Collection<V>> iterator = map.values().iterator();
            while (iterator.hasNext()) {
                final Collection<V> coll = iterator.next();
                if (coll.remove(obj)) {
                    updateEntryCount(-1);
                    if (coll.isEmpty()) {
                        iterator.remove();
                    }
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean removeAll(Collection<?> removeCollection) {
            boolean changed = false;
            final Iterator<Collection<V>> iterator = map.values().iterator();
            while (iterator.hasNext()) {
                final Collection<V> coll = iterator.next();
                final int oldSize = coll.size();
                if (coll.removeAll(removeCollection)) {
                    updateEntryCount(coll.size() - oldSize);;
                    if (coll.isEmpty()) {
                        iterator.remove();
                    }
                    changed = true;
                }
            }
            return changed;
        }

        @Override
        public boolean retainAll(Collection<?> retainCollection) {
            boolean changed = false;
            final Iterator<Collection<V>> iterator = map.values().iterator();
            while (iterator.hasNext()) {
                final Collection<V> coll = iterator.next();
                final int oldSize = coll.size();
                if (coll.retainAll(retainCollection)) {
                    updateEntryCount(coll.size() - oldSize);;
                    if (coll.isEmpty()) {
                        iterator.remove();
                    }
                    changed = true;
                }
            }
            return changed;
        }
    }

    /**
     * Inner class that provides the values iterator.
     * This wraps the collection's iterator to do remove updates.
     */
    private class CollectionValuesIterator extends AbstractIteratorDecorator<V> {
        private final Object key;
        private final Collection<V> values;

        CollectionValuesIterator(final Object key, Collection<V> values) {
            super(values.iterator());
            this.key = key;
            this.values = values;
        }

        @Override
        public void remove() {
            getIterator().remove();
            updateEntryCount(-1);
            if (values.isEmpty()) {
                AbstractMultiValuedMap.this.remove(key);
            }
        }
    }

    /**
     * Inner class that provides the AsMap view.
     */
    private class AsMap extends AbstractMap<K, Collection<V>> {
        final transient Map<K, Collection<V>> decoratedMap;

        AsMap(final Map<K, Collection<V>> map) {
            this.decoratedMap = map;
        }

        @Override
        public Set<Map.Entry<K, Collection<V>>> entrySet() {
            return new AsMapEntrySet();
        }

        @Override
        public boolean containsKey(final Object key) {
            return decoratedMap.containsKey(key);
        }

        @Override
        public Collection<V> get(final Object key) {
            final Collection<V> collection = decoratedMap.get(key);
            if (collection == null) {
                return null;
            }
            @SuppressWarnings("unchecked")
            final K k = (K) key;
            return wrappedCollection(k);
        }

        @Override
        public Set<K> keySet() {
            return AbstractMultiValuedMap.this.keySet();
        }

        @Override
        public Collection<Collection<V>> values() {
            return new AsMapValues();
        }

        @Override
        public int size() {
            return decoratedMap.size();
        }

        @Override
        public Collection<V> remove(final Object key) {
            final Collection<V> collection = decoratedMap.remove(key);
            if (collection == null) {
                return null;
            }

            updateEntryCount(-collection.size());

            final Collection<V> output = createCollection();
            output.addAll(collection);
            collection.clear();
            return output;
        }

        @Override
        public boolean equals(final Object object) {
            return this == object || decoratedMap.equals(object);
        }

        @Override
        public int hashCode() {
            return decoratedMap.hashCode();
        }

        @Override
        public String toString() {
            return decoratedMap.toString();
        }

        @Override
        public void clear() {
            AbstractMultiValuedMap.this.clear();
        }

        @Override
        public Collection<V> compute(K key, BiFunction<? super K, ? super Collection<V>, ? extends Collection<V>> remappingFunction) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<V> merge(K key, Collection<V> value, BiFunction<? super Collection<V>, ? super Collection<V>, ? extends Collection<V>> remappingFunction) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putAll(Map<? extends K, ? extends Collection<V>> m) {
            throw new UnsupportedOperationException();
        }

        class AsMapEntrySet extends AbstractSet<Map.Entry<K, Collection<V>>> {
            private Entry<K, Collection<V>> wrapEntry(final Entry<K, Collection<V>> entry) {
                final K key = entry.getKey();
                return new UnmodifiableMapEntry<>(key, wrappedCollection(key));
            }

            @Override
            public Iterator<Map.Entry<K, Collection<V>>> iterator() {
                return new TransformIterator<>(decoratedMap.entrySet().iterator(), this::wrapEntry);
            }

            @Override
            public Spliterator<Entry<K, Collection<V>>> spliterator() {
                return new TransformSpliterator<>(decoratedMap.entrySet().spliterator(), this::wrapEntry);
            }

            @Override
            public int size() {
                return AsMap.this.size();
            }

            @Override
            public void clear() {
                AsMap.this.clear();
            }

            @Override
            public boolean contains(final Object o) {
                return decoratedMap.entrySet().contains(o);
            }

            @Override
            public boolean remove(final Object o) {
                if (!contains(o)) {
                    return false;
                }
                final Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
                AbstractMultiValuedMap.this.remove(entry.getKey());
                return true;
            }
        }

        class AsMapValues extends AbstractCollection<Collection<V>> {
            @Override
            public Iterator<Collection<V>> iterator() {
                return new TransformIterator<>(decoratedMap.keySet().iterator(), AbstractMultiValuedMap.this::wrappedCollection);
            }

            @Override
            public Spliterator<Collection<V>> spliterator() {
                return new TransformSpliterator<>(decoratedMap.keySet().spliterator(), AbstractMultiValuedMap.this::wrappedCollection);
            }

            @Override
            public int size() {
                return AsMap.this.size();
            }

            @Override
            public void clear() {
                AsMap.this.clear();
            }

            @Override
            public boolean contains(final Object o) {
                return decoratedMap.containsValue(o);
            }

            @Override
            @SuppressWarnings("unchecked")
            public boolean remove(Object obj) {
                if (obj instanceof Collection) {
                    final Collection<V> coll = (Collection<V>) obj;
                    boolean changed = decoratedMap.values().remove(coll);
                    if (changed) {
                        updateEntryCount(-coll.size());
                    }
                    return changed;
                } else {
                    return false;
                }
            }
        }
    }

    /**
     * Write the map out using a custom routine.
     * @param out the output stream
     * @throws IOException any of the usual I/O related exceptions
     */
    protected void doWriteObject(final ObjectOutputStream out) throws IOException {
        out.writeInt(map.size());
        for (final Map.Entry<K, Collection<V>> entry : map.entrySet()) {
            out.writeObject(entry.getKey());
            out.writeInt(entry.getValue().size());
            for (final V value : entry.getValue()) {
                out.writeObject(value);
            }
        }
    }

    /**
     * Read the map in using a custom routine.
     * @param in the input stream
     * @throws IOException any of the usual I/O related exceptions
     * @throws ClassNotFoundException if the stream contains an object which class can not be loaded
     * @throws ClassCastException if the stream does not contain the correct objects
     */
    protected void doReadObject(final ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        final int entrySize = in.readInt();
        for (int i = 0; i < entrySize; i++) {
            @SuppressWarnings("unchecked") // This will fail at runtime if the stream is incorrect
            final K key = (K) in.readObject();
            final Collection<V> values = get(key);
            final int valueSize = in.readInt();
            for (int j = 0; j < valueSize; j++) {
                @SuppressWarnings("unchecked") // see above
                final V value = (V) in.readObject();
                values.add(value);
            }
        }
    }
}
