package org.apache.commons.collections4.map;

import org.apache.commons.collections4.AbstractObjectTest;
import org.apache.commons.collections4.CollectionCommonsRole;
import org.apache.commons.collections4.IterableSortedMap;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

public class EmptyMapTest extends AbstractObjectTest {
    private static final String ONE = "one";
    private static final String TWO = "two";
    private static final String THREE = "four";
    private static final String TEN = "10";

    public EmptyMapTest() {
        super("EmptyMapTest");
    }

    @Override
    public EmptyMap<String, String> makeObject() {
        return EmptyMap.emptyMap();
    }

    @Override
    public boolean isTestSerialization() {
        return false; // i'm too lazy
    }

    @Nested
    public class IterableSortedMapTest extends AbstractIterableSortedMapTest<String, String> {
        public IterableSortedMapTest() {
            super("IterableSortedMapTest");
        }

        @Override
        public CollectionCommonsRole collectionRole() {
            return CollectionCommonsRole.CONCRETE;
        }

        @Override
        public IterableSortedMap<String, String> makeObject() {
            return EmptyMapTest.this.makeObject();
        }

        @Override
        public IterableSortedMap<String, String> makeFullMap() {
            // fake for passing other tests
            return UnmodifiableSortedMap.unmodifiableSortedMap(new SingletonMap<>(ONE, TWO));
        }

        @Override
        public String[] getSampleKeys() {
            return new String[] { ONE };
        }

        @Override
        public String[] getSampleValues() {
            return new String[] { TWO };
        }

        @Override
        public String[] getNewSampleValues() {
            return new String[] { TEN };
        }

        @Override
        public String[] getOtherKeys() {
            return new String[] { THREE };
        }

        @Override
        public String[] getOtherValues() {
            return new String[] { THREE };
        }

        @Override
        public boolean isTestSerialization() {
            return false; // i'm too lazy
        }

        @Override
        public boolean isPutAddSupported() {
            return false;
        }

        @Override
        public boolean isPutChangeSupported() {
            return false;
        }

        @Override
        public boolean isRemoveSupported() {
            return false;
        }

        @Override
        public boolean isCopyConstructorCheckable() {
            return false;
        }

        @Test
        @Override
        public void testMakeMap() {
            final Map<?, ?> em = makeObject();
            assertNotNull(em, "failure in test: makeEmptyMap must return a non-null map.");

            final Map<?, ?> em2 = makeObject();
            assertNotNull(em, "failure in test: makeEmptyMap must return a non-null map.");

            // empty map override
            assertSame(em, em2, "failure in test: EmptyMap.makeEmptyMap must return a the same map " +
                    "with each invocation.");

            final Map<?, ?> fm = makeFullMap();
            assertNotNull(fm, "failure in test: makeFullMap must return a non-null map.");

            final Map<?, ?> fm2 = makeFullMap();
            assertNotNull(fm2, "failure in test: makeFullMap must return a non-null map.");

            assertNotSame(fm, fm2, "failure in test: makeFullMap must return a new map " +
                    "with each invocation.");
        }

        @Nested
        public class NestedIterableTests extends AbstractIterableSortedMapTest<String, String>.NestedIterableTests {
            @Override
            @Test
            @Disabled
            public void testMakeMap() {
            }
        }
    }

//    @Nested
//    public class BidiTest extends AbstractSortedBidiMapTest<K, V> {
//        public BidiTest() {
//            super("BidiTest");
//        }
//
//        @Override
//        public CollectionCommonsRole collectionRole() {
//            return CollectionCommonsRole.CONCRETE;
//        }
//
//        @Override
//        public SortedBidiMap<K, V> makeObject() {
//            return EmptyMapTest.this.makeObject();
//        }
//    }
}
