package org.oppia.util.data

import android.app.Application
import android.content.Context
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.async
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.any
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.reset
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyZeroInteractions
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.testing.TestCoroutineDispatchers
import org.oppia.testing.TestDispatcherModule
import org.oppia.util.crashlytics.CrashlyticsWrapper
import org.oppia.util.threading.BackgroundDispatcher
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

private const val BASE_PROVIDER_ID_0 = "base_id_0"
private const val BASE_PROVIDER_ID_1 = "base_id_1"
private const val OTHER_PROVIDER_ID = "other_id"
private const val TRANSFORMED_PROVIDER_ID = "transformed_id"
private const val COMBINED_PROVIDER_ID = "combined_id"
private const val STR_VALUE_0 = "I used to be indecisive."
private const val STR_VALUE_1 = "Now I'm not so sure."
private const val STR_VALUE_2 = "At least I thought I was."
private const val INT_XFORMED_STR_VALUE_0 = STR_VALUE_0.length
private const val INT_XFORMED_STR_VALUE_1 = STR_VALUE_1.length
private const val INT_XFORMED_STR_VALUE_2 = STR_VALUE_2.length
private const val INT_XFORMED_STR_VALUE_0_DOUBLED = INT_XFORMED_STR_VALUE_0 * 2
private const val COMBINED_STR_VALUE_01 = "I used to be indecisive. Now I'm not so sure."
private const val COMBINED_STR_VALUE_21 = "At least I thought I was. Now I'm not so sure."
private const val COMBINED_STR_VALUE_02 = "I used to be indecisive. At least I thought I was."

/** Tests for [DataProviders], [DataProvider]s, and [AsyncDataSubscriptionManager]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class DataProvidersTest {
  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject lateinit var dataProviders: DataProviders

  @Inject lateinit var asyncDataSubscriptionManager: AsyncDataSubscriptionManager

  @InternalCoroutinesApi @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject @field:BackgroundDispatcher
  lateinit var backgroundCoroutineDispatcher: CoroutineDispatcher

  @Inject
  @Mock
  lateinit var mockCrashlyticsWrapper: CrashlyticsWrapper

  @Mock
  lateinit var mockStringLiveDataObserver: Observer<AsyncResult<String>>

  @Mock
  lateinit var mockIntLiveDataObserver: Observer<AsyncResult<Int>>

  @Mock
  lateinit var exception: IllegalStateException

  @Mock
  lateinit var firebaseCrashlytics: FirebaseCrashlytics

  @Mock
  lateinit var firebaseApp: FirebaseApp

  @Captor
  lateinit var exceptionCaptor: ArgumentCaptor<Exception>

  @Captor
  lateinit var stringResultCaptor: ArgumentCaptor<AsyncResult<String>>

  @Captor
  lateinit var intResultCaptor: ArgumentCaptor<AsyncResult<Int>>

  private var inMemoryCachedStr: String? = null
  private var inMemoryCachedStr2: String? = null

  private val backgroundCoroutineScope by lazy {
    CoroutineScope(backgroundCoroutineDispatcher)
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    /*setUpFirebase()
    FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    CrashlyticsWrapper.firebaseCrashlytics = FirebaseCrashlytics.getInstance()*/
  }

  // Note: custom data providers aren't explicitly tested since their interaction with the infrastructure is tested
  // through the providers created by DataProviders, and through other custom data providers in the stack.

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testConvertToLiveData_fakeDataProvider_noObserver_doesNotCallRetrieve() {
    val fakeDataProvider = object : DataProvider<Int> {
      var hasRetrieveBeenCalled = false

      override fun getId(): Any = "fake_data_provider"

      override suspend fun retrieveData(): AsyncResult<Int> {
        hasRetrieveBeenCalled = true
        return AsyncResult.pending()
      }
    }

    dataProviders.convertToLiveData(fakeDataProvider)
    testCoroutineDispatchers.advanceUntilIdle()

    assertThat(fakeDataProvider.hasRetrieveBeenCalled).isFalse()
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testConvertToLiveData_fakeDataProvider_withObserver_callsRetrieve() {
    val fakeDataProvider = object : DataProvider<Int> {
      var hasRetrieveBeenCalled = false

      override fun getId(): Any = "fake_data_provider"

      override suspend fun retrieveData(): AsyncResult<Int> {
        hasRetrieveBeenCalled = true
        return AsyncResult.pending()
      }
    }

    dataProviders.convertToLiveData(fakeDataProvider).observeForever(mockIntLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    assertThat(fakeDataProvider.hasRetrieveBeenCalled).isTrue()
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testConvertToLiveData_trivialDataProvider_withObserver_observerReceivesValue() {
    val simpleDataProvider = object : DataProvider<Int> {
      override fun getId(): Any = "simple_data_provider"

      override suspend fun retrieveData(): AsyncResult<Int> = AsyncResult.success(123)
    }

    dataProviders.convertToLiveData(simpleDataProvider).observeForever(mockIntLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockIntLiveDataObserver).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isSuccess()).isTrue()
    assertThat(intResultCaptor.value.getOrThrow()).isEqualTo(123)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testConvertToLiveData_dataProviderChanges_withObserver_observerReceivesUpdatedValue() {
    var providerValue = 123
    val simpleDataProvider = object : DataProvider<Int> {
      override fun getId(): Any = "simple_data_provider"

      override suspend fun retrieveData(): AsyncResult<Int> = AsyncResult.success(providerValue)
    }
    dataProviders.convertToLiveData(simpleDataProvider).observeForever(mockIntLiveDataObserver)

    providerValue = 456
    asyncDataSubscriptionManager.notifyChangeAsync(simpleDataProvider.getId())
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockIntLiveDataObserver).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isSuccess()).isTrue()
    assertThat(intResultCaptor.value.getOrThrow()).isEqualTo(456)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testConvertToLiveData_providerChanges_withoutObserver_newObserver_newObserverReceivesValue() {
    var providerValue = 123
    val simpleDataProvider = object : DataProvider<Int> {
      override fun getId(): Any = "simple_data_provider"

      override suspend fun retrieveData(): AsyncResult<Int> = AsyncResult.success(providerValue)
    }
    providerValue = 456
    asyncDataSubscriptionManager.notifyChangeAsync(simpleDataProvider.getId())
    testCoroutineDispatchers.advanceUntilIdle()

    // Add an observer after the data provider has changed.
    dataProviders.convertToLiveData(simpleDataProvider).observeForever(mockIntLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    // The newer value should be observed.
    verify(mockIntLiveDataObserver).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isSuccess()).isTrue()
    assertThat(intResultCaptor.value.getOrThrow()).isEqualTo(456)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testConvertToLiveData_dataProviderNotified_sameValue_withObserver_observerNotCalledAgain() {
    val simpleDataProvider = object : DataProvider<Int> {
      override fun getId(): Any = "simple_data_provider"

      override suspend fun retrieveData(): AsyncResult<Int> = AsyncResult.success(123)
    }
    dataProviders.convertToLiveData(simpleDataProvider).observeForever(mockIntLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    // Reset the observer and notify the provider has changed without providing a new value.
    reset(mockIntLiveDataObserver)
    asyncDataSubscriptionManager.notifyChangeAsync(simpleDataProvider.getId())
    testCoroutineDispatchers.advanceUntilIdle()

    // The observer should have no interactions since the data hasn't changed.
    verifyZeroInteractions(mockIntLiveDataObserver)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testConvertToLiveData_multipleUpdatesNoObserver_newObserver_observerReceivesLatest() {
    val providerOldResult = AsyncResult.success(123)
    testCoroutineDispatchers.advanceTimeBy(10)
    val providerNewResult = AsyncResult.success(456)
    val simpleDataProvider = object : DataProvider<Int> {
      var callCount = 0

      override fun getId(): Any = "simple_data_provider"

      override suspend fun retrieveData(): AsyncResult<Int> {
        // Note that while this behavior is a bit contrived, it's actually representing a real
        // possibility that many different calls to retrieveData() race against each other and could
        // yield results from different times, resulting in out-of-order delivery. The oldest result
        // is assumed to be the correct to help encourage eventual consistency.
        return when (++callCount) {
          1 -> providerNewResult
          2 -> providerOldResult
          else -> AsyncResult.failed(AssertionError("Invalid test case"))
        }
      }
    }
    // Ensure the initial value is retrieved to ensure multiple retrievals occur.
    dataProviders.convertToLiveData(simpleDataProvider).observeForever(mockIntLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    asyncDataSubscriptionManager.notifyChangeAsync(simpleDataProvider.getId())
    testCoroutineDispatchers.advanceUntilIdle()

    // The more recent value should be observed despite it being retrieved first.
    verify(mockIntLiveDataObserver, atLeastOnce()).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isSuccess()).isTrue()
    assertThat(intResultCaptor.value.getOrThrow()).isEqualTo(456)
    assertThat(simpleDataProvider.callCount).isEqualTo(2) // Sanity check for the test logic itself.
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testInMemoryDataProvider_toLiveData_deliversInMemoryValue() {
    val dataProvider = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(STR_VALUE_0)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testInMemoryDataProvider_toLiveData_notifies_doesNotDeliverSameValueAgain() {
    val dataProvider = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    reset(mockStringLiveDataObserver)
    asyncDataSubscriptionManager.notifyChangeAsync(BASE_PROVIDER_ID_0)
    testCoroutineDispatchers.advanceUntilIdle()

    // The observer should not be notified again since the value hasn't changed.
    verifyZeroInteractions(mockStringLiveDataObserver)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testInMemoryDataProvider_toLiveData_withChangedValue_beforeReg_deliversSecondValue() {
    inMemoryCachedStr = STR_VALUE_0
    val dataProvider = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID_0) { inMemoryCachedStr!! }

    inMemoryCachedStr = STR_VALUE_1
    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(STR_VALUE_1)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testInMemoryDataProvider_toLiveData_withChangedValue_afterReg_deliversFirstValue() {
    inMemoryCachedStr = STR_VALUE_0
    val dataProvider = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID_0) { inMemoryCachedStr!! }

    // Ensure the initial state is sent before changing the cache.
    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()
    // Now change the cache & give it time to propagate.
    inMemoryCachedStr = STR_VALUE_1
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(STR_VALUE_0)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testInMemoryDataProvider_changedValueAfterReg_notified_deliversSecondValue() {
    inMemoryCachedStr = STR_VALUE_0
    val dataProvider = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID_0) { inMemoryCachedStr!! }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    inMemoryCachedStr = STR_VALUE_1
    asyncDataSubscriptionManager.notifyChangeAsync(BASE_PROVIDER_ID_0)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockStringLiveDataObserver, atLeastOnce()).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(STR_VALUE_1)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testInMemoryDataProvider_changedValue_notifiesDiffProvider_deliversFirstVal() {
    inMemoryCachedStr = STR_VALUE_0
    val dataProvider = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID_0) { inMemoryCachedStr!! }

    // Ensure the initial state is sent before changing the cache.
    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()
    // Now change the cache & give it time to propagate.
    inMemoryCachedStr = STR_VALUE_1
    asyncDataSubscriptionManager.notifyChangeAsync(OTHER_PROVIDER_ID)
    testCoroutineDispatchers.advanceUntilIdle()

    // The first value should be observed since a completely different provider was notified.
    verify(mockStringLiveDataObserver, atLeastOnce()).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(STR_VALUE_0)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testInMemoryDataProvider_toLiveData_withObserver_doesCallFunction() {
    // It would be nice to use a mock of the lambda (e.g. https://stackoverflow.com/a/53306974/3689782), but this
    // apparently does not work with Robolectric: https://github.com/robolectric/robolectric/issues/3688.
    var fakeLoadMemoryCallbackCalled = false
    val fakeLoadMemoryCallback: () -> String = {
      fakeLoadMemoryCallbackCalled = true
      STR_VALUE_0
    }
    val dataProvider = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID_0, fakeLoadMemoryCallback)

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    // With a LiveData observer, the load memory callback should be called.
    assertThat(fakeLoadMemoryCallbackCalled).isTrue()
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testInMemoryDataProvider_toLiveData_noObserver_doesNotCallFunction() {
    var fakeLoadMemoryCallbackCalled = false
    val fakeLoadMemoryCallback: () -> String = {
      fakeLoadMemoryCallbackCalled = true
      STR_VALUE_0
    }
    val dataProvider = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID_0, fakeLoadMemoryCallback)

    dataProviders.convertToLiveData(dataProvider)
    testCoroutineDispatchers.advanceUntilIdle()

    // Without a LiveData observer, the load memory callback should never be called.
    assertThat(fakeLoadMemoryCallbackCalled).isFalse()
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testInMemoryDataProvider_toLiveData_throwsException_deliversFailure() {
    val dataProvider = createThrowingDataProvider<String>(BASE_PROVIDER_ID_0, IllegalStateException("Failed"))
    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    //doNothing().`when`(mockCrashlyticsWrapper.logException(exception))

    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isFailure()).isTrue()
    assertThat(stringResultCaptor.value.getErrorOrNull()).isInstanceOf(IllegalStateException::class.java)
    verify(mockCrashlyticsWrapper).logException(exceptionCaptor.capture())
    assertThat(exceptionCaptor.value).isInstanceOf(IllegalStateException::class.java)

  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testAsyncInMemoryDataProvider_toLiveData_deliversInMemoryValue() {
    val dataProvider = dataProviders.createInMemoryDataProviderAsync(BASE_PROVIDER_ID_0) {
      AsyncResult.success(STR_VALUE_0)
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(STR_VALUE_0)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testAsyncInMemoryDataProvider_toLiveData_withChangedValue_beforeReg_deliversSecondValue() {
    inMemoryCachedStr = STR_VALUE_0
    val dataProvider = dataProviders.createInMemoryDataProviderAsync(BASE_PROVIDER_ID_0) {
      AsyncResult.success(inMemoryCachedStr!!)
    }

    inMemoryCachedStr = STR_VALUE_1
    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(STR_VALUE_1)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testAsyncInMemoryDataProvider_toLiveData_withChangedValue_afterReg_deliversFirstValue() {
    inMemoryCachedStr = STR_VALUE_0
    val dataProvider = dataProviders.createInMemoryDataProviderAsync(BASE_PROVIDER_ID_0) {
      AsyncResult.success(inMemoryCachedStr!!)
    }

    // Ensure the initial state is sent before changing the cache.
    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()
    // Now change the cache & give it time to propagate.
    inMemoryCachedStr = STR_VALUE_1
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(STR_VALUE_0)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testAsyncInMemoryDataProvider_changedValueAfterReg_notified_deliversValueTwo() {
    inMemoryCachedStr = STR_VALUE_0
    val dataProvider = dataProviders.createInMemoryDataProviderAsync(BASE_PROVIDER_ID_0) {
      AsyncResult.success(inMemoryCachedStr!!)
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    inMemoryCachedStr = STR_VALUE_1
    asyncDataSubscriptionManager.notifyChangeAsync(BASE_PROVIDER_ID_0)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockStringLiveDataObserver, atLeastOnce()).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(STR_VALUE_1)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testAsyncInMemoryDataProvider_blockingFunction_doesNotDeliver() {
    // Ensure the suspend operation is initially blocked.
    val blockingOperation = backgroundCoroutineScope.async { STR_VALUE_0 }
    val dataProvider = dataProviders.createInMemoryDataProviderAsync(BASE_PROVIDER_ID_0) {
      AsyncResult.success(blockingOperation.await())
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)

    // The observer should never be called since the underlying async function hasn't yet completed.
    verifyZeroInteractions(mockStringLiveDataObserver)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testAsyncInMemoryDataProvider_blockingFunctionCompleted_deliversValue() {
    // Ensure the suspend operation is initially blocked.
    val blockingOperation = backgroundCoroutineScope.async { STR_VALUE_0 }
    val dataProvider = dataProviders.createInMemoryDataProviderAsync(BASE_PROVIDER_ID_0) {
      AsyncResult.success(blockingOperation.await())
    }

    // Start observing the provider, then complete its suspend function.
    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    // Finish the blocking operation.
    testCoroutineDispatchers.advanceUntilIdle()

    // The provider will deliver a value immediately when the suspend function completes (no additional notification is
    // needed).
    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(STR_VALUE_0)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testAsyncInMemoryDataProvider_toLiveData_withObserver_doesCallFunction() {
    var fakeLoadMemoryCallbackCalled = false
    val fakeLoadMemoryCallback: suspend () -> AsyncResult<String> = {
      fakeLoadMemoryCallbackCalled = true
      AsyncResult.success(STR_VALUE_0)
    }
    val dataProvider = dataProviders.createInMemoryDataProviderAsync(BASE_PROVIDER_ID_0, fakeLoadMemoryCallback)

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    // With a LiveData observer, the load memory callback should be called.
    assertThat(fakeLoadMemoryCallbackCalled).isTrue()
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testAsyncInMemoryDataProvider_toLiveData_noObserver_doesNotCallFunction() {
    var fakeLoadMemoryCallbackCalled = false
    val fakeLoadMemoryCallback: suspend () -> AsyncResult<String> = {
      fakeLoadMemoryCallbackCalled = true
      AsyncResult.success(STR_VALUE_0)
    }
    val dataProvider = dataProviders.createInMemoryDataProviderAsync(BASE_PROVIDER_ID_0, fakeLoadMemoryCallback)

    dataProviders.convertToLiveData(dataProvider)
    testCoroutineDispatchers.advanceUntilIdle()

    // Without a LiveData observer, the load memory callback should never be called.
    assertThat(fakeLoadMemoryCallbackCalled).isFalse()
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testAsyncInMemoryDataProvider_toLiveData_pendingResult_deliversPendingResult() {
    val dataProvider = createPendingDataProvider<String>(BASE_PROVIDER_ID_0)

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isPending()).isTrue()
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testAsyncInMemoryDataProvider_toLiveData_failure_deliversFailure() {
    val dataProvider = createFailingDataProvider<String>(BASE_PROVIDER_ID_0, IllegalStateException("Failure"))

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isFailure()).isTrue()
    assertThat(stringResultCaptor.value.getErrorOrNull()).isInstanceOf(IllegalStateException::class.java)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testTransform_toLiveData_deliversTransformedValue() {
    val baseProvider = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val dataProvider = dataProviders.transform(TRANSFORMED_PROVIDER_ID, baseProvider) { transformString(it) }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockIntLiveDataObserver).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isSuccess()).isTrue()
    assertThat(intResultCaptor.value.getOrThrow()).isEqualTo(INT_XFORMED_STR_VALUE_0)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testTransform_toLiveData_differentValue_notifiesBase_deliversXformedValueTwo() {
    inMemoryCachedStr = STR_VALUE_0
    val baseProvider = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID_0) { inMemoryCachedStr!! }
    val dataProvider = dataProviders.transform(TRANSFORMED_PROVIDER_ID, baseProvider) { transformString(it) }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    inMemoryCachedStr = STR_VALUE_1
    asyncDataSubscriptionManager.notifyChangeAsync(BASE_PROVIDER_ID_0)
    testCoroutineDispatchers.advanceUntilIdle()

    // Notifying the base results in observers of the transformed provider also being called.
    verify(mockIntLiveDataObserver, atLeastOnce()).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isSuccess()).isTrue()
    assertThat(intResultCaptor.value.getOrThrow()).isEqualTo(INT_XFORMED_STR_VALUE_1)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testTransform_toLiveData_diffValue_notifiesXform_deliversXformedValueTwo() {
    inMemoryCachedStr = STR_VALUE_0
    val baseProvider = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID_0) { inMemoryCachedStr!! }
    val dataProvider = dataProviders.transform(TRANSFORMED_PROVIDER_ID, baseProvider) { transformString(it) }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    inMemoryCachedStr = STR_VALUE_1
    asyncDataSubscriptionManager.notifyChangeAsync(TRANSFORMED_PROVIDER_ID)
    testCoroutineDispatchers.advanceUntilIdle()

    // Notifying the transformed provider has the same result as notifying the base provider.
    verify(mockIntLiveDataObserver, atLeastOnce()).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isSuccess()).isTrue()
    assertThat(intResultCaptor.value.getOrThrow()).isEqualTo(INT_XFORMED_STR_VALUE_1)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testTransform_differentValue_notifiesBase_observeBase_deliversSecondValue() {
    inMemoryCachedStr = STR_VALUE_0
    val baseProvider = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID_0) { inMemoryCachedStr!! }
    val dataProvider = dataProviders.transform(TRANSFORMED_PROVIDER_ID, baseProvider) { transformString(it) }

    dataProviders.convertToLiveData(baseProvider).observeForever(mockStringLiveDataObserver)
    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    inMemoryCachedStr = STR_VALUE_1
    asyncDataSubscriptionManager.notifyChangeAsync(BASE_PROVIDER_ID_0)
    testCoroutineDispatchers.advanceUntilIdle()

    // Having a transformed data provider with an observer does not change the base's notification behavior.
    verify(mockStringLiveDataObserver, atLeastOnce()).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(STR_VALUE_1)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testTransform_differentValue_notifiesXformed_observeBase_deliversFirstValue() {
    inMemoryCachedStr = STR_VALUE_0
    val baseProvider = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID_0) { inMemoryCachedStr!! }
    val dataProvider = dataProviders.transform(TRANSFORMED_PROVIDER_ID, baseProvider) { transformString(it) }

    // Ensure the initial state is sent before changing the cache.
    dataProviders.convertToLiveData(baseProvider).observeForever(mockStringLiveDataObserver)
    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()
    // Now change the cache & give it time to propagate.
    inMemoryCachedStr = STR_VALUE_1
    asyncDataSubscriptionManager.notifyChangeAsync(TRANSFORMED_PROVIDER_ID)
    testCoroutineDispatchers.advanceUntilIdle()

    // However, notifying that the transformed provider has changed should not affect base subscriptions even if the
    // base has changed.
    verify(mockStringLiveDataObserver, atLeastOnce()).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(STR_VALUE_0)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testTransform_toLiveData_basePending_deliversPending() {
    val baseProvider = createPendingDataProvider<String>(BASE_PROVIDER_ID_0)
    val dataProvider = dataProviders.transform(TRANSFORMED_PROVIDER_ID, baseProvider) { transformString(it) }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockIntLiveDataObserver).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isPending()).isTrue()
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testTransform_toLiveData_baseFailure_deliversFailure() {
    val baseProvider = createFailingDataProvider<String>(BASE_PROVIDER_ID_0, IllegalStateException("Failure"))
    val dataProvider = dataProviders.transform(TRANSFORMED_PROVIDER_ID, baseProvider) { transformString(it) }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockIntLiveDataObserver).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isFailure()).isTrue()
    assertThat(intResultCaptor.value.getErrorOrNull()).isInstanceOf(AsyncResult.ChainedFailureException::class.java)
    assertThat(intResultCaptor.value.getErrorOrNull()).hasCauseThat().isInstanceOf(IllegalStateException::class.java)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testTransform_toLiveData_withObserver_callsTransform() {
    var fakeTransformCallbackCalled = false
    val fakeTransformCallback: (String) -> Int = {
      fakeTransformCallbackCalled = true
      transformString(it)
    }
    val baseProvider = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val dataProvider = dataProviders.transform(TRANSFORMED_PROVIDER_ID, baseProvider, fakeTransformCallback)

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    // A successful base provider with a LiveData observer should result in the transform function being called.
    assertThat(fakeTransformCallbackCalled).isTrue()
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testTransform_toLiveData_noObserver_doesNotCallTransform() {
    var fakeTransformCallbackCalled = false
    val fakeTransformCallback: (String) -> Int = {
      fakeTransformCallbackCalled = true
      transformString(it)
    }
    val baseProvider = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val dataProvider = dataProviders.transform(TRANSFORMED_PROVIDER_ID, baseProvider, fakeTransformCallback)

    dataProviders.convertToLiveData(dataProvider)
    testCoroutineDispatchers.advanceUntilIdle()

    // Without an observer, the transform method should not be called.
    assertThat(fakeTransformCallbackCalled).isFalse()
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testTransform_toLiveData_basePending_doesNotCallTransform() {
    var fakeTransformCallbackCalled = false
    val fakeTransformCallback: (String) -> Int = {
      fakeTransformCallbackCalled = true
      transformString(it)
    }
    val baseProvider = createPendingDataProvider<String>(BASE_PROVIDER_ID_0)
    val dataProvider = dataProviders.transform(TRANSFORMED_PROVIDER_ID, baseProvider, fakeTransformCallback)

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    // The transform method shouldn't be called if the base provider is in a pending state.
    assertThat(fakeTransformCallbackCalled).isFalse()
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testTransform_toLiveData_baseFailure_doesNotCallTransform() {
    var fakeTransformCallbackCalled = false
    val fakeTransformCallback: (String) -> Int = {
      fakeTransformCallbackCalled = true
      transformString(it)
    }
    val baseProvider = createFailingDataProvider<String>(BASE_PROVIDER_ID_0, IllegalStateException("Base failure"))
    val dataProvider = dataProviders.transform(TRANSFORMED_PROVIDER_ID, baseProvider, fakeTransformCallback)

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    // The transform method shouldn't be called if the base provider is in a failure state.
    assertThat(fakeTransformCallbackCalled).isFalse()
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testTransform_toLiveData_throwsException_deliversFailure() {
    val baseProvider = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val dataProvider = dataProviders.transform<String, Int>(TRANSFORMED_PROVIDER_ID, baseProvider) {
      throw IllegalStateException("Transform failure")
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    // Note that the exception type here is not chained since the failure occurred in the transform function.
    verify(mockIntLiveDataObserver).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isFailure()).isTrue()
    assertThat(intResultCaptor.value.getErrorOrNull()).isInstanceOf(IllegalStateException::class.java)
    assertThat(intResultCaptor.value.getErrorOrNull()).hasMessageThat().contains("Transform failure")
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testTransform_toLiveData_baseThrowsException_deliversFailure() {
    val baseProvider = createThrowingDataProvider<String>(BASE_PROVIDER_ID_0, IllegalStateException("Base failure"))
    val dataProvider = dataProviders.transform(TRANSFORMED_PROVIDER_ID, baseProvider) {
      transformString(it)
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockIntLiveDataObserver).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isFailure()).isTrue()
    assertThat(intResultCaptor.value.getErrorOrNull()).isInstanceOf(AsyncResult.ChainedFailureException::class.java)
    assertThat(intResultCaptor.value.getErrorOrNull()).hasCauseThat().isInstanceOf(IllegalStateException::class.java)
    assertThat(intResultCaptor.value.getErrorOrNull()).hasCauseThat().hasMessageThat().contains("Base failure")
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testTransformAsync_toLiveData_deliversTransformedValue() {
    val baseProvider = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val dataProvider = dataProviders.transformAsync(TRANSFORMED_PROVIDER_ID, baseProvider) { transformStringAsync(it) }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockIntLiveDataObserver).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isSuccess()).isTrue()
    assertThat(intResultCaptor.value.getOrThrow()).isEqualTo(INT_XFORMED_STR_VALUE_0)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testTransformAsync_toLiveData_diffValue_notifiesBase_deliversXformedValueTwo() {
    inMemoryCachedStr = STR_VALUE_0
    val baseProvider = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID_0) { inMemoryCachedStr!! }
    val dataProvider = dataProviders.transformAsync(TRANSFORMED_PROVIDER_ID, baseProvider) { transformStringAsync(it) }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    inMemoryCachedStr = STR_VALUE_1
    asyncDataSubscriptionManager.notifyChangeAsync(BASE_PROVIDER_ID_0)
    testCoroutineDispatchers.advanceUntilIdle()

    // Notifying the base results in observers of the transformed provider also being called.
    verify(mockIntLiveDataObserver, atLeastOnce()).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isSuccess()).isTrue()
    assertThat(intResultCaptor.value.getOrThrow()).isEqualTo(INT_XFORMED_STR_VALUE_1)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testTransformAsync_toLiveData_diffVal_notifiesXform_deliversXformedValueTwo() {
    inMemoryCachedStr = STR_VALUE_0
    val baseProvider = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID_0) { inMemoryCachedStr!! }
    val dataProvider = dataProviders.transformAsync(TRANSFORMED_PROVIDER_ID, baseProvider) { transformStringAsync(it) }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    inMemoryCachedStr = STR_VALUE_1
    asyncDataSubscriptionManager.notifyChangeAsync(TRANSFORMED_PROVIDER_ID)
    testCoroutineDispatchers.advanceUntilIdle()

    // Notifying the transformed provider has the same result as notifying the base provider.
    verify(mockIntLiveDataObserver, atLeastOnce()).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isSuccess()).isTrue()
    assertThat(intResultCaptor.value.getOrThrow()).isEqualTo(INT_XFORMED_STR_VALUE_1)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testTransformAsync_differentValue_notifiesBase_observeBase_deliversSecondVal() {
    inMemoryCachedStr = STR_VALUE_0
    val baseProvider = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID_0) { inMemoryCachedStr!! }
    val dataProvider = dataProviders.transformAsync(TRANSFORMED_PROVIDER_ID, baseProvider) { transformStringAsync(it) }

    dataProviders.convertToLiveData(baseProvider).observeForever(mockStringLiveDataObserver)
    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    inMemoryCachedStr = STR_VALUE_1
    asyncDataSubscriptionManager.notifyChangeAsync(BASE_PROVIDER_ID_0)
    testCoroutineDispatchers.advanceUntilIdle()

    // Having a transformed data provider with an observer does not change the base's notification behavior.
    verify(mockStringLiveDataObserver, atLeastOnce()).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(STR_VALUE_1)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testTransformAsync_diffValue_notifiesXformed_observeBase_deliversFirstVal() {
    inMemoryCachedStr = STR_VALUE_0
    val baseProvider = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID_0) { inMemoryCachedStr!! }
    val dataProvider = dataProviders.transformAsync(TRANSFORMED_PROVIDER_ID, baseProvider) { transformStringAsync(it) }

    // Ensure the initial state is sent before changing the cache.
    dataProviders.convertToLiveData(baseProvider).observeForever(mockStringLiveDataObserver)
    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()
    // Now change the cache & give it time to propagate.
    inMemoryCachedStr = STR_VALUE_1
    asyncDataSubscriptionManager.notifyChangeAsync(TRANSFORMED_PROVIDER_ID)
    testCoroutineDispatchers.advanceUntilIdle()

    // However, notifying that the transformed provider has changed should not affect base subscriptions even if the
    // base has changed.
    verify(mockStringLiveDataObserver, atLeastOnce()).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(STR_VALUE_0)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testTransformAsync_toLiveData_blockingFunction_doesNotDeliverValue() {
    // Block transformStringAsync().
    val baseProvider = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val dataProvider = dataProviders.transformAsync(TRANSFORMED_PROVIDER_ID, baseProvider) { transformStringAsync(it) }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)

    // No value should be delivered since the async function is blocked.
    verifyZeroInteractions(mockIntLiveDataObserver)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testTransformAsync_toLiveData_blockingFunction_completed_deliversXformedVal() {
    // Block transformStringAsync().
    val baseProvider = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val dataProvider = dataProviders.transformAsync(TRANSFORMED_PROVIDER_ID, baseProvider) { transformStringAsync(it) }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle() // Run transformStringAsync()

    // The value should now be delivered since the async function was unblocked.
    verify(mockIntLiveDataObserver).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isSuccess()).isTrue()
    assertThat(intResultCaptor.value.getOrThrow()).isEqualTo(INT_XFORMED_STR_VALUE_0)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testTransformAsync_toLiveData_blockingFunction_baseObserved_deliversFirstVal() {
    // Block transformStringAsync().
    val baseProvider = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val dataProvider = dataProviders.transformAsync(TRANSFORMED_PROVIDER_ID, baseProvider) { transformStringAsync(it) }

    // Observe the base provider & let it complete, but don't complete the derived data provider.
    dataProviders.convertToLiveData(baseProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()
    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)

    // Verify that even though the transformed provider is blocked, the base can still properly publish changes.
    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(STR_VALUE_0)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testTransformAsync_toLiveData_transformedPending_deliversPending() {
    val baseProvider = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val dataProvider = dataProviders.transformAsync<String, Int>(TRANSFORMED_PROVIDER_ID, baseProvider) {
      AsyncResult.pending()
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    // The transformation result yields a pending delivered result.
    verify(mockIntLiveDataObserver).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isPending()).isTrue()
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testTransformAsync_toLiveData_transformedFailure_deliversFailure() {
    val baseProvider = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val dataProvider = dataProviders.transformAsync<String, Int>(TRANSFORMED_PROVIDER_ID, baseProvider) {
      AsyncResult.failed(IllegalStateException("Transform failure"))
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    // Note that the failure exception in this case is not chained since the failure occurred in the transform function.
    verify(mockIntLiveDataObserver).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isFailure()).isTrue()
    assertThat(intResultCaptor.value.getErrorOrNull()).isInstanceOf(IllegalStateException::class.java)
    assertThat(intResultCaptor.value.getErrorOrNull()).hasMessageThat().contains("Transform failure")
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testTransformAsync_toLiveData_basePending_deliversPending() {
    val baseProvider = createPendingDataProvider<String>(BASE_PROVIDER_ID_0)
    val dataProvider = dataProviders.transformAsync(TRANSFORMED_PROVIDER_ID, baseProvider) { transformStringAsync(it) }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    // Since the base provider is pending, so is the transformed provider.
    verify(mockIntLiveDataObserver).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isPending()).isTrue()
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testTransformAsync_toLiveData_baseFailure_deliversFailure() {
    val baseProvider = createThrowingDataProvider<String>(BASE_PROVIDER_ID_0, IllegalStateException("Base failure"))
    val dataProvider = dataProviders.transformAsync(TRANSFORMED_PROVIDER_ID, baseProvider) {
      transformStringAsync(it)
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    // Note that the failure exception in this case is not chained since the failure occurred in the transform function.
    verify(mockIntLiveDataObserver).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isFailure()).isTrue()
    assertThat(intResultCaptor.value.getErrorOrNull()).isInstanceOf(AsyncResult.ChainedFailureException::class.java)
    assertThat(intResultCaptor.value.getErrorOrNull()).hasCauseThat().isInstanceOf(IllegalStateException::class.java)
    assertThat(intResultCaptor.value.getErrorOrNull()).hasCauseThat().hasMessageThat().contains("Base failure")
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testTransformAsync_toLiveData_withObserver_callsTransform() {
    var fakeTransformCallbackCalled = false
    val fakeTransformCallback: suspend (String) -> AsyncResult<Int> = {
      fakeTransformCallbackCalled = true
      transformStringAsync(it)
    }
    val baseProvider = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val dataProvider = dataProviders.transformAsync(TRANSFORMED_PROVIDER_ID, baseProvider, fakeTransformCallback)

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    // Since there's an observer, the transform method should be called.
    assertThat(fakeTransformCallbackCalled).isTrue()
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testTransformAsync_toLiveData_noObserver_doesNotCallTransform() {
    var fakeTransformCallbackCalled = false
    val fakeTransformCallback: suspend (String) -> AsyncResult<Int> = {
      fakeTransformCallbackCalled = true
      transformStringAsync(it)
    }
    val baseProvider = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val dataProvider = dataProviders.transformAsync(TRANSFORMED_PROVIDER_ID, baseProvider, fakeTransformCallback)

    dataProviders.convertToLiveData(dataProvider)
    testCoroutineDispatchers.advanceUntilIdle()

    // Without an observer, the transform method should not be called.
    assertThat(fakeTransformCallbackCalled).isFalse()
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testTransformAsync_toLiveData_basePending_doesNotCallTransform() {
    var fakeTransformCallbackCalled = false
    val fakeTransformCallback: suspend (String) -> AsyncResult<Int> = {
      fakeTransformCallbackCalled = true
      transformStringAsync(it)
    }
    val baseProvider = createPendingDataProvider<String>(BASE_PROVIDER_ID_0)
    val dataProvider = dataProviders.transformAsync(TRANSFORMED_PROVIDER_ID, baseProvider, fakeTransformCallback)

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    // A pending base provider should result in the transform method not being called.
    assertThat(fakeTransformCallbackCalled).isFalse()
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testTransformAsync_toLiveData_baseFailure_doesNotCallTransform() {
    var fakeTransformCallbackCalled = false
    val fakeTransformCallback: suspend (String) -> AsyncResult<Int> = {
      fakeTransformCallbackCalled = true
      transformStringAsync(it)
    }
    val baseProvider = createThrowingDataProvider<String>(BASE_PROVIDER_ID_0, IllegalStateException("Base failure"))
    val dataProvider = dataProviders.transformAsync(TRANSFORMED_PROVIDER_ID, baseProvider, fakeTransformCallback)

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    // A base provider failure should result in the transform method not being called.
    assertThat(fakeTransformCallbackCalled).isFalse()
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombine_toLiveData_deliversCombinedValue() {
    val baseProvider1 = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val baseProvider2 = createSuccessfulDataProvider(BASE_PROVIDER_ID_1, STR_VALUE_1)
    val dataProvider = dataProviders.combine(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2) { v1, v2 ->
      combineStrings(v1, v2)
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(COMBINED_STR_VALUE_01)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombine_firstProviderChanges_notifiesBase_deliversNewValue() {
    inMemoryCachedStr = STR_VALUE_0
    val baseProvider1 = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID_0) { inMemoryCachedStr!! }
    val baseProvider2 = createSuccessfulDataProvider(BASE_PROVIDER_ID_1, STR_VALUE_1)
    val dataProvider = dataProviders.combine(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2) { v1, v2 ->
      combineStrings(v1, v2)
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    inMemoryCachedStr = STR_VALUE_2
    asyncDataSubscriptionManager.notifyChangeAsync(BASE_PROVIDER_ID_0)
    testCoroutineDispatchers.advanceUntilIdle()

    // Notifying the first base provider results in observers of the combined provider also being called.
    verify(mockStringLiveDataObserver, atLeastOnce()).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(COMBINED_STR_VALUE_21)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombine_firstProviderChanges_notifiesCombined_deliversNewValue() {
    inMemoryCachedStr = STR_VALUE_0
    val baseProvider1 = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID_0) { inMemoryCachedStr!! }
    val baseProvider2 = createSuccessfulDataProvider(BASE_PROVIDER_ID_1, STR_VALUE_1)
    val dataProvider = dataProviders.combine(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2) { v1, v2 ->
      combineStrings(v1, v2)
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    inMemoryCachedStr = STR_VALUE_2
    asyncDataSubscriptionManager.notifyChangeAsync(COMBINED_PROVIDER_ID)
    testCoroutineDispatchers.advanceUntilIdle()

    // Notifying the combined provider results in observers of the combined provider also being called.
    verify(mockStringLiveDataObserver, atLeastOnce()).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(COMBINED_STR_VALUE_21)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombine_firstProviderChanges_observeBase_notifiesBase_deliversNewValue() {
    inMemoryCachedStr = STR_VALUE_0
    val baseProvider1 = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID_0) { inMemoryCachedStr!! }
    val baseProvider2 = createSuccessfulDataProvider(BASE_PROVIDER_ID_1, STR_VALUE_1)
    dataProviders.combine(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2) { v1, v2 ->
      combineStrings(v1, v2)
    }

    dataProviders.convertToLiveData(baseProvider1).observeForever(mockStringLiveDataObserver)
    inMemoryCachedStr = STR_VALUE_2
    asyncDataSubscriptionManager.notifyChangeAsync(BASE_PROVIDER_ID_0)
    testCoroutineDispatchers.advanceUntilIdle()

    // The combined data provider is irrelevant; the base provider's change should be observed.
    verify(mockStringLiveDataObserver, atLeastOnce()).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(STR_VALUE_2)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombine_firstProvChanges_observeBase_notifiesCombined_deliversOldValue() {
    inMemoryCachedStr = STR_VALUE_0
    val baseProvider1 = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID_0) { inMemoryCachedStr!! }
    val baseProvider2 = createSuccessfulDataProvider(BASE_PROVIDER_ID_1, STR_VALUE_1)
    dataProviders.combine(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2) { v1, v2 ->
      combineStrings(v1, v2)
    }

    // Ensure the initial state is sent before changing the cache.
    dataProviders.convertToLiveData(baseProvider1).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()
    // Now change the cache & give it time to propagate.
    inMemoryCachedStr = STR_VALUE_2
    asyncDataSubscriptionManager.notifyChangeAsync(COMBINED_PROVIDER_ID)
    testCoroutineDispatchers.advanceUntilIdle()

    // Notifying the combined data provider will not trigger observers of the changed provider becoming aware of the
    // change.
    verify(mockStringLiveDataObserver, atLeastOnce()).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(STR_VALUE_0)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombine_secondProviderChanges_notifiesBase_deliversNewValue() {
    inMemoryCachedStr = STR_VALUE_1
    val baseProvider1 = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val baseProvider2 = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID_1) { inMemoryCachedStr!! }
    val dataProvider = dataProviders.combine(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2) { v1, v2 ->
      combineStrings(v1, v2)
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    inMemoryCachedStr = STR_VALUE_2
    asyncDataSubscriptionManager.notifyChangeAsync(BASE_PROVIDER_ID_1)
    testCoroutineDispatchers.advanceUntilIdle()

    // Notifying the second base provider results in observers of the combined provider also being called.
    verify(mockStringLiveDataObserver, atLeastOnce()).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(COMBINED_STR_VALUE_02)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombine_secondProviderChanges_notifiesCombined_deliversNewValue() {
    inMemoryCachedStr = STR_VALUE_1
    val baseProvider1 = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val baseProvider2 = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID_1) { inMemoryCachedStr!! }
    val dataProvider = dataProviders.combine(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2) { v1, v2 ->
      combineStrings(v1, v2)
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    inMemoryCachedStr = STR_VALUE_2
    asyncDataSubscriptionManager.notifyChangeAsync(COMBINED_PROVIDER_ID)
    testCoroutineDispatchers.advanceUntilIdle()

    // Notifying the combined provider results in observers of the combined provider also being called.
    verify(mockStringLiveDataObserver, atLeastOnce()).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(COMBINED_STR_VALUE_02)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombine_secondProviderChanges_observeBase_notifiesBase_deliversNewValue() {
    inMemoryCachedStr = STR_VALUE_1
    val baseProvider1 = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val baseProvider2 = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID_1) { inMemoryCachedStr!! }
    dataProviders.combine(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2) { v1, v2 ->
      combineStrings(v1, v2)
    }

    dataProviders.convertToLiveData(baseProvider2).observeForever(mockStringLiveDataObserver)
    inMemoryCachedStr = STR_VALUE_2
    asyncDataSubscriptionManager.notifyChangeAsync(BASE_PROVIDER_ID_1)
    testCoroutineDispatchers.advanceUntilIdle()

    // The combined data provider is irrelevant; the base provider's change should be observed.
    verify(mockStringLiveDataObserver, atLeastOnce()).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(STR_VALUE_2)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombine_secondProvChanges_observeBase_notifiesCombined_deliversOldValue() {
    inMemoryCachedStr = STR_VALUE_1
    val baseProvider1 = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val baseProvider2 = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID_1) { inMemoryCachedStr!! }
    dataProviders.combine(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2) { v1, v2 ->
      combineStrings(v1, v2)
    }

    // Ensure the initial state is sent before changing the cache.
    dataProviders.convertToLiveData(baseProvider2).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()
    // Now change the cache & give it time to propagate.
    inMemoryCachedStr = STR_VALUE_2
    asyncDataSubscriptionManager.notifyChangeAsync(COMBINED_PROVIDER_ID)
    testCoroutineDispatchers.advanceUntilIdle()

    // Notifying the combined data provider will not trigger observers of the changed provider becoming aware of the
    // change.
    verify(mockStringLiveDataObserver, atLeastOnce()).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(STR_VALUE_1)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombine_firstProviderPending_deliversPending() {
    val baseProvider1 = createPendingDataProvider<String>(BASE_PROVIDER_ID_0)
    val baseProvider2 = createSuccessfulDataProvider(BASE_PROVIDER_ID_1, STR_VALUE_1)
    val dataProvider = dataProviders.combine(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2) { v1, v2 ->
      combineStrings(v1, v2)
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isPending()).isTrue()
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombine_secondProviderPending_deliversPending() {
    val baseProvider1 = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val baseProvider2 = createPendingDataProvider<String>(BASE_PROVIDER_ID_1)
    val dataProvider = dataProviders.combine(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2) { v1, v2 ->
      combineStrings(v1, v2)
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isPending()).isTrue()
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombine_bothProvidersPending_deliversPending() {
    val baseProvider1 = createPendingDataProvider<String>(BASE_PROVIDER_ID_0)
    val baseProvider2 = createPendingDataProvider<String>(BASE_PROVIDER_ID_1)
    val dataProvider = dataProviders.combine(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2) { v1, v2 ->
      combineStrings(v1, v2)
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isPending()).isTrue()
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombine_firstProviderFailing_deliversFailure() {
    val baseProvider1 = createFailingDataProvider<String>(BASE_PROVIDER_ID_0, IllegalStateException("Base 1 failure"))
    val baseProvider2 = createSuccessfulDataProvider(BASE_PROVIDER_ID_1, STR_VALUE_1)
    val dataProvider = dataProviders.combine(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2) { v1, v2 ->
      combineStrings(v1, v2)
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isFailure()).isTrue()
    assertThat(stringResultCaptor.value.getErrorOrNull()).isInstanceOf(AsyncResult.ChainedFailureException::class.java)
    assertThat(stringResultCaptor.value.getErrorOrNull()).hasCauseThat().isInstanceOf(IllegalStateException::class.java)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombine_secondProviderFailing_deliversFailure() {
    val baseProvider1 = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val baseProvider2 = createFailingDataProvider<String>(BASE_PROVIDER_ID_1, IllegalStateException("Base 2 failure"))
    val dataProvider = dataProviders.combine(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2) { v1, v2 ->
      combineStrings(v1, v2)
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isFailure()).isTrue()
    assertThat(stringResultCaptor.value.getErrorOrNull()).isInstanceOf(AsyncResult.ChainedFailureException::class.java)
    assertThat(stringResultCaptor.value.getErrorOrNull()).hasCauseThat().isInstanceOf(IllegalStateException::class.java)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombine_bothProvidersFailing_deliversFailure() {
    val baseProvider1 = createFailingDataProvider<String>(BASE_PROVIDER_ID_0, IllegalStateException("Base 1 failure"))
    val baseProvider2 = createFailingDataProvider<String>(BASE_PROVIDER_ID_1, IllegalStateException("Base 2 failure"))
    val dataProvider = dataProviders.combine(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2) { v1, v2 ->
      combineStrings(v1, v2)
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isFailure()).isTrue()
    assertThat(stringResultCaptor.value.getErrorOrNull()).isInstanceOf(AsyncResult.ChainedFailureException::class.java)
    assertThat(stringResultCaptor.value.getErrorOrNull()).hasCauseThat().isInstanceOf(IllegalStateException::class.java)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombine_toLiveData_withObserver_callsCombine() {
    var fakeCombineCallbackCalled = false
    val fakeCombineCallback: (String, String) -> String = { str1, str2 ->
      fakeCombineCallbackCalled = true
      combineStrings(str1, str2)
    }
    val baseProvider1 = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val baseProvider2 = createSuccessfulDataProvider(BASE_PROVIDER_ID_1, STR_VALUE_1)
    val dataProvider = dataProviders.combine(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2, fakeCombineCallback)

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    // Successful base providers with a LiveData observer present should result in the combine function being called.
    assertThat(fakeCombineCallbackCalled).isTrue()
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombine_toLiveData_withObserver_firstProviderPending_doesNotCallCombine() {
    var fakeCombineCallbackCalled = false
    val fakeCombineCallback: (String, String) -> String = { str1, str2 ->
      fakeCombineCallbackCalled = true
      combineStrings(str1, str2)
    }
    val baseProvider1 = createPendingDataProvider<String>(BASE_PROVIDER_ID_0)
    val baseProvider2 = createSuccessfulDataProvider(BASE_PROVIDER_ID_1, STR_VALUE_1)
    val dataProvider = dataProviders.combine(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2, fakeCombineCallback)

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    // One of the base providers not completing should result in the combine function not being called.
    assertThat(fakeCombineCallbackCalled).isFalse()
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombine_toLiveData_withObserver_secondProviderPending_doesNotCallCombine() {
    var fakeCombineCallbackCalled = false
    val fakeCombineCallback: (String, String) -> String = { str1, str2 ->
      fakeCombineCallbackCalled = true
      combineStrings(str1, str2)
    }
    val baseProvider1 = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val baseProvider2 = createPendingDataProvider<String>(BASE_PROVIDER_ID_1)
    val dataProvider = dataProviders.combine(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2, fakeCombineCallback)

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    // One of the base providers not completing should result in the combine function not being called.
    assertThat(fakeCombineCallbackCalled).isFalse()
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombine_toLiveData_withObserver_bothProvidersPending_doesNotCallCombine() {
    var fakeCombineCallbackCalled = false
    val fakeCombineCallback: (String, String) -> String = { str1, str2 ->
      fakeCombineCallbackCalled = true
      combineStrings(str1, str2)
    }
    val baseProvider1 = createPendingDataProvider<String>(BASE_PROVIDER_ID_0)
    val baseProvider2 = createPendingDataProvider<String>(BASE_PROVIDER_ID_1)
    val dataProvider = dataProviders.combine(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2, fakeCombineCallback)

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    // Both of the base providers not completing should result in the combine function not being called.
    assertThat(fakeCombineCallbackCalled).isFalse()
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombine_toLiveData_withObserver_firstProviderFailing_doesNotCallCombine() {
    var fakeCombineCallbackCalled = false
    val fakeCombineCallback: (String, String) -> String = { str1, str2 ->
      fakeCombineCallbackCalled = true
      combineStrings(str1, str2)
    }
    val baseProvider1 = createFailingDataProvider<String>(BASE_PROVIDER_ID_0, IllegalStateException("Base 1 failure"))
    val baseProvider2 = createSuccessfulDataProvider(BASE_PROVIDER_ID_1, STR_VALUE_1)
    val dataProvider = dataProviders.combine(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2, fakeCombineCallback)

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    // One of the base providers not completing should result in the combine function not being called.
    assertThat(fakeCombineCallbackCalled).isFalse()
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombine_toLiveData_withObserver_secondProviderFailing_doesNotCallCombine() {
    var fakeCombineCallbackCalled = false
    val fakeCombineCallback: (String, String) -> String = { str1, str2 ->
      fakeCombineCallbackCalled = true
      combineStrings(str1, str2)
    }
    val baseProvider1 = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val baseProvider2 = createFailingDataProvider<String>(BASE_PROVIDER_ID_1, IllegalStateException("Base 2 failure"))
    val dataProvider = dataProviders.combine(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2, fakeCombineCallback)

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    // One of the base providers not completing should result in the combine function not being called.
    assertThat(fakeCombineCallbackCalled).isFalse()
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombine_toLiveData_withObserver_bothProvidersFailing_doesNotCallCombine() {
    var fakeCombineCallbackCalled = false
    val fakeCombineCallback: (String, String) -> String = { str1, str2 ->
      fakeCombineCallbackCalled = true
      combineStrings(str1, str2)
    }
    val baseProvider1 = createFailingDataProvider<String>(BASE_PROVIDER_ID_0, IllegalStateException("Base 1 failure"))
    val baseProvider2 = createFailingDataProvider<String>(BASE_PROVIDER_ID_1, IllegalStateException("Base 2 failure"))
    val dataProvider = dataProviders.combine(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2, fakeCombineCallback)

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    // Both of the base providers not completing should result in the combine function not being called.
    assertThat(fakeCombineCallbackCalled).isFalse()
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombine_firstProviderThrowsException_deliversFailure() {
    val baseProvider1 = createThrowingDataProvider<String>(BASE_PROVIDER_ID_0, IllegalStateException("Base 1 failure"))
    val baseProvider2 = createSuccessfulDataProvider(BASE_PROVIDER_ID_1, STR_VALUE_1)
    val dataProvider = dataProviders.combine(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2) { v1, v2 ->
      combineStrings(v1, v2)
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isFailure()).isTrue()
    assertThat(stringResultCaptor.value.getErrorOrNull()).isInstanceOf(AsyncResult.ChainedFailureException::class.java)
    assertThat(stringResultCaptor.value.getErrorOrNull()).hasCauseThat().isInstanceOf(IllegalStateException::class.java)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombine_secondProviderThrowsException_deliversFailure() {
    val baseProvider1 = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val baseProvider2 = createThrowingDataProvider<String>(BASE_PROVIDER_ID_1, IllegalStateException("Base 2 failure"))
    val dataProvider = dataProviders.combine(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2) { v1, v2 ->
      combineStrings(v1, v2)
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isFailure()).isTrue()
    assertThat(stringResultCaptor.value.getErrorOrNull()).isInstanceOf(AsyncResult.ChainedFailureException::class.java)
    assertThat(stringResultCaptor.value.getErrorOrNull()).hasCauseThat().isInstanceOf(IllegalStateException::class.java)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombine_bothProvidersThrowExceptions_deliversFailure() {
    val baseProvider1 = createThrowingDataProvider<String>(BASE_PROVIDER_ID_0, IllegalStateException("Base 1 failure"))
    val baseProvider2 = createThrowingDataProvider<String>(BASE_PROVIDER_ID_1, IllegalStateException("Base 2 failure"))
    val dataProvider = dataProviders.combine(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2) { v1, v2 ->
      combineStrings(v1, v2)
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isFailure()).isTrue()
    assertThat(stringResultCaptor.value.getErrorOrNull()).isInstanceOf(AsyncResult.ChainedFailureException::class.java)
    assertThat(stringResultCaptor.value.getErrorOrNull()).hasCauseThat().isInstanceOf(IllegalStateException::class.java)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombine_combinerThrowsException_deliversFailure() {
    val baseProvider1 = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val baseProvider2 = createSuccessfulDataProvider(BASE_PROVIDER_ID_1, STR_VALUE_1)
    val dataProvider = dataProviders.combine<String, String, String>(
      COMBINED_PROVIDER_ID, baseProvider1, baseProvider2) { _, _ ->
      throw IllegalStateException("Combine failure")
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isFailure()).isTrue()
    assertThat(stringResultCaptor.value.getErrorOrNull()).isInstanceOf(IllegalStateException::class.java)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombineAsync_toLiveData_deliversCombinedValue() {
    val baseProvider1 = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val baseProvider2 = createSuccessfulDataProvider(BASE_PROVIDER_ID_1, STR_VALUE_1)
    val dataProvider = dataProviders.combineAsync(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2) { v1, v2 ->
      combineStringsAsync(v1, v2)
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(COMBINED_STR_VALUE_01)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombineAsync_firstProviderChanges_notifiesBase_deliversNewValue() {
    inMemoryCachedStr = STR_VALUE_0
    val baseProvider1 = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID_0) { inMemoryCachedStr!! }
    val baseProvider2 = createSuccessfulDataProvider(BASE_PROVIDER_ID_1, STR_VALUE_1)
    val dataProvider = dataProviders.combineAsync(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2) { v1, v2 ->
      combineStringsAsync(v1, v2)
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    inMemoryCachedStr = STR_VALUE_2
    asyncDataSubscriptionManager.notifyChangeAsync(BASE_PROVIDER_ID_0)
    testCoroutineDispatchers.advanceUntilIdle()

    // Notifying the first base provider results in observers of the combined provider also being called.
    verify(mockStringLiveDataObserver, atLeastOnce()).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(COMBINED_STR_VALUE_21)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombineAsync_firstProviderChanges_notifiesCombined_deliversNewValue() {
    inMemoryCachedStr = STR_VALUE_0
    val baseProvider1 = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID_0) { inMemoryCachedStr!! }
    val baseProvider2 = createSuccessfulDataProvider(BASE_PROVIDER_ID_1, STR_VALUE_1)
    val dataProvider = dataProviders.combineAsync(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2) { v1, v2 ->
      combineStringsAsync(v1, v2)
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    inMemoryCachedStr = STR_VALUE_2
    asyncDataSubscriptionManager.notifyChangeAsync(COMBINED_PROVIDER_ID)
    testCoroutineDispatchers.advanceUntilIdle()

    // Notifying the combined provider results in observers of the combined provider also being called.
    verify(mockStringLiveDataObserver, atLeastOnce()).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(COMBINED_STR_VALUE_21)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombineAsync_firstProvChanges_observeBase_notifiesBase_deliversNewValue() {
    inMemoryCachedStr = STR_VALUE_0
    val baseProvider1 = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID_0) { inMemoryCachedStr!! }
    val baseProvider2 = createSuccessfulDataProvider(BASE_PROVIDER_ID_1, STR_VALUE_1)
    dataProviders.combineAsync(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2) { v1, v2 ->
      combineStringsAsync(v1, v2)
    }

    dataProviders.convertToLiveData(baseProvider1).observeForever(mockStringLiveDataObserver)
    inMemoryCachedStr = STR_VALUE_2
    asyncDataSubscriptionManager.notifyChangeAsync(BASE_PROVIDER_ID_0)
    testCoroutineDispatchers.advanceUntilIdle()

    // The combined data provider is irrelevant; the base provider's change should be observed.
    verify(mockStringLiveDataObserver, atLeastOnce()).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(STR_VALUE_2)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombineAsync_firstProvChanges_obsrvBase_notifiesCombined_deliversOldVal() {
    inMemoryCachedStr = STR_VALUE_0
    val baseProvider1 = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID_0) { inMemoryCachedStr!! }
    val baseProvider2 = createSuccessfulDataProvider(BASE_PROVIDER_ID_1, STR_VALUE_1)
    dataProviders.combineAsync(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2) { v1, v2 ->
      combineStringsAsync(v1, v2)
    }

    // Ensure the initial state is sent before changing the cache.
    dataProviders.convertToLiveData(baseProvider1).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()
    // Now change the cache & give it time to propagate.
    inMemoryCachedStr = STR_VALUE_2
    asyncDataSubscriptionManager.notifyChangeAsync(COMBINED_PROVIDER_ID)
    testCoroutineDispatchers.advanceUntilIdle()

    // Notifying the combined data provider will not trigger observers of the changed provider becoming aware of the
    // change.
    verify(mockStringLiveDataObserver, atLeastOnce()).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(STR_VALUE_0)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombineAsync_secondProviderChanges_notifiesBase_deliversNewValue() {
    inMemoryCachedStr = STR_VALUE_1
    val baseProvider1 = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val baseProvider2 = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID_1) { inMemoryCachedStr!! }
    val dataProvider = dataProviders.combineAsync(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2) { v1, v2 ->
      combineStringsAsync(v1, v2)
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    inMemoryCachedStr = STR_VALUE_2
    asyncDataSubscriptionManager.notifyChangeAsync(BASE_PROVIDER_ID_1)
    testCoroutineDispatchers.advanceUntilIdle()

    // Notifying the second base provider results in observers of the combined provider also being called.
    verify(mockStringLiveDataObserver, atLeastOnce()).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(COMBINED_STR_VALUE_02)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombineAsync_secondProviderChanges_notifiesCombined_deliversNewValue() {
    inMemoryCachedStr = STR_VALUE_1
    val baseProvider1 = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val baseProvider2 = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID_1) { inMemoryCachedStr!! }
    val dataProvider = dataProviders.combineAsync(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2) { v1, v2 ->
      combineStringsAsync(v1, v2)
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    inMemoryCachedStr = STR_VALUE_2
    asyncDataSubscriptionManager.notifyChangeAsync(COMBINED_PROVIDER_ID)
    testCoroutineDispatchers.advanceUntilIdle()

    // Notifying the combined provider results in observers of the combined provider also being called.
    verify(mockStringLiveDataObserver, atLeastOnce()).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(COMBINED_STR_VALUE_02)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombineAsync_secondProvChanges_observeBase_notifiesBase_deliversNewValue() {
    inMemoryCachedStr = STR_VALUE_1
    val baseProvider1 = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val baseProvider2 = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID_1) { inMemoryCachedStr!! }
    dataProviders.combineAsync(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2) { v1, v2 ->
      combineStringsAsync(v1, v2)
    }

    dataProviders.convertToLiveData(baseProvider2).observeForever(mockStringLiveDataObserver)
    inMemoryCachedStr = STR_VALUE_2
    asyncDataSubscriptionManager.notifyChangeAsync(BASE_PROVIDER_ID_1)
    testCoroutineDispatchers.advanceUntilIdle()

    // The combined data provider is irrelevant; the base provider's change should be observed.
    verify(mockStringLiveDataObserver, atLeastOnce()).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(STR_VALUE_2)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombineAsync_secondProvChanges_obsrvBase_notifiesCombined_deliversOldVal() {
    inMemoryCachedStr = STR_VALUE_1
    val baseProvider1 = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val baseProvider2 = dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID_1) { inMemoryCachedStr!! }
    dataProviders.combineAsync(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2) { v1, v2 ->
      combineStringsAsync(v1, v2)
    }

    // Ensure the initial state is sent before changing the cache.
    dataProviders.convertToLiveData(baseProvider2).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()
    // Now change the cache & give it time to propagate.
    inMemoryCachedStr = STR_VALUE_2
    asyncDataSubscriptionManager.notifyChangeAsync(COMBINED_PROVIDER_ID)
    testCoroutineDispatchers.advanceUntilIdle()

    // Notifying the combined data provider will not trigger observers of the changed provider becoming aware of the
    // change.
    verify(mockStringLiveDataObserver, atLeastOnce()).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(STR_VALUE_1)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombineAsync_firstProviderPending_deliversPending() {
    val baseProvider1 = createPendingDataProvider<String>(BASE_PROVIDER_ID_0)
    val baseProvider2 = createSuccessfulDataProvider(BASE_PROVIDER_ID_1, STR_VALUE_1)
    val dataProvider = dataProviders.combineAsync(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2) { v1, v2 ->
      combineStringsAsync(v1, v2)
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isPending()).isTrue()
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombineAsync_secondProviderPending_deliversPending() {
    val baseProvider1 = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val baseProvider2 = createPendingDataProvider<String>(BASE_PROVIDER_ID_1)
    val dataProvider = dataProviders.combineAsync(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2) { v1, v2 ->
      combineStringsAsync(v1, v2)
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isPending()).isTrue()
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombineAsync_bothProvidersPending_deliversPending() {
    val baseProvider1 = createPendingDataProvider<String>(BASE_PROVIDER_ID_0)
    val baseProvider2 = createPendingDataProvider<String>(BASE_PROVIDER_ID_1)
    val dataProvider = dataProviders.combineAsync(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2) { v1, v2 ->
      combineStringsAsync(v1, v2)
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isPending()).isTrue()
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombineAsync_firstProviderFailing_deliversFailure() {
    val baseProvider1 = createFailingDataProvider<String>(BASE_PROVIDER_ID_0, IllegalStateException("Base 1 failure"))
    val baseProvider2 = createSuccessfulDataProvider(BASE_PROVIDER_ID_1, STR_VALUE_1)
    val dataProvider = dataProviders.combineAsync(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2) { v1, v2 ->
      combineStringsAsync(v1, v2)
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isFailure()).isTrue()
    assertThat(stringResultCaptor.value.getErrorOrNull()).isInstanceOf(AsyncResult.ChainedFailureException::class.java)
    assertThat(stringResultCaptor.value.getErrorOrNull()).hasCauseThat().isInstanceOf(IllegalStateException::class.java)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombineAsync_secondProviderFailing_deliversFailure() {
    val baseProvider1 = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val baseProvider2 = createFailingDataProvider<String>(BASE_PROVIDER_ID_1, IllegalStateException("Base 2 failure"))
    val dataProvider = dataProviders.combineAsync(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2) { v1, v2 ->
      combineStringsAsync(v1, v2)
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isFailure()).isTrue()
    assertThat(stringResultCaptor.value.getErrorOrNull()).isInstanceOf(AsyncResult.ChainedFailureException::class.java)
    assertThat(stringResultCaptor.value.getErrorOrNull()).hasCauseThat().isInstanceOf(IllegalStateException::class.java)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombineAsync_bothProvidersFailing_deliversFailure() {
    val baseProvider1 = createFailingDataProvider<String>(BASE_PROVIDER_ID_0, IllegalStateException("Base 1 failure"))
    val baseProvider2 = createFailingDataProvider<String>(BASE_PROVIDER_ID_1, IllegalStateException("Base 2 failure"))
    val dataProvider = dataProviders.combineAsync(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2) { v1, v2 ->
      combineStringsAsync(v1, v2)
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isFailure()).isTrue()
    assertThat(stringResultCaptor.value.getErrorOrNull()).isInstanceOf(AsyncResult.ChainedFailureException::class.java)
    assertThat(stringResultCaptor.value.getErrorOrNull()).hasCauseThat().isInstanceOf(IllegalStateException::class.java)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombineAsync_toLiveData_withObserver_callsCombine() {
    var fakeCombineCallbackCalled = false
    val fakeCombineCallback: suspend (String, String) -> AsyncResult<String> = { str1, str2 ->
      fakeCombineCallbackCalled = true
      combineStringsAsync(str1, str2)
    }
    val baseProvider1 = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val baseProvider2 = createSuccessfulDataProvider(BASE_PROVIDER_ID_1, STR_VALUE_1)
    val dataProvider = dataProviders.combineAsync(
      COMBINED_PROVIDER_ID, baseProvider1, baseProvider2, fakeCombineCallback
    )

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    // Successful base providers with a LiveData observer present should result in the combine function being called.
    assertThat(fakeCombineCallbackCalled).isTrue()
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombineAsync_toLiveData_withObserver_firstProvPending_doesNotCallCombine() {
    var fakeCombineCallbackCalled = false
    val fakeCombineCallback: suspend (String, String) -> AsyncResult<String> = { str1, str2 ->
      fakeCombineCallbackCalled = true
      combineStringsAsync(str1, str2)
    }
    val baseProvider1 = createPendingDataProvider<String>(BASE_PROVIDER_ID_0)
    val baseProvider2 = createSuccessfulDataProvider(BASE_PROVIDER_ID_1, STR_VALUE_1)
    val dataProvider = dataProviders.combineAsync(
      COMBINED_PROVIDER_ID, baseProvider1, baseProvider2, fakeCombineCallback
    )

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    // One of the base providers not completing should result in the combine function not being called.
    assertThat(fakeCombineCallbackCalled).isFalse()
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombineAsync_toLiveData_withObserver_secondProvPending_doesNotCallFunc() {
    var fakeCombineCallbackCalled = false
    val fakeCombineCallback: suspend (String, String) -> AsyncResult<String> = { str1, str2 ->
      fakeCombineCallbackCalled = true
      combineStringsAsync(str1, str2)
    }
    val baseProvider1 = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val baseProvider2 = createPendingDataProvider<String>(BASE_PROVIDER_ID_1)
    val dataProvider = dataProviders.combineAsync(
      COMBINED_PROVIDER_ID, baseProvider1, baseProvider2, fakeCombineCallback
    )

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    // One of the base providers not completing should result in the combine function not being called.
    assertThat(fakeCombineCallbackCalled).isFalse()
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombineAsync_toLiveData_withObserver_bothProvsPending_doesNotCallCombine() {
    var fakeCombineCallbackCalled = false
    val fakeCombineCallback: suspend (String, String) -> AsyncResult<String> = { str1, str2 ->
      fakeCombineCallbackCalled = true
      combineStringsAsync(str1, str2)
    }
    val baseProvider1 = createPendingDataProvider<String>(BASE_PROVIDER_ID_0)
    val baseProvider2 = createPendingDataProvider<String>(BASE_PROVIDER_ID_1)
    val dataProvider = dataProviders.combineAsync(
      COMBINED_PROVIDER_ID, baseProvider1, baseProvider2, fakeCombineCallback
    )

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    // Both of the base providers not completing should result in the combine function not being called.
    assertThat(fakeCombineCallbackCalled).isFalse()
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombineAsync_toLiveData_withObserver_firstProvFailing_doesNotCallCombine() {
    var fakeCombineCallbackCalled = false
    val fakeCombineCallback: suspend (String, String) -> AsyncResult<String> = { str1, str2 ->
      fakeCombineCallbackCalled = true
      combineStringsAsync(str1, str2)
    }
    val baseProvider1 = createFailingDataProvider<String>(BASE_PROVIDER_ID_0, IllegalStateException("Base 1 failure"))
    val baseProvider2 = createSuccessfulDataProvider(BASE_PROVIDER_ID_1, STR_VALUE_1)
    val dataProvider = dataProviders.combineAsync(
      COMBINED_PROVIDER_ID, baseProvider1, baseProvider2, fakeCombineCallback
    )

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    // One of the base providers not completing should result in the combine function not being called.
    assertThat(fakeCombineCallbackCalled).isFalse()
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombineAsync_toLiveData_withObserver_secondProvFailing_doesNotCallFunc() {
    var fakeCombineCallbackCalled = false
    val fakeCombineCallback: suspend (String, String) -> AsyncResult<String> = { str1, str2 ->
      fakeCombineCallbackCalled = true
      combineStringsAsync(str1, str2)
    }
    val baseProvider1 = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val baseProvider2 = createFailingDataProvider<String>(BASE_PROVIDER_ID_1, IllegalStateException("Base 2 failure"))
    val dataProvider = dataProviders.combineAsync(
      COMBINED_PROVIDER_ID, baseProvider1, baseProvider2, fakeCombineCallback
    )

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    // One of the base providers not completing should result in the combine function not being called.
    assertThat(fakeCombineCallbackCalled).isFalse()
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombineAsync_toLiveData_withObserver_bothProvsFailing_doesNotCallCombine() {
    var fakeCombineCallbackCalled = false
    val fakeCombineCallback: suspend (String, String) -> AsyncResult<String> = { str1, str2 ->
      fakeCombineCallbackCalled = true
      combineStringsAsync(str1, str2)
    }
    val baseProvider1 = createFailingDataProvider<String>(BASE_PROVIDER_ID_0, IllegalStateException("Base 1 failure"))
    val baseProvider2 = createFailingDataProvider<String>(BASE_PROVIDER_ID_1, IllegalStateException("Base 2 failure"))
    val dataProvider = dataProviders.combineAsync(
      COMBINED_PROVIDER_ID, baseProvider1, baseProvider2, fakeCombineCallback
    )

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    // Both of the base providers not completing should result in the combine function not being called.
    assertThat(fakeCombineCallbackCalled).isFalse()
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombineAsync_firstProviderThrowsException_deliversFailure() {
    val baseProvider1 = createThrowingDataProvider<String>(BASE_PROVIDER_ID_0, IllegalStateException("Base 1 failure"))
    val baseProvider2 = createSuccessfulDataProvider(BASE_PROVIDER_ID_1, STR_VALUE_1)
    val dataProvider = dataProviders.combineAsync(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2) { v1, v2 ->
      combineStringsAsync(v1, v2)
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isFailure()).isTrue()
    assertThat(stringResultCaptor.value.getErrorOrNull()).isInstanceOf(AsyncResult.ChainedFailureException::class.java)
    assertThat(stringResultCaptor.value.getErrorOrNull()).hasCauseThat().isInstanceOf(IllegalStateException::class.java)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombineAsync_secondProviderThrowsException_deliversFailure() {
    val baseProvider1 = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val baseProvider2 = createThrowingDataProvider<String>(BASE_PROVIDER_ID_1, IllegalStateException("Base 2 failure"))
    val dataProvider = dataProviders.combineAsync(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2) { v1, v2 ->
      combineStringsAsync(v1, v2)
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isFailure()).isTrue()
    assertThat(stringResultCaptor.value.getErrorOrNull()).isInstanceOf(AsyncResult.ChainedFailureException::class.java)
    assertThat(stringResultCaptor.value.getErrorOrNull()).hasCauseThat().isInstanceOf(IllegalStateException::class.java)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombineAsync_bothProvidersThrowExceptions_deliversFailure() {
    val baseProvider1 = createThrowingDataProvider<String>(BASE_PROVIDER_ID_0, IllegalStateException("Base 1 failure"))
    val baseProvider2 = createThrowingDataProvider<String>(BASE_PROVIDER_ID_1, IllegalStateException("Base 2 failure"))
    val dataProvider = dataProviders.combineAsync(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2) { v1, v2 ->
      combineStringsAsync(v1, v2)
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isFailure()).isTrue()
    assertThat(stringResultCaptor.value.getErrorOrNull()).isInstanceOf(AsyncResult.ChainedFailureException::class.java)
    assertThat(stringResultCaptor.value.getErrorOrNull()).hasCauseThat().isInstanceOf(IllegalStateException::class.java)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombineAsync_firstProviderIsBlocking_doesNotDeliver() {
    // Block the first provider.
    val baseProvider1 = createBlockingDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val baseProvider2 = createSuccessfulDataProvider(BASE_PROVIDER_ID_1, STR_VALUE_1)
    val dataProvider = dataProviders.combineAsync(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2) { v1, v2 ->
      // Note that this doesn't use combineStringsAsync since that relies on the blocked
      // backgroundTestCoroutineDispatcher.
      AsyncResult.success(combineStrings(v1, v2))
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)

    // The value should not yet be delivered since the first provider is blocked.
    verifyZeroInteractions(mockStringLiveDataObserver)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombineAsync_firstProviderIsBlocking_resumedAfterReg_valueDelivered() {
    // Block the first provider.
    val baseProvider1 = createBlockingDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val baseProvider2 = createSuccessfulDataProvider(BASE_PROVIDER_ID_1, STR_VALUE_1)
    val dataProvider = dataProviders.combineAsync(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2) { v1, v2 ->
      // Note that this doesn't use combineStringsAsync since that relies on the blocked
      // backgroundTestCoroutineDispatcher.
      AsyncResult.success(combineStrings(v1, v2))
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    // Resume the test thread after registration.
    testCoroutineDispatchers.advanceUntilIdle()

    // The value should now be delivered since the provider was allowed to finish.
    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(COMBINED_STR_VALUE_01)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombineAsync_secondProviderIsBlocking_doesNotDeliver() {
    // Block the second provider.
    val baseProvider1 = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val baseProvider2 = createBlockingDataProvider(BASE_PROVIDER_ID_1, STR_VALUE_1)
    val dataProvider = dataProviders.combineAsync(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2) { v1, v2 ->
      // Note that this doesn't use combineStringsAsync since that relies on the blocked
      // backgroundTestCoroutineDispatcher.
      AsyncResult.success(combineStrings(v1, v2))
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)

    // The value should not yet be delivered since the first provider is blocked.
    verifyZeroInteractions(mockStringLiveDataObserver)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombineAsync_secondProviderIsBlocking_resumedAfterReg_valueDelivered() {
    // Block first provider.
    val baseProvider1 = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val baseProvider2 = createBlockingDataProvider(BASE_PROVIDER_ID_1, STR_VALUE_1)
    val dataProvider = dataProviders.combineAsync(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2) { v1, v2 ->
      // Note that this doesn't use combineStringsAsync since that relies on the blocked
      // backgroundTestCoroutineDispatcher.
      AsyncResult.success(combineStrings(v1, v2))
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    // Resume the test thread after registration.
    testCoroutineDispatchers.advanceUntilIdle()

    // The value should now be delivered since the provider was allowed to finish.
    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(COMBINED_STR_VALUE_01)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombineAsync_combineFuncBlocked_doesNotDeliver() {
    // Block combineStringsAsync().
    val baseProvider1 = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val baseProvider2 = createSuccessfulDataProvider(BASE_PROVIDER_ID_1, STR_VALUE_1)
    val dataProvider = dataProviders.combineAsync(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2) { v1, v2 ->
      combineStringsAsync(v1, v2)
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)

    // The value should not yet be delivered.
    verifyZeroInteractions(mockStringLiveDataObserver)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombineAsync_combineFuncBlocked_resumedAfterRegistration_deliversValue() {
    // Block combineStringsAsync().
    val baseProvider1 = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val baseProvider2 = createSuccessfulDataProvider(BASE_PROVIDER_ID_1, STR_VALUE_1)
    val dataProvider = dataProviders.combineAsync(COMBINED_PROVIDER_ID, baseProvider1, baseProvider2) { v1, v2 ->
      combineStringsAsync(v1, v2)
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    // Allow the async function to complete.
    testCoroutineDispatchers.advanceUntilIdle()

    // The value should be delivered since the async function was allowed to finish.
    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(COMBINED_STR_VALUE_01)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombineAsync_combineReturnsPending_deliversPending() {
    val baseProvider1 = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val baseProvider2 = createSuccessfulDataProvider(BASE_PROVIDER_ID_1, STR_VALUE_1)
    val dataProvider = dataProviders.combineAsync<String, String, String>(
      COMBINED_PROVIDER_ID, baseProvider1, baseProvider2) { _, _ ->
      AsyncResult.pending()
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isPending()).isTrue()
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testCombineAsync_combineReturnsFailure_deliversFailure() {
    val baseProvider1 = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val baseProvider2 = createSuccessfulDataProvider(BASE_PROVIDER_ID_1, STR_VALUE_1)
    val dataProvider = dataProviders.combineAsync<String, String, String>(
      COMBINED_PROVIDER_ID, baseProvider1, baseProvider2) { _, _ ->
      AsyncResult.failed(IllegalStateException("Combine failure"))
    }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isFailure()).isTrue()
    assertThat(stringResultCaptor.value.getErrorOrNull()).isInstanceOf(IllegalStateException::class.java)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testNestedXformedProvider_toLiveData_deliversTransformedValue() {
    val baseProvider = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val dataProvider =
      dataProviders.createNestedTransformedDataProvider(TRANSFORMED_PROVIDER_ID, baseProvider) {
        transformStringAsync(it)
      }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockIntLiveDataObserver).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isSuccess()).isTrue()
    assertThat(intResultCaptor.value.getOrThrow()).isEqualTo(INT_XFORMED_STR_VALUE_0)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testNestedXformedProvider_toLiveData_diffValue_notifiesBase_deliversXformedValueTwo() {
    inMemoryCachedStr = STR_VALUE_0
    val baseProvider =
      dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID_0) { inMemoryCachedStr!! }
    val dataProvider =
      dataProviders.createNestedTransformedDataProvider(TRANSFORMED_PROVIDER_ID, baseProvider) {
        transformStringAsync(it)
      }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    inMemoryCachedStr = STR_VALUE_1
    asyncDataSubscriptionManager.notifyChangeAsync(BASE_PROVIDER_ID_0)
    testCoroutineDispatchers.advanceUntilIdle()

    // Notifying the base results in observers of the transformed provider also being called.
    verify(mockIntLiveDataObserver, atLeastOnce()).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isSuccess()).isTrue()
    assertThat(intResultCaptor.value.getOrThrow()).isEqualTo(INT_XFORMED_STR_VALUE_1)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testNestedXformedProvider_toLiveData_diffVal_notifiesXform_deliversXformedValueTwo() {
    inMemoryCachedStr = STR_VALUE_0
    val baseProvider =
      dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID_0) { inMemoryCachedStr!! }
    val dataProvider =
      dataProviders.createNestedTransformedDataProvider(TRANSFORMED_PROVIDER_ID, baseProvider) {
        transformStringAsync(it)
      }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    inMemoryCachedStr = STR_VALUE_1
    asyncDataSubscriptionManager.notifyChangeAsync(TRANSFORMED_PROVIDER_ID)
    testCoroutineDispatchers.advanceUntilIdle()

    // Notifying the transformed provider has the same result as notifying the base provider.
    verify(mockIntLiveDataObserver, atLeastOnce()).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isSuccess()).isTrue()
    assertThat(intResultCaptor.value.getOrThrow()).isEqualTo(INT_XFORMED_STR_VALUE_1)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testNestedXformedProvider_differentValue_notifiesBase_observeBase_deliversSecondVal() {
    inMemoryCachedStr = STR_VALUE_0
    val baseProvider =
      dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID_0) { inMemoryCachedStr!! }
    val dataProvider =
      dataProviders.createNestedTransformedDataProvider(TRANSFORMED_PROVIDER_ID, baseProvider) {
        transformStringAsync(it)
      }

    dataProviders.convertToLiveData(baseProvider).observeForever(mockStringLiveDataObserver)
    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    inMemoryCachedStr = STR_VALUE_1
    asyncDataSubscriptionManager.notifyChangeAsync(BASE_PROVIDER_ID_0)
    testCoroutineDispatchers.advanceUntilIdle()

    // Having a transformed data provider with an observer does not change the base's
    // notification behavior.
    verify(mockStringLiveDataObserver, atLeastOnce()).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(STR_VALUE_1)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testNestedXformedProvider_diffValue_notifiesXformed_observeBase_deliversFirstVal() {
    inMemoryCachedStr = STR_VALUE_0
    val baseProvider =
      dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID_0) { inMemoryCachedStr!! }
    val dataProvider =
      dataProviders.createNestedTransformedDataProvider(TRANSFORMED_PROVIDER_ID, baseProvider) {
        transformStringAsync(it)
      }

    // Ensure the initial state is sent before changing the cache.
    dataProviders.convertToLiveData(baseProvider).observeForever(mockStringLiveDataObserver)
    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()
    // Now change the cache & give it time to propagate.
    inMemoryCachedStr = STR_VALUE_1
    asyncDataSubscriptionManager.notifyChangeAsync(TRANSFORMED_PROVIDER_ID)
    testCoroutineDispatchers.advanceUntilIdle()

    // However, notifying that the transformed provider has changed should not affect base
    // subscriptions even if the base has changed.
    verify(mockStringLiveDataObserver, atLeastOnce()).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(STR_VALUE_0)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testNestedXformedProvider_toLiveData_blockingFunction_doesNotDeliverValue() {
    // Block transformStringAsync().
    val baseProvider = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val dataProvider =
      dataProviders.createNestedTransformedDataProvider(TRANSFORMED_PROVIDER_ID, baseProvider) {
        transformStringAsync(it)
      }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)

    // No value should be delivered since the async function is blocked.
    verifyZeroInteractions(mockIntLiveDataObserver)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testNestedXformedProvider_toLiveData_blockingFunction_completed_deliversXformedVal() {
    // Block transformStringAsync().
    val baseProvider = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val dataProvider =
      dataProviders.createNestedTransformedDataProvider(TRANSFORMED_PROVIDER_ID, baseProvider) {
        transformStringAsync(it)
      }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle() // Run transformStringAsync()

    // The value should now be delivered since the async function was unblocked.
    verify(mockIntLiveDataObserver).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isSuccess()).isTrue()
    assertThat(intResultCaptor.value.getOrThrow()).isEqualTo(INT_XFORMED_STR_VALUE_0)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testNestedXformedProvider_toLiveData_blockingFunction_baseObserved_deliversFirstVal() {
    // Block transformStringAsync().
    val baseProvider = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val dataProvider =
      dataProviders.createNestedTransformedDataProvider(TRANSFORMED_PROVIDER_ID, baseProvider) {
        transformStringAsync(it)
      }

    // Observe the base provider & let it complete, but don't complete the derived data provider.
    dataProviders.convertToLiveData(baseProvider).observeForever(mockStringLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()
    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)

    // Verify that even though the transformed provider is blocked, the base can still properly
    // publish changes.
    verify(mockStringLiveDataObserver).onChanged(stringResultCaptor.capture())
    assertThat(stringResultCaptor.value.isSuccess()).isTrue()
    assertThat(stringResultCaptor.value.getOrThrow()).isEqualTo(STR_VALUE_0)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testNestedXformedProvider_toLiveData_transformedPending_deliversPending() {
    val baseProvider = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val dataProvider =
      dataProviders.createNestedTransformedDataProvider<String, Int>(
        TRANSFORMED_PROVIDER_ID,
        baseProvider
      ) {
        AsyncResult.pending()
      }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    // The transformation result yields a pending delivered result.
    verify(mockIntLiveDataObserver).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isPending()).isTrue()
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testNestedXformedProvider_toLiveData_transformedFailure_deliversFailure() {
    val baseProvider = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val dataProvider =
      dataProviders.createNestedTransformedDataProvider<String, Int>(
        TRANSFORMED_PROVIDER_ID,
        baseProvider
      ) {
        AsyncResult.failed(IllegalStateException("Transform failure"))
      }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    // Note that the failure exception in this case is not chained since the failure occurred in the
    // transform function.
    verify(mockIntLiveDataObserver).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isFailure()).isTrue()
    assertThat(intResultCaptor.value.getErrorOrNull())
      .isInstanceOf(IllegalStateException::class.java)
    assertThat(intResultCaptor.value.getErrorOrNull())
      .hasMessageThat().contains("Transform failure")
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testNestedXformedProvider_toLiveData_basePending_deliversPending() {
    val baseProvider = createPendingDataProvider<String>(BASE_PROVIDER_ID_0)
    val dataProvider =
      dataProviders.createNestedTransformedDataProvider(TRANSFORMED_PROVIDER_ID, baseProvider) {
        transformStringAsync(it)
      }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    // Since the base provider is pending, so is the transformed provider.
    verify(mockIntLiveDataObserver).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isPending()).isTrue()
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testNestedXformedProvider_toLiveData_baseFailure_deliversFailure() {
    val baseProvider =
      createThrowingDataProvider<String>(BASE_PROVIDER_ID_0, IllegalStateException("Base failure"))
    val dataProvider =
      dataProviders.createNestedTransformedDataProvider(TRANSFORMED_PROVIDER_ID, baseProvider) {
        transformStringAsync(it)
      }

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    // Note that the failure exception in this case is not chained since the failure occurred in the
    // transform function.
    verify(mockIntLiveDataObserver).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isFailure()).isTrue()
    assertThat(intResultCaptor.value.getErrorOrNull())
      .isInstanceOf(AsyncResult.ChainedFailureException::class.java)
    assertThat(intResultCaptor.value.getErrorOrNull())
      .hasCauseThat().isInstanceOf(IllegalStateException::class.java)
    assertThat(intResultCaptor.value.getErrorOrNull())
      .hasCauseThat().hasMessageThat().contains("Base failure")
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testNestedXformedProvider_toLiveData_withObserver_callsTransform() {
    var fakeTransformCallbackCalled = false
    val fakeTransformCallback: suspend (String) -> AsyncResult<Int> = {
      fakeTransformCallbackCalled = true
      transformStringAsync(it)
    }
    val baseProvider = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val dataProvider =
      dataProviders.createNestedTransformedDataProvider(
        TRANSFORMED_PROVIDER_ID, baseProvider, fakeTransformCallback
      )

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    // Since there's an observer, the transform method should be called.
    assertThat(fakeTransformCallbackCalled).isTrue()
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testNestedXformedProvider_toLiveData_noObserver_doesNotCallTransform() {
    var fakeTransformCallbackCalled = false
    val fakeTransformCallback: suspend (String) -> AsyncResult<Int> = {
      fakeTransformCallbackCalled = true
      transformStringAsync(it)
    }
    val baseProvider = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val dataProvider =
      dataProviders.createNestedTransformedDataProvider(
        TRANSFORMED_PROVIDER_ID, baseProvider, fakeTransformCallback
      )

    dataProviders.convertToLiveData(dataProvider)
    testCoroutineDispatchers.advanceUntilIdle()

    // Without an observer, the transform method should not be called.
    assertThat(fakeTransformCallbackCalled).isFalse()
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testNestedXformedProvider_toLiveData_basePending_doesNotCallTransform() {
    var fakeTransformCallbackCalled = false
    val fakeTransformCallback: suspend (String) -> AsyncResult<Int> = {
      fakeTransformCallbackCalled = true
      transformStringAsync(it)
    }
    val baseProvider = createPendingDataProvider<String>(BASE_PROVIDER_ID_0)
    val dataProvider =
      dataProviders.createNestedTransformedDataProvider(
        TRANSFORMED_PROVIDER_ID, baseProvider, fakeTransformCallback
      )

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    // A pending base provider should result in the transform method not being called.
    assertThat(fakeTransformCallbackCalled).isFalse()
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testNestedXformedProvider_toLiveData_baseFailure_doesNotCallTransform() {
    var fakeTransformCallbackCalled = false
    val fakeTransformCallback: suspend (String) -> AsyncResult<Int> = {
      fakeTransformCallbackCalled = true
      transformStringAsync(it)
    }
    val baseProvider =
      createThrowingDataProvider<String>(BASE_PROVIDER_ID_0, IllegalStateException("Base failure"))
    val dataProvider =
      dataProviders.createNestedTransformedDataProvider(
        TRANSFORMED_PROVIDER_ID, baseProvider, fakeTransformCallback
      )

    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    // A base provider failure should result in the transform method not being called.
    assertThat(fakeTransformCallbackCalled).isFalse()
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testNestedXformedProvider_toLiveData_newBaseProvider_newObserver_receivesLatestValue() {
    val baseProvider1 = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val baseProvider2 = createSuccessfulDataProvider(BASE_PROVIDER_ID_1, STR_VALUE_1)
    val dataProvider =
      dataProviders.createNestedTransformedDataProvider(TRANSFORMED_PROVIDER_ID, baseProvider1) {
        transformStringAsync(it)
      }

    // Replace the base data provider with something new, then register an observer.
    dataProvider.setBaseDataProvider(baseProvider2) { transformStringAsync(it) }
    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    // The observer should get the newest value immediately.
    verify(mockIntLiveDataObserver).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isSuccess()).isTrue()
    assertThat(intResultCaptor.value.getOrThrow()).isEqualTo(INT_XFORMED_STR_VALUE_1)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testNestedXformedProvider_toLiveData_newBaseAndTransform_newObserver_receivesLatestValue() {
    val baseProvider1 = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val baseProvider2 = createSuccessfulDataProvider(BASE_PROVIDER_ID_1, STR_VALUE_0)
    val dataProvider =
      dataProviders.createNestedTransformedDataProvider(TRANSFORMED_PROVIDER_ID, baseProvider1) {
        transformStringAsync(it)
      }

    // Replace the base data provider with something new, then register an observer.
    dataProvider.setBaseDataProvider(baseProvider2) { transformStringDoubledAsync(it) }
    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    // The observer should get the newest value immediately.
    verify(mockIntLiveDataObserver).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isSuccess()).isTrue()
    assertThat(intResultCaptor.value.getOrThrow()).isEqualTo(INT_XFORMED_STR_VALUE_0_DOUBLED)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testNestedXformedProvider_toLiveData_newBaseProvider_notifiesObservers() {
    val baseProvider1 = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val baseProvider2 = createSuccessfulDataProvider(BASE_PROVIDER_ID_1, STR_VALUE_1)
    val dataProvider =
      dataProviders.createNestedTransformedDataProvider(TRANSFORMED_PROVIDER_ID, baseProvider1) {
        transformStringAsync(it)
      }
    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    testCoroutineDispatchers.advanceUntilIdle()

    // Replace the base data provider with something new. It should automatically notify observers.
    dataProvider.setBaseDataProvider(baseProvider2) { transformStringAsync(it) }
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockIntLiveDataObserver, atLeastOnce()).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isSuccess()).isTrue()
    assertThat(intResultCaptor.value.getOrThrow()).isEqualTo(INT_XFORMED_STR_VALUE_1)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testNestedXformedProvider_toLiveData_newBaseProvider_notifiedNewValue_isDelivered() {
    inMemoryCachedStr = STR_VALUE_1
    val baseProvider1 = createSuccessfulDataProvider(BASE_PROVIDER_ID_0, STR_VALUE_0)
    val baseProvider2 =
      dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID_1) { inMemoryCachedStr!! }
    val dataProvider =
      dataProviders.createNestedTransformedDataProvider(TRANSFORMED_PROVIDER_ID, baseProvider1) {
        transformStringAsync(it)
      }
    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    dataProvider.setBaseDataProvider(baseProvider2) { transformStringAsync(it) }
    testCoroutineDispatchers.advanceUntilIdle()

    // Update the new base provider and notify that it's changed, triggering a change in the
    // transformed provider.
    inMemoryCachedStr = STR_VALUE_2
    asyncDataSubscriptionManager.notifyChangeAsync(BASE_PROVIDER_ID_1)
    testCoroutineDispatchers.advanceUntilIdle()

    verify(mockIntLiveDataObserver, atLeastOnce()).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isSuccess()).isTrue()
    assertThat(intResultCaptor.value.getOrThrow()).isEqualTo(INT_XFORMED_STR_VALUE_2)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testNestedXformedProvider_toLiveData_newBaseProvider_notifyOldBase_doesNotDeliver() {
    inMemoryCachedStr = STR_VALUE_0
    val baseProvider1 =
      dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID_0) { inMemoryCachedStr!! }
    val baseProvider2 = createSuccessfulDataProvider(BASE_PROVIDER_ID_1, STR_VALUE_2)
    val dataProvider =
      dataProviders.createNestedTransformedDataProvider(TRANSFORMED_PROVIDER_ID, baseProvider1) {
        transformStringAsync(it)
      }
    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    dataProvider.setBaseDataProvider(baseProvider2) { transformStringAsync(it) }
    testCoroutineDispatchers.advanceUntilIdle()

    // Update the old base provider and notify that it's changed.
    inMemoryCachedStr = STR_VALUE_1
    asyncDataSubscriptionManager.notifyChangeAsync(BASE_PROVIDER_ID_0)
    testCoroutineDispatchers.advanceUntilIdle()

    // Since the base provider was replaced, it shouldn't result in any observed change.
    verify(mockIntLiveDataObserver, atLeastOnce()).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isSuccess()).isTrue()
    assertThat(intResultCaptor.value.getOrThrow()).isEqualTo(INT_XFORMED_STR_VALUE_2)
  }

  @Test
  @InternalCoroutinesApi
  @ExperimentalCoroutinesApi
  fun testNestedXformedProvider_toLiveData_newBaseProviderChanged_notifyOld_doesNotDeliverNewVal() {
    inMemoryCachedStr = STR_VALUE_0
    inMemoryCachedStr2 = STR_VALUE_1
    val baseProvider1 =
      dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID_0) { inMemoryCachedStr!! }
    val baseProvider2 =
      dataProviders.createInMemoryDataProvider(BASE_PROVIDER_ID_1) { inMemoryCachedStr2!! }
    val dataProvider =
      dataProviders.createNestedTransformedDataProvider(TRANSFORMED_PROVIDER_ID, baseProvider1) {
        transformStringAsync(it)
      }
    dataProviders.convertToLiveData(dataProvider).observeForever(mockIntLiveDataObserver)
    dataProvider.setBaseDataProvider(baseProvider2) { transformStringAsync(it) }
    testCoroutineDispatchers.advanceUntilIdle()

    // Update the new base provider but notify that the old one changed.
    inMemoryCachedStr2 = STR_VALUE_2
    asyncDataSubscriptionManager.notifyChangeAsync(BASE_PROVIDER_ID_0)
    testCoroutineDispatchers.advanceUntilIdle()

    // Since the base provider was replaced, the old notification should not trigger a newly
    // change even though the new base technically did change (but it wasn't notified yet).
    verify(mockIntLiveDataObserver, atLeastOnce()).onChanged(intResultCaptor.capture())
    assertThat(intResultCaptor.value.isSuccess()).isTrue()
    assertThat(intResultCaptor.value.getOrThrow()).isEqualTo(INT_XFORMED_STR_VALUE_1)
  }

  private fun transformString(str: String): Int {
    return str.length
  }

  /**
   * Transforms the specified string into an integer in the same way as [transformString], except in a blocking context
   * using [backgroundCoroutineDispatcher].
   */
  @ExperimentalCoroutinesApi
  private suspend fun transformStringAsync(str: String): AsyncResult<Int> {
    val deferred = backgroundCoroutineScope.async { transformString(str) }
    deferred.await()
    return AsyncResult.success(deferred.getCompleted())
  }

  /**
   * Transforms the specified string in a similar way as [transformStringAsync], but with a
   * different transformation method.
   */
  @ExperimentalCoroutinesApi
  private suspend fun transformStringDoubledAsync(str: String): AsyncResult<Int> {
    val deferred = backgroundCoroutineScope.async { transformString(str) * 2 }
    deferred.await()
    return AsyncResult.success(deferred.getCompleted())
  }

  private fun combineStrings(str1: String, str2: String): String {
    return "$str1 $str2"
  }

  /**
   * Combines the specified strings into a new string in the same way as [combineStrings], except in a blocking context
   * using [backgroundCoroutineDispatcher].
   */
  @ExperimentalCoroutinesApi
  private suspend fun combineStringsAsync(str1: String, str2: String): AsyncResult<String> {
    val deferred = backgroundCoroutineScope.async { combineStrings(str1, str2) }
    deferred.await()
    return AsyncResult.success(deferred.getCompleted())
  }

  private fun <T> createSuccessfulDataProvider(id: Any, value: T): DataProvider<T> {
    return dataProviders.createInMemoryDataProvider(id) { value }
  }

  private fun <T> createPendingDataProvider(id: Any): DataProvider<T> {
    return dataProviders.createInMemoryDataProviderAsync(id) {
      @Suppress("RemoveExplicitTypeArguments") // Android Studio incorrectly suggests to remove the explicit argument.
      AsyncResult.pending<T>()
    }
  }

  private fun <T> createFailingDataProvider(id: Any, failure: Exception): DataProvider<T> {
    return dataProviders.createInMemoryDataProviderAsync(id) {
      @Suppress("RemoveExplicitTypeArguments") // Android Studio incorrectly suggests to remove the explicit argument.
      AsyncResult.failed<T>(failure)
    }
  }

  private fun <T> createThrowingDataProvider(id: Any, failure: Exception): DataProvider<T> {
    return dataProviders.createInMemoryDataProvider(id) { throw failure }
  }

  /** Returns a successful [DataProvider] that uses a background thread to return the value. */
  @ExperimentalCoroutinesApi
  private fun <T> createBlockingDataProvider(id: Any, value: T): DataProvider<T> {
    return dataProviders.createInMemoryDataProviderAsync(id) {
      val deferred = backgroundCoroutineScope.async { value }
      deferred.await()
      AsyncResult.success(deferred.getCompleted())
    }
  }

  private fun setUpTestApplicationComponent() {
    DaggerDataProvidersTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
/*
    FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
*/
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

  @Module
  class TestCrashlyticsModule {
    @Provides
    @Singleton
    fun provideFirebaseCrashlytics(): FirebaseCrashlytics{
      return Mockito.mock(FirebaseCrashlytics::class.java)
    }

    @Provides
    @Singleton
    fun provideCrashlyticsWrapper(): CrashlyticsWrapper{
      return Mockito.mock(CrashlyticsWrapper::class.java)
    }
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(modules = [TestDispatcherModule::class, TestModule::class, TestCrashlyticsModule::class])
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
