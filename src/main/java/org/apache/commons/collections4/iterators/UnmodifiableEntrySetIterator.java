package org.apache.commons.collections4.iterators;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections4.keyvalue.UnmodifiableMapEntry;

/**
 * Implementation of an entry set, can't remove or set.
 */
public class UnmodifiableEntrySetIterator<K, V> extends AbstractIteratorDecorator<Map.Entry<K, V>> {
    public UnmodifiableEntrySetIterator(final Iterator<Map.Entry<K, V>> iterator) {
        super(iterator);
    }

    @Override
    public Map.Entry<K, V> next() {
        return new UnmodifiableMapEntry<>(getIterator().next());
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
