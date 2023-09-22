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

import java.util.Comparator;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;

public abstract class AbstractSpliteratorDecorator<E, T extends Spliterator<E>> implements Spliterator<E> {
    private final Spliterator<E> spliterator;

    protected AbstractSpliteratorDecorator(Spliterator<E> spliterator) {
        this.spliterator = spliterator;
    }

    protected Spliterator<E> decorated() { return spliterator; }

    @Override
    public boolean tryAdvance(Consumer<? super E> action) {
        return spliterator.tryAdvance(action);
    }

    @Override
    public void forEachRemaining(Consumer<? super E> action) {
        spliterator.forEachRemaining(action);
    }

    @Override
    public T trySplit() {
        Spliterator<E> split = spliterator.trySplit();
        if (split != null)
            return decorateSplit(split);
        else
            return null;
    }

    protected abstract T decorateSplit(Spliterator<E> split);

    @Override
    public long estimateSize() {
        return spliterator.estimateSize();
    }

    @Override
    public int characteristics() {
        return spliterator.characteristics();
    }

    @Override
    public Comparator<? super E> getComparator() {
        return spliterator.getComparator();
    }
}
