package com.teliacompany.tiberius.base.hazelcast;

import com.hazelcast.aggregation.Aggregator;
import com.hazelcast.config.IndexConfig;
import com.hazelcast.core.EntryView;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.map.IMap;
import com.hazelcast.map.LocalMapStats;
import com.hazelcast.map.MapInterceptor;
import com.hazelcast.map.QueryCache;
import com.hazelcast.map.listener.MapListener;
import com.hazelcast.map.listener.MapPartitionLostListener;
import com.hazelcast.projection.Projection;
import com.hazelcast.query.Predicate;
import com.teliacompany.webflux.error.exception.server.NotImplementedException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Use this IMap for unit tests etc
 *
 * Usage:
 * Create a class extending your TiberiusHazelcastCache, and in its constructor call initialize(myHazelcastTestMap)
 */
public class TiberiusHazelcastTestCacheMap<K, V> extends HashMap<K, V> implements IMap<K, V> {
    private final String serviceName;
    private final String cacheName;

    public TiberiusHazelcastTestCacheMap(String serviceName, String cacheName) {
        super();
        this.serviceName = serviceName;
        this.cacheName = cacheName;
    }

    @Override
    public void removeAll(Predicate<K, V> predicate) {
        throw new NotImplementedException("Method not implemented");
    }

    @Override
    public void delete(Object o) {
        super.remove(o);
    }

    @Override
    public void flush() {
        super.clear();
    }

    @Override
    public Map<K, V> getAll(Set<K> set) {
        if(set == null) {
            return new HashMap<>();
        }
        return set.stream().collect(Collectors.toMap(k -> k, super::get));
    }

    @Override
    public void loadAll(boolean b) {

    }

    @Override
    public void loadAll(Set<K> set, boolean b) {

    }

    @Override
    public CompletionStage<V> getAsync(K k) {
        return CompletableFuture.completedFuture(super.get(k));
    }

    @Override
    public CompletionStage<V> putAsync(K k, V v) {
        return CompletableFuture.completedFuture(super.put(k, v));
    }

    @Override
    public CompletionStage<V> putAsync(K k, V v, long l, TimeUnit timeUnit) {
        return CompletableFuture.completedFuture(super.put(k, v));
    }

    @Override
    public CompletionStage<V> putAsync(K k, V v, long l, TimeUnit timeUnit, long l1, TimeUnit timeUnit1) {
        return CompletableFuture.completedFuture(super.put(k, v));
    }

    @Override
    public CompletionStage<Void> putAllAsync(Map<? extends K, ? extends V> map) {
        super.putAll(map);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletionStage<Void> setAsync(K k, V v) {
        super.put(k, v);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletionStage<Void> setAsync(K k, V v, long l, TimeUnit timeUnit) {
        super.put(k, v);
        return CompletableFuture.completedFuture(null);    }

    @Override
    public CompletionStage<Void> setAsync(K k, V v, long l, TimeUnit timeUnit, long l1, TimeUnit timeUnit1) {
        super.put(k, v);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletionStage<V> removeAsync(K k) {
        return CompletableFuture.completedFuture(super.remove(k));
    }

    @Override
    public boolean tryRemove(K k, long l, TimeUnit timeUnit) {
        V v = super.remove(k);
        return v != null;
    }

    @Override
    public boolean tryPut(K k, V v, long l, TimeUnit timeUnit) {
        super.put(k, v);
        return true;
    }

    @Override
    public V put(K k, V v, long l, TimeUnit timeUnit) {
        return super.put(k, v);
    }

    @Override
    public V put(K k, V v, long l, TimeUnit timeUnit, long l1, TimeUnit timeUnit1) {
        return super.put(k, v);
    }

    @Override
    public void putTransient(K k, V v, long l, TimeUnit timeUnit) {
        super.put(k, v);
    }

    @Override
    public void putTransient(K k, V v, long l, TimeUnit timeUnit, long l1, TimeUnit timeUnit1) {
        super.put(k, v);
    }

    @Override
    public V putIfAbsent(K k, V v, long l, TimeUnit timeUnit) {
        return super.putIfAbsent(k, v);
    }

    @Override
    public V putIfAbsent(K k, V v, long l, TimeUnit timeUnit, long l1, TimeUnit timeUnit1) {
        return super.putIfAbsent(k, v);
    }

    @Override
    public void set(K k, V v) {
        super.put(k, v);
    }

    @Override
    public void set(K k, V v, long l, TimeUnit timeUnit) {
        super.put(k, v);
    }

    @Override
    public void set(K k, V v, long l, TimeUnit timeUnit, long l1, TimeUnit timeUnit1) {
        super.put(k, v);
    }

    @Override
    public void setAll(Map<? extends K, ? extends V> map) {
        super.putAll(map);
    }

    @Override
    public CompletionStage<Void> setAllAsync(Map<? extends K, ? extends V> map) {
        super.putAll(map);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void lock(K k) {
        throw new NotImplementedException("Method not implemented");
    }

    @Override
    public void lock(K k, long l, TimeUnit timeUnit) {
        throw new NotImplementedException("Method not implemented");
    }

    @Override
    public boolean isLocked(K k) {
        return false;
    }

    @Override
    public boolean tryLock(K k) {
        return true;
    }

    @Override
    public boolean tryLock(K k, long l, TimeUnit timeUnit) throws InterruptedException {
        return true;
    }

    @Override
    public boolean tryLock(K k, long l, TimeUnit timeUnit, long l1, TimeUnit timeUnit1) throws InterruptedException {
        return true;
    }

    @Override
    public void unlock(K k) {

    }

    @Override
    public void forceUnlock(K k) {

    }

    @Override
    public UUID addLocalEntryListener(MapListener mapListener) {
        throw new NotImplementedException("Method not implemented");
    }

    @Override
    public UUID addLocalEntryListener(MapListener mapListener, Predicate<K, V> predicate, boolean b) {
        throw new NotImplementedException("Method not implemented");
    }

    @Override
    public UUID addLocalEntryListener(MapListener mapListener, Predicate<K, V> predicate, K k, boolean b) {
        throw new NotImplementedException("Method not implemented");
    }

    @Override
    public String addInterceptor(MapInterceptor mapInterceptor) {
        throw new NotImplementedException("Method not implemented");
    }

    @Override
    public boolean removeInterceptor(String s) {
        throw new NotImplementedException("Method not implemented");
    }

    @Override
    public UUID addEntryListener(MapListener mapListener, boolean b) {
        throw new NotImplementedException("Method not implemented");
    }

    @Override
    public boolean removeEntryListener(UUID uuid) {
        throw new NotImplementedException("Method not implemented");
    }

    @Override
    public UUID addPartitionLostListener(MapPartitionLostListener mapPartitionLostListener) {
        throw new NotImplementedException("Method not implemented");
    }

    @Override
    public boolean removePartitionLostListener(UUID uuid) {
        throw new NotImplementedException("Method not implemented");
    }

    @Override
    public UUID addEntryListener(MapListener mapListener, K k, boolean b) {
        throw new NotImplementedException("Method not implemented");
    }

    @Override
    public UUID addEntryListener(MapListener mapListener, Predicate<K, V> predicate, boolean b) {
        throw new NotImplementedException("Method not implemented");
    }

    @Override
    public UUID addEntryListener(MapListener mapListener, Predicate<K, V> predicate, K k, boolean b) {
        throw new NotImplementedException("Method not implemented");
    }

    @Override
    public EntryView<K, V> getEntryView(K k) {
        throw new NotImplementedException("Method not implemented");
    }

    @Override
    public boolean evict(K k) {
        super.remove(k);
        return true;
    }

    @Override
    public void evictAll() {
        super.clear();
    }

    @Override
    public Set<K> keySet(Predicate<K, V> predicate) {
        throw new NotImplementedException("Method not implemented");
    }

    @Override
    public Set<Entry<K, V>> entrySet(Predicate<K, V> predicate) {
        throw new NotImplementedException("Method not implemented");
    }

    @Override
    public Collection<V> values(Predicate<K, V> predicate) {
        throw new NotImplementedException("Method not implemented");
    }

    @Override
    public Set<K> localKeySet() {
        throw new NotImplementedException("Method not implemented");
    }

    @Override
    public Set<K> localKeySet(Predicate<K, V> predicate) {
        throw new NotImplementedException("Method not implemented");
    }

    @Override
    public void addIndex(IndexConfig indexConfig) {

    }

    @Override
    public LocalMapStats getLocalMapStats() {
        throw new NotImplementedException("Method not implemented");
    }

    @Override
    public <R> R executeOnKey(K k, EntryProcessor<K, V, R> entryProcessor) {
        throw new NotImplementedException("Method not implemented");
    }

    @Override
    public <R> Map<K, R> executeOnKeys(Set<K> set, EntryProcessor<K, V, R> entryProcessor) {
        throw new NotImplementedException("Method not implemented");
    }

    @Override
    public <R> CompletionStage<Map<K, R>> submitToKeys(Set<K> set, EntryProcessor<K, V, R> entryProcessor) {
        throw new NotImplementedException("Method not implemented");
    }

    @Override
    public <R> CompletionStage<R> submitToKey(K k, EntryProcessor<K, V, R> entryProcessor) {
        throw new NotImplementedException("Method not implemented");
    }

    @Override
    public <R> Map<K, R> executeOnEntries(EntryProcessor<K, V, R> entryProcessor) {
        throw new NotImplementedException("Method not implemented");
    }

    @Override
    public <R> Map<K, R> executeOnEntries(EntryProcessor<K, V, R> entryProcessor, Predicate<K, V> predicate) {
        throw new NotImplementedException("Method not implemented");
    }

    @Override
    public <R> R aggregate(Aggregator<? super Entry<K, V>, R> aggregator) {
        throw new NotImplementedException("Method not implemented");
    }

    @Override
    public <R> R aggregate(Aggregator<? super Entry<K, V>, R> aggregator, Predicate<K, V> predicate) {
        throw new NotImplementedException("Method not implemented");
    }

    @Override
    public <R> Collection<R> project(Projection<? super Entry<K, V>, R> projection) {
        throw new NotImplementedException("Method not implemented");
    }

    @Override
    public <R> Collection<R> project(Projection<? super Entry<K, V>, R> projection, Predicate<K, V> predicate) {
        throw new NotImplementedException("Method not implemented");
    }

    @Override
    public QueryCache<K, V> getQueryCache(String s) {
        throw new NotImplementedException("Method not implemented");
    }

    @Override
    public QueryCache<K, V> getQueryCache(String s, Predicate<K, V> predicate, boolean b) {
        throw new NotImplementedException("Method not implemented");
    }

    @Override
    public QueryCache<K, V> getQueryCache(String s, MapListener mapListener, Predicate<K, V> predicate, boolean b) {
        throw new NotImplementedException("Method not implemented");
    }

    @Override
    public boolean setTtl(K k, long l, TimeUnit timeUnit) {
        return false;
    }

    public Iterator<Entry<K, V>> iterator() {
        return super.entrySet().iterator();
    }

    public Iterator<Entry<K, V>> iterator(int i) {
        return super.entrySet().iterator();
    }

    @Override
    public String getPartitionKey() {
        throw new NotImplementedException("Method not implemented");
    }

    @Override
    public String getName() {
        return cacheName;
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public void destroy() {
        super.clear();
    }
}
