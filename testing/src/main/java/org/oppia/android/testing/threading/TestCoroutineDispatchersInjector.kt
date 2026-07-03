package org.oppia.android.testing.threading

// TODO(#6160): Migrate TestCoroutineDispatchersInjector to the proper injector/provider pattern
//  (similar to OppiaClockInjector/OppiaClockInjectorProvider). This class should not be used in
//  any new locations.
/**
 * A static injector for [TestCoroutineDispatchers] to allow them to be accessed in some static
 * contexts (like [org.oppia.android.testing.espresso.EditTextInputAction]).
 *
 * This is a temporary solution. The correct approach is to introduce a
 * [TestCoroutineDispatchersInjector] interface and a [TestCoroutineDispatchersInjectorProvider]
 * interface, implement them in test applications, and access the dispatchers via
 * [ApplicationProvider]. See [OppiaClockInjector] and [OppiaClockInjectorProvider] for reference.
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
