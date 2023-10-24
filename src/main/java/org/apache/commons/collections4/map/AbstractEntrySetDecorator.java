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
package org.apache.commons.collections4.map;

import org.apache.commons.collections4.ToArrayUtils;
import org.apache.commons.collections4.iterators.AbstractIteratorDecorator;
import org.apache.commons.collections4.set.AbstractSetDecorator;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class AbstractEntrySetDecorator<K, V> extends AbstractSetDecorator<Map.Entry<K, V>, Set<Map.Entry<K, V>>> {
    protected AbstractEntrySetDecorator(final Set<Map.Entry<K, V>> set) {
        super(set);
    }

    protected Map.Entry<K, V> wrapEntry(final Map.Entry<K, V> entry) {
        return entry;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object[] toArray() {
        return ToArrayUtils.transformed(super::toArray, this::wrapEntry);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(final T[] array) {
        return ToArrayUtils.transformed(super::toArray, this::wrapEntry, array);
    }

    @Override
    public boolean removeIf(final Predicate<? super Map.Entry<K, V>> filter) {
        return super.removeIf(entry -> filter.test(wrapEntry(entry)));
    }

    @Override
    public void forEach(final Consumer<? super Map.Entry<K, V>> action) {
        super.forEach(entry -> action.accept(wrapEntry(entry)));
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        return new EntryWrappingIterator(super.iterator());
    }

    protected class EntryWrappingIterator extends AbstractIteratorDecorator<Map.Entry<K, V>> {
        public EntryWrappingIterator(final Iterator<Map.Entry<K, V>> iterator) {
            super(iterator);
        }

        @Override
        public Map.Entry<K, V> next() {
            return wrapEntry(super.next());
        }

        @Override
        public void forEachRemaining(final Consumer<? super Map.Entry<K, V>> action) {
            super.forEachRemaining(entry -> action.accept(wrapEntry(entry)));
        }
    }
}
