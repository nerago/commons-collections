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
package org.apache.commons.collections4.bag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.CollectionCommonsRole;
import org.apache.commons.collections4.collection.IterationBehaviour;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link CollectionBag}.
 * <p>
 * Note: This test is mainly for serialization support, the CollectionBag decorator
 * is extensively used and tested in AbstractBagTest.
 *
 * @since 4.0
 */
public class CollectionBagTest<T> extends AbstractCollectionBagTest<T> {

    /**
     * JUnit constructor.
     */
    public CollectionBagTest() {
        super(CollectionBagTest.class.getSimpleName());
    }


    @Override
    public Bag<T> makeObject() {
        return CollectionBag.collectionBag(new HashBag<T>());
    }

    /**
     * Returns an empty List for use in modification testing.
     *
     * @return a confirmed empty collection
     */
    @Override
    public Collection<T> makeConfirmedCollection() {
        return new ArrayList<>();
    }

    /**
     * Returns a full Set for use in modification testing.
     *
     * @return a confirmed full collection
     */
    @Override
    public Collection<T> makeConfirmedFullCollection() {
        final Collection<T> set = makeConfirmedCollection();
        set.addAll(Arrays.asList(getFullElements()));
        return set;
    }

    @Override
    public String getCompatibilityVersion() {
        return "4";
    }

    @Override
    protected IterationBehaviour getIterationBehaviour() {
        return IterationBehaviour.UNORDERED;
    }

    @Override
    public CollectionCommonsRole collectionRole() {
        return CollectionCommonsRole.OTHER_DECORATOR;
    }

    //    public void testCreate() throws Exception {
//        resetEmpty();
//        writeExternalFormToDisk((java.io.Serializable) getCollection(), "src/test/resources/data/test/CollectionBag.emptyCollection.version4.obj");
//        resetFull();
//        writeExternalFormToDisk((java.io.Serializable) getCollection(), "src/test/resources/data/test/CollectionBag.fullCollection.version4.obj");
//    }

}
