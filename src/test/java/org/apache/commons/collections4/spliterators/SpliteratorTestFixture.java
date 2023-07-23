package org.apache.commons.collections4.spliterators;

import org.apache.commons.collections4.CollectionCommonsRole;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.Unmodifiable;
import org.apache.commons.collections4.collection.IterationBehaviour;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Spliterator;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SpliteratorTestFixture<E> {
    private final Collection<E> testCollection;
    private final IterationBehaviour iterationBehaviour;
    private final CollectionCommonsRole collectionRole;
    private List<E> resultsInOrder;

    public SpliteratorTestFixture(final Collection<E> testCollection,
                                  final IterationBehaviour iterationBehaviour,
                                  final CollectionCommonsRole collectionRole) {
        this.testCollection = testCollection;
        this.iterationBehaviour = iterationBehaviour;
        this.collectionRole = collectionRole;
    }

    public void testAll() {
        testNotSplit();
        testSingleSplit();
        testMaximumSplit();
    }

    public void testNotSplit() {
        resultsInOrder = extractAndCheckBasics(testCollection.spliterator());
        assertEquals(resultsInOrder, extractSpliteratorWithAdvance(testCollection.spliterator()),
                "should be same contents from either method");
    }

    public void testSingleSplit() {
        final Spliterator<E> spliteratorOriginal = testCollection.spliterator();
        final long estimateSize = spliteratorOriginal.estimateSize();
        final boolean reportedSIZED = spliteratorOriginal.hasCharacteristics(Spliterator.SIZED);
        final boolean reportedSUBSIZED = spliteratorOriginal.hasCharacteristics(Spliterator.SUBSIZED);
        final boolean reportedORDERED = spliteratorOriginal.hasCharacteristics(Spliterator.ORDERED);

        final Spliterator<E> spliteratorPrefix = spliteratorOriginal.trySplit();
        // this is just an arbitrary threshold, mostly to identify spliterators that don't have any trySplit code
        if (estimateSize >= 10 && estimateSize != Long.MAX_VALUE) {
            assertNotNull(spliteratorPrefix, "Couldn't split spliterator with size=" + estimateSize);
        }

        // only do any checking if split actually happened
        if (spliteratorPrefix != null) {
            checkAfterSplit(spliteratorPrefix, spliteratorOriginal, estimateSize, reportedSUBSIZED);

            final List<E> part1 = extractAndCheckBasics(spliteratorPrefix);
            final List<E> part2 = extractAndCheckBasics(spliteratorOriginal);
            final List<E> combined = ListUtils.union(part1, part2);

            checkFinalResultsFromSplit(combined, estimateSize, reportedSIZED, reportedORDERED);
        }
    }
    private void checkAfterSplit(final Spliterator<E> spliteratorPrefix, final Spliterator<E> spliteratorOriginal,
                                 final long estimateSize, final boolean reportedSUBSIZED) {
        assertTrue(spliteratorPrefix.estimateSize() <= estimateSize,
                "estimateSize must decrease across invocations of trySplit");
        assertTrue(spliteratorOriginal.estimateSize() <= estimateSize,
                "estimateSize must decrease across invocations of trySplit");
        assertTrue(spliteratorPrefix.estimateSize() + spliteratorOriginal.estimateSize() <= estimateSize,
                "estimateSize must decrease across invocations of trySplit");
        if (reportedSUBSIZED) {
            assertTrue(spliteratorPrefix.hasCharacteristics(Spliterator.SIZED), "after split of SUBSIZED, should report SIZED");
            assertTrue(spliteratorPrefix.hasCharacteristics(Spliterator.SUBSIZED), "after split of SUBSIZED, should report SIZED");
            assertTrue(spliteratorOriginal.hasCharacteristics(Spliterator.SIZED), "after split of SUBSIZED, should report SIZED");
            assertTrue(spliteratorOriginal.hasCharacteristics(Spliterator.SUBSIZED), "after split of SUBSIZED, should report SIZED");
            assertEquals(estimateSize, spliteratorPrefix.estimateSize() + spliteratorOriginal.estimateSize(),
                    "SUBSIZED spliterator should exactly match total count before and after split");
        }
    }

    private void checkFinalResultsFromSplit(final List<E> combined, final long estimateSize,
                                            final boolean reportedSIZED, final boolean reportedORDERED) {
        if (reportedSIZED) {
            assertEquals(estimateSize, combined.size(),
                    "SIZED spliterator estimate should match combined lists");
        }

        if (reportedORDERED) {
            assertEquals(resultsInOrder, combined, "split spliterator results doesn't match unsplit");
        } else {
            assertTrue(CollectionUtils.isEqualCollection(resultsInOrder, combined),
                    "split spliterator results doesn't match unsplit");
        }
    }

    public void testMaximumSplit() {
        final Spliterator<E> spliteratorOriginal = testCollection.spliterator();
        final long estimateSize = spliteratorOriginal.estimateSize();
        final boolean reportedSIZED = spliteratorOriginal.hasCharacteristics(Spliterator.SIZED);
        final boolean reportedSUBSIZED = spliteratorOriginal.hasCharacteristics(Spliterator.SUBSIZED);
        final boolean reportedORDERED = spliteratorOriginal.hasCharacteristics(Spliterator.ORDERED);

        final List<E> combined = new ArrayList<>();
        final Deque<Spliterator<E>> queue = new LinkedList<>();
        queue.addFirst(spliteratorOriginal);
        while (!queue.isEmpty()) {
            final Spliterator<E> current = queue.removeFirst();
            final long sizeBefore = current.estimateSize();
            final Spliterator<E> split = current.trySplit();
            if (split != null) {
                checkAfterSplit(split, current, sizeBefore, reportedSUBSIZED);
                queue.addFirst(current);
                queue.addFirst(split);
            } else {
                final List<E> data = extractAndCheckBasics(current);
                combined.addAll(data);
            }
        }
        checkFinalResultsFromSplit(combined, estimateSize, reportedSIZED, reportedORDERED);
    }

    private List<E> extractAndCheckBasics(final Spliterator<E> spliterator) {
        final long estimateSize = spliterator.estimateSize();
        final int characteristics = spliterator.characteristics();
        final boolean reportSIZED = (characteristics & Spliterator.SIZED) != 0;
        final boolean reportDISTINCT = (characteristics & Spliterator.DISTINCT) != 0;
        final boolean reportNONNULL = (characteristics & Spliterator.NONNULL) != 0;
        final boolean reportIMMUTABLE = (characteristics & Spliterator.IMMUTABLE) != 0;
        final boolean reportSORTED = (characteristics & Spliterator.SORTED) != 0;
        final boolean reportORDERED = (characteristics & Spliterator.ORDERED) != 0;
        final boolean reportSUBSIZED = (characteristics & Spliterator.SUBSIZED) != 0;
        final boolean reportCONCURRENT = (characteristics & Spliterator.CONCURRENT) != 0;
        assertEquals(reportSIZED, spliterator.hasCharacteristics(Spliterator.SIZED));
        assertEquals(reportDISTINCT, spliterator.hasCharacteristics(Spliterator.DISTINCT));
        assertEquals(reportNONNULL, spliterator.hasCharacteristics(Spliterator.NONNULL));
        assertEquals(reportIMMUTABLE, spliterator.hasCharacteristics(Spliterator.IMMUTABLE));
        assertEquals(reportSORTED, spliterator.hasCharacteristics(Spliterator.SORTED));
        assertEquals(reportORDERED, spliterator.hasCharacteristics(Spliterator.ORDERED));
        assertEquals(reportSUBSIZED, spliterator.hasCharacteristics(Spliterator.SUBSIZED));
        assertEquals(reportCONCURRENT, spliterator.hasCharacteristics(Spliterator.CONCURRENT));

        final List<E> results = extractSpliteratorWithForeach(spliterator);

        if (reportSIZED) {
            assertEquals(results.size(), estimateSize, "SIZED spliterator reported incorrect size");
        }
        if (iterationBehaviour.shouldSpliteratorBeOrdered() && collectionRole != CollectionCommonsRole.INNER) {
            assertTrue(reportORDERED, "Test framework thinks collection is " + iterationBehaviour + " but spliterator doesn't report ORDERED");
        } else if (iterationBehaviour.shouldSpliteratorBeSorted()) {
            assertTrue(reportSORTED, "Test framework thinks collection should be sorted (" + iterationBehaviour + ") "
                    + " but spliterator doesn't report SORTED");
        }
        if (reportSORTED) {
            assertTrue(reportORDERED, "A Spliterator that reports SORTED must also report ORDERED");
            Comparator<? super E> comparator = spliterator.getComparator();
            checkSortedSequence(results, comparator);
        }
        if (reportSUBSIZED) {
            assertTrue(reportSIZED, "Spliterator reported SUBSIZED but not SIZED");
        }
        if (reportDISTINCT) {
            assertTrue(
                    CollectionUtils.isEqualCollection(results, new HashSet<>(results)),
                    "DISTINCT spliterator supplied duplicates");
        }
        if (reportNONNULL) {
            assertFalse(results.contains(null), "NONNULL spliterator supplied null");
        }
        if (testCollection instanceof Unmodifiable && collectionRole != CollectionCommonsRole.INNER) {
            assertTrue(reportIMMUTABLE, "Unmodifiable collection should probably report IMMUTABLE spliterator");
        }
        if (reportIMMUTABLE) {
            if (!(testCollection instanceof Unmodifiable)) {
                System.out.println("Immutable spliterator from non-Unmodifiable collection");
            }
        }
        if (reportCONCURRENT) {
            assertFalse(reportSIZED, "Spliterator should not report both CONCURRENT and SIZED");
            assertFalse(reportIMMUTABLE, "Spliterator should not report both CONCURRENT and IMMUTABLE");
        }

        return results;
    }

    private void checkSortedSequence(List<E> list, Comparator<? super E> comparator) {
        if (comparator != null) {
            for (int i = 0; i < list.size() - 1; ++i) {
                final E a = list.get(i);
                final E b = list.get(i + 1);
                assertTrue(comparator.compare(a, b) <= 0);
            }
        } else {
            for (int i = 0; i < list.size() - 1; ++i) {
                @SuppressWarnings("unchecked")
                final Comparable<Object> comp1 = assertInstanceOf(Comparable.class, list.get(i));
                final Object comp2 = assertInstanceOf(Comparable.class, list.get(i + 1));
                assertTrue(comp1.compareTo(comp2) <= 0);
            }
        }
    }

    private List<E> extractSpliteratorWithForeach(final Spliterator<E> spliterator) {
        final List<E> results = new ArrayList<>();
        spliterator.forEachRemaining(results::add);
        return results;
    }

    private List<E> extractSpliteratorWithAdvance(final Spliterator<E> spliterator) {
        final List<E> results = new ArrayList<>();
        while (spliterator.tryAdvance(results::add)) {
            // empty body
        }
        return results;
    }
}
