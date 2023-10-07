package org.apache.commons.collections4.map;

import org.apache.commons.collections4.AbstractObjectTest;
import org.apache.commons.collections4.CollectionCommonsRole;
import org.apache.commons.collections4.IterableSortedMap;
import org.apache.commons.collections4.SortedBidiMap;
import org.apache.commons.collections4.bidimap.AbstractOrderedBidiMapTest;
import org.apache.commons.collections4.bidimap.AbstractSortedBidiMapTest;
import org.junit.jupiter.api.Nested;

public class EmptyMapTest<K, V> extends AbstractObjectTest {
    public EmptyMapTest() {
        super("EmptyMapTest");
    }

    @Override
    public EmptyMap<K, V> makeObject() {
        return EmptyMap.emptyMap();
    }

    @Nested
    public class IterableSortedMapTest extends AbstractIterableSortedMapTest<K, V> {
        public IterableSortedMapTest() {
            super("IterableSortedMapTest");
        }

        @Override
        public CollectionCommonsRole collectionRole() {
            return CollectionCommonsRole.CONCRETE;
        }

        @Override
        public IterableSortedMap<K, V> makeObject() {
            return EmptyMapTest.this.makeObject();
        }

        @Override
        public boolean isTestSerialization() {
            return super.isTestSerialization();
        }

        @Override
        public boolean isCheckSerializationId() {
            return super.isCheckSerializationId();
        }

        @Override
        public boolean isEqualsCheckable() {
            return super.isEqualsCheckable();
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
        public boolean isSubMapViewsSerializable() {
            return super.isSubMapViewsSerializable();
        }

        @Override
        public boolean isAllowNullValue() {
            return super.isAllowNullValue();
        }

        @Override
        public boolean isAllowDuplicateValues() {
            return super.isAllowDuplicateValues();
        }

        @Override
        public boolean isFailFastExpected() {
            return super.isFailFastExpected();
        }

        @Override
        public boolean isFailFastFunctionalExpected() {
            return super.isFailFastFunctionalExpected();
        }

        @Override
        public boolean isTestFunctionalMethods() {
            return super.isTestFunctionalMethods();
        }

        @Override
        public boolean areEqualElementsIndistinguishable() {
            return super.areEqualElementsIndistinguishable();
        }

        @Override
        public boolean isToStringLikeCommonMaps() {
            return super.isToStringLikeCommonMaps();
        }

        @Override
        public boolean isCopyConstructorCheckable() {
            return super.isCopyConstructorCheckable();
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
