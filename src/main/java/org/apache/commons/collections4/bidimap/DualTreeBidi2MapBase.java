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
package org.apache.commons.collections4.bidimap;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.commons.collections4.IterableGet;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.NavigableExtendedBidiMap;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.ResettableIterator;
import org.apache.commons.collections4.SortedMapRange;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.collection.DualTransformedCollection;
import org.apache.commons.collections4.iterators.TransformIterator;
import org.apache.commons.collections4.keyvalue.AbstractMapEntryDecorator;
import org.apache.commons.collections4.keyvalue.UnmodifiableMapEntry;
import org.apache.commons.collections4.map.AbstractNavigableMapDecorator;
import org.apache.commons.collections4.set.AbstractNavigableSetDecorator;
import org.apache.commons.collections4.set.AbstractSetDecorator;

@SuppressWarnings("ClassWithTooManyFields")
public abstract class DualTreeBidi2MapBase<K extends Comparable<K>, V extends Comparable<V>>
        extends AbstractNavigableMapDecorator<K, V, NavigableMap<K, V>, DualTreeBidi2MapBase<K, V>>
        implements NavigableExtendedBidiMap<K, V, DualTreeBidi2MapBase<K, V>, DualTreeBidi2MapBase<V, K>> {
    protected static final boolean treeMapImplementsRemove2 = checkNotDefault("remove", Object.class, Object.class);
    protected static final boolean treeMapImplementsReplace3 = checkNotDefault("replace", Object.class, Object.class, Object.class);
    protected static final boolean treeMapImplementsPutIfAbsent = checkNotDefault("putIfAbsent", Object.class, Object.class);

    private static final long serialVersionUID = 1440688169573746936L;

    protected NavigableMap<K, V> keyMap;
    protected NavigableMap<V, K> valueMap;
    protected Comparator<? super K> keyComparator;
    protected Comparator<? super V> valueComparator;
    protected final SortedMapRange<V> valueRange;

    private DualTreeBidi2MapBase<V, K> inverseBidiMap;
    private DualTreeBidi2MapBase<K,V> descendingBidiMap;
    private NavigableSet<K> keySet;
    private NavigableSet<K> keySetDescending;
    private Set<V> valueSet;
    private Set<Entry<K, V>> entrySet;

    protected DualTreeBidi2MapBase(final NavigableMap<K, V> keyMap, final SortedMapRange<K> keyRange,
                                   final NavigableMap<V, K> valueMap, final SortedMapRange<V> valueRange) {
        super(keyMap, keyRange);
        this.keyMap = keyMap;
        this.keyComparator = Objects.requireNonNull(keyMap.comparator());
        this.valueMap = valueMap;
        this.valueRange = valueRange;
        this.valueComparator = Objects.requireNonNull(valueMap.comparator());
    }

    @SuppressWarnings("unchecked")
    protected final K castKey(final Object keyObject) {
        return (K) Objects.requireNonNull(keyObject);
    }

    @SuppressWarnings("unchecked")
    protected final V castValue(final Object valueObject) {
        return (V) Objects.requireNonNull(valueObject);
    }

    protected final boolean keyEquals(final K a, final K b) {
        return (a != null && b != null && keyComparator.compare(a, b) == 0) || (a == b);
    }

    protected final boolean valueEquals(final V a, final V b) {
        return (a != null && b != null && valueComparator.compare(a, b) == 0) || (a == b);
    }

    @Override
    protected abstract DualTreeBidi2MapBase<K, V> decorateDerived(final NavigableMap<K, V> subMap, final SortedMapRange<K> keyRange);

    @Override
    public final SortedMapRange<V> getValueRange() {
        return valueRange;
    }

    @Override
    public final DualTreeBidi2MapBase<V, K> inverseBidiMap() {
        if (inverseBidiMap == null) {
            inverseBidiMap = createInverse();
            inverseBidiMap.inverseBidiMap = this;
        }
        return inverseBidiMap;
    }

    @Override
    public final NavigableMap<K, V> descendingMap() {
        if (descendingBidiMap == null)
            descendingBidiMap = createDescending();
        return descendingBidiMap;
    }

    protected abstract DualTreeBidi2Map<K, V> primaryMap();
    protected abstract DualTreeBidi2MapBase<K, V> createDescending();
    protected abstract DualTreeBidi2MapBase<V, K> createInverse();

    @Override
    public Comparator<? super K> comparator() {
        return keyComparator;
    }

    @Override
    public Comparator<? super V> valueComparator() {
        return valueComparator;
    }

    protected abstract void modified();

    protected abstract int modificationCount();

    @Override
    public boolean containsValue(final Object valueObject) {
        final V value = castValue(valueObject);
        return valueMap.containsKey(value);
    }

    @Override
    public K getKey(final Object valueObject) {
        final V value = castValue(valueObject);
        return valueMap.get(value);
    }

    @Override
    public K getKeyOrDefault(final Object valueObject, final K defaultKey) {
        final V value = castValue(valueObject);
        return valueMap.getOrDefault(value, defaultKey);
    }

    @Override
    public K removeValue(final Object valueObject) {
        final V value = castValue(valueObject);
        final K key = valueMap.get(value);
        if (key == null)
            return null;
        keyMapRemoveChecked(key, value);
        valueMapRemoveChecked(value, key);
        modified();
        return key;
    }

    protected boolean removeValueViaCollection(final Object valueObject) {
        final V value = castValue(valueObject);
        final K key = valueMap.get(value);
        if (key == null)
            return false;
        keyMapRemoveChecked(key, value);
        valueMapRemoveChecked(value, key);
        modified();
        return true;
    }

    @Override
    public void replaceAll(final BiFunction<? super K, ? super V, ? extends V> function) {
        Objects.requireNonNull(function);
        for (final Entry<K, V> entry : keyMap.entrySet()) {
            final K key = entry.getKey();
            final V oldValue = entry.getValue();

            final V newValue = function.apply(key, oldValue);

            if (updateValueMapDuringKeyMapIteration(key, oldValue, newValue)) {
                entry.setValue(newValue);
            }
        }
        modified();
    }

    protected boolean collectionRemoveIf(final Predicate<? super Entry<K, V>> filter) {
        final Iterator<Entry<K, V>> iterator = keyMap.entrySet().iterator();
        boolean changed = false;
        while (iterator.hasNext()) {
            final Entry<K, V> entry = iterator.next();
            if (filter.test(entry)) {
                valueMapRemoveChecked(entry.getValue(), entry.getKey());
                iterator.remove();
                changed = true;
            }
        }
        if (changed)
            modified();
        return changed;
    }

    @Override
    public V put(final K key, final V newValue) {
        final V currentValue = keyMap.get(key);

        if (currentValue == null) {
            keyMapAddChecked(key, newValue);
            updateValueMapForNewValue(key, newValue);
            modified();
            return null;
        } else if (valueEquals(newValue, currentValue)) {
            return currentValue;
        } else {
            keyMapReplaceChecked(key, currentValue, newValue);
            valueMapRemoveChecked(currentValue, key);
            updateValueMapForNewValue(key, newValue);
            modified();
            return currentValue;
        }
    }

    protected void putWithKnownState(final K key, final V oldValue, final V newValue) {
        if (!valueEquals(oldValue, newValue)) {
            keyMapReplaceChecked(key, oldValue, newValue);
            valueMapRemoveChecked(oldValue, key);
            updateValueMapForNewValue(key, newValue);
            modified();
        }
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> mapToCopy) {
        if (mapToCopy instanceof IterableGet) {
            final IterableGet<? extends K,? extends V> iterableMap = (IterableGet<? extends K, ? extends V>) mapToCopy;
            final MapIterator<? extends K, ? extends V> mapIterator = iterableMap.mapIterator();
            putAll(mapIterator);
        } else {
            final Iterator<? extends Entry<? extends K, ? extends V>> iterator = mapToCopy.entrySet().iterator();
            while (iterator.hasNext()) {
                final Entry<? extends K, ? extends V> entry = iterator.next();
                put(entry.getKey(), entry.getValue());
            }
        }
        modified();
    }

    @Override
    public void putAll(final MapIterator<? extends K, ? extends V> mapIterator) {
        while (mapIterator.hasNext()) {
            final K key = mapIterator.next();
            final V value = mapIterator.getValue();
            put(key, value);
        }
    }

    @Override
    public V remove(final Object keyObject) {
        final K key = castKey(keyObject);
        if (keyMap.containsKey(key)) {
            final V value = keyMap.remove(key);
            valueMapRemoveChecked(value, key);
            modified();
            return value;
        } else {
            return null;
        }
    }

    protected boolean removeViaCollection(final Object keyObject) {
        final K key = castKey(keyObject);
        if (keyMap.containsKey(key)) {
            final V value = keyMap.remove(key);
            valueMapRemoveChecked(value, key);
            modified();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean remove(final Object keyObject, final Object valueObject) {
        final K key = castKey(keyObject);
        final V value = castValue(valueObject);
        if (keyMap.remove(key, value)) {
            valueMapRemoveChecked(value, key);
            modified();
            return true;
        } else {
            return false;
        }
    }

    protected boolean removeViaCollection(final K key, final V value) {
        if (treeMapImplementsRemove2) {
            if (keyMap.remove(key, value)) {
                valueMapRemoveChecked(value, key);
                modified();
                return true;
            }
            return false;
        } else {
            final V currentValue = keyMap.get(key);
            if (currentValue == null || !valueEquals(currentValue, value))
                return false;
            keyMapRemoveChecked(key, value);
            valueMapRemoveChecked(value, key);
            modified();
            return true;
        }
    }

    protected void removeInternalExpectedGood(final K key, final V value) {
        keyMapRemoveChecked(key, value);
        valueMapRemoveChecked(value, key);
        modified();
    }

    @Override
    public boolean replace(final K key, final V oldValue, final V newValue) {
        if (valueEquals(oldValue, newValue))
            return false;
        if (!keyMap.replace(key, oldValue, newValue))
            return false;
        valueMapRemoveChecked(oldValue, key);
        updateValueMapForNewValue(key, newValue);
        modified();
        return true;
    }

    @Override
    public V replace(final K key, final V newValue) {
        final V currentValue = keyMap.get(key);
        if (currentValue == null)
            return null;
        else if (valueEquals(newValue, currentValue))
            return currentValue;

        keyMapReplaceChecked(key, currentValue, newValue);
        valueMapRemoveChecked(currentValue, key);

        updateValueMapForNewValue(key, newValue);
        modified();

        return currentValue;
    }

    @Override
    public V computeIfPresent(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);

        final V currentValue = keyMap.get(key);
        if (currentValue == null)
            return null;

        final V newValue = remappingFunction.apply(key, currentValue);
        if (valueEquals(newValue, currentValue)) {
            return currentValue;
        } else if (newValue == null) {
            keyMapRemoveChecked(key, currentValue);
            valueMapRemoveChecked(currentValue, key);
            modified();
            return null;
        } else {
            keyMapReplaceChecked(key, currentValue, newValue);
            valueMapRemoveChecked(currentValue, key);
            updateValueMapForNewValue(key, newValue);
            modified();
            return newValue;
        }
    }

    @Override
    public V putIfAbsent(final K key, final V newValue) {
        final V currentValue = keyMap.get(key);
        if (currentValue == null) {
            keyMapAddChecked(key, newValue);
            updateValueMapForNewValue(key, newValue);
            modified();
            return null;
        } else {
            return currentValue;
        }
    }

    @Override
    public V computeIfAbsent(final K key, final Function<? super K, ? extends V> mappingFunction) {
        Objects.requireNonNull(mappingFunction);

        final V currentValue = keyMap.get(key);
        if (currentValue == null) {
            final V newValue = mappingFunction.apply(key);
            if (newValue != null) {
                keyMapAddChecked(key, newValue);
                updateValueMapForNewValue(key, newValue);
                modified();
            }
            return newValue;
        } else {
            return currentValue;
        }
    }

    @Override
    public V compute(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);

        final V currentValue = keyMap.get(key);
        final V newValue = remappingFunction.apply(key, currentValue);

        if (newValue == null && currentValue == null) {
            return null;
        } else if (newValue == null) {
            keyMapRemoveChecked(key, currentValue);
            valueMapRemoveChecked(currentValue, key);
            modified();
            return null;
        } else if (currentValue == null) {
            keyMapAddChecked(key, newValue);
            updateValueMapForNewValue(key, newValue);
            modified();
            return newValue;
        } else if (valueEquals(newValue, currentValue)) {
            return currentValue;
        } else {
            keyMapReplaceChecked(key, currentValue, newValue);
            valueMapRemoveChecked(currentValue, key);
            updateValueMapForNewValue(key, newValue);
            modified();
            return newValue;
        }
    }

    @Override
    public V merge(final K key, final V value, final BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        final int expectedModifications = modificationCount();
        Objects.requireNonNull(remappingFunction);

        final V currentValue = keyMap.get(key);

        if (currentValue == null) {
            keyMapAddChecked(key, value);
            updateValueMapForNewValue(key, value);
            modified();
            return value;
        }

        final V newValue = remappingFunction.apply(currentValue, value);

        if (expectedModifications != modificationCount()) {
            throw new ConcurrentModificationException();
        }

        if (newValue == null) {
            keyMapRemoveChecked(key, currentValue);
            valueMapRemoveChecked(currentValue, key);
            modified();
            return null;
        } else if (valueEquals(newValue, currentValue)) {
            return currentValue;
        } else {
            keyMapReplaceChecked(key, currentValue, newValue);
            valueMapRemoveChecked(currentValue, key);
            updateValueMapForNewValue(key, newValue);
            modified();
            return newValue;
        }
    }

    @Override
    public void clear() {
        keyMap.clear();
        valueMap.clear();
        modified();
    }

    @Override
    public Entry<K, V> pollFirstEntry() {
        final Entry<K, V> entry = keyMap.pollFirstEntry();
        if (entry != null) {
            valueMapRemoveChecked(entry.getValue(), entry.getKey());
            modified();
        }
        return entry;
    }

    @Override
    public Entry<K, V> pollLastEntry() {
        final Entry<K, V> entry = keyMap.pollLastEntry();
        if (entry != null) {
            valueMapRemoveChecked(entry.getValue(), entry.getKey());
            modified();
        }
        return entry;
    }

    @Override
    public final NavigableSet<K> navigableKeySet() {
        if (keySet == null)
            keySet = createKeySet(false);
        return keySet;
    }

    @Override
    public final NavigableSet<K> descendingKeySet() {
        if (keySetDescending == null)
            keySetDescending = createKeySet(true);
        return keySetDescending;
    }

    @Override
    public final Set<K> keySet() {
        return navigableKeySet();
    }

    @Override
    public final Set<Entry<K, V>> entrySet() {
        if (entrySet == null)
            entrySet = createEntrySet();
        return entrySet;
    }

    @Override
    public final Set<V> values() {
        if (valueSet == null)
            valueSet = createValueSet();
        return valueSet;
    }

    protected abstract NavigableSet<K> createKeySet(boolean descending);

    protected abstract Set<V> createValueSet();

    protected abstract Set<Entry<K, V>> createEntrySet();

    @Override
    public OrderedMapIterator<K, V> mapIterator() {
        return new DualTreeMapIterator<>(keyMap, this);
    }

    @Override
    public DualTreeBidi2MapBase<K, V> subMap(final SortedMapRange<K> range) {
        return (DualTreeBidi2MapBase<K, V>) super.subMap(range);
    }

    protected boolean updateValueMapDuringKeyMapIteration(final K key, final V oldValue, final V newValue) {
        if (!valueEquals(oldValue, newValue)) {
            if (valueMap.containsKey(newValue))
                throw new ValueChangeNotAllowedException("value exists for another key and can't update during iteration");
            valueMapRemoveChecked(oldValue, key);
            valueMapAddChecked(newValue, key);
            return true;
        } else {
            return false;
        }
    }

    protected void updateValueMapForNewValue(final K key, final V newValue) {
        final K newValueOldKey = valueMap.get(newValue);
        if (newValueOldKey != null) {
            valueMapReplaceChecked(newValue, newValueOldKey, key);
            keyMapRemoveChecked(newValueOldKey, newValue);
        } else {
            valueMapAddChecked(newValue, key);
        }
    }

    protected void keyMapAddChecked(final K key, final V value) {
        if (treeMapImplementsPutIfAbsent) {
            // putIfAbsent is close to the semantics we're looking for
            // however it's willing to overwrite an unexpected null entry
            // is still fine in the normal non-exceptional case
            if (keyMap.putIfAbsent(key, value) != null)
                throw new IllegalStateException();
        } else {
            // not ideal null characteristics - doesn't detect a null value
            if (keyMap.put(key, value) != null)
                throw new IllegalStateException();
        }
    }

    protected void keyMapReplaceChecked(final K key, final V oldValue, final V newValue) {
        if (treeMapImplementsReplace3) {
            if (!keyMap.replace(key, oldValue, newValue))
                throw new IllegalStateException();
        } else {
            final V previous = keyMap.put(key, newValue);
            if (!valueEquals(previous, oldValue))
                throw new IllegalStateException();
        }
    }

    protected void keyMapRemoveChecked(final K key, final V expectedValue) {
        if (treeMapImplementsRemove2) {
            if (!keyMap.remove(key, expectedValue))
                throw new IllegalStateException();
        } else {
            final V previous = keyMap.remove(key);
            if (!valueEquals(expectedValue, previous))
                throw new IllegalStateException();
        }
    }

    protected void valueMapAddChecked(final V lookup, final K key) {
        // see keyMapAddChecked comments for limitations
        if (treeMapImplementsPutIfAbsent) {
            if (valueMap.putIfAbsent(lookup, key) != null)
                throw new IllegalStateException();
        } else {
            if (valueMap.put(lookup, key) != null)
                throw new IllegalStateException();
        }
    }

    protected void valueMapReplaceChecked(final V lookup, final K oldKey, final K newKey) {
        if (treeMapImplementsReplace3) {
            if (!valueMap.replace(lookup, oldKey, newKey))
                throw new IllegalStateException();
        } else {
            final K previous = valueMap.put(lookup, newKey);
            if (!keyEquals(previous, oldKey))
                throw new IllegalStateException();
        }
    }

    protected void valueMapRemoveChecked(final V lookup, final K content) {
        if (treeMapImplementsRemove2) {
            if (!valueMap.remove(lookup, content))
                throw new IllegalStateException();
        } else {
            final K previous = valueMap.remove(lookup);
            if (!keyEquals(content, previous))
                throw new IllegalStateException();
        }
    }

    protected static <K extends Comparable<K>, V extends Comparable<V>> K getKeyNullSafe(final Entry<K, V> entry) {
        if (entry != null) {
            return entry.getKey();
        } else {
            return null;
        }
    }

    protected static class ValueChangeNotAllowedException extends IllegalArgumentException {
        private static final long serialVersionUID = 5803288170895632334L;

        public ValueChangeNotAllowedException(final String msg) {
            super(msg);
        }
    }

    // don't really make full use of DualTransformedCollection, other options exist
    protected abstract static class BaseTransformedEntrySet<E, K extends Comparable<K>, V extends Comparable<V>>
            extends DualTransformedCollection<E, Entry<K, V>> implements Set<E> {
        private static final long serialVersionUID = -1225190739881897633L;

        protected final DualTreeBidi2MapBase<K, V> parent;

        public BaseTransformedEntrySet(final Set<Entry<K, V>> entrySet,
                                       final Transformer<? super Entry<K, V>, ? extends E> entryToElement,
                                       final DualTreeBidi2MapBase<K, V> parent) {
            super(entrySet, null, entryToElement);
            this.parent = parent;
        }

        public boolean add(final E object) {
            throw new UnsupportedOperationException();
        }

        public boolean addAll(final Collection<? extends E> coll) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isEmpty() {
            return parent.isEmpty();
        }

        @Override
        public int size() {
            return parent.size();
        }

        @Override
        public void clear() {
            parent.clear();
        }

        @Override
        public boolean removeAll(final Collection<?> coll) {
            // from abstractdualbidimap
            if (parent.isEmpty() || coll.isEmpty()) {
                return false;
            }
            boolean modified = false;
            for (final Object current : coll) {
                modified |= remove(current);
            }
            return modified;
        }

        @Override
        public boolean retainAll(final Collection<?> coll) {
            // from abstractdualbidimap
            if (parent.isEmpty()) {
                return false;
            }
            if (coll.isEmpty()) {
                parent.clear();
                return true;
            }
            boolean modified = false;
            final Iterator<E> it = iterator();
            while (it.hasNext()) {
                if (!coll.contains(it.next())) {
                    it.remove();
                    modified = true;
                }
            }
            if (modified)
                parent.modified();
            return modified;
        }
    }

    private abstract static class BaseFilteredEntrySet<E, K extends Comparable<K>, V extends Comparable<V>>
            extends BaseTransformedEntrySet<E, K, V>{

        public BaseFilteredEntrySet(final Set<Entry<K, V>> entrySet, final Transformer<? super Entry<K, V>, ? extends E> entryToElement, final DualTreeBidi2MapBase<K, V> parent) {
            super(entrySet, entryToElement, parent);
        }

//        @Override
//        public boolean contains(Object o) {
//            return super.contains(o);
//        }

//        @Override
//        public boolean remove(Object o) {
//            return super.remove(o);
//        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return super.containsAll(c);
        }

        @Override
        public void forEach(Consumer<? super E> action) {
            super.forEach(action);
        }

        @Override
        public boolean removeIf(Predicate<? super E> filter) {
            return super.removeIf(filter);
        }

        @Override
        public Iterator<E> iterator() {
            return super.iterator();
        }

        @Override
        public Spliterator<E> spliterator() {
            return super.spliterator();
        }
    }

    protected abstract static class BaseNavigableSet<E, K extends Comparable<K>, V extends Comparable<V>>
            extends AbstractNavigableSetDecorator<E, NavigableSet<E>, BaseNavigableSet<E, K, V>> {
        private static final long serialVersionUID = -1231087977922107905L;

        protected final DualTreeBidi2MapBase<K, V> parent;

        protected BaseNavigableSet(final NavigableSet<E> set, final SortedMapRange<E> range, final DualTreeBidi2MapBase<K, V> parent) {
            super(set, range);
            this.parent = parent;
        }

        public boolean add(final E object) {
            throw new UnsupportedOperationException();
        }

        public boolean addAll(final Collection<? extends E> coll) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            parent.clear();
        }

        @Override
        public boolean removeAll(final Collection<?> coll) {
            // from abstractdualbidimap
            if (parent.isEmpty() || coll.isEmpty()) {
                return false;
            }
            boolean modified = false;
            for (final Object current : coll) {
                modified |= remove(current);
            }
            return modified;
        }

        @Override
        public boolean retainAll(final Collection<?> coll) {
            // from abstractdualbidimap
            if (parent.isEmpty()) {
                return false;
            }
            if (coll.isEmpty()) {
                parent.clear();
                return true;
            }
            boolean modified = false;
            final Iterator<E> it = iterator();
            while (it.hasNext()) {
                if (!coll.contains(it.next())) {
                    it.remove();
                    modified = true;
                }
            }
            if (modified)
                parent.modified();
            return modified;
        }
    }

    protected static final class KeySetUsingKeyMapFullRange<K extends Comparable<K>, V extends Comparable<V>>
            extends BaseNavigableSet<K, K, V> {
        private static final long serialVersionUID = 2724700086572295708L;

        KeySetUsingKeyMapFullRange(final DualTreeBidi2Map<K, V> parent, final boolean descending) {
            super(descending ? parent.keyMap.descendingKeySet() : parent.keyMap.navigableKeySet(), parent.getKeyRange(), parent);
        }

        @Override
        protected BaseNavigableSet<K, K, V> decorateDerived(final NavigableSet<K> subSet, final SortedMapRange<K> range) {
            return new KeySetUsingKeyMapSubSet<>(subSet, range, parent);
        }

        @Override
        public Iterator<K> iterator() {
            return new KeyIterator<>(() -> decorated().iterator(), parent.primaryMap());
        }

        @Override
        public Iterator<K> descendingIterator() {
            return new KeyIterator<>(() -> decorated().descendingIterator(), parent.primaryMap());
        }

        @Override
        public boolean remove(final Object object) {
            return parent.removeViaCollection(object);
        }

        @Override
        public boolean removeIf(final Predicate<? super K> filter) {
            // ignores set order
            Objects.requireNonNull(filter);
            return parent.collectionRemoveIf(e -> filter.test(e.getKey()));
        }

        @Override
        public K pollFirst() {
            return getKeyNullSafe(parent.pollFirstEntry());
        }

        @Override
        public K pollLast() {
            return getKeyNullSafe(parent.pollLastEntry());
        }
    }

    // TODO QUERY if (valueRange.isFull())
    protected static final class KeySetUsingKeyMapSubSet<K extends Comparable<K>, V extends Comparable<V>>
            extends BaseNavigableSet<K, K, V> {
        private static final long serialVersionUID = 2724700086572295708L;

        KeySetUsingKeyMapSubSet(final DualTreeBidi2MapSubMap<K, V> parent, final boolean descending) {
            super(descending ? parent.keyMap.descendingKeySet() : parent.keyMap.navigableKeySet(), parent.getKeyRange(), parent);
        }

        private KeySetUsingKeyMapSubSet(final NavigableSet<K> subSet, final SortedMapRange<K> range, final DualTreeBidi2MapBase<K, V> parent) {
            super(subSet, range, parent);
        }

        @Override
        protected BaseNavigableSet<K, K, V> decorateDerived(final NavigableSet<K> subSet, final SortedMapRange<K> range) {
            return new KeySetUsingKeyMapSubSet<>(subSet, range, parent);
        }

        @Override
        public Iterator<K> iterator() {
            // TODO should this use entryset so can filter?
            return new KeyIterator<>(() -> decorated().iterator(), parent.primaryMap());
        }

        @Override
        public Iterator<K> descendingIterator() {
            return new KeyIterator<>(() -> decorated().descendingIterator(), parent.primaryMap());
        }

        @Override
        public boolean remove(final Object object) {
            final K key = parent.castKey(object);
            if (decorated().contains(key)) {
                return parent.removeViaCollection(key);
            } else {
                return false;
            }
        }

        @Override
        public boolean removeIf(final Predicate<? super K> filter) {
            return parent.collectionRemoveIf(e -> filter.test(e.getKey()));
        }

        @Override
        public K pollFirst() {
            if (!decorated().isEmpty()) {
                // TODO not confirming value in range
                final K key = decorated().first();
                final V value = parent.keyMap.remove(key);
                parent.valueMapRemoveChecked(value, key);
                parent.modified();
                return key;
            } else {
                return null;
            }
        }

        @Override
        public K pollLast() {
            if (!decorated().isEmpty()) {
                // TODO not confirming value in range
                final K key = decorated().last();
                final V value = parent.keyMap.remove(key);
                parent.valueMapRemoveChecked(value, key);
                parent.modified();
                return key;
            } else {
                return null;
            }
        }
    }

    protected static class ValueSetUsingKeyEntrySetFullRange<K extends Comparable<K>, V extends Comparable<V>>
            extends BaseTransformedEntrySet<V, K, V> {
        private static final long serialVersionUID = 5296117687879550829L;

        private final DualTreeBidi2Map<K, V> parent;

        public ValueSetUsingKeyEntrySetFullRange(final DualTreeBidi2Map<K,V> parent) {
            super(parent.keyMap.entrySet(), Entry::getValue, parent);
            this.parent = parent;
        }

        @Override
        public Iterator<V> iterator() {
            return new ValueIterator<>(() -> new TransformIterator<>(decorated().iterator(), Entry::getValue), parent.primaryMap());
        }

        @Override
        public boolean remove(final Object object) {
            return parent.removeValueViaCollection(object);
        }

        @Override
        public boolean contains(final Object object) {
            return parent.containsValue(object);
        }
    }

    protected static class ValueSetUsingKeyEntrySetSubSet<K extends Comparable<K>, V extends Comparable<V>>
            extends BaseTransformedEntrySet<V, K, V> {
        private static final long serialVersionUID = 5296117687879550829L;

        private final DualTreeBidi2MapSubMap<K, V> parent;

        public ValueSetUsingKeyEntrySetSubSet(final DualTreeBidi2MapSubMap<K,V> parent) {
            super(parent.keyMap.entrySet(), Entry::getValue, parent);
            this.parent = parent;
        }

        @Override
        public Iterator<V> iterator() {
            return new ValueIterator<>(() -> new TransformIterator<>(decorated().iterator(), Entry::getValue), parent.primaryMap());
        }

        @Override
        public boolean remove(final Object object) {
            return parent.removeValueViaCollection(object);
        }

        @Override
        public boolean contains(final Object object) {
            return parent.containsValue(object);
        }
    }

    static final class ValueSetUsingKeyEntrySetFiltered<K extends Comparable<K>, V extends Comparable<V>>
        extends BaseFilteredEntrySet<V, K, V> {
        public ValueSetUsingKeyEntrySetFiltered(DualTreeBidi2MapSubMap<K, V> parent) {
            super(parent.keyMap.entrySet(), Entry::getValue, parent);
        }
    }

    protected static class EntrySetUsingKeyMap<K extends Comparable<K>, V extends Comparable<V>>
            extends AbstractSetDecorator<Entry<K, V>, Set<Entry<K, V>>> {
        private static final long serialVersionUID = -1069719982246820666L;

        protected final DualTreeBidi2MapBase<K, V> parent;

        protected EntrySetUsingKeyMap(final DualTreeBidi2MapBase<K, V> parent) {
            super(parent.keyMap.entrySet());
            this.parent = parent;
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new EntryIterator<>(() -> decorated().iterator(), parent.primaryMap());
        }

        @Override
        public boolean add(final Entry<K, V> object) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(final Collection<? extends Entry<K, V>> coll) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            parent.clear();
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean remove(final Object object) {
            if (!(object instanceof Map.Entry)) {
                return false;
            }
            final Entry<K, V> entry = (Entry<K, V>) object;
            return parent.removeViaCollection(entry.getKey(), entry.getValue());
        }

        @Override
        public boolean removeIf(final Predicate<? super Entry<K, V>> filter) {
            return parent.collectionRemoveIf(filter);
        }

        @Override
        public boolean removeAll(final Collection<?> coll) {
            // from abstractdualbidimap
            if (parent.isEmpty() || coll.isEmpty()) {
                return false;
            }
            boolean modified = false;
            for (final Object current : coll) {
                modified |= remove(current);
            }
            return modified;
        }

        @Override
        public boolean retainAll(final Collection<?> coll) {
            // from abstractdualbidimap
            if (parent.isEmpty()) {
                return false;
            }
            if (coll.isEmpty()) {
                parent.clear();
                return true;
            }
            boolean modified = false;
            final Iterator<Entry<K, V>> it = iterator();
            while (it.hasNext()) {
                if (!coll.contains(it.next())) {
                    it.remove();
                    modified = true;
                }
            }
            if (modified)
                parent.modified();
            return modified;
        }
    }

    protected abstract static class BaseIterator<K extends Comparable<K>, V extends Comparable<V>, E> implements ResettableIterator<E> {
        /**
         * The parent map
         */
        protected final DualTreeBidi2Map<K, V> primaryMap;
        protected final Supplier<Iterator<E>> makeIterator;
        protected Iterator<E> iterator;
        /**
         * The last returned key/entry/value
         */
        protected E lastResult;

        /**
         * Whether remove is allowed at present
         */
        protected boolean canRemove;
        protected int expectedModCount;

        protected BaseIterator(final Supplier<Iterator<E>> makeIterator, final DualTreeBidi2Map<K, V> primaryMap) {
            this.primaryMap = primaryMap;
            this.makeIterator = makeIterator;
            this.expectedModCount = primaryMap.modificationCount();
            reset();
        }

        @Override
        public void reset() {
            lastResult = null;
            canRemove = false;
            iterator = makeIterator.get();
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public E next() {
            final E result = iterator.next();
            canRemove = true;
            lastResult = result;
            return result;
        }

        @Override
        public void remove() {
            if (!canRemove)
                throw new IllegalStateException();
            if (primaryMap.modificationCount() != expectedModCount)
                throw new ConcurrentModificationException();
            removeExtras(lastResult);
            iterator.remove(); // removes from the iterators map
            primaryMap.modified();
            expectedModCount = primaryMap.modificationCount();
            lastResult = null;
            canRemove = false;
        }

        protected abstract void removeExtras(E result);

        @Override
        public void forEachRemaining(final Consumer<? super E> action) {
            iterator.forEachRemaining(action);
        }
    }

    protected static class KeyIterator<K extends Comparable<K>, V extends Comparable<V>>
            extends BaseIterator<K, V, K> {
        protected KeyIterator(final Supplier<Iterator<K>> makeIterator, final DualTreeBidi2Map<K, V> primaryMap) {
            // normally receives keyMap.navigableKeySet().iterator()
            super(makeIterator, primaryMap);
        }

        @Override
        protected void removeExtras(final K key) {
            final V value = primaryMap.keyMap.get(key);
            primaryMap.valueMapRemoveChecked(value, key);
        }
    }

    protected static class ValueIterator<K extends Comparable<K>, V extends Comparable<V>>
            extends BaseIterator<K, V, V> {
        public ValueIterator(final Supplier<Iterator<V>> makeIterator, final DualTreeBidi2Map<K, V> primaryMap) {
            // normally receives valueMap.navigableKeySet().iterator()
            super(makeIterator, primaryMap);
        }

        @Override
        protected void removeExtras(final V value) {
            final K key = primaryMap.valueMap.get(value);
            primaryMap.keyMapRemoveChecked(key, value);
        }
    }

    protected static class ValueFilteredIterator<K extends Comparable<K>, V extends Comparable<V>>
            extends BaseIterator<K, V, V> {
        public ValueFilteredIterator(final Supplier<Iterator<V>> makeIterator, final DualTreeBidi2Map<K, V> primaryMap) {
            // normally receives valueMap.navigableKeySet().iterator() - nope
            super(makeIterator, primaryMap);
        }

        @Override
        protected void removeExtras(final V value) {
            final K key = primaryMap.valueMap.get(value);
            primaryMap.keyMapRemoveChecked(key, value);
        }
    }

    protected static class EntryIterator<K extends Comparable<K>, V extends Comparable<V>>
            extends BaseIterator<K, V, Entry<K, V>> {
        protected EntryIterator(final Supplier<Iterator<Entry<K, V>>> makeIterator, final DualTreeBidi2Map<K, V> primaryMap) {
            // normally receives keyMap.entrySet().iterator()
            super(makeIterator, primaryMap);
        }

        @Override
        public Entry<K, V> next() {
            return new EntryWithSetValue<>(super.next(), primaryMap);
        }

        @Override
        public void forEachRemaining(final Consumer<? super Entry<K, V>> action) {
            // TODO should fail a test
            super.forEachRemaining(action);
        }

        @Override
        protected void removeExtras(final Entry<K, V> entry) {
            primaryMap.valueMapRemoveChecked(entry.getValue(), entry.getKey());
        }

        protected static class EntryWithSetValue<K extends Comparable<K>, V extends Comparable<V>>
                extends AbstractMapEntryDecorator<K, V> {
            protected final DualTreeBidi2Map<K, V> parent;

            protected EntryWithSetValue(final Entry<K, V> entry, final DualTreeBidi2Map<K, V> parent) {
                super(entry);
                this.parent = parent;
            }

            @Override
            public V setValue(final V value) {
                final Entry<K, V> entry = getMapEntry();
                if (parent.updateValueMapDuringKeyMapIteration(entry.getKey(), entry.getValue(), value)) {
                    return getMapEntry().setValue(value);
                } else {
                    return entry.getValue();
                }
            }
        }
    }

    protected static class DualTreeMapIterator<K extends Comparable<K>, V extends Comparable<V>>
            implements OrderedMapIterator<K, V>, ResettableIterator<K> {

        protected final DualTreeBidi2MapBase<K, V> parent;
        protected final NavigableMap<K, V> map;
        protected boolean forward;
        protected Entry<K, V> previousEntry;
        protected Entry<K, V> nextEntry;
        protected Entry<K, V> latestResult;

        protected DualTreeMapIterator(final NavigableMap<K, V> map, final DualTreeBidi2MapBase<K, V> parent) {
            this.map = map;
            this.parent = parent;
            reset();
        }

        @Override
        public void reset() {
            forward = true;
            nextEntry = map.firstEntry();
            previousEntry = null;
        }

        @Override
        public boolean hasNext() {
            return nextEntry != null;
        }

        @Override
        public boolean hasPrevious() {
            return previousEntry != null;
        }

        @Override
        public K next() {
            final Entry<K, V> result = nextEntry;
            if (result == null)
                throw new NoSuchElementException();
            final K key = result.getKey();
            previousEntry = result;
            latestResult = result;
            nextEntry = map.higherEntry(key);
            forward = true;
            return key;
        }

        @Override
        public K previous() {
            final Entry<K, V> result = previousEntry;
            if (result == null)
                throw new NoSuchElementException();
            final K key = result.getKey();
            previousEntry = map.lowerEntry(key);
            latestResult = result;
            nextEntry = result;
            forward = false;
            return key;
        }

        @Override
        public void forEachRemaining(final Consumer<? super K> action) {
            final Function<K, Entry<K, V>> advance;
            Entry<K, V> node, prev = null;
            if (forward) {
                node = nextEntry;
                advance = map::higherEntry;
            } else {
                node = previousEntry;
                advance = map::lowerEntry;
            }
            while (node != null) {
                final K key = node.getKey();
                action.accept(key);
                prev = node;
                node = advance.apply(key);
            }
            if (forward) {
                nextEntry = null;
                previousEntry = prev;
            } else {
                nextEntry = prev;
                previousEntry = null;
            }
            latestResult = null;
        }

        @Override
        public K getKey() {
            if (latestResult == null)
                throw new IllegalStateException();
            return latestResult.getKey();
        }

        @Override
        public V getValue() {
            if (latestResult == null)
                throw new IllegalStateException();
            return latestResult.getValue();
        }

        @Override
        public V setValue(final V value) {
            if (latestResult == null)
                throw new IllegalStateException();
            final K key = latestResult.getKey();
            final V oldValue = latestResult.getValue();
            parent.putWithKnownState(key, oldValue, value);
            final UnmodifiableMapEntry<K, V> replacementEntry = new UnmodifiableMapEntry<>(key, value);
            if (forward)
                previousEntry = replacementEntry;
            else
                nextEntry = replacementEntry;
            latestResult = replacementEntry;
            return oldValue;
        }

        @Override
        public void remove() {
            final Entry<K, V> entry = latestResult;
            if (entry == null)
                throw new IllegalStateException();
            final K key = entry.getKey();
            parent.removeInternalExpectedGood(key, entry.getValue());
            if (forward)
                previousEntry = map.lowerEntry(key);
            else
                nextEntry = map.higherEntry(key);
            latestResult = null;
        }
    }

    protected static <C> boolean checkNotDefault(final String name, final Class<?>... parameterTypes) {
        try {
            final Method method = TreeMap.class.getMethod(name, parameterTypes);
            return !method.isDefault();
        } catch (final NoSuchMethodException e) {
            return false;
        }
    }
}
