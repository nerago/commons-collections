package org.apache.commons.collections4.map;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableSet;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.EverythingMap;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.SortedMapRange;
import org.apache.commons.collections4.SortedRangedSet;
import org.apache.commons.collections4.iterators.EmptyIterator;
import org.apache.commons.collections4.iterators.EmptyOrderedMapIterator;
import org.apache.commons.collections4.spliterators.EmptyMapSpliterator;
import org.apache.commons.collections4.spliterators.MapSpliterator;

/**
 * A {@code Map} implementation that holds no items and is always empty.
 * Unlike standard JDK empty map this also implements all the other interfaces defined in commons collections.
 * <p>
 * If trying to modify the map, an UnsupportedOperationException is thrown.
 * @param <K> the type of the keys in this map (if it could have any)
 * @param <V> the type of the values in this map (if it could have any)
 * @since X.X
 */
public class EmptyMap<K, V> implements EverythingMap<K, V> {

    private static final long serialVersionUID = -5239565925081890488L;

    private static final EmptyMap<?, ?> instance = new EmptyMap<>();

    @SuppressWarnings("unchecked")
    public static <V, K> EmptyMap<K,V> emptyMap() {
        return (EmptyMap<K, V>) instance;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public int maxSize() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean isFull() {
        return true;
    }

    @Override
    public boolean containsKey(final Object key) {
        return false;
    }

    @Override
    public boolean containsValue(final Object value) {
        return false;
    }

    @Override
    public V get(final Object key) {
        return null;
    }

    @Override
    public V getOrDefault(final Object key, final V defaultValue) {
        return defaultValue;
    }

    @Override
    public boolean containsMapping(final Object key, final Object value) {
        return false;
    }

    @Override
    public K getKey(final Object value) {
        return null;
    }

    @Override
    public K getKeyOrDefault(final Object value, final K defaultKey) {
        return defaultKey;
    }

    @Override
    public void forEach(final BiConsumer<? super K, ? super V> action) {
    }

    @Override
    public V put(final K key, final V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V putIfAbsent(final K key, final V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V replace(final K key, final V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean replace(final K key, final V oldValue, final V newValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V computeIfAbsent(final K key, final Function<? super K, ? extends V> mappingFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V computeIfPresent(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V compute(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V merge(final K key, final V value, final BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAsBoolean(final Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(final Object key, final Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeValueAsBoolean(final Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(final Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(final MapIterator<? extends K, ? extends V> it) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public K removeValue(final Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void replaceAll(final BiFunction<? super K, ? super V, ? extends V> function) {
        throw new UnsupportedOperationException();
    }

    @Override
    public K firstKey() {
        return null;
    }

    @Override
    public K lastKey() {
        return null;
    }

    @Override
    public K nextKey(final K key) {
        return null;
    }

    @Override
    public K previousKey(final K key) {
        return null;
    }

    @Override
    public Entry<K, V> lowerEntry(final K key) {
        return null;
    }

    @Override
    public K lowerKey(final K key) {
        return null;
    }

    @Override
    public Entry<K, V> floorEntry(final K key) {
        return null;
    }

    @Override
    public K floorKey(final K key) {
        return null;
    }

    @Override
    public Entry<K, V> ceilingEntry(final K key) {
        return null;
    }

    @Override
    public K ceilingKey(final K key) {
        return null;
    }

    @Override
    public Entry<K, V> higherEntry(final K key) {
        return null;
    }

    @Override
    public K higherKey(final K key) {
        return null;
    }

    @Override
    public Entry<K, V> firstEntry() {
        return null;
    }

    @Override
    public Entry<K, V> lastEntry() {
        return null;
    }

    @Override
    public Entry<K, V> pollFirstEntry() {
        return null;
    }

    @Override
    public Entry<K, V> pollLastEntry() {
        return null;
    }

    @Override
    public SortedMapRange<K> getKeyRange() {
        return SortedMapRange.full(null);
    }

    @Override
    public SortedMapRange<V> getValueRange() {
        return SortedMapRange.full(null);
    }

    @Override
    public Comparator<? super K> comparator() {
        return null;
    }

    @Override
    public Comparator<? super V> valueComparator() {
        return null;
    }

    @Override
    public EmptyMap<K, V> subMap(final SortedMapRange<K> range) {
        return this;
    }

    @Override
    public EmptyMap<K, V> prefixMap(final K key) {
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public EmptyMap<V, K> inverseBidiMap() {
        return (EmptyMap<V, K>) this;
    }

    @Override
    public EmptyMap<K, V> descendingMap() {
        return this;
    }

    @Override
    public NavigableSet<K> navigableKeySet() {
        return Collections.emptyNavigableSet();
    }

    @Override
    public NavigableSet<K> descendingKeySet() {
        return Collections.emptyNavigableSet();
    }

    @Override
    public SortedRangedSet<K, ?> keySet() {
        return CollectionUtils.emptySet();
    }

    @Override
    public SortedRangedSet<K, ?> sequencedKeySet() {
        return CollectionUtils.emptySet();
    }

    @Override
    public SortedRangedSet<V, ?> values() {
        return CollectionUtils.emptySet();
    }

    @Override
    public SortedRangedSet<V, ?> sequencedValues() {
        return CollectionUtils.emptySet();
    }

    @Override
    public V firstValue() {
        return null;
    }

    @Override
    public V lastValue() {
        return null;
    }

    @Override
    public SortedRangedSet<Entry<K, V>, ?> entrySet() {
        return CollectionUtils.emptySet();
    }

    @Override
    public SortedRangedSet<Entry<K, V>, ?> sequencedEntrySet() {
        return CollectionUtils.emptySet();
    }

    @Override
    public OrderedMapIterator<K, V> mapIterator() {
        return EmptyOrderedMapIterator.emptyOrderedMapIterator();
    }

    @Override
    public OrderedMapIterator<K, V> descendingMapIterator() {
        return EmptyOrderedMapIterator.emptyOrderedMapIterator();
    }

    @Override
    public MapSpliterator<K, V> mapSpliterator() {
        return EmptyMapSpliterator.emptyMapSpliterator();
    }

    @Override
    public Iterator<Entry<K, V>> entryIterator() {
        return EmptyIterator.emptyIterator();
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this)
            return true;

        if (obj instanceof Map) {
            return ((Map<?, ?>) obj).isEmpty();
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "[]";
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
    }

    @Override
    public EverythingMap<V, K> reversed() {
        return this;
    }
}
