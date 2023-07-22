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
package org.apache.commons.collections4.multiset;

import org.apache.commons.collections4.CollectionCommonsRole;
import org.apache.commons.collections4.MultiSet;
import org.apache.commons.collections4.collection.IterationBehaviour;

/**
 * Extension of {@link AbstractMultiSetTest} for exercising the
 * {@link SynchronizedMultiSet} implementation.
 *
 * @since 4.1
 */
public class SynchronizedMultiSetTest<T> extends AbstractMultiSetTest<T> {

    public SynchronizedMultiSetTest() {
        super(SynchronizedMultiSetTest.class.getSimpleName());
    }

    @Override
    public MultiSet<T> makeObject() {
        return SynchronizedMultiSet.synchronizedMultiSet(new HashMultiSet<T>());
    }

    @Override
    public String getCompatibilityVersion() {
        return "4.1";
    }

    @Override
    protected IterationBehaviour getIterationBehaviour() {
        return IterationBehaviour.UNKNOWN;
    }

    @Override
    public CollectionCommonsRole collectionRole() {
        return CollectionCommonsRole.SYNCHRONIZED;
    }

//    public void testCreate() throws Exception {
//        MultiSet<T> multiset = makeObject();
//        writeExternalFormToDisk((java.io.Serializable) multiset, "src/test/resources/data/test/SynchronizedMultiSet.emptyCollection.version4.1.obj");
//        multiset = makeFullCollection();
//        writeExternalFormToDisk((java.io.Serializable) multiset, "src/test/resources/data/test/SynchronizedMultiSet.fullCollection.version4.1.obj");
//    }

}
