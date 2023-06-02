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

import org.apache.commons.collections4.*;
import org.apache.commons.collections4.iterators.AbstractIteratorDecorator;
import org.apache.commons.collections4.map.AbstractNavigableMapDecorator;
import org.apache.commons.collections4.set.AbstractNavigableSetDecorator;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.*;
import java.util.function.Predicate;
import java.util.stream.Stream;


/**
 * Implementation of {@link BidiMap} that uses two {@link TreeMap} instances.
 * <p>
 * The setValue() method on iterators will succeed only if the new value being set is
 * not already in the bidi map.
 * </p>
 * <p>
 * When considering whether to use this class, the {@link TreeBidiMap} class should
 * also be considered. It implements the interface using a dedicated design, and does
 * not store each object twice, which can save on memory use.
 * </p>
 * <p>
 * NOTE: From Commons Collections 3.1, all subclasses will use {@link TreeMap}
 * and the flawed {@code createMap} method is ignored.
 * </p>
 *
 * @param <K> the type of the keys in this map
 * @param <V> the type of the values in this map
 * @since 3.0
 */
public class DualTreeBidi2MapImproved<K extends Comparable<K>, V extends Comparable<V>>
        extends AbstractNavigableMapDecorator<K, V>
        implements SortedBidiMap<K, V>, NavigableMap<K, V>, Externalizable {

    private static final long serialVersionUID = 721969328361809L;

    protected final NavigableMap<K, V> keyMap;
    protected final NavigableMap<V, K> valueMap;
    private final Comparator<? super K> keyComparator;
    private final Comparator<? super V> valueComparator;
    private DualTreeBidi2MapImproved<V, K> inverseBidiMap;
    private int modificationCount = 0;

    private static final Object NULL = new Object();

    @SuppressWarnings("unchecked")
    private K NO_KEY() {
        return (K) NULL;
    }

    @SuppressWarnings("unchecked")
    private V NO_VALUE() {
        return (V) NULL;
    }

    /**
     * Creates an empty {@link DualTreeBidi2MapImproved}.
     */
    public DualTreeBidi2MapImproved() {
        super(new TreeMap<>(ComparatorUtils.naturalComparator()));
        this.keyMap = decorated();
        this.valueMap = new TreeMap<>(ComparatorUtils.naturalComparator());
        this.keyComparator = keyMap.comparator();
        this.valueComparator = valueMap.comparator();
    }

    /**
     * Constructs a {@link DualTreeBidi2MapImproved} and copies the mappings from
     * specified {@link Map}.
     *
     * @param map the map whose mappings are to be placed in this map
     */
    public DualTreeBidi2MapImproved(final Map<? extends K, ? extends V> map) {
        this();
        putAll(map);
    }

    /**
     * Constructs a {@link DualTreeBidi2MapImproved} using the specified {@link Comparator}.
     *
     * @param keyComparator   the comparator
     * @param valueComparator the values comparator to use
     */
    public DualTreeBidi2MapImproved(final Comparator<? super K> keyComparator, final Comparator<? super V> valueComparator) {
        super(new TreeMap<>(keyComparator));
        this.keyMap = decorated();
        this.valueMap = new TreeMap<>(valueComparator);
        this.keyComparator = Objects.requireNonNull(keyComparator);
        this.valueComparator = Objects.requireNonNull(valueComparator);
    }

    protected DualTreeBidi2MapImproved(final NavigableMap<K, V> keyMap, final NavigableMap<V, K> valueMap) {
        super(Objects.requireNonNull(keyMap));
        this.keyMap = decorated();
        this.valueMap = Objects.requireNonNull(valueMap);
        this.keyComparator = keyMap.comparator() != null ? keyMap.comparator() : ComparatorUtils.naturalComparator();
        this.valueComparator = valueMap.comparator() != null ? valueMap.comparator() : ComparatorUtils.naturalComparator();
    }

    protected DualTreeBidi2MapImproved(final NavigableMap<K, V> keyMap, final NavigableMap<V, K> reverseMap, final DualTreeBidi2MapImproved<V, K> inverseBidiMap) {
        this(keyMap, reverseMap);
        this.inverseBidiMap = inverseBidiMap;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
//        out.writeObject(keyComparator);
//        out.writeObject(valueComparator);
        out.writeObject(keyMap);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        // TODO skipped comparators
        TreeMap<K, V> storedMap = (TreeMap<K, V>) in.readObject();
        putAll(storedMap);
    }

    @SuppressWarnings("unchecked")
    protected K castKey(Object keyObject) {
        return (K) keyObject;
    }

    protected boolean keyEquals(K a, K b) {
        return (a != null && b != null && keyComparator.compare(a, b) == 0) || (a == b);
    }

    protected boolean valueEquals(V a, V b) {
        return (a != null && b != null && valueComparator.compare(a, b) == 0) || (a == b);
    }

    @SuppressWarnings("unchecked")
    protected V castValue(Object valueObject) {
        return (V) valueObject;
    }

    @Override
    public SortedBidiMap<V, K> inverseBidiMap() {
        if (inverseBidiMap == null)
            inverseBidiMap = createInverse();
        return inverseBidiMap;
    }

    protected DualTreeBidi2MapImproved<V, K> createInverse() {
        return new DualTreeBidi2MapImproved<>(valueMap, keyMap, this);
    }

    @Override
    public NavigableMap<K, V> descendingMap() {
        return new DualTreeBidi2MapImproved<>(keyMap.descendingMap(), valueMap);
    }

    @Override
    protected NavigableMap<K, V> wrapSubMap(NavigableMap<K, V> subMap) {
        return new DualTreeBidi2MapImprovedSubKeys<>(subMap, this);
    }

    @Override
    public Comparator<? super K> comparator() {
        return keyComparator;
    }

    @Override
    public Comparator<? super V> valueComparator() {
        return valueComparator;
    }

    @Override
    public K getKey(Object valueObject) {
        V value = castValue(valueObject);
        K key = valueMap.getOrDefault(value, NO_KEY());
        if (key != NO_KEY())
            return key;
        else
            return null;
    }

    @Override
    public K removeValue(Object valueObject) {
        V value = castValue(valueObject);
        K key = valueMap.getOrDefault(value, NO_KEY());
        if (key == NO_KEY())
            return null;
        keyMapRemoveChecked(key, value);
        valueMapRemoveChecked(value, key);
        modified();
        return key;
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Objects.requireNonNull(function);
        for (Map.Entry<K, V> entry : keyMap.entrySet()) {
            K key = entry.getKey();
            V oldValue = entry.getValue();

            V newValue = function.apply(key, oldValue);

            if (!valueEquals(oldValue, newValue)) {
                updateValueMapDuringKeyMapIteration(key, oldValue, newValue);
                entry.setValue(newValue);
            }
        }
        modified();
    }

    private boolean collectionRemoveIf(final Predicate<? super Entry<K, V>> filter) {
        final Iterator<Entry<K, V>> iterator = keyMap.entrySet().iterator();
        boolean changed = false;
        while (iterator.hasNext()) {
            final Entry<K, V> entry = iterator.next();
            if (filter.test(entry)) {
                iterator.remove();
                valueMapRemoveChecked(entry.getValue(), entry.getKey());
                changed = true;
            }
        }
        if (changed)
            modified();
        return changed;
    }

    @Override
    public V put(K key, V newValue) {
        V currentValue = keyMap.getOrDefault(key, NO_VALUE());

        if (valueEquals(newValue, currentValue)) {
            return currentValue;
        } else if (currentValue == NO_VALUE()) {
            keyMapAddChecked(key, newValue);
            updateValueMapForNewValue(key, newValue);
            modified();
            return null;
        } else {
            keyMapReplaceChecked(key, currentValue, newValue);
            valueMapRemoveChecked(currentValue, key);
            updateValueMapForNewValue(key, newValue);
            modified();
            return currentValue;
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> mapToCopy) {
        if (mapToCopy instanceof IterableMap) {
            IterableMap<? extends K, ? extends V> iterableMap = (IterableMap<? extends K, ? extends V>) mapToCopy;
            MapIterator<? extends K, ? extends V> mapIterator = iterableMap.mapIterator();
            while (mapIterator.hasNext()) {
                K key = mapIterator.next();
                V value = mapIterator.getValue();
                put(key, value);
            }
        } else {
            Iterator<? extends Entry<? extends K, ? extends V>> iterator = mapToCopy.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<? extends K, ? extends V> entry = iterator.next();
                put(entry.getKey(), entry.getValue());
            }
        }
        modified();
    }

    @Override
    public V remove(Object keyObject) {
        K key = castKey(keyObject);
        if (keyMap.containsKey(key)) {
            V value = keyMap.remove(key);
            valueMapRemoveChecked(value, key);
            modified();
            return value;
        } else {
            return null;
        }
    }

    protected boolean removeViaCollection(Object keyObject) {
        K key = castKey(keyObject);
        if (keyMap.containsKey(key)) {
            V value = keyMap.remove(key);
            valueMapRemoveChecked(value, key);
            modified();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean remove(Object keyObject, Object valueObject) {
        K key = castKey(keyObject);
        V value = castValue(valueObject);
        if (keyMap.remove(key, value)) {
            valueMapRemoveChecked(value, key);
            modified();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        if (!keyMap.replace(key, oldValue, newValue))
            return false;
        valueMapRemoveChecked(oldValue, key);
        updateValueMapForNewValue(key, newValue);
        modified();
        return true;
    }

    @Override
    public V replace(K key, V newValue) {
        V currentValue = keyMap.getOrDefault(key, NO_VALUE());
        if (currentValue == NO_VALUE())
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
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);

        V currentValue = keyMap.get(key);
        if (currentValue == null)
            return null;

        V newValue = remappingFunction.apply(key, currentValue);
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
    public V putIfAbsent(K key, V newValue) {
        V currentValue = keyMap.getOrDefault(key, NO_VALUE());
        if (currentValue == NO_VALUE()) {
            keyMapAddChecked(key, newValue);
            updateValueMapForNewValue(key, newValue);
            modified();
            return null;
        } else {
            return currentValue;
        }
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        Objects.requireNonNull(mappingFunction);

        V currentValue = keyMap.getOrDefault(key, NO_VALUE());
        if (currentValue == NO_VALUE()) {
            V newValue = mappingFunction.apply(key);
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
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);

        V currentValue = keyMap.getOrDefault(key, NO_VALUE());
        V currentValueNormal = currentValue != NO_VALUE() ? currentValue : null;

        V newValue = remappingFunction.apply(key, currentValueNormal);

        if (newValue == null) {
            if (currentValue != NO_VALUE()) {
                keyMapRemoveChecked(key, currentValue);
                valueMapRemoveChecked(currentValue, key);
                modified();
            }
            return null;
        } else if (valueEquals(newValue, currentValue)) {
            return currentValue;
        } else if (currentValue == NO_VALUE()) {
            keyMapAddChecked(key, newValue);
            updateValueMapForNewValue(key, newValue);
            modified();
            return newValue;
        } else {
            keyMapReplaceChecked(key, currentValue, newValue);
            valueMapRemoveChecked(currentValue, key);
            updateValueMapForNewValue(key, newValue);
            modified();
            return newValue;
        }
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);

        V currentValue = keyMap.getOrDefault(key, NO_VALUE());

        if (currentValue == NO_VALUE()) {
            keyMapAddChecked(key, value);
            updateValueMapForNewValue(key, value);
            modified();
            return value;
        } else if (currentValue == null) {
            keyMapReplaceChecked(key, null, value);
            valueMapRemoveChecked(null, key);
            updateValueMapForNewValue(key, value);
            modified();
            return value;
        }

        V newValue = remappingFunction.apply(currentValue, value);

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
    public boolean containsValue(Object valueObject) {
        V value = castValue(valueObject);
        return valueMap.containsKey(value);
    }

    @Override
    public Entry<K, V> pollFirstEntry() {
        Entry<K, V> entry = keyMap.pollFirstEntry();
        if (entry != null) {
            valueMapRemoveChecked(entry.getValue(), entry.getKey());
            modified();
        }
        return entry;
    }

    @Override
    public Entry<K, V> pollLastEntry() {
        Entry<K, V> entry = keyMap.pollLastEntry();
        if (entry != null) {
            valueMapRemoveChecked(entry.getValue(), entry.getKey());
            modified();
        }
        return entry;
    }

    @Override
    public NavigableSet<K> navigableKeySet() {
        return new KeySet<>(keyMap.navigableKeySet(), this);
    }

    @Override
    public NavigableSet<K> descendingKeySet() {
        return new KeySet<>(keyMap.descendingKeySet(), this);
    }

    @Override
    public Set<K> keySet() {
        return navigableKeySet();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new EntrySet<>(keyMap.entrySet(), this);
    }

    @Override
    public Set<V> values() {
        return new ValueMapKeySet<>(valueMap.keySet(), this);
    }

    @Override
    public OrderedMapIterator<K, V> mapIterator() {
        return new DualTreeMapIterator();
    }

    private void updateValueMapDuringKeyMapIteration(K key, V oldValue, V newValue) {
        assert !valueEquals(oldValue, newValue);
        if (valueMap.containsKey(newValue))
            throw new ValueChangeNotAllowedException();
        valueMapRemoveChecked(oldValue, key);
        valueMapAddChecked(newValue, key);
    }

    private void updateValueMapForNewValue(K key, V newValue) {
        K newValueOldKey = valueMap.getOrDefault(newValue, NO_KEY());
        if (newValueOldKey != NO_KEY()) {
            valueMapReplaceChecked(newValue, newValueOldKey, key);
            keyMapRemoveChecked(newValueOldKey, newValue);
        } else {
            valueMapAddChecked(newValue, key);
        }
    }

    private void modified() {
        modificationCount++;
    }

    private static final boolean treeMapImplementsRemove2 = checkNotDefault("remove", Object.class, Object.class);
    private static final boolean treeMapImplementsReplace3 = checkNotDefault("replace", Object.class, Object.class, Object.class);
    private static final boolean treeMapImplementsPutIfAbsent = checkNotDefault("putIfAbsent", Object.class, Object.class);

    private static <C> boolean checkNotDefault(String name, Class<?>... parameterTypes) {
        try {
            Method method = TreeMap.class.getMethod(name, parameterTypes);
            return !method.isDefault();
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private void keyMapAddChecked(K key, V value) {
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

    private void keyMapReplaceChecked(K key, V oldValue, V newValue) {
        if (treeMapImplementsReplace3) {
            if (!keyMap.replace(key, oldValue, newValue))
                throw new IllegalStateException();
        } else {
            V previous = keyMap.put(key, newValue);
            if (!valueEquals(previous, oldValue))
                throw new IllegalStateException();
        }
    }

    private void keyMapRemoveChecked(K key, V expectedValue) {
        if (treeMapImplementsRemove2) {
            if (!keyMap.remove(key, expectedValue))
                throw new IllegalStateException();
        } else {
            V previous = keyMap.remove(key);
            if (!valueEquals(expectedValue, previous))
                throw new IllegalStateException();
        }
    }

    private void valueMapAddChecked(V lookup, K key) {
        // see keyMapAddChecked comments for limitations
        if (treeMapImplementsPutIfAbsent) {
            if (valueMap.putIfAbsent(lookup, key) != null)
                throw new IllegalStateException();
        } else {
            if (valueMap.put(lookup, key) != null)
                throw new IllegalStateException();
        }
    }

    private void valueMapReplaceChecked(V lookup, K oldKey, K newKey) {
        if (treeMapImplementsReplace3) {
            if (!valueMap.replace(lookup, oldKey, newKey))
                throw new IllegalStateException();
        } else {
            K previous = valueMap.put(lookup, newKey);
            if (!keyEquals(previous, oldKey))
                throw new IllegalStateException();
        }
    }

    private void valueMapRemoveChecked(V lookup, K content) {
        if (treeMapImplementsRemove2) {
            if (!valueMap.remove(lookup, content))
                throw new IllegalStateException();
        } else {
            K previous = valueMap.remove(lookup);
            if (!keyEquals(content, previous))
                throw new IllegalStateException();
        }
    }

    protected static class ValueChangeNotAllowedException extends IllegalArgumentException {
    }

    protected static class BaseSet<E, K extends Comparable<K>, V extends Comparable<V>>
            extends AbstractNavigableSetDecorator<E> {
        protected final DualTreeBidi2MapImproved<K, V> parent;

        protected BaseSet(NavigableSet<E> set, DualTreeBidi2MapImproved<K, V> parent) {
            super(set);
            this.parent = parent;
        }

        protected abstract BaseSet<E, K, V> wrapSet(NavigableSet<E> set)

        @Override
        public boolean add(E object) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(Collection<? extends E> coll) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            parent.clear();
        }

        @Override
        public NavigableSet<E> descendingSet() {
            return wrapSet(decorated().descendingSet());
        }

        @Override
        public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
            // TODO or should we do subMap first, then parent references are still useful
            return wrapSet(decorated().subSet(fromElement, fromInclusive, toElement, toInclusive));
        }

        @Override
        public NavigableSet<E> headSet(E toElement, boolean inclusive) {
            return wrapSet(decorated().headSet(toElement, inclusive));
        }

        @Override
        public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
            return wrapSet(decorated().tailSet(fromElement, inclusive));
        }

        @Override
        public NavigableSet<E> subSet(E fromElement, E toElement) {
            return wrapSet(decorated().subSet(fromElement, true, toElement, false));
        }

        @Override
        public NavigableSet<E> headSet(E toElement) {
            return wrapSet(decorated().headSet(toElement, false));
        }

        @Override
        public NavigableSet<E> tailSet(E fromElement) {
            return wrapSet(decorated().tailSet(fromElement, true));
        }
    }

    protected static class KeySet<K extends Comparable<K>, V extends Comparable<V>>
            extends BaseSet<K, K, V> {
        protected KeySet(NavigableSet<K> keySet, DualTreeBidi2MapImproved<K, V> parent) {
            super(keySet, parent);
        }

        @Override
        public Iterator<K> iterator() {
            return new KeyIterator<>(decorated().iterator(), parent);
        }

        @Override
        public Iterator<K> descendingIterator() {
            return new KeyIterator<>(decorated().descendingIterator(), parent);
        }

        @Override
        public boolean remove(Object object) {
            return parent.removeViaCollection(object);
        }

        @Override
        public boolean removeIf(final Predicate<? super K> filter) {
            // ignores set order
            Objects.requireNonNull(filter);
            return parent.collectionRemoveIf(e -> filter.test(e.getKey()));
        }

        @Override
        public boolean removeAll(Collection<?> coll) {
            return super.removeAll(coll);
        }

        @Override
        public boolean retainAll(Collection<?> coll) {
            return super.retainAll(coll);
        }

        @Override
        public K pollFirst() {
            if (!decorated().isEmpty()) {
                // could be improved if we knew parent map aligned
                K key = decorated().first();
                V value = parent.keyMap.remove(key);
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
                // could be improved if we knew parent map aligned
                K key = decorated().last();
                V value = parent.keyMap.remove(key);
                parent.valueMapRemoveChecked(value, key);
                parent.modified();
                return key;
            } else {
                return null;
            }
        }
    }

    protected static class EntrySet<K extends Comparable<K>, V extends Comparable<V>>
            extends BaseSet<Entry<K, V>, K, V> {
        public EntrySet(Set<Entry<K, V>> entrySet) {
            super(entrySet);
        }

        public EntrySet(Set<Entry<K,V>> entries, DualTreeBidi2MapImproved<K,V> kvDualTreeBidi2MapImproved) {
            super();
        }

    }

    protected static class ValueMapKeySet<K extends Comparable<K>, V extends Comparable<V>>
            extends BaseSet<V, K, V> {
        public ValueMapKeySet(Set<V> valueMapKeySet) {
            super(valueMapKeySet);
        }

        public ValueMapKeySet(Set<V> vs, DualTreeBidi2MapImproved<K,V> kvDualTreeBidi2MapImproved) {
            super();
        }
    }

    protected static class KeyIterator<K extends Comparable<K>, V extends Comparable<V>>
            extends AbstractIteratorDecorator<K> {
        public KeyIterator(Iterator<K> iterator, DualTreeBidi2MapImproved<K,V> parent) {
            super(iterator);
        }
    }

    protected static class EntryIterator<K extends Comparable<K>, V extends Comparable<V>>
            extends AbstractIteratorDecorator<K> {
        public EntryIterator(Iterator<K> iterator, DualTreeBidi2MapImproved<K,V> parent) {
            super(iterator);
        }
    }

    protected static class ValueIterator<K extends Comparable<K>, V extends Comparable<V>>
            extends AbstractIteratorDecorator<K> {
        public ValueIterator(Iterator<K> iterator, DualTreeBidi2MapImproved<K,V> parent) {
            super(iterator);
        }
    }

    private class DualTreeMapIterator implements OrderedMapIterator<K, V> {

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public K next() {
            return null;
        }

        @Override
        public K getKey() {
            return null;
        }

        @Override
        public V getValue() {
            return null;
        }

        @Override
        public void remove() {

        }

        @Override
        public V setValue(V value) {
            return null;
        }

        @Override
        public boolean hasPrevious() {
            return false;
        }

        @Override
        public K previous() {
            return null;
        }
    }
}
