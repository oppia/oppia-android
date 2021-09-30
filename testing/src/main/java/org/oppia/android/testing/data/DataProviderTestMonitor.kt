package org.oppia.android.testing.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.reset
import org.mockito.Mockito.verify
import org.oppia.android.testing.data.DataProviderTestMonitor.Factory
import org.oppia.android.testing.mockito.anyOrNull
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

// TODO(#3813): Migrate all data provider tests over to using this utility.
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
  private val resultCaptor by lazy { createCaptor<AsyncResult<T>>() }
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
    liveData.observeForever(mockObserver)
  }

  private fun stopObservingDataProvider() {
    liveData.removeObserver(mockObserver)
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
    verify(mockObserver, atLeastOnce()).onChanged(resultCaptor.capture())
    reset(mockObserver)
    return resultCaptor.value
  }

  private fun retrieveSuccess(operation: () -> AsyncResult<T>): T {
    return operation().also {
      // Sanity check.
      check(it.isSuccess()) { "Expected next result to be a success, not: $it" }
    }.getOrThrow()
  }

  private fun retrieveFailing(operation: () -> AsyncResult<T>): Throwable {
    return operation().also {
      // Sanity check.
      check(it.isFailure()) { "Expected next result to be a failure, not: $it" }
    }.getErrorOrNull() ?: error("Expect result to have a failure error")
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
     * Convenience function for monitoring the specified data provider & waiting for its next result
     * (expected to be a success). See [waitForNextSuccessResult] for specifics.
     *
     * This method ensures that only one result is ever captured from the data provider.
     */
    fun <T> waitForNextSuccessfulResult(dataProvider: DataProvider<T>): T {
      val monitor = createMonitor(dataProvider)
      return monitor.waitForNextSuccessResult().also {
        monitor.stopObservingDataProvider()
      }
    }

    /**
     * Convenience function for monitoring the specified data provider & waiting for its next result
     * (expected to be a failure). See [waitForNextFailingResult] for specifics.
     *
     * This method ensures that only one result is ever captured from the data provider.
     */
    fun <T> waitForNextFailureResult(dataProvider: DataProvider<T>): Throwable {
      val monitor = createMonitor(dataProvider)
      return monitor.waitForNextFailingResult().also {
        monitor.stopObservingDataProvider()
      }
    }
  }

  private companion object {
    private inline fun <reified T> createMock(): T = mock(T::class.java)

    private inline fun <reified T> createCaptor(): ArgumentCaptor<T> =
      ArgumentCaptor.forClass(T::class.java)
  }
}
