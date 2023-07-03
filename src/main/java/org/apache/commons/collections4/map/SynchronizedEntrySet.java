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

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class SynchronizedEntrySet<K, V> extends AbstractEntrySetDecorator<K, V> {
    private final Object lock;

    public SynchronizedEntrySet(Set<Map.Entry<K, V>> entrySet, Object lock) {
        super(entrySet);
        this.lock = lock;
    }

    @Override
    protected Map.Entry<K, V> wrapEntry(Map.Entry<K, V> entry) {
        return new SynchronizedMapEntry<>(entry, lock);
    }

    @Override
    public Object[] toArray() {
        synchronized (lock) {
            return super.toArray();
        }
    }

    @Override
    public <T> T[] toArray(T[] array) {
        synchronized (lock) {
            return super.toArray(array);
        }
    }

    @Override
    public boolean removeIf(Predicate<? super Map.Entry<K, V>> filter) {
        synchronized (lock) {
            return super.removeIf(filter);
        }
    }

    @Override
    public void forEach(Consumer<? super Map.Entry<K, V>> action) {
        synchronized (lock) {
            super.forEach(action);
        }
    }

    @Override
    public boolean equals(Object object) {
        synchronized (lock) {
            return super.equals(object);
        }
    }

    @Override
    public int hashCode() {
        synchronized (lock) {
            return super.hashCode();
        }
    }

    @Override
    public boolean add(Map.Entry<K, V> object) {
        synchronized (lock) {
            return super.add(object);
        }
    }

    @Override
    public boolean addAll(Collection<? extends Map.Entry<K, V>> coll) {
        synchronized (lock) {
            return super.addAll(coll);
        }
    }

    @Override
    public void clear() {
        synchronized (lock) {
            super.clear();
        }
    }

    @Override
    public boolean contains(Object object) {
        synchronized (lock) {
            return super.contains(object);
        }
    }

    @Override
    public boolean isEmpty() {
        synchronized (lock) {
            return super.isEmpty();
        }
    }

    @Override
    public boolean remove(Object object) {
        synchronized (lock) {
            return super.remove(object);
        }
    }

    @Override
    public int size() {
        synchronized (lock) {
            return super.size();
        }
    }

    @Override
    public boolean containsAll(Collection<?> coll) {
        synchronized (lock) {
            return super.containsAll(coll);
        }
    }

    @Override
    public boolean removeAll(Collection<?> coll) {
        synchronized (lock) {
            return super.removeAll(coll);
        }
    }

    @Override
    public boolean retainAll(Collection<?> coll) {
        synchronized (lock) {
            return super.retainAll(coll);
        }
    }

    @Override
    public String toString() {
        synchronized (lock) {
            return super.toString();
        }
    }

    protected static class SynchronizedMapEntry<K, V> implements Map.Entry<K, V> {
        private final Map.Entry<K, V> entry;
        private final Object lock;

        protected SynchronizedMapEntry(Map.Entry<K, V> entry, Object lock) {
            this.entry = entry;
            this.lock = lock;
        }

        @Override
        public K getKey() {
            synchronized (lock) {
                return entry.getKey();
            }
        }

        @Override
        public V getValue() {
            synchronized (lock) {
                return entry.getValue();
            }
        }

        @Override
        public V setValue(V value) {
            synchronized (lock) {
                return entry.setValue(value);
            }
        }
    }
}
