package org.oppia.android.scripts.lint

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.testing.TestBazelWorkspace
import org.oppia.android.testing.assertThrows
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream

/** Tests for [AndroidLintRunner]. */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class AndroidLintRunnerTest {
  @field:[Rule JvmField]
  var tempFolder = TemporaryFolder()

  private lateinit var mockRepoRoot: File
  private lateinit var outputStream: ByteArrayOutputStream
  private lateinit var originalOut: PrintStream
  private lateinit var testBazelWorkspace: TestBazelWorkspace

  @Before
  fun setUp() {
    mockRepoRoot = tempFolder.root
    outputStream = ByteArrayOutputStream()
    originalOut = System.out
    System.setOut(PrintStream(outputStream))
    testBazelWorkspace = TestBazelWorkspace(tempFolder)
  }

  @After
  fun tearDown() {
    System.setOut(originalOut)
  }

  @Test
  fun testMain_noArguments_throwsException() {
    val exception = assertThrows<IllegalArgumentException> {
      main()
    }

    assertThat(exception).hasMessageThat().contains(
      "<path_to_repository_root argument> is required: \$(pwd)"
    )
  }

  @Test
  fun testMain_nonExistentPath_throwsException() {
    val nonExistentPath = File(tempFolder.root, "nonexistent").absolutePath

    val exception = assertThrows<IllegalArgumentException> {
      main(nonExistentPath)
    }

    assertThat(exception).hasMessageThat().contains("Repository root path does not exist")
  }

  @Test
  fun testPrepareLintArguments_includesRequiredFlags() {
    val reportPath = File(tempFolder.root, "report.xml").absolutePath
    val projectPath = File(tempFolder.root, "project.xml").absolutePath
    val lintRunner = AndroidLintRunner(reportPath, projectPath)

    val result = lintRunner.prepareLintArguments()

    assertThat(result).asList().containsAtLeast(
      "-Wall",
      "--quiet",
      "--fullpath",
      "--showall",
      "--exitcode",
      "--offline",
      "--project", projectPath,
      "--xml", reportPath
    )
  }

  @Test
  fun testRunLint_missingApplicationIcon_detectsIssue() {
    setupAndroidProjectWithoutApplicationIcon()
    val lintRunner = createLintRunner()

    lintRunner.runLint(lintRunner.prepareLintArguments())

    verifyLintReportContains("MissingApplicationIcon")
  }

  @Test
  fun testRunLint_unusedResources_detectsIssue() {
    setupAndroidProjectWithUnusedResources()
    val lintRunner = createLintRunner()

    lintRunner.runLint(lintRunner.prepareLintArguments())

    verifyLintReportContains("UnusedResources")
  }

  @Test
  fun testRunLint_duplicateStrings_detectsIssue() {
    setupAndroidProjectWithDuplicateStrings()
    val lintRunner = createLintRunner()

    lintRunner.runLint(lintRunner.prepareLintArguments())

    verifyLintReportContains("DuplicateStrings")
  }

  @Test
  fun testRunLint_unusedIds_detectsIssue() {
    setupAndroidProjectWithUnusedIds()
    val lintRunner = createLintRunner()

    lintRunner.runLint(lintRunner.prepareLintArguments())

    verifyLintReportContains("UnusedIds")
  }

  @Test
  fun testRunLint_rtlHardcoded_detectsIssue() {
    setupAndroidProjectWithRtlHardcoded()
    val lintRunner = createLintRunner()

    lintRunner.runLint(lintRunner.prepareLintArguments())

    verifyLintReportContains("RtlHardcoded")
  }

  @Test
  fun testRunLint_uselessParent_detectsIssue() {
    setupAndroidProjectWithUselessParent()
    val lintRunner = createLintRunner()

    lintRunner.runLint(lintRunner.prepareLintArguments())

    verifyLintReportContains("UselessParent")
  }

  @Test
  fun testRunLint_hardcodedText_detectsIssue() {
    setupAndroidProjectWithHardcodedText()
    val lintRunner = createLintRunner()

    lintRunner.runLint(lintRunner.prepareLintArguments())

    verifyLintReportContains("HardcodedText")
  }

  @Test
  fun testRunLint_invalidId_detectsIssue() {
    setupAndroidProjectWithInvalidId()
    val lintRunner = createLintRunner()

    val exception = assertThrows<IllegalStateException> {
      lintRunner.runLint(lintRunner.prepareLintArguments())
    }

    assertThat(exception.message).isEqualTo("${RED}ANDROID LINT CHECK ${BOLD}FAILED$RESET")
    verifyLintReportContains("InvalidId")
  }

  private fun createLintRunner(): AndroidLintRunner {
    val reportFile = File(tempFolder.root, "lint-report.xml")
    val projectDescriptionFile = createProjectDescriptionFile()

    return AndroidLintRunner(
      reportPath = reportFile.absolutePath,
      projectDescriptionPath = projectDescriptionFile.absolutePath
    )
  }

  private fun verifyLintReportContains(issueType: String) {
    val reportFile = File(tempFolder.root, "lint-report.xml")
    assertThat(reportFile.exists()).isTrue()

    val reportContent = reportFile.readText()
    assertThat(reportContent).contains(issueType)

    val outputContent = outputStream.toString()
    assertThat(outputContent).contains(issueType)
  }

  private fun setupAndroidProjectWithoutApplicationIcon() {
    testBazelWorkspace.initEmptyWorkspace()
    createProjectStructure()

    val manifestFile = File(tempFolder.root, "app/src/main/AndroidManifest.xml")
    manifestFile.writeText(createManifestContent(includeIcon = false))

    createBasicStringResources()
  }

  private fun setupAndroidProjectWithUnusedResources() {
    testBazelWorkspace.initEmptyWorkspace()
    createProjectStructure()
    createBasicManifest()

    val stringsFile = File(tempFolder.root, "app/src/main/res/values/strings.xml")
    stringsFile.writeText(
      """
      <?xml version="1.0" encoding="utf-8"?>
      <resources>
          <string name="app_name">Oppia</string>
          <string name="unused_string">This string is never used</string>
          <string name="another_unused">Another unused string</string>
      </resources>
      """.trimIndent()
    )
  }

  private fun setupAndroidProjectWithDuplicateStrings() {
    testBazelWorkspace.initEmptyWorkspace()
    createProjectStructure()
    createBasicManifest()

    val stringsFile = File(tempFolder.root, "app/src/main/res/values/strings.xml")
    stringsFile.writeText(
      """
      <?xml version="1.0" encoding="utf-8"?>
      <resources>
          <string name="app_name">Oppia</string>
          <string name="duplicate_value">Same text</string>
          <string name="another_duplicate">Same text</string>
      </resources>
      """.trimIndent()
    )
  }

  private fun setupAndroidProjectWithUnusedIds() {
    testBazelWorkspace.initEmptyWorkspace()
    createProjectStructure()
    createBasicManifest()

    createLayoutWithUnusedIds()
    createBasicStringResources()
  }

  private fun setupAndroidProjectWithRtlHardcoded() {
    testBazelWorkspace.initEmptyWorkspace()
    createProjectStructure()
    createBasicManifest()

    createLayoutWithRtlHardcoded()
    createBasicStringResources()
  }

  private fun setupAndroidProjectWithUselessParent() {
    testBazelWorkspace.initEmptyWorkspace()
    createProjectStructure()
    createBasicManifest()

    createLayoutWithUselessParent()
    createBasicStringResources()
  }

  private fun setupAndroidProjectWithHardcodedText() {
    testBazelWorkspace.initEmptyWorkspace()
    createProjectStructure()
    createBasicManifest()

    createLayoutWithHardcodedText()
    createBasicStringResources()
  }

  private fun setupAndroidProjectWithInvalidId() {
    testBazelWorkspace.initEmptyWorkspace()
    createProjectStructure()
    createBasicManifest()

    createLayoutWithInvalidId()
    createBasicStringResources()
  }

  private fun createProjectStructure() {
    val appMainDir = File(tempFolder.root, "app/src/main")
    appMainDir.mkdirs()

    val srcDir = File(appMainDir, "java/org/oppia/android/app/activity")
    srcDir.mkdirs()

    val resValuesDir = File(appMainDir, "res/values")
    resValuesDir.mkdirs()

    val resLayoutDir = File(appMainDir, "res/layout")
    resLayoutDir.mkdirs()

    val resDrawableDir = File(appMainDir, "res/drawable")
    resDrawableDir.mkdirs()
  }

  private fun createBasicManifest() {
    val manifestFile = File(tempFolder.root, "app/src/main/AndroidManifest.xml")
    manifestFile.writeText(createManifestContent(includeIcon = true))
  }

  private fun createManifestContent(includeIcon: Boolean): String {
    val iconAttribute = if (includeIcon) """android:icon="@drawable/ic_launcher"""" else ""

    return """
      <?xml version="1.0" encoding="utf-8"?>
      <manifest xmlns:android="http://schemas.android.com/apk/res/android"
          package="org.oppia.android.app"
          android:versionCode="1"
          android:versionName="1.0">
          
          <uses-sdk android:minSdkVersion="21" />
          
          <application
              $iconAttribute
              android:label="@string/app_name"
              android:theme="@style/OppiaTheme">
              <activity 
                  android:exported="true">
                  <intent-filter>
                      <action android:name="android.intent.action.MAIN" />
                      <category android:name="android.intent.category.LAUNCHER" />
                  </intent-filter>
              </activity>
          </application>
      </manifest>
    """.trimIndent()
  }

  private fun createBasicStringResources() {
    val stringsFile = File(tempFolder.root, "app/src/main/res/values/strings.xml")
    stringsFile.writeText(
      """
      <?xml version="1.0" encoding="utf-8"?>
      <resources>
          <string name="app_name">Oppia</string>
      </resources>
      """.trimIndent()
    )
  }

  private fun createLayoutWithUnusedIds() {
    val layoutFile = File(tempFolder.root, "app/src/main/res/layout/activity_main.xml")
    layoutFile.writeText(
      """
      <?xml version="1.0" encoding="utf-8"?>
      <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
          android:layout_width="match_parent"
          android:layout_height="match_parent"
          android:orientation="vertical">
          <TextView
              android:id="@+id/unused_text_view"
              android:layout_width="wrap_content"
              android:layout_height="wrap_content"
              android:text="@string/app_name" />
          <Button
              android:id="@+id/another_unused_id"
              android:layout_width="wrap_content"
              android:layout_height="wrap_content"
              android:text="Click me" />
      </LinearLayout>
      """.trimIndent()
    )
  }

  private fun createLayoutWithRtlHardcoded() {
    val layoutFile = File(tempFolder.root, "app/src/main/res/layout/activity_main.xml")
    layoutFile.writeText(
      """
      <?xml version="1.0" encoding="utf-8"?>
      <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
          android:layout_width="match_parent"
          android:layout_height="match_parent"
          android:orientation="vertical">
          <TextView
              android:layout_width="wrap_content"
              android:layout_height="wrap_content"
              android:layout_marginLeft="16dp"
              android:text="@string/app_name" />
      </LinearLayout>
      """.trimIndent()
    )
  }

  private fun createLayoutWithUselessParent() {
    val layoutFile = File(tempFolder.root, "app/src/main/res/layout/activity_main.xml")
    layoutFile.writeText(
      """
      <?xml version="1.0" encoding="utf-8"?>
      <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
          android:layout_width="match_parent"
          android:layout_height="match_parent"
          android:orientation="vertical">
          <LinearLayout
              android:layout_width="match_parent"
              android:layout_height="wrap_content"
              android:orientation="vertical">
              <TextView
                  android:layout_width="wrap_content"
                  android:layout_height="wrap_content"
                  android:text="@string/app_name" />
          </LinearLayout>
      </LinearLayout>
      """.trimIndent()
    )
  }

  private fun createLayoutWithHardcodedText() {
    val layoutFile = File(tempFolder.root, "app/src/main/res/layout/activity_main.xml")
    layoutFile.writeText(
      """
      <?xml version="1.0" encoding="utf-8"?>
      <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
          android:layout_width="match_parent"
          android:layout_height="match_parent"
          android:orientation="vertical">
          <TextView
              android:layout_width="wrap_content"
              android:layout_height="wrap_content"
              android:text="Hardcoded text here" />
      </LinearLayout>
      """.trimIndent()
    )
  }

  private fun createLayoutWithInvalidId() {
    val layoutFile = File(tempFolder.root, "app/src/main/res/layout/activity_main.xml")
    layoutFile.writeText(
      """
      <?xml version="1.0" encoding="utf-8"?>
      <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
          android:layout_width="match_parent"
          android:layout_height="match_parent"
          android:orientation="vertical">
          <TextView
              android:id="@+i/123invalid_id"
              android:layout_width="wrap_content"
              android:layout_height="wrap_content"
              android:text="@string/app_name" />
      </LinearLayout>
      """.trimIndent()
    )
  }

  private fun createProjectDescriptionFile(): File {
    val projectDescriptionFile = File(tempFolder.root, "lint-project-description.xml")
    val rootPath = tempFolder.root.absolutePath

    projectDescriptionFile.writeText(
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <project android="true" incomplete="false" desugar="full" client="cli">
        <root dir="$rootPath"/>
        <module name="app" library="false" android="true" compile-sdk-version="34"
                javaLanguage="11" kotlinLanguage="1.6">
          <manifest file="$rootPath/app/src/main/AndroidManifest.xml"/>
          <src dir="$rootPath/app/src/main/java"/>
          <resource dir="$rootPath/app/src/main/res"/>
        </module>
      </project>
      """.trimIndent()
    )

    return projectDescriptionFile
  }
}
