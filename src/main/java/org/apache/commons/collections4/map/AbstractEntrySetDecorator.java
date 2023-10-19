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

import org.apache.commons.collections4.iterators.AbstractIteratorDecorator;
import org.apache.commons.collections4.set.AbstractSetDecorator;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class AbstractEntrySetDecorator<K, V> extends AbstractSetDecorator<Map.Entry<K, V>, Set<Map.Entry<K, V>>> {
    protected AbstractEntrySetDecorator(Set<Map.Entry<K, V>> set) {
        super(set);
    }

    protected Map.Entry<K, V> wrapEntry(Map.Entry<K, V> entry) {
        return entry;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object[] toArray() {
        final Object[] array = super.toArray();
        for (int i = 0; i < array.length; i++) {
            array[i] = wrapEntry((Map.Entry<K, V>) array[i]);
        }
        return array;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] array) {
        Object[] result = array;
        if (array.length > 0) {
            // we must create a new array to handle multithreaded situations
            // where another thread could access data before we decorate it
            result = (Object[]) Array.newInstance(array.getClass().getComponentType(), 0);
        }
        result = super.toArray(result);
        for (int i = 0; i < result.length; i++) {
            result[i] = wrapEntry((Map.Entry<K, V>) result[i]);
        }

        // check to see if result should be returned straight
        if (result.length > array.length) {
            return (T[]) result;
        }

        // copy back into input array to fulfill the method contract
        System.arraycopy(result, 0, array, 0, result.length);
        if (array.length > result.length) {
            array[result.length] = null;
        }
        return array;
    }

    @Override
    public boolean removeIf(Predicate<? super Map.Entry<K, V>> filter) {
        return super.removeIf(entry -> filter.test(wrapEntry(entry)));
    }

    @Override
    public void forEach(Consumer<? super Map.Entry<K, V>> action) {
        super.forEach(entry -> action.accept(wrapEntry(entry)));
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        return new EntryWrappingIterator(super.iterator());
    }

    protected class EntryWrappingIterator extends AbstractIteratorDecorator<Map.Entry<K, V>> {
        public EntryWrappingIterator(Iterator<Map.Entry<K, V>> iterator) {
            super(iterator);
        }

        @Override
        public Map.Entry<K, V> next() {
            return wrapEntry(super.next());
        }

        @Override
        public void forEachRemaining(Consumer<? super Map.Entry<K, V>> action) {
            super.forEachRemaining(entry -> action.accept(wrapEntry(entry)));
        }
    }
}
