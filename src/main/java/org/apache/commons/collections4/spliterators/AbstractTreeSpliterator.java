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
package org.apache.commons.collections4.spliterators;

import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class AbstractTreeSpliterator<K, V, N extends Map.Entry<K, V>> implements MapSpliterator<K, V> {
    protected enum SplitState {READY, READY_SPLIT, SPLITTING_LEFT, SPLITTING_MID, SPLITTING_RIGHT, INITIAL}

    protected final int expectedModCount;
    /**
     * Whether to return KEY or VALUE order.
     */
    protected SplitState state;
    /**
     * The next node to be returned by the spliterator.
     */
    protected N currentNode;
    /**
     * The final node to be returned by the spliterator (mostly just needed if was split).
     */
    protected N lastNode;
    protected long estimatedSize;

    protected AbstractTreeSpliterator() {
        this.expectedModCount = modCount();
        this.state = SplitState.INITIAL;
        this.currentNode = rootNode();
        this.lastNode = subTreeGreatest(currentNode);
    }

    protected AbstractTreeSpliterator(final SplitState state, final N currentNode, final N lastNode, final long estimatedSize) {
        this.expectedModCount = modCount();
        this.state = state;
        this.currentNode = currentNode;
        this.lastNode = lastNode;
        this.estimatedSize = estimatedSize;
    }

    protected abstract int modCount();

    protected abstract N rootNode();
    
    protected abstract N getLeft(N node);

    protected abstract N getRight(N node);
    
    protected abstract N nextLower(N node);

    protected abstract N nextGreater(N node);
    
    protected abstract N subTreeLowest(N node);

    protected abstract N subTreeGreatest(N node);

    protected abstract boolean isLowerThan(N node, N other);

    protected abstract boolean isLowerThanOrEqual(N node, N other);

    protected abstract AbstractTreeSpliterator<K, V, N> makeSplit(SplitState state, N currentNode, N lastNode, long estimatedSize);

    private void checkInit() {
        if (state != SplitState.READY && state != SplitState.READY_SPLIT) {
            initialiseRange();
        }
    }

    protected void initialiseRange() {
        if (state == SplitState.INITIAL) {
            currentNode = subTreeLowest(currentNode);
            state = SplitState.READY;
        } else if (state == SplitState.SPLITTING_MID) {
            currentNode = subTreeLowest(currentNode);
            state = SplitState.READY_SPLIT;
        } else if (state == SplitState.SPLITTING_RIGHT) {
            state = SplitState.READY_SPLIT;
        } else if (state == SplitState.SPLITTING_LEFT) {
            lastNode = subTreeGreatest(lastNode);
            state = SplitState.READY_SPLIT;
        }
    }

    @Override
    public boolean tryAdvance(final Consumer<? super Map.Entry<K, V>> action) {
        if (expectedModCount != modCount()) {
            throw new ConcurrentModificationException();
        }
        checkInit();
        final N current = currentNode;
        if (current != null) {
            action.accept(current);
            if (current != lastNode)
                currentNode = nextGreater(current);
            else
                currentNode = null;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void forEachRemaining(final Consumer<? super Map.Entry<K, V>> action) {
        checkInit();
        N current = currentNode;
        final N last = lastNode;
        while (current != null) {
            action.accept(current);
            if (current != last)
                current = nextGreater(current);
            else
                current = null;
        }
        if (expectedModCount != modCount()) {
            throw new ConcurrentModificationException();
        }
        currentNode = null;
    }

    @Override
    public boolean tryAdvance(final BiConsumer<? super K, ? super V> action) {
        return tryAdvance(entry -> action.accept(entry.getKey(), entry.getValue()));
    }

    @Override
    public void forEachRemaining(final BiConsumer<? super K, ? super V> action) {
        forEachRemaining(entry -> action.accept(entry.getKey(), entry.getValue()));
    }

    public AbstractTreeSpliterator<K, V, N> trySplit() {
        final N left = getLeft(currentNode), right = getRight(currentNode);
        if (left == null || right == null)
            return null;

        AbstractTreeSpliterator<K, V, N> split = null;
        if (state == SplitState.INITIAL) {
            final N splitLast = nextLower(currentNode);
            if (isLowerThanOrEqual(left, splitLast) && isLowerThanOrEqual(currentNode, lastNode)) {
                split = makeSplit(SplitState.SPLITTING_MID, left, splitLast, estimatedSize >>>= 1);
                state = SplitState.SPLITTING_RIGHT;
            }
        } else if (state == SplitState.SPLITTING_MID) {
            final N splitLast = nextLower(currentNode);
            if (isLowerThanOrEqual(left, splitLast) && isLowerThanOrEqual(currentNode, lastNode)) {
                split = makeSplit(SplitState.SPLITTING_MID, left, splitLast, estimatedSize >>>= 1);
                state = SplitState.SPLITTING_RIGHT;
            }
        } else if (state == SplitState.SPLITTING_RIGHT) {
            final N rightLeft = getLeft(right);
            if (rightLeft != null && isLowerThanOrEqual(currentNode, rightLeft) && isLowerThanOrEqual(right, lastNode)) {
                split = makeSplit(SplitState.SPLITTING_LEFT, currentNode, rightLeft, estimatedSize >>>= 1);
                state = SplitState.SPLITTING_RIGHT;
                currentNode = right;
            }
        } else if (state == SplitState.SPLITTING_LEFT) {
            final N passedSubTree = lastNode;
            final N subTreeLeft = getLeft(passedSubTree);
            final N subTreeRight = getRight(passedSubTree);
            if (subTreeLeft != null && isLowerThanOrEqual(currentNode, subTreeLeft) && subTreeRight != null) {
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

    @Override
    public long estimateSize() {
        return estimatedSize;
    }

    @Override
    public int characteristics() {
        int characteristics = Spliterator.DISTINCT | Spliterator.NONNULL | Spliterator.ORDERED;
        if (state == SplitState.INITIAL || state == SplitState.READY) {
            characteristics |= Spliterator.SIZED;
        }
        return characteristics;
    }

    @Override
    public Comparator<? super Map.Entry<K, V>> getComparator() {
        return null;
    }
}