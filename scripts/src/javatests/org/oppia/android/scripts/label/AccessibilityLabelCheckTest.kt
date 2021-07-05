package org.oppia.android.scripts.label

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.common.ACCESSIBILITY_LABEL_CHECK_FAILED_OUTPUT_INDICATOR
import org.oppia.android.scripts.common.ACCESSIBILITY_LABEL_CHECK_PASSED_OUTPUT_INDICATOR
import org.oppia.android.testing.assertThrows
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/** Tests for [AccessibilityLabelCheck]. */
class AccessibilityLabelCheckTest {
  private val outContent: ByteArrayOutputStream = ByteArrayOutputStream()
  private val originalOut: PrintStream = System.out

  @Rule
  @JvmField
  public var tempFolder = TemporaryFolder()

  @Before
  fun setUp() {
    tempFolder.newFolder("testfiles")
    System.setOut(PrintStream(outContent))
  }

  @After
  fun restoreStreams() {
    System.setOut(originalOut)
  }

  @Test
  fun testAccessibilityLabel_labelPresent_activitiesAreDefinedWithAccessibilityLabel() {
    val manifestContent =
      """
      <?xml version="1.0" encoding="utf-8"?>
      <manifest xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:tools="http://schemas.android.com/tools"
        package="org.oppia.android">

        <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
        <uses-permission android:name="android.permission.INTERNET" />
        <application
          android:name=".app.application.OppiaApplication"
          android:allowBackup="true"
          android:icon="@mipmap/ic_launcher"
          android:label="@string/app_name"
          android:roundIcon="@mipmap/ic_launcher_round"
          android:supportsRtl="true"
          android:theme="@style/OppiaTheme">
          <meta-data
            android:name="firebase_analytics_collection_deactivated"
            android:value="true" />
          <meta-data
            android:name="firebase_crashlytics_collection_enabled"
            android:value="false" />
          <meta-data
            android:name="automatic_app_expiration_enabled"
            android:value="false" />
          <meta-data
            android:name="expiration_date"
            android:value="2020-09-01" />

          <activity
            android:name=".app.administratorcontrols.Temp1Activity"
            android:label="@string/administrator_controls_title"
            android:theme="@style/OppiaThemeWithoutActionBar" />
          <activity
            android:name=".app.administratorcontrols.appversion.Temp2Activity"
            android:label="@string/app_version_activity_title"
            android:theme="@style/OppiaThemeWithoutActionBar" />
        </application>
      </manifest>
      """.trimIndent()
    val manifestFile = tempFolder.newFile("testfiles/AndroidManifest.xml")
    manifestFile.writeText(manifestContent)

    runScript("AndroidManifest.xml")

    assertThat(outContent.toString().trim()).isEqualTo(
      ACCESSIBILITY_LABEL_CHECK_PASSED_OUTPUT_INDICATOR
    )
  }

  @Test
  fun testAccessibilityLabel_labelNotPresent_activityIsNotDefinedWithAccessibilityLabel() {
    val manifestContent =
      """
      <?xml version="1.0" encoding="utf-8"?>
      <manifest xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:tools="http://schemas.android.com/tools"
        package="org.oppia.android">

        <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
        <uses-permission android:name="android.permission.INTERNET" />
        <application
          android:name=".app.application.OppiaApplication"
          android:allowBackup="true"
          android:icon="@mipmap/ic_launcher"
          android:label="@string/app_name"
          android:roundIcon="@mipmap/ic_launcher_round"
          android:supportsRtl="true"
          android:theme="@style/OppiaTheme">
          <meta-data
            android:name="firebase_analytics_collection_deactivated"
            android:value="true" />
          <meta-data
            android:name="firebase_crashlytics_collection_enabled"
            android:value="false" />
          <meta-data
            android:name="automatic_app_expiration_enabled"
            android:value="false" />
          <meta-data
            android:name="expiration_date"
            android:value="2020-09-01" />

          <activity
            android:name=".app.administratorcontrols.Temp1Activity"
            android:label="@string/administrator_controls_title"
            android:theme="@style/OppiaThemeWithoutActionBar" />
          <activity
            android:name=".app.administratorcontrols.appversion.Temp2Activity"
            android:theme="@style/OppiaThemeWithoutActionBar" />
        </application>
      </manifest>
      """.trimIndent()
    val manifestFile = tempFolder.newFile("testfiles/AndroidManifest.xml")
    manifestFile.writeText(manifestContent)

    val exception = assertThrows(Exception::class) {
      runScript("AndroidManifest.xml")
    }

    assertThat(exception).hasMessageThat().contains(
      ACCESSIBILITY_LABEL_CHECK_FAILED_OUTPUT_INDICATOR
    )
    val failureMessage =
      """
      Accessiblity labels missing for Activities:
      .app.administratorcontrols.appversion.Temp2Activity
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testAccessibilityLabel_labelsNotPresent_activitiesAreNotDefinedWithAccessibilityLabel() {
    val manifestContent =
      """
      <?xml version="1.0" encoding="utf-8"?>
      <manifest xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:tools="http://schemas.android.com/tools"
        package="org.oppia.android">

        <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
        <uses-permission android:name="android.permission.INTERNET" />
        <application
          android:name=".app.application.OppiaApplication"
          android:allowBackup="true"
          android:icon="@mipmap/ic_launcher"
          android:label="@string/app_name"
          android:roundIcon="@mipmap/ic_launcher_round"
          android:supportsRtl="true"
          android:theme="@style/OppiaTheme">
          <meta-data
            android:name="firebase_analytics_collection_deactivated"
            android:value="true" />
          <meta-data
            android:name="firebase_crashlytics_collection_enabled"
            android:value="false" />
          <meta-data
            android:name="automatic_app_expiration_enabled"
            android:value="false" />
          <meta-data
            android:name="expiration_date"
            android:value="2020-09-01" />

          <activity
            android:name=".app.administratorcontrols.Temp1Activity"
            android:theme="@style/OppiaThemeWithoutActionBar" />
          <activity
            android:name=".app.administratorcontrols.appversion.Temp2Activity"
            android:theme="@style/OppiaThemeWithoutActionBar" />
        </application>
      </manifest>
      """.trimIndent()
    val manifestFile = tempFolder.newFile("testfiles/AndroidManifest.xml")
    manifestFile.writeText(manifestContent)

    val exception = assertThrows(Exception::class) {
      runScript("AndroidManifest.xml")
    }

    assertThat(exception).hasMessageThat().contains(
      ACCESSIBILITY_LABEL_CHECK_FAILED_OUTPUT_INDICATOR
    )
    val failureMessage =
      """
      Accessiblity labels missing for Activities:
      .app.administratorcontrols.Temp1Activity
      .app.administratorcontrols.appversion.Temp2Activity
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  /** Runs the accessibility_label_check. */
  private fun runScript(manifestFile: String) {
    main(tempFolder.getRoot().toString() + "/testfiles", manifestFile)
  }
}
