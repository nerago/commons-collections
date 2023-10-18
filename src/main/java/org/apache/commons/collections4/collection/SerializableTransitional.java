package org.apache.commons.collections4.collection;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

/**
 * Interface mixed between {@link Serializable} and {@link Externalizable} to allow easy later switch.
 */
public interface SerializableTransitional extends Serializable {
    /**
     * @see java.io.Externalizable#readExternal(ObjectInput)
     */
    void writeExternal(ObjectOutput out) throws IOException;

    /**
     * @see java.io.Externalizable#readExternal(ObjectInput)
     */
    void readExternal(ObjectInput in) throws IOException, ClassNotFoundException;
}
