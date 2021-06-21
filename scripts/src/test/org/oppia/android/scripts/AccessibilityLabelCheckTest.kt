package org.oppia.android.scripts

import java.io.File
import kotlin.test.assertEquals
import org.oppia.android.scripts.AccessibilityLabelCheck
import org.junit.Test
import org.junit.Rule
import org.junit.Before
import org.junit.rules.TemporaryFolder
import org.junit.rules.ExpectedException
import org.oppia.android.scripts.ScriptResultConstants

class AccessibilityLabelCheckTest {
  @Rule
  @JvmField
  public var tempFolder: TemporaryFolder = TemporaryFolder()

  @Rule
  @JvmField
  var thrown: ExpectedException = ExpectedException.none()

  @Before
  fun initTestFilesDirectory() {
    val testFilesDirectory = tempFolder.newFolder("testfiles")
  }

  @Test
  fun testAccessibilityLabel_labelPresent_ActivitiesAreDefinedWithAccessibilityLabels(){
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

    runScript("AndroidManifest.xml")
  }

  @Test
  fun testAccessibilityLabel_labelNotPresent_ActivityAreNotDefinedWithAccessibilityLabels(){
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

    expectScriptFailure()

    val manifestFile = tempFolder.newFile("testfiles/AndroidManifest.xml")
    manifestFile.writeText(manifestContent)

    runScript("AndroidManifest.xml")
  }

  fun runScript(manifestFile: String) {
    AccessibilityLabelCheck.main(tempFolder.getRoot().toString()+"/testfiles", manifestFile)
  }

  fun expectScriptFailure() {
    thrown.expect(java.lang.Exception::class.java)
    thrown.expectMessage(ScriptResultConstants.ACCESSIBILITY_LABEL_CHECK_FAILED)
  }
}

