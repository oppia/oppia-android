package org.oppia.data.persistence;

import java.lang.System;

/**
 * An on-disk persistent cache for proto messages that ensures reads and writes happen in a well-defined order. Note
 * that if this cache is used like a [DataProvider], there is a race condition between the initial store's data being
 * retrieved and any early writes to the store (writes generally win). If this is not ideal, callers should use
 * [primeCacheAsync] to synchronously kick-off a read update to the store that is guaranteed to complete before any writes.
 * This will be reflected in the first time the store's state is delivered to a subscriber to a LiveData version of this
 * data provider.
 *
 * Note that this is a fast-response data provider, meaning it will provide a pending [AsyncResult] to subscribers
 * immediately until the actual store is retrieved from disk.
 */
@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000x\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0003\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0006\u0018\u0000*\b\b\u0000\u0010\u0001*\u00020\u00022\b\u0012\u0004\u0012\u0002H\u00010\u0003:\u0004./01B/\b\u0002\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\u000b\u0012\u0006\u0010\f\u001a\u00028\u0000\u00a2\u0006\u0002\u0010\rJ\f\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u001d0\u001cJ\b\u0010\u001e\u001a\u00020\u001fH\u0002J\b\u0010 \u001a\u00020\u001dH\u0016J\u001c\u0010!\u001a\b\u0012\u0004\u0012\u00028\u00000\u00102\f\u0010\"\u001a\b\u0012\u0004\u0012\u00028\u00000\u0010H\u0002J\u0016\u0010#\u001a\b\u0012\u0004\u0012\u00020\u001d0\u001c2\b\b\u0002\u0010$\u001a\u00020%J\u0017\u0010&\u001a\b\u0012\u0004\u0012\u00028\u00000\'H\u0096@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010(J*\u0010)\u001a\b\u0012\u0004\u0012\u00020\u001d0\u001c2\b\b\u0002\u0010*\u001a\u00020%2\u0012\u0010+\u001a\u000e\u0012\u0004\u0012\u00028\u0000\u0012\u0004\u0012\u00028\u00000,J0\u0010-\u001a\b\u0012\u0004\u0012\u00028\u00000\u00102\f\u0010\"\u001a\b\u0012\u0004\u0012\u00028\u00000\u00102\u0012\u0010+\u001a\u000e\u0012\u0004\u0012\u00028\u0000\u0012\u0004\u0012\u00028\u00000,H\u0002R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u000e\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00028\u00000\u00100\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0014\u001a\u0004\u0018\u00010\u00158\u0002@\u0002X\u0083\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0016\u001a\u00020\u0017X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\f\u001a\u00028\u0000X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0018R\u000e\u0010\u0019\u001a\u00020\u001aX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u00062"}, d2 = {"Lorg/oppia/data/persistence/PersistentCacheStore;", "T", "Lcom/google/protobuf/MessageLite;", "Lorg/oppia/util/data/DataProvider;", "context", "Landroid/content/Context;", "cacheFactory", "Lorg/oppia/util/data/InMemoryBlockingCache$Factory;", "asyncDataSubscriptionManager", "Lorg/oppia/util/data/AsyncDataSubscriptionManager;", "cacheName", "", "initialValue", "(Landroid/content/Context;Lorg/oppia/util/data/InMemoryBlockingCache$Factory;Lorg/oppia/util/data/AsyncDataSubscriptionManager;Ljava/lang/String;Lcom/google/protobuf/MessageLite;)V", "cache", "Lorg/oppia/util/data/InMemoryBlockingCache;", "Lorg/oppia/data/persistence/PersistentCacheStore$CachePayload;", "cacheFile", "Ljava/io/File;", "cacheFileName", "deferredLoadCacheFailure", "", "failureLock", "Ljava/util/concurrent/locks/ReentrantLock;", "Lcom/google/protobuf/MessageLite;", "providerId", "Lorg/oppia/data/persistence/PersistentCacheStore$PersistentCacheStoreId;", "clearCacheAsync", "Lkotlinx/coroutines/Deferred;", "", "deferLoadFileAndNotify", "", "getId", "loadFileCache", "currentPayload", "primeCacheAsync", "forceUpdate", "", "retrieveData", "Lorg/oppia/util/data/AsyncResult;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "storeDataAsync", "updateInMemoryCache", "update", "Lkotlin/Function1;", "storeFileCache", "CachePayload", "CacheState", "Factory", "PersistentCacheStoreId", "data_debug"})
public final class PersistentCacheStore<T extends com.google.protobuf.MessageLite> implements org.oppia.util.data.DataProvider<T> {
    private final java.lang.String cacheFileName = null;
    private final org.oppia.data.persistence.PersistentCacheStore.PersistentCacheStoreId providerId = null;
    private final java.util.concurrent.locks.ReentrantLock failureLock = null;
    private final java.io.File cacheFile = null;
    @androidx.annotation.GuardedBy(value = "failureLock")
    private java.lang.Throwable deferredLoadCacheFailure;
    private final org.oppia.util.data.InMemoryBlockingCache<org.oppia.data.persistence.PersistentCacheStore.CachePayload<T>> cache = null;
    private final org.oppia.util.data.AsyncDataSubscriptionManager asyncDataSubscriptionManager = null;
    private final T initialValue = null;
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public java.lang.Object getId() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    @java.lang.Override()
    public java.lang.Object retrieveData(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super org.oppia.util.data.AsyncResult<T>> p0) {
        return null;
    }
    
    /**
     * Kicks off a read operation to update the in-memory cache. This operation blocks against calls to [storeDataAsync]
     * and deferred calls to [retrieveData].
     *
     * @param forceUpdate indicates whether to force a reset of the in-memory cache. Note that this only forces a load; if
     *    the load fails then the store will remain in its same state. If this value is false (the default), it will only
     *    perform file I/O if the cache is not already loaded into memory.
     * @returns a [Deferred] that completes upon the completion of priming the cache, or failure to do so with the failed
     *    exception. Note that the failure reason will not be propagated to a LiveData-converted version of this data
     *    provider, so it must be handled at the callsite for this method.
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Deferred<java.lang.Object> primeCacheAsync(boolean forceUpdate) {
        return null;
    }
    
    /**
     * Calls the specified value with the current on-disk contents and saves the result of the function to disk. Note that
     * the function used here should be non-blocking, thread-safe, and should have no side effects.
     *
     * @param updateInMemoryCache indicates whether this change to the on-disk store should also update the in-memory
     *    store, and propagate that change to all subscribers to this data provider. This may be ideal if callers want to
     *    control "snapshots" of the store that subscribers have access to, however it's recommended to keep all store
     *    calls consistent in whether they update the in-memory cache to avoid complex potential in-memory/on-disk sync
     *    issues.
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Deferred<java.lang.Object> storeDataAsync(boolean updateInMemoryCache, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super T, ? extends T> update) {
        return null;
    }
    
    /**
     * Returns a [Deferred] indicating when the cache was cleared and its on-disk file, removed. This does not notify
     * subscribers.
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Deferred<java.lang.Object> clearCacheAsync() {
        return null;
    }
    
    private final void deferLoadFileAndNotify() {
    }
    
    /**
     * Loads the file store from disk, and returns the most up-to-date cache payload. This should only be called from the
     * cache's update thread.
     */
    @kotlin.Suppress(names = {"UNCHECKED_CAST"})
    private final org.oppia.data.persistence.PersistentCacheStore.CachePayload<T> loadFileCache(org.oppia.data.persistence.PersistentCacheStore.CachePayload<T> currentPayload) {
        return null;
    }
    
    /**
     * Stores the file store to disk, and returns the persisted payload. This should only be called from the cache's
     * update thread.
     */
    private final org.oppia.data.persistence.PersistentCacheStore.CachePayload<T> storeFileCache(org.oppia.data.persistence.PersistentCacheStore.CachePayload<T> currentPayload, kotlin.jvm.functions.Function1<? super T, ? extends T> update) {
        return null;
    }
    
    private PersistentCacheStore(android.content.Context context, org.oppia.util.data.InMemoryBlockingCache.Factory cacheFactory, org.oppia.util.data.AsyncDataSubscriptionManager asyncDataSubscriptionManager, java.lang.String cacheName, T initialValue) {
        super();
    }
    
    @kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0082\b\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\t\u0010\u0005\u001a\u00020\u0003H\u00c2\u0003J\u0013\u0010\u0006\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\u0007\u001a\u00020\b2\b\u0010\t\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\n\u001a\u00020\u000bH\u00d6\u0001J\t\u0010\f\u001a\u00020\u0003H\u00d6\u0001R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\r"}, d2 = {"Lorg/oppia/data/persistence/PersistentCacheStore$PersistentCacheStoreId;", "", "id", "", "(Ljava/lang/String;)V", "component1", "copy", "equals", "", "other", "hashCode", "", "toString", "data_debug"})
    static final class PersistentCacheStoreId {
        private final java.lang.String id = null;
        
        public PersistentCacheStoreId(@org.jetbrains.annotations.NotNull()
        java.lang.String id) {
            super();
        }
        
        private final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final org.oppia.data.persistence.PersistentCacheStore.PersistentCacheStoreId copy(@org.jetbrains.annotations.NotNull()
        java.lang.String id) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        @java.lang.Override()
        public java.lang.String toString() {
            return null;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object p0) {
            return false;
        }
    }
    
    /**
     * Represents different states the cache store can be in.
     */
    @kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0005\b\u0082\u0001\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005\u00a8\u0006\u0006"}, d2 = {"Lorg/oppia/data/persistence/PersistentCacheStore$CacheState;", "", "(Ljava/lang/String;I)V", "UNLOADED", "IN_MEMORY_ONLY", "IN_MEMORY_AND_ON_DISK", "data_debug"})
    static enum CacheState {
        /*public static final*/ UNLOADED /* = new UNLOADED() */,
        /*public static final*/ IN_MEMORY_ONLY /* = new IN_MEMORY_ONLY() */,
        /*public static final*/ IN_MEMORY_AND_ON_DISK /* = new IN_MEMORY_AND_ON_DISK() */;
        
        CacheState() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\f\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\b\u0082\b\u0018\u0000*\u0004\b\u0001\u0010\u00012\u00020\u0002B\u0015\u0012\u0006\u0010\u0003\u001a\u00020\u0004\u0012\u0006\u0010\u0005\u001a\u00028\u0001\u00a2\u0006\u0002\u0010\u0006J\t\u0010\f\u001a\u00020\u0004H\u00c6\u0003J\u000e\u0010\r\u001a\u00028\u0001H\u00c6\u0003\u00a2\u0006\u0002\u0010\nJ(\u0010\u000e\u001a\b\u0012\u0004\u0012\u00028\u00010\u00002\b\b\u0002\u0010\u0003\u001a\u00020\u00042\b\b\u0002\u0010\u0005\u001a\u00028\u0001H\u00c6\u0001\u00a2\u0006\u0002\u0010\u000fJ\u0013\u0010\u0010\u001a\u00020\u00112\b\u0010\u0012\u001a\u0004\u0018\u00010\u0002H\u00d6\u0003J\t\u0010\u0013\u001a\u00020\u0014H\u00d6\u0001J\u0014\u0010\u0015\u001a\b\u0012\u0004\u0012\u00028\u00010\u00002\u0006\u0010\u0016\u001a\u00020\u0004J\t\u0010\u0017\u001a\u00020\u0018H\u00d6\u0001R\u0011\u0010\u0003\u001a\u00020\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\bR\u0013\u0010\u0005\u001a\u00028\u0001\u00a2\u0006\n\n\u0002\u0010\u000b\u001a\u0004\b\t\u0010\n\u00a8\u0006\u0019"}, d2 = {"Lorg/oppia/data/persistence/PersistentCacheStore$CachePayload;", "T", "", "state", "Lorg/oppia/data/persistence/PersistentCacheStore$CacheState;", "value", "(Lorg/oppia/data/persistence/PersistentCacheStore$CacheState;Ljava/lang/Object;)V", "getState", "()Lorg/oppia/data/persistence/PersistentCacheStore$CacheState;", "getValue", "()Ljava/lang/Object;", "Ljava/lang/Object;", "component1", "component2", "copy", "(Lorg/oppia/data/persistence/PersistentCacheStore$CacheState;Ljava/lang/Object;)Lorg/oppia/data/persistence/PersistentCacheStore$CachePayload;", "equals", "", "other", "hashCode", "", "moveToState", "newState", "toString", "", "data_debug"})
    static final class CachePayload<T extends java.lang.Object> {
        @org.jetbrains.annotations.NotNull()
        private final org.oppia.data.persistence.PersistentCacheStore.CacheState state = null;
        private final T value = null;
        
        /**
         * Returns a copy of this payload with the new, specified [CacheState].
         */
        @org.jetbrains.annotations.NotNull()
        public final org.oppia.data.persistence.PersistentCacheStore.CachePayload<T> moveToState(@org.jetbrains.annotations.NotNull()
        org.oppia.data.persistence.PersistentCacheStore.CacheState newState) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final org.oppia.data.persistence.PersistentCacheStore.CacheState getState() {
            return null;
        }
        
        public final T getValue() {
            return null;
        }
        
        public CachePayload(@org.jetbrains.annotations.NotNull()
        org.oppia.data.persistence.PersistentCacheStore.CacheState state, T value) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final org.oppia.data.persistence.PersistentCacheStore.CacheState component1() {
            return null;
        }
        
        public final T component2() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final org.oppia.data.persistence.PersistentCacheStore.CachePayload<T> copy(@org.jetbrains.annotations.NotNull()
        org.oppia.data.persistence.PersistentCacheStore.CacheState state, T value) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        @java.lang.Override()
        public java.lang.String toString() {
            return null;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object p0) {
            return false;
        }
    }
    
    /**
     * An injectable factory for [PersistentCacheStore]s. The stores themselves should be retrievable from central
     * controllers since they can't be placed directly in the Dagger graph.
     */
    @kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001B\u001f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ+\u0010\t\u001a\b\u0012\u0004\u0012\u0002H\u000b0\n\"\b\b\u0001\u0010\u000b*\u00020\f2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u0002H\u000b\u00a2\u0006\u0002\u0010\u0010R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0011"}, d2 = {"Lorg/oppia/data/persistence/PersistentCacheStore$Factory;", "", "context", "Landroid/content/Context;", "cacheFactory", "Lorg/oppia/util/data/InMemoryBlockingCache$Factory;", "asyncDataSubscriptionManager", "Lorg/oppia/util/data/AsyncDataSubscriptionManager;", "(Landroid/content/Context;Lorg/oppia/util/data/InMemoryBlockingCache$Factory;Lorg/oppia/util/data/AsyncDataSubscriptionManager;)V", "create", "Lorg/oppia/data/persistence/PersistentCacheStore;", "T", "Lcom/google/protobuf/MessageLite;", "cacheName", "", "initialValue", "(Ljava/lang/String;Lcom/google/protobuf/MessageLite;)Lorg/oppia/data/persistence/PersistentCacheStore;", "data_debug"})
    @javax.inject.Singleton()
    public static final class Factory {
        private final android.content.Context context = null;
        private final org.oppia.util.data.InMemoryBlockingCache.Factory cacheFactory = null;
        private final org.oppia.util.data.AsyncDataSubscriptionManager asyncDataSubscriptionManager = null;
        
        /**
         * Returns a new [PersistentCacheStore] with the specified cache name and initial value.
         */
        @org.jetbrains.annotations.NotNull()
        public final <T extends com.google.protobuf.MessageLite>org.oppia.data.persistence.PersistentCacheStore<T> create(@org.jetbrains.annotations.NotNull()
        java.lang.String cacheName, @org.jetbrains.annotations.NotNull()
        T initialValue) {
            return null;
        }
        
        @javax.inject.Inject()
        public Factory(@org.jetbrains.annotations.NotNull()
        android.content.Context context, @org.jetbrains.annotations.NotNull()
        org.oppia.util.data.InMemoryBlockingCache.Factory cacheFactory, @org.jetbrains.annotations.NotNull()
        org.oppia.util.data.AsyncDataSubscriptionManager asyncDataSubscriptionManager) {
            super();
        }
    }
}