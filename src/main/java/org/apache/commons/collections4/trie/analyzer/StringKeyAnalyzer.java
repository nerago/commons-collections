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

import static java.lang.StrictMath.floorDiv;

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
    private static int maskOne(final int bit) {
        return MSB >>> bit;
    }

    private static final int ALL_BITS = 0xFFFF;

    private static int maskEndBits(final int bit) {
        return ALL_BITS >>> (LENGTH - bit);
    }

    private static int charLeadingZeros(final int x) {
        return Integer.numberOfLeadingZeros(x) - LENGTH;
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

        if (keyLengthInBits == 0 && otherLengthInBits == 0) {
            return KeyAnalyzer.NULL_BIT_KEY;
        }

        final int beginCharIndexKey = keyOffsetInBits / LENGTH;
        final int beginCharIndexOther = otherOffsetInBits / LENGTH;

        final int endBitIndexKey = keyOffsetInBits + keyLengthInBits;
        final int endBitIndexOther = otherOffsetInBits + otherLengthInBits;
        final int endCharIndexKey = (endBitIndexKey + LENGTH - 1) / LENGTH - 1;
        final int endCharIndexOther = (endBitIndexOther + LENGTH - 1) / LENGTH - 1;


        int indexKey = beginCharIndexKey, indexOther = beginCharIndexOther;

        // Before loop check relevant bits if offset is mid character
        if (indexKey <= endCharIndexKey && indexOther <= endCharIndexOther) {
            if (keyOffsetInBits % LENGTH != otherOffsetInBits % LENGTH)
                throw new IllegalArgumentException("The offsets must be at whole elements or the same mid element index");

            final int numEndBits = LENGTH - (otherOffsetInBits % LENGTH);
            if (numEndBits != LENGTH) {
                final int mask = maskEndBits(numEndBits);
                final int k = key.charAt(indexKey) & mask;
                final int f = (other != null) ? (other.charAt(indexOther) & mask) : 0;

                if (k != f) {
                    final int x = k ^ f;
                    final int keyBitIndex = indexKey * LENGTH + charLeadingZeros(x);
                    if (keyBitIndex <= endBitIndexKey)
                        return keyBitIndex;
                }

                if (k != 0) {
                    allNull = false;
                }

                indexKey++;
                indexOther++;
            }
        }

        // Look at each character, and if they're different
        // then figure out which bit makes the difference
        // and return it.
        while (indexKey <= endCharIndexKey || indexOther <= endCharIndexOther) {
            final char k = indexKey <= endCharIndexKey
                           ? key.charAt(indexKey)
                           : 0;

            final char f = other != null && indexOther <= endCharIndexOther
                           ? other.charAt(indexOther)
                           : 0;

            if (k != f) {
                final int x = k ^ f;
                final int keyBitIndex = indexKey * LENGTH + charLeadingZeros(x);
                if (keyBitIndex <= endBitIndexKey)
                    return keyBitIndex;
                else
                    break;
            }

            if (k != 0) {
                allNull = false;
            }

            indexKey++;
            indexOther++;
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

        return (key.charAt(index) & maskOne(bit)) != 0;
    }

}
