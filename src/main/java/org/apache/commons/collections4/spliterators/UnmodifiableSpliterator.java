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

import org.apache.commons.collections4.Unmodifiable;

import java.util.Spliterator;

// what's this for?
public class UnmodifiableSpliterator<E>
        extends AbstractSpliteratorDecorator<E, Spliterator<E>>
        implements Spliterator<E>, Unmodifiable {
    public UnmodifiableSpliterator(final Spliterator<E> spliterator) {
        super(spliterator);
    }

    @Override
    protected Spliterator<E> decorateSplit(final Spliterator<E> split) {
        return new UnmodifiableSpliterator<>(split);
    }

    @Override
    public int characteristics() {
        return decorated().characteristics() | Spliterator.IMMUTABLE;
    }
}
