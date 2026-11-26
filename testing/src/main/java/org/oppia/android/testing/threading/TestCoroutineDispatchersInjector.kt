package org.oppia.android.testing.threading

/** Application-level injector for [TestCoroutineDispatchers]. */
interface TestCoroutineDispatchersInjector {
  /** Returns a [TestCoroutineDispatchers] from the Dagger graph. */
  fun getTestCoroutineDispatchers(): TestCoroutineDispatchers
}
