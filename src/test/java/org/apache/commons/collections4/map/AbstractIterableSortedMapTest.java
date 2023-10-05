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

import org.apache.commons.collections4.BulkTest;
import org.apache.commons.collections4.CollectionCommonsRole;
import org.apache.commons.collections4.IterableMap;
import org.apache.commons.collections4.IterableSortedMap;
import org.apache.commons.collections4.collection.IterationBehaviour;
import org.junit.jupiter.api.Nested;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

public abstract class AbstractIterableSortedMapTest<K, V> extends AbstractSortedMapTest<K, V> {
    public AbstractIterableSortedMapTest(final String testName) {
        super(testName);
    }

    @Override
    public abstract IterableSortedMap<K, V> makeObject();

    @Nested
    public class NestedIterableTests extends AbstractIterableMapTest<K, V> {
        public NestedIterableTests() {
            super("NestedIterableTests");
        }

        @Override
        public IterableMap<K, V> makeObject() {
            return AbstractIterableSortedMapTest.this.makeObject();
        }

        @Override
        public K[] getSampleKeys() {
            return AbstractIterableSortedMapTest.this.getSampleKeys();
        }
        @Override
        public V[] getSampleValues() {
            return AbstractIterableSortedMapTest.this.getSampleValues();
        }
        @Override
        public V[] getNewSampleValues() {
            return AbstractIterableSortedMapTest.this.getNewSampleValues();
        }

        @Override
        protected IterationBehaviour getIterationBehaviour() {
            return AbstractIterableSortedMapTest.this.getIterationBehaviour();
        }
        @Override
        public CollectionCommonsRole collectionRole() {
            return AbstractIterableSortedMapTest.this.collectionRole();
        }
        @Override
        public boolean isAllowNullKey() {
            return false;
        }
        @Override
        public boolean isAllowNullValue() {
            return AbstractIterableSortedMapTest.this.isAllowNullValue();
        }
        @Override
        public boolean isPutAddSupported() {
            return AbstractIterableSortedMapTest.this.isPutAddSupported();
        }
        @Override
        public boolean isPutChangeSupported() {
            return AbstractIterableSortedMapTest.this.isPutChangeSupported();
        }
        @Override
        public boolean isRemoveSupported() {
            return AbstractIterableSortedMapTest.this.isRemoveSupported();
        }
    }

}
