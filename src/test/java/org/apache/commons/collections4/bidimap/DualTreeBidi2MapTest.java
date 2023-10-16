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

import org.apache.commons.collections4.CollectionCommonsRole;
import org.apache.commons.collections4.SortedBidiMap;
import org.junit.jupiter.api.Test;

/**
 * JUnit tests.
 */
public class DualTreeBidi2MapTest<K extends Comparable<K>, V extends Comparable<V>>
        extends AbstractSortedBidiMapTest<K, V, SortedBidiMap<K, V, ?, ?>> {

    @Override
    public DualTreeBidi2Map<K, V> makeObject() {
        return new DualTreeBidi2Map<>();
    }

    @Override
    public CollectionCommonsRole collectionRole() {
        return CollectionCommonsRole.CONCRETE;
    }

}
