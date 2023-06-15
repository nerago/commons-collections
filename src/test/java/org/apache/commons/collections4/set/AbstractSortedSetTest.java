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

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.condition.EnabledIf;

/**
 * Abstract test class for {@link SortedSet} methods and contracts.
 * <p>
 * To use, subclass and override the {@link #makeObject()}
 * method.  You may have to override other protected methods if your
 * set is not modifiable, or if your set restricts what kinds of
 * elements may be added; see {@link AbstractSetTest} for more details.
 *
 * @since 3.0
 */
public abstract class AbstractSortedSetTest<E> extends AbstractSetTest<E> {

    /**
     * JUnit constructor.
     *
     * @param name  name for test
     */
    public AbstractSortedSetTest(final String name) {
        super(name);
    }

    /**
     * Verification extension, will check the order of elements,
     * the sets should already be verified equal.
     */
    @Override
    public void verify() {
        super.verify();

        // Check that iterator returns elements in order and first() and last()
        // are consistent
        final Iterator<E> collIter = getCollection().iterator();
        final Iterator<E> confIter = getConfirmed().iterator();
        E first = null;
        E last = null;
        while (collIter.hasNext()) {
            if (first == null) {
                first = collIter.next();
                last = first;
            } else {
                last = collIter.next();
            }
            assertEquals(last, confIter.next(), "Element appears to be out of order.");
        }
        if (!getCollection().isEmpty()) {
            assertEquals(first,
                getCollection().first(), "Incorrect element returned by first().");
            assertEquals(last,
                getCollection().last(), "Incorrect element returned by last().");
        }
    }

    /**
     * Overridden because SortedSets don't allow null elements (normally).
     * @return false
     */
    @Override
    public boolean isNullSupported() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract SortedSet<E> makeObject();

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<E> makeFullCollection() {
        return (SortedSet<E>) super.makeFullCollection();
    }

    /**
     * Returns an empty {@link TreeSet} for use in modification testing.
     *
     * @return a confirmed empty collection
     */
    @Override
    public SortedSet<E> makeConfirmedCollection() {
        return new TreeSet<>();
    }

    /**
     * Override to return comparable objects.
     */
    @Override
    @SuppressWarnings("unchecked")
    public E[] getFullNonNullElements() {
        final Object[] elements = new Object[30];

        for (int i = 0; i < 30; i++) {
            elements[i] = Integer.valueOf(i + i + 1);
        }
        return (E[]) elements;
    }

    /**
     * Override to return comparable objects.
     */
    @Override
    @SuppressWarnings("unchecked")
    public E[] getOtherNonNullElements() {
        final Object[] elements = new Object[30];
        for (int i = 0; i < 30; i++) {
            elements[i] = Integer.valueOf(i + i + 2);
        }
        return (E[]) elements;
    }

    protected boolean runSubSetTests() {
        return true;
    }

    /**
     * Bulk test {@link SortedSet#subSet(Object, Object)}.  This method runs through all of
     * the tests in {@link AbstractSortedSetTest}.
     */
    @Nested
    @EnabledIf(value = "runSubSetTests")
    @SuppressWarnings("unchecked")
    public class BulkTestSortedSetSubSet extends TestSortedSetSubSet {
        public BulkTestSortedSetSubSet() {
            super("BulkTestSortedSetSubSet");
            final int loBound = AbstractSortedSetTest.this.getFullElements().length / 3;
            final int hiBound = AbstractSortedSetTest.this.getFullElements().length / 3 * 2;
            lowBound = loBound;
            highBound = hiBound;
            final int length = hiBound - loBound;
            fullElements = (E[]) new Object[length];
            System.arraycopy(AbstractSortedSetTest.this.getFullElements(), loBound, fullElements, 0, length);
            otherElements = (E[]) new Object[length - 1];
            System.arraycopy(//src src_pos dst dst_pos length
                    AbstractSortedSetTest.this.getOtherElements(), loBound, otherElements, 0, length - 1);
        }

        @Override
        protected SortedSet<E> getSubSet(SortedSet<E> set) {
            final E[] elements = AbstractSortedSetTest.this.getFullElements();
            return set.subSet(elements[lowBound], elements[highBound]);
        }
    }

    /**
     * Bulk test {@link SortedSet#headSet(Object)}.  This method runs through all of
     * the tests in {@link AbstractSortedSetTest}.
     */
    @Nested
    @EnabledIf(value = "runSubSetTests")
    @SuppressWarnings("unchecked")
    public class BulkTestSortedSetHeadSet extends TestSortedSetSubSet {
        public BulkTestSortedSetHeadSet() {
            super("BulkTestSortedSetHeadSet");
            final int bound = AbstractSortedSetTest.this.getFullElements().length / 3 * 2;
            highBound = bound;
            fullElements = (E[]) new Object[bound];
            System.arraycopy(AbstractSortedSetTest.this.getFullElements(), 0, fullElements, 0, bound);
            otherElements = (E[]) new Object[bound - 1];
            System.arraycopy(//src src_pos dst dst_pos length
                    AbstractSortedSetTest.this.getOtherElements(), 0, otherElements, 0, bound - 1);
        }

        @Override
        protected SortedSet<E> getSubSet(SortedSet<E> set) {
            final E[] elements = AbstractSortedSetTest.this.getFullElements();
            return set.headSet(elements[lowBound]);
        }
    }

    /**
     * Bulk test {@link SortedSet#tailSet(Object)}.  This method runs through all of
     * the tests in {@link AbstractSortedSetTest}.
     */
    @Nested
    @EnabledIf(value = "runSubSetTests")
    @SuppressWarnings("unchecked")
    public class BulkTestSortedSetTailSet extends TestSortedSetSubSet {
        public BulkTestSortedSetTailSet() {
            super("bulkTestSortedSetTailSet");
            final int bound = AbstractSortedSetTest.this.getFullElements().length / 3;
            lowBound = bound;
            final E[] allElements = AbstractSortedSetTest.this.getFullElements();
            fullElements = (E[]) new Object[allElements.length - bound];
            System.arraycopy(allElements, bound, fullElements, 0, allElements.length - bound);
            otherElements = (E[]) new Object[allElements.length - bound - 1];
            System.arraycopy(//src src_pos dst dst_pos length
                    AbstractSortedSetTest.this.getOtherElements(), bound, otherElements, 0, allElements.length - bound - 1);
        }

        @Override
        protected SortedSet<E> getSubSet(SortedSet<E> set) {
            final E[] elements = AbstractSortedSetTest.this.getFullElements();
            return set.tailSet(elements[lowBound]);
        }
    }

    public abstract class TestSortedSetSubSet extends AbstractSortedSetTest<E> {

        protected int lowBound;
        protected int highBound;
        protected E[] fullElements;
        protected E[] otherElements;

        public TestSortedSetSubSet(String name) {
            super(name);
        }

        @Override
        public boolean isNullSupported() {
            return AbstractSortedSetTest.this.isNullSupported();
        }
        @Override
        public boolean isAddSupported() {
            return AbstractSortedSetTest.this.isAddSupported();
        }
        @Override
        public boolean isRemoveSupported() {
            return AbstractSortedSetTest.this.isRemoveSupported();
        }
        @Override
        public boolean isFailFastSupported() {
            return AbstractSortedSetTest.this.isFailFastSupported();
        }
        @Override
        protected boolean runSubSetTests() {
            return false;
        }

        @Override
        public E[] getFullElements() {
            return fullElements;
        }
        @Override
        public E[] getOtherElements() {
            return otherElements;
        }

        protected abstract SortedSet<E> getSubSet(final SortedSet<E> set);

        @Override
        public SortedSet<E> makeObject() {
            return getSubSet(AbstractSortedSetTest.this.makeObject());
        }

        @Override
        public SortedSet<E> makeFullCollection() {
            return getSubSet(AbstractSortedSetTest.this.makeFullCollection());
        }

        @Override
        public boolean isTestSerialization() {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<E> getCollection() {
        return (SortedSet<E>) super.getCollection();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<E> getConfirmed() {
        return (SortedSet<E>) super.getConfirmed();
    }
}
