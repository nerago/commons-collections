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

import org.apache.commons.collections4.AbstractCommonsCollection;
import org.apache.commons.collections4.ToArrayUtils;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.iterators.TransformIterator;
import org.apache.commons.collections4.spliterators.TransformSpliterator;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Decorates another {@link Collection} to transform objects that are added or retrieved.
 * Provides a view over existing collection with new derived type or content.
 * <p>
 * All methods are affected by this class and act as a collection of the transformed type.
 * Requires transform methods in both directions between stored and interface types.
 * </p>
 * <p>
 * See also {@link TransformedCollection}.
 * </p>
 *
 * @param <TStored> the type of the elements stored in the collection
 * @param <TExternal> the type of the elements external collection interface for the transformed collection
 * @since X.X
 */
public class DualTransformedCollection<TExternal, TStored> extends AbstractCommonsCollection<TExternal> implements Collection<TExternal>, Serializable {

    /** Serialization version */
    private static final long serialVersionUID = 4488366779197789058L;

    /** The transformer to use */
    protected Transformer<? super TExternal, ? extends TStored> interfaceToStorage;
    protected Transformer<? super TStored, ? extends TExternal> storageToInterface;

    protected Collection<TStored> collection;

    public static <S, I> Collection<I> transformingCollection(final Collection<S> coll,
                                                              final Transformer<? super I, ? extends S> interfaceToStorage,
                                                              final Transformer<? super S, ? extends I> storageToInterface) {
        return new DualTransformedCollection<>(coll, interfaceToStorage, storageToInterface);
    }

    public static <S, I> Collection<I> transformedCollection(final Collection<I> input,
                                                             final Collection<S> collection,
                                                             final Transformer<? super I, ? extends S> interfaceToStorage,
                                                             final Transformer<? super S, ? extends I> storageToInterface) {
        for (final I value : input) {
            collection.add(interfaceToStorage.transform(value));
        }
        return new DualTransformedCollection<>(collection, interfaceToStorage, storageToInterface);
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param collection  the collection to decorate, must not be null
     * @param interfaceToStorage  the transformer to use for conversion of inputs, must not be null
     * @param storageToInterface the transformer to use for conversion of outputs, must not be null
     * @throws NullPointerException if collection or transformer is null
     */
    protected DualTransformedCollection(final Collection<TStored> collection,
                                        final Transformer<? super TExternal, ? extends TStored> interfaceToStorage,
                                        final Transformer<? super TStored, ? extends TExternal> storageToInterface) {
        this.collection = Objects.requireNonNull(collection);
        this.interfaceToStorage = Objects.requireNonNull(interfaceToStorage, "interfaceToStored");
        this.storageToInterface = Objects.requireNonNull(storageToInterface, "storedToInterface");
    }

    /**
     * Original wrapped collection.
     */
    public Collection<TStored> decorated() {
        return collection;
    }

    @SuppressWarnings("unchecked")
    private List<TStored> transformCollection(Collection<?> c) {
        final List<TStored> list = new ArrayList<>(c.size());
        for (final Object item : c) {
            list.add(interfaceToStorage.transform((TExternal) item));
        }
        return list;
    }

    @Override
    public int size() {
        return collection.size();
    }

    @Override
    public boolean isEmpty() {
        return collection.isEmpty();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        return collection.contains(interfaceToStorage.transform((TExternal) o));
    }

    @Override
    public boolean add(TExternal o) {
        return collection.add(interfaceToStorage.transform(o));
    }

    @Override
    public boolean addAll(Collection<? extends TExternal> c) {
        final List<TStored> list = transformCollection(c);
        return collection.addAll(list);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(Object o) {
        return collection.remove(interfaceToStorage.transform((TExternal) o));
    }

    @Override
    public void clear() {
        collection.clear();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        final List<TStored> list = transformCollection(c);
        return collection.containsAll(list);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        final List<TStored> list = transformCollection(c);
        return collection.removeAll(list);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        final List<TStored> list = transformCollection(c);
        return collection.retainAll(list);
    }

    @Override
    public void forEach(Consumer<? super TExternal> action) {
        collection.forEach(s -> action.accept(storageToInterface.transform(s)));
    }

    @Override
    public boolean removeIf(Predicate<? super TExternal> filter) {
        return collection.removeIf(s -> filter.test(storageToInterface.transform(s)));
    }

    @Override
    public Iterator<TExternal> iterator() {
        return new TransformIterator<>(collection.iterator(), storageToInterface);
    }

    @Override
    public Spliterator<TExternal> spliterator() {
        return new TransformSpliterator<>(collection.spliterator(), storageToInterface);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(interfaceToStorage);
        out.writeObject(storageToInterface);
        out.writeObject(collection);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        interfaceToStorage = (Transformer<? super TExternal, ? extends TStored>) in.readObject();
        storageToInterface = (Transformer<? super TStored, ? extends TExternal>) in.readObject();
        collection = (Collection<TStored>) in.readObject();
    }
}
