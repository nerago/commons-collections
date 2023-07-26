/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.collections4.trie.analyzer;

import org.apache.commons.collections4.trie.KeyAnalyzer;

/**
 * An {@link KeyAnalyzer} for {@link String}s.
 *
 * @since 4.0
 */
public class StringKeyAnalyzer extends KeyAnalyzer<String> {

    private static final long serialVersionUID = -7032449491269434877L;

    /** A singleton instance of {@link StringKeyAnalyzer}. */
    public static final StringKeyAnalyzer INSTANCE = new StringKeyAnalyzer();

    /** The number of bits per {@link Character}. */
    public static final int LENGTH = Character.SIZE;

    /** A bit mask where the first bit is 1 and the others are zero. */
    private static final int MSB = 0x8000;

    /** Returns a bit mask where the given bit is set. */
    private static int mask(final int bit) {
        return MSB >>> bit;
    }

    @Override
    public int bitsPerElement() {
        return LENGTH;
    }

    @Override
    public int lengthInBits(final String key) {
        return key != null ? key.length() * LENGTH : 0;
    }

    @Override
    public int bitIndex(final String key, final int keyOffsetInBits, final int keyLengthInBits,
                        final String other, final int otherOffsetInBits, final int otherLengthInBits) {

        boolean allNull = true;

        final int beginIndex1 = keyOffsetInBits / LENGTH;
        final int beginIndex2 = otherOffsetInBits / LENGTH;

        final int endIndex1 = (keyOffsetInBits + keyLengthInBits + LENGTH - 1) / LENGTH;
        final int endIndex2 = (otherOffsetInBits + otherLengthInBits + LENGTH - 1) / LENGTH;

        final int length = Math.max(endIndex1, endIndex2);
        final int lengthInBits = Math.max(keyLengthInBits, otherLengthInBits);

        // Look at each character, and if they're different
        // then figure out which bit makes the difference
        // and return it.
        for (int i = 0; i < length; i++) {
            final int index1 = beginIndex1 + i;
            final int index2 = beginIndex2 + i;

            final char k;
            if (index1 >= endIndex1) {
                k = 0;
            } else {
                k = key.charAt(index1);
            }

            final char f;
            if (other == null || index2 >= endIndex2) {
                f = 0;
            } else {
                f = other.charAt(index2);
            }

            if (k != f) {
                final int x = k ^ f;
                final int bitNumber = i * LENGTH + Integer.numberOfLeadingZeros(x) - LENGTH;
                if (bitNumber < lengthInBits) {
                    return bitNumber;
                }
            }

            if (k != 0) {
                allNull = false;
            }
        }

        // All bits are 0
        if (allNull) {
            return KeyAnalyzer.NULL_BIT_KEY;
        }

        // Both keys are equal
        return KeyAnalyzer.EQUAL_BIT_KEY;
    }

    @Override
    public boolean isBitSet(final String key, final int bitIndex, final int lengthInBits) {
        if (key == null || bitIndex >= lengthInBits) {
            return false;
        }

        final int index = bitIndex / LENGTH;
        final int bit = bitIndex % LENGTH;

        return (key.charAt(index) & mask(bit)) != 0;
    }

}
