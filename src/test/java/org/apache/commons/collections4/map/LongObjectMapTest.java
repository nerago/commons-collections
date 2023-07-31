package org.apache.commons.collections4.map;

import org.apache.commons.collections4.AbstractObjectTest;
import org.apache.commons.collections4.CollectionCommonsRole;
import org.apache.commons.collections4.collection.IterationBehaviour;
import org.junit.jupiter.api.Nested;

import java.util.Map;

public class LongObjectMapTest<V> extends AbstractObjectTest {
    public LongObjectMapTest() {
        super("LongObjectMapTest");
    }

    @Override
    public LongObjectMap<V> makeObject() {
        return new LongObjectMap<>();
    }

    @Nested
    public class CheckMapAdapter extends AbstractMapTest<Long, V> {
        public CheckMapAdapter() {
            super("CheckMapAdapter");
        }

        @Override
        public boolean isCopyConstructorCheckable() {
            return false;
        }

        @Override
        public CollectionCommonsRole collectionRole() {
            return CollectionCommonsRole.INNER;
        }

        @Override
        protected IterationBehaviour getIterationBehaviour() {
            return IterationBehaviour.CONSISTENT_SEQUENCE_UNTIL_MODIFY;
        }

        @Override
        public Long[] getSampleKeys() {
            return new Long[] {
                    1L, 7L, 11L, 12L, 13L, 17L, 21L, 22L,
                    -30L, -31L, -32L, -33L, 34L, 35L, 36L,
                    Long.MAX_VALUE,
                    Long.MIN_VALUE,
                    0L
            };
        }

        @Override
        public Map<Long, V> makeObject() {
            return LongObjectMapTest.this.makeObject().asMap();
        }
    }
}
