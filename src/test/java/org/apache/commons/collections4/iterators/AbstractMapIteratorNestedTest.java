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

import org.apache.commons.collections4.IterableMap;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.map.AbstractIterableMapTest;
import org.apache.commons.collections4.map.AbstractMapTest;
import org.junit.jupiter.api.BeforeEach;

import java.util.Map;

/**
 * Abstract class for testing MapIterator to simplify nesting inside AbstractMapTest types
 */
public abstract class AbstractMapIteratorNestedTest<K, V> extends AbstractMapIteratorTest<K, V> {
    protected AbstractIterableMapTest<K, V>.MapTest mapTest;

    public abstract AbstractIterableMapTest<K, V> outerTest();

    @BeforeEach
    protected void prepare() {
        mapTest = outerTest().makeMapTest();
    }

    @Override
    public MapIterator<K, V> makeEmptyIterator() {
        mapTest.resetEmpty();
        return getMap().mapIterator();
    }

    @Override
    public MapIterator<K, V> makeObject() {
        mapTest.resetFull();
        return getMap().mapIterator();
    }

    @Override
    public IterableMap<K, V> getMap() {
        // assumes makeFullMapIterator() called first
        return mapTest.getMap();
    }

    @Override
    public Map<K, V> getConfirmedMap() {
        // assumes makeFullMapIterator() called first
        return mapTest.getConfirmed();
    }

    @Override
    public V[] addSetValues() {
        return outerTest().getNewSampleValues();
    }

    @Override
    public boolean supportsRemove() {
        return outerTest().isRemoveSupported();
    }

    @Override
    public boolean supportsSetValue() {
        return outerTest().isSetValueSupported();
    }

    @Override
    public boolean isGetStructuralModify() {
        return outerTest().isGetStructuralModify();
    }

    @Override
    public void verify() {
        super.verify();
        mapTest.verify();
    }
}
