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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Base class for all test classes that can define both simple and bulk test methods.
 * <p>
 * A <I>simple test method</I> is the type of test
 * in a regular method annotated by {@link Test}.
 * <p>
 * A <I>bulk test</I>, on the other hand, is an inner class (non-static) that also derives (probably) indirectly from {@code BulkTest},
 * is annotated on the class definition with {@link Nested}, and which can itself define new simple and bulk tests.
 * By using this method, you can automatically create a hierarchical suite of tests and child bulk tests.
 * <p>
 * For instance, consider the following two classes:
 *
 * <Pre>
 *  public abstract class SetTest extends BulkTest {
 *
 *      public abstract Set makeSet();
 *
 *      &#60;Test
 *      public void testContains() {
 *          Set set = makeSet();
 *          boolean r = set.contains(set.iterator().next());
 *          assertTrue(r, "Set should contain first element");
 *      }
 *
 *      &#60;Test
 *      public void testClear() {
 *          Set set = makeSet();
 *          set.clear();
 *          assertTrue(set.isEmpty(), "Set should be empty after clear");
 *      }
 *  }
 *
 *
 *  public class HashMapTest extends BulkTest {
 *
 *      private Map makeFullMap() {
 *          HashMap result = new HashMap();
 *          result.put("1", "One");
 *          result.put("2", "Two");
 *          return result;
 *      }
 *
 *      &#60;Test
 *      public void testClear() {
 *          Map map = makeFullMap();
 *          map.clear();
 *          assertTrue(map.isEmpty(), "Map empty after clear");
 *      }
 *
 *      &#60;Nested
 *      public class TestKeySet extends SetTest {
 *          public Set makeSet() {
 *              return makeFullMap().keySet();
 *          }
 *      }
 *
 *      &#60;Nested
 *      public class TestEntrySet extends SetTest {
 *          public Set makeSet() {
 *              return makeFullMap().entrySet();
 *          }
 *      }
 *  }
 *  </Pre>
 *
 *  In the above examples, {@code SetTest} defines two
 *  simple test methods and no bulk tests; {@code HashMapTest}
 *  defines one simple test method and two nested test classes.  When
 *  the tests are run, <I>five</I> simple test methods will be run, in undefined order:
 *
 *  <Ol>
 *  <Li>HashMapTest.testClear()
 *  <Li>HashMapTest.new TestKeySet().testContains();
 *  <Li>HashMapTest.new TestKeySet().testClear();
 *  <Li>HashMapTest.new TestEntrySet().testContains();
 *  <Li>HashMapTest.new TestEntrySet().testClear();
 *  </Ol>
 *
 *  In the graphical junit test runners, the tests would be displayed in
 *  the following tree:<P>
 *
 *  <UL>
 *  <LI>HashMapTest</LI>
 *      <UL>
 *      <LI>testClear
 *      <LI>TestKeySet
 *          <UL>
 *          <LI>testContains
 *          <LI>testClear
 *          </UL>
 *      <LI>TestEntrySet
 *          <UL>
 *          <LI>testContains
 *          <LI>testClear
 *          </UL>
 *      </UL>
 *  </UL>
 *
 *  A subclass can override a superclass's bulk test by
 *  defining its own inner class with the same name.
 */
public class BulkTest implements Cloneable {

    // Note:  BulkTest is Cloneable to make it easier to construct
    // BulkTest instances for simple test methods that are defined in
    // anonymous inner classes.  Basically we don't have to worry about
    // finding weird constructors.  (And even if we found them, technically
    // it'd be illegal for anyone but the outer class to invoke them).
    // Given one BulkTest instance, we can just clone it and reset the
    // method name for every simple test it defines.

    /** Path to test data resources */
    protected static final String TEST_DATA_PATH = "src/test/resources/org/apache/commons/collections4/data/test/";

    /** Path to test properties resources */
    public static final String TEST_PROPERTIES_PATH = "src/test/resources/org/apache/commons/collections4/properties/";

    /**
     *  Creates a clone of this {@code BulkTest}.<P>
     *
     *  @return  a clone of this {@code BulkTest}
     */
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new Error(); // should never happen
        }
    }
}
