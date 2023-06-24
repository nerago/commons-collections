package org.apache.commons.collections4.set;

import org.apache.commons.collections4.collection.SynchronizedCollection;

import java.util.Set;

/**
 * Synchronized Set class.
 */
public class SynchronizedSet<T> extends SynchronizedCollection<T> implements Set<T> {
    /**
     * Serialization version
     */
    private static final long serialVersionUID = 20150629L;

    /**
     * Constructor.
     *
     * @param set  the set to decorate
     * @param lock the lock to use, shared with the parent object.
     */
    public SynchronizedSet(final Set<T> set, final Object lock) {
        super(set, lock);
    }
}
