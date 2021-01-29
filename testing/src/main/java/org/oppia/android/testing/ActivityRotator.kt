package org.oppia.android.testing

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import javax.inject.Inject

/**
 * Helper class to aid in activity rotations during Robolectric & Espresso tests. See
 * [Companion.rotateToPortrait] & [Companion.rotateToLandscape] for more details on how to use these
 * functions.
 */
class ActivityRotator @Inject constructor(
  private val testCoroutineDispatchers: TestCoroutineDispatchers,
  @IsOnRobolectric private val isOnRobolectric: Boolean
) {
  private val runtimeEnvironmentClass by lazy {
    Class.forName("org.robolectric.RuntimeEnvironment")
  }
  private val setQualifiersMethod by lazy {
    runtimeEnvironmentClass.getDeclaredMethod("setQualifiers", String::class.java)
  }

  private fun <A : Activity> changeOrientation(
    scenario: ActivityScenario<A>,
    newOrientation: Orientation
  ) {
    if (isOnRobolectric) {
      // Robolectric doesn't respect setting the requested orientation, but dynamically changing the
      // environment's qualifiers serves the same purpose: it forces a new configuration to be
      // computed. The activity still needs to be recreated to simulate the OS performing an actual
      // configuration change.
      setRobolectricQualifiers("+${newOrientation.robolectricOrientation}")
      scenario.recreate()
    } else {
      // Setting the requested orientation is sufficient to trigger an orientation change on
      // Espresso.
      scenario.onActivity { activity ->
        activity.requestedOrientation = newOrientation.androidOrientation
      }
    }

    // Always synchronize all operations after a configuration change since the operation can be
    // really expensive & often initiates async background flows.
    testCoroutineDispatchers.runCurrent()
  }

  private fun setRobolectricQualifiers(newQualifiers: String) {
    setQualifiersMethod.invoke(/* obj= */ null, newQualifiers)
  }

  private enum class Orientation(
    internal val androidOrientation: Int,
    internal val robolectricOrientation: String
  ) {
    PORTRAIT(androidOrientation = SCREEN_ORIENTATION_PORTRAIT, robolectricOrientation = "port"),
    LANDSCAPE(androidOrientation = SCREEN_ORIENTATION_LANDSCAPE, robolectricOrientation = "land")
  }

  companion object {
    /**
     * Rotates the [Activity] being managed by this [ActivityScenario] to portrait, or keeps it the
     * same if the activity is already in portrait mode. This method will work for both Robolectric
     * and Espresso tests.
     *
     * Note that this method does not guarantee activity recreations happening/not happening in the
     * situation where the orientation doesn't change. It's the responsibility of the calling test
     * to ensure this function is only called when an orientation will occur if the test is
     * sensitive to extra activity recreations.
     *
     * This method will synchronize background thread operations (e.g. with
     * [TestCoroutineDispatchers.runCurrent]) to ensure the potentially new activity returns to a
     * stable state.
     *
     * Note that this method cannot be used unless the host test's TestApplication implements
     * [Provider] & the test's test ApplicationComponent implements [Injector].
     */
    fun <A : Activity> ActivityScenario<A>.rotateToPortrait() =
      retrieveActivityRotator().changeOrientation(
        scenario = this,
        newOrientation = Orientation.PORTRAIT
      )

    /**
     * Rotates the [Activity] being managed by this [ActivityScenario] to a landscape orientation.
     * See [rotateToPortrait] for specifics on how to use this method correctly.
     */
    fun <A : Activity> ActivityScenario<A>.rotateToLandscape() =
      retrieveActivityRotator().changeOrientation(
        scenario = this,
        newOrientation = Orientation.LANDSCAPE
      )

    private fun retrieveActivityRotator(): ActivityRotator {
      val context = ApplicationProvider.getApplicationContext<Context>()
      check(context is Provider) {
        "Error: the TestApplication is not implementing ActivityRotator.Provider. Please update " +
          "he test component before using the companion rotateToLandscape/rotateToPortrait " +
          "helper functions."
      }
      return context.getActivityRotatorInjector().getActivityRotator()
    }
  }

  /**
   * Custom application-level injector that needs to be implemented by test ApplicationComponent
   * classes in order to use [ActivityRotator]'s companion functions.
   *
   * This must be used in conjunction with [Provider].
   */
  interface Injector {
    fun getActivityRotator(): ActivityRotator
  }

  /**
   * Custom application-level provider that needs to be implemented by TestApplication classes in
   * order to use [ActivityRotator]'s companion functions.
   *
   * This must be used in conjunction with [Injector].
   */
  interface Provider {
    /**
     * Returns the [Injector] corresponding to the test application.
     *
     * Note that this is expected to just return the test application component since the component
     * should be implementing [Injector].
     */
    fun getActivityRotatorInjector(): Injector
  }
}
