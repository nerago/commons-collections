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
package org.apache.commons.collections4;

import java.util.NavigableSet;

public interface NavigableRangedSet<E, TSubSet extends NavigableRangedSet<E, ?>>
        extends NavigableSet<E>, SortedRangedSet<E, TSubSet> {
    @Override
    default TSubSet subSet(final E fromElement, final boolean fromInclusive, final E toElement, final boolean toInclusive) {
        return subSet(getRange().subRange(fromElement,fromInclusive,toElement,toInclusive));
    }

    @Override
    default TSubSet headSet(final E toElement, final boolean inclusive) {
        return subSet(getRange().head(toElement));
    }

    @Override
    default TSubSet tailSet(final E fromElement, final boolean inclusive) {
        return subSet(getRange().tail(fromElement));
    }

    @Override
    default TSubSet subSet(final E fromElement, final E toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    default TSubSet headSet(final E toElement) {
        return headSet(toElement, false);
    }

    @Override
    default TSubSet tailSet(final E fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    default TSubSet reversed() {
        return descendingSet();
    }

    @Override
    TSubSet descendingSet();
}
