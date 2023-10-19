package org.apache.commons.collections4.primitive;


@FunctionalInterface
public interface LongCompute {
    /**
     * Remaps state for a {@link LongToLongMap#compute}.
     *
     * @param key key in question
     * @param present is key present in the map
     * @param oldValue existing value in the map if present
     * @return new value for the mapping or {@code null} to remove
     */
    Long apply(long key, boolean present, long oldValue);
}