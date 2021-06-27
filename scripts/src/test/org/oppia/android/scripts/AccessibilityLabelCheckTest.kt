package org.oppia.android.scripts

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.testing.assertThrows
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/** Tests for [AccessibilityLabelCheck]. */
class AccessibilityLabelCheckTest {
  private val outContent: ByteArrayOutputStream = ByteArrayOutputStream()
  private val originalOut: PrintStream = java.lang.System.out

  @Rule
  @JvmField
  public var tempFolder: TemporaryFolder = TemporaryFolder()

  @Before
  fun setUpTests() {
    tempFolder.newFolder("testfiles")
    java.lang.System.setOut(PrintStream(outContent))
  }

  @After
  fun restoreStreams() {
    java.lang.System.setOut(originalOut)
  }

  @Test
  fun testAccessibilityLabel_labelPresent_activitiesAreDefinedWithAccessibilityLabel() {
    val manifestContent = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
      "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
      "  xmlns:tools=\"http://schemas.android.com/tools\"\n" +
      "  package=\"org.oppia.android\">\n" +
      "\n" +
      "  <uses-permission android:name=\"android.permission.ACCESS_NETWORK_STATE\" />\n" +
      "  <uses-permission android:name=\"android.permission.INTERNET\" />\n" +
      "  <application\n" +
      "    android:name=\".app.application.OppiaApplication\"\n" +
      "    android:allowBackup=\"true\"\n" +
      "    android:icon=\"@mipmap/ic_launcher\"\n" +
      "    android:label=\"@string/app_name\"\n" +
      "    android:roundIcon=\"@mipmap/ic_launcher_round\"\n" +
      "    android:supportsRtl=\"true\"\n" +
      "    android:theme=\"@style/OppiaTheme\">\n" +
      "    <meta-data\n" +
      "      android:name=\"firebase_analytics_collection_deactivated\"\n" +
      "      android:value=\"true\" />\n" +
      "    <meta-data\n" +
      "      android:name=\"firebase_crashlytics_collection_enabled\"\n" +
      "      android:value=\"false\" />\n" +
      "    <meta-data\n" +
      "      android:name=\"automatic_app_expiration_enabled\"\n" +
      "      android:value=\"false\" />\n" +
      "    <meta-data\n" +
      "      android:name=\"expiration_date\"\n" +
      "      android:value=\"2020-09-01\" />\n" +
      "\n" +
      "    <activity\n" +
      "      android:name=\"TempActivity1\"\n" +
      "      android:label=\"@string/administrator_controls_title\"\n" +
      "      android:theme=\"@style/OppiaThemeWithoutActionBar\" />\n" +
      "    <activity\n" +
      "      android:name=\"TempActivity2\"\n" +
      "      android:label=\"@string/app_version_activity_title\"\n" +
      "      android:theme=\"@style/OppiaThemeWithoutActionBar\" />\n" +
      "  </application>\n" +
      "</manifest>\n"
    val manifestFile = tempFolder.newFile("testfiles/AndroidManifest.xml")
    manifestFile.writeText(manifestContent)

    runScript(
      manifestFile = "AndroidManifest.xml"
    )

    assertThat(outContent.toString().trim()).isEqualTo(
      ScriptResultConstants.ACCESSIBILITY_LABEL_CHECK_PASSED
    )
  }

  @Test
  fun testAccessibilityLabel_labelNotPresent_activityIsNotDefinedWithAccessibilityLabel() {
    val manifestContent = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
      "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
      "  xmlns:tools=\"http://schemas.android.com/tools\"\n" +
      "  package=\"org.oppia.android\">\n" +
      "\n" +
      "  <uses-permission android:name=\"android.permission.ACCESS_NETWORK_STATE\" />\n" +
      "  <uses-permission android:name=\"android.permission.INTERNET\" />\n" +
      "  <application\n" +
      "    android:name=\".app.application.OppiaApplication\"\n" +
      "    android:allowBackup=\"true\"\n" +
      "    android:icon=\"@mipmap/ic_launcher\"\n" +
      "    android:label=\"@string/app_name\"\n" +
      "    android:roundIcon=\"@mipmap/ic_launcher_round\"\n" +
      "    android:supportsRtl=\"true\"\n" +
      "    android:theme=\"@style/OppiaTheme\">\n" +
      "    <meta-data\n" +
      "      android:name=\"firebase_analytics_collection_deactivated\"\n" +
      "      android:value=\"true\" />\n" +
      "    <meta-data\n" +
      "      android:name=\"firebase_crashlytics_collection_enabled\"\n" +
      "      android:value=\"false\" />\n" +
      "    <meta-data\n" +
      "      android:name=\"automatic_app_expiration_enabled\"\n" +
      "      android:value=\"false\" />\n" +
      "    <meta-data\n" +
      "      android:name=\"expiration_date\"\n" +
      "      android:value=\"2020-09-01\" />\n" +
      "\n" +
      "    <activity\n" +
      "      android:name=\"TempActivity1\"\n" +
      "      android:label=\"@string/administrator_controls_title\"\n" +
      "      android:theme=\"@style/OppiaThemeWithoutActionBar\" />\n" +
      "    <activity\n" +
      "      android:name=\"TempActivity2\"\n" +
      "      android:theme=\"@style/OppiaThemeWithoutActionBar\" />\n" +
      "  </application>\n" +
      "</manifest>\n"
    val manifestFile = tempFolder.newFile("testfiles/AndroidManifest.xml")
    manifestFile.writeText(manifestContent)

    val exception = assertThrows(Exception::class) {
      runScript(
        manifestFile = "AndroidManifest.xml"
      )
    }

    assertThat(exception).hasMessageThat().contains(
      ScriptResultConstants.ACCESSIBILITY_LABEL_CHECK_FAILED
    )
    assertThat(outContent.toString().trim()).isEqualTo(
      "Accessiblity labels missing for Activities:\nTempActivity2"
    )
  }

  @Test
  fun testAccessibilityLabel_labelsNotPresent_activitiesAreNotDefinedWithAccessibilityLabel() {
    val manifestContent = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
      "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
      "  xmlns:tools=\"http://schemas.android.com/tools\"\n" +
      "  package=\"org.oppia.android\">\n" +
      "\n" +
      "  <uses-permission android:name=\"android.permission.ACCESS_NETWORK_STATE\" />\n" +
      "  <uses-permission android:name=\"android.permission.INTERNET\" />\n" +
      "  <application\n" +
      "    android:name=\".app.application.OppiaApplication\"\n" +
      "    android:allowBackup=\"true\"\n" +
      "    android:icon=\"@mipmap/ic_launcher\"\n" +
      "    android:label=\"@string/app_name\"\n" +
      "    android:roundIcon=\"@mipmap/ic_launcher_round\"\n" +
      "    android:supportsRtl=\"true\"\n" +
      "    android:theme=\"@style/OppiaTheme\">\n" +
      "    <meta-data\n" +
      "      android:name=\"firebase_analytics_collection_deactivated\"\n" +
      "      android:value=\"true\" />\n" +
      "    <meta-data\n" +
      "      android:name=\"firebase_crashlytics_collection_enabled\"\n" +
      "      android:value=\"false\" />\n" +
      "    <meta-data\n" +
      "      android:name=\"automatic_app_expiration_enabled\"\n" +
      "      android:value=\"false\" />\n" +
      "    <meta-data\n" +
      "      android:name=\"expiration_date\"\n" +
      "      android:value=\"2020-09-01\" />\n" +
      "\n" +
      "    <activity\n" +
      "      android:name=\"TempActivity1\"\n" +
      "      android:theme=\"@style/OppiaThemeWithoutActionBar\" />\n" +
      "    <activity\n" +
      "      android:name=\"TempActivity2\"\n" +
      "      android:theme=\"@style/OppiaThemeWithoutActionBar\" />\n" +
      "  </application>\n" +
      "</manifest>\n"
    val manifestFile = tempFolder.newFile("testfiles/AndroidManifest.xml")
    manifestFile.writeText(manifestContent)

    val exception = assertThrows(Exception::class) {
      runScript(
        manifestFile = "AndroidManifest.xml"
      )
    }

    assertThat(exception).hasMessageThat().contains(
      ScriptResultConstants.ACCESSIBILITY_LABEL_CHECK_FAILED
    )
    assertThat(outContent.toString().trim()).isEqualTo(
      "Accessiblity labels missing for Activities:\n" +
        "TempActivity1\n" +
        "TempActivity2"
    )
  }

  /** Helper function which executes the main method of the script. */
  private fun runScript(manifestFile: String) {
    AccessibilityLabelCheck.main(tempFolder.getRoot().toString() + "/testfiles", manifestFile)
  }
}
