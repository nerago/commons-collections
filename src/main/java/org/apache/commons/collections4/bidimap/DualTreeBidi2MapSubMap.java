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
import org.apache.commons.collections4.map.AbstractMapViewNavigableSet;


import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
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
    protected DualTreeBidi2MapSubMap<K, V> decorateDerived(final NavigableMap<K, V> subMap, final SortedMapRange<K> keyRange) {
        return new DualTreeBidi2MapSubMap<>(subMap, keyRange, parent);
    }

    @Override
    protected void modified() {
        parent.modified();
    }

    @Override
    protected int modificationCount() {
        return parent.modificationCount();
    }

    private void checkKey(final K key) {
        if (!getKeyRange().contains(key))
            throw new IllegalArgumentException();
    }

    private void checkValue(final V value) {
        if (!getValueRange().contains(value))
            throw new IllegalArgumentException();
    }

    @Override
    public boolean containsKey(final Object keyObject) {
        final K key = castKey(keyObject);
        final V value = keyMap.get(key);
        if (value != null)
            return getValueRange().contains(value);
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
        final V value = keyMap.get(key);
        if (value != null) {
            if (getValueRange().contains(value))
                return value;
        }
        return defaultValue;
    }

    @Override
    public boolean containsValue(final Object valueObject) {
        final V value = castValue(valueObject);
        final K key = valueMap.get(value);
        return key != null && keyMap.containsKey(key);
    }

    @Override
    public K getKey(final Object valueObject) {
        final V value = castValue(valueObject);
        final K key = valueMap.get(value);
        if (key != null && keyMap.containsKey(key))
            return key;
        else
            return null;
    }

    @Override
    public K removeValue(final Object valueObject) {
        final V value = castValue(valueObject);
        final K key = valueMap.get(value);
        if (key == null || !keyMap.containsKey(key))
            return null;
        keyMapRemoveChecked(key, value);
        valueMapRemoveChecked(value, key);
        modified();
        return key;
    }

    @Override
    protected boolean removeValueViaCollection(final Object valueObject) {
        final V value = castValue(valueObject);
        final K key = valueMap.get(value);
        if (key == null || !keyMap.containsKey(key))
            return false;
        keyMapRemoveChecked(key, value);
        valueMapRemoveChecked(value, key);
        modified();
        return true;
    }

    @Override
    public V firstValue() {
        final Entry<V, K> entry = valueMap.firstEntry();
        if (keyMap.containsKey(entry.getValue())) {
            return entry.getKey();
        } else {
            return null;
        }
    }

    @Override
    public V lastValue() {
        final Entry<V, K> entry = valueMap.lastEntry();
        if (keyMap.containsKey(entry.getValue())) {
            return entry.getKey();
        } else {
            return null;
        }
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

        final V currentValue = keyMap.get(key);

        if (currentValue == null) {
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
        final V value = keyMap.get(key);
        if (value != null && valueMap.containsKey(value)) {
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
        final V value = keyMap.get(key);
        if (value != null && valueMap.containsKey(value)) {
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

        final V currentValue = keyMap.get(key);
        if (currentValue == null)
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
        final V currentValue = keyMap.get(key);
        if (currentValue == null) {
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
        final V currentValue = keyMap.get(key);
        if (currentValue == null) {
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

        final V currentValue = keyMap.get(key);
        if ((currentValue == null && primaryMap().keyMap.containsKey(key)) || !valueMap.containsKey(currentValue))
            throw new ValueChangeNotAllowedException("key already exists in primary map but not this sub map");

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
        Objects.requireNonNull(remappingFunction);

        final V currentValue = keyMap.get(key);
        if ((currentValue == null && primaryMap().keyMap.containsKey(key)) || !valueMap.containsKey(currentValue))
                throw new ValueChangeNotAllowedException("key already exists in primary map but not this sub map");

        if (currentValue == null) {
            keyMapAddChecked(key, value);
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
        final K oldKeyForNewValue = primaryMap().valueMap.get(newValue);
        if (oldKeyForNewValue != null) {
            if (!keyMap.containsKey(oldKeyForNewValue) || !keyEquals(valueMap.get(newValue), oldKeyForNewValue))
                throw new ValueChangeNotAllowedException("old value exists outside this sub map and can't update");
            valueMapReplaceChecked(newValue, oldKeyForNewValue, key);
            keyMapRemoveChecked(oldKeyForNewValue, newValue);
        } else {
            valueMapAddChecked(newValue, key);
        }
    }

    @Override
    protected NavigableRangedSet<K> createKeySet(final boolean descending) {
        if (valueRange.isFull())
            return new KeySetUsingKeyMapSubSet<>(this, descending);
        else
            return new KeySetUsingBoth<>(this, descending);
    }

    @Override
    protected NavigableRangedSet<V> createValueSet() {
        if (valueRange.isFull())
            return new ValueSetUsingKeyEntrySetSubKeyRange<>(this);
        else
            return new ValueSetUsingKeyEntrySetFiltered<>(this);
    }

    @Override
    protected NavigableRangedSet<Entry<K, V>> createEntrySet() {
        if (valueRange.isFull())
            return new EntrySetUsingKeyMap<>(this);
        else
            return new EntrySetUsingBoth<>(this, false);
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
        protected BaseNavigableSet<K, K, V> decorateDerived(final NavigableSet<K> subSet, final SortedMapRange<K> range) {
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

    private abstract static class BaseUsingBoth<E, K extends Comparable<K>, V extends Comparable<V>>
            extends AbstractMapViewNavigableSet<E> {
        protected final DualTreeBidi2MapSubMap<K, V> parent;
        protected SortedMapRange<E> range;
        private boolean descending;
        protected final NavigableMap<K, V> keyMap;
        protected final Map<V, K> valueMap;

        public BaseUsingBoth(final DualTreeBidi2MapSubMap<K, V> parent, final SortedMapRange<E> range, final boolean descending) {
            this.parent = parent;
            this.range = range;
            this.descending = descending;
            this.keyMap = descending ? parent.keyMap.descendingMap() : parent.keyMap;
            this.valueMap = parent.valueMap;
        }

        protected abstract E toResult(Entry<K, V> entry);

        protected abstract K toKey(E element);

        protected abstract NavigableRangedSet<E> createSimilar(DualTreeBidi2MapSubMap<K, V> parent, boolean descending);

        @Override
        public SortedMapRange<E> getRange() {
            return range;
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
        public void clear() {
            parent.clear();
        }

        @Override
        public Iterator<E> iterator() {
            return new BothIterator<>(parent, keyMap, valueMap, this::toResult);
        }

        @Override
        public Iterator<E> descendingIterator() {
            return new BothIterator<>(parent, keyMap.descendingMap(), valueMap, this::toResult);
        }

        @Override
        public E lower(final E element) {
            return scanFirstMatch(toKey(element), keyMap::lowerEntry, keyMap::lowerEntry, valueMap::containsKey, this::toResult);
        }

        @Override
        public E floor(final E element) {
            return scanFirstMatch(toKey(element), keyMap::floorEntry, keyMap::lowerEntry, valueMap::containsKey, this::toResult);
        }

        @Override
        public E higher(final E element) {
            return scanFirstMatch(toKey(element), keyMap::higherEntry, keyMap::higherEntry, valueMap::containsKey, this::toResult);
        }

        @Override
        public E ceiling(final E element) {
            return scanFirstMatch(toKey(element), keyMap::ceilingEntry, keyMap::higherEntry, valueMap::containsKey, this::toResult);
        }

        @Override
        public E first() {
            return scanFirstMatch(keyMap::firstEntry, keyMap::higherEntry, valueMap::containsKey, this::toResult);
        }

        @Override
        public E last() {
            return scanFirstMatch(keyMap::lastEntry, keyMap::lowerEntry, valueMap::containsKey, this::toResult);
        }

        @Override
        public E pollFirst() {
            final Entry<K, V> entry = scanFirstMatch(keyMap::firstEntry, keyMap::higherEntry, valueMap::containsKey, Function.identity());
            if (entry != null) {
                parent.removeInternalExpectedGood(entry.getKey(), entry.getValue());
                return toResult(entry);
            }
            return null;
        }

        @Override
        public E pollLast() {
            final Entry<K, V> entry = scanFirstMatch(keyMap::lastEntry, keyMap::lowerEntry, valueMap::containsKey, Function.identity());
            if (entry != null) {
                parent.removeInternalExpectedGood(entry.getKey(), entry.getValue());
                return toResult(entry);
            }
            return null;
        }

        @Override
        public boolean removeIf(final Predicate<? super E> filter) {
            return parent.collectionRemoveIf(entry -> filter.test(toResult(entry)));
        }

        @Override
        public NavigableRangedSet<E> descendingSet() {
            return createSimilar(parent, !descending);
        }

//        @Override
//        public NavigableRangedSet<E> reversed() {
//            return descendingSet();
//        }

        @Override
        public NavigableRangedSet<E> subSet(final SortedMapRange<E> range) {
            return createSimilar(
                    (DualTreeBidi2MapSubMap<K, V>)
                    parent.subMap(toKey(range.getFromKey()), range.isFromInclusive(), toKey(range.getToKey()), range.isToInclusive()),
                    descending);
        }

        @Override
        public void writeExternal(final ObjectOutput out) throws IOException {
            out.writeBoolean(descending);
            out.writeObject(range);
            parent.writeExternal(out);
        }

        @Override
        public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
            descending = in.readBoolean();
            range = (SortedMapRange<E>) in.readObject();
            parent.readExternal(in);
        }
    }

    private static final class KeySetUsingBoth<K extends Comparable<K>, V extends Comparable<V>>
            extends BaseUsingBoth<K, K, V> {
        public KeySetUsingBoth(final DualTreeBidi2MapSubMap<K, V> parent, final boolean descending) {
            super(parent, parent.getKeyRange(), descending);
        }

        @Override
        protected K toResult(final Entry<K, V> entry) {
            return getKeyNullSafe(entry);
        }

        @Override
        protected K toKey(final K key) {
            return key;
        }

        @Override
        protected NavigableRangedSet<K> createSimilar(final DualTreeBidi2MapSubMap<K, V> parent, final boolean descending) {
            return new KeySetUsingBoth<>(parent, descending);
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
        public Comparator<? super K> comparator() {
            return parent.comparator();
        }
    }

    private static final class EntrySetUsingBoth<K extends Comparable<K>, V extends Comparable<V>>
            extends BaseUsingBoth<Entry<K, V>, K, V> {
        public EntrySetUsingBoth(final DualTreeBidi2MapSubMap<K, V> parent, final boolean descending) {
            super(parent, parent.getKeyRange().toEntryRange(), descending);
        }

        @Override
        protected Entry<K, V> toResult(final Entry<K, V> entry) {
            return entry;
        }

        @Override
        protected K toKey(final Entry<K, V> entry) {
            return getKeyNullSafe(entry);
        }

        @Override
        protected NavigableRangedSet<Entry<K, V>> createSimilar(final DualTreeBidi2MapSubMap<K, V> parent, final boolean descending) {
            return new EntrySetUsingBoth<>(parent, descending);
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
        public Comparator<? super Entry<K, V>> comparator() {
            return Entry.comparingByKey(parent.comparator());
        }
    }

    private static class BothIterator<E, K extends Comparable<K>, V extends Comparable<V>>
            implements ResettableIterator<E> {
        private final DualTreeBidi2MapSubMap<K, V> parent;
        private final NavigableMap<K, V> keyMap;
        private final Map<V, K> valueMap;
        private final Function<Entry<K, V>, E> transform;
        private Entry<K, V> next;
        private Entry<K, V> lastResult;
        private int expectedModCount;

        protected BothIterator(final DualTreeBidi2MapSubMap<K, V> parent,
                               final NavigableMap<K, V> keyMap, final Map<V, K> valueMap,
                               final Function<Entry<K, V>, E> transform) {
            this.parent = parent;
            this.keyMap = keyMap;
            this.valueMap = valueMap;
            this.transform = transform;
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
            expectedModCount = parent.modificationCount();
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public E next() {
            if (parent.modificationCount() != expectedModCount)
                throw new ConcurrentModificationException();

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
            return transform.apply(result);
        }

        @Override
        public void forEachRemaining(final Consumer<? super E> action) {
            if (parent.modificationCount() != expectedModCount)
                throw new ConcurrentModificationException();

            Entry<K, V> entry = next;
            while (entry != null) {
                final K key = entry.getKey();
                if (valueMap.containsKey(entry.getValue())) {
                    action.accept(transform.apply(entry));
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
            if (parent.modificationCount() != expectedModCount)
                throw new ConcurrentModificationException();
            parent.removeInternalExpectedGood(lastResult.getKey(), lastResult.getValue());
            expectedModCount = parent.modificationCount();
            lastResult = null;
        }
    }
}
