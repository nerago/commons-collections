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

import org.apache.commons.collections4.set.AbstractSetTest;

import java.util.Map;
import java.util.Set;

public abstract class AbstractNestedMapKeySetTest<K, V> extends AbstractSetTest<K> {

    public abstract AbstractMapTest<K, V> outerTest();

    public abstract AbstractMapTest<K, V>.MapTest mapTest();

    public Map<K, V> getMap() {
        return mapTest().getMap();
    }

    public Map<K, V> getConfirmedMap() {
        return mapTest().getConfirmed();
    }

    @Override
    public K[] getFullElements() {
        return outerTest().getSampleKeys();
    }

    @Override
    public K[] getOtherElements() {
        return outerTest().getOtherKeys();
    }

    @Override
    public Set<K> makeObject() {
        return outerTest().makeObject().keySet();
    }

    @Override
    public Set<K> makeFullCollection() {
        return outerTest().makeFullMap().keySet();
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
    public void resetEmpty() {
        mapTest().resetEmpty();
        setCollection(getMap().keySet());
        setConfirmed(getConfirmedMap().keySet());
    }

    @Override
    public void resetFull() {
        mapTest().resetFull();
        setCollection(getMap().keySet());
        setConfirmed(getConfirmedMap().keySet());
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
