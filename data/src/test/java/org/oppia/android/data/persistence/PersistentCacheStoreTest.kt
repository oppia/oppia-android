package org.oppia.android.data.persistence

import android.app.Application
import android.content.Context
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.protobuf.MessageLite
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.reset
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyZeroInteractions
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.android.app.model.TestMessage
import org.oppia.android.testing.TestCoroutineDispatchers
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.threading.BackgroundDispatcher
import org.oppia.android.testing.TestCoroutineDispatchers
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val CACHE_NAME_1 = "test_cache_1"
private const val CACHE_NAME_2 = "test_cache_2"

/** Tests for [PersistentCacheStore]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = PersistentCacheStoreTest.TestApplication::class)
class PersistentCacheStoreTest {
  private companion object {
    private val TEST_MESSAGE_VERSION_1 = TestMessage.newBuilder().setVersion(1).build()
    private val TEST_MESSAGE_VERSION_2 = TestMessage.newBuilder().setVersion(2).build()
  }

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject
  lateinit var cacheFactory: PersistentCacheStore.Factory

  @Inject
  lateinit var dataProviders: DataProviders

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  @field:BackgroundDispatcher
  lateinit var backgroundDispatcher: CoroutineDispatcher

  @Mock
  lateinit var mockUserAppHistoryObserver1: Observer<AsyncResult<TestMessage>>

  @Mock
  lateinit var mockUserAppHistoryObserver2: Observer<AsyncResult<TestMessage>>

  @Captor
  lateinit var userAppHistoryResultCaptor1: ArgumentCaptor<AsyncResult<TestMessage>>

  @Captor
  lateinit var userAppHistoryResultCaptor2: ArgumentCaptor<AsyncResult<TestMessage>>

  private val backgroundDispatcherScope by lazy {
    CoroutineScope(backgroundDispatcher)
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  // TODO(#59): Create a test-only proto for this test rather than needing to reuse a production-facing proto.
  @Test
  fun testCache_toLiveData_initialState_isPending() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())

    // Directly call retrieveData() to get the very initial state of the provider. Relying on
    // traditional notification mechanisms like LiveData won't work because TestCoroutineDispatchers
    // will synchronize execution such that all operations (including the multiple async steps
    // needed to properly initialize the cache store) will complete without sending the pending
    // state, yet the pending state is really likely to be observed in production situations. The
    // timing model in tests is a bit too different from production to properly simulate this case.
    // This seems like a reasonable workaround to verify the same effective behavior.
    val deferredResult = backgroundDispatcherScope.async {
      cacheStore.retrieveData()
    }
    testCoroutineDispatchers.advanceUntilIdle()

    val result = deferredResult.getCompleted()
    assertThat(result.isPending()).isTrue()
  }

  @Test
  fun testCache_toLiveData_loaded_providesInitialValue() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())

    observeCache(cacheStore, mockUserAppHistoryObserver1)

    // The initial cache state should be the default cache value.
    verify(
      mockUserAppHistoryObserver1,
      atLeastOnce()
    ).onChanged(userAppHistoryResultCaptor1.capture())
    assertThat(userAppHistoryResultCaptor1.value.isSuccess()).isTrue()
    assertThat(userAppHistoryResultCaptor1.value.getOrThrow()).isEqualTo(
      TestMessage.getDefaultInstance()
    )
  }

  @Test
  fun testCache_nonDefaultInitialState_toLiveData_loaded_providesCorrectInitialVal() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, TEST_MESSAGE_VERSION_1)

    observeCache(cacheStore, mockUserAppHistoryObserver1)

    // Caches can have non-default initial states.
    verify(
      mockUserAppHistoryObserver1,
      atLeastOnce()
    ).onChanged(userAppHistoryResultCaptor1.capture())
    assertThat(userAppHistoryResultCaptor1.value.isSuccess()).isTrue()
    assertThat(userAppHistoryResultCaptor1.value.getOrThrow()).isEqualTo(TEST_MESSAGE_VERSION_1)
  }

  @Test
  fun testCache_registerObserver_updateAfter_observerNotifiedOfNewValue() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())

    observeCache(cacheStore, mockUserAppHistoryObserver1)
    reset(mockUserAppHistoryObserver1)
    val storeOp = cacheStore.storeDataAsync { TEST_MESSAGE_VERSION_1 }
    testCoroutineDispatchers.advanceUntilIdle()

    // The store operation should be completed, and the observer should be notified of the changed value.
    assertThat(storeOp.isCompleted).isTrue()
    verify(mockUserAppHistoryObserver1).onChanged(userAppHistoryResultCaptor1.capture())
    assertThat(userAppHistoryResultCaptor1.value.isSuccess()).isTrue()
    assertThat(userAppHistoryResultCaptor1.value.getOrThrow()).isEqualTo(TEST_MESSAGE_VERSION_1)
  }

  @Test
  fun testCache_registerObserver_updateBefore_observesUpdatedStateInitially() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())

    val storeOp = cacheStore.storeDataAsync { TEST_MESSAGE_VERSION_1 }
    testCoroutineDispatchers.advanceUntilIdle()
    observeCache(cacheStore, mockUserAppHistoryObserver1)

    // The store operation should be completed, and the observer's only call should be the updated state.
    assertThat(storeOp.isCompleted).isTrue()
    verify(mockUserAppHistoryObserver1).onChanged(userAppHistoryResultCaptor1.capture())
    assertThat(userAppHistoryResultCaptor1.value.isSuccess()).isTrue()
    assertThat(userAppHistoryResultCaptor1.value.getOrThrow()).isEqualTo(TEST_MESSAGE_VERSION_1)
  }

  @Test
  fun testCache_noMemoryCacheUpdate_updateAfterReg_observerNotNotified() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())

    observeCache(cacheStore, mockUserAppHistoryObserver1)
    reset(mockUserAppHistoryObserver1)
    val storeOp = cacheStore.storeDataAsync(updateInMemoryCache = false) { TEST_MESSAGE_VERSION_1 }
    testCoroutineDispatchers.advanceUntilIdle()

    // The store operation should be completed, but the observe will not be notified of changes since the in-memory
    // cache was not changed.
    assertThat(storeOp.isCompleted).isTrue()
    verifyZeroInteractions(mockUserAppHistoryObserver1)
  }

  @Test
  fun testCache_noMemoryCacheUpdate_updateBeforeReg_observesUpdatedState() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())

    val storeOp = cacheStore.storeDataAsync(updateInMemoryCache = false) { TEST_MESSAGE_VERSION_1 }
    testCoroutineDispatchers.advanceUntilIdle()
    observeCache(cacheStore, mockUserAppHistoryObserver1)

    // The store operation should be completed, but the observer will receive the updated state since the cache wasn't
    // primed and no previous observers initialized it.
    // NB: This may not be ideal behavior long-term; the store may need to be updated to be more resilient to these
    // types of scenarios.
    assertThat(storeOp.isCompleted).isTrue()
    verify(
      mockUserAppHistoryObserver1,
      atLeastOnce()
    ).onChanged(userAppHistoryResultCaptor1.capture())
    assertThat(userAppHistoryResultCaptor1.value.isSuccess()).isTrue()
    assertThat(userAppHistoryResultCaptor1.value.getOrThrow()).isEqualTo(TEST_MESSAGE_VERSION_1)
  }

  @Test
  fun testCache_updated_newCache_newObserver_observesNewValue() {
    val cacheStore1 = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    val storeOp = cacheStore1.storeDataAsync { TEST_MESSAGE_VERSION_1 }
    testCoroutineDispatchers.advanceUntilIdle()

    // Create a new cache with the same name.
    val cacheStore2 = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    observeCache(cacheStore2, mockUserAppHistoryObserver1)

    // The new cache should have the updated value since it points to the same file as the first cache. This is
    // simulating something closer to an app restart or non-UI Dagger component refresh since UI components should share
    // the same cache instance via an application-bound controller object.
    assertThat(storeOp.isCompleted).isTrue()
    verify(
      mockUserAppHistoryObserver1,
      atLeastOnce()
    ).onChanged(userAppHistoryResultCaptor1.capture())
    assertThat(userAppHistoryResultCaptor1.value.isSuccess()).isTrue()
    assertThat(userAppHistoryResultCaptor1.value.getOrThrow()).isEqualTo(TEST_MESSAGE_VERSION_1)
  }

  @Test
  fun testCache_updated_noInMemoryCacheUpdate_newCache_newObserver_observesNewVal() {
    val cacheStore1 = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    val storeOp = cacheStore1.storeDataAsync(updateInMemoryCache = false) { TEST_MESSAGE_VERSION_1 }
    testCoroutineDispatchers.advanceUntilIdle()

    // Create a new cache with the same name.
    val cacheStore2 = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    observeCache(cacheStore2, mockUserAppHistoryObserver1)

    // The new cache should have the updated value since it points to the same file as the first cache, even though the
    // update operation did not update the in-memory cache (the new cache has a separate in-memory cache). This is
    // simulating something closer to an app restart or non-UI Dagger component refresh since UI components should share
    // the same cache instance via an application-bound controller object.
    assertThat(storeOp.isCompleted).isTrue()
    verify(
      mockUserAppHistoryObserver1,
      atLeastOnce()
    ).onChanged(userAppHistoryResultCaptor1.capture())
    assertThat(userAppHistoryResultCaptor1.value.isSuccess()).isTrue()
    assertThat(userAppHistoryResultCaptor1.value.getOrThrow()).isEqualTo(TEST_MESSAGE_VERSION_1)
  }

  @Test
  fun testExistingDiskCache_newCacheObject_updateNoMemThenRead_receivesNewValue() {
    val cacheStore1 = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    val storeOp1 =
      cacheStore1.storeDataAsync(updateInMemoryCache = false) { TEST_MESSAGE_VERSION_1 }
    testCoroutineDispatchers.advanceUntilIdle()

    // Create a new cache with the same name and update it, then observe it.
    val cacheStore2 = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    val storeOp2 =
      cacheStore2.storeDataAsync(updateInMemoryCache = false) { TEST_MESSAGE_VERSION_2 }
    testCoroutineDispatchers.advanceUntilIdle()
    observeCache(cacheStore2, mockUserAppHistoryObserver1)

    // Both operations should be complete, and the observer will receive the latest value since the update was posted
    // before the read occurred.
    assertThat(storeOp1.isCompleted).isTrue()
    assertThat(storeOp2.isCompleted).isTrue()
    verify(
      mockUserAppHistoryObserver1,
      atLeastOnce()
    ).onChanged(userAppHistoryResultCaptor1.capture())
    assertThat(userAppHistoryResultCaptor1.value.isSuccess()).isTrue()
    assertThat(userAppHistoryResultCaptor1.value.getOrThrow()).isEqualTo(TEST_MESSAGE_VERSION_2)
  }

  @Test
  fun testExistingDiskCache_newObject_updateNoMemThenRead_primed_receivesPrevVal() {
    val cacheStore1 = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    val storeOp1 =
      cacheStore1.storeDataAsync(updateInMemoryCache = false) { TEST_MESSAGE_VERSION_1 }
    testCoroutineDispatchers.advanceUntilIdle()

    // Create a new cache with the same name and update it, then observe it. However, first prime it.
    val cacheStore2 = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    val primeOp = cacheStore2.primeCacheAsync()
    testCoroutineDispatchers.advanceUntilIdle()
    val storeOp2 =
      cacheStore2.storeDataAsync(updateInMemoryCache = false) { TEST_MESSAGE_VERSION_2 }
    testCoroutineDispatchers.advanceUntilIdle()
    observeCache(cacheStore2, mockUserAppHistoryObserver1)

    // All operations should be complete, but the observer will receive the previous update rather than th elatest since
    // it wasn't updated in memory and the cache was pre-primed.
    assertThat(storeOp1.isCompleted).isTrue()
    assertThat(storeOp2.isCompleted).isTrue()
    assertThat(primeOp.isCompleted).isTrue()
    verify(
      mockUserAppHistoryObserver1,
      atLeastOnce()
    ).onChanged(userAppHistoryResultCaptor1.capture())
    assertThat(userAppHistoryResultCaptor1.value.isSuccess()).isTrue()
    assertThat(userAppHistoryResultCaptor1.value.getOrThrow()).isEqualTo(TEST_MESSAGE_VERSION_1)
  }

  @Test
  fun testExistingDiskCache_newObject_updateMemThenRead_primed_receivesNewVal() {
    val cacheStore1 = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    val storeOp1 =
      cacheStore1.storeDataAsync(updateInMemoryCache = false) { TEST_MESSAGE_VERSION_1 }
    testCoroutineDispatchers.advanceUntilIdle()

    // Create a new cache with the same name and update it, then observe it. However, first prime it.
    val cacheStore2 = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    val primeOp = cacheStore2.primeCacheAsync()
    testCoroutineDispatchers.advanceUntilIdle()
    val storeOp2 = cacheStore2.storeDataAsync { TEST_MESSAGE_VERSION_2 }
    testCoroutineDispatchers.advanceUntilIdle()
    observeCache(cacheStore2, mockUserAppHistoryObserver1)

    // Similar to the previous test, except due to the in-memory update the observer will receive the latest result
    // regardless of the cache priming.
    assertThat(storeOp1.isCompleted).isTrue()
    assertThat(storeOp2.isCompleted).isTrue()
    assertThat(primeOp.isCompleted).isTrue()
    verify(
      mockUserAppHistoryObserver1,
      atLeastOnce()
    ).onChanged(userAppHistoryResultCaptor1.capture())
    assertThat(userAppHistoryResultCaptor1.value.isSuccess()).isTrue()
    assertThat(userAppHistoryResultCaptor1.value.getOrThrow()).isEqualTo(TEST_MESSAGE_VERSION_2)
  }

  @Test
  fun testCache_primed_afterStoreUpdateWithoutMemUpdate_notForced_observesOldValue() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    observeCache(
      cacheStore,
      mockUserAppHistoryObserver1
    ) // Force initializing the store's in-memory cache

    val storeOp = cacheStore.storeDataAsync(updateInMemoryCache = false) { TEST_MESSAGE_VERSION_1 }
    testCoroutineDispatchers.advanceUntilIdle()
    val primeOp = cacheStore.primeCacheAsync(forceUpdate = false)
    testCoroutineDispatchers.advanceUntilIdle()
    observeCache(cacheStore, mockUserAppHistoryObserver2)

    // Both ops will succeed, and the observer will receive the old value due to the update not changing the in-memory
    // cache, and the prime no-oping due to the cache already being initialized.
    assertThat(storeOp.isCompleted).isTrue()
    assertThat(primeOp.isCompleted).isTrue()
    verify(
      mockUserAppHistoryObserver2,
      atLeastOnce()
    ).onChanged(userAppHistoryResultCaptor1.capture())
    assertThat(userAppHistoryResultCaptor1.value.isSuccess()).isTrue()
    assertThat(userAppHistoryResultCaptor1.value.getOrThrow()).isEqualTo(
      TestMessage.getDefaultInstance()
    )
  }

  @Test
  fun testCache_primed_afterStoreUpdateWithoutMemoryUpdate_forced_observesNewValue() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    observeCache(
      cacheStore,
      mockUserAppHistoryObserver1
    ) // Force initializing the store's in-memory cache

    val storeOp = cacheStore.storeDataAsync(updateInMemoryCache = false) { TEST_MESSAGE_VERSION_1 }
    testCoroutineDispatchers.advanceUntilIdle()
    val primeOp = cacheStore.primeCacheAsync(forceUpdate = true)
    testCoroutineDispatchers.advanceUntilIdle()
    observeCache(cacheStore, mockUserAppHistoryObserver2)

    // The observer will receive the new value because the prime was forced. This ensures the store's in-memory cache is
    // now up-to-date with the on-disk representation.
    assertThat(storeOp.isCompleted).isTrue()
    assertThat(primeOp.isCompleted).isTrue()
    verify(
      mockUserAppHistoryObserver2,
      atLeastOnce()
    ).onChanged(userAppHistoryResultCaptor1.capture())
    assertThat(userAppHistoryResultCaptor1.value.isSuccess()).isTrue()
    assertThat(userAppHistoryResultCaptor1.value.getOrThrow()).isEqualTo(TEST_MESSAGE_VERSION_1)
  }

  @Test
  fun testCache_clear_initialState_keepsCacheStateTheSame() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())

    val clearOp = cacheStore.clearCacheAsync()
    testCoroutineDispatchers.advanceUntilIdle()
    observeCache(cacheStore, mockUserAppHistoryObserver1)

    // The new observer should observe the store with its default state since nothing needed to be cleared.
    assertThat(clearOp.isCompleted).isTrue()
    verify(
      mockUserAppHistoryObserver1,
      atLeastOnce()
    ).onChanged(userAppHistoryResultCaptor1.capture())
    assertThat(userAppHistoryResultCaptor1.value.isSuccess()).isTrue()
    assertThat(userAppHistoryResultCaptor1.value.getOrThrow()).isEqualTo(
      TestMessage.getDefaultInstance()
    )
  }

  @Test
  fun testCache_update_clear_resetsCacheToInitialState() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    val storeOp = cacheStore.storeDataAsync { TEST_MESSAGE_VERSION_1 }
    testCoroutineDispatchers.advanceUntilIdle()

    val clearOp = cacheStore.clearCacheAsync()
    testCoroutineDispatchers.advanceUntilIdle()
    observeCache(cacheStore, mockUserAppHistoryObserver1)

    // The new observer should observe that the store is cleared.
    assertThat(storeOp.isCompleted).isTrue()
    assertThat(clearOp.isCompleted).isTrue()
    verify(
      mockUserAppHistoryObserver1,
      atLeastOnce()
    ).onChanged(userAppHistoryResultCaptor1.capture())
    assertThat(userAppHistoryResultCaptor1.value.isSuccess()).isTrue()
    assertThat(userAppHistoryResultCaptor1.value.getOrThrow()).isEqualTo(
      TestMessage.getDefaultInstance()
    )
  }

  @Test
  fun testCache_update_existingObserver_clear_isNotifiedOfClear() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    val storeOp = cacheStore.storeDataAsync { TEST_MESSAGE_VERSION_1 }
    testCoroutineDispatchers.advanceUntilIdle()

    observeCache(cacheStore, mockUserAppHistoryObserver1)
    reset(mockUserAppHistoryObserver1)
    val clearOp = cacheStore.clearCacheAsync()
    testCoroutineDispatchers.advanceUntilIdle()

    // The observer should not be notified the cache was cleared.
    assertThat(storeOp.isCompleted).isTrue()
    assertThat(clearOp.isCompleted).isTrue()
    verify(
      mockUserAppHistoryObserver1,
      atLeastOnce()
    ).onChanged(userAppHistoryResultCaptor1.capture())
    assertThat(userAppHistoryResultCaptor1.value.isSuccess()).isTrue()
    assertThat(userAppHistoryResultCaptor1.value.getOrThrow()).isEqualTo(
      TestMessage.getDefaultInstance()
    )
  }

  @Test
  fun testCache_update_newCache_observesInitialState() {
    val cacheStore1 = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    val storeOp = cacheStore1.storeDataAsync { TEST_MESSAGE_VERSION_1 }
    testCoroutineDispatchers.advanceUntilIdle()
    val clearOp = cacheStore1.clearCacheAsync()
    testCoroutineDispatchers.advanceUntilIdle()

    val cacheStore2 = cacheFactory.create(CACHE_NAME_1, TEST_MESSAGE_VERSION_2)
    observeCache(cacheStore2, mockUserAppHistoryObserver1)

    // The new observer should observe that there's no persisted on-disk store since it has a different default value
    // that would only be used if there wasn't already on-disk storage.
    assertThat(storeOp.isCompleted).isTrue()
    assertThat(clearOp.isCompleted).isTrue()
    verify(
      mockUserAppHistoryObserver1,
      atLeastOnce()
    ).onChanged(userAppHistoryResultCaptor1.capture())
    assertThat(userAppHistoryResultCaptor1.value.isSuccess()).isTrue()
    assertThat(userAppHistoryResultCaptor1.value.getOrThrow()).isEqualTo(TEST_MESSAGE_VERSION_2)
  }

  @Test
  fun testMultipleCaches_oneUpdates_newCacheSameNameDiffInit_observesUpdatedValue() {
    val cacheStore1 = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    val storeOp = cacheStore1.storeDataAsync { TEST_MESSAGE_VERSION_1 }
    testCoroutineDispatchers.advanceUntilIdle()

    val cacheStore2 = cacheFactory.create(CACHE_NAME_1, TEST_MESSAGE_VERSION_2)
    observeCache(cacheStore2, mockUserAppHistoryObserver1)

    // The new cache should observe the updated on-disk value rather than its new default since an on-disk value exists.
    // This isn't a very realistic test since all caches should use default proto instances for initialization, but it's
    // a possible edge case that should at least have established behavior that can be adjusted later if it isn't
    // desirable in some circumstances.
    assertThat(storeOp.isCompleted).isTrue()
    verify(
      mockUserAppHistoryObserver1,
      atLeastOnce()
    ).onChanged(userAppHistoryResultCaptor1.capture())
    assertThat(userAppHistoryResultCaptor1.value.isSuccess()).isTrue()
    assertThat(userAppHistoryResultCaptor1.value.getOrThrow()).isEqualTo(TEST_MESSAGE_VERSION_1)
  }

  @Test
  fun testMultipleCaches_differentNames_oneUpdates_otherDoesNotObserveChange() {
    val cacheStore1 = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    val cacheStore2 = cacheFactory.create(CACHE_NAME_2, TestMessage.getDefaultInstance())

    observeCache(cacheStore1, mockUserAppHistoryObserver1)
    observeCache(cacheStore2, mockUserAppHistoryObserver2)
    reset(mockUserAppHistoryObserver2)
    val storeOp = cacheStore1.storeDataAsync { TEST_MESSAGE_VERSION_1 }
    testCoroutineDispatchers.advanceUntilIdle()

    // The observer of the second store will be not notified of the change to the first store since they have different
    // names.
    assertThat(storeOp.isCompleted).isTrue()
    verifyZeroInteractions(mockUserAppHistoryObserver2)
  }

  @Test
  fun testMultipleCaches_diffNames_oneUpdates_cachesRecreated_onlyOneObservesVal() {
    val cacheStore1a = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    cacheFactory.create(CACHE_NAME_2, TestMessage.getDefaultInstance())
    val storeOp = cacheStore1a.storeDataAsync { TEST_MESSAGE_VERSION_1 }
    testCoroutineDispatchers.advanceUntilIdle()

    // Recreate the stores and observe them.
    val cacheStore1b = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    val cacheStore2b = cacheFactory.create(CACHE_NAME_2, TestMessage.getDefaultInstance())
    observeCache(cacheStore1b, mockUserAppHistoryObserver1)
    observeCache(cacheStore2b, mockUserAppHistoryObserver2)

    // Only the observer of the first cache should notice the update since they are different caches.
    assertThat(storeOp.isCompleted).isTrue()
    verify(
      mockUserAppHistoryObserver1,
      atLeastOnce()
    ).onChanged(userAppHistoryResultCaptor1.capture())
    verify(
      mockUserAppHistoryObserver2,
      atLeastOnce()
    ).onChanged(userAppHistoryResultCaptor2.capture())
    assertThat(userAppHistoryResultCaptor1.value.isSuccess()).isTrue()
    assertThat(userAppHistoryResultCaptor1.value.getOrThrow()).isEqualTo(TEST_MESSAGE_VERSION_1)
    assertThat(userAppHistoryResultCaptor2.value.isSuccess()).isTrue()
    assertThat(userAppHistoryResultCaptor2.value.getOrThrow()).isEqualTo(
      TestMessage.getDefaultInstance()
    )
  }

  @Test
  fun testNewCache_fileCorrupted_providesError() {
    val cacheStore1 = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    val storeOp = cacheStore1.storeDataAsync { TEST_MESSAGE_VERSION_1 }
    testCoroutineDispatchers.advanceUntilIdle()

    // Simulate the file being corrupted & reopen the file in a new store.
    corruptFileCache(CACHE_NAME_1)
    val cacheStore2 = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    observeCache(cacheStore2, mockUserAppHistoryObserver1)

    // The new observer should receive an IOException error when trying to read the file.
    assertThat(storeOp.isCompleted).isTrue()
    verify(
      mockUserAppHistoryObserver1,
      atLeastOnce()
    ).onChanged(userAppHistoryResultCaptor1.capture())
    assertThat(userAppHistoryResultCaptor1.value.isFailure()).isTrue()
    assertThat(userAppHistoryResultCaptor1.value.getErrorOrNull()).isInstanceOf(
      IOException::class.java
    )
  }

  private fun <T : MessageLite> observeCache(
    cacheStore: PersistentCacheStore<T>,
    observer: Observer<AsyncResult<T>>
  ) {
    cacheStore.toLiveData().observeForever(observer)
    testCoroutineDispatchers.advanceUntilIdle()
  }

  private fun corruptFileCache(cacheName: String) {
    // NB: This is unfortunately tied to the implementation details of PersistentCacheStore. If this ends up being an
    // issue, the store should be updated to call into a file path provider that can also be used in this test to
    // retrieve the file cache. This may also be needed for downstream profile work if per-profile data stores are done
    // via subdirectories or altered filenames.
    val cacheFileName = "$cacheName.cache"
    val cacheFile = File(
      ApplicationProvider.getApplicationContext<Context>().filesDir, cacheFileName
    )
    FileOutputStream(cacheFile).use {
      it.write(byteArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9))
    }
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestDispatcherModule::class,
      TestModule::class,
      TestLogReportingModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(persistentCacheStoreTest: PersistentCacheStoreTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerPersistentCacheStoreTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(persistentCacheStoreTest: PersistentCacheStoreTest) {
      component.inject(persistentCacheStoreTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
