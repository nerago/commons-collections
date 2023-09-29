package org.apache.commons.collections4;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.commons.collections4.collection.AbstractCollectionTest;
import org.apache.commons.collections4.collection.IndexedCollection;
import org.apache.commons.collections4.collection.IndexedCollectionOriginal;
import org.apache.commons.collections4.collection.IndexedCollectionTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CollectionRandomMonkeys<E> {
    private final Transformer<String, String> toLower = String::toLowerCase;

    private static class DataSource<E> {
        AbstractCollectionTest<E> testClass;
        private final List<E> elements;
        Random rand = new Random();

        private DataSource(final AbstractCollectionTest<E> testClass) {
            this.testClass = testClass;
            this.elements = Stream.concat(
                    Arrays.stream(testClass.getFullElements()),
                    Arrays.stream(testClass.getOtherElements())
            ).collect(Collectors.toList());
        }

        E randomElement() {
            return elements.get(rand.nextInt(elements.size()));
        }

        List<E> randomElementList() {
            final List<E> list = new ArrayList<>();
            final int count = rand.nextInt(elements.size() / 4);
            for (int i = 0; i < count; ++i) {
                list.add(randomElement());
            }
            return list;
        }
    }

    @FunctionalInterface
    private interface Action<E> {
        void execute(Collection<E> a, Collection<E> b, DataSource<E> ds);
    }

    private static final class RunVoid<E, X> implements Action<E> {
        Consumer<Collection<E>> func;
        private RunVoid(final Consumer<Collection<E>> func) {
            this.func = func;
        }

        @Override
        public void execute(final Collection<E> a, final Collection<E> b, final DataSource<E> ds) {
            func.accept(a);
            func.accept(b);
        }
    }

    private static final class CompareNoArg<E, X> implements Action<E> {
        Function<Collection<E>, X> func;
        private CompareNoArg(final Function<Collection<E>, X> func) {
            this.func = func;
        }

        @Override
        public void execute(final Collection<E> a, final Collection<E> b, final DataSource<E> ds) {
            assertEquals(func.apply(a), func.apply(b));
        }
    }

    private static final class CompareOneArgElement<E, X> implements Action<E> {
        BiFunction<Collection<E>, E, X> func;

        private CompareOneArgElement(final BiFunction<Collection<E>, E, X> func) {
            this.func = func;
        }

        @Override
        public void execute(final Collection<E> a, final Collection<E> b, final DataSource<E> ds) {
            final E arg = ds.randomElement();
            assertEquals(func.apply(a, arg), func.apply(b, arg));
        }
    }

    private static final class CompareOneArgElementList<E, X> implements Action<E> {
        BiFunction<Collection<E>, List<E>, X> func;
        private CompareOneArgElementList(final BiFunction<Collection<E>, List<E>, X> func) {
            this.func = func;
        }

        @Override
        public void execute(final Collection<E> a, final Collection<E> b, final DataSource<E> ds) {
            final List<E> arg = ds.randomElementList();
            assertEquals(func.apply(a, arg), func.apply(b, arg));
        }
    }

    private static class CheckIterator<E> implements Action<E> {
        boolean canRemove = false;

        @Override
        public void execute(final Collection<E> collA, final Collection<E> collB, final DataSource<E> ds) {
            final Iterator<E> iterA = collA.iterator();
            final Iterator<E> iterB = collB.iterator();
            boolean hasA = iterA.hasNext();
            boolean hasB = iterB.hasNext();
            assertEquals(hasA, hasB);
            while (hasA) {
                assertEquals(iterA.next(), iterB.next());
                if (canRemove && ds.rand.nextInt(4) == 0) {
                    System.out.println("iter.remove");
                    iterA.remove();
                    iterB.remove();
                }
                hasA = iterA.hasNext();
                hasB = iterB.hasNext();
                assertEquals(hasA, hasB);
            }
        }
    }

    private static class CheckEquality<E> implements Action<E> {
        @Override
        public void execute(final Collection<E> a, final Collection<E> b, final DataSource<E> ds) {
            assertEquals(a, b);
        }
    }

    private List<Action<E>> makeActionList() {
        final List<Action<E>> list = new ArrayList<>();
        list.add(new CompareNoArg<>(Collection::isEmpty));
        list.add(new CompareNoArg<>(Collection::size));
        list.add(new CompareNoArg<>(Collection::hashCode));
        list.add(new CompareNoArg<>(Collection::toArray));
        list.add(new CompareOneArgElement<>(Collection::contains));
        list.add(new CompareOneArgElement<>(Collection::add));
        list.add(new CompareOneArgElement<>(Collection::remove));
        list.add(new CompareOneArgElementList<>(Collection::containsAll));
        list.add(new CompareOneArgElementList<>(Collection::addAll));
        list.add(new CompareOneArgElementList<>(Collection::removeAll));
        list.add(new CompareOneArgElementList<>(Collection::retainAll));
        list.add(new RunVoid<>(Collection::clear));
        list.add(new CheckIterator<>());
        list.add(new CheckEquality<>());
        return list;
    }

    @Test
    public void testAtRandom() {
        final Collection<String> collection = IndexedCollection.uniqueIndexedCollection(new ArrayList<>(), toLower);
        final Collection<String> confirm = IndexedCollectionOriginal.uniqueIndexedCollection(new ArrayList<>(), toLower);
        final DataSource<String> ds = new DataSource<>(new IndexedCollectionTest());
        final ListRandom<Action<E>> randomAction = new ListRandom<>(makeActionList());
        for (int loops = 0; loops < 1000; ++loops) {
            final Action<String> action = (Action<String>) randomAction.chooseOne();
            System.out.println(action);
            action.execute(confirm, collection, ds);
        }
    }
}
