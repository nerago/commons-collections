/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */
package org.apache.commons.collections4.primitive;

/**
 * Alternate functional interface similar to {@link java.util.function.ObjLongConsumer} with flipped order.
 */
@FunctionalInterface
public interface LongObjConsumer<T> {

    /**
     * Performs this operation on the given arguments.
     *
     * @param value the first input argument
     * @param t the second input argument
     */
    void accept(long value, T t);
}
