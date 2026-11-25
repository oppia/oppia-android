package org.oppia.android.testing.threading

/** Provider for [TestCoroutineDispatchersInjector]. */
interface TestCoroutineDispatchersInjectorProvider {
  /** Returns a [TestCoroutineDispatchersInjector]. */
  fun getTestCoroutineDispatchersInjector(): TestCoroutineDispatchersInjector
}
