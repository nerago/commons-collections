package org.apache.commons.collections4.bits;

/**
 * Bits impl of the specified length with all bits set.
 */
public class MatchAllBits implements Bits {
    final int len;

    public MatchAllBits(int len) {
        this.len = len;
    }

    @Override
    public boolean get(int index) {
        return true;
    }

    @Override
    public int length() {
        return len;
    }
}
