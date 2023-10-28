package org.apache.commons.collections4.map;


import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SequencedCollection;
import java.util.SequencedMap;
import java.util.SequencedSet;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.OrderedMap;
import org.apache.commons.collections4.OrderedMapIterator;

// see java.util.ReverseOrderSortedMapView
public class ReverseOrderedMap<K, V> extends AbstractOrderedMapDecorator<K, V> {
    /**
     * Constructor only used in deserialization, do not use otherwise.
     *
     * @since X.X
     */
    protected ReverseOrderedMap() {
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param map the map to decorate, must not be null
     * @throws NullPointerException if the map is null
     */
    public ReverseOrderedMap(final OrderedMap<K, V> map) {
        super(map);
    }

    @Override
    public K firstKey() {
        return super.lastKey();
    }

    @Override
    public K lastKey() {
        return super.firstKey();
    }

    @Override
    public K nextKey(K key) {
        return super.previousKey(key);
    }

    @Override
    public K previousKey(K key) {
        return super.nextKey(key);
    }

    @Override
    public OrderedMapIterator<K, V> mapIterator() {
        return super.mapIterator();
    }

    /**
     * Returns the first key-value mapping in this map,
     * or {@code null} if the map is empty.
     *
     * @return the first key-value mapping,
     * or {@code null} if this map is empty
     * @implSpec The implementation in this interface obtains the iterator of this map's entrySet.
     * If the iterator has an element, it returns an unmodifiable copy of that element.
     * Otherwise, it returns null.
     */
    @Override
    public Entry<K, V> firstEntry() {
        return super.firstEntry();
    }

    /**
     * Returns the last key-value mapping in this map,
     * or {@code null} if the map is empty.
     *
     * @return the last key-value mapping,
     * or {@code null} if this map is empty
     * @implSpec The implementation in this interface obtains the iterator of the entrySet of this map's
     * reversed view. If the iterator has an element, it returns an unmodifiable copy of
     * that element. Otherwise, it returns null.
     */
    @Override
    public Entry<K, V> lastEntry() {
        return super.lastEntry();
    }

    /**
     * Removes and returns the first key-value mapping in this map,
     * or {@code null} if the map is empty (optional operation).
     *
     * @return the removed first entry of this map,
     * or {@code null} if this map is empty
     * @throws UnsupportedOperationException if this collection implementation does not
     *                                       support this operation
     * @implSpec The implementation in this interface obtains the iterator of this map's entrySet.
     * If the iterator has an element, it calls {@code remove} on the iterator and
     * then returns an unmodifiable copy of that element. Otherwise, it returns null.
     */
    @Override
    public Entry<K, V> pollFirstEntry() {
        return super.pollFirstEntry();
    }

    /**
     * Removes and returns the last key-value mapping in this map,
     * or {@code null} if the map is empty (optional operation).
     *
     * @return the removed last entry of this map,
     * or {@code null} if this map is empty
     * @throws UnsupportedOperationException if this collection implementation does not
     *                                       support this operation
     * @implSpec The implementation in this interface obtains the iterator of the entrySet of this map's
     * reversed view. If the iterator has an element, it calls {@code remove} on the iterator
     * and then returns an unmodifiable copy of that element. Otherwise, it returns null.
     */
    @Override
    public Entry<K, V> pollLastEntry() {
        return super.pollLastEntry();
    }

    /**
     * Inserts the given mapping into the map if it is not already present, or replaces the
     * value of a mapping if it is already present (optional operation). After this operation
     * completes normally, the given mapping will be present in this map, and it will be the
     * first mapping in this map's encounter order.
     *
     * @param k the key
     * @param v the value
     * @return the value previously associated with k, or null if none
     * @throws UnsupportedOperationException if this collection implementation does not
     *                                       support this operation
     * @implSpec The implementation in this interface always throws
     * {@code UnsupportedOperationException}.
     */
    @Override
    public V putFirst(K k, V v) {
        return super.putFirst(k, v);
    }

    /**
     * Inserts the given mapping into the map if it is not already present, or replaces the
     * value of a mapping if it is already present (optional operation). After this operation
     * completes normally, the given mapping will be present in this map, and it will be the
     * last mapping in this map's encounter order.
     *
     * @param k the key
     * @param v the value
     * @return the value previously associated with k, or null if none
     * @throws UnsupportedOperationException if this collection implementation does not
     *                                       support this operation
     * @implSpec The implementation in this interface always throws
     * {@code UnsupportedOperationException}.
     */
    @Override
    public V putLast(K k, V v) {
        return super.putLast(k, v);
    }

    /**
     * Returns a {@code SequencedSet} view of this map's {@link #keySet keySet}.
     *
     * @return a {@code SequencedSet} view of this map's {@code keySet}
     * @implSpec The implementation in this interface returns a {@code SequencedSet} instance
     * that behaves as follows. Its {@link SequencedSet#add add} and {@link
     * SequencedSet#addAll addAll} methods throw {@link UnsupportedOperationException}.
     * Its {@link SequencedSet#reversed reversed} method returns the {@link
     * #sequencedKeySet sequencedKeySet} view of the {@link #reversed reversed} view of
     * this map. Each of its other methods calls the corresponding method of the {@link
     * #keySet keySet} view of this map.
     */
    @Override
    public SequencedSet<K> sequencedKeySet() {
        return super.sequencedKeySet();
    }

    /**
     * Returns a {@code SequencedCollection} view of this map's {@link #values values} collection.
     *
     * @return a {@code SequencedCollection} view of this map's {@code values} collection
     * @implSpec The implementation in this interface returns a {@code SequencedCollection} instance
     * that behaves as follows. Its {@link SequencedCollection#add add} and {@link
     * SequencedCollection#addAll addAll} methods throw {@link UnsupportedOperationException}.
     * Its {@link SequencedCollection#reversed reversed} method returns the {@link
     * #sequencedValues sequencedValues} view of the {@link #reversed reversed} view of
     * this map. Its {@link Object#equals equals} and {@link Object#hashCode hashCode} methods
     * are inherited from {@link Object}. Each of its other methods calls the corresponding
     * method of the {@link #values values} view of this map.
     */
    @Override
    public SequencedCollection<V> sequencedValues() {
        return super.sequencedValues();
    }

    /**
     * Returns a {@code SequencedSet} view of this map's {@link #entrySet entrySet}.
     *
     * @return a {@code SequencedSet} view of this map's {@code entrySet}
     * @implSpec The implementation in this interface returns a {@code SequencedSet} instance
     * that behaves as follows. Its {@link SequencedSet#add add} and {@link
     * SequencedSet#addAll addAll} methods throw {@link UnsupportedOperationException}.
     * Its {@link SequencedSet#reversed reversed} method returns the {@link
     * #sequencedEntrySet sequencedEntrySet} view of the {@link #reversed reversed} view of
     * this map. Each of its other methods calls the corresponding method of the {@link
     * #entrySet entrySet} view of this map.
     */
    @Override
    public SequencedSet<Entry<K, V>> sequencedEntrySet() {
        return super.sequencedEntrySet();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return super.entrySet();
    }

    @Override
    public Set<K> keySet() {
        return super.keySet();
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        super.forEach(action);
    }

    @Override
    public Collection<V> values() {
        return super.values();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
    }

    /**
     * Returns a reverse-ordered <a href="Collection.html#view">view</a> of this map.
     * Double reverse means original.
     *
     * @return original map
     */
    @Override
    public SequencedMap<K, V> reversed() {
        return decorated();
    }
}
