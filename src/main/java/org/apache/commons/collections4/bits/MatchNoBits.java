package org.apache.commons.collections4.bits;

/**
 * Bits impl of the specified length with no bits set.
 */
public class MatchNoBits implements Bits {
    final int len;

    public MatchNoBits(int len) {
        this.len = len;
    }

    @Override
    public boolean get(int index) {
        return false;
    }

    @Override
    public int length() {
        return len;
    }
}
