package org.apache.commons.collections4.spliterators;

import org.apache.commons.collections4.SortedMapRange;

import java.util.Map;

public abstract class AbstractTreeRangeSpliterator<K, V, N extends Map.Entry<K, V>> extends AbstractTreeSpliterator<K, V, N> {
    protected final SortedMapRange<K> keyRange;

    protected AbstractTreeRangeSpliterator(final SortedMapRange<K> keyRange) {
        this.keyRange = keyRange;

        // dig down tree until in range
        if (!keyRange.inRange(currentNode.getKey())) {
            final N minimumNode = findFirstNode(), maximumNode = findLastNode();
            while (currentNode != null && isLowerThan(maximumNode, currentNode)) {
                currentNode = getLeft(currentNode);
            }
            while (currentNode != null && isLowerThan(currentNode, minimumNode)) {
                currentNode = getRight(currentNode);
            }
        }

        if (keyRange.hasTo()) {
            this.lastNode = findLastNode();
        }
    }

    protected AbstractTreeRangeSpliterator(final SortedMapRange<K> keyRange, final SplitState state, final N currentNode, final N lastNode, final long estimatedSize) {
        super(state, currentNode, lastNode, estimatedSize);
        this.keyRange = keyRange;
    }

    protected abstract N findFirstNode();

    protected abstract N findLastNode();

    @Override
    protected void initialiseRange() {
        super.initialiseRange();

        if (!checkRangeStart() || !checkRangeEnd()) {
            currentNode = null;
            lastNode = null;
        }
    }

    private boolean checkRangeStart() {
        if (currentNode == null) {
            return true;
        }

        if (keyRange.inRange(currentNode.getKey())) {
            return true;
        }

        if (keyRange.hasFrom()) {
            final N minimumNode = findFirstNode();
            if (keyRange.inRange(minimumNode.getKey()) && isLowerThanOrEqual(currentNode, minimumNode) && isLowerThanOrEqual(minimumNode, lastNode)) {
                currentNode = minimumNode;
                return true;
            }
        }

        return false;
    }

    private boolean checkRangeEnd() {
        if (lastNode == null && !keyRange.hasTo()) {
            return true;
        } else if (lastNode == null) {
            return false;
        }

        if (keyRange.inRange(lastNode.getKey())) {
            return true;
        }

        if (keyRange.hasTo()) {
            final N maximumNode = findLastNode();
            if (keyRange.inRange(maximumNode.getKey()) && isLowerThanOrEqual(currentNode, maximumNode) && isLowerThanOrEqual(maximumNode, lastNode)) {
                lastNode = maximumNode;
                return true;
            }
        }

        return false;
    }

    public AbstractTreeSpliterator<K, V, N> trySplit() {
        final N left = getLeft(currentNode), right = getRight(currentNode);
        if (left == null || right == null)
            return null;

        AbstractTreeSpliterator<K, V, N> split = null;
        if (state == SplitState.INITIAL) {
            // currentNode is guaranteed in range
            final N splitLast = nextLower(currentNode);
            if (isLowerThanOrEqual(left, splitLast) && isLowerThanOrEqual(currentNode, lastNode) && keyRange.inRange(splitLast.getKey())) {
                // mid = current is unknown but last is good in range
                split = makeSplit(SplitState.SPLITTING_MID, left, splitLast, estimatedSize >>>= 1);
                // right = current good, last good
                state = SplitState.SPLITTING_RIGHT;
            }
        } else if (state == SplitState.SPLITTING_MID) {
            // mid = current is unchecked but last is good in range
            final N splitLast = nextLower(currentNode);
            if (isLowerThanOrEqual(left, splitLast) && isLowerThanOrEqual(currentNode, lastNode) && keyRange.inRange(splitLast.getKey())) {
                // mid = current unknown but last is good in range
                split = makeSplit(SplitState.SPLITTING_MID, left, splitLast, estimatedSize >>>= 1);
                // right = current probably good, splitLast good
                state = SplitState.SPLITTING_RIGHT;
            }
            // else we leave mid having current unchecked, last good
        } else if (state == SplitState.SPLITTING_RIGHT) {
            // right = current probably good, splitLast good
            final N rightLeft = getLeft(right);
            if (rightLeft != null && isLowerThanOrEqual(currentNode, rightLeft) && isLowerThanOrEqual(right, lastNode) && keyRange.inRange(rightLeft.getKey())) {
                // left = current unchecked, last is good
                split = makeSplit(SplitState.SPLITTING_LEFT, currentNode, rightLeft, estimatedSize >>>= 1);
                // right = current unchecked, last is good. inconsistent but maybe think about it some more
                state = SplitState.SPLITTING_RIGHT;
                currentNode = right;
            }
        } else if (state == SplitState.SPLITTING_LEFT) {
            final N passedSubTree = lastNode;
            final N subTreeLeft = getLeft(passedSubTree);
            final N subTreeRight = getRight(passedSubTree);
            if (subTreeLeft != null && isLowerThanOrEqual(currentNode, subTreeLeft) && subTreeRight != null && keyRange.inRange(subTreeLeft.getKey())) {
                split = makeSplit(SplitState.SPLITTING_LEFT, currentNode, subTreeLeft, estimatedSize >>>= 1);
                state = SplitState.SPLITTING_RIGHT;
                currentNode = passedSubTree;
                lastNode = subTreeGreatest(passedSubTree);
            }
        } else {
            throw new IllegalStateException();
        }

        return split;
    }
}