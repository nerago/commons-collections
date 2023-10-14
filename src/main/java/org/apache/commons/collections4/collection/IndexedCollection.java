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
package org.apache.commons.collections4.collection;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.iterators.AbstractIteratorDecorator;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

/**
 * An IndexedCollection is a Map-like view onto a Collection. It accepts a
 * keyTransformer to define how the keys are converted from the values.
 * <p>
 * Modifications made to this decorator modify the index as well as the
 * decorated {@link Collection}. However, modifications to the underlying
 * {@link Collection} will not update the index and it will get out of sync.
 * </p>
 * <p>
 * If modification of the decorated {@link Collection} is unavoidable, then a
 * call to {@link #reindex()} will update the index to the current contents of
 * the {@link Collection}.
 * </p>
 *
 * @param <K> the type of object in the index.
 * @param <C> the type of object in the collection.
 *
 * @since 4.0
 */
public class IndexedCollection<K, C> extends AbstractCollectionDecorator<C> {

    /** Serialization version */
    private static final long serialVersionUID = -5512610452568370038L;

    /** The {@link Transformer} for generating index keys. */
    private final Transformer<C, K> keyTransformer;

    /** The map of indexes to collected objects. */
    private final ListValuedMap<K, C> index;

    /** The uniqueness constraint for the index. */
    private final boolean uniqueIndex;

    /**
     * Create an {@link IndexedCollection} for a unique index.
     * <p>
     * If an element is added, which maps to an existing key, an {@link IllegalArgumentException}
     * will be thrown.
     *
     * @param <K> the index object type.
     * @param <C> the collection type.
     * @param coll the decorated {@link Collection}.
     * @param keyTransformer the {@link Transformer} for generating index keys.
     * @return the created {@link IndexedCollection}.
     */
    public static <K, C> IndexedCollection<K, C> uniqueIndexedCollection(final Collection<C> coll,
                                                                         final Transformer<C, K> keyTransformer) {
        return new IndexedCollection<>(coll, keyTransformer,
                                           new ArrayListValuedHashMap<>(1),
                                           true);
    }

    /**
     * Create an {@link IndexedCollection} for a non-unique index.
     *
     * @param <K> the index object type.
     * @param <C> the collection type.
     * @param coll the decorated {@link Collection}.
     * @param keyTransformer the {@link Transformer} for generating index keys.
     * @return the created {@link IndexedCollection}.
     */
    public static <K, C> IndexedCollection<K, C> nonUniqueIndexedCollection(final Collection<C> coll,
                                                                            final Transformer<C, K> keyTransformer) {
        return new IndexedCollection<>(coll, keyTransformer,
                                            new ArrayListValuedHashMap<>(),
                                           false);
    }

    /**
     * Create a {@link IndexedCollection}.
     *
     * @param coll  decorated {@link Collection}
     * @param keyTransformer  {@link Transformer} for generating index keys
     * @param map  map to use as index
     * @param uniqueIndex  if the index shall enforce uniqueness of index keys
     */
    public IndexedCollection(final Collection<C> coll, final Transformer<C, K> keyTransformer,
                             final ListValuedMap<K, C> map, final boolean uniqueIndex) {
        super(coll);
        this.keyTransformer = keyTransformer;
        this.index = map;
        this.uniqueIndex = uniqueIndex;
        reindex();
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if the object maps to an existing key and the index
     *   enforces a uniqueness constraint
     */
    @Override
    public boolean add(final C object) {
        final K key = checkCanAddToIndex(object);
        final boolean added = super.add(object);
        if (added) {
            index.put(key, object);
        }
        return added;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if the object maps to an existing key and the index
     *   enforces a uniqueness constraint
     */
    @Override
    public boolean addAll(final Collection<? extends C> coll) {
        checkCanAddAllToIndex(coll);

        boolean changed = false;
        for (final C c: coll) {
            changed |= add(c);
        }
        return changed;
    }

    @Override
    public void clear() {
        super.clear();
        index.clear();
    }

    /**
     * Returns <tt>true</tt> if this collection contains the specified element.
     * More formally, returns <tt>true</tt> if and only if this collection
     * contains at least one element <tt>e</tt> such that keyTransformer(object) == keyTransformer(e)
     * <p>
     * Note: uses the index for fast lookup.
     * Will return true for any object that maps to the same index key as a contained element.
     * If you need normal {@link Collection#contains} semantics then see {@link #containsExact}.
     *
     * @param object element whose presence in this collection is to be tested
     * @return <tt>true</tt> if this collection contains any element with the same index key
     * @throws ClassCastException if the type of the specified element is incompatible with this collection or transformer
     * @throws NullPointerException if the specified element is null and this collection or transformer does not permit null elements
     */
    @Override
    public boolean contains(final Object object) {
        return index.containsKey(transform(object));
    }

    /**
     * Returns <tt>true</tt> if this collection contains the specified element.
     * More formally, returns <tt>true</tt> if and only if this collection
     * contains at least one element <tt>e</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
     *
     * @param object element whose presence in this collection is to be tested
     * @return <tt>true</tt> if this collection contains the specified element
     * @throws ClassCastException if the type of the specified element is incompatible with this collection or transformer
     * @throws NullPointerException if the specified element is null and this collection or transformer does not permit null elements
     */
    public boolean containsExact(final Object object) {
        final K key = transform(object);
        return index.containsMapping(key, object);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note: uses the index for fast lookup.
     * Will return true for any object that maps to the same index key as a contained element.
     */
    @Override
    public boolean containsAll(final Collection<?> coll) {
        for (final Object o : coll) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get the element associated with the given key.
     * <p>
     * In case of a non-unique index, this method will return the first
     * value associated with the given key. To retrieve all elements associated
     * with a key, use {@link #values(Object)}.
     *
     * @param key  key to look up
     * @return element found
     * @see #values(Object)
     */
    public C get(final K key) {
        // index is a MultiValuedMap which returns a Collection
        if (index.containsKey(key)) {
            final List<C> coll = index.get(key);
            if (!coll.isEmpty()) {
                return coll.get(0);
            }
        }
        return null;
    }

    /**
     * Get all elements associated with the given key.
     *
     * @param key  key to look up
     * @return a collection of elements found, or null if {@code contains(key) == false}
     */
    public Collection<C> values(final K key) {
        // index is a MultiValuedMap which returns a Collection
        if (index.containsKey(key)) {
            final List<C> coll = index.get(key);
            if (!coll.isEmpty()) {
                return UnmodifiableCollection.unmodifiableCollection(coll);
            }
        }
        return null;
    }

    /**
     * Clears the index and re-indexes the entire decorated {@link Collection}.
     */
    public void reindex() {
        index.clear();
        for (final C c : decorated()) {
            final K key = checkCanAddToIndex(c);
            index.put(key, c);
        }
    }

    @Override
    public boolean remove(final Object object) {
        final boolean removed = super.remove(object);
        if (removed) {
            final K key = transform(object);
            index.removeMapping(key, object);
        }
        return removed;
    }

    /**
     * @since 4.4
     */
    @Override
    public boolean removeIf(final Predicate<? super C> filter) {
        Objects.requireNonNull(filter);
        boolean changed = false;
        final Iterator<C> it = decorated().iterator();
        while (it.hasNext()) {
            if (filter.test(it.next())) {
                it.remove();
                changed = true;
            }
        }
        if (changed) {
            reindex();
        }
        return changed;
    }

    @Override
    public boolean removeAll(final Collection<?> coll) {
        return removeIf(coll::contains);
    }

    @Override
    public boolean retainAll(final Collection<?> coll) {
        final boolean changed = super.retainAll(coll);
        if (changed) {
            reindex();
        }
        return changed;
    }

    @Override
    public Iterator<C> iterator() {
        return new IndexedIterator(super.iterator());
    }

    @SuppressWarnings("unchecked")
    private K transform(final Object object) {
        return keyTransformer.transform((C) object);
    }

    /**
     * Provides checking for adding to the index before any changes made.
     *
     * @param object the object to check
     * @return the transformed key for object
     * @throws IllegalArgumentException if the object maps to an existing key
     */
    private K checkCanAddToIndex(final C object) {
        final K key = transform(object);
        if (uniqueIndex && index.containsKey(key)) {
            throw new IllegalArgumentException("Duplicate key in uniquely indexed collection.");
        }
        return key;
    }

    /**
     * Provides checking for adding to the index before any changes made.
     *
     * @param coll the objects to check
     * @throws IllegalArgumentException if any object maps to an existing key
     */
    private void checkCanAddAllToIndex(final Iterable<? extends C> coll) {
        if (uniqueIndex) {
            for (final C object : coll) {
                final K key = transform(object);
                if (index.containsKey(key)) {
                    throw new IllegalArgumentException("Duplicate key in uniquely indexed collection.");
                }
            }
        }
    }

    private final class IndexedIterator extends AbstractIteratorDecorator<C> {
        private C last;

        private IndexedIterator(final Iterator<C> iterator) {
            super(iterator);
        }

        @Override
        public C next() {
            final C item = super.next();
            last = item;
            return item;
        }

        @Override
        public void remove() {
            super.remove();

            final K key = transform(last);
            index.removeMapping(key, last);
        }
    }
}
