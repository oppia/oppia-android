package org.oppia.android.testing.threading

/**
 * A static injector for [TestCoroutineDispatchers] to allow them to be accessed in some static
 * contexts (like [EditTextInputAction]).
 */
object TestCoroutineDispatchersInjector {
  private lateinit var dispatchers: TestCoroutineDispatchers

  /** Initializes the injector with the provided [dispatchers]. */
  fun initialize(dispatchers: TestCoroutineDispatchers) {
    this.dispatchers = dispatchers
  }

  /** Returns the injected [TestCoroutineDispatchers]. */
  fun getDispatcher(): TestCoroutineDispatchers {
    return dispatchers
  }
}
