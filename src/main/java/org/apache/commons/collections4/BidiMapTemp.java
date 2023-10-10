package org.apache.commons.collections4;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

public class BidiMapTemp {
    public <K, V> void playpen() {
        MyBidiMap<K, V> bm = new MyBidiMap<K, V>();
        BidiMap<V, K, ?> two = bm.inverseBidiMap();
        BidiMap<K, V, ?> three = two.inverseBidiMap();


        MyOrdBidiMap<K, V> om = new MyOrdBidiMap<>();
        MyOrdBidiMap<V, K> o2 = om.inverseBidiMap();
        MyOrdBidiMap<K, V> o3 = o2.inverseBidiMap();

        BidiMap<K, V, ?> omm = new MyOrdBidiMap<>();
        BidiMap<V, K, ?> omm2 = omm.inverseBidiMap();

        OrderedBidiMap<K, V, ?> obm = new MyOrdBidiMap<>();
        OrderedBidiMap<V, K, ?> obm2 = obm.inverseBidiMap();

        MyExBidiMap<K, V> ex = new MyExBidiMap<>();
        SortedExtendedBidiMap<K, V, ?, ?> ex2 = ex;
        SortedExtendedBidiMap<V, K, ?, ?> ex3 = ex2.inverseBidiMap();
        SortedExtendedBidiMap<K, V, ?, ?> ex4 = ex2.subMap(null, null);
    }

    private static class MyExBidiMap<K, V> implements SortedExtendedBidiMap<K, V, MyExBidiMap<K, V>, MyExBidiMap<V, K>> {

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean containsKey(Object key) {
            return false;
        }

        @Override
        public boolean containsValue(Object value) {
            return false;
        }

        @Override
        public V get(Object key) {
            return null;
        }

        @Override
        public V put(K key, V value) {
            return null;
        }

        @Override
        public V remove(Object key) {
            return null;
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> m) {

        }

        @Override
        public void clear() {

        }

        @Override
        public Set<K> keySet() {
            return null;
        }

        @Override
        public K getKey(Object value) {
            return null;
        }

        @Override
        public K getKeyOrDefault(Object value, K defaultKey) {
            return null;
        }

        @Override
        public K removeValue(Object value) {
            return null;
        }

        @Override
        public MyExBidiMap<V, K> inverseBidiMap() {
            return null;
        }

        @Override
        public Set<V> values() {
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
            return null;
        }

        @Override
        public K firstKey() {
            return null;
        }

        @Override
        public K lastKey() {
            return null;
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
        public Comparator<? super V> valueComparator() {
            return null;
        }

        @Override
        public SortedMapRange<V> getValueRange() {
            return null;
        }

        @Override
        public MyExBidiMap<K, V> subMap(SortedMapRange<K> range) {
            return null;
        }

        @Override
        public SortedMapRange<K> getKeyRange() {
            return null;
        }
    }

    private static class MyOrdBidiMap<K, V> implements OrderedBidiMap<K, V, MyOrdBidiMap<V, K>> {

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean containsKey(Object key) {
            return false;
        }

        @Override
        public boolean containsValue(Object value) {
            return false;
        }

        @Override
        public V get(Object key) {
            return null;
        }

        @Override
        public V put(K key, V value) {
            return null;
        }

        @Override
        public V remove(Object key) {
            return null;
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> m) {

        }

        @Override
        public void clear() {

        }

        @Override
        public Set<K> keySet() {
            return null;
        }

        @Override
        public K getKey(Object value) {
            return null;
        }

        @Override
        public K getKeyOrDefault(Object value, K defaultKey) {
            return null;
        }

        @Override
        public K removeValue(Object value) {
            return null;
        }

        @Override
        public MyOrdBidiMap<V, K> inverseBidiMap() {
            return null;
        }

        @Override
        public Set<V> values() {
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
        public K firstKey() {
            return null;
        }

        @Override
        public K lastKey() {
            return null;
        }

        @Override
        public K nextKey(K key) {
            return null;
        }

        @Override
        public K previousKey(K key) {
            return null;
        }
    }

    private static class MyBidiMap<K, V> implements BidiMap<K, V, MyBidiMap<V, K>> {

        @Override
        public MapIterator mapIterator() {
            return null;
        }

        @Override
        public K getKey(Object value) {
            return null;
        }

        @Override
        public K removeValue(Object value) {
            return null;
        }

        @Override
        public BidiMap<V, K, ?> inverseBidiMap() {
            return null;
        }

        @Override
        public Set<V> values() {
            return null;
        }

        @Override
        public Set<Entry> entrySet() {
            return null;
        }

        @Override
        public K getKeyOrDefault(Object value, Object defaultKey) {
            return null;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean containsKey(Object key) {
            return false;
        }

        @Override
        public boolean containsValue(Object value) {
            return false;
        }

        @Override
        public Object get(Object key) {
            return null;
        }

        @Override
        public V put(Object key, Object value) {
            return null;
        }

        @Override
        public Object remove(Object key) {
            return null;
        }

        @Override
        public void putAll(Map m) {

        }

        @Override
        public void clear() {

        }

        @Override
        public Set keySet() {
            return null;
        }
    }
}
