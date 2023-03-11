package org.oppia.android.testing.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.test.platform.app.InstrumentationRegistry
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.atMost
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.reset
import org.mockito.Mockito.verify
import org.oppia.android.testing.data.AsyncResultSubject.Companion.assertThat
import org.oppia.android.testing.data.DataProviderTestMonitor.Factory
import org.oppia.android.testing.mockito.anyOrNull
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import java.lang.IllegalStateException
import javax.inject.Inject

/**
 * A test monitor for [DataProvider]s that provides operations to simplify waiting for the
 * provider's results, or to verify that notifications actually change the data provider when
 * expected.
 *
 * Note that this utility leverages [TestCoroutineDispatchers] to synchronize data providers with
 * the test thread. This may result in other operations unintentionally being completed, so care
 * should be taken when using this monitor in complex multi-operation scenarios. Further, this
 * utility does not internally support synchronizing against data providers which perform operations
 * at a future time (i.e. those that would require [TestCoroutineDispatchers.advanceTimeBy] to
 * synchronize). There are separate methods to use for these cases (e.g.
 * [ensureNextResultIsSuccess]) which can be called after manually synchronizing test dispatchers
 * (even for future timed operations).
 *
 * To use this monitor, inject its [Factory] and either create a new monitor or use an available
 * helper method.
 */
class DataProviderTestMonitor<T> private constructor(
  private val testCoroutineDispatchers: TestCoroutineDispatchers,
  private val dataProvider: DataProvider<T>
) {
  private val mockObserver by lazy { createMock<Observer<AsyncResult<T>>>() }
  private val liveData: LiveData<AsyncResult<T>> by lazy { dataProvider.toLiveData() }

  /**
   * Waits for the data provider to execute & returns the most recent [AsyncResult] produced by the
   * provider.
   *
   * This method assumes that the data provider has at least one update (which may be any result).
   *
   * This can be useful to verify that other operations notify the data provider since subsequent
   * calls to this method will reset state to ensure the latest state is always being observed.
   */
  fun waitForNextResult(): AsyncResult<T> {
    reset(mockObserver)
    testCoroutineDispatchers.runCurrent()
    return ensureNextResultIsPresent()
  }

  /**
   * Same as [waitForNextResult] except this also assumes that the most recent result is a success &
   * then returns the success value of the result.
   */
  fun waitForNextSuccessResult(): T = retrieveSuccess(this::waitForNextResult)

  /**
   * A version of [waitForNextSuccessResult] that doesn't wait for the operation. This is useful for
   * cases when the calling code is already synchronizing dispatchers and doing so again would break
   * monitor behavior.
   */
  fun ensureNextResultIsSuccess(): T = retrieveSuccess(this::ensureNextResultIsPresent)

  /**
   * Same as [waitForNextResult] except this also assumes that the most recent result is a failure &
   * then returns the reason for the result's failure.
   */
  fun waitForNextFailingResult(): Throwable = retrieveFailing(this::waitForNextResult)

  /** Same as [ensureNextResultIsSuccess] except for failing cases instead of successes. */
  fun ensureNextResultIsFailing(): Throwable = retrieveFailing(this::ensureNextResultIsPresent)

  /**
   * Waits for the data provider to potentially update, then verifies that no new result is
   * available from the provider. This can be used in contrast with [waitForNextResult] to validate
   * that the data provider has not been been notified or updated.
   */
  fun verifyProviderIsNotUpdated() {
    reset(mockObserver)
    testCoroutineDispatchers.runCurrent()
    verify(mockObserver, never()).onChanged(anyOrNull())
  }

  private fun startObservingDataProvider() {
    InstrumentationRegistry.getInstrumentation().runOnMainSync {
      liveData.observeForever(mockObserver)
    }
  }

  private fun stopObservingDataProvider() {
    InstrumentationRegistry.getInstrumentation().runOnMainSync {
      liveData.removeObserver(mockObserver)
    }
  }

  private fun ensureNextResultIsPresent(): AsyncResult<T> {
    // Note to reader: if you encounter the following line in a stack trace then that means the
    // monitored data provider has not updated in the time before calling one of the waitFor* or
    // ensure* methods. For uses of ensure* methods, this might mean you should use a waitFor*
    // method, instead. For uses of waitFor* methods, this might mean you're using one of the
    // Factory convenience methods instead of creating a monitor. If you're using a waitFor* method
    // and an explicit monitor, it either means the provider hasn't updated or it requires advancing
    // the clock (in which case you should use TestCoroutineDispatchers.advanceUntilIdle() and an
    // ensure* method from this class).
    return createCaptor<AsyncResult<T>>().also { resultCaptor ->
      verify(mockObserver, atLeastOnce()).onChanged(resultCaptor.capture())
      reset(mockObserver)
    }.value
  }

  private fun retrieveSuccess(operation: () -> AsyncResult<T>): T {
    return when (val result = operation()) {
      // Sanity check. Ensure that the full failure stack trace is thrown.
      is AsyncResult.Failure -> {
        throw IllegalStateException(
          "Expected next result to be a success, not: $result", result.error
        )
      }
      is AsyncResult.Pending -> error("Expected next result to be a success, not: $result")
      is AsyncResult.Success -> result.value
    }
  }

  private fun retrieveFailing(operation: () -> AsyncResult<T>): Throwable {
    return when (val result = operation()) {
      is AsyncResult.Failure -> result.error
      is AsyncResult.Pending, is AsyncResult.Success ->
        error("Expected next result to be a failure, not: $result")
    }
  }

  private fun collectAllResults(): List<AsyncResult<T>> {
    return createCaptor<AsyncResult<T>>().also { resultCaptor ->
      testCoroutineDispatchers.advanceUntilIdle()
      verify(mockObserver, AnyNumber).onChanged(resultCaptor.capture())
    }.allValues
  }

  /**
   * Factory for creating new [DataProviderTestMonitor]s. This class should be injected within tests
   * at the application scope & requires the host test application component to include the
   * necessary modules for [TestCoroutineDispatchers].
   */
  class Factory @Inject constructor(
    private val testCoroutineDispatchers: TestCoroutineDispatchers
  ) {
    /** Returns a new monitor for the specified [DataProvider]. */
    fun <T> createMonitor(dataProvider: DataProvider<T>): DataProviderTestMonitor<T> {
      return DataProviderTestMonitor(testCoroutineDispatchers, dataProvider).also {
        // Immediately start observing since it doesn't make sense not to always be observing for
        // the current monitor.
        it.startObservingDataProvider()
      }
    }

    /**
     * Convenience method for verifying that [dataProvider] has at least one result (whether it be
     * successful or an error), waiting if needed for the result (see [waitForNextResult]).
     *
     * This method ought to be used when data providers need to be processed mid-test since using
     * [waitForNextSuccessfulResult] or [waitForNextFailureResult] have the disadvantages that they
     * are also verifying pass/fail state (which is usually not desired mid-test during the
     * arrangement and act portions). While this method is also verifying something (execution), it
     * can be considered more of a sanity check than an actual check for correctness (i.e. "this
     * data provider must have executed for the test to proceed").
     *
     * Note that this will fail if the result of the data provider is pending (it must provide at
     * least one success or failure).
     */
    fun <T> ensureDataProviderExecutes(dataProvider: DataProvider<T>) {
      // Waiting for a result is the same as ensuring the conditions are right for the provider to
      // execute (since it must return a result if it's executed, even if it's pending).
      val monitor = createMonitor(dataProvider)
      monitor.waitForNextResult().also {
        monitor.stopObservingDataProvider()
      }.also {
        // There must be an actual result for the provider to be successful.
        assertThat(it).isNotPending()
      }
    }

    /**
     * Convenience function for monitoring the specified data provider & waiting for its next result
     * (expected to be a success). See [waitForNextSuccessResult] for specifics.
     *
     * This method ensures that only one result is ever captured from the data provider.
     */
    fun <T> waitForNextSuccessfulResult(dataProvider: DataProvider<T>): T {
      val monitor = createMonitor(dataProvider)
      return monitor.waitForNextSuccessResult().also { monitor.stopObservingDataProvider() }
    }

    /**
     * Convenience function for monitoring the specified data provider & waiting for its next result
     * (expected to be a failure). See [waitForNextFailingResult] for specifics.
     *
     * This method ensures that only one result is ever captured from the data provider.
     */
    fun <T> waitForNextFailureResult(dataProvider: DataProvider<T>): Throwable {
      val monitor = createMonitor(dataProvider)
      return monitor.waitForNextFailingResult().also { monitor.stopObservingDataProvider() }
    }

    /**
     * Monitors for all possible results that are received starting exactly from the moment a new
     * [DataProvider] is created by [createProvider].
     *
     * Note a few things:
     * 1. This method tries to keep provider creation as close as possible to observation, but the
     *    asynchronous nature of [DataProvider]s means that early results can still be missed.
     * 2. [DataProvider]s are not an event system. Their "eventual consistency" property means that
     *    the last result is most important to guarantee, so "in-between" states are possible to
     *    miss (even in carefully time-coordinated test environments like Oppia's). It's
     *    **highly suggested** to verify tests across ~100 runs that depend on this method to ensure
     *    that they cannot flake out. Callers may need to verify specific values are present in the
     *    list rather than verifying the whole list (since it could potentially change across test
     *    runs with only the ending being reliable).
     * 3. This method makes use of [TestCoroutineDispatchers.advanceUntilIdle] to minimize the
     *    possibility of missing events, so callers should be aware that this will cause all pending
     *    operations (even those in the future) to be run.
     * 4. Care needs to be taken when [createProvider] returns, rather than creates, a
     *    [DataProvider] as other methods in this class may cause the provider's results to be fully
     *    observed before this method runs (which will cause it to return an empty list since it
     *    only observes values *starting* at the time [createProvider] is called; any prior provided
     *    values will be lost except the provider's latest value).
     */
    fun <T> waitForAllNextResults(createProvider: () -> DataProvider<T>): List<AsyncResult<T>> {
      val monitor = createMonitor(createProvider())
      return monitor.collectAllResults().also { monitor.stopObservingDataProvider() }
    }
  }

  private companion object {
    private inline fun <reified T> createMock(): T = mock(T::class.java)

    private inline fun <reified T> createCaptor(): ArgumentCaptor<T> =
      ArgumentCaptor.forClass(T::class.java)

    // Approximate "any number" by limiting to a max number of calls.
    private val AnyNumber by lazy { atMost(Integer.MAX_VALUE) }
  }
}
