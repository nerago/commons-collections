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
package org.apache.commons.collections4.set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionCommonsRole;
import org.apache.commons.collections4.collection.IterationBehaviour;
import org.apache.commons.collections4.set.CompositeSet.SetMutator;
import org.junit.jupiter.api.Test;

/**
 * Extension of {@link AbstractSetTest} for exercising the
 * {@link CompositeSet} implementation.
 *
 * @since 3.0
 */
public class CompositeSetTest<E> extends AbstractSetTest<E> {

    public CompositeSetTest() {
        super(CompositeSetTest.class.getSimpleName());
    }

    @Override
    public CompositeSet<E> makeObject() {
        final HashSet<E> contained = new HashSet<>();
        final CompositeSet<E> set = new CompositeSet<>(contained);
        set.setMutator( new EmptySetMutator<>(contained) );
        return set;
    }

    @SuppressWarnings("unchecked")
    public Set<E> buildOne() {
        final HashSet<E> set = new HashSet<>();
        set.add((E) "1");
        set.add((E) "2");
        return set;
    }

    @SuppressWarnings("unchecked")
    public Set<E> buildTwo() {
        final HashSet<E> set = new HashSet<>();
        set.add((E) "3");
        set.add((E) "4");
        return set;
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testContains() {
        final CompositeSet<E> set = new CompositeSet<>(buildOne(), buildTwo());
        assertTrue(set.contains("1"));
        assertTrue(set.contains("3"));
        assertFalse(set.contains("5"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testContainsAll() {
        final CompositeSet<E> set = new CompositeSet<>(buildOne(), buildTwo());
        assertTrue(set.containsAll(Arrays.asList()));
        assertTrue(set.containsAll(Arrays.asList("1")));
        assertTrue(set.containsAll(Arrays.asList("1", "3")));
        assertFalse(set.containsAll(Arrays.asList("1", "5")));
        assertThrows(NullPointerException.class, () -> set.containsAll(null));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRemoveAll() {
        final CompositeSet<E> set = new CompositeSet<>(buildOne(), buildTwo());
        assertFalse(set.removeAll(Arrays.asList()));
        assertEquals(4, set.size());
        assertTrue(set.removeAll(Arrays.asList("1")));
        assertEquals(3, set.size());
        assertTrue(set.removeAll(Arrays.asList("1", "3")));
        assertEquals(2, set.size());
        assertFalse(set.removeAll(Arrays.asList("3")));
        assertEquals(2, set.size());
        assertFalse(set.removeAll(Arrays.asList("1", "5")));
        assertThrows(NullPointerException.class, () -> set.removeAll(null));
        assertEquals(2, set.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRemoveUnderlying() {
        final Set<E> one = buildOne();
        final Set<E> two = buildTwo();
        final CompositeSet<E> set = new CompositeSet<>(one, two);
        one.remove("1");
        assertFalse(set.contains("1"));

        two.remove("3");
        assertFalse(set.contains("3"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRemoveComposited() {
        final Set<E> one = buildOne();
        final Set<E> two = buildTwo();
        final CompositeSet<E> set = new CompositeSet<>(one, two);
        set.remove("1");
        assertFalse(one.contains("1"));

        set.remove("3");
        assertFalse(one.contains("3"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFailedCollisionResolution() {
        final Set<E> one = buildOne();
        final Set<E> two = buildTwo();
        final CompositeSet<E> set = new CompositeSet<>(one, two);
        set.setMutator(new SetMutator<E>() {
            private static final long serialVersionUID = 1L;

            @Override
            public void resolveCollision(final CompositeSet<E> comp, final Set<E> existing,
                final Set<E> added, final Collection<E> intersects) {
                //noop
            }

            @Override
            public boolean add(final CompositeSet<E> composite,
                    final List<Set<E>> collections, final E obj) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean addAll(final CompositeSet<E> composite,
                    final List<Set<E>> collections, final Collection<? extends E> coll) {
                throw new UnsupportedOperationException();
            }
        });

        final HashSet<E> three = new HashSet<>();
        three.add((E) "1");
        assertThrows(IllegalArgumentException.class, () -> set.addComposited(three),
                "IllegalArgumentException should have been thrown");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAddComposited() {
        final Set<E> one = buildOne();
        final Set<E> two = buildTwo();
        final CompositeSet<E> set = new CompositeSet<>();
        set.addComposited(one, two);
        set.addComposited((Set<E>) null);
        set.addComposited((Set<E>[]) null);
        set.addComposited(null, null);
        set.addComposited(null, null, null);
        final CompositeSet<E> set2 = new CompositeSet<>(buildOne());
        set2.addComposited(buildTwo());
        assertEquals(set, set2);
        final HashSet<E> set3 = new HashSet<>();
        set3.add((E) "1");
        set3.add((E) "2");
        set3.add((E) "3");
        final HashSet<E> set4 = new HashSet<>();
        set4.add((E) "4");
        final CompositeSet<E> set5 = new CompositeSet<>(set3);
        set5.addComposited(set4);
        assertEquals(set, set5);
        assertThrows(UnsupportedOperationException.class, () -> set.addComposited(set3),
                "Expecting UnsupportedOperationException.");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAddCompositedCollision() {
        final HashSet<E> set1 = new HashSet<>();
        set1.add((E) "1");
        set1.add((E) "2");
        set1.add((E) "3");
        final HashSet<E> set2 = new HashSet<>();
        set2.add((E) "4");
        final CompositeSet<E> set3 = new CompositeSet<>(set1);
        assertThrows(UnsupportedOperationException.class, () -> set3.addComposited(set1, buildOne()),
                "Expecting UnsupportedOperationException.");
        assertThrows(UnsupportedOperationException.class, () -> set3.addComposited(set1, buildOne(), buildTwo()),
                "Expecting UnsupportedOperationException.");
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
        return CollectionCommonsRole.COMPOSITE;
    }

//    public void testCreate() throws Exception {
//        resetEmpty();
//        writeExternalFormToDisk((java.io.Serializable) getCollection(), "src/test/resources/data/test/CompositeSet.emptyCollection.version4.obj");
//        resetFull();
//        writeExternalFormToDisk((java.io.Serializable) getCollection(), "src/test/resources/data/test/CompositeSet.fullCollection.version4.obj");
//    }

}
