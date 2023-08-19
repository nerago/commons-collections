package org.apache.commons.collections4.map;

import org.apache.commons.collections4.BulkTest;
import org.apache.commons.collections4.CollectionCommonsRole;
import org.apache.commons.collections4.IterableMap;
import org.apache.commons.collections4.collection.IterationBehaviour;
import org.junit.jupiter.api.Nested;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

public abstract class AbstractIterableSortedMapTest<K, V> extends AbstractSortedMapTest<K, V> {
    public AbstractIterableSortedMapTest(final String testName) {
        super(testName);
    }

    @Nested
    public class NestedIterableTests extends AbstractIterableMapTest<K, V> {
        public NestedIterableTests() {
            super("NestedIterableTests");
        }

        @Override
        public IterableMap<K, V> makeObject() {
            return (IterableMap<K, V>) AbstractIterableSortedMapTest.this.makeObject();
        }

        @Override
        public K[] getSampleKeys() {
            return AbstractIterableSortedMapTest.this.getSampleKeys();
        }
        @Override
        public V[] getSampleValues() {
            return AbstractIterableSortedMapTest.this.getSampleValues();
        }
        @Override
        public V[] getNewSampleValues() {
            return AbstractIterableSortedMapTest.this.getNewSampleValues();
        }

        @Override
        protected IterationBehaviour getIterationBehaviour() {
            return AbstractIterableSortedMapTest.this.getIterationBehaviour();
        }
        @Override
        public CollectionCommonsRole collectionRole() {
            return AbstractIterableSortedMapTest.this.collectionRole();
        }
        @Override
        public boolean isAllowNullKey() {
            return false;
        }
        @Override
        public boolean isAllowNullValue() {
            return AbstractIterableSortedMapTest.this.isAllowNullValue();
        }
        @Override
        public boolean isPutAddSupported() {
            return AbstractIterableSortedMapTest.this.isPutAddSupported();
        }
        @Override
        public boolean isPutChangeSupported() {
            return AbstractIterableSortedMapTest.this.isPutChangeSupported();
        }
        @Override
        public boolean isRemoveSupported() {
            return AbstractIterableSortedMapTest.this.isRemoveSupported();
        }
    }

}
