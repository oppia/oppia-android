package org.oppia.util.data

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.testing.TestCoroutineDispatcher
import org.oppia.testing.TestCoroutineDispatchers
import org.oppia.testing.TestDispatcherModule
import org.robolectric.annotation.Config
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass
import kotlin.reflect.full.cast
import kotlin.test.fail

private const val INITIALIZED_CACHE_VALUE = "inited cache value"
private const val CREATED_CACHE_VALUE = "created cache value"
private const val RECREATED_CACHE_VALUE = "recreated cache value"
private const val CREATED_ASYNC_VALUE = "created async value"
private const val UPDATED_ASYNC_VALUE = "updated async value"

/** Tests for [InMemoryBlockingCache]. */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class InMemoryBlockingCacheTest {
  @Inject
  lateinit var cacheFactory: InMemoryBlockingCache.Factory

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var testCoroutineDispatcherFactory: TestCoroutineDispatcher.Factory

  private val blockingFunctionDispatcher by lazy {
    testCoroutineDispatcherFactory.createDispatcher(
      Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    )
  }

  private val blockingFunctionDispatcherScope by lazy { CoroutineScope(blockingFunctionDispatcher) }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testReadCache_withoutInitialValue_providesNull() {
    val cache = cacheFactory.create<String>()

    val cachedValue = awaitCompletion(cache.readAsync())

    assertThat(cachedValue).isNull()
  }

  @Test
  fun testReadCache_withInitialValue_providesInitialValue() {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)

    val cachedValue = awaitCompletion(cache.readAsync())

    assertThat(cachedValue).isEqualTo(INITIALIZED_CACHE_VALUE)
  }

  @Test
  fun testCreateCache_withoutInitialValue_returnsCreatedValue() {
    val cache = cacheFactory.create<String>()

    val createResult = cache.createAsync(CREATED_CACHE_VALUE)

    assertThat(awaitCompletion(createResult)).isEqualTo(CREATED_CACHE_VALUE)
  }

  @Test
  fun testCreateCache_withoutInitialValue_setsValueOfCache() {
    val cache = cacheFactory.create<String>()

    doNotAwaitCompletion(cache.createAsync(CREATED_CACHE_VALUE))

    assertThat(awaitCompletion(cache.readAsync())).isEqualTo(CREATED_CACHE_VALUE)
  }

  @Test
  fun testRecreateCache_withInitialValue_returnsCreatedValue() {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)

    val createResult = cache.createAsync(RECREATED_CACHE_VALUE)

    assertThat(awaitCompletion(createResult)).isEqualTo(RECREATED_CACHE_VALUE)
  }

  @Test
  fun testRecreateCache_withInitialValue_setsValueOfCache() {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)

    doNotAwaitCompletion(cache.createAsync(RECREATED_CACHE_VALUE))

    assertThat(awaitCompletion(cache.readAsync())).isEqualTo(RECREATED_CACHE_VALUE)
  }

  @Test
  fun testCreateIfAbsent_withoutInitialValue_returnsCreatedValue() {
    val cache = cacheFactory.create<String>()

    val createResult = cache.createIfAbsentAsync { CREATED_ASYNC_VALUE }

    assertThat(awaitCompletion(createResult)).isEqualTo(CREATED_ASYNC_VALUE)
  }

  @Test
  fun testCreateIfAbsent_withoutInitialValue_setsValueOfCache() {
    val cache = cacheFactory.create<String>()

    doNotAwaitCompletion(cache.createIfAbsentAsync { CREATED_ASYNC_VALUE })

    assertThat(awaitCompletion(cache.readAsync())).isEqualTo(CREATED_ASYNC_VALUE)
  }

  @Test
  fun testCreateIfAbsent_withInitialValue_returnsCurrentCacheValue() {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)

    val createResult = cache.createIfAbsentAsync { CREATED_ASYNC_VALUE }

    // Because the cache is already initialized, it's not recreated.
    assertThat(awaitCompletion(createResult)).isEqualTo(INITIALIZED_CACHE_VALUE)
  }

  @Test
  fun testCreateIfAbsent_withInitialValue_doesNotChangeCacheValue() {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)

    doNotAwaitCompletion(cache.createIfAbsentAsync { CREATED_ASYNC_VALUE })

    // Because the cache is already initialized, it's not recreated.
    assertThat(awaitCompletion(cache.readAsync())).isEqualTo(INITIALIZED_CACHE_VALUE)
  }

  @Test
  fun testCreateIfAbsent_emptyCache_blockingFunction_createIsNotComplete() {
    val cache = cacheFactory.create<String>()

    val blockingOperation = blockingFunctionDispatcherScope.async { CREATED_ASYNC_VALUE }
    val createOperation = cache.createIfAbsentAsync { blockingOperation.await() }
    testCoroutineDispatchers.runCurrent()

    // The blocking operation should also block creation.
    assertThat(createOperation.isCompleted).isFalse()
  }

  @Test
  fun testCreateIfAbsent_emptyCache_blockingFunction_completed_createCompletes() {
    val cache = cacheFactory.create<String>()
    val blockingOperation = blockingFunctionDispatcherScope.async { CREATED_ASYNC_VALUE }
    val createOperation = cache.createIfAbsentAsync { blockingOperation.await() }
    testCoroutineDispatchers.runCurrent()

    blockingFunctionDispatcher.runCurrent()
    testCoroutineDispatchers.runCurrent()

    // Completing the blocking operation should complete creation.
    assertThat(createOperation.isCompleted).isTrue()
  }

  @Test
  fun testReadIfPresent_withInitialValue_providesInitialValue() {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)

    val cachedValue = awaitCompletion(cache.readIfPresentAsync())

    assertThat(cachedValue).isEqualTo(INITIALIZED_CACHE_VALUE)
  }

  @Test
  fun testReadIfPresent_afterCreate_providesCachedValue() {
    val cache = cacheFactory.create<String>()
    doNotAwaitCompletion(cache.createAsync(CREATED_CACHE_VALUE))

    val cachedValue = awaitCompletion(cache.readIfPresentAsync())

    assertThat(cachedValue).isEqualTo(CREATED_CACHE_VALUE)
  }

  @Test
  fun testReadIfPresent_withoutInitialValue_throwsException() {
    val cache = cacheFactory.create<String>()

    val deferredRead = cache.readIfPresentAsync()

    val exception =
      assertThrows(IllegalStateException::class) { awaitCompletion(deferredRead) }
    assertThat(exception).hasMessageThat()
      .contains("Expected to read the cache only after it's been created")
  }

  @Test
  fun testUpdateCache_withoutInitialValue_returnsUpdatedValue() {
    val cache = cacheFactory.create<String>()

    val returnedValue = awaitCompletion(cache.updateAsync { UPDATED_ASYNC_VALUE })

    assertThat(returnedValue).isEqualTo(UPDATED_ASYNC_VALUE)
  }

  @Test
  fun testUpdateCache_withoutInitialValue_changesCachedValue() {
    val cache = cacheFactory.create<String>()

    doNotAwaitCompletion(cache.updateAsync { UPDATED_ASYNC_VALUE })

    assertThat(awaitCompletion(cache.readAsync())).isEqualTo(UPDATED_ASYNC_VALUE)
  }

  @Test
  fun testUpdateCache_withInitialValue_returnsUpdatedValue() {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)

    val returnedValue = awaitCompletion(cache.updateAsync { UPDATED_ASYNC_VALUE })

    assertThat(returnedValue).isEqualTo(UPDATED_ASYNC_VALUE)
  }

  @Test
  fun testUpdateCache_withInitialValue_changesCachedValue() {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)

    doNotAwaitCompletion(cache.updateAsync { UPDATED_ASYNC_VALUE })

    assertThat(awaitCompletion(cache.readAsync())).isEqualTo(UPDATED_ASYNC_VALUE)
  }

  @Test
  fun testUpdateCache_blockingFunction_blocksUpdate() {
    val cache = cacheFactory.create<String>()

    val blockingOperation = blockingFunctionDispatcherScope.async { UPDATED_ASYNC_VALUE }
    val updateOperation = cache.updateAsync { blockingOperation.await() }
    testCoroutineDispatchers.runCurrent()

    // The blocking operation should also block updating.
    assertThat(updateOperation.isCompleted).isFalse()
  }

  @Test
  fun testUpdateCache_blockingFunction_completed_updateCompletes() {
    val cache = cacheFactory.create<String>()
    val blockingOperation = blockingFunctionDispatcherScope.async { UPDATED_ASYNC_VALUE }
    val updateOperation = cache.updateAsync { blockingOperation.await() }
    testCoroutineDispatchers.runCurrent()

    blockingFunctionDispatcher.runCurrent()
    testCoroutineDispatchers.runCurrent()

    // Completing the blocking operation should complete updating.
    assertThat(updateOperation.isCompleted).isTrue()
  }

  @Test
  fun testUpdateIfPresent_withInitialValue_returnsUpdatedValue() {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)

    val returnedValue = awaitCompletion(cache.updateIfPresentAsync { UPDATED_ASYNC_VALUE })

    // Since the cache is initialized, it should be updated.
    assertThat(returnedValue).isEqualTo(UPDATED_ASYNC_VALUE)
  }

  @Test
  fun testUpdateIfPresent_withInitialValue_changesCachedValue() {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)

    doNotAwaitCompletion(cache.updateIfPresentAsync { UPDATED_ASYNC_VALUE })

    // Since the cache is initialized, it should be updated.
    assertThat(awaitCompletion(cache.readAsync())).isEqualTo(UPDATED_ASYNC_VALUE)
  }

  @Test
  fun testUpdateIfPresent_withoutInitialValue_throwsException() {
    val cache = cacheFactory.create<String>()

    val deferredUpdate = cache.updateIfPresentAsync { UPDATED_ASYNC_VALUE }

    // The operation should fail since the method expects the cache to be initialized.
    val exception =
      assertThrows(IllegalStateException::class) { awaitCompletion(deferredUpdate) }
    assertThat(exception).hasMessageThat()
      .contains("Expected to update the cache only after it's been created")
  }

  @Test
  fun testUpdateIfPresent_initedCache_blockingFunction_blocksUpdate() {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)

    val blockingOperation = blockingFunctionDispatcherScope.async { UPDATED_ASYNC_VALUE }
    val updateOperation = cache.updateIfPresentAsync { blockingOperation.await() }
    testCoroutineDispatchers.runCurrent()

    // The blocking operation should also block updating.
    assertThat(updateOperation.isCompleted).isFalse()
  }

  @Test
  fun testUpdateIfPresent_initedCache_blockingFunction_completed_updateCompletes() {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)
    val blockingOperation = blockingFunctionDispatcherScope.async { UPDATED_ASYNC_VALUE }
    val updateOperation = cache.updateIfPresentAsync { blockingOperation.await() }
    testCoroutineDispatchers.runCurrent()

    blockingFunctionDispatcher.runCurrent()
    testCoroutineDispatchers.runCurrent()

    // Completing the blocking operation should complete updating.
    assertThat(updateOperation.isCompleted).isTrue()
  }

  @Test
  fun testDeleteAsync_withoutInitialValue_keepsCacheNull() {
    val cache = cacheFactory.create<String>()

    doNotAwaitCompletion(cache.deleteAsync())

    assertThat(awaitCompletion(cache.readAsync())).isNull()
  }

  @Test
  fun testDeleteAsync_withInitialValue_setsCacheNull() {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)

    doNotAwaitCompletion(cache.deleteAsync())

    assertThat(awaitCompletion(cache.readAsync())).isNull()
  }

  @Test
  fun testDeleteAsync_withRecreatedValue_setsCacheNull() {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)
    doNotAwaitCompletion(cache.createAsync(RECREATED_CACHE_VALUE))

    doNotAwaitCompletion(cache.deleteAsync())

    assertThat(awaitCompletion(cache.readAsync())).isNull()
  }

  @Test
  fun testDeleteAsync_withUpdatedValue_setsCacheNull() {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)
    doNotAwaitCompletion(cache.updateAsync { UPDATED_ASYNC_VALUE })

    doNotAwaitCompletion(cache.deleteAsync())

    assertThat(awaitCompletion(cache.readAsync())).isNull()
  }

  @Test
  fun testRecreateCache_afterDeletion_returnsCreatedValue() {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)
    doNotAwaitCompletion(cache.deleteAsync())

    val createResult = cache.createAsync(RECREATED_CACHE_VALUE)

    assertThat(awaitCompletion(createResult)).isEqualTo(RECREATED_CACHE_VALUE)
  }

  @Test
  fun testRecreateCache_afterDeletion_setsValueOfCache() {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)
    doNotAwaitCompletion(cache.deleteAsync())

    doNotAwaitCompletion(cache.createAsync(RECREATED_CACHE_VALUE))

    assertThat(awaitCompletion(cache.readAsync())).isEqualTo(RECREATED_CACHE_VALUE)
  }

  @Test
  fun testCreateIfAbsent_afterDeletion_returnsCreatedValue() {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)
    doNotAwaitCompletion(cache.deleteAsync())

    val createResult = cache.createIfAbsentAsync { CREATED_ASYNC_VALUE }

    // Deleting the cache clears it to be recreated.
    assertThat(awaitCompletion(createResult)).isEqualTo(CREATED_ASYNC_VALUE)
  }

  @Test
  fun testCreateIfAbsent_afterDeletion_setsValueOfCache() {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)
    doNotAwaitCompletion(cache.deleteAsync())

    doNotAwaitCompletion(cache.createIfAbsentAsync { CREATED_ASYNC_VALUE })

    // Deleting the cache clears it to be recreated.
    assertThat(awaitCompletion(cache.readAsync())).isEqualTo(CREATED_ASYNC_VALUE)
  }

  @Test
  fun testReadIfPresent_afterDeletion_throwsException() {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)
    doNotAwaitCompletion(cache.deleteAsync())

    val deferredRead = cache.readIfPresentAsync()

    // Deleting the cache should result in readIfPresent()'s expectations to fail.
    val exception =
      assertThrows(IllegalStateException::class) { awaitCompletion(deferredRead) }
    assertThat(exception).hasMessageThat()
      .contains("Expected to read the cache only after it's been created")
  }

  @Test
  fun testUpdateCache_afterDeletion_returnsUpdatedValue() {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)
    doNotAwaitCompletion(cache.deleteAsync())

    val returnedValue = awaitCompletion(cache.updateAsync { UPDATED_ASYNC_VALUE })

    assertThat(returnedValue).isEqualTo(UPDATED_ASYNC_VALUE)
  }

  @Test
  fun testUpdateCache_afterDeletion_changesCachedValue() {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)
    doNotAwaitCompletion(cache.deleteAsync())

    doNotAwaitCompletion(cache.updateAsync { UPDATED_ASYNC_VALUE })

    assertThat(awaitCompletion(cache.readAsync())).isEqualTo(UPDATED_ASYNC_VALUE)
  }

  @Test
  fun testUpdateIfPresent_afterDeletion_throwsException() {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)
    doNotAwaitCompletion(cache.deleteAsync())

    val deferredUpdate = cache.updateIfPresentAsync { UPDATED_ASYNC_VALUE }

    // The operation should fail since the method expects the cache to be initialized.
    val exception =
      assertThrows(IllegalStateException::class) { awaitCompletion(deferredUpdate) }
    assertThat(exception).hasMessageThat()
      .contains("Expected to update the cache only after it's been created")
  }

  @Test
  fun testMaybeDelete_emptyCache_falsePredicate_returnsFalse() {
    val cache = cacheFactory.create<String>()

    val maybeDeleteResult = cache.maybeDeleteAsync { false }

    // An empty cache cannot be deleted.
    assertThat(awaitCompletion(maybeDeleteResult)).isFalse()
  }

  @Test
  fun testMaybeDelete_emptyCache_truePredicate_returnsFalse() {
    val cache = cacheFactory.create<String>()

    val maybeDeleteResult = cache.maybeDeleteAsync { true }

    // An empty cache cannot be deleted.
    assertThat(awaitCompletion(maybeDeleteResult)).isFalse()
  }

  @Test
  fun testMaybeDelete_emptyCache_keepsCacheNull() {
    val cache = cacheFactory.create<String>()

    doNotAwaitCompletion(cache.maybeDeleteAsync { true })

    // The empty cache should stay empty.
    assertThat(awaitCompletion(cache.readAsync())).isNull()
  }

  @Test
  fun testMaybeDelete_nonEmptyCache_falsePredicate_returnsFalse() {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)

    val maybeDeleteResult = cache.maybeDeleteAsync { false }

    // The predicate's false return value should be piped up to the deletion result.
    assertThat(awaitCompletion(maybeDeleteResult)).isFalse()
  }

  @Test
  fun testMaybeDelete_nonEmptyCache_falsePredicate_keepsCacheNonEmpty() {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)

    doNotAwaitCompletion(cache.maybeDeleteAsync { false })

    // The cache should retain its value since the deletion predicate indicated it shouldn't be cleared.
    assertThat(awaitCompletion(cache.readAsync())).isEqualTo(INITIALIZED_CACHE_VALUE)
  }

  @Test
  fun testMaybeDelete_nonEmptyCache_truePredicate_returnsTrue() {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)

    val maybeDeleteResult = cache.maybeDeleteAsync { true }

    // The predicate's true return value should be piped up to the deletion result.
    assertThat(awaitCompletion(maybeDeleteResult)).isTrue()
  }

  @Test
  fun testMaybeDelete_nonEmptyCache_truePredicate_emptiesCache() {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)

    doNotAwaitCompletion(cache.maybeDeleteAsync { true })

    // The cache should be emptied as indicated by the deletion predicate.
    assertThat(awaitCompletion(cache.readAsync())).isNull()
  }

  @Test
  fun testMaybeDelete_blockingFunction_blocksDeletion() {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)

    val blockingOperation = blockingFunctionDispatcherScope.async { true }
    val deleteOperation = cache.maybeDeleteAsync { blockingOperation.await() }
    testCoroutineDispatchers.runCurrent()

    // The blocking operation should also block deletion.
    assertThat(deleteOperation.isCompleted).isFalse()
  }

  @Test
  fun testMaybeDelete_blockingFunction_completed_deletionCompletes() {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)
    val blockingOperation = blockingFunctionDispatcherScope.async { true }
    val deleteOperation = cache.maybeDeleteAsync { blockingOperation.await() }
    testCoroutineDispatchers.runCurrent()

    blockingFunctionDispatcher.runCurrent()
    testCoroutineDispatchers.runCurrent()

    // Completing the blocking operation should complete deletion.
    assertThat(deleteOperation.isCompleted).isTrue()
  }

  @Test
  fun testMaybeForceDelete_emptyCache_falsePredicate_returnsFalse() {
    val cache = cacheFactory.create<String>()

    val maybeDeleteResult = cache.maybeForceDeleteAsync { false }

    // An empty cache cannot be deleted.
    assertThat(awaitCompletion(maybeDeleteResult)).isFalse()
  }

  @Test
  fun testMaybeForceDelete_emptyCache_truePredicate_returnsTrue() {
    val cache = cacheFactory.create<String>()

    val maybeDeleteResult = cache.maybeForceDeleteAsync { true }

    // An empty cache cannot be deleted, but with force deletion the state of the cache is not checked. It's assumed
    // that the cache was definitely cleared.
    assertThat(awaitCompletion(maybeDeleteResult)).isTrue()
  }

  @Test
  fun testMaybeForceDelete_emptyCache_keepsCacheNull() {
    val cache = cacheFactory.create<String>()

    doNotAwaitCompletion(cache.maybeForceDeleteAsync { true })

    // The empty cache should stay empty.
    assertThat(awaitCompletion(cache.readAsync())).isNull()
  }

  @Test
  fun testMaybeForceDelete_nonEmptyCache_falsePredicate_returnsFalse() {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)

    val maybeDeleteResult = cache.maybeForceDeleteAsync { false }

    // The predicate's false return value should be piped up to the deletion result.
    assertThat(awaitCompletion(maybeDeleteResult)).isFalse()
  }

  @Test
  fun testMaybeForceDelete_nonEmptyCache_falsePredicate_keepsCacheNonEmpty() {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)

    doNotAwaitCompletion(cache.maybeForceDeleteAsync { false })

    // The cache should retain its value since the deletion predicate indicated it shouldn't be cleared.
    assertThat(awaitCompletion(cache.readAsync())).isEqualTo(INITIALIZED_CACHE_VALUE)
  }

  @Test
  fun testMaybeForceDelete_nonEmptyCache_truePredicate_returnsTrue() {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)

    val maybeDeleteResult = cache.maybeForceDeleteAsync { true }

    // The predicate's true return value should be piped up to the deletion result.
    assertThat(awaitCompletion(maybeDeleteResult)).isTrue()
  }

  @Test
  fun testMaybeForceDelete_nonEmptyCache_truePredicate_emptiesCache() {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)

    doNotAwaitCompletion(cache.maybeForceDeleteAsync { true })

    // The cache should be emptied as indicated by the deletion predicate.
    assertThat(awaitCompletion(cache.readAsync())).isNull()
  }

  @Test
  fun testMaybeForceDelete_blockingFunction_blocksDeletion() {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)

    val blockingOperation = blockingFunctionDispatcherScope.async { true }
    val deleteOperation = cache.maybeForceDeleteAsync { blockingOperation.await() }
    testCoroutineDispatchers.runCurrent()

    // The blocking operation should also block deletion.
    assertThat(deleteOperation.isCompleted).isFalse()
  }

  @Test
  fun testMaybeForceDelete_blockingFunction_completed_deletionCompletes() {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)
    val blockingOperation = blockingFunctionDispatcherScope.async { true }
    val deleteOperation = cache.maybeForceDeleteAsync { blockingOperation.await() }
    testCoroutineDispatchers.runCurrent()

    blockingFunctionDispatcher.runCurrent()
    testCoroutineDispatchers.runCurrent()

    // Completing the blocking operation should complete deletion.
    assertThat(deleteOperation.isCompleted).isTrue()
  }

  /**
   * Silences the warning that [Deferred] is unused. This is okay for tests that ensure await() is
   * called at the end of the test since the cache guarantees sequential execution.
   */
  private fun <T> doNotAwaitCompletion(
    @Suppress("UNUSED_PARAMETER") deferred: Deferred<T>
  ) {
  }

  /**
   * Waits for the specified deferred to execute after advancing test dispatcher. Without this
   * function, results cannot be observed from cache operations.
   */
  @Suppress("EXPERIMENTAL_API_USAGE")
  private fun <T> awaitCompletion(deferred: Deferred<T>): T {
    testCoroutineDispatchers.runCurrent()
    return deferred.getCompleted()
  }

  // TODO(#89): Move to a common test library.
  /** A replacement to JUnit5's assertThrows() with Kotlin lambda support. */
  private fun <T : Throwable> assertThrows(
    type: KClass<T>,
    operation: () -> Unit
  ): T {
    try {
      operation()
      fail("Expected to encounter exception of $type")
    } catch (t: Throwable) {
      if (type.isInstance(t)) {
        return type.cast(t)
      }
      // Unexpected exception; throw it.
      throw t
    }
  }

  private fun setUpTestApplicationComponent() {
    DaggerInMemoryBlockingCacheTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
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
  @Component(modules = [TestModule::class, TestDispatcherModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(inMemoryBlockingCacheTest: InMemoryBlockingCacheTest)
  }
}
