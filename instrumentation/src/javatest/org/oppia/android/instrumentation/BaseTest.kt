package org.oppia.android.instrumentation

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

/** Tests to load Oppia using UI Automator. */
class BaseTest {
  private val OPPIA_PACKAGE = "org.oppia.android"
  private val LAUNCH_TIMEOUT = 5000
  private lateinit var device: UiDevice

  @Before
  fun startOppiaFromHomeScreen() {
    // Initialize UiDevice instance
    device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    // Start from the home screen
    device.pressHome()

    // Wait for launcher
    val launcherPackage = getLauncherPackageName()
    assertNotNull(launcherPackage)
    device.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), LAUNCH_TIMEOUT.toLong())

    // Launch the blueprint app
    val context = ApplicationProvider.getApplicationContext<Context>()
    val intent = context.packageManager
      .getLaunchIntentForPackage(OPPIA_PACKAGE)
    intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK) // Clear out any previous instances
    context.startActivity(intent)

    // Wait for the app to appear
    device.wait(Until.hasObject(By.pkg(OPPIA_PACKAGE).depth(0)), LAUNCH_TIMEOUT.toLong())
  }

  @Test
  fun baseTest_uiDevice_isNotNull() {
    assertNotNull(device)
  }

  @Test
  fun baseTest_openProfileDashboard_titleExists() {
    val skip_button = device.findObject(By.res("$OPPIA_PACKAGE:id/skip_text_view"))
    skip_button?.let {
      it.click()
      device.wait(Until.hasObject(By.res("$OPPIA_PACKAGE:id/get_started_button")), 1000L)
      device.findObject(By.res("org.oppia.android:id/get_started_button"))
        .click()
    }
    device.wait(Until.hasObject(By.res("$OPPIA_PACKAGE:id/profile_select_text")), 1000L)
    assertNotNull(device.findObject(By.res("$OPPIA_PACKAGE:id/profile_select_text")))
  }

  /**
   * Uses package manager to find the package name of the device launcher. Usually this package
   * is "com.android.launcher" but can be different at times. This is a generic solution which
   * works on all platforms.`
   */
  private fun getLauncherPackageName(): String {
    // Create launcher Intent
    val intent = Intent(Intent.ACTION_MAIN)
    intent.addCategory(Intent.CATEGORY_HOME)

    // Use PackageManager to get the launcher package name
    val pm = ApplicationProvider.getApplicationContext<Context>().packageManager
    val resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
    return resolveInfo!!.activityInfo.packageName
  }
}
