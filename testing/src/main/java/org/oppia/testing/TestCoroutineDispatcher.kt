package org.oppia.testing;

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.test.DelayController

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
abstract class TestCoroutineDispatcher: CoroutineDispatcher(), Delay, DelayController {
  abstract fun hasPendingTasks(): Boolean

  /**
   * Returns the clock time at which the next future task will execute ('future' indicates that the
   * task cannot execute right now due to its execution time being in the future).
   */
  abstract fun getNextFutureTaskCompletionTimeMillis(timeMillis: Long): Long?
  abstract fun hasPendingCompletableTasks(): Boolean

  abstract fun setTaskIdleListener(taskIdleListener: TaskIdleListener)

  interface TaskIdleListener {
    // Can be called on different threads.
    fun onDispatcherRunning()

    // Can be called on different threads.
    fun onDispatcherIdle()
  }

  interface Factory {
    fun createDispatcher(realDispatcher: CoroutineDispatcher): TestCoroutineDispatcher
  }
}
