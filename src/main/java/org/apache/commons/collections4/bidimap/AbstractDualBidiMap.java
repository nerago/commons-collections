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
package org.apache.commons.collections4.bidimap;

import java.util.*;
import java.util.function.Predicate;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.ResettableIterator;
import org.apache.commons.collections4.collection.AbstractCollectionDecorator;
import org.apache.commons.collections4.iterators.AbstractIteratorDecorator;
import org.apache.commons.collections4.keyvalue.AbstractMapEntryDecorator;

/**
 * Abstract {@link BidiMap} implemented using two maps.
 * <p>
 * An implementation can be written simply by implementing the
 * {@link #createBidiMap(Map, Map, BidiMap)} method.
 * </p>
 *
 * @param <K> the type of the keys in the map
 * @param <V> the type of the values in the map
 *
 * @see DualHashBidiMap
 * @see DualTreeBidiMap
 * @since 3.0
 */
public abstract class AbstractDualBidiMap<K, V> implements BidiMap<K, V> {

    /**
     * Normal delegate map.
     */
    private transient Map<K, V> normalMap;

    /**
     * Reverse delegate map.
     */
    private transient Map<V, K> reverseMap;

    /**
     * Inverse view of this map.
     */
    private transient BidiMap<V, K> inverseBidiMap;

    /**
     * View of the keys.
     */
    private transient Set<K> keySet;

    /**
     * View of the values.
     */
    private transient Set<V> values;

    /**
     * View of the entries.
     */
    private transient Set<Map.Entry<K, V>> entrySet;

    /**
     * Creates an empty map, initialized by {@code createMap}.
     * <p>
     * This constructor remains in place for deserialization.
     * All other usage is deprecated in favour of
     * {@link #AbstractDualBidiMap(Map, Map)}.
     */
    protected AbstractDualBidiMap() {
    }

    /**
     * Creates an empty map using the two maps specified as storage.
     * <p>
     * The two maps must be a matching pair, normal and reverse.
     * They will typically both be empty.
     * <p>
     * Neither map is validated, so nulls may be passed in.
     * If you choose to do this then the subclass constructor must populate
     * the {@code maps[]} instance variable itself.
     *
     * @param normalMap  the normal direction map
     * @param reverseMap  the reverse direction map
     * @since 3.1
     */
    protected AbstractDualBidiMap(final Map<K, V> normalMap, final Map<V, K> reverseMap) {
        this.normalMap = normalMap;
        this.reverseMap = reverseMap;
    }

    /**
     * Constructs a map that decorates the specified maps,
     * used by the subclass {@code createBidiMap} implementation.
     *
     * @param normalMap  the normal direction map
     * @param reverseMap  the reverse direction map
     * @param inverseBidiMap  the inverse BidiMap
     */
    protected AbstractDualBidiMap(final Map<K, V> normalMap, final Map<V, K> reverseMap,
                                  final BidiMap<V, K> inverseBidiMap) {
        this.normalMap = normalMap;
        this.reverseMap = reverseMap;
        this.inverseBidiMap = inverseBidiMap;
    }

    /**
     * Creates a new instance of the subclass.
     *
     * @return the bidi map
     */
    protected abstract BidiMap<V, K> createInverse();

    /**
     * Sets the collection being decorated.
     * <p>
     * <b>NOTE:</b> this method should only be used during deserialization
     *
     * @param normalMap  the normal direction map
     * @param reverseMap  the reverse direction map
     */
    protected void setInternalMaps(Map<K, V> normalMap, Map<V, K> reverseMap) {
        this.normalMap = normalMap;
        this.reverseMap = reverseMap;
    }

    protected Map<K, V> normalMap() {
        return normalMap;
    }

    protected Map<V, K> reverseMap() {
        return reverseMap;
    }

    protected boolean keyEquals(K a, K b) {
        return Objects.equals(a, b);
    }

    protected boolean valueEquals(V a, V b) {
        return Objects.equals(a, b);
    }

    @Override
    public V get(final Object key) {
        return normalMap.get(key);
    }

    @Override
    public int size() {
        return normalMap.size();
    }

    @Override
    public boolean isEmpty() {
        return normalMap.isEmpty();
    }

    @Override
    public boolean containsKey(final Object key) {
        return normalMap.containsKey(key);
    }

    @Override
    public boolean equals(final Object obj) {
        return normalMap.equals(obj);
    }

    @Override
    public int hashCode() {
        return normalMap.hashCode();
    }

    @Override
    public String toString() {
        return normalMap.toString();
    }

    // BidiMap changes

    @Override
    public V put(final K key, final V value) {
        if (normalMap.containsKey(key)) {
            reverseMap.remove(normalMap.get(key));
        }
        if (reverseMap.containsKey(value)) {
            normalMap.remove(reverseMap.get(value));
        }
        final V obj = normalMap.put(key, value);
        reverseMap.put(value, key);
        return obj;
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> map) {
        for (final Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public V remove(final Object key) {
        V value = null;
        if (normalMap.containsKey(key)) {
            value = normalMap.remove(key);
            reverseMap.remove(value);
        }
        return value;
    }

    protected boolean removeViaCollection(K key) {
        if (normalMap.containsKey(key)) {
            V value  = normalMap.remove(key);
            reverseMap.remove(value);
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(Object key, Object expectedValue) {
        if (normalMap.containsKey(key)) {
            final V value = normalMap.get(key);
            if (valueEquals(value, (V) expectedValue)) {
                normalMap.remove(key);
                reverseMap.remove(value);
                return true;
            }
        }
        return false;
    }

    @Override
    public void clear() {
        normalMap.clear();
        reverseMap.clear();
    }

    @Override
    public boolean containsValue(final Object value) {
        return reverseMap.containsKey(value);
    }

    // BidiMap
    /**
     * Obtains a {@code MapIterator} over the map.
     * The iterator implements {@link BidiMapIterator}.
     * This implementation relies on the entrySet iterator.
     * <p>
     *
     * @return a map iterator
     */
    @Override
    public MapIterator<K, V> mapIterator() {
        return new BidiMapIterator<>(this);
    }

    @Override
    public K getKey(final Object value) {
        return reverseMap.get(value);
    }

    @Override
    public K removeValue(final Object value) {
        K key = null;
        if (reverseMap.containsKey(value)) {
            key = reverseMap.remove(value);
            normalMap.remove(key);
        }
        return key;
    }

    protected boolean removeValueViaCollection(V value) {
        if (reverseMap.containsKey(value)) {
            final Object key = reverseMap.remove(value);
            normalMap.remove(key);
            return true;
        }
        return false;
    }

    @Override
    public BidiMap<V, K> inverseBidiMap() {
        if (inverseBidiMap == null) {
            inverseBidiMap = createInverse();
        }
        return inverseBidiMap;
    }

    // Map views
    /**
     * Gets a keySet view of the map.
     * Changes made on the view are reflected in the map.
     * The set supports remove and clear but not add.
     *
     * @return the keySet view
     */
    @Override
    public Set<K> keySet() {
        if (keySet == null) {
            keySet = createKeySet();
        }
        return keySet;
    }

    protected KeySet<K> createKeySet() {
        return new KeySet<>(this);
    }

    /**
     * Creates a key set iterator.
     * Subclasses can override this to return iterators with different properties.
     *
     * @param iterator  the iterator to decorate
     * @return the keySet iterator
     */
    protected Iterator<K> createKeySetIterator(final Iterator<K> iterator) {
        return new KeySetIterator<>(iterator, this);
    }

    /**
     * Gets a values view of the map.
     * Changes made on the view are reflected in the map.
     * The set supports remove and clear but not add.
     *
     * @return the values view
     */
    @Override
    public Set<V> values() {
        if (values == null) {
            values = createValues();
        }
        return values;
    }

    protected Values<V> createValues() {
        return new Values<>(this);
    }

    /**
     * Creates a values iterator.
     * Subclasses can override this to return iterators with different properties.
     *
     * @param iterator  the iterator to decorate
     * @return the values iterator
     */
    protected Iterator<V> createValuesIterator(final Iterator<V> iterator) {
        return new ValuesIterator<>(iterator, this);
    }

    /**
     * Gets an entrySet view of the map.
     * Changes made on the set are reflected in the map.
     * The set supports remove and clear but not add.
     * <p>
     * The Map Entry setValue() method only allow a new value to be set.
     * If the value being set is already in the map, an IllegalArgumentException
     * is thrown (as setValue cannot change the size of the map).
     *
     * @return the entrySet view
     */
    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        if (entrySet == null) {
            entrySet = createEntrySet();
        }
        return entrySet;
    }

    protected Set<Map.Entry<K, V>> createEntrySet() {
        return new EntrySet<>(this);
    }

    /**
     * Creates an entry set iterator.
     * Subclasses can override this to return iterators with different properties.
     *
     * @param iterator  the iterator to decorate
     * @return the entrySet iterator
     */
    protected Iterator<Map.Entry<K, V>> createEntrySetIterator(final Iterator<Map.Entry<K, V>> iterator) {
        return new EntrySetIterator<>(iterator, this);
    }

    protected V setValueViaCollection(K key, V value) {
        if (reverseMap.containsKey(value) &&
                !keyEquals(reverseMap.get(value), key)) {
            throw new IllegalArgumentException(
                    "Cannot use setValue() when the value being set is already in the map");
        }
        return put(key, value);
    }

    /**
     * Inner class View.
     */
    protected abstract static class View<K, V, E> extends AbstractCollectionDecorator<E> {

        /** Generated serial version ID. */
        private static final long serialVersionUID = 4621510560119690639L;

        /** The parent map */
        protected final AbstractDualBidiMap<K, V> parent;

        /**
         * Constructor.
         *
         * @param coll  the collection view being decorated
         * @param parent  the parent BidiMap
         */
        protected View(final Collection<E> coll, final AbstractDualBidiMap<K, V> parent) {
            super(coll);
            this.parent = parent;
        }

        @Override
        public boolean equals(final Object object) {
            return object == this || decorated().equals(object);
        }

        @Override
        public int hashCode() {
            return decorated().hashCode();
        }

        /**
         * @since 4.4
         */
        @Override
        public boolean removeIf(final Predicate<? super E> filter) {
            Objects.requireNonNull(filter);
            if (parent.isEmpty() || Objects.isNull(filter)) {
                return false;
            }
            boolean modified = false;
            final Iterator<?> it = iterator();
            while (it.hasNext()) {
                @SuppressWarnings("unchecked")
                final E e = (E) it.next();
                if (filter.test(e)) {
                    it.remove();
                    modified = true;
                }
            }
            return modified;
        }

        @Override
        public boolean removeAll(final Collection<?> coll) {
            if (parent.isEmpty() || coll.isEmpty()) {
                return false;
            }
            boolean modified = false;
            for (final Object current : coll) {
                modified |= remove(current);
            }
            return modified;
        }

        /**
         * {@inheritDoc}
         * <p>
         * This implementation iterates over the elements of this bidi map, checking each element in
         * turn to see if it's contained in {@code coll}. If it's not contained, it's removed
         * from this bidi map. As a consequence, it is advised to use a collection type for
         * {@code coll} that provides a fast (e.g. O(1)) implementation of
         * {@link Collection#contains(Object)}.
         */
        @Override
        public boolean retainAll(final Collection<?> coll) {
            if (parent.isEmpty()) {
                return false;
            }
            if (coll.isEmpty()) {
                parent.clear();
                return true;
            }
            boolean modified = false;
            final Iterator<E> it = iterator();
            while (it.hasNext()) {
                if (!coll.contains(it.next())) {
                    it.remove();
                    modified = true;
                }
            }
            return modified;
        }

        @Override
        public void clear() {
            parent.clear();
        }
    }

    /**
     * Inner class KeySet.
     */
    protected static class KeySet<K> extends View<K, Object, K> implements Set<K> {

        /** Serialization version */
        private static final long serialVersionUID = -7107935777385040694L;

        /**
         * Constructor.
         *
         * @param parent  the parent BidiMap
         */
        @SuppressWarnings("unchecked")
        protected KeySet(final AbstractDualBidiMap<K, ?> parent) {
            super(parent.normalMap.keySet(), (AbstractDualBidiMap<K, Object>) parent);
        }

        @SuppressWarnings("unchecked")
        protected KeySet(Collection<K> keys, AbstractDualBidiMap<K, ?> parent) {
            super(keys, (AbstractDualBidiMap<K, Object>) parent);
        }

        @Override
        public Iterator<K> iterator() {
            return parent.createKeySetIterator(super.iterator());
        }

        @Override
        public boolean contains(final Object key) {
            return parent.normalMap.containsKey(key);
        }

        @Override
        public boolean remove(final Object key) {
            return parent.removeViaCollection((K) key);
        }
    }

    /**
     * Inner class KeySetIterator.
     */
    protected static class KeySetIterator<K> extends AbstractIteratorDecorator<K> {

        /** The parent map */
        protected final AbstractDualBidiMap<K, ?> parent;

        /** The last returned key */
        protected K lastKey;

        /** Whether remove is allowed at present */
        protected boolean canRemove;

        /**
         * Constructor.
         * @param iterator  the iterator to decorate
         * @param parent  the parent map
         */
        protected KeySetIterator(final Iterator<K> iterator, final AbstractDualBidiMap<K, ?> parent) {
            super(iterator);
            this.parent = parent;
        }

        @Override
        public K next() {
            lastKey = super.next();
            canRemove = true;
            return lastKey;
        }

        @Override
        public void remove() {
            if (!canRemove) {
                throw new IllegalStateException("Iterator remove() can only be called once after next()");
            }
            final Object value = parent.normalMap.get(lastKey);
            parent.reverseMap.remove(value);
            super.remove(); // removes from normalMap
            lastKey = null;
            canRemove = false;
        }
    }

    /**
     * Inner class Values.
     */
    protected static class Values<V> extends View<Object, V, V> implements Set<V> {

        /** Serialization version */
        private static final long serialVersionUID = 4023777119829639864L;

        /**
         * Constructor.
         *
         * @param parent  the parent BidiMap
         */
        @SuppressWarnings("unchecked")
        protected Values(final AbstractDualBidiMap<?, V> parent) {
            super(parent.normalMap.values(), (AbstractDualBidiMap<Object, V>) parent);
        }

        @SuppressWarnings("unchecked")
        public Values(Collection<V> set, final AbstractDualBidiMap<?, V> parent) {
            super(set, (AbstractDualBidiMap<Object, V>) parent);
        }

        @Override
        public Iterator<V> iterator() {
            return parent.createValuesIterator(super.iterator());
        }

        @Override
        public boolean contains(final Object value) {
            return parent.reverseMap.containsKey(value);
        }

        @Override
        public boolean remove(final Object value) {
            return parent.removeValueViaCollection((V) value);
        }
    }

    /**
     * Inner class ValuesIterator.
     */
    protected static class ValuesIterator<V> extends AbstractIteratorDecorator<V> {

        /** The parent map */
        protected final AbstractDualBidiMap<Object, V> parent;

        /** The last returned value */
        protected V lastValue;

        /** Whether remove is allowed at present */
        protected boolean canRemove;

        /**
         * Constructor.
         * @param iterator  the iterator to decorate
         * @param parent  the parent map
         */
        @SuppressWarnings("unchecked")
        protected ValuesIterator(final Iterator<V> iterator, final AbstractDualBidiMap<?, V> parent) {
            super(iterator);
            this.parent = (AbstractDualBidiMap<Object, V>) parent;
        }

        @Override
        public V next() {
            lastValue = super.next();
            canRemove = true;
            return lastValue;
        }

        @Override
        public void remove() {
            if (!canRemove) {
                throw new IllegalStateException("Iterator remove() can only be called once after next()");
            }
            parent.reverseMap.remove(lastValue);
            super.remove(); // removes from normalMap via it's values collection
            lastValue = null;
            canRemove = false;
        }
    }

    /**
     * Inner class EntrySet.
     */
    protected static class EntrySet<K, V> extends View<K, V, Map.Entry<K, V>> implements Set<Map.Entry<K, V>> {

        /** Serialization version */
        private static final long serialVersionUID = 4040410962603292348L;

        /**
         * Constructor.
         *
         * @param parent  the parent BidiMap
         */
        protected EntrySet(final AbstractDualBidiMap<K, V> parent) {
            super(parent.normalMap.entrySet(), parent);
        }

        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return parent.createEntrySetIterator(super.iterator());
        }

        @Override
        public boolean remove(final Object obj) {
            if (!(obj instanceof Map.Entry)) {
                return false;
            }
            final Map.Entry<?, ?> entry = (Map.Entry<?, ?>) obj;
            return parent.remove(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Inner class EntrySetIterator.
     */
    protected static class EntrySetIterator<K, V> extends AbstractIteratorDecorator<Map.Entry<K, V>> {

        /** The parent map */
        protected final AbstractDualBidiMap<K, V> parent;

        /** The last returned entry */
        protected Map.Entry<K, V> last;

        /** Whether remove is allowed at present */
        protected boolean canRemove;

        /**
         * Constructor.
         * @param iterator  the iterator to decorate
         * @param parent  the parent map
         */
        protected EntrySetIterator(final Iterator<Map.Entry<K, V>> iterator, final AbstractDualBidiMap<K, V> parent) {
            super(iterator);
            this.parent = parent;
        }

        @Override
        public Map.Entry<K, V> next() {
            last = new MapEntry<>(super.next(), parent);
            canRemove = true;
            return last;
        }

        @Override
        public void remove() {
            if (!canRemove) {
                throw new IllegalStateException("Iterator remove() can only be called once after next()");
            }
            parent.reverseMap.remove(last.getValue());
            super.remove(); // removes from normalMap
            last = null;
            canRemove = false;
        }
    }

    /**
     * Inner class MapEntry.
     */
    protected static class MapEntry<K, V> extends AbstractMapEntryDecorator<K, V> {

        /** The parent map */
        protected final AbstractDualBidiMap<K, V> parent;

        /**
         * Constructor.
         * @param entry  the entry to decorate
         * @param parent  the parent map
         */
        protected MapEntry(final Map.Entry<K, V> entry, final AbstractDualBidiMap<K, V> parent) {
            super(entry);
            this.parent = parent;
        }

        @Override
        public V setValue(final V value) {
            final K key = getKey();
            parent.setValueViaCollection(key, value);
            return super.setValue(value);
        }
    }

    /**
     * Inner class MapIterator.
     */
    protected static class BidiMapIterator<K, V> implements MapIterator<K, V>, ResettableIterator<K> {

        /** The parent map */
        protected final AbstractDualBidiMap<K, V> parent;

        /** The iterator being wrapped */
        protected Iterator<Map.Entry<K, V>> iterator;

        /** The last returned entry */
        protected Map.Entry<K, V> last;

        /** Whether remove is allowed at present */
        protected boolean canRemove;

        /**
         * Constructor.
         * @param parent  the parent map
         */
        protected BidiMapIterator(final AbstractDualBidiMap<K, V> parent) {
            this.parent = parent;
            this.iterator = parent.normalMap.entrySet().iterator();
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public K next() {
            last = iterator.next();
            canRemove = true;
            return last.getKey();
        }

        @Override
        public void remove() {
            if (!canRemove) {
                throw new IllegalStateException("Iterator remove() can only be called once after next()");
            }
            parent.reverseMap.remove(last.getValue());
            iterator.remove(); // removes from normalMap
            last = null;
            canRemove = false;
        }

        @Override
        public K getKey() {
            if (last == null) {
                throw new IllegalStateException(
                        "Iterator getKey() can only be called after next() and before remove()");
            }
            return last.getKey();
        }

        @Override
        public V getValue() {
            if (last == null) {
                throw new IllegalStateException(
                        "Iterator getValue() can only be called after next() and before remove()");
            }
            return last.getValue();
        }

        @Override
        public V setValue(final V value) {
            if (last == null) {
                throw new IllegalStateException(
                        "Iterator setValue() can only be called after next() and before remove()");
            }
            final K key = last.getKey();
            return parent.setValueViaCollection(key, value);
        }

        @Override
        public void reset() {
            iterator = parent.normalMap.entrySet().iterator();
            last = null;
            canRemove = false;
        }

        @Override
        public String toString() {
            if (last != null) {
                return "MapIterator[" + getKey() + "=" + getValue() + "]";
            }
            return "MapIterator[]";
        }
    }

}
