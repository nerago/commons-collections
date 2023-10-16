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
package org.apache.commons.collections4.bidimap;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.CollectionCommonsRole;
import org.apache.commons.collections4.collection.IterationBehaviour;

/**
 * JUnit tests.
 */
public class DualHashBidiMapTest<K, V> extends AbstractBidiMapTest<K, V, BidiMap<K, V, ?>> {

    /**
     * {@inheritDoc}
     */
    @Override
    public BidiMap<K, V, ?> makeObject() {
        return new DualHashBidiMap<>();
    }

    @Override
    protected IterationBehaviour getIterationBehaviour() {
        return IterationBehaviour.CONSISTENT_SEQUENCE_UNTIL_MODIFY;
    }

    @Override
    public CollectionCommonsRole collectionRole() {
        return CollectionCommonsRole.CONCRETE;
    }

    @Override
    public boolean isFailFastFunctionalExpected() {
        return false;
    }

//    public void testCreate() throws Exception {
//        resetEmpty();
//        writeExternalFormToDisk((java.io.Serializable) map, "src/test/resources/data/test/DualHashBidiMap.emptyCollection.version4.obj");
//        resetFull();
//        writeExternalFormToDisk((java.io.Serializable) map, "src/test/resources/data/test/DualHashBidiMap.fullCollection.version4.obj");
//    }

}
