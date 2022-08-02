package org.oppia.android.data.persistence

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.extensions.proto.LiteProtoTruth.assertThat
import com.google.protobuf.MessageLite
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.TestMessage
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.AsyncResultSubject.Companion.assertThat
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.threading.BackgroundDispatcher
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.lang.IllegalStateException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.full.staticFunctions
import org.junit.Rule
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.android.data.persistence.PersistentCacheStore.PublishMode.DO_NOT_PUBLISH_TO_IN_MEMORY_CACHE
import org.oppia.android.data.persistence.PersistentCacheStore.PublishMode.PUBLISH_TO_IN_MEMORY_CACHE
import org.oppia.android.data.persistence.PersistentCacheStore.UpdateMode.UPDATE_ALWAYS
import org.oppia.android.data.persistence.PersistentCacheStore.UpdateMode.UPDATE_IF_NEW_CACHE
import org.oppia.android.data.persistence.PersistentCacheStoreTest.SubscriptionCallback.Companion.toAsyncChange
import org.oppia.android.util.data.AsyncDataSubscriptionManager
import org.oppia.android.util.data.ObserveAsyncChange

private const val CACHE_NAME_1 = "test_cache_1"
private const val CACHE_NAME_2 = "test_cache_2"

/** Tests for [PersistentCacheStore]. */
// Same parameter value: helpers reduce test context, even if they are used by 1 test.
// Function name: test names are conventionally named with underscores.
@Suppress("SameParameterValue", "FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = PersistentCacheStoreTest.TestApplication::class)
class PersistentCacheStoreTest {
  private companion object {
    private const val TEST_INT_V1 = 1
    private const val TEST_INT_V2 = 2
    private const val TEST_STR_V1 = "test string"
    private val DEFAULT_TEST_MESSAGE = TestMessage.getDefaultInstance()
    private val TEST_INT_MESSAGE_V1 = createTestMessage(intValue = TEST_INT_V1)
    private val TEST_INT_MESSAGE_V2 = createTestMessage(intValue = TEST_INT_V2)

    private fun TestMessage.addString(strValue: String) = toBuilder().apply {
      this.strValue = strValue
    }.build()

    private fun createTestMessage(
      intValue: Int = DEFAULT_TEST_MESSAGE.intValue,
      strValue: String = DEFAULT_TEST_MESSAGE.strValue
    ) = TestMessage.newBuilder().apply {
      this.intValue = intValue
      this.strValue = strValue
    }.build()
  }

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject lateinit var context: Context
  @Inject lateinit var cacheFactory: PersistentCacheStore.Factory
  @Inject lateinit var dataProviders: DataProviders
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @field:[Inject BackgroundDispatcher] lateinit var backgroundDispatcher: CoroutineDispatcher
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory
  @Inject lateinit var asyncDataSubscriptionManager: AsyncDataSubscriptionManager
  @Mock lateinit var mockSubscriptionCallback: SubscriptionCallback

  private val backgroundDispatcherScope by lazy { CoroutineScope(backgroundDispatcher) }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  // TODO(#59): Create a test-only proto for this test rather than needing to reuse a
  //  production-facing proto.
  @Test
  @ExperimentalCoroutinesApi
  fun testCache_initialState_isPending() {
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

    assertThat(deferredResult.getCompleted()).isPending()
  }

  @Test
  fun testCache_loaded_providesInitialValue() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())

    val value = monitorFactory.waitForNextSuccessfulResult(cacheStore)

    // The initial cache state should be the default cache value.
    assertThat(value).isEqualToDefaultInstance()
  }

  @Test
  fun testCache_nonDefaultInitialState_loaded_providesCorrectInitialVal() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, TEST_INT_MESSAGE_V1)

    val value = monitorFactory.waitForNextSuccessfulResult(cacheStore)

    // Caches can have non-default initial states.
    assertThat(value).isEqualTo(TEST_INT_MESSAGE_V1)
  }

  @Test
  fun testCache_registerObserver_updateAfter_observerNotifiedOfNewValue() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())

    monitorFactory.waitForNextSuccessfulResult(cacheStore)
    val storeOp = cacheStore.storeDataAsync { TEST_INT_MESSAGE_V1 }
    testCoroutineDispatchers.advanceUntilIdle()

    // The store operation should be completed, and the observer should be notified of the changed
    // value.
    assertThat(storeOp.isCompleted).isTrue()
    assertThat(monitorFactory.waitForNextSuccessfulResult(cacheStore)).isEqualTo(TEST_INT_MESSAGE_V1)
  }

  @Test
  fun testCache_registerObserver_updateBefore_observesUpdatedStateInitially() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())

    val storeOp = cacheStore.storeDataAsync { TEST_INT_MESSAGE_V1 }
    testCoroutineDispatchers.advanceUntilIdle()

    // The store operation should be completed, and the observer's only call should be the updated
    // state.
    val value = monitorFactory.waitForNextSuccessfulResult(cacheStore)
    assertThat(storeOp.isCompleted).isTrue()
    assertThat(value).isEqualTo(TEST_INT_MESSAGE_V1)
  }

  @Test
  fun testCache_noMemoryCacheUpdate_updateAfterReg_observerNotNotified() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    val monitor = monitorFactory.createMonitor(cacheStore)

    monitor.waitForNextSuccessResult()
    val storeOp = cacheStore.storeDataAsync(updateInMemoryCache = false) { TEST_INT_MESSAGE_V1 }
    testCoroutineDispatchers.advanceUntilIdle()

    // The store operation should be completed, but the observe will not be notified of changes
    // since the in-memory cache was not changed.
    assertThat(storeOp.isCompleted).isTrue()
    monitor.verifyProviderIsNotUpdated()
  }

  @Test
  fun testCache_noMemoryCacheUpdate_updateBeforeReg_observesUpdatedState() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())

    val storeOp = cacheStore.storeDataAsync(updateInMemoryCache = false) { TEST_INT_MESSAGE_V1 }
    testCoroutineDispatchers.advanceUntilIdle()

    // The store operation should be completed, but the observer will receive the updated state
    // since the cache wasn't primed and no previous observers initialized it.
    // NB: This may not be ideal behavior long-term; the store may need to be updated to be more
    // resilient to these types of scenarios.
    assertThat(storeOp.isCompleted).isTrue()
    assertThat(monitorFactory.waitForNextSuccessfulResult(cacheStore)).isEqualTo(TEST_INT_MESSAGE_V1)
  }

  @Test
  fun testCache_updated_newCache_newObserver_observesNewValue() {
    val cacheStore1 = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    val storeOp = cacheStore1.storeDataAsync { TEST_INT_MESSAGE_V1 }
    testCoroutineDispatchers.advanceUntilIdle()

    // Create a new cache with the same name.
    val cacheStore2 = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())

    // The new cache should have the updated value since it points to the same file as the first
    // cache. This is simulating something closer to an app restart or non-UI Dagger component
    // refresh since UI components should share the same cache instance via an application-bound
    // controller object.
    assertThat(storeOp.isCompleted).isTrue()
    assertThat(monitorFactory.waitForNextSuccessfulResult(cacheStore2)).isEqualTo(TEST_INT_MESSAGE_V1)
  }

  @Test
  fun testCache_updated_noInMemoryCacheUpdate_newCache_newObserver_observesNewVal() {
    val cacheStore1 = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    val storeOp = cacheStore1.storeDataAsync(updateInMemoryCache = false) { TEST_INT_MESSAGE_V1 }
    testCoroutineDispatchers.advanceUntilIdle()

    // Create a new cache with the same name.
    val cacheStore2 = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())

    // The new cache should have the updated value since it points to the same file as the first
    // cache, even though the update operation did not update the in-memory cache (the new cache has
    // a separate in-memory cache). This is simulating something closer to an app restart or non-UI
    // Dagger component refresh since UI components should share the same cache instance via an
    // application-bound controller object.
    assertThat(storeOp.isCompleted).isTrue()
    assertThat(monitorFactory.waitForNextSuccessfulResult(cacheStore2)).isEqualTo(TEST_INT_MESSAGE_V1)
  }

  @Test
  fun testExistingDiskCache_newCacheObject_updateNoMemThenRead_receivesNewValue() {
    val cacheStore1 = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    val storeOp1 =
      cacheStore1.storeDataAsync(updateInMemoryCache = false) { TEST_INT_MESSAGE_V1 }
    testCoroutineDispatchers.advanceUntilIdle()

    // Create a new cache with the same name and update it, then observe it.
    val cacheStore2 = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    val storeOp2 = cacheStore2.storeDataAsync(updateInMemoryCache = false) { TEST_INT_MESSAGE_V2 }
    testCoroutineDispatchers.advanceUntilIdle()

    // Both operations should be complete, and the observer will receive the latest value since the
    // update was posted before the read occurred.
    assertThat(storeOp1.isCompleted).isTrue()
    assertThat(storeOp2.isCompleted).isTrue()
    assertThat(monitorFactory.waitForNextSuccessfulResult(cacheStore2)).isEqualTo(TEST_INT_MESSAGE_V2)
  }

  @Test
  fun testCache_clear_initialState_keepsCacheStateTheSame() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())

    val clearOp = cacheStore.clearCacheAsync()
    testCoroutineDispatchers.advanceUntilIdle()

    // The new observer should observe the store with its default state since nothing needed to be
    // cleared.
    assertThat(clearOp.isCompleted).isTrue()
    assertThat(monitorFactory.waitForNextSuccessfulResult(cacheStore)).isEqualToDefaultInstance()
  }

  @Test
  fun testCache_update_clear_resetsCacheToInitialState() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    val storeOp = cacheStore.storeDataAsync { TEST_INT_MESSAGE_V1 }
    testCoroutineDispatchers.advanceUntilIdle()

    val clearOp = cacheStore.clearCacheAsync()
    testCoroutineDispatchers.advanceUntilIdle()

    // The new observer should observe that the store is cleared.
    assertThat(storeOp.isCompleted).isTrue()
    assertThat(clearOp.isCompleted).isTrue()
    assertThat(monitorFactory.waitForNextSuccessfulResult(cacheStore)).isEqualToDefaultInstance()
  }

  @Test
  fun testCache_update_existingObserver_clear_isNotifiedOfClear() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    val storeOp = cacheStore.storeDataAsync { TEST_INT_MESSAGE_V1 }
    testCoroutineDispatchers.advanceUntilIdle()

    val monitor = monitorFactory.createMonitor(cacheStore)
    monitor.waitForNextSuccessResult()
    val clearOp = cacheStore.clearCacheAsync()
    testCoroutineDispatchers.advanceUntilIdle()

    // The observer should not be notified the cache was cleared.
    assertThat(storeOp.isCompleted).isTrue()
    assertThat(clearOp.isCompleted).isTrue()
    monitor.verifyProviderIsNotUpdated()
  }

  @Test
  fun testCache_update_newCache_observesInitialState() {
    val cacheStore1 = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    val storeOp = cacheStore1.storeDataAsync { TEST_INT_MESSAGE_V1 }
    testCoroutineDispatchers.advanceUntilIdle()
    val clearOp = cacheStore1.clearCacheAsync()
    testCoroutineDispatchers.advanceUntilIdle()

    val cacheStore2 = cacheFactory.create(CACHE_NAME_1, TEST_INT_MESSAGE_V2)

    // The new observer should observe that there's no persisted on-disk store since it has a
    // different default value that would only be used if there wasn't already on-disk storage.
    assertThat(storeOp.isCompleted).isTrue()
    assertThat(clearOp.isCompleted).isTrue()
    assertThat(monitorFactory.waitForNextSuccessfulResult(cacheStore2)).isEqualTo(TEST_INT_MESSAGE_V2)
  }

  @Test
  fun testMultipleCaches_oneUpdates_newCacheSameNameDiffInit_observesUpdatedValue() {
    val cacheStore1 = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    val storeOp = cacheStore1.storeDataAsync { TEST_INT_MESSAGE_V1 }
    testCoroutineDispatchers.advanceUntilIdle()

    val cacheStore2 = cacheFactory.create(CACHE_NAME_1, TEST_INT_MESSAGE_V2)

    // The new cache should observe the updated on-disk value rather than its new default since an
    // on-disk value exists. This isn't a very realistic test since all caches should use default
    // proto instances for initialization, but it's a possible edge case that should at least have
    // established behavior that can be adjusted later if it isn't desirable in some circumstances.
    assertThat(storeOp.isCompleted).isTrue()
    assertThat(monitorFactory.waitForNextSuccessfulResult(cacheStore2)).isEqualTo(TEST_INT_MESSAGE_V1)
  }

  @Test
  fun testMultipleCaches_differentNames_oneUpdates_otherDoesNotObserveChange() {
    val cacheStore1 = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    val cacheStore2 = cacheFactory.create(CACHE_NAME_2, TestMessage.getDefaultInstance())

    val monitor1 = monitorFactory.createMonitor(cacheStore1)
    val monitor2 = monitorFactory.createMonitor(cacheStore2)
    val storeOp = cacheStore1.storeDataAsync { TEST_INT_MESSAGE_V1 }
    testCoroutineDispatchers.advanceUntilIdle()

    // The observer of the second store will be not notified of the change to the first store since
    // they have different names.
    assertThat(storeOp.isCompleted).isTrue()
    monitor1.verifyProviderIsNotUpdated()
    monitor2.verifyProviderIsNotUpdated()
  }

  @Test
  fun testMultipleCaches_diffNames_oneUpdates_cachesRecreated_onlyOneObservesVal() {
    val cacheStore1a = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    cacheFactory.create(CACHE_NAME_2, TestMessage.getDefaultInstance())
    val storeOp = cacheStore1a.storeDataAsync { TEST_INT_MESSAGE_V1 }
    testCoroutineDispatchers.advanceUntilIdle()

    // Recreate the stores and observe them.
    val cacheStore1b = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    val cacheStore2b = cacheFactory.create(CACHE_NAME_2, TestMessage.getDefaultInstance())

    // Only the observer of the first cache should notice the update since they are different
    // caches.
    assertThat(storeOp.isCompleted).isTrue()
    assertThat(monitorFactory.waitForNextSuccessfulResult(cacheStore1b)).isEqualTo(TEST_INT_MESSAGE_V1)
    assertThat(monitorFactory.waitForNextSuccessfulResult(cacheStore2b)).isEqualToDefaultInstance()
  }

  @Test
  fun testNewCache_fileCorrupted_providesError() {
    val cacheStore1 = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    val storeOp = cacheStore1.storeDataAsync { TEST_INT_MESSAGE_V1 }
    testCoroutineDispatchers.advanceUntilIdle()

    // Simulate the file being corrupted & reopen the file in a new store.
    corruptFileCache(CACHE_NAME_1)
    val cacheStore2 = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())

    // The new observer should receive an IOException error when trying to read the file.
    assertThat(storeOp.isCompleted).isTrue()
    val error = monitorFactory.waitForNextFailureResult(cacheStore2)
    assertThat(error).isInstanceOf(IOException::class.java)
  }

  @Test
  fun testNewCache_notYetRead_noCacheFileOnDisk() {
    cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())

    val cacheFile = getCacheFile(CACHE_NAME_1)
    assertThat(cacheFile.exists()).isFalse()
  }

  @Test
  fun testNewCache_readIntoMemory_noCacheFileOnDisk() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())

    monitorFactory.ensureDataProviderExecutes(cacheStore)

    // Even though the cache was 'read', it doesn't yet exist on disk. This test helps provide the
    // initial state for primeInMemoryAndDiskCacheAsync (to ensure it does what the caller expects).
    val cacheFile = getCacheFile(CACHE_NAME_1)
    assertThat(cacheFile.exists()).isFalse()
  }

  @Test
  fun testNewCache_fileAlreadyOnDisk_readIntoMemory_returnsOnDiskValue() {
    writeFileCache(CACHE_NAME_1, TestMessage.newBuilder().apply { strValue = "initial" }.build())
    val cacheStore = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())

    monitorFactory.ensureDataProviderExecutes(cacheStore)

    val cacheFile = getCacheFile(CACHE_NAME_1)
    assertThat(cacheFile.exists()).isTrue()
    assertThat(monitorFactory.waitForNextSuccessfulResult(cacheStore).strValue).isEqualTo("initial")
  }

  @Test
  fun testNoPrime_noDiskCache_unloadedCache_noNotifyOrLoadedOrDisk() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    subscribeToCacheStoreChanges(cacheStore)

    // Verify that a default cache store performs no notifications, nor does it create a disk cache.
    // This helps to establish the baseline assumed in later testPrime_* tests.
    verifyCacheStoreDidNotSendDataProviderNotification()
    assertThat(getCacheFile(CACHE_NAME_1).exists()).isFalse()
  }

  @Test
  fun testPrime_noDiskCache_unloadedCache_updateIfNew_publish_noXform_initsMemDiskNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(UPDATE_IF_NEW_CACHE, PUBLISH_TO_IN_MEMORY_CACHE)
    primeDeferred.waitForSuccessfulResult()

    verifyCacheStoreSentDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, DEFAULT_TEST_MESSAGE)
  }

  @Test
  fun testPrime_noDiskCache_unloadedCache_updateIfNew_publish_withXform_initsMemDiskNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(UPDATE_IF_NEW_CACHE, PUBLISH_TO_IN_MEMORY_CACHE) {
        it.addString(TEST_STR_V1)
      }
    primeDeferred.waitForSuccessfulResult()

    val expectedMessage = createTestMessage(strValue = TEST_STR_V1)
    verifyCacheStoreSentDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, expectedMessage)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, expectedMessage)
  }

  @Test
  fun testPrime_noDiskCache_unloadedCache_updateIfNew_noPublish_noXform_initsMemDiskNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(
        UPDATE_IF_NEW_CACHE, DO_NOT_PUBLISH_TO_IN_MEMORY_CACHE
      )
    primeDeferred.waitForSuccessfulResult()

    // The cache will notify in this case since the in-memory cache wasn't yet initialized.
    verifyCacheStoreSentDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, DEFAULT_TEST_MESSAGE)
  }

  @Test
  fun testPrime_noDiskCache_unloadedCache_updateIfNew_noPublish_withXform_initsMemDiskNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(
        UPDATE_IF_NEW_CACHE, DO_NOT_PUBLISH_TO_IN_MEMORY_CACHE
      ) { it.addString(TEST_STR_V1) }
    primeDeferred.waitForSuccessfulResult()

    // The cache will notify in this case since the in-memory cache wasn't yet initialized.
    val expectedMessage = createTestMessage(strValue = TEST_STR_V1)
    verifyCacheStoreSentDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, expectedMessage)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, expectedMessage)
  }

  @Test
  fun testPrime_noDiskCache_unloadedCache_updateAlways_publish_noXform_initsMemDiskNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(UPDATE_ALWAYS, PUBLISH_TO_IN_MEMORY_CACHE)
    primeDeferred.waitForSuccessfulResult()

    verifyCacheStoreSentDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, DEFAULT_TEST_MESSAGE)
  }

  @Test
  fun testPrime_noDiskCache_unloadedCache_updateAlways_publish_withXform_initsMemDiskNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(UPDATE_ALWAYS, PUBLISH_TO_IN_MEMORY_CACHE) {
        it.addString(TEST_STR_V1)
      }
    primeDeferred.waitForSuccessfulResult()

    val expectedMessage = createTestMessage(strValue = TEST_STR_V1)
    verifyCacheStoreSentDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, expectedMessage)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, expectedMessage)
  }

  @Test
  fun testPrime_noDiskCache_unloadedCache_updateAlways_noPublish_noXform_initsMemDiskNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(UPDATE_ALWAYS, DO_NOT_PUBLISH_TO_IN_MEMORY_CACHE)
    primeDeferred.waitForSuccessfulResult()

    // The cache will notify in this case since the in-memory cache wasn't yet initialized.
    verifyCacheStoreSentDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, DEFAULT_TEST_MESSAGE)
  }

  @Test
  fun testPrime_noDiskCache_unloadedCache_updateAlways_noPublish_withXform_initsMemDiskNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(UPDATE_ALWAYS, DO_NOT_PUBLISH_TO_IN_MEMORY_CACHE) {
        it.addString(TEST_STR_V1)
      }
    primeDeferred.waitForSuccessfulResult()

    // The cache will notify in this case since the in-memory cache wasn't yet initialized.
    val expectedMessage = createTestMessage(strValue = TEST_STR_V1)
    verifyCacheStoreSentDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, expectedMessage)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, expectedMessage)
  }

  @Test
  fun testPrime_noDiskCache_cacheFromMem_updateIfNew_publish_noXform_initsDiskOnlyNoNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    loadCacheIntoMemory(cacheStore)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(UPDATE_IF_NEW_CACHE, PUBLISH_TO_IN_MEMORY_CACHE)
    primeDeferred.waitForSuccessfulResult()

    // No notification is sent since the in-memory cache is already initialized ahead of priming.
    // The on-disk cache still needs to be initialized.
    verifyCacheStoreDidNotSendDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, DEFAULT_TEST_MESSAGE)
  }

  @Test
  fun testPrime_noDiskCache_cacheFromMem_updateIfNew_publish_withXform_initsMemDiskNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    loadCacheIntoMemory(cacheStore)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(UPDATE_IF_NEW_CACHE, PUBLISH_TO_IN_MEMORY_CACHE) {
        it.addString(TEST_STR_V1)
      }
    primeDeferred.waitForSuccessfulResult()

    val expectedMessage = createTestMessage(strValue = TEST_STR_V1)
    verifyCacheStoreSentDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, expectedMessage)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, expectedMessage)
  }

  @Test
  fun testPrime_noDiskCache_cacheFromMem_updateIfNew_noPublish_noXform_initsDiskOnlyNoNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    loadCacheIntoMemory(cacheStore)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(
        UPDATE_IF_NEW_CACHE, DO_NOT_PUBLISH_TO_IN_MEMORY_CACHE
      )
    primeDeferred.waitForSuccessfulResult()

    verifyCacheStoreDidNotSendDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, DEFAULT_TEST_MESSAGE)
  }

  @Test
  fun testPrime_noDiskCache_cacheFromMem_updateIfNew_noPublish_withXform_initsDiskOnlyNoNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    loadCacheIntoMemory(cacheStore)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(
        UPDATE_IF_NEW_CACHE, DO_NOT_PUBLISH_TO_IN_MEMORY_CACHE
      ) { it.addString(TEST_STR_V1) }
    primeDeferred.waitForSuccessfulResult()

    // The in-memory cache will not be updated in this situation since it was already loaded ahead
    // of priming the cache (and thus established).
    val expectedDiskMessage = createTestMessage(strValue = TEST_STR_V1)
    verifyCacheStoreDidNotSendDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, expectedDiskMessage)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, DEFAULT_TEST_MESSAGE)
  }

  @Test
  fun testPrime_noDiskCache_cacheFromMem_updateAlways_publish_noXform_initsDiskOnlyNoNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    loadCacheIntoMemory(cacheStore)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(UPDATE_ALWAYS, PUBLISH_TO_IN_MEMORY_CACHE)
    primeDeferred.waitForSuccessfulResult()

    // Despite the update & publish policies, no notification will be sent here since priming
    // doesn't change the cache store and the store has already be initialized into memory.
    verifyCacheStoreDidNotSendDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, DEFAULT_TEST_MESSAGE)
  }

  @Test
  fun testPrime_noDiskCache_cacheFromMem_updateAlways_publish_withXform_initsMemDiskNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    loadCacheIntoMemory(cacheStore)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(UPDATE_ALWAYS, PUBLISH_TO_IN_MEMORY_CACHE) {
        it.addString(TEST_STR_V1)
      }
    primeDeferred.waitForSuccessfulResult()

    val expectedMessage = createTestMessage(strValue = TEST_STR_V1)
    verifyCacheStoreSentDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, expectedMessage)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, expectedMessage)
  }

  @Test
  fun testPrime_noDiskCache_cacheFromMem_updateAlways_noPublish_noXform_initsDiskOnlyNoNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    loadCacheIntoMemory(cacheStore)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(UPDATE_ALWAYS, DO_NOT_PUBLISH_TO_IN_MEMORY_CACHE)
    primeDeferred.waitForSuccessfulResult()

    verifyCacheStoreDidNotSendDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, DEFAULT_TEST_MESSAGE)
  }

  @Test
  fun testPrime_noDiskCache_cacheFromMem_updateAlways_noPublish_withXform_initsDiskOnlyNoNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    loadCacheIntoMemory(cacheStore)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(UPDATE_ALWAYS, DO_NOT_PUBLISH_TO_IN_MEMORY_CACHE) {
        it.addString(TEST_STR_V1)
      }
    primeDeferred.waitForSuccessfulResult()

    // The in-memory cache will not be updated in this situation since it was already loaded ahead
    // of priming the cache (and thus established).
    val expectedDiskMessage = createTestMessage(strValue = TEST_STR_V1)
    verifyCacheStoreDidNotSendDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, expectedDiskMessage)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, DEFAULT_TEST_MESSAGE)
  }

  @Test
  fun testPrime_diskCache_unloadedCache_updateIfNew_publish_noXform_initsMemOnlyNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    writeFileCache(CACHE_NAME_1, TEST_INT_MESSAGE_V1)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(UPDATE_IF_NEW_CACHE, PUBLISH_TO_IN_MEMORY_CACHE)
    primeDeferred.waitForSuccessfulResult()

    // Effectively, only the in-memory version of the cache changes.
    verifyCacheStoreSentDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, TEST_INT_MESSAGE_V1)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, TEST_INT_MESSAGE_V1)
  }

  @Test
  fun testPrime_diskCache_unloadedCache_updateIfNew_publish_withXform_initsMemOnlyNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    writeFileCache(CACHE_NAME_1, TEST_INT_MESSAGE_V1)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(UPDATE_IF_NEW_CACHE, PUBLISH_TO_IN_MEMORY_CACHE) {
        it.addString(TEST_STR_V1)
      }
    primeDeferred.waitForSuccessfulResult()

    // The in-memory version should be initialized, but not updated (since it already exists). Plus,
    // the disk cache should not be read or updated.
    val expectedMessage = createTestMessage(intValue = TEST_INT_V1)
    verifyCacheStoreSentDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, expectedMessage)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, expectedMessage)
  }

  @Test
  fun testPrime_diskCache_unloadedCache_updateIfNew_noPublish_noXform_initsMemOnlyNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    writeFileCache(CACHE_NAME_1, TEST_INT_MESSAGE_V1)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(
        UPDATE_IF_NEW_CACHE, DO_NOT_PUBLISH_TO_IN_MEMORY_CACHE
      )
    primeDeferred.waitForSuccessfulResult()

    // Effectively, only the in-memory version of the cache changes, which in turn still results in
    // a notification being sent.
    verifyCacheStoreSentDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, TEST_INT_MESSAGE_V1)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, TEST_INT_MESSAGE_V1)
  }

  @Test
  fun testPrime_diskCache_unloadedCache_updateIfNew_noPublish_withXform_initsMemOnlyNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    writeFileCache(CACHE_NAME_1, TEST_INT_MESSAGE_V1)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(
        UPDATE_IF_NEW_CACHE, DO_NOT_PUBLISH_TO_IN_MEMORY_CACHE
      ) { it.addString(TEST_STR_V1) }
    primeDeferred.waitForSuccessfulResult()

    // The in-memory version should be initialized, but not updated (since it already exists). Plus,
    // the disk cache should not be read or updated. However, this will result in a notification
    // since the in-memory cache was unloaded.
    val expectedMessage = createTestMessage(intValue = TEST_INT_V1)
    verifyCacheStoreSentDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, expectedMessage)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, expectedMessage)
  }

  @Test
  fun testPrime_diskCache_unloadedCache_updateAlways_publish_noXform_initsMemOnlyNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    writeFileCache(CACHE_NAME_1, TEST_INT_MESSAGE_V1)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(UPDATE_ALWAYS, PUBLISH_TO_IN_MEMORY_CACHE)
    primeDeferred.waitForSuccessfulResult()

    // Effectively, only the in-memory version of the cache changes.
    verifyCacheStoreSentDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, TEST_INT_MESSAGE_V1)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, TEST_INT_MESSAGE_V1)
  }

  @Test
  fun testPrime_diskCache_unloadedCache_updateAlways_publish_withXform_initsMemDiskNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    writeFileCache(CACHE_NAME_1, TEST_INT_MESSAGE_V1)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(UPDATE_ALWAYS, PUBLISH_TO_IN_MEMORY_CACHE) {
        it.addString(TEST_STR_V1)
      }
    primeDeferred.waitForSuccessfulResult()

    // Both the in-memory and on-disk variants should change per the UPDATE_ALWAYS policy.
    val expectedMessage = createTestMessage(intValue = TEST_INT_V1, strValue = TEST_STR_V1)
    verifyCacheStoreSentDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, expectedMessage)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, expectedMessage)
  }

  @Test
  fun testPrime_diskCache_unloadedCache_updateAlways_noPublish_noXform_initsMemOnlyNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    writeFileCache(CACHE_NAME_1, TEST_INT_MESSAGE_V1)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(UPDATE_ALWAYS, DO_NOT_PUBLISH_TO_IN_MEMORY_CACHE)
    primeDeferred.waitForSuccessfulResult()

    // Effectively, only the in-memory version of the cache changes, which in turn still results in
    // a notification being sent.
    verifyCacheStoreSentDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, TEST_INT_MESSAGE_V1)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, TEST_INT_MESSAGE_V1)
  }

  @Test
  fun testPrime_diskCache_unloadedCache_updateAlways_noPublish_withXform_initsDiskOnlyNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    writeFileCache(CACHE_NAME_1, TEST_INT_MESSAGE_V1)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(UPDATE_ALWAYS, DO_NOT_PUBLISH_TO_IN_MEMORY_CACHE) {
        it.addString(TEST_STR_V1)
      }
    primeDeferred.waitForSuccessfulResult()

    // Only the on-disk variant should actually change (per UPDATE_ALWAYS), since the publish policy
    // indicates that the in-memory cache shouldn't reflect the new value (only the existing disk
    // cache). Furthermore, a notification will be sent anyway since the in-memory cache hasn't yet
    // been initialized.
    val expectedMemoryMessage = createTestMessage(intValue = TEST_INT_V1)
    val expectedDiskMessage = createTestMessage(intValue = TEST_INT_V1, strValue = TEST_STR_V1)
    verifyCacheStoreSentDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, expectedDiskMessage)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, expectedMemoryMessage)
  }

  @Test
  fun testPrime_diskCache_cacheFromDisk_updateIfNew_publish_noXform_noChangeOrNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    writeFileCache(CACHE_NAME_1, TEST_INT_MESSAGE_V1)
    loadCacheIntoMemory(cacheStore)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(UPDATE_IF_NEW_CACHE, PUBLISH_TO_IN_MEMORY_CACHE)
    primeDeferred.waitForSuccessfulResult()

    // Nothing should be notified, or change on-disk or in-memory (other initing the cache).
    verifyCacheStoreDidNotSendDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, TEST_INT_MESSAGE_V1)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, TEST_INT_MESSAGE_V1)
  }

  @Test
  fun testPrime_diskCache_cacheFromDisk_updateIfNew_publish_withXform_noChangeOrNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    writeFileCache(CACHE_NAME_1, TEST_INT_MESSAGE_V1)
    loadCacheIntoMemory(cacheStore)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(UPDATE_IF_NEW_CACHE, PUBLISH_TO_IN_MEMORY_CACHE) {
        it.addString(TEST_STR_V1)
      }
    primeDeferred.waitForSuccessfulResult()

    // Nothing should be notified, or change on-disk or in-memory per UPDATE_IF_NEW_CACHE.
    val expectedMessage = createTestMessage(intValue = TEST_INT_V1)
    verifyCacheStoreDidNotSendDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, expectedMessage)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, expectedMessage)
  }

  @Test
  fun testPrime_diskCache_cacheFromDisk_updateIfNew_noPublish_noXform_noChangeOrNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    writeFileCache(CACHE_NAME_1, TEST_INT_MESSAGE_V1)
    loadCacheIntoMemory(cacheStore)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(
        UPDATE_IF_NEW_CACHE, DO_NOT_PUBLISH_TO_IN_MEMORY_CACHE
      )
    primeDeferred.waitForSuccessfulResult()

    // Nothing should be notified, or change on-disk or in-memory (other initing the cache).
    verifyCacheStoreDidNotSendDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, TEST_INT_MESSAGE_V1)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, TEST_INT_MESSAGE_V1)
  }

  @Test
  fun testPrime_diskCache_cacheFromDisk_updateIfNew_noPublish_withXform_noChangeOrNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    writeFileCache(CACHE_NAME_1, TEST_INT_MESSAGE_V1)
    loadCacheIntoMemory(cacheStore)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(
        UPDATE_IF_NEW_CACHE, DO_NOT_PUBLISH_TO_IN_MEMORY_CACHE
      ) { it.addString(TEST_STR_V1) }
    primeDeferred.waitForSuccessfulResult()

    // Nothing should be notified, or change on-disk or in-memory per UPDATE_IF_NEW_CACHE.
    val expectedMessage = createTestMessage(intValue = TEST_INT_V1)
    verifyCacheStoreDidNotSendDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, expectedMessage)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, expectedMessage)
  }

  @Test
  fun testPrime_diskCache_cacheFromDisk_updateAlways_publish_noXform_noChangeOrNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    writeFileCache(CACHE_NAME_1, TEST_INT_MESSAGE_V1)
    loadCacheIntoMemory(cacheStore)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(UPDATE_ALWAYS, PUBLISH_TO_IN_MEMORY_CACHE)
    primeDeferred.waitForSuccessfulResult()

    // Nothing should be notified, or change on-disk or in-memory (other initing the cache).
    verifyCacheStoreDidNotSendDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, TEST_INT_MESSAGE_V1)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, TEST_INT_MESSAGE_V1)
  }

  @Test
  fun testPrime_diskCache_cacheFromDisk_updateAlways_publish_withXform_initsMemDiskNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    writeFileCache(CACHE_NAME_1, TEST_INT_MESSAGE_V1)
    loadCacheIntoMemory(cacheStore)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(UPDATE_ALWAYS, PUBLISH_TO_IN_MEMORY_CACHE) {
        it.addString(TEST_STR_V1)
      }
    primeDeferred.waitForSuccessfulResult()

    // Since the policy is to always update the cache, the in-memory & on-disk versions should be
    // updated plus a notification sent.
    val expectedMessage = createTestMessage(intValue = TEST_INT_V1, strValue = TEST_STR_V1)
    verifyCacheStoreSentDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, expectedMessage)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, expectedMessage)
  }

  @Test
  fun testPrime_diskCache_cacheFromDisk_updateAlways_noPublish_noXform_noChangeOrNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    writeFileCache(CACHE_NAME_1, TEST_INT_MESSAGE_V1)
    loadCacheIntoMemory(cacheStore)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(UPDATE_ALWAYS, DO_NOT_PUBLISH_TO_IN_MEMORY_CACHE)
    primeDeferred.waitForSuccessfulResult()

    // Nothing should be notified, or change on-disk or in-memory (other initing the cache).
    verifyCacheStoreDidNotSendDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, TEST_INT_MESSAGE_V1)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, TEST_INT_MESSAGE_V1)
  }

  @Test
  fun testPrime_diskCache_cacheFromDisk_updateAlways_noPublish_withXform_initsMemDiskNoNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    writeFileCache(CACHE_NAME_1, TEST_INT_MESSAGE_V1)
    loadCacheIntoMemory(cacheStore)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(UPDATE_ALWAYS, DO_NOT_PUBLISH_TO_IN_MEMORY_CACHE) {
        it.addString(TEST_STR_V1)
      }
    primeDeferred.waitForSuccessfulResult()

    // Since the policy is to always update the cache, the on-disk version should be updated.
    // However, the in-memory cache will not be updated since it was already loaded ahead of priming
    // (and hence established). Furthermore, no notification sent per the publish policy.
    val expectedMemoryMessage = createTestMessage(intValue = TEST_INT_V1)
    val expectedDiskMessage = createTestMessage(intValue = TEST_INT_V1, strValue = TEST_STR_V1)
    verifyCacheStoreDidNotSendDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, expectedDiskMessage)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, expectedMemoryMessage)
  }

  @Test
  fun testPrime_badDiskCache_unloadedCache_updateIfNew_publish_noXform_initsMemDiskNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    corruptFileCache(CACHE_NAME_1)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(UPDATE_IF_NEW_CACHE, PUBLISH_TO_IN_MEMORY_CACHE)
    primeDeferred.waitForSuccessfulResult()

    // A corrupted disk cache is treated as a completely new cache.
    verifyCacheStoreSentDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, DEFAULT_TEST_MESSAGE)
  }

  @Test
  fun testPrime_badDiskCache_unloadedCache_updateIfNew_publish_withXform_initsMemDiskNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    corruptFileCache(CACHE_NAME_1)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(UPDATE_IF_NEW_CACHE, PUBLISH_TO_IN_MEMORY_CACHE) {
        it.addString(TEST_STR_V1)
      }
    primeDeferred.waitForSuccessfulResult()

    // A corrupted disk cache is treated as a completely new cache.
    val expectedMessage = createTestMessage(strValue = TEST_STR_V1)
    verifyCacheStoreSentDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, expectedMessage)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, expectedMessage)
  }

  @Test
  fun testPrime_badDiskCache_unloadedCache_updateIfNew_noPublish_noXform_initsMemDiskNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    corruptFileCache(CACHE_NAME_1)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(
        UPDATE_IF_NEW_CACHE, DO_NOT_PUBLISH_TO_IN_MEMORY_CACHE
      )
    primeDeferred.waitForSuccessfulResult()

    // A corrupted disk cache is treated as a completely new cache. Because of that, a notification
    // is actually sent in this case.
    verifyCacheStoreSentDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, DEFAULT_TEST_MESSAGE)
  }

  @Test
  fun testPrime_badDiskCache_unloadedCache_updateIfNew_noPublish_withXform_initsMemDiskNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    corruptFileCache(CACHE_NAME_1)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(
        UPDATE_IF_NEW_CACHE, DO_NOT_PUBLISH_TO_IN_MEMORY_CACHE
      ) { it.addString(TEST_STR_V1) }
    primeDeferred.waitForSuccessfulResult()

    // A corrupted disk cache is treated as a completely new cache. Because of that, a notification
    // is actually sent in this case.
    val expectedMessage = createTestMessage(strValue = TEST_STR_V1)
    verifyCacheStoreSentDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, expectedMessage)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, expectedMessage)
  }

  @Test
  fun testPrime_badDiskCache_unloadedCache_updateAlways_publish_noXform_initsMemDiskNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    corruptFileCache(CACHE_NAME_1)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(UPDATE_ALWAYS, PUBLISH_TO_IN_MEMORY_CACHE)
    primeDeferred.waitForSuccessfulResult()

    verifyCacheStoreSentDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, DEFAULT_TEST_MESSAGE)
  }

  @Test
  fun testPrime_badDiskCache_unloadedCache_updateAlways_publish_withXform_initsMemDiskNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    corruptFileCache(CACHE_NAME_1)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(UPDATE_ALWAYS, PUBLISH_TO_IN_MEMORY_CACHE) {
        it.addString(TEST_STR_V1)
      }
    primeDeferred.waitForSuccessfulResult()

    val expectedMessage = createTestMessage(strValue = TEST_STR_V1)
    verifyCacheStoreSentDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, expectedMessage)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, expectedMessage)
  }

  @Test
  fun testPrime_badDiskCache_unloadedCache_updateAlways_noPublish_noXform_initsMemDiskNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    corruptFileCache(CACHE_NAME_1)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(UPDATE_ALWAYS, DO_NOT_PUBLISH_TO_IN_MEMORY_CACHE)
    primeDeferred.waitForSuccessfulResult()

    // A corrupted disk cache is treated as a completely new cache. Because of that, a notification
    // is actually sent in this case.
    verifyCacheStoreSentDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, DEFAULT_TEST_MESSAGE)
  }

  @Test
  fun testPrime_badDiskCache_unloadedCache_updateAlways_noPublish_withXform_initsMemDiskNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    corruptFileCache(CACHE_NAME_1)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(UPDATE_ALWAYS, DO_NOT_PUBLISH_TO_IN_MEMORY_CACHE) {
        it.addString(TEST_STR_V1)
      }
    primeDeferred.waitForSuccessfulResult()

    // A corrupted disk cache is treated as a completely new cache. Because of that, a notification
    // is actually sent in this case.
    val expectedMessage = createTestMessage(strValue = TEST_STR_V1)
    verifyCacheStoreSentDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, expectedMessage)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, expectedMessage)
  }

  @Test
  fun testPrime_badDiskCache_cacheFromMem_updateIfNew_publish_noXform_initsDiskOnlyNoNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    corruptFileCache(CACHE_NAME_1)
    loadCacheIntoMemory(cacheStore)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(UPDATE_IF_NEW_CACHE, PUBLISH_TO_IN_MEMORY_CACHE)
    primeDeferred.waitForSuccessfulResult()

    // The corrupted cache should have failed to be read, so it will be treated as a new cache.
    // However, only the disk cache actually needs to be updated since the in-memory one has already
    // been established prior to priming (which is also why no notification is sent in this case).
    verifyCacheStoreDidNotSendDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, DEFAULT_TEST_MESSAGE)
  }

  @Test
  fun testPrime_badDiskCache_cacheFromMem_updateIfNew_publish_withXform_initsMemDiskNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    corruptFileCache(CACHE_NAME_1)
    loadCacheIntoMemory(cacheStore)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(UPDATE_IF_NEW_CACHE, PUBLISH_TO_IN_MEMORY_CACHE) {
        it.addString(TEST_STR_V1)
      }
    primeDeferred.waitForSuccessfulResult()

    // The corrupted cache should have failed to be read, so it will be treated as a new cache.
    val expectedMessage = createTestMessage(strValue = TEST_STR_V1)
    verifyCacheStoreSentDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, expectedMessage)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, expectedMessage)
  }

  @Test
  fun testPrime_badDiskCache_cacheFromMem_updateIfNew_noPublish_noXform_initsDiskOnlyNoNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    corruptFileCache(CACHE_NAME_1)
    loadCacheIntoMemory(cacheStore)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(
        UPDATE_IF_NEW_CACHE, DO_NOT_PUBLISH_TO_IN_MEMORY_CACHE
      )
    primeDeferred.waitForSuccessfulResult()

    // The corrupted cache should have failed to be read, so it will be treated as a new cache.
    verifyCacheStoreDidNotSendDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, DEFAULT_TEST_MESSAGE)
  }

  @Test
  fun testPrime_badDiskCache_cacheFromMem_updateIfNew_noPublish_withXform_initsMemDiskNoNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    corruptFileCache(CACHE_NAME_1)
    loadCacheIntoMemory(cacheStore)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(
        UPDATE_IF_NEW_CACHE, DO_NOT_PUBLISH_TO_IN_MEMORY_CACHE
      ) {  it.addString(TEST_STR_V1) }
    primeDeferred.waitForSuccessfulResult()

    // The corrupted cache should have failed to be read, so it will be treated as a new cache. Note
    // though that the initial load operation will result in the in-memory cache becoming the
    // default cache value (and thus different from the on-disk version) per the publish policy.
    val expectedOnDiskMessage = createTestMessage(strValue = TEST_STR_V1)
    verifyCacheStoreDidNotSendDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, expectedOnDiskMessage)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, DEFAULT_TEST_MESSAGE)
  }

  @Test
  fun testPrime_badDiskCache_cacheFromMem_updateAlways_publish_noXform_initsDiskOnlyNoNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    corruptFileCache(CACHE_NAME_1)
    loadCacheIntoMemory(cacheStore)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(UPDATE_ALWAYS, PUBLISH_TO_IN_MEMORY_CACHE)
    primeDeferred.waitForSuccessfulResult()

    // Despite the update & publish policies, no notification will be sent here since priming
    // doesn't change the cache store and the store has already be initialized into memory.
    verifyCacheStoreDidNotSendDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, DEFAULT_TEST_MESSAGE)
  }

  @Test
  fun testPrime_badDiskCache_cacheFromMem_updateAlways_publish_withXform_initsMemDiskNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    corruptFileCache(CACHE_NAME_1)
    loadCacheIntoMemory(cacheStore)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(UPDATE_ALWAYS, PUBLISH_TO_IN_MEMORY_CACHE) {
        it.addString(TEST_STR_V1)
      }
    primeDeferred.waitForSuccessfulResult()

    val expectedMessage = createTestMessage(strValue = TEST_STR_V1)
    verifyCacheStoreSentDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, expectedMessage)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, expectedMessage)
  }

  @Test
  fun testPrime_badDiskCache_cacheFromMem_updateAlways_noPublish_noXform_initsDiskOnlyNoNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    corruptFileCache(CACHE_NAME_1)
    loadCacheIntoMemory(cacheStore)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(UPDATE_ALWAYS, DO_NOT_PUBLISH_TO_IN_MEMORY_CACHE)
    primeDeferred.waitForSuccessfulResult()

    verifyCacheStoreDidNotSendDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, DEFAULT_TEST_MESSAGE)
  }

  @Test
  fun testPrime_badDiskCache_cacheFromMem_updateAlways_noPublish_withXform_initsDiskOnlyNoNotify() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, DEFAULT_TEST_MESSAGE)
    corruptFileCache(CACHE_NAME_1)
    loadCacheIntoMemory(cacheStore)
    subscribeToCacheStoreChanges(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(UPDATE_ALWAYS, DO_NOT_PUBLISH_TO_IN_MEMORY_CACHE) {
        it.addString(TEST_STR_V1)
      }
    primeDeferred.waitForSuccessfulResult()

    // The in-memory cache will not be updated in this situation since it was already loaded ahead
    // of priming the cache (and thus established).
    val expectedDiskMessage = createTestMessage(strValue = TEST_STR_V1)
    verifyCacheStoreDidNotSendDataProviderNotification()
    verifyDiskCacheHasValue(CACHE_NAME_1, expectedDiskMessage)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, DEFAULT_TEST_MESSAGE)
  }

  @Test
  fun testPrime_badDiskCache_cacheFromMem_passesDefaultMessageInToTransform() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, TEST_INT_MESSAGE_V1)
    corruptFileCache(CACHE_NAME_1)
    loadCacheIntoMemory(cacheStore)

    val primeDeferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(UPDATE_ALWAYS, DO_NOT_PUBLISH_TO_IN_MEMORY_CACHE) {
        it
      }
    primeDeferred.waitForSuccessfulResult()

    // Verify that the proto passed into prime's update method is the default in this case (since
    // the disk cache is corrupted).
    val expectedMessage = createTestMessage(intValue = TEST_INT_V1)
    verifyDiskCacheHasValue(CACHE_NAME_1, expectedMessage)
    verifyCacheStoreHasInMemoryValue(CACHE_NAME_1, cacheStore, expectedMessage)
  }

  @Test
  fun testPrimeInMemoryAndOnDisk_newObject_updateNoMemThenRead_primed_receivesPrevVal() {
    val cacheStore1 = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    val storeOp1 =
      cacheStore1.storeDataAsync(updateInMemoryCache = false) { TEST_INT_MESSAGE_V1 }
    testCoroutineDispatchers.advanceUntilIdle()

    // Create a new cache with the same name and update it, then observe it. However, first prime
    // it.
    val cacheStore2 = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    val primeOp =
      cacheStore2.primeInMemoryAndDiskCacheAsync(
        updateMode = UPDATE_IF_NEW_CACHE, publishMode = DO_NOT_PUBLISH_TO_IN_MEMORY_CACHE
      )
    testCoroutineDispatchers.advanceUntilIdle()
    val storeOp2 = cacheStore2.storeDataAsync(updateInMemoryCache = false) { TEST_INT_MESSAGE_V2 }
    testCoroutineDispatchers.advanceUntilIdle()

    // All operations should be complete, but the observer will receive the previous update rather
    // than the latest since it wasn't updated in memory and the cache was pre-primed.
    assertThat(storeOp1.isCompleted).isTrue()
    assertThat(storeOp2.isCompleted).isTrue()
    assertThat(primeOp.isCompleted).isTrue()
    assertThat(monitorFactory.waitForNextSuccessfulResult(cacheStore2)).isEqualTo(TEST_INT_MESSAGE_V1)
  }

  @Test
  fun testPrimeInMemoryAndOnDisk_onDisk_newObject_updateMemThenRead_primed_receivesNewVal() {
    val cacheStore1 = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    val storeOp1 =
      cacheStore1.storeDataAsync(updateInMemoryCache = false) { TEST_INT_MESSAGE_V1 }
    testCoroutineDispatchers.advanceUntilIdle()

    // Create a new cache with the same name and update it, then observe it. However, first prime
    // it.
    val cacheStore2 = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    val primeOp =
      cacheStore2.primeInMemoryAndDiskCacheAsync(
        updateMode = UPDATE_IF_NEW_CACHE, publishMode = DO_NOT_PUBLISH_TO_IN_MEMORY_CACHE
      )
    testCoroutineDispatchers.advanceUntilIdle()
    val storeOp2 = cacheStore2.storeDataAsync { TEST_INT_MESSAGE_V2 }
    testCoroutineDispatchers.advanceUntilIdle()

    // Similar to the previous test, except due to the in-memory update the observer will receive
    // the latest result regardless of the cache priming.
    assertThat(storeOp1.isCompleted).isTrue()
    assertThat(storeOp2.isCompleted).isTrue()
    assertThat(primeOp.isCompleted).isTrue()
    assertThat(monitorFactory.waitForNextSuccessfulResult(cacheStore2)).isEqualTo(TEST_INT_MESSAGE_V2)
  }

  @Test
  fun testPrimeInMemoryAndOnDisk_afterStoreUpdateWithoutMemUpdate_observesOldValue() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    // Force initializing the store's in-memory cache
    monitorFactory.waitForNextSuccessfulResult(cacheStore)

    val storeOp = cacheStore.storeDataAsync(updateInMemoryCache = false) { TEST_INT_MESSAGE_V1 }
    testCoroutineDispatchers.advanceUntilIdle()
    val primeOp =
      cacheStore.primeInMemoryAndDiskCacheAsync(
        updateMode = UPDATE_IF_NEW_CACHE,
        publishMode = DO_NOT_PUBLISH_TO_IN_MEMORY_CACHE
      )
    testCoroutineDispatchers.advanceUntilIdle()

    // Both ops will succeed, and the observer will receive the old value due to the update not
    // changing the in-memory cache, and the prime no-oping due to the cache already being
    // initialized.
    assertThat(storeOp.isCompleted).isTrue()
    assertThat(primeOp.isCompleted).isTrue()
    assertThat(monitorFactory.waitForNextSuccessfulResult(cacheStore)).isEqualToDefaultInstance()
  }

  @Test
  fun testPrimeInMemoryAndOnDisk_newCache_notOnDisk_notInMem_writesFileAndRetsNewVal() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())

    val deferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(
        updateMode = UPDATE_IF_NEW_CACHE, publishMode = PUBLISH_TO_IN_MEMORY_CACHE
      ) { it.toBuilder().apply { strValue += " first transform" }.build() }

    // The on-disk and in-memory values should change.
    deferred.waitForSuccessfulResult()
    val onDiskValue = readFileCache<TestMessage>(CACHE_NAME_1)
    val cacheValue = monitorFactory.waitForNextSuccessfulResult(cacheStore)
    assertThat(cacheValue).isEqualTo(onDiskValue)
    assertThat(cacheValue.strValue.trim()).isEqualTo("first transform")
  }

  @Test
  fun testPrimeInMemoryAndOnDisk_newCache_onDisk_notInMem_writesFileAndRetsOldVal() {
    writeFileCache(CACHE_NAME_1, TestMessage.newBuilder().apply { strValue = "initial" }.build())
    val cacheStore = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())

    val deferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(
        updateMode = UPDATE_IF_NEW_CACHE, publishMode = PUBLISH_TO_IN_MEMORY_CACHE
      ) { it.toBuilder().apply { strValue += " first transform" }.build() }

    // The on-disk value should be the same, and the in-memory value should become the on-disk
    // value. The initializer shouldn't be used since the value is already on disk.
    deferred.waitForSuccessfulResult()
    val onDiskValue = readFileCache<TestMessage>(CACHE_NAME_1)
    val cacheValue = monitorFactory.waitForNextSuccessfulResult(cacheStore)
    assertThat(cacheValue).isEqualTo(onDiskValue)
    assertThat(cacheValue.strValue).isEqualTo("initial")
  }

  @Test
  fun testPrimeInMemoryAndOnDisk_existingCache_unchanged_onDisk_inMem_onlyReadsFileAndRetsOldVal() {
    writeFileCache(CACHE_NAME_1, TestMessage.newBuilder().apply { strValue = "initial" }.build())
    val cacheStore = cacheFactory.create(
      CACHE_NAME_1, TestMessage.newBuilder().apply { strValue = "different initial" }.build()
    )

    val deferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(
        updateMode = UPDATE_IF_NEW_CACHE, publishMode = PUBLISH_TO_IN_MEMORY_CACHE
      ) { it.toBuilder().apply { strValue += " first transform" }.build() }

    // Priming should ignore both the on-disk and in-memory values of the cache store since only the
    // initial value matters.
    deferred.waitForSuccessfulResult()
    val onDiskValue = readFileCache<TestMessage>(CACHE_NAME_1)
    val cacheValue = monitorFactory.waitForNextSuccessfulResult(cacheStore)
    assertThat(cacheValue).isEqualTo(onDiskValue)
    assertThat(cacheValue.strValue).isEqualTo("initial")
  }

  @Test
  fun testPrimeInMemoryAndOnDisk_existingCache_changed_notOnDisk_inMem_writesFileAndRetsOldVal() {
    val cacheStore = cacheFactory.create(
      CACHE_NAME_1, TestMessage.newBuilder().apply { strValue = "different initial" }.build()
    )
    cacheStore.storeDataAsync {
      it.toBuilder().apply { strValue = "different update" }.build()
    }.waitForResult()

    val deferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(
        updateMode = UPDATE_IF_NEW_CACHE, publishMode = PUBLISH_TO_IN_MEMORY_CACHE
      ) { it.toBuilder().apply { strValue += " first transform" }.build() }

    // Priming shouldn't really change much since the recent change to the cache store takes
    // precedence.
    deferred.waitForSuccessfulResult()
    val onDiskValue = readFileCache<TestMessage>(CACHE_NAME_1)
    val cacheValue = monitorFactory.waitForNextSuccessfulResult(cacheStore)
    assertThat(cacheValue).isEqualTo(onDiskValue)
    assertThat(cacheValue.strValue).isEqualTo("different update")
  }

  @Test
  fun testPrimeInMemoryAndOnDisk_existingCache_changed_onDisk_inMem_onlyReadsFileAndRetsOldVal() {
    writeFileCache(CACHE_NAME_1, TestMessage.newBuilder().apply { strValue = "initial" }.build())
    val cacheStore = cacheFactory.create(
      CACHE_NAME_1, TestMessage.newBuilder().apply { strValue = "different initial" }.build()
    )
    cacheStore.storeDataAsync {
      it.toBuilder().apply { strValue = "different update" }.build()
    }.waitForResult()

    val deferred =
      cacheStore.primeInMemoryAndDiskCacheAsync(
        updateMode = UPDATE_IF_NEW_CACHE, publishMode = PUBLISH_TO_IN_MEMORY_CACHE
      ) { it.toBuilder().apply { strValue += " first transform" }.build() }

    // Priming shouldn't really change much since the recent change to the cache store takes
    // precedence.
    deferred.waitForSuccessfulResult()
    val onDiskValue = readFileCache<TestMessage>(CACHE_NAME_1)
    val cacheValue = monitorFactory.waitForNextSuccessfulResult(cacheStore)
    assertThat(cacheValue).isEqualTo(onDiskValue)
    assertThat(cacheValue.strValue).isEqualTo("different update")
  }

  @Test
  fun testPrimeInMemoryAndOnDisk_existingCache_corruptedOnDisk_updatesFileAndRetsNewVal() {
    corruptFileCache(CACHE_NAME_1)
    val cacheStore1 = cacheFactory.create(
      CACHE_NAME_1, TestMessage.newBuilder().apply { strValue = "different initial" }.build()
    )
    val cacheStore2 = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    monitorFactory.ensureDataProviderExecutes(cacheStore1)

    val deferred =
      cacheStore1.primeInMemoryAndDiskCacheAsync(
        updateMode = UPDATE_IF_NEW_CACHE, publishMode = PUBLISH_TO_IN_MEMORY_CACHE
      ) { it.toBuilder().apply { strValue += " first transform" }.build() }

    // The corrupted cache will trigger an in-memory only state that will lead to the cache being
    // overwritten (since it can't be determined whether the on-disk cache matches the expected
    // value). Note the on-disk cache will still be updated, and that the second instance of the
    // cache wasn't yet primed until this step, so it's being used to validate that the in-memory
    // copy is also correct after the on-disk value has been updated.
    deferred.waitForSuccessfulResult()
    val onDiskValue = readFileCache<TestMessage>(CACHE_NAME_1)
    val cacheValue = monitorFactory.waitForNextSuccessfulResult(cacheStore2)
    assertThat(cacheValue).isEqualTo(onDiskValue)
    assertThat(onDiskValue.strValue).isEqualTo("different initial first transform")
  }

  private fun <T: MessageLite> subscribeToCacheStoreChanges(cacheStore: PersistentCacheStore<T>) {
    asyncDataSubscriptionManager.subscribe(
      cacheStore.getId(), mockSubscriptionCallback.toAsyncChange()
    )
  }

  private fun <T: MessageLite> loadCacheIntoMemory(cacheStore: PersistentCacheStore<T>) {
    // Attempt to load the cache store from disk--this ensures that the cache store is at least
    // loaded into memory.
    monitorFactory.ensureDataProviderExecutes(cacheStore)
  }

  private fun <T: MessageLite> verifyCacheStoreHasInMemoryValue(
    cacheName: String, cacheStore: PersistentCacheStore<T>, value: T
  ) {
    // Delete the cache before reading from the store to verify that the read value is actually
    // in-memory and not being read from disk.
    deleteCacheFile(cacheName)
    assertThat(monitorFactory.waitForNextSuccessfulResult(cacheStore)).isEqualTo(value)
  }

  private fun <T: MessageLite> verifyDiskCacheHasValue(cacheName: String, value: T) {
    assertThat(readFileCache<TestMessage>(cacheName)).isEqualTo(value)
  }

  private fun verifyCacheStoreSentDataProviderNotification() {
    verify(mockSubscriptionCallback).onDataProviderChanged()
  }

  private fun verifyCacheStoreDidNotSendDataProviderNotification() {
    verify(mockSubscriptionCallback, never()).onDataProviderChanged()
  }
  
  private fun getCacheFile(cacheName: String) = File(context.filesDir, "$cacheName.cache")

  private fun deleteCacheFile(cacheName: String) = getCacheFile(cacheName).delete()

  private fun corruptFileCache(cacheName: String) {
    // NB: This is unfortunately tied to the implementation details of PersistentCacheStore. If this
    // ends up being an issue, the store should be updated to call into a file path provider that
    // can also be used in this test to retrieve the file cache. This may also be needed for
    // downstream profile work if per-profile data stores are done via subdirectories or altered
    // filenames.
    getCacheFile(cacheName).writeBytes(byteArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9))
  }

  private fun <T : MessageLite> writeFileCache(cacheName: String, value: T) {
    getCacheFile(cacheName).writeBytes(value.toByteArray())
  }

  private fun File.writeBytes(data: ByteArray) {
    FileOutputStream(this).use { it.write(data) }
  }

  private inline fun <reified T : MessageLite> readFileCache(cacheName: String): T {
    // Use reflection to simplify the test API.
    val defaultInstance =
      T::class.staticFunctions.find { it.name == "getDefaultInstance" }?.call() as? T?
    return FileInputStream(getCacheFile(cacheName)).use {
      checkNotNull(defaultInstance).newBuilderForType().mergeFrom(it).build()
    } as T
  }

  private fun <T> Deferred<T>.waitForSuccessfulResult() {
    return when (val result = waitForResult()) {
      is AsyncResult.Pending -> error("Deferred never finished.")
      is AsyncResult.Success -> {} // Nothing to do; the result succeeded.
      is AsyncResult.Failure -> throw IllegalStateException("Deferred failed", result.error)
    }
  }

  private fun <T> Deferred<T>.waitForResult() = toStateFlow().waitForLatestValue()

  private fun <T> Deferred<T>.toStateFlow(): StateFlow<AsyncResult<T>> {
    val deferred = this
    return MutableStateFlow<AsyncResult<T>>(value = AsyncResult.Pending()).also { flow ->
      backgroundDispatcherScope.async {
        flow.emit(AsyncResult.Success(deferred.await()))
      }.invokeOnCompletion {
        it?.let { flow.tryEmit(AsyncResult.Failure(it)) }
      }
    }
  }

  private fun <T> StateFlow<T>.waitForLatestValue(): T =
    also { testCoroutineDispatchers.runCurrent() }.value

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
      RobolectricModule::class,
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

  interface SubscriptionCallback {
    fun onDataProviderChanged()

    companion object {
      fun SubscriptionCallback.toAsyncChange(): ObserveAsyncChange = { onDataProviderChanged() }
    }
  }
}
