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

import java.util.Arrays;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

/**
 * Abstract test class for {@link NavigableSet} methods and contracts.
 * <p>
 * To use, subclass and override the {@link #makeObject()}
 * method.  You may have to override other protected methods if your
 * set is not modifiable, or if your set restricts what kinds of
 * elements may be added; see {@link AbstractSetTest} for more details.
 *
 * @since 4.1
 */
public abstract class AbstractNavigableSetTest<E> extends AbstractSortedSetTest<E> {

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract NavigableSet<E> makeObject();

    /**
     * {@inheritDoc}
     */
    @Override
    public NavigableSet<E> makeFullCollection() {
        return (NavigableSet<E>) super.makeFullCollection();
    }

    /**
     * Returns an empty {@link TreeSet} for use in modification testing.
     *
     * @return a confirmed empty collection
     */
    @Override
    public NavigableSet<E> makeConfirmedCollection() {
        return new TreeSet<>();
    }


    /**
     * Verification extension, will check the order of elements,
     * the sets should already be verified equal.
     */
    @Override
    public void verify() {
        super.verify();

        // Check that descending iterator returns elements in order and higher(), lower(),
        // floor() and ceiling() are consistent
        final Iterator<E> collIter = getCollection().descendingIterator();
        final Iterator<E> confIter = getConfirmed().descendingIterator();
        while (collIter.hasNext()) {
            final E element = collIter.next();
            final E confElement = confIter.next();
            assertEquals(confElement, element, "Element appears to be out of order.");

            assertEquals(getConfirmed().higher(element),
                    getCollection().higher(element), "Incorrect element returned by higher().");

            assertEquals(getConfirmed().lower(element),
                    getCollection().lower(element), "Incorrect element returned by lower().");

            assertEquals(getConfirmed().floor(element),
                    getCollection().floor(element), "Incorrect element returned by floor().");

            assertEquals(getConfirmed().ceiling(element),
                    getCollection().ceiling(element), "Incorrect element returned by ceiling().");
        }
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

    @Override
    @TestFactory
    public DynamicNode subSetTests() {
        if (runSubSetTests()) {
            return DynamicContainer.dynamicContainer("subSetTests", Arrays.asList(
                    new BulkTestSortedSetSubSet().getDynamicTests(),
                    new BulkTestSortedSetHeadSet().getDynamicTests(),
                    new BulkTestSortedSetTailSet().getDynamicTests(),
                    new BulkTestNavigableSetSubSet().getDynamicTests(),
                    new BulkTestNavigableSetHeadSet().getDynamicTests(),
                    new BulkTestNavigableSetTailSet().getDynamicTests()
            ));
        } else {
            return DynamicContainer.dynamicContainer("subSetTests", Stream.empty());
        }
    }

    /**
     * Bulk test {@link NavigableSet#subSet(Object, boolean, Object, boolean)}.
     * This method runs through all of the tests in {@link AbstractNavigableSetTest}.
     * After modification operations, {@link #verify()} is invoked to ensure
     * that the set and the other collection views are still valid.
     */
    public class BulkTestNavigableSetSubSet extends TestNavigableSetSubSet {
        @SuppressWarnings("unchecked")
        public BulkTestNavigableSetSubSet() {
            lowBound = AbstractNavigableSetTest.this.getFullElements().length / 3;
            highBound = AbstractNavigableSetTest.this.getFullElements().length / 3 * 2;

            final int fullLoBound = lowBound + 1;
            final int length = highBound - lowBound - 1;
            fullElements = (E[]) new Object[length];
            System.arraycopy(AbstractNavigableSetTest.this.getFullElements(), fullLoBound, fullElements, 0, length);
            final int otherLength = highBound - lowBound;
            otherElements = (E[]) new Object[otherLength - 1];
            System.arraycopy(//src src_pos dst dst_pos length
                    AbstractNavigableSetTest.this.getOtherElements(), lowBound, otherElements, 0, otherLength - 1);
        }

        @Override
        protected NavigableSet<E> getSubSet(NavigableSet<E> set) {
            final E[] elements = AbstractNavigableSetTest.this.getFullElements();
            return set.subSet(elements[lowBound], false, elements[highBound], false);
        }
    }

    /**
     * Bulk test {@link NavigableSet#headSet(Object, boolean)}.
     * This method runs through all of the tests in {@link AbstractNavigableSetTest}.
     * After modification operations, {@link #verify()} is invoked to ensure
     * that the set and the other collection views are still valid.
     */
    public class BulkTestNavigableSetHeadSet extends TestNavigableSetSubSet {
        @SuppressWarnings("unchecked")
        public BulkTestNavigableSetHeadSet() {
            highBound = AbstractNavigableSetTest.this.getFullElements().length / 3 * 2;

            final int realBound = highBound + 1;
            fullElements = (E[]) new Object[realBound];
            System.arraycopy(AbstractNavigableSetTest.this.getFullElements(), 0, fullElements, 0, realBound);
            otherElements = (E[]) new Object[highBound - 1];
            System.arraycopy(//src src_pos dst dst_pos length
                    AbstractNavigableSetTest.this.getOtherElements(), 0, otherElements, 0, highBound - 1);
        }

        @Override
        protected NavigableSet<E> getSubSet(NavigableSet<E> set) {
            final E[] elements = AbstractNavigableSetTest.this.getFullElements();
            return set.headSet(elements[highBound], true);
        }
    }

    /**
     * Bulk test {@link NavigableSet#tailSet(Object, boolean)}.
     * This method runs through all of the tests in {@link AbstractNavigableSetTest}.
     * After modification operations, {@link #verify()} is invoked to ensure
     * that the set and the other collection views are still valid.
     */
    public class BulkTestNavigableSetTailSet extends TestNavigableSetSubSet {
        @SuppressWarnings("unchecked")
        public BulkTestNavigableSetTailSet() {
            lowBound = AbstractNavigableSetTest.this.getFullElements().length / 3;
            final E[] allElements = AbstractNavigableSetTest.this.getFullElements();
            final int realBound = lowBound + 1;
            fullElements = (E[]) new Object[allElements.length - realBound];
            System.arraycopy(allElements, realBound, fullElements, 0, allElements.length - realBound);
            otherElements = (E[]) new Object[allElements.length - lowBound - 1];
            System.arraycopy(//src src_pos dst dst_pos length
                    AbstractNavigableSetTest.this.getOtherElements(), lowBound, otherElements, 0, allElements.length - lowBound - 1);
        }

        @Override
        protected NavigableSet<E> getSubSet(NavigableSet<E> set) {
            final E[] elements = AbstractNavigableSetTest.this.getFullElements();
            return set.tailSet(elements[lowBound], false);
        }
    }

    public abstract class TestNavigableSetSubSet extends AbstractNavigableSetTest<E> {

        protected int lowBound;
        protected int highBound;
        protected E[] fullElements;
        protected E[] otherElements;

        @Override
        public boolean isNullSupported() {
            return AbstractNavigableSetTest.this.isNullSupported();
        }
        @Override
        public boolean isAddSupported() {
            return AbstractNavigableSetTest.this.isAddSupported();
        }
        @Override
        public boolean isRemoveSupported() {
            return AbstractNavigableSetTest.this.isRemoveSupported();
        }
        @Override
        public boolean isFailFastSupported() {
            return AbstractNavigableSetTest.this.isFailFastSupported();
        }
        @Override
        protected int getIterationBehaviour() {
            return 0;
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

        protected abstract NavigableSet<E> getSubSet(final NavigableSet<E> set);

        @Override
        public NavigableSet<E> makeObject() {
            return getSubSet(AbstractNavigableSetTest.this.makeObject());
        }

        @Override
        public NavigableSet<E> makeFullCollection() {
            return getSubSet(AbstractNavigableSetTest.this.makeFullCollection());
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
    public NavigableSet<E> getCollection() {
        return (NavigableSet<E>) super.getCollection();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NavigableSet<E> getConfirmed() {
        return (NavigableSet<E>) super.getConfirmed();
    }

}
