package org.oppia.android.scripts.label

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.proto.AccessibilityLabelExemptions
import org.oppia.android.testing.assertThrows
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream

/** Tests for [AccessibilityLabelCheck]. */
class AccessibilityLabelCheckTest {
  private val outContent: ByteArrayOutputStream = ByteArrayOutputStream()
  private val originalOut: PrintStream = System.out
  private val ACCESSIBILITY_LABEL_CHECK_PASSED_OUTPUT_INDICATOR = "ACCESSIBILITY LABEL CHECK PASSED"
  private val ACCESSIBILITY_LABEL_CHECK_FAILED_OUTPUT_INDICATOR = "ACCESSIBILITY LABEL CHECK FAILED"
  private val pathToProtoBinary = "scripts/assets/accessibility_label_exemptions.pb"
  private val failureNotePartOne = "If this is correct, please update " +
    "scripts/assets/accessibility_label_exemptions.textproto"
  private val failureNotePartTwo = "Note that, in general, all Activities should have labels. " +
    "If you choose to add an exemption, please specifically call this out in your PR description."

  @Rule
  @JvmField
  var tempFolder = TemporaryFolder()

  @Before
  fun setUp() {
    tempFolder.newFolder("testfiles")
    tempFolder.newFolder("scripts", "assets")
    tempFolder.newFile(pathToProtoBinary)
    System.setOut(PrintStream(outContent))
  }

  @After
  fun restoreStreams() {
    System.setOut(originalOut)
  }

  @Test
  fun testAccessibilityLabel_labelPresent_checkShouldPass() {
    val testContent =
      """
      <manifest xmlns:android="http://schemas.android.com/apk/res/android"
          package="org.oppia.android.splash">
          <activity
              android:name=".FirstSplashActivity"
              android:label="@string/administrator_controls_title1" />
          <activity
              android:name=".SecondSplashActivity"
              android:label="@string/administrator_controls_title2" />
      </manifest>
      """.trimIndent()
    tempFolder.newFolder(
      "testfiles", "app", "src", "main", "java", "org", "oppia", "android", "splash"
    )
    val tempFileRelativePath = "app/src/main/java/org/oppia/android/splash/AndroidManifest.xml"
    val manifestFile = tempFolder.newFile("testfiles/$tempFileRelativePath")
    manifestFile.writeText(testContent)

    main(
      retrieveTestFilesDirectoryPath(),
      "${tempFolder.root}/$pathToProtoBinary",
      tempFileRelativePath
    )

    assertThat(outContent.toString().trim()).isEqualTo(
      ACCESSIBILITY_LABEL_CHECK_PASSED_OUTPUT_INDICATOR
    )
  }

  @Test
  fun testAccessibilityLabel_activityNameIsRelative_labelNotPresent_checkShouldFail() {
    val testContent =
      """
      <manifest xmlns:android="http://schemas.android.com/apk/res/android"
          package="org.oppia.android.splash">
          <activity
              android:name=".FirstSplashActivity"
              android:label="@string/administrator_controls_title1" />
          <activity
              android:name=".SecondSplashActivity" />
      </manifest>
      """.trimIndent()
    tempFolder.newFolder(
      "testfiles", "app", "src", "main", "java", "org", "oppia", "android", "splash"
    )
    val tempFileRelativePath = "app/src/main/java/org/oppia/android/splash/AndroidManifest.xml"
    val manifestFile = tempFolder.newFile("testfiles/$tempFileRelativePath")
    manifestFile.writeText(testContent)

    val exception = assertThrows(Exception::class) {
      main(
        retrieveTestFilesDirectoryPath(),
        "${tempFolder.root}/$pathToProtoBinary",
        tempFileRelativePath
      )
    }

    assertThat(exception).hasMessageThat().contains(
      ACCESSIBILITY_LABEL_CHECK_FAILED_OUTPUT_INDICATOR
    )
    val activityRelativePath = "app/src/main/java/org/oppia/android/splash/SecondSplashActivity"
    val failureMessage =
      """
      Accessibility label missing for Activities:
      - $activityRelativePath
      
      $failureNotePartOne
      $failureNotePartTwo
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testAccessibilityLabel_activityNameIsAbsolute_labelNotPresent_checkShouldFail() {
    val testContent =
      """
      <manifest xmlns:android="http://schemas.android.com/apk/res/android"
          package="org.oppia.android.splash">
          <activity
              android:name=".FirstSplashActivity"
              android:label="@string/administrator_controls_title1" />
          <activity
              android:name="org.oppia.android.splash.SecondSplashActivity" />
      </manifest>
      """.trimIndent()
    tempFolder.newFolder(
      "testfiles", "app", "src", "main", "java", "org", "oppia", "android", "splash"
    )
    val tempFileRelativePath = "app/src/main/java/org/oppia/android/splash/AndroidManifest.xml"
    val manifestFile = tempFolder.newFile("testfiles/$tempFileRelativePath")
    manifestFile.writeText(testContent)

    val exception = assertThrows(Exception::class) {
      main(
        retrieveTestFilesDirectoryPath(),
        "${tempFolder.root}/$pathToProtoBinary",
        tempFileRelativePath
      )
    }

    assertThat(exception).hasMessageThat().contains(
      ACCESSIBILITY_LABEL_CHECK_FAILED_OUTPUT_INDICATOR
    )
    val activityRelativePath = "app/src/main/java/org/oppia/android/splash/SecondSplashActivity"
    val failureMessage =
      """
      Accessibility label missing for Activities:
      - $activityRelativePath
      
      $failureNotePartOne
      $failureNotePartTwo
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testAccessibilityLabel_passMultipleManifests_allLabelsAreDefined_checkShouldPass() {
    val testContent1 =
      """
      <manifest xmlns:android="http://schemas.android.com/apk/res/android"
          package="org.oppia.android">
          <activity
              android:name=".app.TempActivity"
              android:label="@string/administrator_controls_title1" />
          <activity
              android:name=".app.SecondTempActivity"
              android:label="@string/administrator_controls_title2" />
      </manifest>
      """.trimIndent()
    val testContent2 =
      """
      <manifest xmlns:android="http://schemas.android.com/apk/res/android"
          package="org.oppia.android.splash">
          <activity
              android:name=".FirstSplashActivity"
              android:label="@string/administrator_controls_title1" />
          <activity
              android:name=".SecondSplashActivity"
              android:label="@string/administrator_controls_title2" />
      </manifest>
      """.trimIndent()
    tempFolder.newFolder(
      "testfiles", "app", "src", "main", "java", "org", "oppia", "android", "app"
    )
    tempFolder.newFolder(
      "testfiles", "app", "src", "main", "java", "org", "oppia", "android", "splash"
    )
    val appManifestPath = "app/src/main/AndroidManifest.xml"
    val splashManifestPath = "app/src/main/java/org/oppia/android/splash/AndroidManifest.xml"
    val appManifestFile = tempFolder.newFile("testfiles/$appManifestPath")
    val splashManifestFile = tempFolder.newFile("testfiles/$splashManifestPath")
    appManifestFile.writeText(testContent1)
    splashManifestFile.writeText(testContent2)

    main(
      retrieveTestFilesDirectoryPath(),
      "${tempFolder.root}/$pathToProtoBinary",
      appManifestPath,
      splashManifestPath
    )

    assertThat(outContent.toString().trim()).isEqualTo(
      ACCESSIBILITY_LABEL_CHECK_PASSED_OUTPUT_INDICATOR
    )
  }

  @Test
  fun testAccessibilityLabel_passMultipleManifests_labelsNotDefined_allFailuresShouldBeLogged() {
    val testContent1 =
      """
      <manifest xmlns:android="http://schemas.android.com/apk/res/android"
          package="org.oppia.android">
          <activity
              android:name=".app.TempActivity" />
          <activity
              android:name=".app.SecondTempActivity"
              android:label="@string/administrator_controls_title2" />
      </manifest>
      """.trimIndent()
    val testContent2 =
      """
      <manifest xmlns:android="http://schemas.android.com/apk/res/android"
          package="org.oppia.android.splash">
          <activity
              android:name=".FirstSplashActivity"
              android:label="@string/administrator_controls_title1" />
          <activity
              android:name=".SecondSplashActivity" />
      </manifest>
      """.trimIndent()
    tempFolder.newFolder(
      "testfiles", "app", "src", "main", "java", "org", "oppia", "android", "app"
    )
    tempFolder.newFolder(
      "testfiles", "app", "src", "main", "java", "org", "oppia", "android", "splash"
    )
    val appManifestPath = "app/src/main/AndroidManifest.xml"
    val splashManifestPath = "app/src/main/java/org/oppia/android/splash/AndroidManifest.xml"
    val appManifestFile = tempFolder.newFile("testfiles/$appManifestPath")
    val splashManifestFile = tempFolder.newFile("testfiles/$splashManifestPath")
    appManifestFile.writeText(testContent1)
    splashManifestFile.writeText(testContent2)

    val exception = assertThrows(Exception::class) {
      main(
        retrieveTestFilesDirectoryPath(),
        "${tempFolder.root}/$pathToProtoBinary",
        appManifestPath,
        splashManifestPath
      )
    }

    assertThat(exception).hasMessageThat().contains(
      ACCESSIBILITY_LABEL_CHECK_FAILED_OUTPUT_INDICATOR
    )
    val appActivityPath = "app/src/main/java/org/oppia/android/app/TempActivity"
    val splashActivityPath = "app/src/main/java/org/oppia/android/splash/SecondSplashActivity"
    val failureMessage =
      """
      Accessibility label missing for Activities:
      - $appActivityPath
      - $splashActivityPath
      
      $failureNotePartOne
      $failureNotePartTwo
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testAccessibilityLabel_multipleFailures_logsShouldBeLexicographicallySorted() {
    val testContent1 =
      """
      <manifest xmlns:android="http://schemas.android.com/apk/res/android"
          package="org.oppia.android">
          <activity
              android:name=".app.FourthTempActivity" />
          <activity
              android:name=".app.ThirdTempActivity" />
          <activity
              android:name=".app.FirstTempActivity" />
          <activity
              android:name=".app.SecondTempActivity"
              android:label="@string/administrator_controls_title2" />
      </manifest>
      """.trimIndent()
    val testContent2 =
      """
      <manifest xmlns:android="http://schemas.android.com/apk/res/android"
          package="org.oppia.android.splash">
          <activity
              android:name=".FirstSplashActivity"
              android:label="@string/administrator_controls_title1" />
          <activity
              android:name=".SecondSplashActivity" />
      </manifest>
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main")
    tempFolder.newFolder(
      "testfiles", "app", "src", "main", "java", "org", "oppia", "android", "splash"
    )
    val appManifestPath = "app/src/main/AndroidManifest.xml"
    val splashManifestPath = "app/src/main/java/org/oppia/android/splash/AndroidManifest.xml"
    val appManifestFile = tempFolder.newFile("testfiles/$appManifestPath")
    val splashManifestFile = tempFolder.newFile("testfiles/$splashManifestPath")
    appManifestFile.writeText(testContent1)
    splashManifestFile.writeText(testContent2)

    val exception = assertThrows(Exception::class) {
      main(
        retrieveTestFilesDirectoryPath(),
        "${tempFolder.root}/$pathToProtoBinary",
        appManifestPath,
        splashManifestPath
      )
    }

    assertThat(exception).hasMessageThat().contains(
      ACCESSIBILITY_LABEL_CHECK_FAILED_OUTPUT_INDICATOR
    )
    val firstAppActivityPath = "app/src/main/java/org/oppia/android/app/FirstTempActivity"
    val thirdAppActivityPath = "app/src/main/java/org/oppia/android/app/ThirdTempActivity"
    val fourthAppActivityPath = "app/src/main/java/org/oppia/android/app/FourthTempActivity"
    val splashActivityPath = "app/src/main/java/org/oppia/android/splash/SecondSplashActivity"
    val failureMessage =
      """
      Accessibility label missing for Activities:
      - $firstAppActivityPath
      - $fourthAppActivityPath
      - $thirdAppActivityPath
      - $splashActivityPath
      
      $failureNotePartOne
      $failureNotePartTwo
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testAccessibilityLabel_accessibilityLabelNotDefinedForExemptedActivity_checkShouldPass() {
    val testContent =
      """
      <manifest xmlns:android="http://schemas.android.com/apk/res/android"
          package="org.oppia.android">
          <activity
              android:name=".app.home.HomeActivity" />
      </manifest>
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main")
    val tempFileRelativePath = "app/src/main/AndroidManifest.xml"
    val manifestFile = tempFolder.newFile("testfiles/$tempFileRelativePath")
    manifestFile.writeText(testContent)
    val exemptionFile = File("${tempFolder.root}/$pathToProtoBinary")
    val builder = AccessibilityLabelExemptions.newBuilder()
    builder.addExemptedActivity("app/src/main/java/org/oppia/android/app/home/HomeActivity")
    val exemptions = builder.build()
    exemptions.writeTo(exemptionFile.outputStream())

    main(
      retrieveTestFilesDirectoryPath(),
      "${tempFolder.root}/$pathToProtoBinary",
      tempFileRelativePath
    )

    assertThat(outContent.toString().trim()).isEqualTo(
      ACCESSIBILITY_LABEL_CHECK_PASSED_OUTPUT_INDICATOR
    )
  }

  @Test
  fun testAccessibilityLabel_addRedundantExemption_checkShouldFail() {
    val testContent =
      """
      <manifest xmlns:android="http://schemas.android.com/apk/res/android"
          package="org.oppia.android">
          <activity
              android:name=".app.home.HomeActivity"
              android:label="@string/administrator_controls_title1" />
      </manifest>
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main")
    val tempFileRelativePath = "app/src/main/AndroidManifest.xml"
    val manifestFile = tempFolder.newFile("testfiles/$tempFileRelativePath")
    manifestFile.writeText(testContent)
    val exemptionFile = File("${tempFolder.root}/$pathToProtoBinary")
    val builder = AccessibilityLabelExemptions.newBuilder()
    builder.addExemptedActivity("app/src/main/java/org/oppia/android/app/home/HomeActivity")
    val exemptions = builder.build()
    exemptions.writeTo(exemptionFile.outputStream())

    val exception = assertThrows(Exception::class) {
      main(
        retrieveTestFilesDirectoryPath(),
        "${tempFolder.root}/$pathToProtoBinary",
        tempFileRelativePath
      )
    }

    assertThat(exception).hasMessageThat().contains(
      ACCESSIBILITY_LABEL_CHECK_FAILED_OUTPUT_INDICATOR
    )
    val failureMessage =
      """
      Redundant exemptions:
      - app/src/main/java/org/oppia/android/app/home/HomeActivity
      Please remove them from scripts/assets/accessibility_label_exemptions.textproto
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testAccessibilityLabel_addRedundantExemption_activityMissingLabel_allFailuresShouldLog() {
    val testContent =
      """
      <manifest xmlns:android="http://schemas.android.com/apk/res/android"
          package="org.oppia.android">
          <activity
              android:name=".app.home.SplashActivity" />
      </manifest>
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main")
    val tempFileRelativePath = "app/src/main/AndroidManifest.xml"
    val manifestFile = tempFolder.newFile("testfiles/$tempFileRelativePath")
    manifestFile.writeText(testContent)
    val activityPath = "app/src/main/java/org/oppia/android/app/home/SplashActivity"
    val exemptionFile = File("${tempFolder.root}/$pathToProtoBinary")
    val builder = AccessibilityLabelExemptions.newBuilder()
    builder.addExemptedActivity("app/src/main/java/org/oppia/android/app/home/HomeActivity")
    val exemptions = builder.build()
    exemptions.writeTo(exemptionFile.outputStream())

    val exception = assertThrows(Exception::class) {
      main(
        retrieveTestFilesDirectoryPath(),
        "${tempFolder.root}/$pathToProtoBinary",
        tempFileRelativePath
      )
    }

    assertThat(exception).hasMessageThat().contains(
      ACCESSIBILITY_LABEL_CHECK_FAILED_OUTPUT_INDICATOR
    )
    val failureMessage =
      """
      Redundant exemptions:
      - app/src/main/java/org/oppia/android/app/home/HomeActivity
      Please remove them from scripts/assets/accessibility_label_exemptions.textproto
      
      Accessibility label missing for Activities:
      - $activityPath
      
      $failureNotePartOne
      $failureNotePartTwo
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  /** Retrieves the absolute path of testfiles directory. */
  private fun retrieveTestFilesDirectoryPath(): String {
    return "${tempFolder.root}/testfiles"
  }
}
