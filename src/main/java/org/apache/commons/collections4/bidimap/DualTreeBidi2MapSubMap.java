package org.apache.commons.collections4.bidimap;

import org.apache.commons.collections4.*;


import java.util.*;
import java.util.function.*;
import java.util.function.Predicate;

class DualTreeBidi2MapSubMap<K extends Comparable<K>, V extends Comparable<V>>
        extends DualTreeBidi2MapBase<K, V> {
    protected final DualTreeBidi2Map<K, V> parent;

    DualTreeBidi2MapSubMap(NavigableMap<K, V> subKeyMap, SortedMapRange<K> keyRange, DualTreeBidi2Map<K, V> parent) {
        super(subKeyMap, keyRange, parent.valueMap, parent.valueRange);
        this.parent = parent;
    }

    public DualTreeBidi2MapSubMap(NavigableMap<K, V> keyMap, SortedMapRange<K> keyRange,
                                  NavigableMap<V, K> valueMap, SortedMapRange<V> valueRange, DualTreeBidi2Map<K, V> parent) {
        super(keyMap, keyRange, valueMap, valueRange);
        this.parent = parent;
    }

    @Override
    protected DualTreeBidi2Map<K, V> primaryMap() {
        return parent;
    }

    @Override
    protected DualTreeBidi2MapBase<V, K> createInverse() {
        return new DualTreeBidi2MapSubMap<>(valueMap, valueRange, keyMap, keyRange, (DualTreeBidi2Map<V, K>) parent.inverseBidiMap());
    }

    @Override
    protected DualTreeBidi2MapBase<K, V> createDescending() {
        return new DualTreeBidi2MapSubMap<>(keyMap.descendingMap(), getKeyRange().reversed(), parent);
    }

    @Override
    protected NavigableBoundMap<K, V> wrapMap(SortedMap<K, V> subMap, SortedMapRange<K> range) {
        return new DualTreeBidi2MapSubMap<>((NavigableMap<K, V>) subMap, range, parent);
    }

    @Override
    protected void modified() {
        parent.modified();
    }

    @Override
    protected K castKey(Object keyObject) {
        K key = super.castKey(keyObject);
        if (!keyRange.inRange(key))
            throw new IllegalArgumentException();
        return key;
    }

    private void checkKey(K key) {
        if (!keyRange.inRange(key))
            throw new IllegalArgumentException();
    }

    @Override
    protected V castValue(Object valueObject) {
        V value = super.castValue(valueObject);
        if (!valueRange.inRange(value))
            throw new IllegalArgumentException();
        return value;
    }

    private void checkValue(V value) {
        if (!valueRange.inRange(value))
            throw new IllegalArgumentException();
    }

    @Override
    public boolean containsKey(Object keyObject) {
        K key = castKey(keyObject);
        V value = keyMap.getOrDefault(key, NO_VALUE());
        if (value != NO_VALUE())
            return valueMap.containsKey(value);
        else
            return false;
    }

    @Override
    public V get(Object keyObject) {
        return getOrDefault(keyObject, null);
    }

    @Override
    public V getOrDefault(Object keyObject, V defaultValue) {
        K key = castKey(keyObject);
        V value = keyMap.getOrDefault(key, NO_VALUE());
        if (value != NO_VALUE()) {
            if (valueMap.containsKey(value))
                return value;
        }
        return defaultValue;
    }

    @Override
    public boolean containsValue(Object valueObject) {
        V value = castValue(valueObject);
        K key = valueMap.getOrDefault(value, NO_KEY());
        return key != NO_KEY() && keyMap.containsKey(key);
    }

    @Override
    public K getKey(Object valueObject) {
        V value = castValue(valueObject);
        K key = valueMap.getOrDefault(value, NO_KEY());
        if (key != NO_KEY() && keyMap.containsKey(key))
            return key;
        else
            return null;
    }

    @Override
    public K removeValue(Object valueObject) {
        V value = castValue(valueObject);
        K key = valueMap.getOrDefault(value, NO_KEY());
        if (key == NO_KEY() || !keyMap.containsKey(key))
            return null;
        keyMapRemoveChecked(key, value);
        valueMapRemoveChecked(value, key);
        modified();
        return key;
    }

    @Override
    protected boolean removeValueViaCollection(Object valueObject) {
        V value = castValue(valueObject);
        K key = valueMap.getOrDefault(value, NO_KEY());
        if (key == NO_KEY() || !keyMap.containsKey(key))
            return false;
        keyMapRemoveChecked(key, value);
        valueMapRemoveChecked(value, key);
        modified();
        return true;
    }

    @Override
    public boolean isEmpty() {
        if (!keyRange.isFull() && valueRange.isFull()) {
            return keyMap.isEmpty();
        } else if (keyRange.isFull() && !valueRange.isFull()) {
            return valueMap.isEmpty();
        } else {
            for (Entry<K, V> entry : keyMap.entrySet()) {
                if (valueMap.containsKey(entry.getValue()))
                    return false;
            }
            return true;
        }
    }

    @Override
    public int size() {
        if (!keyRange.isFull() && valueRange.isFull()) {
            return keyMap.size();
        } else if (keyRange.isFull() && !valueRange.isFull()) {
            return valueMap.size();
        } else {
            int count = 0;
            for (Entry<K, V> entry : keyMap.entrySet()) {
                if (valueMap.containsKey(entry.getValue()))
                    count++;
            }
            return count;
        }
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        Objects.requireNonNull(action);
        if (!keyRange.isFull() && valueRange.isFull()) {
            keyMap.forEach(action);
        } else if (keyRange.isFull() && !valueRange.isFull()) {
            // wrong order?
            valueMap.forEach((v, k) -> action.accept(k, v));
        } else {
            for (Entry<K, V> entry : keyMap.entrySet()) {
                K key = entry.getKey();
                V value = entry.getValue();
                if (valueMap.containsKey(value)) {
                    action.accept(key, value);
                }
            }
        }
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Objects.requireNonNull(function);
        for (Entry<K, V> entry : keyMap.entrySet()) {
            K key = entry.getKey();
            V oldValue = entry.getValue();
            if (valueMap.containsKey(oldValue)) {
                V newValue = function.apply(key, oldValue);
                checkValue(newValue);
                if (!valueEquals(oldValue, newValue) && updateValueMapDuringKeyMapIteration(key, oldValue, newValue)) {
                    entry.setValue(newValue);
                }
            }
        }
        modified();
    }

    @Override
    public V put(K key, V newValue) {
        checkKey(key);
        checkValue(newValue);

        V currentValue = keyMap.getOrDefault(key, NO_VALUE());

        if (currentValue == NO_VALUE()) {
            if (primaryMap().keyMap.containsKey(key))
                throw new ValueChangeNotAllowedException("key already exists in primary map but not this sub map");
            keyMapAddChecked(key, newValue);
            updateValueMapForNewValue(key, newValue);
            modified();
            return null;
        } else if (valueMap.containsKey(currentValue)) {
            if (!valueEquals(newValue, currentValue)) {
                keyMapReplaceChecked(key, currentValue, newValue);
                valueMapRemoveChecked(currentValue, key);
                updateValueMapForNewValue(key, newValue);
                modified();
            }
            return currentValue;
        } else {
            throw new ValueChangeNotAllowedException("key already exists in primary map but not this sub map");
        }
    }

    @Override
    protected boolean collectionRemoveIf(final Predicate<? super Entry<K, V>> filter) {
        final Iterator<Entry<K, V>> iterator = keyMap.entrySet().iterator();
        boolean changed = false;
        while (iterator.hasNext()) {
            final Entry<K, V> entry = iterator.next();
            if (valueMap.containsKey(entry.getValue()) && filter.test(entry)) {
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
    public V remove(Object keyObject) {
        K key = castKey(keyObject);
        V value = keyMap.getOrDefault(key, NO_VALUE());
        if (value != NO_VALUE() && valueMap.containsKey(value)) {
            keyMapRemoveChecked(key, value);
            valueMapRemoveChecked(value, key);
            modified();
            return value;
        } else {
            return null;
        }
    }

    protected boolean removeViaCollection(Object keyObject) {
        K key = castKey(keyObject);
        V value = keyMap.getOrDefault(key, NO_VALUE());
        if (value != NO_VALUE() && valueMap.containsKey(value)) {
            keyMapRemoveChecked(key, value);
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
        return remove2(key, value);
    }

    @Override
    protected boolean removeViaCollection(K key, V value) {
        checkKey(key);
        checkValue(value);
        return remove2(key, value);
    }

    private boolean remove2(K key, V value) {
        if (keyMap.containsKey(key) && valueMap.containsKey(value)) {
            keyMapRemoveChecked(key, value);
            valueMapRemoveChecked(value, key);
            modified();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        if (valueEquals(oldValue, newValue))
            return false;
        if (!valueMap.containsKey(oldValue))
            return false;
        if (!keyMap.replace(key, oldValue, newValue))
            return false;
        valueMapRemoveChecked(oldValue, key);
        updateValueMapForNewValue(key, newValue);
        modified();
        return true;
    }

    @Override
    public V replace(K key, V newValue) {
        checkKey(key);
        checkValue(newValue);

        V currentValue = keyMap.getOrDefault(key, NO_VALUE());
        if (currentValue == NO_VALUE())
            return null;
        else if (!valueMap.containsKey(currentValue))
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

        checkKey(key);

        V currentValue = keyMap.get(key);
        if (currentValue == null)
            return null;
        else if (!valueMap.containsKey(currentValue))
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
            checkValue(newValue);
            keyMapReplaceChecked(key, currentValue, newValue);
            valueMapRemoveChecked(currentValue, key);
            updateValueMapForNewValue(key, newValue);
            modified();
            return newValue;
        }
    }

    @Override
    public V putIfAbsent(K key, V newValue) {
        checkKey(key);
        V currentValue = keyMap.getOrDefault(key, NO_VALUE());
        if (currentValue == NO_VALUE()) {
            if (primaryMap().keyMap.containsKey(key))
                throw new ValueChangeNotAllowedException("key already exists in primary map but not this sub map");
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
        checkKey(key);
        V currentValue = keyMap.getOrDefault(key, NO_VALUE());
        if (currentValue == NO_VALUE()) {
            if (primaryMap().keyMap.containsKey(key))
                throw new ValueChangeNotAllowedException("key already exists in primary map but not this sub map");
            V newValue = mappingFunction.apply(key);
            if (newValue != null) {
                checkValue(newValue);
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
        checkKey(key);

        V currentValue = keyMap.getOrDefault(key, NO_VALUE());
        if ((currentValue == NO_VALUE() && primaryMap().keyMap.containsKey(key)) || !valueMap.containsKey(currentValue))
            throw new ValueChangeNotAllowedException("key already exists in primary map but not this sub map");

        V currentValueNormal = currentValue != NO_VALUE() ? currentValue : null;
        V newValue = remappingFunction.apply(key, currentValueNormal);

        if (newValue == null) {
            if (currentValue != NO_VALUE()) {
                keyMapRemoveChecked(key, currentValue);
                valueMapRemoveChecked(currentValue, key);
                modified();
            }
            return null;
        } else if (currentValue == NO_VALUE()) {
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
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);

        V currentValue = keyMap.getOrDefault(key, NO_VALUE());
        if ((currentValue == NO_VALUE() && primaryMap().keyMap.containsKey(key)) || !valueMap.containsKey(currentValue))
                throw new ValueChangeNotAllowedException("key already exists in primary map but not this sub map");

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
        if (!isEmpty()) {
            final Iterator<Entry<K, V>> iterator = keyMap.entrySet().iterator();
            while (iterator.hasNext()) {
                final Entry<K, V> entry = iterator.next();
                if (valueMap.containsKey(entry.getValue())) {
                    iterator.remove();
                    valueMapRemoveChecked(entry.getValue(), entry.getKey());
                }
            }
            modified();
        }
    }

    @Override
    public Entry<K, V> pollFirstEntry() {
        Entry<K, V> entry = keyMap.firstEntry();
        while (entry != null && !valueMap.containsKey(entry.getValue())) {
            entry = keyMap.higherEntry(entry.getKey());
        }
        if (entry != null) {
            keyMapRemoveChecked(entry.getKey(), entry.getValue());
            valueMapRemoveChecked(entry.getValue(), entry.getKey());
            return entry;
        }
        return null;
    }

    @Override
    public Entry<K, V> pollLastEntry() {
        Entry<K, V> entry = keyMap.lastEntry();
        while (entry != null && !valueMap.containsKey(entry.getValue())) {
            entry = keyMap.lowerEntry(entry.getKey());
        }
        if (entry != null) {
            keyMapRemoveChecked(entry.getKey(), entry.getValue());
            valueMapRemoveChecked(entry.getValue(), entry.getKey());
            return entry;
        }
        return null;
    }

    protected boolean updateValueMapDuringKeyMapIteration(K key, V oldValue, V newValue) {
        // caller should have checked current value is in valueMap
        if (!valueEquals(oldValue, newValue)) {
            if (primaryMap().valueMap.containsKey(newValue))
                throw new ValueChangeNotAllowedException("value exists for another key and can't update during iteration");
            valueMapRemoveChecked(oldValue, key);
            valueMapAddChecked(newValue, key);
            return true;
        } else {
            return false;
        }
    }

    protected void updateValueMapForNewValue(K key, V newValue) {
        K oldKeyForNewValue = primaryMap().valueMap.getOrDefault(newValue, NO_KEY());
        if (oldKeyForNewValue != NO_KEY()) {
            if (!keyMap.containsKey(oldKeyForNewValue) || !keyEquals(valueMap.get(newValue), oldKeyForNewValue))
                throw new ValueChangeNotAllowedException("old value exists outside this sub map and can't update");
            valueMapReplaceChecked(newValue, oldKeyForNewValue, key);
            keyMapRemoveChecked(oldKeyForNewValue, newValue);
        } else {
            valueMapAddChecked(newValue, key);
        }
    }

    @Override
    protected NavigableSet<K> createKeySet(boolean descending) {
        if (valueRange.isFull())
            return new KeySetUsingKeyMap<>(descending ? keyMap.descendingKeySet() : keyMap.navigableKeySet(), this);
        else if (keyRange.isFull())
            return new KeySetUsingValueMap<>(valueMap.values(), this);
        else
            return new KeySetUsingBoth<>(this, descending);
    }

    @Override
    protected NavigableSet<V> createValueSet() {
        return new ValueSetUsingValueMap<>(valueMap.navigableKeySet(), this);
    }

    @Override
    protected Set<Entry<K, V>> createEntrySet() {
        return new EntrySetUsingKeyMap<>(keyMap.entrySet(), this);
    }
    @Override
    public OrderedMapIterator<K, V> mapIterator() {
        return new DualTreeMapIterator<>(keyMap, this);
    }

    private static class KeySetUsingValueMap<K extends Comparable<K>, V extends Comparable<V>>
            extends BaseNavigableSet<K, K, V> {
        protected KeySetUsingValueMap(NavigableSet<K> set, DualTreeBidi2MapBase<K, V> parent) {
            // but the order will be wrong...
            super(set, parent);
        }

        public KeySetUsingValueMap(Collection<K> values, DualTreeBidi2MapSubMap<K,V> parent) {
            super(null, parent);
        }

        @Override
        protected BaseNavigableSet<K, K, V> wrapSet(NavigableSet<K> set) {
            return null;
        }
    }

    protected static <K, V, R> R scanFirstMatch(Supplier<Entry<K, V>> start,
                                                Function<K, Entry<K, V>> next,
                                                Predicate<V> confirm,
                                                Function<Entry<K, V>, R> transform) {
        Entry<K, V> entry = start.get();
        while (entry != null) {
            if (confirm.test(entry.getValue()))
                return transform.apply(entry);
            entry = next.apply(entry.getKey());
        }
        return null;
    }

    protected static <K, V, R> R scanFirstMatch(K key,
                                                Function<K, Entry<K, V>> start,
                                                Function<K, Entry<K, V>> next,
                                                Predicate<V> confirm,
                                                Function<Entry<K, V>, R> transform) {
        Entry<K, V> entry = start.apply(key);
        while (entry != null) {
            if (confirm.test(entry.getValue()))
                return transform.apply(entry);
            entry = next.apply(entry.getKey());
        }
        return null;
    }

    private static class KeySetUsingBoth<K extends Comparable<K>, V extends Comparable<V>>
            extends AbstractCollection<K>
            implements NavigableSet<K> {
        private final DualTreeBidi2MapSubMap<K, V> parent;
        private final NavigableMap<K, V> keyMap;
        private final Map<V, K> valueMap;

        public KeySetUsingBoth(DualTreeBidi2MapSubMap<K, V> parent, boolean descending) {
            this.parent = parent;
            this.keyMap = descending ? parent.keyMap.descendingMap() : parent.keyMap;
            this.valueMap = parent.valueMap;
        }

        @Override
        public K lower(K key) {
            return scanFirstMatch(key, keyMap::lowerEntry, keyMap::lowerEntry, valueMap::containsKey, Entry::getKey);
        }

        @Override
        public K floor(K key) {
            return scanFirstMatch(key, keyMap::floorEntry, keyMap::lowerEntry, valueMap::containsKey, Entry::getKey);
        }

        @Override
        public K higher(K key) {
            return scanFirstMatch(key, keyMap::higherEntry, keyMap::higherEntry, valueMap::containsKey, Entry::getKey);
        }

        @Override
        public K ceiling(K key) {
            return scanFirstMatch(key, keyMap::ceilingEntry, keyMap::higherEntry, valueMap::containsKey, Entry::getKey);
        }

        @Override
        public K first() {
            return scanFirstMatch(keyMap::firstEntry, keyMap::higherEntry, valueMap::containsKey, Entry::getKey);
        }

        @Override
        public K last() {
            return scanFirstMatch(keyMap::lastEntry, keyMap::lowerEntry, valueMap::containsKey, Entry::getKey);
        }

        @Override
        public K pollFirst() {
            Entry<K, V> entry = scanFirstMatch(keyMap::firstEntry, keyMap::higherEntry, valueMap::containsKey, Function.identity());
            if (entry != null) {
                parent.removeInternalExpectedGood(entry.getKey(), entry.getValue());
                return entry.getKey();
            }
            return null;
        }

        @Override
        public K pollLast() {
            Entry<K, V> entry = scanFirstMatch(keyMap::lastEntry, keyMap::lowerEntry, valueMap::containsKey, Function.identity());
            if (entry != null) {
                parent.removeInternalExpectedGood(entry.getKey(), entry.getValue());
                return entry.getKey();
            }
            return null;
        }

        @Override
        public int size() {
            return parent.size();
        }

        @Override
        public boolean isEmpty() {
            return parent.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return parent.containsKey(o);
        }

        @Override
        public boolean remove(Object o) {
            return parent.removeViaCollection(o);
        }

        @Override
        public void clear() {
            parent.clear();
        }

        @Override
        public void forEach(Consumer<? super K> action) {
            // doesn't follow my order?
            parent.forEach((k,v) -> action.accept(k));
        }

        @Override
        public boolean removeIf(Predicate<? super K> filter) {
            // doesn't follow my order?
            return parent.collectionRemoveIf(entry -> filter.test(entry.getKey()));
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            // doesn't follow my order?
            return parent.collectionRemoveIf(entry -> !c.contains(entry.getKey()));
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            // doesn't follow my order?
            return parent.collectionRemoveIf(entry -> c.contains(entry.getKey()));
        }

        @Override
        public Comparator<? super K> comparator() {
            return parent.comparator();
        }

        @Override
        public Iterator<K> iterator() {
            return new BothKeyIterator<>(parent, keyMap, valueMap);
        }

        @Override
        public Iterator<K> descendingIterator() {
            return new BothKeyIterator<>(parent, keyMap.descendingMap(), valueMap);
        }

        @Override
        public NavigableSet<K> descendingSet() {
            return parent.descendingMap().navigableKeySet();
        }

        @Override
        public NavigableSet<K> subSet(K fromElement, boolean fromInclusive, K toElement, boolean toInclusive) {
            return parent.subMap(fromElement, fromInclusive, toElement, toInclusive).navigableKeySet();
        }

        @Override
        public NavigableSet<K> headSet(K toElement, boolean inclusive) {
            return parent.headMap(toElement, inclusive).navigableKeySet();
        }

        @Override
        public NavigableSet<K> tailSet(K fromElement, boolean inclusive) {
            return parent.tailMap(fromElement, inclusive).navigableKeySet();
        }

        @Override
        public SortedSet<K> subSet(K fromElement, K toElement) {
            return parent.subMap(fromElement, toElement).navigableKeySet();
        }

        @Override
        public SortedSet<K> headSet(K toElement) {
            return parent.headMap(toElement).navigableKeySet();
        }

        @Override
        public SortedSet<K> tailSet(K fromElement) {
            return parent.tailMap(fromElement).navigableKeySet();
        }

        @Override
        public boolean addAll(Collection<? extends K> coll) {
            throw new UnsupportedOperationException();
        }
    }

    private static class BothKeyIterator<K extends Comparable<K>, V extends Comparable<V>>
            implements ResettableIterator<K> {
        private final DualTreeBidi2MapSubMap<K, V> parent;
        private final NavigableMap<K, V> keyMap;
        private final Map<V, K> valueMap;
        private Entry<K, V> next;
        private Entry<K, V> lastResult;
        private int expectedModCount;

        protected BothKeyIterator(DualTreeBidi2MapSubMap<K, V> parent, NavigableMap<K, V> keyMap, Map<V, K> valueMap) {
            this.parent = parent;
            this.keyMap = keyMap;
            this.valueMap = valueMap;
            reset();
        }

        @Override
        public void reset() {
            Entry<K, V> entry = keyMap.firstEntry();
            while (entry != null && !valueMap.containsKey(entry.getValue())) {
                entry = keyMap.higherEntry(entry.getKey());
            }
            next = entry;
            lastResult = null;
            expectedModCount = parent.primaryMap().modificationCount;
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public K next() {
            Entry<K, V> result = next;
            if (result == null)
                throw new NoSuchElementException();
            K key = result.getKey();

            Entry<K, V> possible = keyMap.higherEntry(key);
            while (possible != null && !valueMap.containsKey(possible.getValue())) {
                possible = keyMap.higherEntry(possible.getKey());
            }
            next = possible;

            lastResult = result;
            return key;
        }

        @Override
        public void forEachRemaining(Consumer<? super K> action) {
            Entry<K, V> entry = next;
            while (entry != null) {
                K key = entry.getKey();
                if (valueMap.containsKey(entry.getValue())) {
                    action.accept(key);
                }
                entry = keyMap.higherEntry(key);
            }
            next = null;
            lastResult = null;
        }

        @Override
        public void remove() {
            if (lastResult == null)
                throw new IllegalStateException();
            if (parent.primaryMap().modificationCount != expectedModCount)
                throw new ConcurrentModificationException();
            parent.removeInternalExpectedGood(lastResult.getKey(), lastResult.getValue());
            expectedModCount = parent.primaryMap().modificationCount;
            lastResult = null;
        }
    }
}
