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

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.collections4.CollectionCommonsRole;
import org.apache.commons.collections4.IterableSortedMap;
import org.apache.commons.collections4.collection.IterationBehaviour;

/**
 * JUnit tests.
 */
public class JDKTreeMapTest<K extends Comparable<K>, V> extends AbstractIterableSortedMapTest<K, V, IterableSortedMap<K, V, ?>> {

    @Override
    public IterableSortedMap<K, V, ?> makeObject() {
        return new AbstractSortedMapDecorator.BasicWrapper<>(new TreeMap<>());
    }

    @Override
    protected Map<K, V> makeObjectCopy(final Map<K, V> map) {
        final AbstractSortedMapDecorator.BasicWrapper<K, V> wrapper = (AbstractSortedMapDecorator.BasicWrapper<K, V>) map;
        final SortedMap<K, V> inner = wrapper.decorated();
        return new AbstractSortedMapDecorator.BasicWrapper<>(new TreeMap<>(inner));
    }

    @Override
    public CollectionCommonsRole collectionRole() {
        return CollectionCommonsRole.CONCRETE;
    }

    @Override
    public boolean isFailFastFunctionalExpected() {
        return false;
    }

    @Override
    public String getCompatibilityVersion() {
        return "4";
    }

}
