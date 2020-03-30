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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.util.threading.BlockingDispatcher
import org.robolectric.annotation.Config
import javax.inject.Inject
import javax.inject.Qualifier
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
  @Inject lateinit var cacheFactory: InMemoryBlockingCache.Factory

  @ExperimentalCoroutinesApi
  @Inject
  @field:TestDispatcher
  lateinit var testDispatcher: TestCoroutineDispatcher

  // TODO(#89): Remove the need for this custom scope by allowing tests to instead rely on rely background dispatchers.
  /**
   * A [CoroutineScope] with a dispatcher that ensures its corresponding task is run on a background thread rather than
   * synchronously on the test thread, allowing blocking operations.
   */
  @ExperimentalCoroutinesApi
  private val backgroundTestCoroutineScope by lazy {
    CoroutineScope(backgroundTestCoroutineDispatcher)
  }

  @ExperimentalCoroutinesApi
  private val backgroundTestCoroutineDispatcher by lazy {
    TestCoroutineDispatcher()
  }

  @Before
  @ExperimentalCoroutinesApi
  fun setUp() {
    setUpTestApplicationComponent()
    // Intentionally pause the test dispatcher to help test that the blocking cache's order is sequential even if
    // multiple operations are stacked up and executed in quick succession.
    testDispatcher.pauseDispatcher()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testReadCache_withoutInitialValue_providesNull() = runBlockingTest(testDispatcher) {
    val cache = cacheFactory.create<String>()

    val cachedValue = awaitCompletion(cache.readAsync())

    assertThat(cachedValue).isNull()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testReadCache_withInitialValue_providesInitialValue() = runBlockingTest(testDispatcher) {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)

    val cachedValue = awaitCompletion(cache.readAsync())

    assertThat(cachedValue).isEqualTo(INITIALIZED_CACHE_VALUE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testCreateCache_withoutInitialValue_returnsCreatedValue() = runBlockingTest(testDispatcher) {
    val cache = cacheFactory.create<String>()

    val createResult = cache.createAsync(CREATED_CACHE_VALUE)

    assertThat(awaitCompletion(createResult)).isEqualTo(CREATED_CACHE_VALUE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testCreateCache_withoutInitialValue_setsValueOfCache() = runBlockingTest(testDispatcher) {
    val cache = cacheFactory.create<String>()

    doNotAwaitCompletion(cache.createAsync(CREATED_CACHE_VALUE))

    assertThat(awaitCompletion(cache.readAsync())).isEqualTo(CREATED_CACHE_VALUE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testRecreateCache_withInitialValue_returnsCreatedValue() = runBlockingTest(testDispatcher) {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)

    val createResult = cache.createAsync(RECREATED_CACHE_VALUE)

    assertThat(awaitCompletion(createResult)).isEqualTo(RECREATED_CACHE_VALUE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testRecreateCache_withInitialValue_setsValueOfCache() = runBlockingTest(testDispatcher) {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)

    doNotAwaitCompletion(cache.createAsync(RECREATED_CACHE_VALUE))

    assertThat(awaitCompletion(cache.readAsync())).isEqualTo(RECREATED_CACHE_VALUE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testCreateIfAbsent_withoutInitialValue_returnsCreatedValue() = runBlockingTest(testDispatcher) {
    val cache = cacheFactory.create<String>()

    val createResult = cache.createIfAbsentAsync { CREATED_ASYNC_VALUE }

    assertThat(awaitCompletion(createResult)).isEqualTo(CREATED_ASYNC_VALUE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testCreateIfAbsent_withoutInitialValue_setsValueOfCache() = runBlockingTest(testDispatcher) {
    val cache = cacheFactory.create<String>()

    doNotAwaitCompletion(cache.createIfAbsentAsync { CREATED_ASYNC_VALUE })

    assertThat(awaitCompletion(cache.readAsync())).isEqualTo(CREATED_ASYNC_VALUE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testCreateIfAbsent_withInitialValue_returnsCurrentCacheValue() = runBlockingTest(testDispatcher) {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)

    val createResult = cache.createIfAbsentAsync { CREATED_ASYNC_VALUE }

    // Because the cache is already initialized, it's not recreated.
    assertThat(awaitCompletion(createResult)).isEqualTo(INITIALIZED_CACHE_VALUE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testCreateIfAbsent_withInitialValue_doesNotChangeCacheValue() = runBlockingTest(testDispatcher) {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)

    doNotAwaitCompletion(cache.createIfAbsentAsync { CREATED_ASYNC_VALUE })

    // Because the cache is already initialized, it's not recreated.
    assertThat(awaitCompletion(cache.readAsync())).isEqualTo(INITIALIZED_CACHE_VALUE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testCreateIfAbsent_emptyCache_blockingFunction_createIsNotComplete() = runBlockingTest(testDispatcher) {
    testDispatcher.resumeDispatcher() // Keep the test dispatcher active since this test is verifying blocking behavior.
    val cache = cacheFactory.create<String>()
    backgroundTestCoroutineDispatcher.pauseDispatcher()

    val blockingOperation = backgroundTestCoroutineScope.async { CREATED_ASYNC_VALUE }
    val createOperation = cache.createIfAbsentAsync { blockingOperation.await() }

    // The blocking operation should also block creation.
    assertThat(createOperation.isCompleted).isFalse()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testCreateIfAbsent_emptyCache_blockingFunction_completed_createCompletes() = runBlockingTest(testDispatcher) {
    testDispatcher.resumeDispatcher() // Keep the test dispatcher active since this test is verifying blocking behavior.
    val cache = cacheFactory.create<String>()
    backgroundTestCoroutineDispatcher.pauseDispatcher()
    val blockingOperation = backgroundTestCoroutineScope.async { CREATED_ASYNC_VALUE }
    val createOperation = cache.createIfAbsentAsync { blockingOperation.await() }

    backgroundTestCoroutineDispatcher.advanceUntilIdle()

    // Completing the blocking operation should complete creation.
    assertThat(createOperation.isCompleted).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testReadIfPresent_withInitialValue_providesInitialValue() = runBlockingTest(testDispatcher) {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)

    val cachedValue = awaitCompletion(cache.readIfPresentAsync())

    assertThat(cachedValue).isEqualTo(INITIALIZED_CACHE_VALUE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testReadIfPresent_afterCreate_providesCachedValue() = runBlockingTest(testDispatcher) {
    val cache = cacheFactory.create<String>()
    doNotAwaitCompletion(cache.createAsync(CREATED_CACHE_VALUE))

    val cachedValue = awaitCompletion(cache.readIfPresentAsync())

    assertThat(cachedValue).isEqualTo(CREATED_CACHE_VALUE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testReadIfPresent_withoutInitialValue_throwsException() = runBlockingTest(testDispatcher) {
    val cache = cacheFactory.create<String>()

    val deferredRead = cache.readIfPresentAsync()

    val exception = assertThrowsAsync(IllegalStateException::class) { awaitCompletion(deferredRead) }
    assertThat(exception).hasMessageThat().contains("Expected to read the cache only after it's been created")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testUpdateCache_withoutInitialValue_returnsUpdatedValue() = runBlockingTest(testDispatcher) {
    val cache = cacheFactory.create<String>()

    val returnedValue = awaitCompletion(cache.updateAsync { UPDATED_ASYNC_VALUE })

    assertThat(returnedValue).isEqualTo(UPDATED_ASYNC_VALUE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testUpdateCache_withoutInitialValue_changesCachedValue() = runBlockingTest(testDispatcher) {
    val cache = cacheFactory.create<String>()

    doNotAwaitCompletion(cache.updateAsync { UPDATED_ASYNC_VALUE })

    assertThat(awaitCompletion(cache.readAsync())).isEqualTo(UPDATED_ASYNC_VALUE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testUpdateCache_withInitialValue_returnsUpdatedValue() = runBlockingTest(testDispatcher) {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)

    val returnedValue = awaitCompletion(cache.updateAsync { UPDATED_ASYNC_VALUE })

    assertThat(returnedValue).isEqualTo(UPDATED_ASYNC_VALUE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testUpdateCache_withInitialValue_changesCachedValue() = runBlockingTest(testDispatcher) {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)

    doNotAwaitCompletion(cache.updateAsync { UPDATED_ASYNC_VALUE })

    assertThat(awaitCompletion(cache.readAsync())).isEqualTo(UPDATED_ASYNC_VALUE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testUpdateCache_blockingFunction_blocksUpdate() = runBlockingTest(testDispatcher) {
    testDispatcher.resumeDispatcher() // Keep the test dispatcher active since this test is verifying blocking behavior.
    val cache = cacheFactory.create<String>()
    backgroundTestCoroutineDispatcher.pauseDispatcher()

    val blockingOperation = backgroundTestCoroutineScope.async { UPDATED_ASYNC_VALUE }
    val updateOperation = cache.updateAsync { blockingOperation.await() }

    // The blocking operation should also block updating.
    assertThat(updateOperation.isCompleted).isFalse()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testUpdateCache_blockingFunction_completed_updateCompletes() = runBlockingTest(testDispatcher) {
    testDispatcher.resumeDispatcher() // Keep the test dispatcher active since this test is verifying blocking behavior.
    val cache = cacheFactory.create<String>()
    backgroundTestCoroutineDispatcher.pauseDispatcher()
    val blockingOperation = backgroundTestCoroutineScope.async { UPDATED_ASYNC_VALUE }
    val updateOperation = cache.updateAsync { blockingOperation.await() }

    backgroundTestCoroutineDispatcher.advanceUntilIdle()

    // Completing the blocking operation should complete updating.
    assertThat(updateOperation.isCompleted).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testUpdateIfPresent_withInitialValue_returnsUpdatedValue() = runBlockingTest(testDispatcher) {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)

    val returnedValue = awaitCompletion(cache.updateIfPresentAsync { UPDATED_ASYNC_VALUE })

    // Since the cache is initialized, it should be updated.
    assertThat(returnedValue).isEqualTo(UPDATED_ASYNC_VALUE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testUpdateIfPresent_withInitialValue_changesCachedValue() = runBlockingTest(testDispatcher) {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)

    doNotAwaitCompletion(cache.updateIfPresentAsync { UPDATED_ASYNC_VALUE })

    // Since the cache is initialized, it should be updated.
    assertThat(awaitCompletion(cache.readAsync())).isEqualTo(UPDATED_ASYNC_VALUE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testUpdateIfPresent_withoutInitialValue_throwsException() = runBlockingTest(testDispatcher) {
    val cache = cacheFactory.create<String>()

    val deferredUpdate = cache.updateIfPresentAsync { UPDATED_ASYNC_VALUE }

    // The operation should fail since the method expects the cache to be initialized.
    val exception = assertThrowsAsync(IllegalStateException::class) { awaitCompletion(deferredUpdate) }
    assertThat(exception).hasMessageThat().contains("Expected to update the cache only after it's been created")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testUpdateIfPresent_initedCache_blockingFunction_blocksUpdate() = runBlockingTest(testDispatcher) {
    testDispatcher.resumeDispatcher() // Keep the test dispatcher active since this test is verifying blocking behavior.
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)
    backgroundTestCoroutineDispatcher.pauseDispatcher()

    val blockingOperation = backgroundTestCoroutineScope.async { UPDATED_ASYNC_VALUE }
    val updateOperation = cache.updateIfPresentAsync { blockingOperation.await() }

    // The blocking operation should also block updating.
    assertThat(updateOperation.isCompleted).isFalse()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testUpdateIfPresent_initedCache_blockingFunction_completed_updateCompletes() = runBlockingTest(testDispatcher) {
    testDispatcher.resumeDispatcher() // Keep the test dispatcher active since this test is verifying blocking behavior.
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)
    backgroundTestCoroutineDispatcher.pauseDispatcher()
    val blockingOperation = backgroundTestCoroutineScope.async { UPDATED_ASYNC_VALUE }
    val updateOperation = cache.updateIfPresentAsync { blockingOperation.await() }

    backgroundTestCoroutineDispatcher.advanceUntilIdle()

    // Completing the blocking operation should complete updating.
    assertThat(updateOperation.isCompleted).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testDeleteAsync_withoutInitialValue_keepsCacheNull() = runBlockingTest(testDispatcher) {
    val cache = cacheFactory.create<String>()

    doNotAwaitCompletion(cache.deleteAsync())

    assertThat(awaitCompletion(cache.readAsync())).isNull()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testDeleteAsync_withInitialValue_setsCacheNull() = runBlockingTest(testDispatcher) {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)

    doNotAwaitCompletion(cache.deleteAsync())

    assertThat(awaitCompletion(cache.readAsync())).isNull()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testDeleteAsync_withRecreatedValue_setsCacheNull() = runBlockingTest(testDispatcher) {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)
    doNotAwaitCompletion(cache.createAsync(RECREATED_CACHE_VALUE))

    doNotAwaitCompletion(cache.deleteAsync())

    assertThat(awaitCompletion(cache.readAsync())).isNull()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testDeleteAsync_withUpdatedValue_setsCacheNull() = runBlockingTest(testDispatcher) {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)
    doNotAwaitCompletion(cache.updateAsync { UPDATED_ASYNC_VALUE })

    doNotAwaitCompletion(cache.deleteAsync())

    assertThat(awaitCompletion(cache.readAsync())).isNull()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testRecreateCache_afterDeletion_returnsCreatedValue() = runBlockingTest(testDispatcher) {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)
    doNotAwaitCompletion(cache.deleteAsync())

    val createResult = cache.createAsync(RECREATED_CACHE_VALUE)

    assertThat(awaitCompletion(createResult)).isEqualTo(RECREATED_CACHE_VALUE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testRecreateCache_afterDeletion_setsValueOfCache() = runBlockingTest(testDispatcher) {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)
    doNotAwaitCompletion(cache.deleteAsync())

    doNotAwaitCompletion(cache.createAsync(RECREATED_CACHE_VALUE))

    assertThat(awaitCompletion(cache.readAsync())).isEqualTo(RECREATED_CACHE_VALUE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testCreateIfAbsent_afterDeletion_returnsCreatedValue() = runBlockingTest(testDispatcher) {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)
    doNotAwaitCompletion(cache.deleteAsync())

    val createResult = cache.createIfAbsentAsync { CREATED_ASYNC_VALUE }

    // Deleting the cache clears it to be recreated.
    assertThat(awaitCompletion(createResult)).isEqualTo(CREATED_ASYNC_VALUE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testCreateIfAbsent_afterDeletion_setsValueOfCache() = runBlockingTest(testDispatcher) {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)
    doNotAwaitCompletion(cache.deleteAsync())

    doNotAwaitCompletion(cache.createIfAbsentAsync { CREATED_ASYNC_VALUE })

    // Deleting the cache clears it to be recreated.
    assertThat(awaitCompletion(cache.readAsync())).isEqualTo(CREATED_ASYNC_VALUE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testReadIfPresent_afterDeletion_throwsException() = runBlockingTest(testDispatcher) {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)
    doNotAwaitCompletion(cache.deleteAsync())

    val deferredRead = cache.readIfPresentAsync()

    // Deleting the cache should result in readIfPresent()'s expectations to fail.
    val exception = assertThrowsAsync(IllegalStateException::class) { awaitCompletion(deferredRead) }
    assertThat(exception).hasMessageThat().contains("Expected to read the cache only after it's been created")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testUpdateCache_afterDeletion_returnsUpdatedValue() = runBlockingTest(testDispatcher) {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)
    doNotAwaitCompletion(cache.deleteAsync())

    val returnedValue = awaitCompletion(cache.updateAsync { UPDATED_ASYNC_VALUE })

    assertThat(returnedValue).isEqualTo(UPDATED_ASYNC_VALUE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testUpdateCache_afterDeletion_changesCachedValue() = runBlockingTest(testDispatcher) {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)
    doNotAwaitCompletion(cache.deleteAsync())

    doNotAwaitCompletion(cache.updateAsync { UPDATED_ASYNC_VALUE })

    assertThat(awaitCompletion(cache.readAsync())).isEqualTo(UPDATED_ASYNC_VALUE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testUpdateIfPresent_afterDeletion_throwsException() = runBlockingTest(testDispatcher) {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)
    doNotAwaitCompletion(cache.deleteAsync())

    val deferredUpdate = cache.updateIfPresentAsync { UPDATED_ASYNC_VALUE }

    // The operation should fail since the method expects the cache to be initialized.
    val exception = assertThrowsAsync(IllegalStateException::class) { awaitCompletion(deferredUpdate) }
    assertThat(exception).hasMessageThat().contains("Expected to update the cache only after it's been created")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testMaybeDelete_emptyCache_falsePredicate_returnsFalse() = runBlockingTest(testDispatcher) {
    val cache = cacheFactory.create<String>()

    val maybeDeleteResult = cache.maybeDeleteAsync { false }

    // An empty cache cannot be deleted.
    assertThat(awaitCompletion(maybeDeleteResult)).isFalse()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testMaybeDelete_emptyCache_truePredicate_returnsFalse() = runBlockingTest(testDispatcher) {
    val cache = cacheFactory.create<String>()

    val maybeDeleteResult = cache.maybeDeleteAsync { true }

    // An empty cache cannot be deleted.
    assertThat(awaitCompletion(maybeDeleteResult)).isFalse()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testMaybeDelete_emptyCache_keepsCacheNull() = runBlockingTest(testDispatcher) {
    val cache = cacheFactory.create<String>()

    doNotAwaitCompletion(cache.maybeDeleteAsync { true })

    // The empty cache should stay empty.
    assertThat(awaitCompletion(cache.readAsync())).isNull()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testMaybeDelete_nonEmptyCache_falsePredicate_returnsFalse() = runBlockingTest(testDispatcher) {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)

    val maybeDeleteResult = cache.maybeDeleteAsync { false }

    // The predicate's false return value should be piped up to the deletion result.
    assertThat(awaitCompletion(maybeDeleteResult)).isFalse()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testMaybeDelete_nonEmptyCache_falsePredicate_keepsCacheNonEmpty() = runBlockingTest(testDispatcher) {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)

    doNotAwaitCompletion(cache.maybeDeleteAsync { false })

    // The cache should retain its value since the deletion predicate indicated it shouldn't be cleared.
    assertThat(awaitCompletion(cache.readAsync())).isEqualTo(INITIALIZED_CACHE_VALUE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testMaybeDelete_nonEmptyCache_truePredicate_returnsTrue() = runBlockingTest(testDispatcher) {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)

    val maybeDeleteResult = cache.maybeDeleteAsync { true }

    // The predicate's true return value should be piped up to the deletion result.
    assertThat(awaitCompletion(maybeDeleteResult)).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testMaybeDelete_nonEmptyCache_truePredicate_emptiesCache() = runBlockingTest(testDispatcher) {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)

    doNotAwaitCompletion(cache.maybeDeleteAsync { true })

    // The cache should be emptied as indicated by the deletion predicate.
    assertThat(awaitCompletion(cache.readAsync())).isNull()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testMaybeDelete_blockingFunction_blocksDeletion() = runBlockingTest(testDispatcher) {
    testDispatcher.resumeDispatcher() // Keep the test dispatcher active since this test is verifying blocking behavior.
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)
    backgroundTestCoroutineDispatcher.pauseDispatcher()

    val blockingOperation = backgroundTestCoroutineScope.async { true }
    val deleteOperation = cache.maybeDeleteAsync { blockingOperation.await() }

    // The blocking operation should also block deletion.
    assertThat(deleteOperation.isCompleted).isFalse()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testMaybeDelete_blockingFunction_completed_deletionCompletes() = runBlockingTest(testDispatcher) {
    testDispatcher.resumeDispatcher() // Keep the test dispatcher active since this test is verifying blocking behavior.
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)
    backgroundTestCoroutineDispatcher.pauseDispatcher()
    val blockingOperation = backgroundTestCoroutineScope.async { true }
    val deleteOperation = cache.maybeDeleteAsync { blockingOperation.await() }

    backgroundTestCoroutineDispatcher.advanceUntilIdle()

    // Completing the blocking operation should complete deletion.
    assertThat(deleteOperation.isCompleted).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testMaybeForceDelete_emptyCache_falsePredicate_returnsFalse() = runBlockingTest(testDispatcher) {
    val cache = cacheFactory.create<String>()

    val maybeDeleteResult = cache.maybeForceDeleteAsync { false }

    // An empty cache cannot be deleted.
    assertThat(awaitCompletion(maybeDeleteResult)).isFalse()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testMaybeForceDelete_emptyCache_truePredicate_returnsTrue() = runBlockingTest(testDispatcher) {
    val cache = cacheFactory.create<String>()

    val maybeDeleteResult = cache.maybeForceDeleteAsync { true }

    // An empty cache cannot be deleted, but with force deletion the state of the cache is not checked. It's assumed
    // that the cache was definitely cleared.
    assertThat(awaitCompletion(maybeDeleteResult)).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testMaybeForceDelete_emptyCache_keepsCacheNull() = runBlockingTest(testDispatcher) {
    val cache = cacheFactory.create<String>()

    doNotAwaitCompletion(cache.maybeForceDeleteAsync { true })

    // The empty cache should stay empty.
    assertThat(awaitCompletion(cache.readAsync())).isNull()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testMaybeForceDelete_nonEmptyCache_falsePredicate_returnsFalse() = runBlockingTest(testDispatcher) {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)

    val maybeDeleteResult = cache.maybeForceDeleteAsync { false }

    // The predicate's false return value should be piped up to the deletion result.
    assertThat(awaitCompletion(maybeDeleteResult)).isFalse()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testMaybeForceDelete_nonEmptyCache_falsePredicate_keepsCacheNonEmpty() = runBlockingTest(testDispatcher) {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)

    doNotAwaitCompletion(cache.maybeForceDeleteAsync { false })

    // The cache should retain its value since the deletion predicate indicated it shouldn't be cleared.
    assertThat(awaitCompletion(cache.readAsync())).isEqualTo(INITIALIZED_CACHE_VALUE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testMaybeForceDelete_nonEmptyCache_truePredicate_returnsTrue() = runBlockingTest(testDispatcher) {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)

    val maybeDeleteResult = cache.maybeForceDeleteAsync { true }

    // The predicate's true return value should be piped up to the deletion result.
    assertThat(awaitCompletion(maybeDeleteResult)).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testMaybeForceDelete_nonEmptyCache_truePredicate_emptiesCache() = runBlockingTest(testDispatcher) {
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)

    doNotAwaitCompletion(cache.maybeForceDeleteAsync { true })

    // The cache should be emptied as indicated by the deletion predicate.
    assertThat(awaitCompletion(cache.readAsync())).isNull()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testMaybeForceDelete_blockingFunction_blocksDeletion() = runBlockingTest(testDispatcher) {
    testDispatcher.resumeDispatcher() // Keep the test dispatcher active since this test is verifying blocking behavior.
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)
    backgroundTestCoroutineDispatcher.pauseDispatcher()

    val blockingOperation = backgroundTestCoroutineScope.async { true }
    val deleteOperation = cache.maybeForceDeleteAsync { blockingOperation.await() }

    // The blocking operation should also block deletion.
    assertThat(deleteOperation.isCompleted).isFalse()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testMaybeForceDelete_blockingFunction_completed_deletionCompletes() = runBlockingTest(testDispatcher) {
    testDispatcher.resumeDispatcher() // Keep the test dispatcher active since this test is verifying blocking behavior.
    val cache = cacheFactory.create(INITIALIZED_CACHE_VALUE)
    backgroundTestCoroutineDispatcher.pauseDispatcher()
    val blockingOperation = backgroundTestCoroutineScope.async { true }
    val deleteOperation = cache.maybeForceDeleteAsync { blockingOperation.await() }

    backgroundTestCoroutineDispatcher.advanceUntilIdle()

    // Completing the blocking operation should complete deletion.
    assertThat(deleteOperation.isCompleted).isTrue()
  }

  /**
   * Silences the warning that [Deferred] is unused. This is okay for tests that ensure await() is called at the end of
   * the test since the cache guarantees sequential execution.
   */
  private fun <T> doNotAwaitCompletion(@Suppress("UNUSED_PARAMETER") deferred: Deferred<T>) {}

  /**
   * Waits for the specified deferred to execute after advancing test dispatcher. Without this function, results cannot
   * be observed from cache operations.
   */
  @ExperimentalCoroutinesApi
  private suspend fun <T> awaitCompletion(deferred: Deferred<T>): T {
    testDispatcher.advanceUntilIdle()
    return deferred.await()
  }

  // TODO(#89): Move to a common test library.
  /** A replacement to JUnit5's assertThrows() with Kotlin suspend coroutine support. */
  private suspend fun <T : Throwable> assertThrowsAsync(type: KClass<T>, operation: suspend () -> Unit): T {
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

  @Qualifier annotation class TestDispatcher

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    @ExperimentalCoroutinesApi
    @Singleton
    @Provides
    @TestDispatcher
    fun provideTestDispatcher(): TestCoroutineDispatcher {
      return TestCoroutineDispatcher()
    }

    @ExperimentalCoroutinesApi
    @Singleton
    @Provides
    @BlockingDispatcher
    fun provideBlockingDispatcher(@TestDispatcher testDispatcher: TestCoroutineDispatcher): CoroutineDispatcher {
      return testDispatcher
    }
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(modules = [TestModule::class])
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
