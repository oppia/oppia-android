package org.oppia.android.instrumentation

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import org.junit.Assert.assertNotNull

/** This object contains common operations used for end-to-end tests. */
object EndToEndTestHelper {

  private val OPPIA_PACKAGE = "org.oppia.android"
  private val LAUNCH_TIMEOUT = 30000L
  private val TRANSITION_TIMEOUT = 5000L

  /** Starts oppia from HomeScreen. */
  fun UiDevice.startOppiaFromScratch() {
    // Start from the home screen
    this.pressHome()

    // Wait for launcher
    val launcherPackage = launcherPackageName
    assertNotNull(launcherPackage)
    this.wait(Until.hasObject(By.pkg(launcherPackage).depth(1)), LAUNCH_TIMEOUT)

    // Launch the blueprint app
    val context = ApplicationProvider.getApplicationContext<Context>()
    val intent = context.packageManager
      .getLaunchIntentForPackage(OPPIA_PACKAGE)
    intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK) // Clear out any previous instances
    context.startActivity(intent)

    // Wait for the app to appear
    this.wait(Until.hasObject(By.pkg(OPPIA_PACKAGE)), LAUNCH_TIMEOUT)
  }

  /** Waits for the view with given resource name to appear. */
  fun UiDevice.waitForRes(resourceName: String, timeout: Long = TRANSITION_TIMEOUT) {
    this.wait(Until.hasObject(By.res(resourceName)), timeout)
  }

  /** Scrolls to the view with given text. */
  fun scrollToText(text: String, isVertical: Boolean = true) {
    val recyclerview = UiScrollable(UiSelector().scrollable(true))
    if (isVertical) {
      recyclerview.setAsVerticalList()
    } else {
      recyclerview.setAsHorizontalList()
    }
    recyclerview.scrollTextIntoView(text)
  }

  /** Scrolls to the view with given UiObject. */
  fun scrollToView(uiSelector: UiSelector, isVertical: Boolean = true) {
    val recyclerview = UiScrollable(UiSelector().scrollable(true))
    if (isVertical) {
      recyclerview.setAsVerticalList()
    } else {
      recyclerview.setAsHorizontalList()
    }
    recyclerview.scrollIntoView(uiSelector)
  }
}
