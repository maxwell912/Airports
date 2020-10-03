package airport.info;

interface PrefixMap<K, V> {
    void add(K key, V value);
    Iterable<V> getByPrefix(K key);
}
