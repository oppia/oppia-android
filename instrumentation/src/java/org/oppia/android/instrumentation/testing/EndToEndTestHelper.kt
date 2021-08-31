package org.oppia.android.instrumentation.testing

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import com.google.common.truth.Truth.assertThat

/** This object contains common operations used for end-to-end tests. */
object EndToEndTestHelper {

  private val OPPIA_PACKAGE = "org.oppia.android"
  private val LAUNCH_TIMEOUT_SECONDS = 30000L
  private val TRANSITION_TIMEOUT_SECONDS = 5000L

  /** Starts Oppia from the home screen. */
  fun UiDevice.startOppiaFromScratch() {
    // Start from the home screen
    pressHome()

    // Wait for launcher
    val launcherPackage = launcherPackageName
    assertThat(launcherPackage).isNotNull()
    wait(Until.hasObject(By.pkg(launcherPackage).depth(1)), LAUNCH_TIMEOUT_SECONDS)

    // Launch the blueprint app
    val context = ApplicationProvider.getApplicationContext<Context>()
    val intent = context.packageManager
      .getLaunchIntentForPackage(OPPIA_PACKAGE)
    intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK) // Clear out any previous instances
    context.startActivity(intent)

    // Wait for the app to appear
    wait(Until.hasObject(By.pkg(OPPIA_PACKAGE)), LAUNCH_TIMEOUT_SECONDS)
  }

  /** Waits for the view with given resourceId to appear. */
  fun UiDevice.waitForRes(resourceId: String, timeout: Long = TRANSITION_TIMEOUT_SECONDS) {
    wait(Until.hasObject(By.res(resourceId)), timeout)
  }

  /** Waits for the view with given text to appear. */
  fun UiDevice.waitForText(text: String) {
    wait(Until.hasObject(By.text(text)), TRANSITION_TIMEOUT_SECONDS)
  }

  /** Waits for the view with given content description to appear. */
  fun UiDevice.waitForDesc(text: String) {
    wait(Until.hasObject(By.desc(text)), TRANSITION_TIMEOUT_SECONDS)
  }

  /** Return the UiObject with the given text. */
  fun UiDevice.findObjectByText(text: String): UiObject2 {
    waitForText(text)
    return checkNotNull(findObject(By.text(text)))
  }

  /** Returns the UiObject for the given resourceId. */
  fun UiDevice.findObjectByRes(resourceId: String): UiObject2 {
    waitForRes(resourceId)
    return checkNotNull(findObject(By.res("$OPPIA_PACKAGE:id/$resourceId")))
  }

  /** Returns the UiObject for the given resourceId. */
  fun UiObject2.findObjectByRes(resourceId: String): UiObject2 {
    return checkNotNull(findObject(By.res("$OPPIA_PACKAGE:id/$resourceId")))
  }

  /** Returns the UiObject for the given content description. */
  fun UiDevice.findObjectByDesc(text: String): UiObject2 {
    waitForDesc(text)
    return checkNotNull(findObject(By.desc(text)))
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
