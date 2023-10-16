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

import org.apache.commons.collections4.CollectionCommonsRole;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.OrderedMap;
import org.apache.commons.collections4.ResettableIterator;
import org.apache.commons.collections4.collection.IterationBehaviour;
import org.apache.commons.collections4.list.AbstractListTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * JUnit tests.
 */
public class BinaryTreeMapTest<K extends Comparable<K>, V> extends AbstractIterableSortedMapTest<K, V, BinaryTreeMap<K, V>> {

    @Override
    public BinaryTreeMap<K, V> makeObject() {
        return new BinaryTreeMap<>();
    }

    @Override
    public CollectionCommonsRole collectionRole() {
        return CollectionCommonsRole.CONCRETE;
    }

    @Override
    protected IterationBehaviour getIterationBehaviour() {
        return IterationBehaviour.STABLE_SEQUENCE;
    }

    @Override
    public String getCompatibilityVersion() {
        return "4";
    }

//    public void testCreate() throws Exception {
//        resetEmpty();
//        writeExternalFormToDisk((java.io.Serializable) map, "src/test/resources/data/test/BinaryTreeMap.emptyCollection.version4.obj");
//        resetFull();
//        writeExternalFormToDisk((java.io.Serializable) map, "src/test/resources/data/test/BinaryTreeMap.fullCollection.version4.obj");
//    }

}
