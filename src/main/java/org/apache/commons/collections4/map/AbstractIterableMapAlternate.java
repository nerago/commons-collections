package org.apache.commons.collections4.map;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.MutableBoolean;
import org.apache.commons.collections4.spliterators.MapSpliterator;
import org.apache.commons.collections4.spliterators.TransformMapSpliterator;
import org.apache.commons.collections4.spliterators.TransformSpliterator;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class AbstractIterableMapAlternate<K, V> extends AbstractIterableMap<K, V> {
    private static final long serialVersionUID = 4016005260054821088L;

    abstract Iterator<Map.Entry<K, V>> entryIterator();

    abstract MapSpliterator<K, V> mapSpliterator();

    @Override
    public abstract V getOrDefault(Object key, V defaultValue);

    @Override
    public final V get(final Object key) {
        return getOrDefault(key, null);
    }

    abstract V doPut(K key, V value, final boolean addIfAbsent, final boolean updateIfPresent);

    abstract V doPut(final K key,
                     final Function<? super K, ? extends V> absentFunc,
                     final BiFunction<? super K, ? super V, ? extends V> presentFunc,
                     final boolean saveNulls);

    @Override
    public final V put(final K key, final V value) {
        return doPut(key, value, true, true);
    }

    @Override
    public final V putIfAbsent(final K key, final V value) {
        return doPut(key, value, true, false);
    }

    @Override
    public final V replace(final K key, final V value) {
        return doPut(key, value, false, true);
    }

    @Override
    public final boolean replace(final K key, final V oldValue, final V newValue) {
        final MutableBoolean didUpdate = new MutableBoolean();
        doPut(key, null,
                (k, currentValue) -> {
                    if (Objects.equals(oldValue, currentValue)) {
                        didUpdate.flag = true;
                        return newValue;
                    } else {
                        return currentValue;
                    }
                }, true);
        return didUpdate.flag;
    }

    @Override
    public final V computeIfAbsent(final K key, final Function<? super K, ? extends V> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        return doPut(key, mappingFunction, null, false);
    }

    @Override
    public final V computeIfPresent(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        return doPut(key, null, remappingFunction, false);
    }

    @Override
    public final V compute(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        return doPut(key,
                k -> remappingFunction.apply(k, null),
                remappingFunction, false);
    }

    @Override
    public final V merge(final K key, final V value, final BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        return doPut(key,
                k -> value,
                (k, v) -> remappingFunction.apply(v, value), false);
    }

    abstract boolean removeAsBoolean(Object key);

    @Override
    public abstract boolean remove(Object key, Object value);

    @Override
    public int size() {
        return IteratorUtils.size(mapIterator());
    }

    @Override
    public boolean isEmpty() {
        return IteratorUtils.isEmpty(mapIterator());
    }

    @Override
    public void clear() {
        final MapIterator<K, V> iterator = mapIterator();
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
    }

    @Override
    public final Set<K> keySet() {
        return new AbsIterMapKeySet();
    }

    @Override
    public final Set<Map.Entry<K, V>> entrySet() {
        return new AbsIterMapEntrySet();
    }

    @Override
    public final Collection<V> values() {
        return new AbsIterMapValues();
    }

    private final class AbsIterMapKeySet extends AbstractSet<K> {
        @Override
        public Iterator<K> iterator() {
            return mapIterator();
        }

        @Override
        public Spliterator<K> spliterator() {
            return new TransformSpliterator<>(mapSpliterator(), Map.Entry::getKey);
        }

        @Override
        public int size() {
            return AbstractIterableMapAlternate.this.size();
        }

        @Override
        public boolean isEmpty() {
            return AbstractIterableMapAlternate.this.isEmpty();
        }

        @Override
        public boolean contains(final Object o) {
            return AbstractIterableMapAlternate.this.containsKey(o);
        }

        @Override
        public boolean remove(Object o) {
            return AbstractIterableMapAlternate.this.removeAsBoolean(o);
        }

        @Override
        public void clear() {
            AbstractIterableMapAlternate.this.clear();
        }
    }

    private final class AbsIterMapEntrySet extends AbstractSet<Map.Entry<K, V>> {
        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return entryIterator();
        }

        @Override
        public Spliterator<Map.Entry<K, V>> spliterator() {
            return mapSpliterator();
        }

        @Override
        public int size() {
            return AbstractIterableMapAlternate.this.size();
        }

        @Override
        public boolean isEmpty() {
            return AbstractIterableMapAlternate.this.isEmpty();
        }

        @Override
        public boolean contains(final Object o) {
            return AbstractIterableMapAlternate.this.containsKey(o);
        }

        @Override
        public boolean remove(Object o) {
            return AbstractIterableMapAlternate.this.removeAsBoolean(o);
        }

        @Override
        public void clear() {
            AbstractIterableMapAlternate.this.clear();
        }
    }

    private final class AbsIterMapValues extends AbstractCollection<V> {
        @Override
        public Iterator<V> iterator() {
            return new AbsIterMapValueIterator<>(mapIterator());
        }

        @Override
        public Spliterator<V> spliterator() {
            return new TransformMapSpliterator<>(mapSpliterator(), (k, v) -> v);
        }

        @Override
        public int size() {
            return AbstractIterableMapAlternate.this.size();
        }

        @Override
        public boolean isEmpty() {
            return AbstractIterableMapAlternate.this.isEmpty();
        }

        @Override
        public boolean contains(final Object o) {
            return AbstractIterableMapAlternate.this.containsKey(o);
        }

        @Override
        public boolean remove(Object o) {
            return AbstractIterableMapAlternate.this.removeAsBoolean(o);
        }

        @Override
        public void clear() {
            AbstractIterableMapAlternate.this.clear();
        }
    }

    private static class AbsIterMapValueIterator<V> implements Iterator<V> {
        private final MapIterator<?, V> mapIterator;

        public AbsIterMapValueIterator(final MapIterator<?, V> mapIterator) {
            this.mapIterator = mapIterator;
        }

        @Override
        public boolean hasNext() {
            return mapIterator.hasNext();
        }

        @Override
        public V next() {
            mapIterator.next()
            return mapIterator.getValue();
        }

        @Override
        public void remove() {
            mapIterator.remove();
        }
    }
}
