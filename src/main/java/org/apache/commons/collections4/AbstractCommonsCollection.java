package org.apache.commons.collections4;

import java.util.Collection;

public abstract class AbstractCommonsCollection<E> implements Collection<E> {
    protected void fillArray(final Object[] array) {
        ToArrayUtils.fillArrayAssumingSized(iterator(), array);
    }

    /**
     * Returns an array of all of this bag's elements.
     *
     * @return an array of all of this bag's elements
     */
    @Override
    public Object[] toArray() {
        return ToArrayUtils.fromFunction(this::fillArray, size());
    }

    /**
     * Returns an array of all of this bag's elements.
     * If the input array has more elements than are in the bag,
     * trailing elements will be set to null.
     *
     * @param <T> the type of the array elements
     * @param array the array to populate
     * @return an array of all of this bag's elements
     * @throws ArrayStoreException if the runtime type of the specified array is not
     *   a supertype of the runtime type of the elements in this list
     * @throws NullPointerException if the specified array is null
     */
    @Override
    public <T> T[] toArray(final T[] array) {
        return ToArrayUtils.fromFunction(this::fillArray, size(), array);
    }
}
