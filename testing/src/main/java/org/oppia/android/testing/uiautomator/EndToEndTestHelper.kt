package org.oppia.android.testing.uiautomator

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import org.junit.Assert.assertTrue

/** This object contains common operations used for end-to-end tests. */
object EndToEndTestHelper {

  private val OPPIA_PACKAGE = "org.oppia.android"
  private val LAUNCH_TIMEOUT = 30000L
  private val TRANSITION_TIMEOUT = 5000L

  /** Starts Oppia from the home screen. */
  fun UiDevice.startOppiaFromScratch() {
    // Start from the home screen
    pressHome()

    // Wait for launcher
    val launcherPackage = findObject(UiSelector().packageName(launcherPackageName))
    launcherPackage.waitForExists(LAUNCH_TIMEOUT)
    assertTrue(launcherPackage.exists())

    // Launch the blueprint app
    val context = ApplicationProvider.getApplicationContext<Context>()
    val intent = context.packageManager.getLaunchIntentForPackage(OPPIA_PACKAGE)
    intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK) // Clear out any previous instances
    context.startActivity(intent)

    // Wait for the app to appear
    val oppiaPackage = findObject(UiSelector().packageName(OPPIA_PACKAGE))
    oppiaPackage.waitForExists(LAUNCH_TIMEOUT)
    assertTrue(oppiaPackage.exists())
  }

  /** Waits for the view with given resourceId to appear. */
  fun UiDevice.waitForRes(resourceId: String, timeout: Long = TRANSITION_TIMEOUT) {
    wait(Until.hasObject(By.res(resourceId)), timeout)
  }

  /** Returns the UiObject for the given resourceId. */
  fun UiDevice.findObjectByRes(resourceId: String): UiObject2 {
    return findObject(By.res("$OPPIA_PACKAGE:id/$resourceId"))
  }

  /** Performs a scroll until the view with the give text is visible. */
  fun scrollRecyclerViewTextIntoView(text: String, isVertical: Boolean = true) {
    val recyclerView = UiScrollable(UiSelector().scrollable(true))
    if (isVertical) {
      recyclerView.setAsVerticalList()
    } else {
      recyclerView.setAsHorizontalList()
    }
    recyclerView.scrollTextIntoView(text)
  }
}
