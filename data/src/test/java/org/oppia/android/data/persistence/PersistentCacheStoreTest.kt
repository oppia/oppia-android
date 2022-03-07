package org.oppia.android.data.persistence

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.extensions.proto.LiteProtoTruth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
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
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.threading.BackgroundDispatcher
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

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
    private val TEST_MESSAGE_V1 = TestMessage.newBuilder().setIntValue(1).build()
    private val TEST_MESSAGE_V2 = TestMessage.newBuilder().setIntValue(2).build()
  }

  @Inject lateinit var cacheFactory: PersistentCacheStore.Factory
  @Inject lateinit var dataProviders: DataProviders
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @field:[Inject BackgroundDispatcher] lateinit var backgroundDispatcher: CoroutineDispatcher
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory

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
    val cacheStore = cacheFactory.create(CACHE_NAME_1, TEST_MESSAGE_V1)

    val value = monitorFactory.waitForNextSuccessfulResult(cacheStore)

    // Caches can have non-default initial states.
    assertThat(value).isEqualTo(TEST_MESSAGE_V1)
  }

  @Test
  fun testCache_registerObserver_updateAfter_observerNotifiedOfNewValue() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())

    monitorFactory.waitForNextSuccessfulResult(cacheStore)
    val storeOp = cacheStore.storeDataAsync { TEST_MESSAGE_V1 }
    testCoroutineDispatchers.advanceUntilIdle()

    // The store operation should be completed, and the observer should be notified of the changed
    // value.
    assertThat(storeOp.isCompleted).isTrue()
    assertThat(monitorFactory.waitForNextSuccessfulResult(cacheStore)).isEqualTo(TEST_MESSAGE_V1)
  }

  @Test
  fun testCache_registerObserver_updateBefore_observesUpdatedStateInitially() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())

    val storeOp = cacheStore.storeDataAsync { TEST_MESSAGE_V1 }
    testCoroutineDispatchers.advanceUntilIdle()

    // The store operation should be completed, and the observer's only call should be the updated
    // state.
    val value = monitorFactory.waitForNextSuccessfulResult(cacheStore)
    assertThat(storeOp.isCompleted).isTrue()
    assertThat(value).isEqualTo(TEST_MESSAGE_V1)
  }

  @Test
  fun testCache_noMemoryCacheUpdate_updateAfterReg_observerNotNotified() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    val monitor = monitorFactory.createMonitor(cacheStore)

    monitor.waitForNextSuccessResult()
    val storeOp = cacheStore.storeDataAsync(updateInMemoryCache = false) { TEST_MESSAGE_V1 }
    testCoroutineDispatchers.advanceUntilIdle()

    // The store operation should be completed, but the observe will not be notified of changes
    // since the in-memory cache was not changed.
    assertThat(storeOp.isCompleted).isTrue()
    monitor.verifyProviderIsNotUpdated()
  }

  @Test
  fun testCache_noMemoryCacheUpdate_updateBeforeReg_observesUpdatedState() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())

    val storeOp = cacheStore.storeDataAsync(updateInMemoryCache = false) { TEST_MESSAGE_V1 }
    testCoroutineDispatchers.advanceUntilIdle()

    // The store operation should be completed, but the observer will receive the updated state
    // since the cache wasn't primed and no previous observers initialized it.
    // NB: This may not be ideal behavior long-term; the store may need to be updated to be more
    // resilient to these types of scenarios.
    assertThat(storeOp.isCompleted).isTrue()
    assertThat(monitorFactory.waitForNextSuccessfulResult(cacheStore)).isEqualTo(TEST_MESSAGE_V1)
  }

  @Test
  fun testCache_updated_newCache_newObserver_observesNewValue() {
    val cacheStore1 = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    val storeOp = cacheStore1.storeDataAsync { TEST_MESSAGE_V1 }
    testCoroutineDispatchers.advanceUntilIdle()

    // Create a new cache with the same name.
    val cacheStore2 = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())

    // The new cache should have the updated value since it points to the same file as the first
    // cache. This is simulating something closer to an app restart or non-UI Dagger component
    // refresh since UI components should share the same cache instance via an application-bound
    // controller object.
    assertThat(storeOp.isCompleted).isTrue()
    assertThat(monitorFactory.waitForNextSuccessfulResult(cacheStore2)).isEqualTo(TEST_MESSAGE_V1)
  }

  @Test
  fun testCache_updated_noInMemoryCacheUpdate_newCache_newObserver_observesNewVal() {
    val cacheStore1 = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    val storeOp = cacheStore1.storeDataAsync(updateInMemoryCache = false) { TEST_MESSAGE_V1 }
    testCoroutineDispatchers.advanceUntilIdle()

    // Create a new cache with the same name.
    val cacheStore2 = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())

    // The new cache should have the updated value since it points to the same file as the first
    // cache, even though the update operation did not update the in-memory cache (the new cache has
    // a separate in-memory cache). This is simulating something closer to an app restart or non-UI
    // Dagger component refresh since UI components should share the same cache instance via an
    // application-bound controller object.
    assertThat(storeOp.isCompleted).isTrue()
    assertThat(monitorFactory.waitForNextSuccessfulResult(cacheStore2)).isEqualTo(TEST_MESSAGE_V1)
  }

  @Test
  fun testExistingDiskCache_newCacheObject_updateNoMemThenRead_receivesNewValue() {
    val cacheStore1 = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    val storeOp1 =
      cacheStore1.storeDataAsync(updateInMemoryCache = false) { TEST_MESSAGE_V1 }
    testCoroutineDispatchers.advanceUntilIdle()

    // Create a new cache with the same name and update it, then observe it.
    val cacheStore2 = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    val storeOp2 = cacheStore2.storeDataAsync(updateInMemoryCache = false) { TEST_MESSAGE_V2 }
    testCoroutineDispatchers.advanceUntilIdle()

    // Both operations should be complete, and the observer will receive the latest value since the
    // update was posted before the read occurred.
    assertThat(storeOp1.isCompleted).isTrue()
    assertThat(storeOp2.isCompleted).isTrue()
    assertThat(monitorFactory.waitForNextSuccessfulResult(cacheStore2)).isEqualTo(TEST_MESSAGE_V2)
  }

  @Test
  fun testExistingDiskCache_newObject_updateNoMemThenRead_primed_receivesPrevVal() {
    val cacheStore1 = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    val storeOp1 =
      cacheStore1.storeDataAsync(updateInMemoryCache = false) { TEST_MESSAGE_V1 }
    testCoroutineDispatchers.advanceUntilIdle()

    // Create a new cache with the same name and update it, then observe it. However, first prime
    // it.
    val cacheStore2 = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    val primeOp = cacheStore2.primeCacheAsync()
    testCoroutineDispatchers.advanceUntilIdle()
    val storeOp2 = cacheStore2.storeDataAsync(updateInMemoryCache = false) { TEST_MESSAGE_V2 }
    testCoroutineDispatchers.advanceUntilIdle()

    // All operations should be complete, but the observer will receive the previous update rather
    // than the latest since it wasn't updated in memory and the cache was pre-primed.
    assertThat(storeOp1.isCompleted).isTrue()
    assertThat(storeOp2.isCompleted).isTrue()
    assertThat(primeOp.isCompleted).isTrue()
    assertThat(monitorFactory.waitForNextSuccessfulResult(cacheStore2)).isEqualTo(TEST_MESSAGE_V1)
  }

  @Test
  fun testExistingDiskCache_newObject_updateMemThenRead_primed_receivesNewVal() {
    val cacheStore1 = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    val storeOp1 =
      cacheStore1.storeDataAsync(updateInMemoryCache = false) { TEST_MESSAGE_V1 }
    testCoroutineDispatchers.advanceUntilIdle()

    // Create a new cache with the same name and update it, then observe it. However, first prime
    // it.
    val cacheStore2 = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    val primeOp = cacheStore2.primeCacheAsync()
    testCoroutineDispatchers.advanceUntilIdle()
    val storeOp2 = cacheStore2.storeDataAsync { TEST_MESSAGE_V2 }
    testCoroutineDispatchers.advanceUntilIdle()

    // Similar to the previous test, except due to the in-memory update the observer will receive
    // the latest result regardless of the cache priming.
    assertThat(storeOp1.isCompleted).isTrue()
    assertThat(storeOp2.isCompleted).isTrue()
    assertThat(primeOp.isCompleted).isTrue()
    assertThat(monitorFactory.waitForNextSuccessfulResult(cacheStore2)).isEqualTo(TEST_MESSAGE_V2)
  }

  @Test
  fun testCache_primed_afterStoreUpdateWithoutMemUpdate_notForced_observesOldValue() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    // Force initializing the store's in-memory cache
    monitorFactory.waitForNextSuccessfulResult(cacheStore)

    val storeOp = cacheStore.storeDataAsync(updateInMemoryCache = false) { TEST_MESSAGE_V1 }
    testCoroutineDispatchers.advanceUntilIdle()
    val primeOp = cacheStore.primeCacheAsync(forceUpdate = false)
    testCoroutineDispatchers.advanceUntilIdle()

    // Both ops will succeed, and the observer will receive the old value due to the update not
    // changing the in-memory cache, and the prime no-oping due to the cache already being
    // initialized.
    assertThat(storeOp.isCompleted).isTrue()
    assertThat(primeOp.isCompleted).isTrue()
    assertThat(monitorFactory.waitForNextSuccessfulResult(cacheStore)).isEqualToDefaultInstance()
  }

  @Test
  fun testCache_primed_afterStoreUpdateWithoutMemoryUpdate_forced_observesNewValue() {
    val cacheStore = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    monitorFactory.waitForNextSuccessfulResult(cacheStore)

    val storeOp = cacheStore.storeDataAsync(updateInMemoryCache = false) { TEST_MESSAGE_V1 }
    testCoroutineDispatchers.advanceUntilIdle()
    val primeOp = cacheStore.primeCacheAsync(forceUpdate = true)
    testCoroutineDispatchers.advanceUntilIdle()

    // The observer will receive the new value because the prime was forced. This ensures the
    // store's in-memory cache is now up-to-date with the on-disk representation.
    assertThat(storeOp.isCompleted).isTrue()
    assertThat(primeOp.isCompleted).isTrue()
    assertThat(monitorFactory.waitForNextSuccessfulResult(cacheStore)).isEqualTo(TEST_MESSAGE_V1)
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
    val storeOp = cacheStore.storeDataAsync { TEST_MESSAGE_V1 }
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
    val storeOp = cacheStore.storeDataAsync { TEST_MESSAGE_V1 }
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
    val storeOp = cacheStore1.storeDataAsync { TEST_MESSAGE_V1 }
    testCoroutineDispatchers.advanceUntilIdle()
    val clearOp = cacheStore1.clearCacheAsync()
    testCoroutineDispatchers.advanceUntilIdle()

    val cacheStore2 = cacheFactory.create(CACHE_NAME_1, TEST_MESSAGE_V2)

    // The new observer should observe that there's no persisted on-disk store since it has a
    // different default value that would only be used if there wasn't already on-disk storage.
    assertThat(storeOp.isCompleted).isTrue()
    assertThat(clearOp.isCompleted).isTrue()
    assertThat(monitorFactory.waitForNextSuccessfulResult(cacheStore2)).isEqualTo(TEST_MESSAGE_V2)
  }

  @Test
  fun testMultipleCaches_oneUpdates_newCacheSameNameDiffInit_observesUpdatedValue() {
    val cacheStore1 = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    val storeOp = cacheStore1.storeDataAsync { TEST_MESSAGE_V1 }
    testCoroutineDispatchers.advanceUntilIdle()

    val cacheStore2 = cacheFactory.create(CACHE_NAME_1, TEST_MESSAGE_V2)

    // The new cache should observe the updated on-disk value rather than its new default since an
    // on-disk value exists. This isn't a very realistic test since all caches should use default
    // proto instances for initialization, but it's a possible edge case that should at least have
    // established behavior that can be adjusted later if it isn't desirable in some circumstances.
    assertThat(storeOp.isCompleted).isTrue()
    assertThat(monitorFactory.waitForNextSuccessfulResult(cacheStore2)).isEqualTo(TEST_MESSAGE_V1)
  }

  @Test
  fun testMultipleCaches_differentNames_oneUpdates_otherDoesNotObserveChange() {
    val cacheStore1 = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    val cacheStore2 = cacheFactory.create(CACHE_NAME_2, TestMessage.getDefaultInstance())

    val monitor1 = monitorFactory.createMonitor(cacheStore1)
    val monitor2 = monitorFactory.createMonitor(cacheStore2)
    val storeOp = cacheStore1.storeDataAsync { TEST_MESSAGE_V1 }
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
    val storeOp = cacheStore1a.storeDataAsync { TEST_MESSAGE_V1 }
    testCoroutineDispatchers.advanceUntilIdle()

    // Recreate the stores and observe them.
    val cacheStore1b = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    val cacheStore2b = cacheFactory.create(CACHE_NAME_2, TestMessage.getDefaultInstance())

    // Only the observer of the first cache should notice the update since they are different
    // caches.
    assertThat(storeOp.isCompleted).isTrue()
    assertThat(monitorFactory.waitForNextSuccessfulResult(cacheStore1b)).isEqualTo(TEST_MESSAGE_V1)
    assertThat(monitorFactory.waitForNextSuccessfulResult(cacheStore2b)).isEqualToDefaultInstance()
  }

  @Test
  fun testNewCache_fileCorrupted_providesError() {
    val cacheStore1 = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())
    val storeOp = cacheStore1.storeDataAsync { TEST_MESSAGE_V1 }
    testCoroutineDispatchers.advanceUntilIdle()

    // Simulate the file being corrupted & reopen the file in a new store.
    corruptFileCache(CACHE_NAME_1)
    val cacheStore2 = cacheFactory.create(CACHE_NAME_1, TestMessage.getDefaultInstance())

    // The new observer should receive an IOException error when trying to read the file.
    assertThat(storeOp.isCompleted).isTrue()
    val error = monitorFactory.waitForNextFailureResult(cacheStore2)
    assertThat(error).isInstanceOf(IOException::class.java)
  }

  private fun corruptFileCache(cacheName: String) {
    // NB: This is unfortunately tied to the implementation details of PersistentCacheStore. If this
    // ends up being an issue, the store should be updated to call into a file path provider that
    // can also be used in this test to retrieve the file cache. This may also be needed for
    // downstream profile work if per-profile data stores are done via subdirectories or altered
    // filenames.
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
}
