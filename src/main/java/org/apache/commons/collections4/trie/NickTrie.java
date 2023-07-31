package org.apache.commons.collections4.trie;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.ResettableIterator;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.keyvalue.AbstractMapEntry;
import org.apache.commons.collections4.map.EntrySetUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.IntFunction;
import java.util.function.Predicate;

// rename to GeneralRadixTrie?
// https://en.wikipedia.org/wiki/Radix_tree
public class NickTrie<K extends Comparable<K>, V extends Comparable<V>>
        extends AbstractMap<K, V>
        implements Trie<K, V>, Serializable {
    private static final long serialVersionUID = -1993317552691676845L;

    private transient final KeyAnalyzer<K> keyAnalyzer;
    private transient final TEntry<K, V> root;
    private transient int size;
    private transient int modCount;

    private transient Set<K> keySet;
    private transient Collection<V> values;
    private transient Set<Map.Entry<K, V>> entrySet;

    public NickTrie(KeyAnalyzer<K> keyAnalyzer) {
        this.keyAnalyzer = keyAnalyzer;
        this.root = TEntry.makeRoot();
        this.size = 0;
        this.modCount = 0;
    }

    @SuppressWarnings("unchecked")
    protected final K castKey(final Object key) {
        return (K) Objects.requireNonNull(key);
    }

    @SuppressWarnings("unchecked")
    protected final V castValue(final Object value) {
        return (V) value;
    }

    protected boolean valueEquals(final V a, final V b) {
        return Objects.equals(a, b);
    }

    protected TEntry<K, V> getEffectiveRoot() {
        if (size == 0 || !root.isEmptyRoot())
            return root;
        else
            return root.right;
    }

    /**
     * A utility method for calling {@link KeyAnalyzer#compare(Comparable, Comparable)}
     */
    protected boolean equalKeys(final K key, final K other) {
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
    protected void incrementSize() {
        size++;
        incrementModCount();
    }

    /**
     * A helper method to decrement the {@link org.apache.commons.collections4.Trie} size and increment the modification counter.
     */
    protected void decrementSize() {
        size--;
        incrementModCount();
    }

    /**
     * A helper method to increment the modification counter.
     */
    protected void incrementModCount() {
        ++modCount;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public V get(final Object key) {
        return getOrDefault(key, null);
    }

    @Override
    public V getOrDefault(final Object key, final V defaultValue) {
        if (!(key instanceof Comparable<?>))
            return null;
        TEntry<K, V> entry = getEntry(castKey(key));
        if (entry != null)
            return entry.getValue();
        else
            return defaultValue;
    }

    @Override
    public boolean containsKey(final Object key) {
        if (!(key instanceof Comparable<?>))
            return false;
        TEntry<K, V> entry = getEntry(castKey(key));
        return entry != null;
    }

    @Override
    public boolean containsValue(final Object valueObject) {
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

    private boolean removeValue(final Object valueObject) {
        if (!(valueObject instanceof Comparable<?>))
            return false;
        V value = castValue(valueObject);
        TEntry<K, V> entry = firstEntry();
        while (entry != null) {
            if (valueEquals(value, entry.getValue())) {
                removeEntry(entry);
                return true;
            }
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
    public K nextKey(final K key) {
        TEntry<K, V> entry = getRelativeEntry(key, true, false);
        return entry != null ? entry.getKey() : null;
    }

    @Override
    public K previousKey(final K key) {
        TEntry<K, V> entry = getRelativeEntry(key, false, false);
        return entry != null ? entry.getKey() : null;
    }

    @Override
    public V put(final K key, final V value) {
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
                if (node.isEmptyRoot())
                    incrementSize();
                else
                    incrementModCount();
                return node.setKeyValue(key, value);
            } else if (size == 0 || (size == 1 && !root.isEmptyRoot())) {
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
                incrementSize();
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
                incrementSize();
                return null;
            } else if (node.diffBit <= path.diffBit) {
                // we've hit a self-link or uplink
                TEntry<K, V> add = new TEntry<>(key, value, diffBit, path, null, null);
                assert path.left != node || path.right != node; // not sure if true
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
                incrementSize();
                return null;
//            } else if (diffBit == node.diffBit) {
//                // may be able to keep going down
//                return null;
            } else if (!keyAnalyzer.isBitSet(key, diffBit, keyLengthInBits)) {
                // key < node
                path = node;
                node = node.left;
            } else {
                // key > node
                path = node;
                node = node.right;
            }

            startIndex = node.diffBit + 1;
        }
    }

    protected void removeEntry(final TEntry<K,V> entry) {
        final TEntry<K, V> parent = entry.parent, left = entry.left, right = entry.right;
        assert parent.left == entry || parent.right == entry;
        assert parent.left != entry || parent.right != entry;
        assert left != right;
        assert entry != root;

//        final TEntry<K, V> leftOuterLink = left.lowestUplink();
//        final TEntry<K, V> leftInnerLink = left.highestUplink();
//        final TEntry<K, V> rightInnerLink = right.lowestUplink();
//        final TEntry<K, V> rightOuterLink = right.highestUplink();

        final TEntry<K, V> leftOuterNode = left.lowestGrandchild();
        final TEntry<K, V> leftInnerNode = left.highestGrandchild();
        final TEntry<K, V> rightInnerNode = right.lowestGrandchild();
        final TEntry<K, V> rightOuterNode = right.highestGrandchild();

        assert leftInnerNode.right == entry;
        assert rightInnerNode.left == entry;

        TEntry<K, V> replacement = null;
        if (entry == entry.left) {
            replacement = entry.right;
        } else if (entry == entry.right) {
            replacement = entry.left;
        }

        if (parent.left == entry) {
            parent.left = replacement;
        } else {
            parent.right = replacement;
        }
        // TODO recalculate replacement's indexes


        entry.setKeyValue(null, null);
        entry.left = null;
        entry.right = null;
        entry.parent = null;

        decrementSize();
    }

    @Override
    public V remove(final Object keyObject) {
        if (!(keyObject instanceof Comparable<?>))
            return null;
        final TEntry<K, V> entry = getEntry(castKey(keyObject));
        if (entry != null) {
            removeEntry(entry);
            return entry.getValue();
        } else {
            return null;
        }
    }

    @Override
    public void clear() {
        root.left = root;
        root.right = root;
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
        if (keySet != null)
            return keySet;
        else
            return keySet = new TrieKeySet<>(this);
    }

    @Override
    public Collection<V> values() {
        if (values != null)
            return values;
        else
            return values = new TrieValues<>(this);
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        if (entrySet != null)
            return entrySet;
        else
            return entrySet = new TrieEntrySet<>(this);
    }

    @Override
    public OrderedMapIterator<K, V> mapIterator() {
        return new TrieMapIterator<>(this);
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

    private @Nullable TEntry<K, V> getEntry(K key) {
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

        public TEntry<K, V> lowestUplink() {
            TEntry<K, V> path = this, child = path.left;
            while (path.diffBit < child.diffBit) {
                path = child;
                child = child.left;
            }
            return child;
        }

        public TEntry<K,V> highestGrandchild() {
            TEntry<K, V> path = this, child = path.right;
            while (path.diffBit < child.diffBit) {
                path = child;
                child = child.right;
            }
            return path;
        }

        public TEntry<K, V> highestUplink() {
            TEntry<K, V> path = this, child = path.right;
            while (path.diffBit < child.diffBit) {
                path = child;
                child = child.right;
            }
            return child;
        }

        private TEntry<K,V> next() {
            //assert right.diffBit != diffBit;
            if (right == this) {
                return null;
            } else if (right.diffBit > diffBit) { // is deeper
                return right.lowestGrandchild();
            } else { // uplink into higher in the tree
                return right;
            }
        }

        private TEntry<K,V> prev() {
            // assert left.diffBit != diffBit;
            if (left == this) {
                return null;
            } else if (left.diffBit > diffBit) { // is deeper
                return left.highestGrandchild();
            } else { // uplink into higher in the tree
                return left;
            }
        }
    }

    private static class TrieMapIterator<K extends Comparable<K>, V extends Comparable<V>>
            implements OrderedMapIterator<K, V>, ResettableIterator<K> {
        private final NickTrie<K, V> parent;
        private TEntry<K, V> current;
        private TEntry<K, V> nextEntry;
        private TEntry<K, V> previousEntry;

        public TrieMapIterator(NickTrie<K, V> parent) {
            this.parent = parent;
            reset();
        }

        @Override
        public void reset() {
            nextEntry = parent.firstEntry();
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
            if (nextEntry == null)
                throw new NoSuchElementException();
            current = nextEntry;
            nextEntry = nextEntry.next();
            previousEntry = current;
            return current.getKey();
        }

        @Override
        public K previous() {
            if (previousEntry == null)
                throw new NoSuchElementException();
            current = previousEntry;
            previousEntry = previousEntry.prev();
            nextEntry = current;
            return current.getKey();
        }

        @Override
        public K getKey() {
            if (current == null)
                throw new IllegalStateException();
            return current.getKey();
        }

        @Override
        public V getValue() {
            if (current == null)
                throw new IllegalStateException();
            return current.getValue();
        }

        @Override
        public void remove() {
            if (current == null)
                throw new IllegalStateException();
            parent.removeEntry(current);
            current = null;
        }

        @Override
        public V setValue(V value) {
            parent.incrementModCount();
            return current.setValue(value);
        }
    }

    private static abstract class TrieView<E, K extends Comparable<K>, V extends Comparable<V>>
        extends AbstractCollection<E> {
        protected final NickTrie<K, V> parent;

        public TrieView(NickTrie<K,V> parent) {
            this.parent = parent;
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
        public boolean equals(Object other) {
            if (this == other) return true;
            if (other instanceof Set) {
                Set<?> otherSet = (Set<?>) other;
                return SetUtils.isEqualSet(this, otherSet);
            } else {
                return false;
            }
        }
    }

    private static class TrieEntrySet<K extends Comparable<K>, V extends Comparable<V>>
            extends TrieView<Entry<K, V>, K, V>
            implements Set<Entry<K, V>> {
        public TrieEntrySet(NickTrie<K, V> parent) {
            super(parent);
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return null;
        }

        @Override
        public Spliterator<Entry<K, V>> spliterator() {
            return Set.super.spliterator();
        }

        protected TEntry<K, V> findCandidateEntry(final Object obj) {
            if (!(obj instanceof Map.Entry)) {
                return null;
            }

            final Map.Entry<?, ?> entry = (Entry<?, ?>) obj;
            final Object keyObject = entry.getKey();
            if (!(keyObject instanceof Comparable)) {
                return null;
            }

            final K key = parent.castKey(keyObject);
            return parent.getEntry(key);
        }

        @Override
        public boolean contains(@Nullable Object obj) {
            final TEntry<K, V> candidate = findCandidateEntry(obj);
            return Objects.equals(candidate, obj);
        }

        @Override
        public boolean remove(final Object obj) {
            final TEntry<K, V> candidate = findCandidateEntry(obj);
            if (Objects.equals(candidate, obj)) {
                parent.removeEntry(candidate);
                return true;
            }
            return false;
        }

        @Override
        public Object[] toArray() {
            return EntrySetUtil.toArrayUnmodifiable(this);
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return EntrySetUtil.toArrayUnmodifiable(this, a);
        }

        @Override
        public boolean add(final Entry<K, V> entry) {
            Objects.requireNonNull(entry);
            V oldValue = parent.put(entry.getKey(), entry.getValue());
            return parent.valueEquals(oldValue, entry.getValue());
        }

        @Override
        public boolean removeAll(Collection<?> coll) {
            boolean changed = false;
            for (Object item : coll) {
                changed |= remove(item);
            }
            return changed;
        }
    }

    private static class TrieKeySet<K extends Comparable<K>, V extends Comparable<V>>
            extends TrieView<K, K, V>
            implements Set<K> {
        public TrieKeySet(NickTrie<K, V> parent) {
            super(parent);
        }

        @Override
        public Iterator<K> iterator() {
            return null;
        }

        @Override
        public Spliterator<K> spliterator() {
            return Set.super.spliterator();
        }

        @Override
        public boolean contains(Object o) {
            return parent.containsKey(o);
        }

        @Override
        public boolean remove(Object keyObject) {
            if (keyObject instanceof Comparable<?>) {
                final TEntry<K, V> entry = parent.getEntry(parent.castKey(keyObject));
                if (entry != null) {
                    parent.removeEntry(entry);
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean removeAll(Collection<?> coll) {
            boolean changed = false;
            for (Object item : coll) {
                changed |= remove(item);
            }
            return changed;
        }
    }

    private static class TrieValues<K extends Comparable<K>, V extends Comparable<V>>
            extends TrieView<V, K, V>
            implements Collection<V> {
        public TrieValues(NickTrie<K, V> parent) {
            super(parent);
        }

        @Override
        public Iterator<V> iterator() {
            return null;
        }

        @Override
        public Spliterator<V> spliterator() {
            return super.spliterator();
        }

        @Override
        public boolean contains(Object value) {
            return parent.containsValue(value);
        }

        @Override
        public boolean remove(Object value) {
            return parent.removeValue(value);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (other instanceof Collection) {
                Collection<?> otherCollection = (Collection<?>) other;
                return CollectionUtils.isEqualCollection(this, otherCollection);
            } else {
                return false;
            }
        }
    }
}
