package org.oppia.android.scripts.lint

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineScope
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.common.AndroidBuildSdkProperties
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import org.oppia.android.scripts.common.testing.FakeCommandExecutor
import org.oppia.android.scripts.proto.AndroidLintExemption
import org.oppia.android.scripts.proto.AndroidLintExemptions
import org.oppia.android.scripts.proto.LintIssueId
import org.oppia.android.scripts.testing.TestBazelWorkspace
import org.oppia.android.testing.assertThrows
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

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
  private val fakeCommandExecutor by lazy { FakeCommandExecutor() }
  private lateinit var androidLintAnalyzerWithFakeExecutor: AndroidLintAnalyzer
  private lateinit var workingDirectory: File
  private lateinit var reportfile: File
  private val pathToProtoBinary = "scripts/assets/android_lint_exemptions.pb"
  private lateinit var bazelBinFolder: File
  private lateinit var projectDescriptionFile: File
  private lateinit var kotlinVersion: String
  private var fakeTime = 0L
  private val fakeTimeProvider = { fakeTime }
  private val scriptBgDispatcher by lazy { ScriptBackgroundCoroutineDispatcher() }
  private lateinit var elapsedTimeDisplayer: ElapsedTimeDisplayer

  companion object {
    private const val JAVA_VERSION = "11.0.6"
    private const val MIN_SDK_VERSION = "21"
    private const val TARGET_SDK_VERSION = "34"
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
    tempFolder.newFolder("scripts", "assets")
    tempFolder.newFile(pathToProtoBinary)
    val sdkProperties = AndroidBuildSdkProperties()
    buildSdkVersion = sdkProperties.buildSdkVersion.toString()
    kotlinVersion = sdkProperties.kotlinCompilerVersion.substringBeforeLast('.')
    workingDirectory = tempFolder.newFolder("lint_analysis")
    reportfile = File(workingDirectory, "lint-report.xml")
    bazelBinFolder = tempFolder.newFolder("bazel-bin")
    androidLintAnalyzerWithFakeExecutor = AndroidLintAnalyzer(
      commandExecutor = fakeCommandExecutor,
      workingDirectory = workingDirectory,
      exemptionProtoPath = "${tempFolder.root}/$pathToProtoBinary",
      repoRoot = tempFolder.root,
    )
    projectDescriptionFile = File(workingDirectory, "lint-project-description.xml")
    fakeTime = 0L
    elapsedTimeDisplayer = ElapsedTimeDisplayer(
      CoroutineScope(scriptBgDispatcher),
      fakeTimeProvider
    )
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
  fun testStopTimer_afterRunning_returnsCorrectElapsedTime() {
    elapsedTimeDisplayer.start()
    fakeTime += 2000L // Simulate 2 seconds passing

    val elapsedTime = elapsedTimeDisplayer.stop()

    assertThat(elapsedTime).isEqualTo(2000L)
  }

  @Test
  fun testStopTimer_withNoTimeAdvance_returnsZero() {
    elapsedTimeDisplayer.start()

    val elapsedTime = elapsedTimeDisplayer.stop()

    assertThat(elapsedTime).isEqualTo(0L)
  }

  @Test
  fun testStartTimer_calledMultipleTimes_onlyStartsOnce() {
    elapsedTimeDisplayer.start()
    elapsedTimeDisplayer.start() // Should be ignored

    fakeTime += 1000L
    val elapsedTime = elapsedTimeDisplayer.stop()

    assertThat(elapsedTime).isEqualTo(1000L)
  }

  @Test
  fun testStopTimer_withVariousElapsedTimes_returnsCorrectDuration() {
    elapsedTimeDisplayer.start()

    // Test 1 second
    fakeTime += 1000L
    var elapsed = elapsedTimeDisplayer.stop()
    assertThat(elapsed).isEqualTo(1000L)

    // Test 1 minute 1 second
    elapsedTimeDisplayer.start()
    fakeTime += 61000L
    elapsed = elapsedTimeDisplayer.stop()
    assertThat(elapsed).isEqualTo(61000L)

    // Test 1 hour 1 minute 1 second
    elapsedTimeDisplayer.start()
    fakeTime += 3661000L
    elapsed = elapsedTimeDisplayer.stop()
    assertThat(elapsed).isEqualTo(3661000L)
  }

  @Test
  fun testAndroidLintAnalyzer_validRootPath_generatesReports() {
    setupProjectStructure()
    androidLintAnalyzerWithFakeExecutor.runAnalysis()

    val output = outputStream.toString()
    assertThat(output).contains("${GREEN}ANDROID LINT CHECK ${BOLD}PASSED$RESET")
    assertThat(output).contains("Total Issues: 0")
    assertThat(reportfile.exists()).isTrue()

    val projectDescription = File(workingDirectory, "lint-project-description.xml")
    assertThat(projectDescription.exists()).isTrue()
  }

  @Test
  fun testAndroidLintAnalyzer_validRootPath_generatesFilesInWorkingDirectory() {
    setupProjectStructure()
    androidLintAnalyzerWithFakeExecutor.runAnalysis()

    val output = outputStream.toString()
    assertThat(output).contains("${GREEN}ANDROID LINT CHECK ${BOLD}PASSED$RESET")
    val report = File(workingDirectory, "lint-report.xml")
    assertThat(report.exists()).isTrue()

    val projectDescription = File(workingDirectory, "lint-project-description.xml")
    assertThat(projectDescription.exists()).isTrue()
    val extractedAars = File(workingDirectory, "extracted-aars")
    val extractedAarFile = File("$extractedAars/app", "test-library-1.0.0")
    assertThat(extractedAarFile.exists()).isTrue()
    val lintCacheDirectory = File(workingDirectory, "lint-cache-directory")
    assertThat(lintCacheDirectory.exists()).isTrue()
  }

  @Test
  fun testAndroidLintAnalyzer_validRootPath_generatesModelDirectory() {
    setupProjectStructure()

    androidLintAnalyzerWithFakeExecutor.runAnalysis()

    val output = outputStream.toString()
    assertThat(output).contains("${GREEN}ANDROID LINT CHECK ${BOLD}PASSED$RESET")
    val modelDirectory = File(workingDirectory, "models-directory/app")
    assertThat(modelDirectory.exists()).isTrue()

    val expectedFiles = listOf(
      File(modelDirectory, "main.xml"),
      File(modelDirectory, "module.xml"),
      File(modelDirectory, "main-mainArtifact-dependencies.xml"),
      File(modelDirectory, "main-mainArtifact-libraries.xml")
    )

    expectedFiles.forEach { file ->
      assertThat(file.exists()).isTrue()
    }
  }

  @Test
  fun testPrepareLintArguments_ensuresAllRequiredArgumentsArePresent() {
    val reportFile = File(workingDirectory, "report.xml")
    val projectFile = File(workingDirectory, "project.xml")
    val lintRunner = AndroidLintRunner(
      reportFile,
      projectFile,
      tempFolder.root,
      "${tempFolder.root}/$pathToProtoBinary"
    )

    val suppressLintIssues = setOf(
      "MissingTranslation",
      "GradleOverrides",
      "SyntheticAccessor",
      "DuplicateStrings",
    )

    val result = lintRunner.prepareLintArguments(
      jdkHome,
      JAVA_VERSION,
      buildSdkVersion,
      kotlinVersion,
      suppressLintIssues
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
      "--disable", suppressLintIssues.joinToString(","),
      "--project", projectFile.absolutePath,
      "--xml", reportFile.absolutePath
    )

    assertThat(result.toList()).containsExactlyElementsIn(expectedArguments)
  }

  @Test
  fun testPrepareLintArguments_withCustomBuildSdkVersion_includesCorrectVersion() {
    val reportFile = File(workingDirectory, "report.xml")
    val projectFile = File(workingDirectory, "project.xml")
    val lintRunner = AndroidLintRunner(
      reportFile,
      projectFile,
      tempFolder.root,
      "${tempFolder.root}/$pathToProtoBinary"
    )
    val customBuildSdk = TARGET_SDK_VERSION

    val result = lintRunner.prepareLintArguments(
      jdkHome,
      JAVA_VERSION,
      customBuildSdk,
      kotlinVersion,
      emptySet()
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
    val lintRunner = AndroidLintRunner(
      reportFile,
      projectFile,
      tempFolder.root,
      "${tempFolder.root}/$pathToProtoBinary"
    )

    lintRunner.prepareLintArguments(
      tempJdkDir,
      JAVA_VERSION,
      buildSdkVersion,
      kotlinVersion,
      emptySet()
    )

    assertThat(releaseFile.readText()).isEqualTo(originalContent)
  }

  @Test
  fun testPrepareLintArguments_generatesValidModulesString() {
    val tempJdkDir = File(tempFolder.root, "temp_jdk_modules")
    tempJdkDir.mkdirs()

    val reportFile = File(tempFolder.root, "report.xml")
    val projectFile = File(tempFolder.root, "project.xml")

    val lintRunner = AndroidLintRunner(
      reportFile,
      projectFile,
      tempFolder.root,
      "${tempFolder.root}/$pathToProtoBinary"
    )

    lintRunner.prepareLintArguments(
      tempJdkDir,
      JAVA_VERSION,
      buildSdkVersion,
      kotlinVersion,
      emptySet()
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
    setupProjectStructure()
    val lintRunner = createLintRunner()
    lintRunner.runLint(
      lintRunner.prepareLintArguments(
        jdkHome,
        JAVA_VERSION,
        buildSdkVersion,
        kotlinVersion,
        emptySet()
      )
    )

    val output = outputStream.toString()
    assertThat(output).contains("${GREEN}ANDROID LINT CHECK ${BOLD}PASSED$RESET")
  }

  @Test
  fun testRunLint_whenExitCodeIs1_shouldFailScript() {
    setupProjectWithMissingTranslation()
    val lintRunner = createLintRunner()
    val exception = assertThrows<IllegalStateException> {
      lintRunner.runLint(
        lintRunner.prepareLintArguments(
          jdkHome,
          JAVA_VERSION,
          buildSdkVersion,
          kotlinVersion,
          emptySet()
        )
      )
    }

    val reportFile = File(workingDirectory, "lint-report.xml")
    assertThat(reportFile.exists()).isTrue()
    assertThat(exception.message).contains("${RED}ANDROID LINT CHECK ${BOLD}FAILED$RESET")
    val output = outputStream.toString()
    assertThat(output).contains("MissingTranslation")
  }

  @Test
  fun testRunLint_withExitCode2_throwsException() {
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
    val outputDirectory = File(workingDirectory, "reports")
    outputDirectory.mkdirs()

    val reportPath = File(outputDirectory, "lint-report.xml")
    reportPath.createNewFile()
    reportPath.writeText("existing content")

    val disabledWrite = outputDirectory.setWritable(false)
    assertThat(disabledWrite).isTrue()

    val projectPath = createProjectDescriptionFile()
    val lintRunner = AndroidLintRunner(
      reportPath,
      projectPath,
      tempFolder.root,
      "${tempFolder.root}/$pathToProtoBinary"
    )

    val exception = assertThrows<IllegalStateException> {
      lintRunner.runLint(
        lintRunner.prepareLintArguments(
          jdkHome,
          JAVA_VERSION,
          buildSdkVersion,
          kotlinVersion,
          emptySet()
        )
      )
    }

    assertThat(exception.message).contains("Lint analysis failed with exit code 3")
    assertThat(exception.message).contains("Cannot overwrite existing file")

    outputDirectory.setWritable(true)
  }

  @Test
  fun testRunLint_withExitCode4_throwsException() {
    val reportPath = File(workingDirectory, "lint-report.xml")
    val projectPath = File(workingDirectory, "lint-project-description.xml")
    val lintRunner = AndroidLintRunner(
      reportPath,
      projectPath,
      tempFolder.root,
      "${tempFolder.root}/$pathToProtoBinary"
    )

    // Won't happen in actual usage.
    val exception = assertThrows<IllegalStateException> {
      lintRunner.runLint(arrayOf("--help"))
    }
    assertThat(exception.message).contains("Lint analysis failed with exit code 4")
    assertThat(exception.message).contains("Help command invoked.")
  }

  @Test
  fun testRunLint_withExitCode5_throwsException() {
    val reportPath = File(workingDirectory, "lint-report.xml")
    val projectPath = File(workingDirectory, "lint-project-description.xml")
    val lintRunner = AndroidLintRunner(
      reportPath,
      projectPath,
      tempFolder.root,
      "${tempFolder.root}/$pathToProtoBinary"
    )

    val exception = assertThrows<IllegalStateException> {
      lintRunner.runLint(
        lintRunner.prepareLintArguments(
          jdkHome,
          JAVA_VERSION,
          buildSdkVersion,
          kotlinVersion,
          emptySet()
        )
      )
    }
    assertThat(exception.message).contains("Lint analysis failed with exit code 5")
    assertThat(exception.message).contains("Invalid command-line argument")
  }

  @Test
  fun testRunLint_withProjectDescription_withNonExistentFilePath_throwsInternalIssue() {
    setupProjectStructure()

    val projectDescriptionFile = createProjectDescriptionFileWithInvalidPath()
    val reportFile = File(workingDirectory, "lint-report.xml")
    val lintRunner = AndroidLintRunner(
      reportFile,
      projectDescriptionFile,
      tempFolder.root,
      "${tempFolder.root}/$pathToProtoBinary"
    )

    val exception = assertThrows<IllegalStateException> {
      lintRunner.runLint(
        lintRunner.prepareLintArguments(
          jdkHome,
          JAVA_VERSION,
          buildSdkVersion,
          kotlinVersion,
          emptySet()
        )
      )
    }

    assertThat(exception.message)
      .contains("${RED}ANDROID LINT CHECK ${BOLD}FAILED WITH INTERNAL LINT ISSUES$RESET")
    assertThat(reportFile.exists()).isTrue()
    val report = reportFile.readText()
    assertThat(report).contains("LintError")
    assertThat(report).contains("app/src/main/nonexistent_java does not exist")
    assertThat(report).contains("line=\"8\"")
  }

  @Test
  fun testRunLint_withInvalidFlag_throwsException() {
    val reportFile = File(workingDirectory, "report.xml")
    val projectFile = File(workingDirectory, "project.xml")
    val lintRunner = AndroidLintRunner(
      reportFile,
      projectFile,
      tempFolder.root,
      "${tempFolder.root}/$pathToProtoBinary"
    )

    val exception = assertThrows<IllegalStateException> {
      lintRunner.runLint(arrayOf("--InvalidFlag"))
    }

    assertThat(exception.message).contains("Lint analysis failed with exit code 5")
    assertThat(exception.message).contains("Invalid command-line argument")
  }

  @Test
  fun testPrepareLintArguments_withJdkEnvironmentSetup_createsReleaseFile() {
    val tempJdkDir = File(tempFolder.root, "temp_jdk")
    tempJdkDir.mkdirs()

    val reportFile = File(tempFolder.root, "report.xml")
    val projectFile = File(tempFolder.root, "project.xml")
    val lintRunner = AndroidLintRunner(
      reportFile,
      projectFile,
      tempFolder.root,
      "${tempFolder.root}/$pathToProtoBinary"
    )

    // Ensure no release file exists initially
    val releaseFile = File(tempJdkDir, "release")
    assertThat(releaseFile.exists()).isFalse()

    lintRunner.prepareLintArguments(
      tempJdkDir,
      JAVA_VERSION,
      buildSdkVersion,
      kotlinVersion,
      emptySet()
    )

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
    val lintRunner = AndroidLintRunner(
      reportFile,
      projectFile,
      tempFolder.root,
      "${tempFolder.root}/$pathToProtoBinary"
    )

    val exception = assertThrows<IllegalArgumentException> {
      lintRunner.prepareLintArguments(
        nonExistentJdk,
        JAVA_VERSION,
        buildSdkVersion,
        kotlinVersion,
        emptySet()
      )
    }

    assertThat(exception.message).contains("JDK home path does not exist or is not a directory")
  }

  @Test
  fun testAndroidLintAnalyzer_withDuplicateStringResources_issueIsSuppressed() {
    setupProjectWithDuplicateStringIssue()

    androidLintAnalyzerWithFakeExecutor.runAnalysis()

    val output = outputStream.toString()
    assertThat(output).contains("${GREEN}ANDROID LINT CHECK ${BOLD}PASSED$RESET")
    assertThat(output).doesNotContain("DuplicateStrings")
  }

  @Test
  fun testAndroidLintAnalyzer_withUselessParent_detectsIssue() {
    setupProjectWithUselessParent()

    val exception = assertThrows<IllegalStateException> {
      androidLintAnalyzerWithFakeExecutor.runAnalysis()
    }
    assertThat(exception.message)
      .contains("${RED}ANDROID LINT CHECK ${BOLD}FAILED$RESET")

    val output = outputStream.toString()
    assertThat(output).contains("UselessParent")
    assertThat(output)
      .contains("<RelativeLayout")
    assertThat(output).contains("Line: 5")
    assertThat(output)
      .contains("This `RelativeLayout` layout or its `FrameLayout` parent is unnecessary")
    val projectDescriptionContent = projectDescriptionFile.readText()
    assertThat(projectDescriptionContent)
      .contains("app/src/main/res")
  }

  @Test
  fun testAndroidLintAnalyzer_withUselessLeaf_detectsIssue() {
    setupProjectWithUselessLeaf()

    val exception = assertThrows<IllegalStateException> {
      androidLintAnalyzerWithFakeExecutor.runAnalysis()
    }
    assertThat(exception.message)
      .contains("${RED}ANDROID LINT CHECK ${BOLD}FAILED$RESET")

    val output = outputStream.toString()
    assertThat(output).contains("UselessLeaf")
    assertThat(output)
      .contains("<FrameLayout")
    assertThat(output).contains("Line: 5")
    assertThat(output)
      .contains(
        "This `FrameLayout` view is unnecessary " +
          "(no children, no `background`, no `id`, no `style`)"
      )
    val projectDescriptionContent = projectDescriptionFile.readText()
    assertThat(projectDescriptionContent)
      .contains("app/src/main/res")
  }

  @Test
  fun testAndroidLintAnalyzer_withRtlHardCoded_detectsIssue() {
    setupProjectWithRtlHardCoded()

    val exception = assertThrows<IllegalArgumentException> {
      androidLintAnalyzerWithFakeExecutor.runAnalysis()
    }
    val output = reportfile.readText()
    assertThat(output).contains("RtlHardcoded")
    assertThat(output)
      .contains("android:layout_alignParentRight=&quot;true&quot; />")
    assertThat(output).contains("line=\"8\"")
    assertThat(output)
      .contains("Using left/right instead of start/end attributes")

    assertThat(exception.message)
      .contains("Unknown lint issue ID 'RtlHardcoded' found during analysis.")

    val projectDescriptionContent = projectDescriptionFile.readText()
    assertThat(projectDescriptionContent)
      .contains("app/src/main/res")
  }

  @Test
  fun testAndroidLintAnalyzer_withRtlSymmetry_detectsIssue() {
    setupProjectWithRtlSymmetry()
    val exception = assertThrows<IllegalStateException> {
      androidLintAnalyzerWithFakeExecutor.runAnalysis()
    }

    val output = outputStream.toString()
    assertThat(output).contains("RtlSymmetry")
    assertThat(output)
      .contains("android:paddingEnd=\"120dip\"")
    assertThat(output).contains("Line: 29")
    assertThat(output)
      .contains(
        "When you define `paddingEnd` you should probably also define " +
          "`paddingStart` for right-to-left symmetry"
      )
    val projectDescriptionContent = projectDescriptionFile.readText()
    assertThat(projectDescriptionContent)
      .contains("app/src/main/res")
    assertThat(projectDescriptionContent)
      .contains("app/src/main/AndroidManifest.xml")

    assertThat(exception.message)
      .isEqualTo("${RED}ANDROID LINT CHECK ${BOLD}FAILED$RESET")
  }

  @Test
  fun testAndroidLintAnalyzer_withNewApi_detectsIssue() {
    setupProjectWithNewApi()
    val exception = assertThrows<IllegalStateException> {
      androidLintAnalyzerWithFakeExecutor.runAnalysis()
    }

    val output = outputStream.toString()
    assertThat(output).contains("NewApi")
    assertThat(output)
      .contains("val network = cm.activeNetwork")
    assertThat(output)
      .doesNotContain("val network2 = cm.activeNetwork")
    assertThat(output).contains("Line: 11")
    assertThat(output)
      .contains(
        "Call requires API level 23 (current min is 21): " +
          "`android.net.ConnectivityManager#getActiveNetwork`"
      )
    assertThat(exception.message).contains("${RED}ANDROID LINT CHECK ${BOLD}FAILED$RESET")

    val projectDescriptionContent = projectDescriptionFile.readText()
    assertThat(projectDescriptionContent)
      .contains("app/src/main/java/org/oppia/android/app/NewApiUsage.kt")
    assertThat(projectDescriptionContent)
      .contains("app/src/main/AndroidManifest.xml")
  }

  @Test
  fun testAndroidLintAnalyzer_withInlinedApi_detectsIssue() {
    setupProjectWithInlinedApi()
    val exception = assertThrows<IllegalArgumentException> {
      androidLintAnalyzerWithFakeExecutor.runAnalysis()
    }

    val output = reportfile.readText()
    assertThat(output).contains("InlinedApi")
    assertThat(output)
      .contains("val format: String = MediaFormat.MIMETYPE_AUDIO_AC4")
    assertThat(output).contains("line=\"14\"")
    assertThat(output)
      .contains(
        "Field requires API level 29 (current min is 21):" +
          " `android.media.MediaFormat#MIMETYPE_AUDIO_AC4`"
      )

    assertThat(exception.message)
      .contains("Unknown lint issue ID 'InlinedApi' found during analysis.")

    val projectDescriptionContent = projectDescriptionFile.readText()
    assertThat(projectDescriptionContent)
      .contains("app/src/main/java/org/oppia/android/app/InlinedApiUsage.kt")
    assertThat(projectDescriptionContent)
      .contains("app/src/main/AndroidManifest.xml")
  }

  @Test
  fun testAndroidLintAnalyzer_withSyntheticAccessor_issueIsSuppressed() {
    setupProjectWithSyntheticAccessor()

    androidLintAnalyzerWithFakeExecutor.runAnalysis()

    val output = outputStream.toString()
    assertThat(output).contains("${GREEN}ANDROID LINT CHECK ${BOLD}PASSED$RESET")
    assertThat(output).doesNotContain("SyntheticAccessor")
  }

  @Test
  fun testAndroidLintAnalyzer_withLabelFor_detectsIssue() {
    setupProjectWithLabelFor()

    val exception = assertThrows<IllegalStateException> {
      androidLintAnalyzerWithFakeExecutor.runAnalysis()
    }
    assertThat(exception.message)
      .contains("${RED}ANDROID LINT CHECK ${BOLD}FAILED$RESET")

    val output = outputStream.toString()
    assertThat(output).contains("LabelFor")
    assertThat(output)
      .contains("android:hint=\"\"")
    assertThat(output).contains("Line: 11")
    assertThat(output)
      .contains(
        "Editable text fields should provide an `android:hint`"
      )
    val projectDescriptionContent = projectDescriptionFile.readText()
    assertThat(projectDescriptionContent)
      .contains("app/src/main/res")
  }

  @Test
  fun testAndroidLintAnalyzer_withUnusedAttribute_detectsIssue() {
    setupProjectWithUnusedAttribute()

    val exception = assertThrows<IllegalStateException> {
      androidLintAnalyzerWithFakeExecutor.runAnalysis()
    }
    assertThat(exception.message)
      .contains("${RED}ANDROID LINT CHECK ${BOLD}FAILED$RESET")

    val output = outputStream.toString()
    assertThat(output).contains("UnusedAttribute")
    assertThat(output)
      .contains("android:theme=\"@android:style/Theme.Holo\" />")
    assertThat(output).contains("Line: 11")
    assertThat(output)
      .contains(
        "Attribute `android:theme` is only used by `<include>` tags "
      )
    val projectDescriptionContent = projectDescriptionFile.readText()
    assertThat(projectDescriptionContent)
      .contains("app/src/main/res")
    assertThat(projectDescriptionContent)
      .contains("app/src/main/AndroidManifest.xml")
  }

  @Test
  fun testAndroidLintAnalyzer_withNotifyDataSetChanged_detectsIssue() {
    setupProjectWithNotifyDataSetChanged()

    val exception = assertThrows<IllegalStateException> {
      androidLintAnalyzerWithFakeExecutor.runAnalysis()
    }
    assertThat(exception.message)
      .contains("${RED}ANDROID LINT CHECK ${BOLD}FAILED$RESET")
    val output = outputStream.toString()
    assertThat(output).contains("NotifyDataSetChanged")
    assertThat(output)
      .contains("notifyDataSetChanged()")
    assertThat(output).contains("Line: 13")
    assertThat(output)
      .contains(
        "It will always be more efficient to use more specific change events if you can. " +
          "Rely on `notifyDataSetChanged` as a last resort."
      )
    val projectDescriptionContent = projectDescriptionFile.readText()
    assertThat(projectDescriptionContent)
      .contains("app/src/main/java/androidx/recyclerview/widget/RecyclerView.kt")
    assertThat(projectDescriptionContent)
      .contains("app/src/main/java/org/oppia/android/app/RecyclerViewUsage.kt")
  }

  @Test
  fun testAndroidLintAnalyzer_withUseCompoundDrawables_detectsIssue() {
    setupProjectWithUseCompoundDrawables()

    val exception = assertThrows<IllegalStateException> {
      androidLintAnalyzerWithFakeExecutor.runAnalysis()
    }
    assertThat(exception.message)
      .contains("${RED}ANDROID LINT CHECK ${BOLD}FAILED$RESET")

    val output = reportfile.readText()
    assertThat(output).contains("UseCompoundDrawables")
    assertThat(output)
      .contains("&lt;LinearLayout")
    assertThat(output).contains("line=\"1\"")
    assertThat(output)
      .contains(
        "Node can be replaced by a `TextView` with compound drawables"
      )
    val projectDescriptionContent = projectDescriptionFile.readText()
    assertThat(projectDescriptionContent)
      .contains("app/src/main/res")
  }

  private fun setupProjectWithUseCompoundDrawables() {
    setupProjectStructure()
    createFileWithContent(
      "app/src/main/res/layout/compound.xml",
      """
      <LinearLayout
          xmlns:android="http://schemas.android.com/apk/res/android"
          android:layout_width="match_parent"
          android:layout_height="match_parent">

          <ImageView
              android:layout_width="wrap_content"
              android:layout_height="wrap_content"
              android:contentDescription="@string/app_name" />

          <TextView
              android:layout_width="wrap_content"
              android:layout_height="wrap_content" />

      </LinearLayout>
    """
    )
  }

  private fun setupProjectWithNotifyDataSetChanged() {
    setupProjectStructure()
    createFileWithContent(
      "app/src/main/java/androidx/recyclerview/widget/RecyclerView.kt",
      """
    package androidx.recyclerview.widget

    import android.content.Context
    import android.util.AttributeSet
    import android.view.View

    open class RecyclerView(context: Context, attrs: AttributeSet) : View(context, attrs) {
      open class ViewHolder(val itemView: View)

      abstract class Adapter<VH : ViewHolder> {
        abstract fun onBindViewHolder(holder: VH, position: Int)
        open fun notifyDataSetChanged() {} 
      }
    }
  """
    )

    createFileWithContent(
      "app/src/main/java/org/oppia/android/app/RecyclerViewUsage.kt",
      """
    package org.oppia.android.app

    import android.view.View
    import android.widget.TextView
    import androidx.recyclerview.widget.RecyclerView

    class RecyclerViewUsage {

      class TestAdapter(private val dataSet: Array<String>) :
        RecyclerView.Adapter<TestAdapter.TextViewHolder>() {

        init {
          notifyDataSetChanged() 
        }

        class TextViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun onBindViewHolder(holder: TextViewHolder, position: Int) {}
      }
    }
  """
    )
  }

  private fun setupProjectWithUnusedAttribute() {
    setupProjectStructure()
    createFileWithContent(
      "app/src/main/res/layout/linear.xml",
      """
    <?xml version="1.0" encoding="utf-8"?>
    <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical">

        <include
            layout="@layout/included"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:theme="@android:style/Theme.Holo" />
    </LinearLayout>
  """
    )
  }

  private fun setupProjectWithLabelFor() {
    setupProjectStructure()
    createFileWithContent(
      "app/src/main/res/layout/labelfororhint_empty_hint.xml",
      """
                <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
                              android:layout_width="match_parent"
                              android:layout_height="match_parent"
                              android:orientation="vertical">

                    <EditText
                            android:id="@+id/editText1"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:ems="10"
                            android:hint=""
                            android:inputType="textPersonName">
                        <requestFocus/>
                    </EditText>

                    <AutoCompleteTextView
                            android:id="@+id/autoCompleteTextView1"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:ems="10"
                            android:hint=""
                            android:text="@string/app_name"/>

                    <MultiAutoCompleteTextView
                            android:id="@+id/multiAutoCompleteTextView1"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:ems="10"
                            android:hint=""
                            android:text="@string/app_name" />
                </LinearLayout>
                """
    )
  }

  private fun setupProjectWithSyntheticAccessor() {
    setupProjectStructure()
    createFileWithContent(
      "app/src/main/java/org/oppia/android/app/AccessTest.kt",
      """
    package org.oppia.android.app

    class AccessTest2 private constructor() {
      private val secret = 42
      private fun hiddenMethod() {}

      inner class Inner {
        fun trigger() {
          AccessTest2()       
          val x = secret       
          hiddenMethod()       
        }
      }
    }
  """
    )
  }

  private fun setupProjectWithInlinedApi() {
    setupProjectStructure()
    createFileWithContent(
      "app/src/main/java/org/oppia/android/app/InlinedApiUsage.kt",
      """
    package org.oppia.android.app

    import android.media.MediaFormat

    fun encode(format: String) {
      // Dummy placeholder function
    }

    fun test() {
        // This constant will be copied in by value, which means
        // it will run without crashing on older devices. However,
        // depending on what we *do* with the value, the code may
        // not work correctly.
        val format: String = MediaFormat.MIMETYPE_AUDIO_AC4
        encode(format) // might crash!
    }
  """
    )
  }

  private fun setupProjectWithNewApi() {
    setupProjectStructure()

    createFileWithContent(
      "app/src/main/java/org/oppia/android/app/NewApiUsage.kt",
      """
      package org.oppia.android.test

      import android.annotation.SuppressLint
      import android.content.Context
      import android.net.ConnectivityManager
      import android.os.Build
      
      @SuppressLint("MissingPermission") 
      fun test(context: Context) {
          val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
          val network = cm.activeNetwork 
          if (Build.VERSION.SDK_INT >= 23) {
              val network2 = cm.activeNetwork // OK
          }
      }
      """.trimIndent()
    )
  }

  private fun setupProjectWithRtlSymmetry() {
    setupProjectStructure()
    createFileWithContent(
      "app/src/main/res/layout/rtl_symmetry.xml",
      """
      <?xml version="1.0" encoding="utf-8"?>
      <RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
          android:layout_width="wrap_content"
          android:layout_height="wrap_content">

          <ProgressBar
              android:id="@+id/loading_progress"
              android:layout_width="wrap_content"
              android:layout_height="wrap_content"
              android:layout_alignParentStart="true"
              android:layout_alignParentTop="true"
              android:layout_marginBottom="60dip"
              android:layout_marginStart="40dip"
              android:layout_marginTop="40dip"
              android:max="10000" />

          <TextView
              android:id="@+id/text"
              android:layout_width="wrap_content"
              android:layout_height="wrap_content"
              android:layout_alignParentTop="true"
              android:layout_alignWithParentIfMissing="true"
              android:layout_marginBottom="60dip"
              android:layout_marginStart="40dip"
              android:layout_marginTop="40dip"
              android:layout_toEndOf="@id/loading_progress"
              android:ellipsize="end"
              android:maxLines="3"
              android:paddingEnd="120dip"
              android:text="@string/app_name"
              android:textAppearance="?android:attr/textAppearanceMedium" />
      </RelativeLayout>
    """
    )

    createFileWithContent(
      "app/src/main/AndroidManifest.xml",
      """
    <manifest xmlns:android="http://schemas.android.com/apk/res/android"
        package="org.oppia.android.app">

        <uses-sdk
            android:minSdkVersion="$MIN_SDK_VERSION"
            android:targetSdkVersion="$TARGET_SDK_VERSION" />

        <application
            android:supportsRtl="true"
            android:allowBackup="true"
            android:label="RTL Test App"
            android:icon="@mipmap/ic_launcher">
        </application>
    </manifest>
    """
    )
  }

  private fun setupProjectWithRtlHardCoded() {
    setupProjectStructure()
    createFileWithContent(
      "app/src/main/res/layout/rtl_hardcoded.xml",
      """
        <RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
            android:layout_width="match_parent"
            android:layout_height="match_parent">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_alignParentRight="true" />
        </RelativeLayout>
        """
    )
  }

  private fun setupProjectWithUselessParent() {
    setupProjectStructure()
    createFileWithContent(
      "app/src/main/res/layout/useless5.xml",
      """
                <FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
                             android:layout_width="match_parent"
                             android:layout_height="wrap_content">

                    <RelativeLayout
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:paddingBottom="16dp"
                            android:paddingLeft="16dp"
                            android:paddingRight="16dp"
                            android:paddingTop="16dp">

                        <TextView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"/>
                    </RelativeLayout>
                </FrameLayout>
                """
    )
  }

  private fun setupProjectWithUselessLeaf() {
    setupProjectStructure()
    createFileWithContent(
      "app/src/main/res/layout/useless_leaf.xml",
      """
        <merge xmlns:android="http://schemas.android.com/apk/res/android"
            android:layout_width="match_parent"
            android:layout_height="match_parent">

            <FrameLayout
                android:layout_width="match_parent"
                android:layout_height="match_parent" />
        </merge>
        """
    )
  }

  private fun setupProjectWithMissingTranslation() {
    setupProjectStructure()

    createFileWithContent(
      "app/src/main/res/values/strings.xml",
      """
      <?xml version="1.0" encoding="utf-8"?>
      <resources>
          <string name="hello">Hello</string>
          <string name="goodbye">Goodbye</string>
      </resources>
      """.trimIndent()
    )

    createFileWithContent(
      "app/src/main/res/values-es/strings.xml",
      """
      <?xml version="1.0" encoding="utf-8"?>
      <resources>
          <string name="goodbye">Adiós</string>
      </resources>
      """.trimIndent()
    )
  }

  private fun setupProjectWithDuplicateStringIssue() {
    setupProjectStructure()
    createFileWithContent(
      "app/src/main/res/values/strings.xml",
      """
      <?xml version="1.0" encoding="utf-8"?>
      <resources>
          <string name="app_name">Oppia</string>
          <string name="duplicate_value">Same text</string>
          <string name="another_duplicate">Same text</string>
      </resources>
      """
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

  private fun setupProjectStructure() {
    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.setUpWorkspaceForRulesJvmExternal(listOf("junit:junit:4.12"))

    createModule("app")
    createModule("utility")
    createModule("domain")
    createModule("testing")
    createModule("data")

    setupFakeCommandExecutor()
  }

  private fun createModule(
    moduleName: String,
  ) {
    createModuleDirectories(moduleName)
    createModuleFiles(moduleName)
  }

  private fun createModuleDirectories(moduleName: String) {
    val directories = listOf(
      moduleName,
      "$moduleName/src",
      "$moduleName/src/main",
      "$moduleName/src/main/java",
      "$moduleName/src/main/res",
      "$moduleName/src/main/res/values",
      "$moduleName/src/test",
      "$moduleName/src/test/java"
    )

    directories.forEach { dir ->
      tempFolder.newFolder(*dir.split("/").toTypedArray())
    }
  }

  private fun createModuleFiles(moduleName: String) {
    createManifestFile(moduleName)
    createTestManifestFile(moduleName)
    createSourceFile(moduleName)
    createTestFile(moduleName)
    createResourceFile(moduleName)
  }

  private fun createManifestFile(moduleName: String) {
    val manifest = tempFolder.newFile("$moduleName/src/main/AndroidManifest.xml")
    manifest.writeText(
      """
    <?xml version="1.0" encoding="utf-8"?>
    <manifest xmlns:android="http://schemas.android.com/apk/res/android"
      package="org.oppia.android.$moduleName"
      android:versionCode="1"
      android:versionName="1.0.0">
      <uses-sdk android:minSdkVersion="$MIN_SDK_VERSION" android:targetSdkVersion="$TARGET_SDK_VERSION" />
      <application
        android:icon="@drawable/ic_launcher"
        android:label="@string/app_name" >
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
    exemptRedundantIssue(
      LintIssueId.GRADLE_OVERRIDES,
      "$moduleName/src/main/AndroidManifest.xml"
    )
  }

  private fun createTestManifestFile(moduleName: String) {
    val manifest = tempFolder.newFile("$moduleName/src/test/AndroidManifest.xml")
    manifest.writeText(
      """
    <?xml version="1.0" encoding="utf-8"?>
    <manifest xmlns:android="http://schemas.android.com/apk/res/android"
      package="org.oppia.android.$moduleName">
      <uses-sdk android:minSdkVersion="$MIN_SDK_VERSION" android:targetSdkVersion="$TARGET_SDK_VERSION" />
    </manifest>
      """.trimIndent()
    )
  }

  private fun createSourceFile(moduleName: String) {
    val className = moduleName.replaceFirstChar { it.uppercase() }
    val sourceDir = tempFolder.newFolder(
      moduleName, "src", "main", "java", "org", "oppia", "android", moduleName
    )
    val sourceFile = File(sourceDir, "${className}Class.kt")
    sourceFile.writeText(
      """
    package org.oppia.android.$moduleName
    
    class ${className}Class {
        fun doSomething(): String = "Hello from $moduleName"
    }
      """.trimIndent()
    )
  }

  private fun createTestFile(moduleName: String) {
    val className = moduleName.replaceFirstChar { it.uppercase() }
    val testDir = tempFolder.newFolder(
      moduleName, "src", "test", "java", "org", "oppia", "android", moduleName
    )
    val testFile = File(testDir, "${className}ClassTest.kt")
    testFile.writeText(
      """
    package org.oppia.android.$moduleName
    
    import org.junit.Test
    import org.junit.Assert.assertEquals
    
    class ${className}ClassTest {
        @Test
        fun testDoSomething() {
            val instance = ${className}Class()
            assertEquals("Hello from $moduleName", instance.doSomething())
        }
    }
      """.trimIndent()
    )
  }

  private fun createResourceFile(moduleName: String) {
    val resourceFile = tempFolder.newFile("$moduleName/src/main/res/values/strings.xml")
    resourceFile.writeText(
      """
      <?xml version="1.0" encoding="utf-8"?>
      <resources>
          <string name="${moduleName}_name">$moduleName Module</string>
      </resources>
      """.trimIndent()
    )
    exemptRedundantIssue(
      LintIssueId.UNUSED_RESOURCES,
      "$moduleName/src/main/res/values/strings.xml"
    )
  }

  private fun createFileWithContent(relativePath: String, content: String): File {
    val pathParts = relativePath.split("/")
    val directoryParts = pathParts.dropLast(1)

    if (directoryParts.isNotEmpty()) {
      val parentDir = File(tempFolder.root, directoryParts.joinToString("/"))
      if (!parentDir.exists()) {
        parentDir.mkdirs()
      }
    }

    val file = File(tempFolder.root, relativePath)
    file.writeText(content.trimIndent())
    return file
  }

  private fun createLintRunner(): AndroidLintRunner {
    val reportFile = File(workingDirectory, "lint-report.xml")
    val projectDescriptionFile = createProjectDescriptionFile()

    return AndroidLintRunner(
      reportFile = reportFile,
      projectDescriptionFile = projectDescriptionFile,
      repoRoot = tempFolder.root,
      exemptionProtoPath = "${tempFolder.root}/$pathToProtoBinary"
    )
  }

  /** Exempt redundant issues related to test setup. */
  private fun exemptRedundantIssue(
    issueId: LintIssueId,
    exemptedPath: String
  ) {
    val exemptionFile = File("${tempFolder.root}/$pathToProtoBinary")

    val builder = if (exemptionFile.exists()) {
      AndroidLintExemptions.parseFrom(exemptionFile.inputStream()).toBuilder()
    } else {
      AndroidLintExemptions.newBuilder()
    }

    builder.addAndroidLintExemption(
      AndroidLintExemption.newBuilder().apply {
        exemptedFilePath = exemptedPath
        addLintIssueId(issueId)
      }.build()
    )

    builder.build().writeTo(exemptionFile.outputStream())
  }

  private fun setupFakeCommandExecutor() {
    val aarPath = createTestAarFile("test-library", "1.0.0").absolutePath
    val jarPath = createTestJarFile("test-library", "1.0.0").absolutePath

    fakeCommandExecutor.registerHandler("bazel") { _, args, outputStream, _ ->
      when {
        args.contains("cquery") && args.contains("deps(//app:*)") -> {
          val relativeAarPath = File(aarPath).relativeTo(tempFolder.root).path
          outputStream.println(relativeAarPath)
          val relativeJarPath = File(jarPath).relativeTo(tempFolder.root).path
          outputStream.println(relativeJarPath)
          0
        }
        args.contains("cquery") && args.any { it.startsWith("deps(//") } -> {
          // Return some dependencies for other modules
          outputStream.println("external/junit/junit-4.12.jar")
          outputStream.println("external/hamcrest/hamcrest-core-1.3.jar")
          0
        }
        args.contains("info") -> {
          outputStream.println("output_base: ${tempFolder.root.absolutePath}/bazel-out")
          outputStream.println("java-home: $jdkHome")
          outputStream.println("java-runtime: OpenJDK Runtime Environment (build 11.0.16+8-post)")
          0
        }
        else -> 0
      }
    }
  }

  private fun createTestAarFile(libraryName: String, version: String): File {
    val aarFile = File(bazelBinFolder, "$libraryName-$version.aar")

    ZipOutputStream(FileOutputStream(aarFile)).use { zipOut ->
      // Add AndroidManifest.xml
      zipOut.putNextEntry(ZipEntry("AndroidManifest.xml"))
      zipOut.write(
        """
      <?xml version="1.0" encoding="utf-8"?>
      <manifest package="com.example.$libraryName" />
        """.trimIndent().toByteArray()
      )
      zipOut.closeEntry()

      // Create a valid empty JAR file in memory
      val byteStream = ByteArrayOutputStream()
      ZipOutputStream(byteStream).use {
        // optionally you can add a dummy .class here too
        it.putNextEntry(ZipEntry("META-INF/MANIFEST.MF"))
        it.write("Manifest-Version: 1.0\n".toByteArray())
        it.closeEntry()
      }
      zipOut.putNextEntry(ZipEntry("classes.jar"))
      zipOut.write(byteStream.toByteArray())
      zipOut.closeEntry()

      // Add resources
      zipOut.putNextEntry(ZipEntry("res/values/strings.xml"))
      zipOut.write(
        """
      <?xml version="1.0" encoding="utf-8"?>
      <resources>
          <string name="library_name">$libraryName</string>
      </resources>
        """.trimIndent().toByteArray()
      )
      zipOut.closeEntry()
    }

    return aarFile
  }

  private fun createTestJarFile(libraryName: String, version: String): File {
    val jarFile = File(bazelBinFolder, "$libraryName-$version.jar")

    ZipOutputStream(FileOutputStream(jarFile)).use { zipOut ->
      val classEntry = ZipEntry("com/example/$libraryName/Class.class")
      zipOut.putNextEntry(classEntry)
      zipOut.write(ByteArray(10))
      zipOut.closeEntry()
    }

    return jarFile
  }
}
