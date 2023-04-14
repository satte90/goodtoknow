package com.teliacompany.tiberius.base.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Util class to initialize maps in one line. Useful when creating static maps with pre defined, never changing key-value pairs.
 */
@SuppressWarnings("unused")
public final class MapInitializer {
    private MapInitializer() {
        //Util class - not to be initialized
    }

    @SafeVarargs
    public static <K, V> Map<K, V> hashMapWithEntries(Entry<K, V>... entries) {
        return Arrays.stream(entries).collect(Collectors.toMap(Entry::getKey, Entry::getValue, MapInitializer::mergeUsingLast, HashMap::new));
    }

    @SafeVarargs
    public static <K, V> Map<K, V> treeMapWithEntries(Entry<K, V>... entries) {
        return Arrays.stream(entries).collect(Collectors.toMap(Entry::getKey, Entry::getValue, MapInitializer::mergeUsingLast, TreeMap::new));
    }

    @SafeVarargs
    public static <K, V> Map<K, V> linkedHashMapWithEntries(Entry<K, V>... entries) {
        return Arrays.stream(entries).collect(Collectors.toMap(Entry::getKey, Entry::getValue, MapInitializer::mergeUsingLast, LinkedHashMap::new));
    }

    private static <U> U mergeUsingLast(U u, U u1) {
        return u1;
    }

    public static class Entry<K, V> {
        private final K key;
        private final V value;

        private Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        K getKey() {
            return key;
        }

        V getValue() {
            return value;
        }

        public static <K, V> Entry<K, V> of(K key, V value) {
            return new Entry<>(key, value);
        }
    }

}
