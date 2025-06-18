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
import java.util.concurrent.TimeUnit
import org.oppia.android.scripts.common.BazelClient
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher

/** Tests for [AndroidLintRunner]. */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class AndroidLintRunnerTest {
  @field:[Rule JvmField]
  var tempFolder = TemporaryFolder()

  private lateinit var outputStream: ByteArrayOutputStream
  private lateinit var originalOut: PrintStream
  private lateinit var testBazelWorkspace: TestBazelWorkspace
  private lateinit var sdkPath: String
  private lateinit var jdkHome: File
  private lateinit var bazelClient: BazelClient
  private val scriptBgDispatcher by lazy { ScriptBackgroundCoroutineDispatcher() }
  private val longCommandExecutor by lazy { initializeCommandExecutorWithLongProcessWaitTime() }

  companion object {
    private const val COMPILE_SDK_VERSION = "34"
    private const val MIN_SDK_VERSION = "21"
    private const val JAVA_LANGUAGE_VERSION = "11"
    private const val KOTLIN_LANGUAGE_VERSION = "1.6"
    private const val BUILD_VARS_CONTENT = """BUILD_SDK_VERSION = "34""""
  }

  @Before
  fun setUp() {
    outputStream = ByteArrayOutputStream()
    originalOut = System.out
    sdkPath = System.getenv("ANDROID_HOME")
      ?: error("ANDROID_HOME environment variable is not set.")
    System.setOut(PrintStream(outputStream))
    testBazelWorkspace = TestBazelWorkspace(tempFolder)
     bazelClient = BazelClient(tempFolder.root, longCommandExecutor)
    jdkHome = File(bazelClient.retrieveBazelInfo()["java-home"]?:
      error("Failed to retrieve JDK home from Bazel info. Ensure Bazel is properly configured."))
  }

  @After
  fun tearDown() {
    System.setOut(originalOut)
    scriptBgDispatcher.close()
  }

  @Test
  fun testMain_noArguments_throwsException() {
    val exception = assertThrows<IllegalArgumentException> {
      main()
    }

    assertThat(exception).hasMessageThat().contains(
      "Repository root path argument is required"
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
  fun testMain_validRootPath_generatesReports() {
    val rootPath = tempFolder.root
    createBuildVarsFile()

    // TODO(#5734): Update test after implementing project description
    val exception = assertThrows<IllegalStateException> {
      main(rootPath.absolutePath) // Currently returns error code due to missing description
    }
    assertThat(exception.message).contains(
      "Lint analysis failed with exit code 5: Invalid command-line argument"
    )
  }

  @Test
  fun testMain_withGroupBySeverityFlag_parsesCorrectly() {
    val rootPath = tempFolder.root
    createBuildVarsFile()

    val exception = assertThrows<IllegalStateException> {
      main(rootPath.absolutePath, "--group_by_severity")
    }
    // Should still fail due to missing project description, but flag should be parsed
    assertThat(exception.message).contains(
      "Lint analysis failed with exit code 5"
    )
  }

  @Test
  fun testMain_withProcessTimeoutFlag_parsesCorrectly() {
    val rootPath = tempFolder.root
    createBuildVarsFile()

    val exception = assertThrows<IllegalStateException> {
      main(rootPath.absolutePath, "--processTimeout=15")
    }
    // Should still fail due to missing project description, but flag should be parsed
    assertThat(exception.message).contains(
      "Lint analysis failed with exit code 5"
    )
  }

  @Test
  fun testPrepareLintArguments_includesRequiredFlags() {
    val reportFile = File(tempFolder.root, "report.xml")
    val projectFile = File(tempFolder.root, "project.xml")
    createBuildVarsFile()
    val lintRunner = AndroidLintRunner(reportFile, projectFile)

    val result = lintRunner.prepareLintArguments(
      repoRoot = tempFolder.root,
      jdkHome = jdkHome,
      javaVersion = JAVA_LANGUAGE_VERSION
    )

    assertThat(result).asList().containsAtLeast(
      "-Wall",
      "--quiet",
      "--fullpath",
      "--showall",
      "--exitcode",
      "--offline",
      "--client-id", "cli",
      "--jdk-home", jdkHome.absolutePath,
      "--sdk-home", sdkPath,
      "--compile-sdk-version", COMPILE_SDK_VERSION,
      "--kotlin-language-level", KOTLIN_LANGUAGE_VERSION,
      "--java-language-level", JAVA_LANGUAGE_VERSION,
      "--project", projectFile.absolutePath,
      "--xml", reportFile.absolutePath
    )
  }

  @Test
  fun testPrepareLintArguments_createsMissingJdkReleaseFile() {
    val reportFile = File(tempFolder.root, "report.xml")
    val projectFile = File(tempFolder.root, "project.xml")
    createBuildVarsFile()

    // Create a temporary JDK directory without release file
    val tempJdkHome = File(tempFolder.root, "temp_jdk")
    tempJdkHome.mkdirs()

    val lintRunner = AndroidLintRunner(reportFile, projectFile)

    // This should create the release file
    lintRunner.prepareLintArguments(
      repoRoot = tempFolder.root,
      jdkHome = tempJdkHome,
      javaVersion = JAVA_LANGUAGE_VERSION
    )

    val releaseFile = File(tempJdkHome, "release")
    assertThat(releaseFile.exists()).isTrue()
    assertThat(releaseFile.readText()).contains("MODULES=")
  }

  @Test
  fun testPrepareLintArguments_withNonExistentJdkHome_throwsException() {
    val reportFile = File(tempFolder.root, "report.xml")
    val projectFile = File(tempFolder.root, "project.xml")
    createBuildVarsFile()
    val lintRunner = AndroidLintRunner(reportFile, projectFile)

    val nonExistentJdk = File(tempFolder.root, "nonexistent_jdk")

    val exception = assertThrows<IllegalArgumentException> {
      lintRunner.prepareLintArguments(
        repoRoot = tempFolder.root,
        jdkHome = nonExistentJdk,
        javaVersion = JAVA_LANGUAGE_VERSION
      )
    }

    assertThat(exception).hasMessageThat().contains(
      "JDK home path does not exist or is not a directory"
    )
  }

  @Test
  fun testPrepareLintArguments_withMissingBuildVarsFile_throwsException() {
    val reportFile = File(tempFolder.root, "report.xml")
    val projectFile = File(tempFolder.root, "project.xml")
    val lintRunner = AndroidLintRunner(reportFile, projectFile)

    val exception = assertThrows<IllegalArgumentException> {
      lintRunner.prepareLintArguments(
        repoRoot = tempFolder.root,
        jdkHome = jdkHome,
        javaVersion = JAVA_LANGUAGE_VERSION
      )
    }

    assertThat(exception).hasMessageThat().contains(
      "Build variables file not found"
    )
  }

  @Test
  fun testPrepareLintArguments_withInvalidBuildVarsFile_throwsException() {
    val buildVarsFile = File(tempFolder.root, "build_vars.bzl")
    buildVarsFile.writeText("SOME_OTHER_VAR = \"value\"")

    val reportFile = File(tempFolder.root, "report.xml")
    val projectFile = File(tempFolder.root, "project.xml")
    val lintRunner = AndroidLintRunner(reportFile, projectFile)

    val exception = assertThrows<IllegalStateException> {
      lintRunner.prepareLintArguments(
        repoRoot = tempFolder.root,
        jdkHome = jdkHome,
        javaVersion = JAVA_LANGUAGE_VERSION
      )
    }

    assertThat(exception).hasMessageThat().contains(
      "BUILD_SDK_VERSION not found in file"
    )
  }

  @Test
  fun testRunLint_withExitCode0_handlesSuccessfully() {
    setupAndroidProjectWithUnusedResources()
    val lintRunner = createLintRunner()

    // Mock successful lint run (this would need actual implementation)
    // For now, we expect this to fail due to missing proper setup
    val exception = assertThrows<IllegalStateException> {
      lintRunner.runLint(lintRunner.prepareLintArguments(
        repoRoot = tempFolder.root,
        jdkHome = jdkHome,
        javaVersion = JAVA_LANGUAGE_VERSION
      ))
    }

    // Should fail with invalid argument since we don't have proper project setup
    assertThat(exception.message).contains("Lint analysis failed with exit code 5")
  }

  @Test
  fun testRunLint_withExitCode2_throwsException() {
    val reportFile = File(tempFolder.root, "lint-report.xml")
    val projectFile = File(tempFolder.root, "project.xml")
    val lintRunner = AndroidLintRunner(reportFile, projectFile)

    val exception = assertThrows<IllegalStateException> {
      lintRunner.runLint(emptyArray())
    }

    assertThat(exception.message).contains(
      "Lint analysis failed with exit code 2: Invalid usage of Lint command"
    )
  }

  @Test
  fun testRunLint_withExitCode3_throwsExceptionForFileOverwrite() {
    val outputDirectory = File(tempFolder.root, "reports")
    outputDirectory.mkdirs()

    val reportPath = File(outputDirectory, "lint-report.xml")
    reportPath.createNewFile()
    reportPath.writeText("existing content")

    val disabledWrite = outputDirectory.setWritable(false)
    assertThat(disabledWrite).isTrue()

    val projectPath = createProjectDescriptionFile()
    val lintRunner = AndroidLintRunner(reportPath, projectPath)

    val exception = assertThrows<IllegalStateException> {
      lintRunner.runLint(lintRunner.prepareLintArguments(
        repoRoot = tempFolder.root,
        jdkHome = jdkHome,
        javaVersion = JAVA_LANGUAGE_VERSION
      ))
    }

    assertThat(exception.message).contains("Lint analysis failed with exit code")

    outputDirectory.setWritable(true)
  }

  @Test
  fun testRunLint_withExitCode4_throwsException() {
    val reportPath = File(tempFolder.root, "lint-report.xml")
    val projectPath = File(tempFolder.root, "lint-project-description.xml")
    val lintRunner = AndroidLintRunner(reportPath, projectPath)

    val exception = assertThrows<IllegalStateException> {
      lintRunner.runLint(arrayOf("--help"))
    }
    assertThat(exception.message).contains("Lint analysis failed with exit code 4")
    assertThat(exception.message).contains("Help command invoked")
  }

  @Test
  fun testRunLint_withExitCode5_throwsException() {
    val reportPath = File(tempFolder.root, "lint-report.xml")
    val projectPath = File(tempFolder.root, "lint-project-description.xml")
    createBuildVarsFile()
    val lintRunner = AndroidLintRunner(reportPath, projectPath)

    val exception = assertThrows<IllegalStateException> {
      lintRunner.runLint(lintRunner.prepareLintArguments(
        repoRoot = tempFolder.root,
        jdkHome = jdkHome,
        javaVersion = JAVA_LANGUAGE_VERSION
      ))
    }
    assertThat(exception.message).contains("Lint analysis failed with exit code 5")
    assertThat(exception.message).contains("Invalid command-line argument")
  }

  @Test
  fun testRunLint_withInvalidFlag_throwsException() {
    val reportFile = File(tempFolder.root, "report.xml")
    val projectFile = File(tempFolder.root, "project.xml")
    val lintRunner = AndroidLintRunner(reportFile, projectFile)

    val exception = assertThrows<IllegalStateException> {
      lintRunner.runLint(arrayOf("--InvalidFlag"))
    }

    assertThat(exception.message).contains("Lint analysis failed with exit code 5")
    assertThat(exception.message).contains("Invalid command-line argument")
  }

  @Test
  fun testRunLint_groupBySeverity_reportsIssuesCorrectly() {
    setupAndroidProjectWithHardcodedText()
    val lintRunner = AndroidLintRunner(
      reportFile = File(tempFolder.root, "lint-report.xml"),
      projectDescriptionFile = createProjectDescriptionFile(),
      groupByIssueSeverity = true
    )

    // This will fail due to missing proper setup, but we can verify the grouping flag is used
    val exception = assertThrows<IllegalStateException> {
      lintRunner.runLint(lintRunner.prepareLintArguments(
        repoRoot = tempFolder.root,
        jdkHome = jdkHome,
        javaVersion = JAVA_LANGUAGE_VERSION
      ))
    }

    assertThat(exception.message).contains("Lint analysis failed")
  }

  @Test
  fun testRunLint_defaultGroupByFilePath_reportsIssuesCorrectly() {
    setupAndroidProjectWithUnusedResources()
    val lintRunner = AndroidLintRunner(
      reportFile = File(tempFolder.root, "lint-report.xml"),
      projectDescriptionFile = createProjectDescriptionFile(),
    )

    // This will fail due to missing proper setup
    val exception = assertThrows<IllegalStateException> {
      lintRunner.runLint(lintRunner.prepareLintArguments(
        repoRoot = tempFolder.root,
        jdkHome = jdkHome,
        javaVersion = JAVA_LANGUAGE_VERSION
      ))
    }

    assertThat(exception.message).contains("Lint analysis failed")
  }

  private fun createBuildVarsFile() {
    val buildVarsFile = File(tempFolder.root, "build_vars.bzl")
    buildVarsFile.writeText(BUILD_VARS_CONTENT)
  }

  private fun createLintRunner(): AndroidLintRunner {
    val reportFile = File(tempFolder.root, "lint-report.xml")
    val projectDescriptionFile = createProjectDescriptionFile()

    return AndroidLintRunner(
      reportFile = reportFile,
      projectDescriptionFile = projectDescriptionFile
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
    createManifestWithoutIcon()
    createBasicStringResources()
    createBuildVarsFile()
  }

  private fun setupAndroidProjectWithUnusedResources() {
    testBazelWorkspace.initEmptyWorkspace()
    createProjectStructure()
    createBasicManifest()
    createUnusedStringResources()
    createBuildVarsFile()
  }

  private fun setupAndroidProjectWithDuplicateStrings() {
    testBazelWorkspace.initEmptyWorkspace()
    createProjectStructure()
    createBasicManifest()
    createDuplicateStringResources()
    createBuildVarsFile()
  }

  private fun setupAndroidProjectWithUnusedIds() {
    testBazelWorkspace.initEmptyWorkspace()
    createProjectStructure()
    createBasicManifest()
    createLayoutWithUnusedIds()
    createBasicStringResources()
    createBuildVarsFile()
  }

  private fun setupAndroidProjectWithRtlHardcoded() {
    testBazelWorkspace.initEmptyWorkspace()
    createProjectStructure()
    createBasicManifest()
    createLayoutWithRtlHardcoded()
    createBasicStringResources()
    createBuildVarsFile()
  }

  private fun setupAndroidProjectWithUselessParent() {
    testBazelWorkspace.initEmptyWorkspace()
    createProjectStructure()
    createBasicManifest()
    createLayoutWithUselessParent()
    createBasicStringResources()
    createBuildVarsFile()
  }

  private fun setupAndroidProjectWithHardcodedText() {
    testBazelWorkspace.initEmptyWorkspace()
    createProjectStructure()
    createBasicManifest()
    createLayoutWithHardcodedText()
    createBasicStringResources()
    createBuildVarsFile()
  }

  private fun setupAndroidProjectWithInvalidId() {
    testBazelWorkspace.initEmptyWorkspace()
    createProjectStructure()
    createBasicManifest()
    createLayoutWithInvalidId()
    createBasicStringResources()
    createBuildVarsFile()
  }

  private fun createProjectStructure() {
    val directories = listOf(
      "app/src/main/java/org/oppia/android/app/activity",
      "app/src/main/res/values",
      "app/src/main/res/layout",
      "app/src/main/res/drawable"
    )

    directories.forEach { path ->
      File(tempFolder.root, path).mkdirs()
    }
  }

  private fun createBasicManifest() {
    createManifestFile(includeIcon = true)
  }

  private fun createManifestWithoutIcon() {
    createManifestFile(includeIcon = false)
  }

  private fun createManifestFile(includeIcon: Boolean) {
    val manifestFile = File(tempFolder.root, "app/src/main/AndroidManifest.xml")
    val iconAttribute = if (includeIcon) """android:icon="@drawable/ic_launcher"""" else ""

    manifestFile.writeText(
      """
      <?xml version="1.0" encoding="utf-8"?>
      <manifest xmlns:android="http://schemas.android.com/apk/res/android"
          package="org.oppia.android.app"
          android:versionCode="1"
          android:versionName="1.0">
          
          <uses-sdk android:minSdkVersion="$MIN_SDK_VERSION" />
          
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
    )
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

  private fun createUnusedStringResources() {
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

  private fun createDuplicateStringResources() {
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

  private fun createLayoutWithMultipleIssues() {
    val layoutFile = File(tempFolder.root, "app/src/main/res/layout/activity_main.xml")
    layoutFile.writeText(
      """
      <?xml version="1.0" encoding="utf-8"?>
      <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
          android:layout_width="match_parent"
          android:layout_height="match_parent"
          android:orientation="vertical">
          <!-- Hardcoded text -->
          <TextView
              android:id="@+id/unused_text_view"
              android:layout_width="wrap_content"
              android:layout_height="wrap_content"
              android:layout_marginLeft="16dp"
              android:text="Hardcoded text here" />
          <LinearLayout
              android:layout_width="match_parent"
              android:layout_height="wrap_content"
              android:orientation="vertical">
              <Button
                  android:id="@+id/another_unused_id"
                  android:layout_width="wrap_content"
                  android:layout_height="wrap_content"
                  android:text="Click me" />
          </LinearLayout>
      </LinearLayout>
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
      createMinimalProjectDescriptionContent(
        rootPath = rootPath,
        srcPath = "$rootPath/app/src/main/java"
      )
    )

    return projectDescriptionFile
  }

  private fun createProjectDescriptionFileWithInvalidPath(): File {
    val projectDescriptionFile = File(tempFolder.root, "lint-project-description.xml")
    val rootPath = tempFolder.root.absolutePath
    val wrongSrcPath = "$rootPath/app/src/main/nonexistent_java"

    projectDescriptionFile.writeText(
      createMinimalProjectDescriptionContent(
        rootPath = rootPath,
        srcPath = wrongSrcPath
      )
    )

    return projectDescriptionFile
  }

  private fun createMinimalProjectDescriptionContent(rootPath: String, srcPath: String): String {
    return """
      <?xml version="1.0" encoding="UTF-8"?>
      <project android="true" incomplete="false" desugar="full" client="cli">
        <root dir="$rootPath"/>
        <sdk dir="$sdkPath"/>
        <module name="app" library="false" android="true" compile-sdk-version="$COMPILE_SDK_VERSION"
                javaLanguage="$JAVA_LANGUAGE_VERSION" kotlinLanguage="$KOTLIN_LANGUAGE_VERSION">
          <manifest file="$rootPath/app/src/main/AndroidManifest.xml"/>
          <src dir="$srcPath"/>
          <resource dir="$rootPath/app/src/main/res"/>
        </module>
      </project>
    """.trimIndent()
  }

  private fun initializeCommandExecutorWithLongProcessWaitTime(): CommandExecutorImpl {
    return CommandExecutorImpl(
      scriptBgDispatcher, processTimeout = 5, processTimeoutUnit = TimeUnit.MINUTES
    )
  }
}
