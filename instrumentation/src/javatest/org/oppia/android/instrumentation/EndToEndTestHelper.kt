package org.oppia.android.instrumentation

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert

object EndToEndTestHelper {

  private val OPPIA_PACKAGE = "org.oppia.android"
  private val LAUNCH_TIMEOUT = 30000L

  /** Starts oppia from HomeScreen. */
  fun UiDevice.startOppiaFromScratch() {
    // Start from the home screen
    this.pressHome()

    // Wait for launcher
    val launcherPackage = launcherPackageName
    Assert.assertNotNull(launcherPackage)
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
}
