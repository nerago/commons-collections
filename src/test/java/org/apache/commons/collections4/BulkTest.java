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

import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.jupiter.api.*;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.util.ReflectionUtils;

import java.net.URI;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A {@link TestCase} that can define both simple and bulk test methods.
 * <p>
 * A <I>simple test method</I> is the type of test traditionally
 * supplied by {@link TestCase}.  To define a simple test, create a public
 * no-argument method whose name starts with "test".  You can specify
 * the name of simple test in the constructor of {@code BulkTest};
 * a subsequent call to {@link TestCase#run} will run that simple test.
 * <p>
 * A <I>bulk test method</I>, on the other hand, returns a new instance
 * of {@code BulkTest}, which can itself define new simple and bulk
 * test methods.  By using the {@link #makeSuite} method, you can
 * automatically create a hierarchical suite of tests and child bulk tests.
 * <p>
 * For instance, consider the following two classes:
 *
 * <Pre>
 *  public class SetTest extends BulkTest {
 *
 *      private Set set;
 *
 *      public SetTest(Set set) {
 *          this.set = set;
 *      }
 *
 *      @Test
 *      public void testContains() {
 *          boolean r = set.contains(set.iterator().next()));
 *          assertTrue("Set should contain first element, r);
 *      }
 *
 *      @Test
 *      public void testClear() {
 *          set.clear();
 *          assertTrue("Set should be empty after clear", set.isEmpty());
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
 *      @Test
 *      public void testClear() {
 *          Map map = makeFullMap();
 *          map.clear();
 *          assertTrue("Map empty after clear", map.isEmpty());
 *      }
 *
 *      public BulkTest bulkTestKeySet() {
 *          return new SetTest(makeFullMap().keySet());
 *      }
 *
 *      public BulkTest bulkTestEntrySet() {
 *          return new SetTest(makeFullMap().entrySet());
 *      }
 *  }
 *  </Pre>
 *
 *  In the above examples, {@code SetTest} defines two
 *  simple test methods and no bulk test methods; {@code HashMapTest}
 *  defines one simple test method and two bulk test methods.  When
 *  {@code makeSuite(HashMapTest.class).run} is executed,
 *  <I>five</I> simple test methods will be run, in this order:<P>
 *
 *  <Ol>
 *  <Li>HashMapTest.testClear()
 *  <Li>HashMapTest.bulkTestKeySet().testContains();
 *  <Li>HashMapTest.bulkTestKeySet().testClear();
 *  <Li>HashMapTest.bulkTestEntrySet().testContains();
 *  <Li>HashMapTest.bulkTestEntrySet().testClear();
 *  </Ol>
 *
 *  In the graphical junit test runners, the tests would be displayed in
 *  the following tree:<P>
 *
 *  <UL>
 *  <LI>HashMapTest</LI>
 *      <UL>
 *      <LI>testClear
 *      <LI>bulkTestKeySet
 *          <UL>
 *          <LI>testContains
 *          <LI>testClear
 *          </UL>
 *      <LI>bulkTestEntrySet
 *          <UL>
 *          <LI>testContains
 *          <LI>testClear
 *          </UL>
 *      </UL>
 *  </UL>
 *
 *  A subclass can override a superclass's bulk test by
 *  returning {@code null} from the bulk test method.
 *
 *  Note that if you want to use the bulk test methods, you <I>must</I>
 *  define a method annotated with {@code TestFactory} and call {@link #makeSuite}.
 *  The ordinary {@link TestSuite} constructor doesn't know how to
 *  interpret bulk test methods.
 */
public class BulkTest {
    /** Path to test data resources */
    protected static final String TEST_DATA_PATH = "src/test/resources/org/apache/commons/collections4/data/test/";

    /** Path to test properties resources */
    public static final String TEST_PROPERTIES_PATH = "src/test/resources/org/apache/commons/collections4/properties/";

    public <N> DynamicNode getDynamicTests() {
        final Class<? extends BulkTest> type = getClass();
        final BulkTest instance = this;
        return DynamicContainer.dynamicContainer(type.getSimpleName(),
                URI.create("class:" + type.getName()),
                Stream.concat(
                    AnnotationSupport.findAnnotatedMethods(type, Test.class, HierarchyTraversalMode.TOP_DOWN)
                            .stream()
                            .filter(method -> !AnnotationSupport.isAnnotated(method, Disabled.class))
                            .map(method -> DynamicTest.dynamicTest(
                                    method.getName(),
                                    URI.create("method:" + ReflectionUtils.getFullyQualifiedMethodName(type, method)),
                                    () -> ReflectionUtils.invokeMethod(method, instance))),
                    AnnotationSupport.findAnnotatedMethods(type, TestFactory.class, HierarchyTraversalMode.TOP_DOWN)
                            .stream()
                            .map(method -> (DynamicNode) ReflectionUtils.invokeMethod(method, instance))
                            )
        );
    }

    public DynamicNode getDynamicTests(BooleanSupplier enableTests) {
        if (enableTests.getAsBoolean()) {
            return getDynamicTests();
        } else {
            return DynamicContainer.dynamicContainer(getClass().getSimpleName(), Stream.empty());
        }
    }
}
