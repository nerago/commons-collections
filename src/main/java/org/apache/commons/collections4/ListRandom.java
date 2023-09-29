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

import org.apache.commons.collections4.bag.HashBag;
import org.apache.commons.collections4.functors.DefaultEquator;
import org.apache.commons.collections4.list.FixedSizeList;
import org.apache.commons.collections4.list.LazyList;
import org.apache.commons.collections4.list.PredicatedList;
import org.apache.commons.collections4.list.TransformedList;
import org.apache.commons.collections4.list.UnmodifiableList;
import org.apache.commons.collections4.sequence.CommandVisitor;
import org.apache.commons.collections4.sequence.EditScript;
import org.apache.commons.collections4.sequence.SequencesComparator;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class ListRandom<E> {
    private final List<E> list;
    private Random random;

    public ListRandom(final List<E> list) {
        this(list, new Random());

    }

    public ListRandom(final List<E> list, final Random random) {
        if (list.size() < 1) {
            throw new IllegalArgumentException();
        }
        this.list = list;
        this.random = random;
    }

    public E chooseOne() {
        return list.get(random.nextInt(list.size()));
    }

    public List<E> chooseMany(final int count) {
        if (count <= 0) {
            throw new IllegalArgumentException();
        } else if (count == 1) {
            return Collections.singletonList(chooseOne());
        } else {
            final List<E> result = new ArrayList<>(count);
            final int listSize = list.size();
            for (int i = 0; i < count; ++i) {
                result.add(list.get(random.nextInt(listSize)));
            }
            return result;
        }
    }

    public List<E> chooseManyDistinct(final int count) {
        final int listSize = list.size();
        if (count <= 0 || count > listSize) {
            throw new IllegalArgumentException();
        } else if (count == 1) {
            return Collections.singletonList(chooseOne());
        } else {
            final List<E> result = new ArrayList<>(count);
            final Set<Integer> indices = new HashSet<>();
            for (int i = 0; i < count; ++i) {
                while (true) {
                    final int index = random.nextInt(listSize);
                    if (indices.add(index)) {
                        result.add(list.get(index));
                        break;
                    }
                }
            }
            return result;
        }
    }

    public List<E> original() {
        return UnmodifiableList.unmodifiableList(list);
    }

    public List<E> shuffled() {
        final List<E> result = new ArrayList<>(list);
        for (int i = result.size() - 1; i > 0; i--) {
            final E swap = result.get(i);
            final int j = random.nextInt(i + 1);
            result.set(i, result.get(j));
            result.set(j, swap);
        }
        return result;
    }
}
