package org.oppia.testing

// TODO(#1274): Add thorough testing for this class.

/**
 * Helper class to coordinate execution between all threads currently running in a test environment,
 * using both Robolectric for the main thread and [TestCoroutineDispatcher] for application-specific
 * threads.
 *
 * This class should be used at any point in a test where the test should ensure that a clean thread
 * synchronization point is needed (such as after an async operation is kicked off). This class can
 * guarantee that all threads enter a truly idle state (e.g. even in cases where execution "ping
 * pongs" across multiple threads will still be resolved with a single call to [runCurrent],
 * [advanceTimeBy], or [advanceUntilIdle]).
 *
 * Note that it's recommended all Robolectric tests that utilize this class run in a PAUSED looper
 * mode so that clock coordination is consistent between Robolectric's scheduler and this utility
 * class, otherwise unexpected inconsistencies may arise.
 *
 * *NOTE TO DEVELOPERS*: This class is NOT yet ready for broad use until after #89 is resolved.
 * Please ask in oppia-android-dev if you have a use case that you think requires this class.
 * Specific cases will be allowed to integrate with if other options are infeasible. Other tests
 * should rely on existing mechanisms until this utility is ready for broad use.
 */
interface TestCoroutineDispatchers {
  fun registerIdlingResource()
  fun unregisterIdlingResource()

  /**
   * Runs all current tasks pending, but does not follow up with executing any tasks that are
   * scheduled after this method finishes.
   *
   * It's recommended to always use this method when trying to bring a test to a reasonable idle
   * state since it doesn't change the clock time. If a test needs to advance time to complete some
   * operation, it should use [advanceTimeBy].
   */
  fun runCurrent()

  /**
   * Advances the system clock by the specified time in milliseconds and then ensures any new tasks
   * that were scheduled are fully executed before proceeding. This does not guarantee the
   * dispatchers enter an idle state, but it should guarantee that any tasks previously not executed
   * due to it not yet being the time for them to be executed may now run if the clock was
   * sufficiently forwarded. That is, running [runCurrent] after this method returns will do
   * nothing.
   *
   * It's recommended to always use this method when a test needs to wait for a specific future task
   * to complete. If a test doesn't require time to change to reach an idle state, [runCurrent]
   * should be used, instead. [advanceUntilIdle] should be reserved for cases when the test needs to
   * wait for a future operation, but doesn't know how long.
   */
  fun advanceTimeBy(delayTimeMillis: Long)

  /**
   * Runs all tasks on all tracked threads & coroutine dispatchers until no other tasks are pending.
   * However, tasks that require the clock to be advanced will likely not be run (depending on
   * whether the test under question is using a paused execution model, which is recommended for
   * Robolectric tests).
   *
   * It's only recommended to use this method in cases when a test needs to wait for a future task
   * to complete, but is unaware how long it needs to wait. [advanceTimeBy] and [runCurrent] are
   * preferred methods for synchronizing execution with tests since this method may have the
   * unintentional side effect of executing future tasks before the test anticipates it.
   */
  fun advanceUntilIdle()
}
