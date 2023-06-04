package org.apache.commons.collections4.bidimap;

import org.apache.commons.collections4.NavigableBoundMap;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.SortedMapRange;


import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

class DualTreeBidi2MapSubKeys<K extends Comparable<K>, V extends Comparable<V>>
        extends DualTreeBidi2MapBase<K, V> {
    protected final DualTreeBidi2Map<?, ?> parent;
    protected final SortedMapRange<K> range;

    DualTreeBidi2MapSubKeys(NavigableMap<K, V> subKeyMap, SortedMapRange<K> range, DualTreeBidi2Map<K, V> parent) {
        super(subKeyMap, parent.valueMap);
        this.range = range;
        this.parent = parent;
    }

    @Override
    protected DualTreeBidi2Map<?, ?> primaryMap() {
        return parent;
    }

    @Override
    protected DualTreeBidi2MapBase<V, K> createInverse() {
        return new DualTreeBidi2MapSubValues<>(valueMap, keyMap, this);
    }

    @Override
    public NavigableMap<K, V> descendingMap() {
        return new DualTreeBidi2MapSubKeys<>(keyMap.descendingMap(), getMapRange(), parent);
    }

    @Override
    protected NavigableBoundMap<K, V> wrapMap(SortedMap<K, V> subMap, SortedMapRange<K> range) {
        return new DualTreeBidi2MapSubKeys<>((NavigableMap<K, V>) subMap, range, parent);
    }

    @Override
    protected void modified() {
        parent.modified();
    }

    @Override
    public K getKey(Object valueObject) {
        V value = castValue(valueObject);
        K key = valueMap.get(value);
        if (keyMap.containsKey(key))
            return key;
        else
            return null;
    }

    @Override
    public K removeValue(Object valueObject) {
        V value = castValue(valueObject);
        K key = valueMap.getOrDefault(value, NO_KEY());
        if (key != NO_KEY() && keyMap.containsKey(key)) {
            keyMapRemoveChecked(key, value);
            valueMapRemoveChecked(value, key);
            modified();
            return key;
        } else {
            return null;
        }
    }

    @Override
    protected boolean removeValueViaCollection(Object valueObject) {
        V value = castValue(valueObject);
        K key = valueMap.getOrDefault(value, NO_KEY());
        if (key != NO_KEY() && keyMap.containsKey(key)) {
            keyMapRemoveChecked(key, value);
            valueMapRemoveChecked(value, key);
            modified();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public V put(K key, V newValue) {
        if (!range.inRange(key))
            throw new IllegalArgumentException();

        V currentValue = keyMap.getOrDefault(key, NO_VALUE());

        if (currentValue == NO_VALUE()) {
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

    @Override
    protected void putWithKnownState(K key, V oldValue, V newValue) {
        super.putWithKnownState(key, oldValue, newValue);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> mapToCopy) {
        super.putAll(mapToCopy);
    }

    @Override
    public V remove(Object keyObject) {
        return super.remove(keyObject);
    }

    @Override
    protected boolean removeViaCollection(Object keyObject) {
        return super.removeViaCollection(keyObject);
    }

    @Override
    public boolean remove(Object keyObject, Object valueObject) {
        return super.remove(keyObject, valueObject);
    }

    @Override
    protected void removeViaMapIterator(K key, V value) {
        super.removeViaMapIterator(key, value);
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return super.replace(key, oldValue, newValue);
    }

    @Override
    public V replace(K key, V newValue) {
        return super.replace(key, newValue);
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return super.computeIfPresent(key, remappingFunction);
    }

    @Override
    public V putIfAbsent(K key, V newValue) {
        return super.putIfAbsent(key, newValue);
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return super.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return super.compute(key, remappingFunction);
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return super.merge(key, value, remappingFunction);
    }

    @Override
    public void clear() {
        super.clear();
    }

    @Override
    public boolean containsValue(Object valueObject) {
        return super.containsValue(valueObject);
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
    public Set<K> keySet() {
        return super.keySet();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return super.entrySet();
    }

    @Override
    public Set<V> values() {
        return super.values();
    }

    @Override
    protected KeySet<K, V> createKeySet(boolean descending) {
        return null;
    }

    @Override
    protected ValueMapKeySet<K, V> createValueSet() {
        return null;
    }

    @Override
    protected EntrySet<K, V> createEntrySet() {
        return null;
    }

    @Override
    public OrderedMapIterator<K, V> mapIterator() {
        return super.mapIterator();
    }


}
