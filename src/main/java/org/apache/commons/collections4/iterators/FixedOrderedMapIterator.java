package org.apache.commons.collections4.iterators;

import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.OrderedMapIterator;

import java.util.Objects;

/**
 * Decorates a map iterator such that it cannot be modified in ways that change collection size.
 * <p>
 * Attempts to modify it will result in an UnsupportedOperationException.
 * </p>
 *
 * @param <K> the type of keys
 * @param <V> the type of mapped values
 * @since X.X
 */
public class FixedOrderedMapIterator<K, V> extends AbstractOrderedMapIteratorDecorator<K, V> {

    /**
     * Decorates the specified iterator such that it cannot be modified in ways that change collection size.
     *
     * @param <K>  the key type
     * @param <V>  the value type
     * @param iterator  the iterator to decorate
     * @return a new unmodifiable map iterator
     * @throws NullPointerException if the iterator is null
     */
    public static <K, V> OrderedMapIterator<K, V> fixedOrderedMapIterator(
            final OrderedMapIterator<K, V> iterator) {
        Objects.requireNonNull(iterator, "iterator");
        if (iterator instanceof FixedOrderedMapIterator) {
            return iterator;
        }
        return new FixedOrderedMapIterator<>(iterator);
    }

    protected FixedOrderedMapIterator(final OrderedMapIterator<K, V> iterator) {
        super(iterator);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove() is not supported");
    }

}
