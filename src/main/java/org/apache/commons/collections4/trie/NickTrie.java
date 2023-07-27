package org.apache.commons.collections4.trie;

import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.keyvalue.AbstractMapEntry;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

// rename to GeneralRadixTrie?
// https://en.wikipedia.org/wiki/Radix_tree
public final class NickTrie<K extends Comparable<K>, V extends Comparable<V>>
        extends AbstractMap<K, V>
        implements Trie<K, V>, Serializable {
    private static final long serialVersionUID = -1993317552691676845L;

    private transient final KeyAnalyzer<K> keyAnalyzer;
    private transient final TEntry<K, V> root;
    private transient int size;
    private transient int modCount;

    public NickTrie(KeyAnalyzer<K> keyAnalyzer) {
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

    private TEntry<K, V> getEffectiveRoot() {
        if (size == 0 || !root.isEmptyRoot())
            return root;
        else
            return root.right;
    }

    /**
     * A utility method for calling {@link KeyAnalyzer#compare(Comparable, Comparable)}
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

    /**
     * A helper method to increment the {@link org.apache.commons.collections4.Trie} size and the modification counter.
     */
    void incrementSize() {
        size++;
        incrementModCount();
    }

    /**
     * A helper method to decrement the {@link org.apache.commons.collections4.Trie} size and increment the modification counter.
     */
    void decrementSize() {
        size--;
        incrementModCount();
    }

    /**
     * A helper method to increment the modification counter.
     */
    private void incrementModCount() {
        ++modCount;
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
        if (!(key instanceof Comparable<?>))
            return null;
        TEntry<K, V> entry = getEntry(castKey(key));
        if (entry != null)
            return entry.getValue();
        else
            return defaultValue;
    }

    @Override
    public boolean containsKey(Object key) {
        if (!(key instanceof Comparable<?>))
            return false;
        TEntry<K, V> entry = getEntry(castKey(key));
        return entry != null;
    }

    @Override
    public boolean containsValue(Object valueObject) {
        if (!(valueObject instanceof Comparable<?>))
            return false;
        V value = castValue(valueObject);
        TEntry<K, V> entry = firstEntry();
        while (entry != null) {
            if (valueEquals(value, entry.getValue()))
                return true;
            entry = entry.next();
        }
        return false;
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

    private TEntry<K, V> firstEntry() {
        return getEffectiveRoot().lowestGrandchild();
    }

    private TEntry<K, V> lastEntry() {
        return getEffectiveRoot().highestGrandchild();
    }

    @Override
    public K nextKey(K key) {
        TEntry<K, V> entry = getRelativeEntry(key, true, false);
        return entry != null ? entry.getKey() : null;
    }

    @Override
    public K previousKey(K key) {
        TEntry<K, V> entry = getRelativeEntry(key, false, false);
        return entry != null ? entry.getKey() : null;
    }

    @Override
    public V put(K key, V value) {
        final int keyLengthInBits = keyAnalyzer.lengthInBits(key);
        if (keyLengthInBits == 0) {
            if (root.isEmptyRoot())
                incrementSize();
            else
                incrementModCount();
            return root.setKeyValue(key, value);
        }

        TEntry<K, V> node = root.right, path = root;
        int startIndex = 0;
        while (true) {
            final K nodeKey = node.getKey();
            final int diffBit = keyAnalyzer.bitIndex(
                    key, startIndex, keyLengthInBits - startIndex,
                    nodeKey, startIndex, keyAnalyzer.lengthInBits(nodeKey) - startIndex);

            if (KeyAnalyzer.isEqualBitKey(diffBit)) {
                incrementModCount();
                return node.setValue(value);
            } else if (KeyAnalyzer.isNullBitKey(diffBit)) {
                if (root.isEmptyRoot())
                    incrementSize();
                else
                    incrementModCount();
                return node.setValue(value);
            } else if (size == 0) {
                root.right = new TEntry<>(key, value, diffBit, root, root, null);
                incrementSize();
                return null;
            } else if (diffBit < node.diffBit) {
                // need to split before we get here, attached to path
                TEntry<K, V> add = new TEntry<>(key, value, diffBit, path, null, null);
                if (path.left == node) {
                    path.left = add;
                } else {
                    path.right = add;
                }
                if (keyAnalyzer.isBitSet(key, diffBit, keyLengthInBits)) {
                    // node < add
                    add.left = node;
                    add.right = node.right;
                    node.right = add;
                } else {
                    // node > add
                    add.right = node;
                    add.left = node.left;
                    node.left = add;
                }
                // might we want to copy an uplink too?
                node.parent = add;
                return null;
            } else if (node == path) {
                TEntry<K, V> add = new TEntry<>(key, value, diffBit, path, null, null);
                assert node != root;
                assert path.left != node || path.right != node; // kinda expect another link in the mix, root if nothing else?
                if (path.left == node) {
                    path.left = add;
                    add.right = path;
                } else {
                    path.right = add;
                    add.left = path;
                }
                return null;
            } else if (node.diffBit <= path.diffBit) {
                // we've hit a self-link or uplink
                TEntry<K, V> add = new TEntry<>(key, value, diffBit, path, null, null);
                // could also be both?
                if (path.left == node) {
                    path.left = add;
                    add.right = path;
                    add.left = node;
                } else {
                    path.right = add;
                    add.left = path;
                    add.right = node;
                }
                node.parent = add;
                return null;
            } else if (diffBit == node.diffBit) {
                // may be able to keep going down
                return null;
            } else if (!keyAnalyzer.isBitSet(key, diffBit, keyLengthInBits)) {
                // zero bit means key < node
                path = node;
                node = node.left;
            } else {
                path = node;
                node = node.right;
            }

            startIndex = node.diffBit + 1;
        }
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

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    private TEntry<K, V> getEntry(K key) {
        final int keyLengthInBits = keyAnalyzer.lengthInBits(key);
        if (keyLengthInBits == 0) {
            return root.isEmptyRoot() ? null : root;
        }

        TEntry<K, V> node = getEffectiveRoot(), path = null;
        int startIndex = 0;
        while (true) {
            final K nodeKey = node.getKey();
            final int diffBit = keyAnalyzer.bitIndex(
                    key, startIndex, keyLengthInBits - startIndex,
                    nodeKey, startIndex, keyAnalyzer.lengthInBits(nodeKey) - startIndex);

            if (KeyAnalyzer.isEqualBitKey(diffBit)) {
                return node;
            } else if (KeyAnalyzer.isNullBitKey(diffBit)) {
                return node.isEmptyRoot() ? null : node;
            } else if (diffBit <= node.diffBit) {
                // if we aren't equal by this point we've gone too far
                return null;
            } else if (path != null && node.diffBit >= path.diffBit) {
                // we've hit a self-link or uplink
                return null;
            } else if (!keyAnalyzer.isBitSet(key, diffBit, keyLengthInBits)) {
                // zero bit means key < node
                path = node;
                node = node.left;
            } else {
                path = node;
                node = node.right;
            }

            startIndex = node.diffBit + 1;
        }
    }

    private TEntry<K, V> getRelativeEntry(K key, boolean higher, boolean includeEqual) {
        if (size == 0)
            return null;

        final int keyLengthInBits = keyAnalyzer.lengthInBits(key);
        if (keyLengthInBits == 0 && includeEqual) {
            if (!root.isEmptyRoot())
                return root;
        }

        TEntry<K, V> node = getEffectiveRoot(), path = null, nearest = null;
        int startIndex = 0;
        while (true) {
            final K nodeKey = node.getKey();
            final int diffBit = keyAnalyzer.bitIndex(
                    key, startIndex, keyLengthInBits - startIndex,
                    nodeKey, startIndex, keyAnalyzer.lengthInBits(nodeKey) - startIndex);

            if (KeyAnalyzer.isEqualBitKey(diffBit) && includeEqual) {
                return node;
            } else if (KeyAnalyzer.isNullBitKey(diffBit) && !node.isEmptyRoot() && includeEqual) {
                return node;
            } else if (diffBit <= node.diffBit) {
                return nearest;
            } else if (path != null && node.diffBit >= path.diffBit) {
                return nearest;
            } else if (!keyAnalyzer.isBitSet(key, diffBit, keyLengthInBits)) {
                // node > key
                if (higher) {
                    nearest = node;
                }
                path = node;
                node = node.left;
            } else {
                // node < key
                if (!higher) {
                    nearest = node;
                }
                path = node;
                node = node.right;
            }

            startIndex = node.diffBit + 1;
        }
    }

    private final static class TEntry<K, V> extends AbstractMapEntry<K, V> {
        private int diffBit;
        private TEntry<K, V> parent;
        private TEntry<K, V> left;
        private TEntry<K, V> right;

        private TEntry(K key, V value, int diffBit, TEntry<K, V> parent, TEntry<K, V> left, TEntry<K, V> right) {
            super(key, value);
            this.diffBit = diffBit;
            this.parent = parent != null ? parent : this;
            this.left = left != null ? left : this;
            this.right = right != null ? right : this;
        }

        public static <K, V> TEntry<K,V> makeRoot() {
            return new TEntry<>(null, null, -1, null, null, null);
        }

        public boolean isRoot() {
            return parent == this;
        }

        public boolean isEmptyRoot() {
            return getKey() == null;
        }

        public V setKeyValue(K key, V value) {
            setKey(key);
            return setValue(value);
        }

        public TEntry<K, V> lowestGrandchild() {
            TEntry<K, V> path = this, child = path.left;
            while (path.diffBit < child.diffBit) {
                path = child;
                child = child.left;
            }
            return path;
        }

        public TEntry<K,V> highestGrandchild() {
            TEntry<K, V> path = this, child = path.right;
            while (path.diffBit < child.diffBit) {
                path = child;
                child = child.right;
            }
            return path;
        }

        private TEntry<K,V> next() {
            assert right.diffBit != diffBit;
            if (right.diffBit > diffBit) { // is deeper
                return right.lowestGrandchild();
            } else { // uplink into higher in the tree
                return right;
            }
        }
        //            } else if (right.bitIndex == bitIndex) { // is self
//                assert false;
//                TEntry<K, V> curr = this;
//                while (curr == curr.parent.right) {
//                    curr = curr.parent;
//                }
//                if (curr == curr.parent.left) {
//                    return curr.nullIfEmptyRoot();
//                } else {
//                    return null;
//                }

        private TEntry<K,V> prev() {
            assert left.diffBit != diffBit;
            if (left.diffBit > diffBit) { // is deeper
                return left.highestGrandchild();
            } else { // uplink into higher in the tree
                return left;
            }
        }
        //            } else if (left.bitIndex == bitIndex) { // is self
//                assert false;
//                TEntry<K, V> curr = this, above = parent;
//                while (curr == above.left) {
//                    curr = above;
//                    above = curr.parent;
//                }
//                if (curr == above.right)
//                    return curr.nullIfEmptyRoot();
//                else
//                    return null;
    }
}
