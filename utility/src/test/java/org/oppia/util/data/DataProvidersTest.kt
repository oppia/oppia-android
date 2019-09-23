package org.oppia.util.data

import android.app.Application
import android.content.Context
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
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
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import org.robolectric.annotation.Config
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

private const val BASE_PROVIDER_ID = "base_id"
private const val OTHER_PROVIDER_ID = "other_id"
private const val TRANSFORMED_PROVIDER_ID = "transformed_id"
private const val FIRST_STR_VALUE = "first str value"
private const val SECOND_STR_VALUE = "second and longer str value"
private const val TRANSFORMED_FIRST_INT_VALUE = FIRST_STR_VALUE.length
private const val TRANSFORMED_SECOND_INT_VALUE = SECOND_STR_VALUE.length

/** Tests for [DataProviders], [DataProvider]s, and [AsyncDataSubscriptionManager]. */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class DataProvidersTest {
  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject
  lateinit var dataProviders: DataProviders

  @Inject
  lateinit var asyncDataSubscriptionManager: AsyncDataSubscriptionManager

  @Inject
  @field:TestDispatcher
  lateinit var testDispatcher: CoroutineDispatcher

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

  @Mock
  lateinit var mockStringLiveDataObserver: Observer<AsyncResult<String>>

  @Mock
  lateinit var mockIntLiveDataObserver: Observer<AsyncResult<Int>>

  @Captor
  lateinit var stringResultCaptor: ArgumentCaptor<AsyncResult<String>>

  @Captor
  lateinit var intResultCaptor: ArgumentCaptor<AsyncResult<Int>>

  private var inMemoryCachedStr: String? = null

  @Before
  @ExperimentalCoroutinesApi
  fun setUp() {
    setUpTestApplicationComponent()
    Dispatchers.setMain(testDispatcher)
  }

  @After
  @ExperimentalCoroutinesApi
  fun tearDown() {
    Dispatchers.resetMain()
  }

  // Note: custom data providers aren't explicitly tested since their interaction with the infrastructure is tested
  // through the providers created by DataProviders, and through other custom data providers in the stack.

  @Test
  fun testInMemoryDataProvider_toLiveData_deliversInMemoryValue() {
    val dataProvider = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID) { FIRST_STR_VALUE }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)

    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(FIRST_STR_VALUE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testInMemoryDataProvider_toLiveData_notifies_doesNotDeliverSameValueAgain() = runBlockingTest(testDispatcher) {
    val dataProvider = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID) { FIRST_STR_VALUE }
    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    advanceUntilIdle()

    reset(mockStringLiveDataObserver)
    asyncDataSubscriptionManager.notifyChange(BASE_PROVIDER_ID)
    advanceUntilIdle()

    // The observer should not be notified again since the value hasn't changed.
    verifyZeroInteractions(mockStringLiveDataObserver)
  }

  @Test
  fun testInMemoryDataProvider_toLiveData_withChangedValue_beforeReg_deliversSecondValue() {
    inMemoryCachedStr = FIRST_STR_VALUE
    val dataProvider = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID) { inMemoryCachedStr!! }

    inMemoryCachedStr = SECOND_STR_VALUE
    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)

    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(SECOND_STR_VALUE)
  }

  @Test
  fun testInMemoryDataProvider_toLiveData_withChangedValue_afterReg_deliversFirstValue() {
    inMemoryCachedStr = FIRST_STR_VALUE
    val dataProvider = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID) { inMemoryCachedStr!! }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    inMemoryCachedStr = SECOND_STR_VALUE

    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(FIRST_STR_VALUE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testInMemoryDataProvider_changedValueAfterReg_notified_deliversSecondValue() = runBlockingTest(testDispatcher) {
    inMemoryCachedStr = FIRST_STR_VALUE
    val dataProvider = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID) { inMemoryCachedStr!! }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    inMemoryCachedStr = SECOND_STR_VALUE
    asyncDataSubscriptionManager.notifyChange(BASE_PROVIDER_ID)
    advanceUntilIdle()

    verify(mockStringLiveDataObserver, atLeastOnce()).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(SECOND_STR_VALUE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testInMemoryDataProvider_changedValue_notifiesDiffProvider_deliversFirstVal() = runBlockingTest(testDispatcher) {
    inMemoryCachedStr = FIRST_STR_VALUE
    val dataProvider = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID) { inMemoryCachedStr!! }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    inMemoryCachedStr = SECOND_STR_VALUE
    asyncDataSubscriptionManager.notifyChange(OTHER_PROVIDER_ID)
    advanceUntilIdle()

    // The first value should be observed since a completely different provider was notified.
    verify(mockStringLiveDataObserver, atLeastOnce()).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(FIRST_STR_VALUE)
  }

  @Test
  fun testInMemoryDataProvider_toLiveData_withObserver_doesCallFunction() {
    // It would be nice to use a mock of the lambda (e.g. https://stackoverflow.com/a/53306974/3689782), but this
    // apparently does not work with Robolectric: https://github.com/robolectric/robolectric/issues/3688.
    var fakeLoadMemoryCallbackCalled = false
    val fakeLoadMemoryCallback: () -> String = {
      fakeLoadMemoryCallbackCalled = true
      FIRST_STR_VALUE
    }
    val dataProvider = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID, fakeLoadMemoryCallback)

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)

    // With a LiveData observer, the load memory callback should be called.
    assertThat(fakeLoadMemoryCallbackCalled).isTrue()
  }

  @Test
  fun testInMemoryDataProvider_toLiveData_noObserver_doesNotCallFunction() {
    var fakeLoadMemoryCallbackCalled = false
    val fakeLoadMemoryCallback: () -> String = {
      fakeLoadMemoryCallbackCalled = true
      FIRST_STR_VALUE
    }
    val dataProvider = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID, fakeLoadMemoryCallback)

    dataProviders.convertToLiveData(dataProvider)

    // Without a LiveData observer, the load memory callback should never be called.
    assertThat(fakeLoadMemoryCallbackCalled).isFalse()
  }

  @Test
  fun testInMemoryDataProvider_toLiveData_throwsException_deliversFailure() {
    val dataProvider: DataProvider<String> =
      dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID) { throw IllegalStateException("Failed") }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)

    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isFailure()).isTrue()
    assertThat(stringResultCaptor.value.getErrorOrNull()).isInstanceOf(IllegalStateException::class.java)
  }

  @Test
  fun testAsyncInMemoryDataProvider_toLiveData_deliversInMemoryValue() {
    val dataProvider = dataProviders.createInMemoryDataProviderAsync(BASE_PROVIDER_ID) {
      AsyncResult.success(FIRST_STR_VALUE)
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)

    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(FIRST_STR_VALUE)
  }

  @Test
  fun testAsyncInMemoryDataProvider_toLiveData_withChangedValue_beforeReg_deliversSecondValue() {
    inMemoryCachedStr = FIRST_STR_VALUE
    val dataProvider = dataProviders.createInMemoryDataProviderAsync(BASE_PROVIDER_ID) {
      AsyncResult.success(inMemoryCachedStr!!)
    }

    inMemoryCachedStr = SECOND_STR_VALUE
    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)

    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(SECOND_STR_VALUE)
  }

  @Test
  fun testAsyncInMemoryDataProvider_toLiveData_withChangedValue_afterReg_deliversFirstValue() {
    inMemoryCachedStr = FIRST_STR_VALUE
    val dataProvider = dataProviders.createInMemoryDataProviderAsync(BASE_PROVIDER_ID) {
      AsyncResult.success(inMemoryCachedStr!!)
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    inMemoryCachedStr = SECOND_STR_VALUE

    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(FIRST_STR_VALUE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testAsyncInMemoryDataProvider_changedValueAfterReg_notified_deliversValueTwo() = runBlockingTest(testDispatcher) {
    inMemoryCachedStr = FIRST_STR_VALUE
    val dataProvider = dataProviders.createInMemoryDataProviderAsync(BASE_PROVIDER_ID) {
      AsyncResult.success(inMemoryCachedStr!!)
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    inMemoryCachedStr = SECOND_STR_VALUE
    asyncDataSubscriptionManager.notifyChange(BASE_PROVIDER_ID)
    advanceUntilIdle()

    verify(mockStringLiveDataObserver, atLeastOnce()).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(SECOND_STR_VALUE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testAsyncInMemoryDataProvider_blockingFunction_doesNotDeliver() = runBlockingTest(testDispatcher) {
    // Ensure the suspend operation is initially blocked.
    backgroundTestCoroutineDispatcher.pauseDispatcher()
    val blockingOperation = backgroundTestCoroutineScope.async { FIRST_STR_VALUE }
    val dataProvider = dataProviders.createInMemoryDataProviderAsync(BASE_PROVIDER_ID) {
      AsyncResult.success(blockingOperation.await())
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    advanceUntilIdle()

    // The observer should never be called since the underlying async function hasn't yet completed.
    verifyZeroInteractions(mockStringLiveDataObserver)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testAsyncInMemoryDataProvider_blockingFunctionCompleted_deliversValue() = runBlockingTest(testDispatcher) {
    // Ensure the suspend operation is initially blocked.
    backgroundTestCoroutineDispatcher.pauseDispatcher()
    val blockingOperation = backgroundTestCoroutineScope.async { FIRST_STR_VALUE }
    val dataProvider = dataProviders.createInMemoryDataProviderAsync(BASE_PROVIDER_ID) {
      AsyncResult.success(blockingOperation.await())
    }

    // Start observing the provider, then complete its suspend function.
    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    // Finish the blocking operation.
    backgroundTestCoroutineDispatcher.advanceUntilIdle()
    advanceUntilIdle()

    // The provider will deliver a value immediately when the suspend function completes (no additional notification is
    // needed).
    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(FIRST_STR_VALUE)
  }

  @Test
  fun testAsyncInMemoryDataProvider_toLiveData_withObserver_doesCallFunction() {
    var fakeLoadMemoryCallbackCalled = false
    val fakeLoadMemoryCallback: suspend () -> AsyncResult<String> = {
      fakeLoadMemoryCallbackCalled = true
      AsyncResult.success(FIRST_STR_VALUE)
    }
    val dataProvider = dataProviders.createInMemoryDataProviderAsync(BASE_PROVIDER_ID, fakeLoadMemoryCallback)

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)

    // With a LiveData observer, the load memory callback should be called.
    assertThat(fakeLoadMemoryCallbackCalled).isTrue()
  }

  @Test
  fun testAsyncInMemoryDataProvider_toLiveData_noObserver_doesNotCallFunction() {
    var fakeLoadMemoryCallbackCalled = false
    val fakeLoadMemoryCallback: suspend () -> AsyncResult<String> = {
      fakeLoadMemoryCallbackCalled = true
      AsyncResult.success(FIRST_STR_VALUE)
    }
    val dataProvider = dataProviders.createInMemoryDataProviderAsync(BASE_PROVIDER_ID, fakeLoadMemoryCallback)

    dataProviders.convertToLiveData(dataProvider)

    // Without a LiveData observer, the load memory callback should never be called.
    assertThat(fakeLoadMemoryCallbackCalled).isFalse()
  }

  @Test
  fun testAsyncInMemoryDataProvider_toLiveData_pendingResult_deliversPendingResult() {
    val dataProvider = dataProviders.createInMemoryDataProviderAsync<String>(BASE_PROVIDER_ID) { AsyncResult.pending() }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)

    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isPending()).isTrue()
  }

  @Test
  fun testAsyncInMemoryDataProvider_toLiveData_failure_deliversFailure() {
    val dataProvider = dataProviders.createInMemoryDataProviderAsync<String>(BASE_PROVIDER_ID) {
      AsyncResult.failed(IllegalStateException("Failure"))
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)

    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isFailure()).isTrue()
    assertThat(stringResultCaptor.value.getErrorOrNull()).isInstanceOf(IllegalStateException::class.java)
  }

  @Test
  fun testTransform_toLiveData_deliversTransformedValue() {
    val baseProvider = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID) { FIRST_STR_VALUE }
    val dataProvider = dataProviders.transform(TRANSFORMED_PROVIDER_ID, baseProvider) { transformString(it) }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)

    verify(mockIntLiveDataObserver).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isSuccess()).isTrue()
    assertThat(intResultCaptor.value.getOrThrow()).isEqualTo(TRANSFORMED_FIRST_INT_VALUE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testTransform_toLiveData_differentValue_notifiesBase_deliversXformedValueTwo() = runBlockingTest(testDispatcher) {
    inMemoryCachedStr = FIRST_STR_VALUE
    val baseProvider = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID) { inMemoryCachedStr!! }
    val dataProvider = dataProviders.transform(TRANSFORMED_PROVIDER_ID, baseProvider) { transformString(it) }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    inMemoryCachedStr = SECOND_STR_VALUE
    asyncDataSubscriptionManager.notifyChange(BASE_PROVIDER_ID)
    advanceUntilIdle()

    // Notifying the base results in observers of the transformed provider also being called.
    verify(mockIntLiveDataObserver, atLeastOnce()).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isSuccess()).isTrue()
    assertThat(intResultCaptor.value.getOrThrow()).isEqualTo(TRANSFORMED_SECOND_INT_VALUE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testTransform_toLiveData_differentValue_notifiesXform_deliversXformedValueTwo() = runBlockingTest(testDispatcher) {
    inMemoryCachedStr = FIRST_STR_VALUE
    val baseProvider = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID) { inMemoryCachedStr!! }
    val dataProvider = dataProviders.transform(TRANSFORMED_PROVIDER_ID, baseProvider) { transformString(it) }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    inMemoryCachedStr = SECOND_STR_VALUE
    asyncDataSubscriptionManager.notifyChange(TRANSFORMED_PROVIDER_ID)
    advanceUntilIdle()

    // Notifying the transformed provider has the same result as notifying the base provider.
    verify(mockIntLiveDataObserver, atLeastOnce()).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isSuccess()).isTrue()
    assertThat(intResultCaptor.value.getOrThrow()).isEqualTo(TRANSFORMED_SECOND_INT_VALUE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testTransform_differentValue_notifiesBase_observeBase_deliversSecondValue() = runBlockingTest(testDispatcher) {
    inMemoryCachedStr = FIRST_STR_VALUE
    val baseProvider = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID) { inMemoryCachedStr!! }
    val dataProvider = dataProviders.transform(TRANSFORMED_PROVIDER_ID, baseProvider) { transformString(it) }

    dataProviders.convertToLiveData(baseProvider).observeForever(mockStringLiveDataObserver)
    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    inMemoryCachedStr = SECOND_STR_VALUE
    asyncDataSubscriptionManager.notifyChange(BASE_PROVIDER_ID)
    advanceUntilIdle()

    // Having a transformed data provider with an observer does not change the base's notification behavior.
    verify(mockStringLiveDataObserver, atLeastOnce()).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(SECOND_STR_VALUE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testTransform_differentValue_notifiesXformed_observeBase_deliversFirstValue() = runBlockingTest(testDispatcher) {
    inMemoryCachedStr = FIRST_STR_VALUE
    val baseProvider = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID) { inMemoryCachedStr!! }
    val dataProvider = dataProviders.transform(TRANSFORMED_PROVIDER_ID, baseProvider) { transformString(it) }

    dataProviders.convertToLiveData(baseProvider).observeForever(mockStringLiveDataObserver)
    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    inMemoryCachedStr = SECOND_STR_VALUE
    asyncDataSubscriptionManager.notifyChange(TRANSFORMED_PROVIDER_ID)
    advanceUntilIdle()

    // However, notifying that the transformed provider has changed should not affect base subscriptions even if the
    // base has changed.
    verify(mockStringLiveDataObserver, atLeastOnce()).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(FIRST_STR_VALUE)
  }

  @Test
  fun testTransform_toLiveData_basePending_deliversPending() {
    val baseProvider = dataProviders.createInMemoryDataProviderAsync<String>(BASE_PROVIDER_ID) { AsyncResult.pending() }
    val dataProvider = dataProviders.transform(TRANSFORMED_PROVIDER_ID, baseProvider) { transformString(it) }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)

    verify(mockIntLiveDataObserver).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isPending()).isTrue()
  }

  @Test
  fun testTransform_toLiveData_baseFailure_deliversFailure() {
    val baseProvider = dataProviders.createInMemoryDataProviderAsync<String>(BASE_PROVIDER_ID) {
      AsyncResult.failed(IllegalStateException("Failed"))
    }
    val dataProvider = dataProviders.transform(TRANSFORMED_PROVIDER_ID, baseProvider) { transformString(it) }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)

    verify(mockIntLiveDataObserver).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isFailure()).isTrue()
    assertThat(intResultCaptor.value.getErrorOrNull()).isInstanceOf(AsyncResult.ChainedFailureException::class.java)
    assertThat(intResultCaptor.value.getErrorOrNull()).hasCauseThat().isInstanceOf(IllegalStateException::class.java)
  }

  @Test
  fun testTransform_toLiveData_withObserver_callsTransform() {
    var fakeTransformCallbackCalled = false
    val fakeTransformCallback: (String) -> Int = {
      fakeTransformCallbackCalled = true
      transformString(it)
    }
    val baseProvider = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID) { FIRST_STR_VALUE }
    val dataProvider = dataProviders.transform(TRANSFORMED_PROVIDER_ID, baseProvider, fakeTransformCallback)

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)

    // A successful base provider with a LiveData observer should result in the transform function being called.
    assertThat(fakeTransformCallbackCalled).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testTransform_toLiveData_noObserver_doesNotCallTransform() {
    var fakeTransformCallbackCalled = false
    val fakeTransformCallback: (String) -> Int = {
      fakeTransformCallbackCalled = true
      transformString(it)
    }
    val baseProvider = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID) { FIRST_STR_VALUE }
    val dataProvider = dataProviders.transform(TRANSFORMED_PROVIDER_ID, baseProvider, fakeTransformCallback)

    dataProviders.convertToLiveData(dataProvider)

    // Without an observer, the transform method should not be called.
    assertThat(fakeTransformCallbackCalled).isFalse()
  }

  @Test
  fun testTransform_toLiveData_basePending_doesNotCallTransform() {
    var fakeTransformCallbackCalled = false
    val fakeTransformCallback: (String) -> Int = {
      fakeTransformCallbackCalled = true
      transformString(it)
    }
    val baseProvider = dataProviders.createInMemoryDataProviderAsync<String>(BASE_PROVIDER_ID) { AsyncResult.pending() }
    val dataProvider = dataProviders.transform(TRANSFORMED_PROVIDER_ID, baseProvider, fakeTransformCallback)

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)

    // The transform method shouldn't be called if the base provider is in a pending state.
    assertThat(fakeTransformCallbackCalled).isFalse()
  }

  @Test
  fun testTransform_toLiveData_baseFailure_doesNotCallTransform() {
    var fakeTransformCallbackCalled = false
    val fakeTransformCallback: (String) -> Int = {
      fakeTransformCallbackCalled = true
      transformString(it)
    }
    val baseProvider = dataProviders.createInMemoryDataProviderAsync<String>(BASE_PROVIDER_ID) {
      AsyncResult.failed(IllegalStateException("Base failure"))
    }
    val dataProvider = dataProviders.transform(TRANSFORMED_PROVIDER_ID, baseProvider, fakeTransformCallback)

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)

    // The transform method shouldn't be called if the base provider is in a failure state.
    assertThat(fakeTransformCallbackCalled).isFalse()
  }

  @Test
  fun testTransform_toLiveData_throwsException_deliversFailure() {
    val baseProvider = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID) { FIRST_STR_VALUE }
    val dataProvider = dataProviders.transform<String, Int>(TRANSFORMED_PROVIDER_ID, baseProvider) {
      throw IllegalStateException("Transform failure")
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)

    // Note that the exception type here is not chained since the failure occurred in the transform function.
    verify(mockIntLiveDataObserver).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isFailure()).isTrue()
    assertThat(intResultCaptor.value.getErrorOrNull()).isInstanceOf(IllegalStateException::class.java)
    assertThat(intResultCaptor.value.getErrorOrNull()).hasMessageThat().contains("Transform failure")
  }

  @Test
  fun testTransform_toLiveData_baseThrowsException_deliversFailure() {
    val baseProvider = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID) {
      throw IllegalStateException("Base failure")
    }
    val dataProvider = dataProviders.transform(TRANSFORMED_PROVIDER_ID, baseProvider) {
      @Suppress("UNREACHABLE_CODE") // This is expected to be unreachable code for this test.
      transformString(it)
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)

    verify(mockIntLiveDataObserver).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isFailure()).isTrue()
    assertThat(intResultCaptor.value.getErrorOrNull()).isInstanceOf(AsyncResult.ChainedFailureException::class.java)
    assertThat(intResultCaptor.value.getErrorOrNull()).hasCauseThat().isInstanceOf(IllegalStateException::class.java)
    assertThat(intResultCaptor.value.getErrorOrNull()).hasCauseThat().hasMessageThat().contains("Base failure")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testTransformAsync_toLiveData_deliversTransformedValue() {
    val baseProvider = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID) { FIRST_STR_VALUE }
    val dataProvider = dataProviders.transformAsync(TRANSFORMED_PROVIDER_ID, baseProvider) { transformStringAsync(it) }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)

    verify(mockIntLiveDataObserver).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isSuccess()).isTrue()
    assertThat(intResultCaptor.value.getOrThrow()).isEqualTo(TRANSFORMED_FIRST_INT_VALUE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testTransformAsync_toLiveData_diffValue_notifiesBase_deliversXformedValueTwo() = runBlockingTest(testDispatcher) {
    inMemoryCachedStr = FIRST_STR_VALUE
    val baseProvider = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID) { inMemoryCachedStr!! }
    val dataProvider = dataProviders.transformAsync(TRANSFORMED_PROVIDER_ID, baseProvider) { transformStringAsync(it) }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    inMemoryCachedStr = SECOND_STR_VALUE
    asyncDataSubscriptionManager.notifyChange(BASE_PROVIDER_ID)
    advanceUntilIdle()

    // Notifying the base results in observers of the transformed provider also being called.
    verify(mockIntLiveDataObserver, atLeastOnce()).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isSuccess()).isTrue()
    assertThat(intResultCaptor.value.getOrThrow()).isEqualTo(TRANSFORMED_SECOND_INT_VALUE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testTransformAsync_toLiveData_diffVal_notifiesXform_deliversXformedValueTwo() = runBlockingTest(testDispatcher) {
    inMemoryCachedStr = FIRST_STR_VALUE
    val baseProvider = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID) { inMemoryCachedStr!! }
    val dataProvider = dataProviders.transformAsync(TRANSFORMED_PROVIDER_ID, baseProvider) { transformStringAsync(it) }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    inMemoryCachedStr = SECOND_STR_VALUE
    asyncDataSubscriptionManager.notifyChange(TRANSFORMED_PROVIDER_ID)
    advanceUntilIdle()

    // Notifying the transformed provider has the same result as notifying the base provider.
    verify(mockIntLiveDataObserver, atLeastOnce()).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isSuccess()).isTrue()
    assertThat(intResultCaptor.value.getOrThrow()).isEqualTo(TRANSFORMED_SECOND_INT_VALUE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testTransformAsync_differentValue_notifiesBase_observeBase_deliversSecondVal() = runBlockingTest(testDispatcher) {
    inMemoryCachedStr = FIRST_STR_VALUE
    val baseProvider = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID) { inMemoryCachedStr!! }
    val dataProvider = dataProviders.transformAsync(TRANSFORMED_PROVIDER_ID, baseProvider) { transformStringAsync(it) }

    dataProviders.convertToLiveData(baseProvider).observeForever(mockStringLiveDataObserver)
    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    inMemoryCachedStr = SECOND_STR_VALUE
    asyncDataSubscriptionManager.notifyChange(BASE_PROVIDER_ID)
    advanceUntilIdle()

    // Having a transformed data provider with an observer does not change the base's notification behavior.
    verify(mockStringLiveDataObserver, atLeastOnce()).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(SECOND_STR_VALUE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testTransformAsync_diffValue_notifiesXformed_observeBase_deliversFirstVal() = runBlockingTest(testDispatcher) {
    inMemoryCachedStr = FIRST_STR_VALUE
    val baseProvider = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID) { inMemoryCachedStr!! }
    val dataProvider = dataProviders.transformAsync(TRANSFORMED_PROVIDER_ID, baseProvider) { transformStringAsync(it) }

    dataProviders.convertToLiveData(baseProvider).observeForever(mockStringLiveDataObserver)
    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    inMemoryCachedStr = SECOND_STR_VALUE
    asyncDataSubscriptionManager.notifyChange(TRANSFORMED_PROVIDER_ID)
    advanceUntilIdle()

    // However, notifying that the transformed provider has changed should not affect base subscriptions even if the
    // base has changed.
    verify(mockStringLiveDataObserver, atLeastOnce()).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(FIRST_STR_VALUE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testTransformAsync_toLiveData_blockingFunction_doesNotDeliverValue() = runBlockingTest(testDispatcher) {
    // Block transformStringAsync().
    backgroundTestCoroutineDispatcher.pauseDispatcher()
    val baseProvider = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID) { FIRST_STR_VALUE }
    val dataProvider = dataProviders.transformAsync(TRANSFORMED_PROVIDER_ID, baseProvider) { transformStringAsync(it) }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    advanceUntilIdle()

    // No value should be delivered since the async function is blocked.
    verifyZeroInteractions(mockIntLiveDataObserver)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testTransformAsync_toLiveData_blockingFunction_completed_deliversXformedVal() = runBlockingTest(testDispatcher) {
    // Block transformStringAsync().
    backgroundTestCoroutineDispatcher.pauseDispatcher()
    val baseProvider = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID) { FIRST_STR_VALUE }
    val dataProvider = dataProviders.transformAsync(TRANSFORMED_PROVIDER_ID, baseProvider) { transformStringAsync(it) }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    backgroundTestCoroutineDispatcher.advanceUntilIdle() // Run transformStringAsync()
    advanceUntilIdle()

    // The value should now be delivered since the async function was unblocked.
    verify(mockIntLiveDataObserver).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isSuccess()).isTrue()
    assertThat(intResultCaptor.value.getOrThrow()).isEqualTo(TRANSFORMED_FIRST_INT_VALUE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testTransformAsync_toLiveData_blockingFunction_baseObserved_deliversFirstVal() = runBlockingTest(testDispatcher) {
    // Block transformStringAsync().
    backgroundTestCoroutineDispatcher.pauseDispatcher()
    val baseProvider = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID) { FIRST_STR_VALUE }
    val dataProvider = dataProviders.transformAsync(TRANSFORMED_PROVIDER_ID, baseProvider) { transformStringAsync(it) }

    dataProviders.convertToLiveData(baseProvider).observeForever(mockStringLiveDataObserver)
    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    advanceUntilIdle()

    // Verify that even though the transformed provider is blocked, the base can still properly publish changes.
    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(FIRST_STR_VALUE)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testTransformAsync_toLiveData_transformedPending_deliversPending() {
    val baseProvider = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID) { FIRST_STR_VALUE }
    val dataProvider = dataProviders.transformAsync<String, Int>(TRANSFORMED_PROVIDER_ID, baseProvider) {
      AsyncResult.pending()
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)

    // The transformation result yields a pending delivered result.
    verify(mockIntLiveDataObserver).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isPending()).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testTransformAsync_toLiveData_transformedFailure_deliversFailure() {
    val baseProvider = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID) { FIRST_STR_VALUE }
    val dataProvider = dataProviders.transformAsync<String, Int>(TRANSFORMED_PROVIDER_ID, baseProvider) {
      AsyncResult.failed(IllegalStateException("Transform failure"))
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)

    // Note that the failure exception in this case is not chained since the failure occurred in the transform function.
    verify(mockIntLiveDataObserver).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isFailure()).isTrue()
    assertThat(intResultCaptor.value.getErrorOrNull()).isInstanceOf(IllegalStateException::class.java)
    assertThat(intResultCaptor.value.getErrorOrNull()).hasMessageThat().contains("Transform failure")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testTransformAsync_toLiveData_basePending_deliversPending() {
    val baseProvider = dataProviders.createInMemoryDataProviderAsync<String>(BASE_PROVIDER_ID) { AsyncResult.pending() }
    val dataProvider = dataProviders.transformAsync(TRANSFORMED_PROVIDER_ID, baseProvider) { transformStringAsync(it) }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)

    // Since the base provider is pending, so is the transformed provider.
    verify(mockIntLiveDataObserver).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isPending()).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testTransformAsync_toLiveData_baseFailure_deliversFailure() {
    val baseProvider = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID) {
      throw IllegalStateException("Base failure")
    }
    val dataProvider = dataProviders.transformAsync(TRANSFORMED_PROVIDER_ID, baseProvider) {
      @Suppress("UNREACHABLE_CODE") // This code is intentionally unreachable for this test case.
      transformStringAsync(it)
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)

    // Note that the failure exception in this case is not chained since the failure occurred in the transform function.
    verify(mockIntLiveDataObserver).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isFailure()).isTrue()
    assertThat(intResultCaptor.value.getErrorOrNull()).isInstanceOf(AsyncResult.ChainedFailureException::class.java)
    assertThat(intResultCaptor.value.getErrorOrNull()).hasCauseThat().isInstanceOf(IllegalStateException::class.java)
    assertThat(intResultCaptor.value.getErrorOrNull()).hasCauseThat().hasMessageThat().contains("Base failure")
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testTransformAsync_toLiveData_withObserver_callsTransform() {
    var fakeTransformCallbackCalled = false
    val fakeTransformCallback: suspend (String) -> AsyncResult<Int> = {
      fakeTransformCallbackCalled = true
      transformStringAsync(it)
    }
    val baseProvider = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID) { FIRST_STR_VALUE }
    val dataProvider = dataProviders.transformAsync(TRANSFORMED_PROVIDER_ID, baseProvider, fakeTransformCallback)

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)

    // Since there's an observer, the transform method should be called.
    assertThat(fakeTransformCallbackCalled).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testTransformAsync_toLiveData_noObserver_doesNotCallTransform() {
    var fakeTransformCallbackCalled = false
    val fakeTransformCallback: suspend (String) -> AsyncResult<Int> = {
      fakeTransformCallbackCalled = true
      transformStringAsync(it)
    }
    val baseProvider = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID) { FIRST_STR_VALUE }
    val dataProvider = dataProviders.transformAsync(TRANSFORMED_PROVIDER_ID, baseProvider, fakeTransformCallback)

    dataProviders.convertToLiveData(dataProvider)

    // Without an observer, the transform method should not be called.
    assertThat(fakeTransformCallbackCalled).isFalse()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testTransformAsync_toLiveData_basePending_doesNotCallTransform() {
    var fakeTransformCallbackCalled = false
    val fakeTransformCallback: suspend (String) -> AsyncResult<Int> = {
      fakeTransformCallbackCalled = true
      transformStringAsync(it)
    }
    val baseProvider = dataProviders.createInMemoryDataProviderAsync<String>(BASE_PROVIDER_ID) { AsyncResult.pending() }
    val dataProvider = dataProviders.transformAsync(TRANSFORMED_PROVIDER_ID, baseProvider, fakeTransformCallback)

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)

    // A pending base provider should result in the transform method not being called.
    assertThat(fakeTransformCallbackCalled).isFalse()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testTransformAsync_toLiveData_baseFailure_doesNotCallTransform() {
    var fakeTransformCallbackCalled = false
    val fakeTransformCallback: suspend (String) -> AsyncResult<Int> = {
      fakeTransformCallbackCalled = true
      transformStringAsync(it)
    }
    val baseProvider = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID) {
      throw IllegalStateException("Base failure")
    }
    val dataProvider = dataProviders.transformAsync(TRANSFORMED_PROVIDER_ID, baseProvider, fakeTransformCallback)

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)

    // A base provider failure should result in the transform method not being called.
    assertThat(fakeTransformCallbackCalled).isFalse()
  }

  private fun transformString(str: String): Int {
    return str.length
  }

  /**
   * Transforms the specified string into an integer in the same way as [transformString], except in a blocking context
   * using [backgroundTestCoroutineDispatcher].
   */
  @ExperimentalCoroutinesApi
  private suspend fun transformStringAsync(str: String): AsyncResult<Int> {
    val deferred = backgroundTestCoroutineScope.async { transformString(str) }
    deferred.await()
    return AsyncResult.success(deferred.getCompleted())
  }

  private fun setUpTestApplicationComponent() {
    DaggerDataProvidersTest_TestApplicationComponent.builder()
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
    fun provideTestDispatcher(): CoroutineDispatcher {
      return TestCoroutineDispatcher()
    }

    @Singleton
    @Provides
    @BackgroundDispatcher
    fun provideBackgroundDispatcher(@TestDispatcher testDispatcher: CoroutineDispatcher): CoroutineDispatcher {
      return testDispatcher
    }

    @Singleton
    @Provides
    @BlockingDispatcher
    fun provideBlockingDispatcher(@TestDispatcher testDispatcher: CoroutineDispatcher): CoroutineDispatcher {
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

    fun inject(dataProvidersTest: DataProvidersTest)
  }
}
