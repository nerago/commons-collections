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

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.collections4.*;
import org.apache.commons.collections4.map.AbstractNavigableMapDecorator;


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

    private final TreeMap<K, V> keyMap;
    private final TreeMap<V, K> valueMap;
    private final Comparator<? super K> keyComparator;
    private final Comparator<? super V> valueComparator;
    private DualTreeBidi2MapImproved<V, K> inverseBidiMap;
    private int modificationCount = 0;


    private static final Object NULL = new Object();
    @SuppressWarnings("unchecked")
    private K NO_KEY() { return (K) NULL; }
    @SuppressWarnings("unchecked")
    private V NO_VALUE() { return (V) NULL; }

    /**
     * Creates an empty {@link DualTreeBidi2MapImproved}.
     */
    public DualTreeBidi2MapImproved() {
        super(new TreeMap<>());
        this.keyMap = decorated();
        this.valueMap = new TreeMap<>();
        this.keyComparator = ComparatorUtils.naturalComparator();
        this.valueComparator = ComparatorUtils.naturalComparator();
    }

    /**
     * Constructs a {@link DualTreeBidi2MapImproved} and copies the mappings from
     * specified {@link Map}.
     *
     * @param map  the map whose mappings are to be placed in this map
     */
    public DualTreeBidi2MapImproved(final Map<? extends K, ? extends V> map) {
        this();
        putAll(map);
    }

    /**
     * Constructs a {@link DualTreeBidi2MapImproved} using the specified {@link Comparator}.
     *
     * @param keyComparator  the comparator
     * @param valueComparator  the values comparator to use
     */
    public DualTreeBidi2MapImproved(final Comparator<? super K> keyComparator, final Comparator<? super V> valueComparator) {
        super(new TreeMap<>(keyComparator));
        this.keyMap = decorated();
        this.valueMap = new TreeMap<>(valueComparator);
        this.keyComparator = Objects.requireNonNull(keyComparator);
        this.valueComparator = Objects.requireNonNull(valueComparator);
    }

    protected DualTreeBidi2MapImproved(final TreeMap<K, V> keyMap, final TreeMap<V, K> valueMap) {
        super(Objects.requireNonNull(keyMap));
        this.keyMap = decorated();
        this.valueMap = Objects.requireNonNull(valueMap);
        this.keyComparator = keyMap.comparator() != null ? keyMap.comparator() : ComparatorUtils.naturalComparator();
        this.valueComparator = valueMap.comparator() != null ? valueMap.comparator() : ComparatorUtils.naturalComparator();
    }

    protected DualTreeBidi2MapImproved(final TreeMap<K, V> keyMap, final TreeMap<V, K> reverseMap, final DualTreeBidi2MapImproved<V, K> inverseBidiMap) {
        this(keyMap, reverseMap);
        this.inverseBidiMap = inverseBidiMap;
    }

    @Override
    protected TreeMap<K, V> decorated() {
        return (TreeMap<K, V>) super.decorated();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(keyMap);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
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
    public Set<K> keySet() {
        return super.keySet();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return super.entrySet();
    }

    @Override
    public Set<V> values() {
        return (Set<V>) super.values();
    }

    @Override
    public boolean equals(Object object) {
        return super.equals(object);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    protected NavigableMap<K, V> wrapSubMap(NavigableMap<K, V> subMap) {
        return super.wrapSubMap(subMap);
    }

    @Override
    public K nextKey(K key) {
        return super.nextKey(key);
    }

    @Override
    public K previousKey(K key) {
        return super.previousKey(key);
    }

    @Override
    public Entry<K, V> lowerEntry(K key) {
        return super.lowerEntry(key);
    }

    @Override
    public K lowerKey(K key) {
        return super.lowerKey(key);
    }

    @Override
    public Entry<K, V> floorEntry(K key) {
        return super.floorEntry(key);
    }

    @Override
    public K floorKey(K key) {
        return super.floorKey(key);
    }

    @Override
    public Entry<K, V> ceilingEntry(K key) {
        return super.ceilingEntry(key);
    }

    @Override
    public K ceilingKey(K key) {
        return super.ceilingKey(key);
    }

    @Override
    public Entry<K, V> higherEntry(K key) {
        return super.higherEntry(key);
    }

    @Override
    public K higherKey(K key) {
        return super.higherKey(key);
    }

    @Override
    public Entry<K, V> firstEntry() {
        return super.firstEntry();
    }

    @Override
    public Entry<K, V> lastEntry() {
        return super.lastEntry();
    }

    @Override
    public Entry<K, V> pollFirstEntry() {
        return super.pollFirstEntry();
    }

    @Override
    public Entry<K, V> pollLastEntry() {
        return super.pollLastEntry();
    }

    @Override
    public NavigableSet<K> navigableKeySet() {
        return super.navigableKeySet();
    }

    @Override
    public NavigableSet<K> descendingKeySet() {
        return super.descendingKeySet();
    }

    @Override
    public NavigableMap<K, V> descendingMap() {
        return super.descendingMap();
    }

    @Override
    public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
        return super.subMap(fromKey, fromInclusive, toKey, toInclusive);
    }

    @Override
    public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
        return super.headMap(toKey, inclusive);
    }

    @Override
    public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
        return super.tailMap(fromKey, inclusive);
    }

    @Override
    public NavigableMap<K, V> subMap(K fromKey, K toKey) {
        return super.subMap(fromKey, toKey);
    }

    @Override
    public NavigableMap<K, V> headMap(K toKey) {
        return super.headMap(toKey);
    }

    @Override
    public NavigableMap<K, V> tailMap(K fromKey) {
        return super.tailMap(fromKey);
    }

    @Override
    public K firstKey() {
        return super.firstKey();
    }

    @Override
    public K lastKey() {
        return super.lastKey();
    }

    @Override
    public OrderedMapIterator<K, V> mapIterator() {
        return super.mapIterator();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
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

    private static final boolean treeMapImplementsRemove2 = checkNotDefault(TreeMap.class, "remove", Object.class, Object.class);
    private static final boolean treeMapImplementsReplace3 = checkNotDefault(TreeMap.class, "replace", Object.class, Object.class, Object.class);
    private static final boolean treeMapImplementsPutIfAbsent = checkNotDefault(TreeMap.class, "putIfAbsent", Object.class, Object.class);

    private static <C> boolean checkNotDefault(Class<C> targetClass, String name, Class<?>... parameterTypes) {
        try {
            Method method = targetClass.getMethod(name, parameterTypes);
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

    private static class ValueChangeNotAllowedException extends IllegalArgumentException {
    }
}
