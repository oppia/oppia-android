package org.oppia.android.testing.threading

import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

/**
 * Espresso-specific implementation of [TestCoroutineDispatchers].
 *
 * This utilizes a real-time clock and provides hooks for an [IdlingResource] that Espresso can use
 * to monitor background coroutines being run as part of the application.
 */
class TestCoroutineDispatchersEspressoImpl @Inject constructor(
  private val monitoredTaskCoordinators: Set<@JvmSuppressWildcards MonitoredTaskCoordinator>
) : TestCoroutineDispatchers {
  private val idlingResource by lazy { TestCoroutineDispatcherIdlingResource() }
  private val monitoredExecutorIdlenessTracker =
    MonitoredExecutorIdlenessTracker(monitoredTaskCoordinators)

  override fun registerIdlingResource() {
    IdlingRegistry.getInstance().register(idlingResource)
    monitoredExecutorIdlenessTracker.initialize()
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
  private fun hasPendingCompletableTasks(): Boolean =
    monitoredTaskCoordinators.any(MonitoredTaskCoordinator::hasPendingCompletableTasks)

  /** [IdlingResource] used to communicate task execution state to Espresso. */
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

    /** Notifies Espresso that the executors coordinated by this implementation are now idle. */
    fun setToIdle() {
      resourceCallback?.onTransitionToIdle()
    }
  }

  /**
   * Helper class to track idleness among a group of [MonitoredTaskCoordinator]s.
   *
   * 'Idleness' here is defined as all of the coordinators being idle per their own contracts for
   * idleness.
   */
  private inner class MonitoredExecutorIdlenessTracker(
    private val executors: Collection<MonitoredTaskCoordinator>
  ) {
    private val dispatcherRunningStates = Array(executors.size) { AtomicBoolean() }

    /**
     * Registers idle listeners among all provided [executors] to begin tracking whether they're
     * idle.
     */
    fun initialize() {
      executors.forEachIndexed { index, dispatcher ->
        dispatcher.setTaskIdleListener(object : MonitoredTaskCoordinator.TaskIdleListener {
          override fun onCoordinatorRunning() {
            dispatcherRunningStates[index].set(true)
          }

          override fun onCoordinatorIdle() {
            dispatcherRunningStates[index].set(false)
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
