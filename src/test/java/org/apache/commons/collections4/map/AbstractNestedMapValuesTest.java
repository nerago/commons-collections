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

import org.apache.commons.collections4.collection.AbstractCollectionTest;

import java.util.Collection;
import java.util.Map;

public abstract class AbstractNestedMapValuesTest<K, V> extends AbstractCollectionTest<V> {

    public abstract AbstractMapTest<K, V> outerTest();

    public abstract AbstractMapTest<K, V>.MapTest mapTest();

    public Map<K, V> getMap() {
        return mapTest().getMap();
    }

    public Map<K, V> getConfirmedMap() {
        return mapTest().getConfirmed();
    }

    @Override
    public V[] getFullElements() {
        return outerTest().getSampleValues();
    }

    @Override
    public V[] getOtherElements() {
        return outerTest().getOtherValues();
    }

    @Override
    public Collection<V> makeObject() {
        return outerTest().makeObject().values();
    }

    @Override
    public Collection<V> makeFullCollection() {
        return outerTest().makeFullMap().values();
    }

    @Override
    public boolean isNullSupported() {
        return outerTest().isAllowNullKey();
    }

    @Override
    public boolean isAddSupported() {
        return false;
    }

    @Override
    public boolean isRemoveSupported() {
        return outerTest().isRemoveSupported();
    }

    @Override
    public boolean isTestSerialization() {
        return false;
    }

    @Override
    public boolean areEqualElementsDistinguishable() {
        // equal values are associated with different keys, so they are
        // distinguishable.
        return true;
    }

    @Override
    public Collection<V> makeConfirmedCollection() {
        // never gets called, reset methods are overridden
        return null;
    }

    @Override
    public Collection<V> makeConfirmedFullCollection() {
        // never gets called, reset methods are overridden
        return null;
    }

    @Override
    public void resetFull() {
        mapTest().resetFull();
        setCollection(getMap().values());
        setConfirmed(getConfirmedMap().values());
    }

    @Override
    public void resetEmpty() {
        mapTest().resetEmpty();
        setCollection(getMap().values());
        setConfirmed(getConfirmedMap().values());
    }

    @Override
    public void verify() {
        super.verify();
        mapTest().verify();
    }

    @Override
    protected int getIterationBehaviour() {
        return outerTest().getIterationBehaviour();
    }
}
