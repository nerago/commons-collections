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
package org.apache.commons.collections4.spliterators;

import org.apache.commons.collections4.Transformer;

import java.util.Comparator;
import java.util.Spliterator;
import java.util.function.Consumer;

public class TransformSpliterator<I, O> implements Spliterator<O> {
    private final Spliterator<I> spliterator;
    private final Transformer<? super I, ? extends O> transformer;

    public TransformSpliterator(final Spliterator<I> spliterator, final Transformer<? super I, ? extends O> transformer) {
        this.spliterator = spliterator;
        this.transformer = transformer;
    }

    @Override
    public boolean tryAdvance(final Consumer<? super O> action) {
        return spliterator.tryAdvance(e -> action.accept(transformer.transform(e)));
    }

    @Override
    public void forEachRemaining(final Consumer<? super O> action) {
        spliterator.forEachRemaining(e -> action.accept(transformer.transform(e)));
    }

    @Override
    public Spliterator<O> trySplit() {
        final Spliterator<I> split = spliterator.trySplit();
        if (split != null)
            return new TransformSpliterator<>(split, transformer);
        else
            return null;
    }

    @Override
    public long estimateSize() {
        return spliterator.estimateSize();
    }

    @Override
    public int characteristics() {
        return spliterator.characteristics();
    }

    @Override
    public long getExactSizeIfKnown() {
        return spliterator.getExactSizeIfKnown();
    }

    @Override
    public boolean hasCharacteristics(final int characteristics) {
        return spliterator.hasCharacteristics(characteristics);
    }
}
