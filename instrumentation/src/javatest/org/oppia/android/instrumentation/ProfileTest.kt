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
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4


@RunWith(AndroidJUnit4::class)
class ProfileTest {

  private val BASIC_SAMPLE_PACKAGE = "com.example.android.testing.uiautomator.BasicSample"

  private val OPPIA_PACKAGE = "org.oppia.android"

  private val LAUNCH_TIMEOUT = 5000

  private val STRING_TO_BE_TYPED = "UiAutomator"

  private lateinit var mDevice: UiDevice

  @Before
  fun startMainActivityFromHomeScreen() {
    // Initialize UiDevice instance
    mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    // Start from the home screen
    mDevice.pressHome()

    // Wait for launcher
    val launcherPackage = getLauncherPackageName()
    assertNotNull(launcherPackage)
    mDevice.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), LAUNCH_TIMEOUT.toLong())

    // Launch the blueprint app
    val context = ApplicationProvider.getApplicationContext<Context>()
    val intent = context.packageManager
      .getLaunchIntentForPackage(OPPIA_PACKAGE)
    intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK) // Clear out any previous instances
    context.startActivity(intent)

    // Wait for the app to appear
    mDevice.wait(Until.hasObject(By.pkg(OPPIA_PACKAGE).depth(0)), LAUNCH_TIMEOUT.toLong())
  }

  @Test
  fun checkPreconditions() {
    assertNotNull(mDevice)
  }
  
  @Test
  fun exitOnBoarding() {
    mDevice.findObject(By.res("org.oppia.android:id/skip_text_view"))
          .click()
    mDevice.findObject(By.res("org.oppia.android:id/get_started_button"))
          .click()
  }
  
  @Test
  fun openOppia() {
    // Start from the home screen
    mDevice.pressHome()
    // Wait for launcher
    val launcherPackage = getLauncherPackageName()
    assertNotNull(launcherPackage)
    mDevice.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), LAUNCH_TIMEOUT.toLong())

    // Launch the blueprint app
    val context = InstrumentationRegistry.getInstrumentation().context
    val intent = context.packageManager
      .getLaunchIntentForPackage(OPPIA_PACKAGE)
    intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK) // Clear out any previous instances
    context.startActivity(intent)

    // Wait for the app to appear
    mDevice.wait(Until.hasObject(By.pkg(BASIC_SAMPLE_PACKAGE).depth(0)), LAUNCH_TIMEOUT.toLong())
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
