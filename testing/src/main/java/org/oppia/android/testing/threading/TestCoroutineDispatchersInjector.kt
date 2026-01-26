package org.oppia.android.testing.threading

/**
 * A test-only injector to retrieve [TestCoroutineDispatchers] statically.
 *
 * This is used to avoid injecting [TestCoroutineDispatchers] into utility classes that should be
 * usage-site agnostic.
 */
object TestCoroutineDispatchersInjector {
  private var testCoroutineDispatchers: TestCoroutineDispatchers? = null

  /**
   * Initializes the static [TestCoroutineDispatchers] instance.
   *
   * This should only be called by [TestDispatcherModule].
   */
  fun initialize(dispatchers: TestCoroutineDispatchers) {
    testCoroutineDispatchers = dispatchers
  }

  /** Returns the current [TestCoroutineDispatchers]. */
  fun getDispatcher(): TestCoroutineDispatchers {
    return checkNotNull(testCoroutineDispatchers) {
      "TestCoroutineDispatchers not initialized. Ensure the test environment is set up correctly."
    }
  }
}
