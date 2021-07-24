package org.oppia.android.instrumentation

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiCollection
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until.hasObject
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

/** Tests for Explorations. */
class ExplorationPlayerTest {
  private val OPPIA_PACKAGE = "org.oppia.android"
  private val LAUNCH_TIMEOUT = 30000L
  private val TRANSITION_TIMEOUT = 5000L
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
    device.wait(hasObject(By.pkg(launcherPackage)), LAUNCH_TIMEOUT)

    // Launch the blueprint app
    val context = ApplicationProvider.getApplicationContext<Context>()
    val intent = context.packageManager
      .getLaunchIntentForPackage(OPPIA_PACKAGE)
    intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK) // Clear out any previous instances
    context.startActivity(intent)

    // Wait for the app to appear
    device.wait(hasObject(By.pkg(OPPIA_PACKAGE).depth(0)), LAUNCH_TIMEOUT)
  }

  @Test
  fun testExploration_prototypeExploration_toolbarTitle_isDisplayedSuccessfully() {
    NavigateToPrototypeExploration()
    device.wait(
      hasObject(
        By.res("$OPPIA_PACKAGE:id/exploration_toolbar_title")
      ),
      TRANSITION_TIMEOUT
    )
    assertNotNull(device.findObject(By.res("$OPPIA_PACKAGE:id/exploration_toolbar_title")))
  }

  /** Navigates and opens the Prototype Exploration using the admin profile. */
  fun NavigateToPrototypeExploration() {
    val skip_button = device.findObject(By.res("$OPPIA_PACKAGE:id/skip_text_view"))
    skip_button?.let {
      it.click()
      device.wait(
        hasObject(By.res("$OPPIA_PACKAGE:id/get_started_button")),
        TRANSITION_TIMEOUT
      )
      device.findObject(By.res("$OPPIA_PACKAGE:id/get_started_button"))
        .click()
    }
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/profile_select_text")),
      TRANSITION_TIMEOUT
    )
    val profiles = UiCollection(UiSelector().className("androidx.recyclerview.widget.RecyclerView"))
    profiles.getChildByText(UiSelector().className("android.widget.LinearLayout"), "Admin").click()
    val recyclerview = UiScrollable(UiSelector().scrollable(true))
    recyclerview.setAsVerticalList()
    recyclerview.scrollTextIntoView("First Test Topic")
    val firstTestTopicText = device.findObject(UiSelector().text("First Test Topic"))
    firstTestTopicText.click()
    device.findObject(UiSelector().text("First Test Topic"))
      .click()
    device.findObject(UiSelector().text("LESSONS"))
      .click()
    device.findObject(UiSelector().text("First Story"))
      .click()
    device.findObject(UiSelector().text("Chapter 1: Prototype Exploration")).click()
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
