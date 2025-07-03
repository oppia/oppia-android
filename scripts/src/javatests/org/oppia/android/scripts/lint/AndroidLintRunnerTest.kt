package org.oppia.android.scripts.lint

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.common.AndroidBuildSdkProperties
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

  private lateinit var outputStream: ByteArrayOutputStream
  private lateinit var originalOut: PrintStream
  private lateinit var testBazelWorkspace: TestBazelWorkspace
  private lateinit var sdkPath: String
  private lateinit var jdkHome: File
  private lateinit var buildSdkVersion: String
  private lateinit var kotlinVersion: String

  companion object {
    private const val MIN_SDK_VERSION = "21"
    private const val JAVA_VERSION = "11"
  }

  @Before
  fun setUp() {
    outputStream = ByteArrayOutputStream()
    originalOut = System.out
    sdkPath = System.getenv("ANDROID_HOME")
      ?: error("ANDROID_HOME environment variable is not set.")
    jdkHome = File(
      System.getProperty("java.home")
        ?: error("java.home system property is not set.")
    )
    System.setOut(PrintStream(outputStream))
    testBazelWorkspace = TestBazelWorkspace(tempFolder)
    val sdkProperties = AndroidBuildSdkProperties()
    buildSdkVersion = sdkProperties.buildSdkVersion.toString()
    kotlinVersion = sdkProperties.kotlinCompilerVersion
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
  fun testMain_validRootPath_generatesReports() {
    val rootPath = tempFolder.root

    // TODO(#5734): Update test once final lint tool configurations are done
    assertThrows<IllegalStateException> {
      main(rootPath.absolutePath) // Currently returns error code due to missing description
    }
  }

  @Test
  fun testPrepareLintArguments_ensuresAllRequiredArgumentsArePresent() {
    val reportFile = File(tempFolder.root, "report.xml")
    val projectFile = File(tempFolder.root, "project.xml")
    val lintRunner = AndroidLintRunner(reportFile, projectFile)

    val result = lintRunner.prepareLintArguments(
      jdkHome,
      JAVA_VERSION,
      buildSdkVersion,
      kotlinVersion
    )

    val expectedArguments = listOf(
      "-Wall",
      "--quiet",
      "--fullpath",
      "--showall",
      "--exitcode",
      "--offline",
      "--client-id", "cli",
      "--jdk-home", jdkHome.absolutePath,
      "--sdk-home", sdkPath,
      "--compile-sdk-version", buildSdkVersion,
      "--kotlin-language-level", kotlinVersion,
      "--java-language-level", JAVA_VERSION,
      "--project", projectFile.absolutePath,
      "--xml", reportFile.absolutePath
    )

    assertThat(result.toList()).containsExactlyElementsIn(expectedArguments)
  }

  @Test
  fun testJavaConfiguration_withValidBazelInfo_extractsJdkHomeAndVersion() {
    val bazelInfo = mapOf(
      "java-home" to "/usr/lib/jvm/java-11-openjdk",
      "java-runtime" to "OpenJDK Runtime Environment (build 11.0.6+10-LTS) by Azul Systems, Inc."
    )

    val javaConfigClass = Class.forName(
      "org.oppia.android.scripts.lint.AndroidLintAnalyzer\$JavaConfiguration"
    )
    val constructor = javaConfigClass.getDeclaredConstructor(Map::class.java)
    constructor.isAccessible = true
    val javaConfig = constructor.newInstance(bazelInfo)

    val getJdkHomeMethod = javaConfigClass.getDeclaredMethod("getJdkHome")
    getJdkHomeMethod.isAccessible = true
    val jdkHome = getJdkHomeMethod.invoke(javaConfig) as File

    val getVersionMethod = javaConfigClass.getDeclaredMethod("getVersion")
    getVersionMethod.isAccessible = true
    val version = getVersionMethod.invoke(javaConfig) as String

    assertThat(jdkHome.absolutePath).isEqualTo("/usr/lib/jvm/java-11-openjdk")
    assertThat(version).isEqualTo("11.0.6")
  }

  @Test
  fun testJavaConfiguration_withMissingJavaRuntime_throwsException() {
    val bazelInfo = mapOf(
      "java-home" to "/usr/lib/jvm/java-11-openjdk"
    )

    val javaConfigClass = Class.forName(
      "org.oppia.android.scripts.lint.AndroidLintAnalyzer\$JavaConfiguration"
    )
    val constructor = javaConfigClass.getDeclaredConstructor(Map::class.java)
    constructor.isAccessible = true

    val exception = assertThrows<Exception> {
      constructor.newInstance(bazelInfo)
    }

    val cause = exception.cause
    assertThat(cause).isInstanceOf(IllegalStateException::class.java)
    assertThat(cause?.message).contains("java-runtime not found in bazel info output")
  }

  @Test
  fun testJavaConfiguration_withInvalidVersionFormat_throwsException() {
    val bazelInfo = mapOf(
      "java-home" to "/usr/lib/jvm/java-11-openjdk",
      "java-runtime" to "Invalid runtime format without version"
    )

    val javaConfigClass = Class.forName(
      "org.oppia.android.scripts.lint.AndroidLintAnalyzer\$JavaConfiguration"
    )
    val constructor = javaConfigClass.getDeclaredConstructor(Map::class.java)
    constructor.isAccessible = true

    val exception = assertThrows<Exception> {
      constructor.newInstance(bazelInfo)
    }

    val cause = exception.cause
    assertThat(cause).isInstanceOf(IllegalStateException::class.java)
    assertThat(cause?.message).contains("Could not extract Java version from")
  }

  @Test
  fun testPrepareLintArguments_withCustomBuildSdkVersion_includesCorrectVersion() {
    val reportFile = File(tempFolder.root, "report.xml")
    val projectFile = File(tempFolder.root, "project.xml")
    val lintRunner = AndroidLintRunner(reportFile, projectFile)
    val customBuildSdk = "34"

    val result = lintRunner.prepareLintArguments(
      jdkHome,
      JAVA_VERSION,
      customBuildSdk,
      kotlinVersion
    )

    assertThat(result).asList().contains("--compile-sdk-version")
    val sdkVersionIndex = result.indexOf("--compile-sdk-version")
    assertThat(result[sdkVersionIndex + 1]).isEqualTo(customBuildSdk)
  }

  @Test
  fun testPrepareLintArguments_withExistingReleaseFile_doesNotOverwrite() {
    val tempJdkDir = File(tempFolder.root, "temp_jdk_with_release")
    tempJdkDir.mkdirs()

    val releaseFile = File(tempJdkDir, "release")
    val originalContent = "ORIGINAL_CONTENT=test"
    releaseFile.writeText(originalContent)

    val reportFile = File(tempFolder.root, "report.xml")
    val projectFile = File(tempFolder.root, "project.xml")
    val lintRunner = AndroidLintRunner(reportFile, projectFile)

    lintRunner.prepareLintArguments(
      jdkHome,
      JAVA_VERSION,
      buildSdkVersion,
      kotlinVersion
    )

    assertThat(releaseFile.readText()).isEqualTo(originalContent)
  }

  @Test
  fun testPrepareLintArguments_generatesValidModulesString() {
    val tempJdkDir = File(tempFolder.root, "temp_jdk_modules")
    tempJdkDir.mkdirs()

    val reportFile = File(tempFolder.root, "report.xml")
    val projectFile = File(tempFolder.root, "project.xml")
    val lintRunner = AndroidLintRunner(reportFile, projectFile)

    lintRunner.prepareLintArguments(
      jdkHome,
      JAVA_VERSION,
      buildSdkVersion,
      kotlinVersion
    )

    val releaseFile = File(tempJdkDir, "release")
    assertThat(releaseFile.exists()).isTrue()

    val releaseContent = releaseFile.readText()
    assertThat(releaseContent).startsWith("MODULES=\"")
    assertThat(releaseContent).endsWith("\"")
    assertThat(releaseContent).contains("java.base") // Common module that should be present
  }

  @Test
  fun testRunLint_whenExitCodeIs0_shouldPassSuccessfully() {
    setupAndroidProjectWithUnusedResources()
    val lintRunner = createLintRunner()
    lintRunner.runLint(
      lintRunner.prepareLintArguments(
        jdkHome,
        JAVA_VERSION,
        buildSdkVersion,
        kotlinVersion
      )
    )

    val output = outputStream.toString()
    assertThat(output).contains("${GREEN}ANDROID LINT CHECK ${BOLD}PASSED$RESET")
  }

  @Test
  fun testRunLint_whenExitCodeIs1_shouldFailScript() {
    setupAndroidProjectWithInvalidId()
    val lintRunner = createLintRunner()
    val exception = assertThrows<IllegalStateException> {
      lintRunner.runLint(
        lintRunner.prepareLintArguments(
          jdkHome,
          JAVA_VERSION,
          buildSdkVersion,
          kotlinVersion
        )
      )
    }

    val reportFile = File(tempFolder.root, "lint-report.xml")
    assertThat(reportFile.exists()).isTrue()
    assertThat(exception.message).contains("${RED}ANDROID LINT CHECK ${BOLD}FAILED$RESET")
  }

  @Test
  fun testRunLint_withExitCode2_throwsException() {
    setupAndroidProjectWithInvalidId()
    val lintRunner = createLintRunner()
    val exception = assertThrows<IllegalStateException> {
      lintRunner.runLint(emptyArray())
    }

    assertThat(exception.message).contains(
      "Lint analysis failed with exit code 2: Invalid usage of Lint command."
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
      lintRunner.runLint(
        lintRunner.prepareLintArguments(
          jdkHome,
          JAVA_VERSION,
          buildSdkVersion,
          kotlinVersion
        )
      )
    }

    assertThat(exception.message).contains("Lint analysis failed with exit code 3")
    assertThat(exception.message).contains("Cannot overwrite existing file")

    outputDirectory.setWritable(true)
  }

  @Test
  fun testRunLint_withExitCode4_throwsException() {
    val reportPath = File(tempFolder.root, "lint-report.xml")
    val projectPath = File(tempFolder.root, "lint-project-description.xml")
    val lintRunner = AndroidLintRunner(reportPath, projectPath)

    // Won't happen in actual usage.
    val exception = assertThrows<IllegalStateException> {
      lintRunner.runLint(arrayOf("--help"))
    }
    assertThat(exception.message).contains("Lint analysis failed with exit code 4")
    assertThat(exception.message).contains("Help command invoked.")
  }

  @Test
  fun testRunLint_withExitCode5_throwsException() {
    val reportPath = File(tempFolder.root, "lint-report.xml")
    val projectPath = File(tempFolder.root, "lint-project-description.xml")
    val lintRunner = AndroidLintRunner(reportPath, projectPath)

    val exception = assertThrows<IllegalStateException> {
      lintRunner.runLint(
        lintRunner.prepareLintArguments(
          jdkHome,
          JAVA_VERSION,
          buildSdkVersion,
          kotlinVersion
        )
      )
    }
    assertThat(exception.message).contains("Lint analysis failed with exit code 5")
    assertThat(exception.message).contains("Invalid command-line argument")
  }

  @Test
  fun testRunLint_multipleIssueTypes_detectsAll() {
    testBazelWorkspace.initEmptyWorkspace()
    createProjectStructure()
    createBasicManifest()
    createLayoutWithMultipleIssues()
    createBasicStringResources()

    val lintRunner = createLintRunner()
    val exception = assertThrows<IllegalStateException> {
      lintRunner.runLint(
        lintRunner.prepareLintArguments(
          jdkHome,
          JAVA_VERSION,
          buildSdkVersion,
          kotlinVersion
        )
      )
    }

    val reportFile = File(tempFolder.root, "lint-report.xml")
    val reportContent = reportFile.readText()

    assertThat(exception.message).contains("${RED}ANDROID LINT CHECK ${BOLD}FAILED$RESET")
    assertThat(reportContent).contains("InvalidId")
    assertThat(reportContent).contains("HardcodedText")
    assertThat(reportContent).contains("RtlHardcoded")
    assertThat(reportContent).contains("UnusedIds")
  }

  @Test
  fun testRunLint_withProjectDescription_withNonExistentFilePath_throwsInternalIssue() {
    testBazelWorkspace.initEmptyWorkspace()
    createProjectStructure()
    createBasicManifest()
    createBasicStringResources()

    val projectDescriptionFile = createProjectDescriptionFileWithInvalidPath()
    val reportFile = File(tempFolder.root, "lint-report.xml")
    val lintRunner = AndroidLintRunner(
      reportFile = reportFile,
      projectDescriptionFile = projectDescriptionFile
    )

    val exception = assertThrows<IllegalStateException> {
      lintRunner.runLint(
        lintRunner.prepareLintArguments(
          jdkHome,
          JAVA_VERSION,
          buildSdkVersion,
          kotlinVersion
        )
      )
    }

    assertThat(exception.message)
      .contains("${RED}ANDROID LINT CHECK ${BOLD}FAILED WITH INTERNAL LINT ISSUES$RESET")
    assertThat(reportFile.exists()).isTrue()
    val report = reportFile.readText()
    assertThat(report).contains("LintError")
    assertThat(report).contains("app/src/main/nonexistent_java does not exist")
    assertThat(report).contains("line=\"7\"")
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
  fun testRunLint_missingApplicationIcon_detectsIssue() {
    setupAndroidProjectWithoutApplicationIcon()
    val lintRunner = createLintRunner()

    lintRunner.runLint(
      lintRunner.prepareLintArguments(
        jdkHome,
        JAVA_VERSION,
        buildSdkVersion,
        kotlinVersion
      )
    )

    verifyLintReportContains("MissingApplicationIcon")
  }

  @Test
  fun testRunLint_unusedResources_detectsIssue() {
    setupAndroidProjectWithUnusedResources()
    val lintRunner = createLintRunner()

    lintRunner.runLint(
      lintRunner.prepareLintArguments(
        jdkHome,
        JAVA_VERSION,
        buildSdkVersion,
        kotlinVersion
      )
    )

    verifyLintReportContains("UnusedResources")
  }

  @Test
  fun testRunLint_duplicateStrings_detectsIssue() {
    setupAndroidProjectWithDuplicateStrings()
    val lintRunner = createLintRunner()

    lintRunner.runLint(
      lintRunner.prepareLintArguments(
        jdkHome,
        JAVA_VERSION,
        buildSdkVersion,
        kotlinVersion
      )
    )

    verifyLintReportContains("DuplicateStrings")
  }

  @Test
  fun testRunLint_unusedIds_detectsIssue() {
    setupAndroidProjectWithUnusedIds()
    val lintRunner = createLintRunner()

    lintRunner.runLint(
      lintRunner.prepareLintArguments(
        jdkHome,
        JAVA_VERSION,
        buildSdkVersion,
        kotlinVersion
      )
    )

    verifyLintReportContains("UnusedIds")
  }

  @Test
  fun testRunLint_rtlHardcoded_detectsIssue() {
    setupAndroidProjectWithRtlHardcoded()
    val lintRunner = createLintRunner()

    lintRunner.runLint(
      lintRunner.prepareLintArguments(
        jdkHome,
        JAVA_VERSION,
        buildSdkVersion,
        kotlinVersion
      )
    )

    verifyLintReportContains("RtlHardcoded")
  }

  @Test
  fun testRunLint_uselessParent_detectsIssue() {
    setupAndroidProjectWithUselessParent()
    val lintRunner = createLintRunner()

    lintRunner.runLint(
      lintRunner.prepareLintArguments(
        jdkHome,
        JAVA_VERSION,
        buildSdkVersion,
        kotlinVersion
      )
    )

    verifyLintReportContains("UselessParent")
  }

  @Test
  fun testRunLint_hardcodedText_detectsIssue() {
    setupAndroidProjectWithHardcodedText()
    val lintRunner = createLintRunner()

    lintRunner.runLint(
      lintRunner.prepareLintArguments(
        jdkHome,
        JAVA_VERSION,
        buildSdkVersion,
        kotlinVersion
      )
    )

    verifyLintReportContains("HardcodedText")
  }

  @Test
  fun testRunLint_invalidId_detectsIssue() {
    setupAndroidProjectWithInvalidId()
    val lintRunner = createLintRunner()

    val exception = assertThrows<IllegalStateException> {
      lintRunner.runLint(
        lintRunner.prepareLintArguments(
          jdkHome,
          JAVA_VERSION,
          buildSdkVersion,
          kotlinVersion
        )
      )
    }

    assertThat(exception.message).isEqualTo("${RED}ANDROID LINT CHECK ${BOLD}FAILED$RESET")
    verifyLintReportContains("InvalidId")
  }

  @Test
  fun testRunLint_groupBySeverity_reportsIssuesCorrectly() {
    setupAndroidProjectWithHardcodedText()
    val lintRunner = AndroidLintRunner(
      reportFile = File(tempFolder.root, "lint-report.xml"),
      projectDescriptionFile = createProjectDescriptionFile(),
      groupByIssueSeverity = true
    )

    lintRunner.runLint(
      lintRunner.prepareLintArguments(jdkHome, JAVA_VERSION, buildSdkVersion, kotlinVersion)
    )
    val outputContent = outputStream.toString()
    assertThat(outputContent).contains("SEVERITY: WARNING")
    assertThat(outputContent).contains("HardcodedText")
    assertThat(outputContent).contains("app/src/main/res/layout/activity_main.xml")
    assertThat(outputContent).contains("Line: 9")
    assertThat(outputContent).contains("android:text=\"Hardcoded text here\" />")
  }

  @Test
  fun testRunLint_defaultGroupByFilePath_reportsIssuesCorrectly() {
    setupAndroidProjectWithUnusedResources()
    val lintRunner = AndroidLintRunner(
      reportFile = File(tempFolder.root, "lint-report.xml"),
      projectDescriptionFile = createProjectDescriptionFile(),
    )

    lintRunner.runLint(
      lintRunner.prepareLintArguments(jdkHome, JAVA_VERSION, buildSdkVersion, kotlinVersion)
    )
    val outputContent = outputStream.toString()
    assertThat(outputContent).contains("FILE:")
    assertThat(outputContent).contains("app/src/main/res/values/strings.xml")
    assertThat(outputContent).contains("Issue #1: UnusedResources")
    assertThat(outputContent).contains("Line: 4")
    assertThat(outputContent).contains(
      "<string name=\"unused_string\">This string is never used</string>"
    )
  }

  @Test
  fun testPrepareLintArguments_withJdkEnvironmentSetup_createsReleaseFile() {
    val tempJdkDir = File(tempFolder.root, "temp_jdk")
    tempJdkDir.mkdirs()

    val reportFile = File(tempFolder.root, "report.xml")
    val projectFile = File(tempFolder.root, "project.xml")
    val lintRunner = AndroidLintRunner(reportFile, projectFile)

    // Ensure no release file exists initially
    val releaseFile = File(tempJdkDir, "release")
    assertThat(releaseFile.exists()).isFalse()

    lintRunner.prepareLintArguments(tempJdkDir, JAVA_VERSION, buildSdkVersion, kotlinVersion)

    // Verify release file was created
    assertThat(releaseFile.exists()).isTrue()
    val releaseContent = releaseFile.readText()
    assertThat(releaseContent).contains("MODULES=")
  }

  @Test
  fun testPrepareLintArguments_withInvalidJdkHome_throwsException() {
    val nonExistentJdk = File(tempFolder.root, "nonexistent_jdk")
    val reportFile = File(tempFolder.root, "report.xml")
    val projectFile = File(tempFolder.root, "project.xml")
    val lintRunner = AndroidLintRunner(reportFile, projectFile)

    val exception = assertThrows<IllegalArgumentException> {
      lintRunner.prepareLintArguments(
        nonExistentJdk, JAVA_VERSION, buildSdkVersion, kotlinVersion
      )
    }

    assertThat(exception.message).contains("JDK home path does not exist or is not a directory")
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
  }

  private fun setupAndroidProjectWithUnusedResources() {
    testBazelWorkspace.initEmptyWorkspace()
    createProjectStructure()
    createBasicManifest()
    createUnusedStringResources()
  }

  private fun setupAndroidProjectWithDuplicateStrings() {
    testBazelWorkspace.initEmptyWorkspace()
    createProjectStructure()
    createBasicManifest()
    createDuplicateStringResources()
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
              android:id="@+i/unused_text_view"
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
        <module name="app" library="false" android="true" compile-sdk-version="$buildSdkVersion"
                javaLanguage="$JAVA_VERSION" kotlinLanguage="$kotlinVersion">
          <manifest file="$rootPath/app/src/main/AndroidManifest.xml"/>
          <src dir="$srcPath"/>
          <resource dir="$rootPath/app/src/main/res"/>
        </module>
      </project>
    """.trimIndent()
  }
}
