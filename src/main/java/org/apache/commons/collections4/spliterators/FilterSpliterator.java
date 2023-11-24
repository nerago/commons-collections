package org.apache.commons.collections4.spliterators;

import java.util.Comparator;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;

import org.apache.commons.collections4.MutableBoolean;
import org.apache.commons.collections4.Predicate;

public class FilterSpliterator<E> extends AbstractSpliteratorDecorator<E, FilterSpliterator<E>> {
    /** The predicate being used */
    private Predicate<? super E> predicate;

    public FilterSpliterator(final Spliterator<E> spliterator, final Predicate<? super E> predicate) {
        super(spliterator);
    }

    @Override
    protected FilterSpliterator<E> decorateSplit(final Spliterator<E> split) {
        return new FilterSpliterator<>(split, predicate);
    }

    /**
     * Gets the predicate this iterator is using.
     *
     * @return the predicate
     */
    public Predicate<? super E> getPredicate() {
        return predicate;
    }

    /**
     * Sets the predicate this the iterator to use.
     *
     * @param predicate  the predicate to use
     */
    public void setPredicate(final Predicate<? super E> predicate) {
        this.predicate = predicate;
    }

    @Override
    public boolean tryAdvance(final Consumer<? super E> action) {
        final Spliterator<E> decorated = decorated();
        final MutableBoolean found = new MutableBoolean();
        do {} while (decorated.tryAdvance(item -> {
            if (predicate.evaluate(item)) {
                action.accept(item);
                found.flag = true;
            }
        }) && !found.flag);
        return found.flag;
    }

    @Override
    public void forEachRemaining(final Consumer<? super E> action) {
        decorated().forEachRemaining(item -> {
            if (predicate.evaluate(item)) {
                action.accept(item);
            }
        });
    }

    @Override
    public int characteristics() {
        return super.characteristics() & ~(Spliterator.SIZED | Spliterator.SUBSIZED);
    }
}
