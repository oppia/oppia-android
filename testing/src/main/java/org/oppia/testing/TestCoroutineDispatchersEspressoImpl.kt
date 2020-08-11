package org.oppia.testing

import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import org.oppia.testing.TestCoroutineDispatcher.TaskIdleListener
import javax.inject.Inject

class TestCoroutineDispatchersEspressoImpl @Inject constructor(
  @BackgroundTestDispatcher private val backgroundTestDispatcher: TestCoroutineDispatcher,
  @BlockingTestDispatcher private val blockingTestDispatcher: TestCoroutineDispatcher
) : TestCoroutineDispatchers {
  private val idlingResource by lazy { TestCoroutineDispatcherIdlingResource() }
  private val dispatcherIdlenessTracker = DispatcherIdlenessTracker(
    arrayOf(backgroundTestDispatcher, blockingTestDispatcher)
  )

  override fun registerIdlingResource() {
    IdlingRegistry.getInstance().register(idlingResource)
    dispatcherIdlenessTracker.initialize()
  }

  override fun unregisterIdlingResource() {
    IdlingRegistry.getInstance().unregister(idlingResource)
  }

  override fun runCurrent() {
    advanceUntilIdle()
  }

  override fun advanceTimeBy(delayTimeMillis: Long) {
    // No actual sleep is needed since Espresso will automatically run until all tasks are
    // completed since idleness ties to all tasks, even future ones.
    advanceUntilIdle()
  }

  override fun advanceUntilIdle() {
    // Test coroutine dispatchers run in real-time, so let Espresso run until it idles.
    onIdle()
  }

  /** Returns whether any of the dispatchers have tasks that can be run now. */
  private fun hasPendingCompletableTasks(): Boolean {
    return backgroundTestDispatcher.hasPendingCompletableTasks() ||
      blockingTestDispatcher.hasPendingCompletableTasks()
  }

  // TODO: make this based on real-time. If running in Espresso, make the test coroutine dispatchers
  // use real-time and no-op the runCurrent/advanceUntilIdle since they are implied (or, rather,
  // make them call onIdle() since that's effectively what they're proxying in Robolectric land).
  private inner class TestCoroutineDispatcherIdlingResource : IdlingResource {
    private var resourceCallback: IdlingResource.ResourceCallback? = null

    override fun getName(): String {
      return "TestCoroutineDispatcherIdlingResource"
    }

    override fun isIdleNow(): Boolean {
      return !hasPendingCompletableTasks()
    }

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
      resourceCallback = callback
    }

    internal fun setToIdle() {
      resourceCallback?.onTransitionToIdle()
    }
  }

  private inner class DispatcherIdlenessTracker(
    private val dispatchers: Array<TestCoroutineDispatcher>
  ) {
    private val dispatcherRunningStates = Array(dispatchers.size) { false }

    internal fun initialize() {
      dispatchers.forEachIndexed { index, dispatcher ->
        dispatcher.setTaskIdleListener(object : TaskIdleListener {
          override fun onDispatcherRunning() {
            dispatcherRunningStates[index] = true
          }

          override fun onDispatcherIdle() {
            dispatcherRunningStates[index] = false
            notifyIfDispatchersAreIdle()
          }
        })
      }
    }

    private fun notifyIfDispatchersAreIdle() {
      if (!hasPendingCompletableTasks()) {
        idlingResource.setToIdle()
      }
    }
  }
}
