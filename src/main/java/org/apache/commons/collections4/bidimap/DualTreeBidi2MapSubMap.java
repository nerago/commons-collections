package org.apache.commons.collections4.bidimap;

import org.apache.commons.collections4.*;


import java.util.*;
import java.util.function.*;
import java.util.function.Predicate;

class DualTreeBidi2MapSubMap<K extends Comparable<K>, V extends Comparable<V>>
        extends DualTreeBidi2MapBase<K, V> {
    private static final long serialVersionUID = 4419593787116101216L;

    protected final DualTreeBidi2Map<K, V> parent;

    DualTreeBidi2MapSubMap(final NavigableMap<K, V> subKeyMap, final SortedMapRange<K> keyRange, final DualTreeBidi2Map<K, V> parent) {
        super(subKeyMap, keyRange, parent.valueMap, parent.valueRange);
        this.parent = parent;
    }

    public DualTreeBidi2MapSubMap(final NavigableMap<K, V> keyMap, final SortedMapRange<K> keyRange,
                                  final NavigableMap<V, K> valueMap, final SortedMapRange<V> valueRange, final DualTreeBidi2Map<K, V> parent) {
        super(keyMap, keyRange, valueMap, valueRange);
        this.parent = parent;
    }

    @Override
    protected DualTreeBidi2Map<K, V> primaryMap() {
        return parent;
    }

    @Override
    protected DualTreeBidi2MapBase<V, K> createInverse() {
        return new DualTreeBidi2MapSubMap<>(valueMap, valueRange, keyMap, getKeyRange(), (DualTreeBidi2Map<V, K>) parent.inverseBidiMap());
    }

    @Override
    protected DualTreeBidi2MapBase<K, V> createDescending() {
        return new DualTreeBidi2MapSubMap<>(keyMap.descendingMap(), getKeyRange().reversed(), parent);
    }

    @Override
    protected NavigableBoundMap<K, V> decorateDerived(final NavigableMap<K, V> subMap, final SortedMapRange<K> keyRange) {
        return new DualTreeBidi2MapSubMap<>(subMap, keyRange, parent);
    }

    @Override
    protected void modified() {
        parent.modified();
    }

    @Override
    protected K castKey(final Object keyObject) {
        final K key = super.castKey(keyObject);
//        if (!getKeyRange().inRange(key))
//            throw new IllegalArgumentException();
        return key;
    }

    private void checkKey(final K key) {
        if (!getKeyRange().inRange(key))
            throw new IllegalArgumentException();
    }

    @Override
    protected V castValue(final Object valueObject) {
        final V value = super.castValue(valueObject);
        if (!valueRange.inRange(value))
            throw new IllegalArgumentException();
        return value;
    }

    private void checkValue(final V value) {
        if (!valueRange.inRange(value))
            throw new IllegalArgumentException();
    }

    @Override
    public boolean containsKey(final Object keyObject) {
        final K key = castKey(keyObject);
        final V value = keyMap.getOrDefault(key, NO_VALUE());
        if (value != NO_VALUE())
            return valueMap.containsKey(value);
        else
            return false;
    }

    @Override
    public V get(final Object keyObject) {
        return getOrDefault(keyObject, null);
    }

    @Override
    public V getOrDefault(final Object keyObject, final V defaultValue) {
        final K key = castKey(keyObject);
        final V value = keyMap.getOrDefault(key, NO_VALUE());
        if (value != NO_VALUE()) {
            if (valueMap.containsKey(value))
                return value;
        }
        return defaultValue;
    }

    @Override
    public boolean containsValue(final Object valueObject) {
        final V value = castValue(valueObject);
        final K key = valueMap.getOrDefault(value, NO_KEY());
        return key != NO_KEY() && keyMap.containsKey(key);
    }

    @Override
    public K getKey(final Object valueObject) {
        final V value = castValue(valueObject);
        final K key = valueMap.getOrDefault(value, NO_KEY());
        if (key != NO_KEY() && keyMap.containsKey(key))
            return key;
        else
            return null;
    }

    @Override
    public K removeValue(final Object valueObject) {
        final V value = castValue(valueObject);
        final K key = valueMap.getOrDefault(value, NO_KEY());
        if (key == NO_KEY() || !keyMap.containsKey(key))
            return null;
        keyMapRemoveChecked(key, value);
        valueMapRemoveChecked(value, key);
        modified();
        return key;
    }

    @Override
    protected boolean removeValueViaCollection(final Object valueObject) {
        final V value = castValue(valueObject);
        final K key = valueMap.getOrDefault(value, NO_KEY());
        if (key == NO_KEY() || !keyMap.containsKey(key))
            return false;
        keyMapRemoveChecked(key, value);
        valueMapRemoveChecked(value, key);
        modified();
        return true;
    }

    @Override
    public boolean isEmpty() {
        if (!getKeyRange().isFull() && valueRange.isFull()) {
            return keyMap.isEmpty();
        } else if (getKeyRange().isFull() && !valueRange.isFull()) {
            return valueMap.isEmpty();
        } else {
            for (final Entry<K, V> entry : keyMap.entrySet()) {
                if (valueMap.containsKey(entry.getValue()))
                    return false;
            }
            return true;
        }
    }

    @Override
    public int size() {
        if (!getKeyRange().isFull() && valueRange.isFull()) {
            return keyMap.size();
        } else if (getKeyRange().isFull() && !valueRange.isFull()) {
            return valueMap.size();
        } else {
            int count = 0;
            for (final Entry<K, V> entry : keyMap.entrySet()) {
                if (valueMap.containsKey(entry.getValue()))
                    count++;
            }
            return count;
        }
    }

    @Override
    public void forEach(final BiConsumer<? super K, ? super V> action) {
        Objects.requireNonNull(action);
        if (!getKeyRange().isFull() && valueRange.isFull()) {
            keyMap.forEach(action);
        } else if (getKeyRange().isFull() && !valueRange.isFull()) {
            // wrong order?
            valueMap.forEach((v, k) -> action.accept(k, v));
        } else {
            for (final Entry<K, V> entry : keyMap.entrySet()) {
                final K key = entry.getKey();
                final V value = entry.getValue();
                if (valueMap.containsKey(value)) {
                    action.accept(key, value);
                }
            }
        }
    }

    @Override
    public void replaceAll(final BiFunction<? super K, ? super V, ? extends V> function) {
        Objects.requireNonNull(function);
        for (final Entry<K, V> entry : keyMap.entrySet()) {
            final K key = entry.getKey();
            final V oldValue = entry.getValue();
            if (valueMap.containsKey(oldValue)) {
                final V newValue = function.apply(key, oldValue);
                checkValue(newValue);
                if (!valueEquals(oldValue, newValue) && updateValueMapDuringKeyMapIteration(key, oldValue, newValue)) {
                    entry.setValue(newValue);
                }
            }
        }
        modified();
    }

    @Override
    public V put(final K key, final V newValue) {
        checkKey(key);
        checkValue(newValue);

        final V currentValue = keyMap.getOrDefault(key, NO_VALUE());

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
    public V remove(final Object keyObject) {
        final K key = castKey(keyObject);
        final V value = keyMap.getOrDefault(key, NO_VALUE());
        if (value != NO_VALUE() && valueMap.containsKey(value)) {
            keyMapRemoveChecked(key, value);
            valueMapRemoveChecked(value, key);
            modified();
            return value;
        } else {
            return null;
        }
    }

    protected boolean removeViaCollection(final Object keyObject) {
        final K key = castKey(keyObject);
        final V value = keyMap.getOrDefault(key, NO_VALUE());
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
    public boolean remove(final Object keyObject, final Object valueObject) {
        final K key = castKey(keyObject);
        final V value = castValue(valueObject);
        return remove2(key, value);
    }

    @Override
    protected boolean removeViaCollection(final K key, final V value) {
        checkKey(key);
        checkValue(value);
        return remove2(key, value);
    }

    private boolean remove2(final K key, final V value) {
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
    public boolean replace(final K key, final V oldValue, final V newValue) {
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
    public V replace(final K key, final V newValue) {
        checkKey(key);
        checkValue(newValue);

        final V currentValue = keyMap.getOrDefault(key, NO_VALUE());
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
    public V computeIfPresent(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);

        checkKey(key);

        final V currentValue = keyMap.get(key);
        if (currentValue == null)
            return null;
        else if (!valueMap.containsKey(currentValue))
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
            checkValue(newValue);
            keyMapReplaceChecked(key, currentValue, newValue);
            valueMapRemoveChecked(currentValue, key);
            updateValueMapForNewValue(key, newValue);
            modified();
            return newValue;
        }
    }

    @Override
    public V putIfAbsent(final K key, final V newValue) {
        checkKey(key);
        final V currentValue = keyMap.getOrDefault(key, NO_VALUE());
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
    public V computeIfAbsent(final K key, final Function<? super K, ? extends V> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        checkKey(key);
        final V currentValue = keyMap.getOrDefault(key, NO_VALUE());
        if (currentValue == NO_VALUE()) {
            if (primaryMap().keyMap.containsKey(key))
                throw new ValueChangeNotAllowedException("key already exists in primary map but not this sub map");
            final V newValue = mappingFunction.apply(key);
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
    public V compute(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        checkKey(key);

        final V currentValue = keyMap.getOrDefault(key, NO_VALUE());
        if ((currentValue == NO_VALUE() && primaryMap().keyMap.containsKey(key)) || !valueMap.containsKey(currentValue))
            throw new ValueChangeNotAllowedException("key already exists in primary map but not this sub map");

        final V currentValueNormal = currentValue != NO_VALUE() ? currentValue : null;
        final V newValue = remappingFunction.apply(key, currentValueNormal);

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
    public V merge(final K key, final V value, final BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);

        final V currentValue = keyMap.getOrDefault(key, NO_VALUE());
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

        final V newValue = remappingFunction.apply(currentValue, value);

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
                    valueMapRemoveChecked(entry.getValue(), entry.getKey());
                    iterator.remove();
                }
            }
            // TODO counts?
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

    protected boolean updateValueMapDuringKeyMapIteration(final K key, final V oldValue, final V newValue) {
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

    protected void updateValueMapForNewValue(final K key, final V newValue) {
        final K oldKeyForNewValue = primaryMap().valueMap.getOrDefault(newValue, NO_KEY());
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
    protected NavigableSet<K> createKeySet(final boolean descending) {
        if (valueRange.isFull())
            return new KeySetUsingKeyMap<>(descending ? keyMap.descendingKeySet() : keyMap.navigableKeySet(), getKeyRange(), this);
        else if (getKeyRange().isFull())
            // could use valueMap.entries but order is wrong
            return new KeySetUsingValueMap<>(valueMap.values(), getKeyRange(), this);
        else
            return new KeySetUsingBoth<>(this, descending);
    }

    @Override
    protected NavigableSet<V> createValueSet() {
        // TODO doesn't this need boths too
        return new ValueSetUsingValueMap<>(valueMap.navigableKeySet(), , this);
    }

    @Override
    protected Set<Entry<K, V>> createEntrySet() {
        // boths
        return new EntrySetUsingKeyMap<>(keyMap.entrySet(), this);
    }

    @Override
    public OrderedMapIterator<K, V> mapIterator() {
        // TODO doesn't look very bounded
        return new DualTreeMapIterator<>(keyMap, this);
    }

    private static class KeySetUsingValueMap<K extends Comparable<K>, V extends Comparable<V>>
            extends BaseNavigableSet<K, K, V> {
        private static final long serialVersionUID = 3575453794269732960L;

        protected KeySetUsingValueMap(final NavigableSet<K> set, final SortedMapRange<K> keyRange,  final DualTreeBidi2MapBase<K, V> parent) {
            // but the order will be wrong...
            super(set, keyRange, parent);
        }

        @Override
        protected NavigableBoundSet<K> decorateDerived(final NavigableSet<K> subSet, final SortedMapRange<K> range) {
            return new KeySetUsingValueMap<>(subSet, range, parent);
        }
    }

    protected static <K, V, R> R scanFirstMatch(final Supplier<Entry<K, V>> start,
                                                final Function<K, Entry<K, V>> next,
                                                final Predicate<V> confirm,
                                                final Function<Entry<K, V>, R> transform) {
        Entry<K, V> entry = start.get();
        while (entry != null) {
            if (confirm.test(entry.getValue()))
                return transform.apply(entry);
            entry = next.apply(entry.getKey());
        }
        return null;
    }

    protected static <K, V, R> R scanFirstMatch(final K key,
                                                final Function<K, Entry<K, V>> start,
                                                final Function<K, Entry<K, V>> next,
                                                final Predicate<V> confirm,
                                                final Function<Entry<K, V>, R> transform) {
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

        public KeySetUsingBoth(final DualTreeBidi2MapSubMap<K, V> parent, final boolean descending) {
            this.parent = parent;
            this.keyMap = descending ? parent.keyMap.descendingMap() : parent.keyMap;
            this.valueMap = parent.valueMap;
        }

        @Override
        public K lower(final K key) {
            return scanFirstMatch(key, keyMap::lowerEntry, keyMap::lowerEntry, valueMap::containsKey, Entry::getKey);
        }

        @Override
        public K floor(final K key) {
            return scanFirstMatch(key, keyMap::floorEntry, keyMap::lowerEntry, valueMap::containsKey, Entry::getKey);
        }

        @Override
        public K higher(final K key) {
            return scanFirstMatch(key, keyMap::higherEntry, keyMap::higherEntry, valueMap::containsKey, Entry::getKey);
        }

        @Override
        public K ceiling(final K key) {
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
            final Entry<K, V> entry = scanFirstMatch(keyMap::firstEntry, keyMap::higherEntry, valueMap::containsKey, Function.identity());
            if (entry != null) {
                parent.removeInternalExpectedGood(entry.getKey(), entry.getValue());
                return entry.getKey();
            }
            return null;
        }

        @Override
        public K pollLast() {
            final Entry<K, V> entry = scanFirstMatch(keyMap::lastEntry, keyMap::lowerEntry, valueMap::containsKey, Function.identity());
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
        public boolean contains(final Object o) {
            return parent.containsKey(o);
        }

        @Override
        public boolean remove(final Object o) {
            return parent.removeViaCollection(o);
        }

        @Override
        public void clear() {
            parent.clear();
        }

        @Override
        public void forEach(final Consumer<? super K> action) {
            // doesn't follow my order?
            parent.forEach((k,v) -> action.accept(k));
        }

        @Override
        public boolean removeIf(final Predicate<? super K> filter) {
            // doesn't follow my order?
            return parent.collectionRemoveIf(entry -> filter.test(entry.getKey()));
        }

        @Override
        public boolean retainAll(final Collection<?> c) {
            // doesn't follow my order?
            return parent.collectionRemoveIf(entry -> !c.contains(entry.getKey()));
        }

        @Override
        public boolean removeAll(final Collection<?> c) {
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
        public NavigableSet<K> subSet(final K fromElement, final boolean fromInclusive, final K toElement, final boolean toInclusive) {
            return parent.subMap(fromElement, fromInclusive, toElement, toInclusive).navigableKeySet();
        }

        @Override
        public NavigableSet<K> headSet(final K toElement, final boolean inclusive) {
            return parent.headMap(toElement, inclusive).navigableKeySet();
        }

        @Override
        public NavigableSet<K> tailSet(final K fromElement, final boolean inclusive) {
            return parent.tailMap(fromElement, inclusive).navigableKeySet();
        }

        @Override
        public SortedSet<K> subSet(final K fromElement, final K toElement) {
            return parent.subMap(fromElement, toElement).navigableKeySet();
        }

        @Override
        public SortedSet<K> headSet(final K toElement) {
            return parent.headMap(toElement).navigableKeySet();
        }

        @Override
        public SortedSet<K> tailSet(final K fromElement) {
            return parent.tailMap(fromElement).navigableKeySet();
        }

        @Override
        public boolean addAll(final Collection<? extends K> coll) {
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

        protected BothKeyIterator(final DualTreeBidi2MapSubMap<K, V> parent, final NavigableMap<K, V> keyMap, final Map<V, K> valueMap) {
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
            final Entry<K, V> result = next;
            if (result == null)
                throw new NoSuchElementException();
            final K key = result.getKey();

            Entry<K, V> possible = keyMap.higherEntry(key);
            while (possible != null && !valueMap.containsKey(possible.getValue())) {
                possible = keyMap.higherEntry(possible.getKey());
            }
            next = possible;

            lastResult = result;
            return key;
        }

        @Override
        public void forEachRemaining(final Consumer<? super K> action) {
            Entry<K, V> entry = next;
            while (entry != null) {
                final K key = entry.getKey();
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
