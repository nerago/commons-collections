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
package org.apache.commons.collections4.iterators;

import org.apache.commons.collections4.Transformer;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Decorates an iterator such that each element returned is transformed.
 * Add/set methods require a reverse transformer but otherwise works like other transformers.
 * @since X.X
 */
public class TransformListIterator<I, O> implements ListIterator<O> {

    /** The iterator being used */
    private ListIterator<I> iterator;
    /** The transformer being used */
    private Transformer<? super I, ? extends O> transformer;
    private Transformer<? super O, ? extends I> reverseTransformer;

    /**
     * Constructs a new {@code TransformListIterator} that will not function
     * until the {@link #setIterator(ListIterator) setIterator} and
     * {@link #setTransformer(Transformer)} methods are invoked.
     */
    public TransformListIterator() {
    }

    /**
     * Constructs a new {@code TransformListIterator} that won't transform
     * elements from the given iterator.
     *
     * @param iterator  the iterator to use
     */
    public TransformListIterator(final ListIterator<I> iterator) {
        this.iterator = iterator;
    }

    /**
     * Constructs a new {@code TransformListIterator} that will use the
     * given iterator and transformer.  If the given transformer is null,
     * then objects will not be transformed.
     *
     * @param iterator  the iterator to use
     * @param transformer  the transformer to use
     */
    public TransformListIterator(final ListIterator<I> iterator,
                                 final Transformer<? super I, ? extends O> transformer) {
        this.iterator = iterator;
        this.transformer = transformer;
    }

    /**
     * Constructs a new {@code TransformListIterator} that will use the
     * given iterator and transformer.  If the given transformer is null,
     * then objects will not be transformed.
     *
     * @param iterator  the iterator to use
     * @param transformer  the transformer to use
     */
    public TransformListIterator(final ListIterator<I> iterator,
                                 final Transformer<? super I, ? extends O> transformer,
                                 final Transformer<? super O, ? extends I> reverseTransformer) {
        this.iterator = iterator;
        this.transformer = transformer;
        this.reverseTransformer = reverseTransformer;
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public boolean hasPrevious() {
        return iterator.hasPrevious();
    }

    /**
     * Gets the next object from the iteration, transforming it using the
     * current transformer. If the transformer is null, no transformation
     * occurs and the object from the iterator is returned directly.
     *
     * @return the next object
     * @throws java.util.NoSuchElementException if there are no more elements
     */
    @Override
    public O next() {
        return transform(iterator.next());
    }

    /**
     * Gets the next object from the iteration, transforming it using the
     * current transformer. If the transformer is null, no transformation
     * occurs and the object from the iterator is returned directly.
     *
     * @return the next object
     * @throws java.util.NoSuchElementException if there are no more elements
     */
    @Override
    public O previous() {
        return transform(iterator.previous());
    }

    public void forEachRemaining(Consumer<? super O> action) {
        Objects.requireNonNull(action);
        iterator.forEachRemaining(i -> action.accept(transform(i)));
    }

    @Override
    public void remove() {
        iterator.remove();
    }

    @Override
    public int nextIndex() {
        return iterator.nextIndex();
    }

    @Override
    public int previousIndex() {
        return iterator.previousIndex();
    }

    @Override
    public void set(O o) {
        iterator.set(transformReverse(o));
    }

    @Override
    public void add(O o) {
        iterator.add(transformReverse(o));
    }

    /**
     * Gets the iterator this iterator is using.
     *
     * @return the iterator.
     */
    public ListIterator<? extends I> getIterator() {
        return iterator;
    }

    /**
     * Sets the iterator for this iterator to use.
     * If iteration has started, this effectively resets the iterator.
     *
     * @param iterator  the iterator to use
     */
    public void setIterator(final ListIterator<I> iterator) {
        this.iterator = iterator;
    }

    /**
     * Gets the transformer this iterator is using.
     *
     * @return the transformer.
     */
    public Transformer<? super I, ? extends O> getTransformer() {
        return transformer;
    }

    /**
     * Sets the transformer this the iterator to use.
     * A null transformer is a no-op transformer.
     *
     * @param transformer  the transformer to use
     */
    public void setTransformer(final Transformer<? super I, ? extends O> transformer) {
        this.transformer = transformer;
    }

    /**
     * Gets the transformer this iterator is using.
     *
     * @return the transformer.
     */
    public Transformer<? super O, ? extends I> getReverseTransformer() {
        return reverseTransformer;
    }

    /**
     * Sets the transformer this the iterator to use.
     * Enables set/add methods.
     *
     * @param reverseTransformer  the transformer to use
     */
    public void setReverseTransformer(final Transformer<? super O, ? extends I> reverseTransformer) {
        this.reverseTransformer = reverseTransformer;
    }

    /**
     * Transforms the given object using the transformer.
     * If the transformer is null, the original object is returned as-is.
     *
     * @param source  the object to transform
     * @return the transformed object
     */
    @SuppressWarnings("unchecked")
    protected O transform(final I source) {
        if (transformer != null)
            return transformer.transform(source);
        else
            return (O) source;
    }

    /**
     * Transforms the given object using the transformer.
     * If the transformer is null, the original object is returned as-is.
     *
     * @param source  the object to transform
     * @return the transformed object
     */
    protected I transformReverse(final O source) {
        if (reverseTransformer == null)
            throw new UnsupportedOperationException();
        return reverseTransformer.transform(source);
    }
}
