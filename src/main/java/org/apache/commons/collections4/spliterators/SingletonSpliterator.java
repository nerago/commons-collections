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

import java.util.Spliterator;
import java.util.function.Consumer;

public class SingletonSpliterator<E> implements Spliterator<E> {
    /** Is the cursor before the first element */
    private boolean beforeFirst = true;
    /** The object */
    private final E object;

    public SingletonSpliterator(E object) {
        this.object = object;
    }

    @Override
    public boolean tryAdvance(Consumer<? super E> action) {
        if (beforeFirst) {
            action.accept(object);
            beforeFirst = false;
            return true;
        }
        return false;
    }

    @Override
    public Spliterator<E> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        return 1;
    }

    @Override
    public int characteristics() {
        return Spliterator.SIZED;
    }
}