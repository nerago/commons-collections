package org.apache.commons.collections4.collection;

public enum IterationBehaviour {
    UNKNOWN, UNORDERED, CONSISTENT_SEQUENCE_UNTIL_MODIFY, STABLE_SEQUENCE, FULLY_SORTED;

    public boolean couldToArrayOrderVary() {
        return this == UNKNOWN || this == UNORDERED;
    }

    public boolean couldSpliteratorOrderVary() {
        return this == UNKNOWN || this == UNORDERED;
    }

    public boolean couldIteratorOrdersVary() {
        return this == UNKNOWN || this == UNORDERED;
    }

    public boolean shouldSpliteratorBeOrdered() {
        return this == STABLE_SEQUENCE || this == FULLY_SORTED;
    }

    public boolean shouldSpliteratorBeSorted() {
        return this == FULLY_SORTED;
    }
}
