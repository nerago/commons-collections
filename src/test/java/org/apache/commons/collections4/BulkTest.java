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
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.engine.support.discovery.EngineDiscoveryRequestResolver;
import org.junit.platform.engine.support.discovery.SelectorResolver;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;

import java.lang.reflect.Method;
import java.util.ArrayList;
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
 *  returning {@code null} from the bulk test method.  If you only
 *  want to override specific simple tests within a bulk test, use the
 *  {@link #ignoredTests} method.<P>
 *
 *  Note that if you want to use the bulk test methods, you <I>must</I>
 *  define your {@code suite()} method to use {@link #makeSuite}.
 *  The ordinary {@link TestSuite} constructor doesn't know how to
 *  interpret bulk test methods.
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
     *  The full name of this bulk test instance.  This is the full name
     *  that is compared to {@link #ignoredTests} to see if this
     *  test should be ignored.  It's also displayed in the text runner
     *  to ease debugging.
     */
    String verboseName;

    /**
     *  the name of the simple test method
     */
    private String name;

    /**
     *  Constructs a new {@code BulkTest} instance that will run the
     *  specified simple test.
     *
     *  @param name  the name of the simple test method to run
     */
    public BulkTest(final String name) {
        this.name = name;
        this.verboseName = getClass().getName();
    }

    /**
     *  Returns the name of the simple test method of this {@code BulkTest}.
     *
     *  @return the name of the simple test method of this {@code BulkTest}
     */
    public String getName() {
        return name;
    }

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

    /**
     *  Returns an array of test names to ignore.<P>
     *
     *  If a test that's defined by this {@code BulkTest} or
     *  by one of its bulk test methods has a name that's in the returned
     *  array, then that simple test will not be executed.<P>
     *
     *  A test's name is formed by taking the class name of the
     *  root {@code BulkTest}, eliminating the package name, then
     *  appending the names of any bulk test methods that were invoked
     *  to get to the simple test, and then appending the simple test
     *  method name.  The method names are delimited by periods:
     *
     *  <pre>
     *  HashMapTest.bulkTestEntrySet.testClear
     *  </pre>
     *
     *  is the name of one of the simple tests defined in the sample classes
     *  described above.  If the sample {@code HashMapTest} class
     *  included this method:
     *
     *  <pre>
     *  public String[] ignoredTests() {
     *      return new String[] { "HashMapTest.bulkTestEntrySet.testClear" };
     *  }
     *  </pre>
     *
     *  then the entry set's clear method wouldn't be tested, but the key
     *  set's clear method would.
     *
     *  @return an array of the names of tests to ignore, or null if
     *   no tests should be ignored
     */
    public String[] ignoredTests() {
        return null;
    }

    /**
     *  Returns the display name of this {@code BulkTest}.
     *
     *  @return the display name of this {@code BulkTest}
     */
    @Override
    public String toString() {
        return getName() + "(" + verboseName + ") ";
    }

    protected <N> DynamicNode findTestsOnNestedClass(Class<N> type, Supplier<N> instanceSupplier) {
//        ClassSource cs = ClassSource.from(type);



        JupiterTestEngine engine = new JupiterTestEngine();
        LauncherDiscoveryRequest discoveryRequest = LauncherDiscoveryRequestBuilder
                .request()
                .selectors(DiscoverySelectors.selectClass(type))
                .build();
        TestDescriptor tests = engine.discover(discoveryRequest, UniqueId.forEngine(engine.getId()));

        ArrayList<? extends DynamicNode> dynamicNodes = new ArrayList<>();
        tests.accept(descriptor -> {
            System.out.println(descriptor.getClass() + " " + (descriptor instanceof TestMethodTestDescriptor));

            if (descriptor instanceof TestMethodTestDescriptor) {
                TestMethodTestDescriptor methodDescriptor = (TestMethodTestDescriptor) descriptor;
                Method method = methodDescriptor.getTestMethod();
                System.out.println(method.getDeclaringClass() + " " + method.getName());
//                DynamicTest test = DynamicTest.dynamicTest(methodDescriptor.getDisplayName(), );
//                dynamicNodes.add(test);
            }

            // JupiterEngineDescriptor
            // ClassTestDescriptor
            // NestedClassTestDescriptor
            // TestMethodTestDescriptor
        });


        return DynamicContainer.dynamicContainer(type.getSimpleName(), dynamicNodes);

//        N instance = instanceSupplier.get();
//        return DynamicContainer.dynamicContainer(type.getSimpleName(),
//                Stream.concat(
//                    AnnotationSupport.findAnnotatedMethods(type, Test.class, HierarchyTraversalMode.TOP_DOWN)
//                            .stream()
//                            .map(method -> DynamicTest.dynamicTest(method.getName(), () -> method.invoke(instance))),
//                    AnnotationSupport.findAnnotatedMethods(type, TestFactory.class, HierarchyTraversalMode.TOP_DOWN)
//                            .stream()
//                            .map(method -> (DynamicNode) ReflectionUtils.invokeMethod(method, instance))
//                            )
//        );

    }

    protected <N> DynamicNode findTestsOnNestedClass(Class<N> type, Supplier<N> instanceSupplier, BooleanSupplier enableTests) {
        if (enableTests.getAsBoolean()) {
            return findTestsOnNestedClass(type, instanceSupplier);
        } else {
            return DynamicContainer.dynamicContainer(type.getName(), Stream.empty());
        }
    }
}
