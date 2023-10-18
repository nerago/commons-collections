package org.apache.commons.collections4.bits;/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Interface for Bitset-like structures.
 * <p>
 * Based on {@link org.apache.lucene.util.Bits}.
 */
public interface Bits {
    /**
     * Returns the value of the bit with the specified {@code index}.
     *
     * @param index index, should be non-negative and &lt; {@link #length()}.
     *              Positive out of range values will return false.
     * @return {@code true} if the bit is set, {@code false} otherwise.
     * @throws IndexOutOfBoundsException for negative values
     */
    boolean get(int index);

    /**
     * Returns the number of bits in this set.
     * Maximum bit index range with storage allocated.
     */
    int length();
}