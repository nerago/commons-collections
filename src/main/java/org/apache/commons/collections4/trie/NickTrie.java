package org.apache.commons.collections4.trie;

import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.keyvalue.AbstractMapEntry;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public final class NickTrie<K, V> implements Trie<K, V>, Serializable {
    private static final long serialVersionUID = -1993317552691676845L;

    private transient final KeyAnalyzer<? super K> keyAnalyzer;
    private transient final TEntry<K, V> root;
    private transient int size;
    private transient int modCount;

    public NickTrie(KeyAnalyzer<? super K> keyAnalyzer) {
        this.keyAnalyzer = keyAnalyzer;
        this.root = TEntry.makeRoot();
        this.size = 0;
        this.modCount = 0;
    }

    @SuppressWarnings("unchecked")
    private K castKey(final Object key) {
        return (K) Objects.requireNonNull(key);
    }

    @SuppressWarnings("unchecked")
    private V castValue(Object value) {
        return (V) value;
    }

    private boolean valueEquals(V a, V b) {
        return Objects.equals(a, b);
    }

    private TEntry<K, V> getRoot() {
        if (size == 0 || !root.isEmptyRoot())
            return root;
        else
            return root.right;
    }

    /**
     * A utility method for calling {@link KeyAnalyzer#compare(Object, Object)}
     */
    private boolean equalKeys(final K key, final K other) {
        if (key == null) {
            return other == null;
        }
        if (other == null) {
            return false;
        }

        return keyAnalyzer.compare(key, other) == 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public V get(Object key) {
        return getOrDefault(key, null);
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        TEntry<K, V> entry = getEntry(castKey(key));
        if (entry != null)
            return entry.getValue();
        else
            return defaultValue;
    }

    @Override
    public boolean containsKey(Object key) {
        TEntry<K, V> entry = getEntry(castKey(key));
        return entry != null;
    }

    @Override
    public boolean containsValue(Object valueObject) {
        V value = castValue(valueObject);
        TEntry<K, V> entry = firstEntry();
        while (entry != null) {
            if (valueEquals(value, entry.getValue()))
                return true;
            entry = entry.next();
        }
        return false;
    }

    private TEntry<K, V> getEntry(K key) {
        final int keyLengthInBits = keyAnalyzer.lengthInBits(key);
        final int keyLastIndex = keyLengthInBits - 1;
        if (keyLengthInBits == 0) {
            return root.nullIfEmptyRoot();
        }

        TEntry<K, V> node = getRoot();
        int startIndex = 0;
        while (true) {
            final boolean rangeEquals = keyAnalyzer.rangeEquals(
                    key, startIndex, Math.min(node.bitIndex, keyLastIndex),
                    node.getKey(), startIndex, node.bitIndex);
            if (!rangeEquals) {
                return null;
            }

            final boolean keyBit = keyAnalyzer.isBitSet(key, node.bitIndex, keyLengthInBits);
            if (keyBit) {

            }

            startIndex = node.bitIndex + 1;
        }


//        if (keyAnalyzer.isPrefix())
////        if (keyAnalyzer.isPrefix())
//        // -1 is no good
//        // TODO
//return null;
    }

    @Override
    public K firstKey() {
        TEntry<K, V> first = firstEntry();
        return first != null ? first.getKey() : null;
    }

    @Override
    public K lastKey() {
        TEntry<K, V> last = lastEntry();
        return last != null ? last.getKey() : null;
    }

    private TEntry<K,V> firstEntry() {
        return getRoot().lowestGrandChild();
    }

    private TEntry<K,V> lastEntry() {
        return getRoot().highestGrandChild();
    }

    @Override
    public K nextKey(K key) {
        return null;
    }

    @Override
    public K previousKey(K key) {
        return null;
    }

    @Override
    public V put(K key, V value) {
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {

    }

    @Override
    public V remove(Object key) {
        return null;
    }

    @Override
    public void clear() {
        root.left = null;
        root.right = null;
        root.setKeyValue(null, null);
        size = 0;
    }

//    @Override
//    public boolean remove(Object key, Object value) {
//        return Trie.super.remove(key, value);
//    }
//
//    @Override
//    public boolean replace(K key, V oldValue, V newValue) {
//        return Trie.super.replace(key, oldValue, newValue);
//    }
//
//    @Override
//    public V replace(K key, V value) {
//        return Trie.super.replace(key, value);
//    }
//
//    @Override
//    public V putIfAbsent(K key, V value) {
//        return Trie.super.putIfAbsent(key, value);
//    }
//
//    @Override
//    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
//        return Trie.super.computeIfAbsent(key, mappingFunction);
//    }
//
//    @Override
//    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
//        return Trie.super.computeIfPresent(key, remappingFunction);
//    }
//
//    @Override
//    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
//        return Trie.super.compute(key, remappingFunction);
//    }
//
//    @Override
//    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
//        return Trie.super.merge(key, value, remappingFunction);
//    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        Trie.super.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Trie.super.replaceAll(function);
    }

    @Override
    public SortedMap<K, V> prefixMap(K key) {
        return null;
    }

    @Override
    public SortedMap<K, V> subMap(K fromKey, K toKey) {
        return null;
    }

    @Override
    public SortedMap<K, V> headMap(K toKey) {
        return null;
    }

    @Override
    public SortedMap<K, V> tailMap(K fromKey) {
        return null;
    }

    @Override
    public Set<K> keySet() {
        return null;
    }

    @Override
    public Collection<V> values() {
        return null;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return null;
    }

    @Override
    public OrderedMapIterator<K, V> mapIterator() {
        return null;
    }

    @Override
    public Comparator<? super K> comparator() {
        return keyAnalyzer;
    }

    private final static class TEntry<K, V> extends AbstractMapEntry<K, V> {
        private int bitIndex;
        private TEntry<K, V> parent;
        private TEntry<K, V> left;
        private TEntry<K, V> right;

        private TEntry(K key, V value, int bitIndex, TEntry<K, V> parent, TEntry<K, V> left, TEntry<K, V> right) {
            super(key, value);
            this.bitIndex = bitIndex;
            this.parent = parent != null ? parent : this;
            this.left = left != null ? left : this;
            this.right = right != null ? right : this;
        }

        public static <K, V> TEntry<K,V> makeRoot() {
            return new TEntry<>(null, null, -1, null, null, null);
        }

        public boolean isEmptyRoot() {
            return getKey() == null;
        }

        private TEntry<K,V> nullIfEmptyRoot() {
            return isEmptyRoot() ? null : this;
        }

        public void setKeyValue(K key, V value) {
            setKey(key);
            setValue(value);
        }

        public TEntry<K, V> lowestGrandChild() {
            TEntry<K, V> parent = this, child = parent.left;
            while (parent.bitIndex < child.bitIndex) {
                parent = child;
                child = child.left;
            }
            return child;
        }

        public TEntry<K,V> highestGrandChild() {
            TEntry<K, V> parent = this, child = parent.right;
            while (parent.bitIndex < child.bitIndex) {
                parent = child;
                child = child.right;
            }
            return child;
        }

        private TEntry<K,V> next() {
            if (right.bitIndex > bitIndex) { // is deeper
                return right;
            } else if (right.bitIndex == bitIndex) { // is self
                TEntry<K, V> curr = this;
                while (curr == curr.parent.right) {
                    curr = curr.parent;
                }
                if (curr == curr.parent.left) {
                    return curr.nullIfEmptyRoot();
                } else {
                    return null;
                }
            } else { // uplink into higher in the tree
                TEntry<K, V> above = right;
                assert above != above.right;
                return above.right.nullIfEmptyRoot();
            }
        }

        private TEntry<K,V> prev() {
            if (left.bitIndex > bitIndex) { // is deeper
                return left;
            } else if (left.bitIndex == bitIndex) { // is self
                TEntry<K, V> curr = this, above = parent;
                while (curr == above.left) {
                    curr = above;
                    above = curr.parent;
                }
                if (curr == above.right)
                    return curr.nullIfEmptyRoot();
                else
                    return null;
            } else { // uplink into higher in the tree
                TEntry<K, V> above = left;
                assert above != above.left;
                return above.left.nullIfEmptyRoot();
            }
        }
    }
}
