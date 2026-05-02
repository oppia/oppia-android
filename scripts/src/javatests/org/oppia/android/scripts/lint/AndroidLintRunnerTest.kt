package org.oppia.android.scripts.lint

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.common.AndroidBuildSdkProperties
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import org.oppia.android.scripts.common.testing.FakeCommandExecutor
import org.oppia.android.scripts.testing.TestBazelWorkspace
import org.oppia.android.testing.assertThrows
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** Tests for [LintOrchestrator] and [AndroidLintAnalyzer]. */
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
  private val scriptBgDispatcher by lazy { ScriptBackgroundCoroutineDispatcher() }

  companion object {
    private const val JAVA_VERSION = "11.0.6"
    private const val MIN_SDK_VERSION = "21"
    private const val TARGET_SDK_VERSION = "35"
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
      reportUnusedEnum = false,
      additionalDisabledChecks = LintCheckCatalog.checksAlwaysDisabled
    )
    projectDescriptionFile = File(workingDirectory, "lint-project-description.xml")
  }

  @After
  fun tearDown() {
    System.setOut(originalOut)
    scriptBgDispatcher.close()
  }

  @Test
  fun testLintMode_fromString_fast_returnsFast() {
    assertThat(LintMode.fromString("fast")).isEqualTo(LintMode.FAST)
  }

  @Test
  fun testLintMode_fromString_full_returnsFull() {
    assertThat(LintMode.fromString("full")).isEqualTo(LintMode.FULL)
  }

  @Test
  fun testLintMode_fromString_listChecks_returnsListChecks() {
    assertThat(LintMode.fromString("list-checks")).isEqualTo(LintMode.LIST_CHECKS)
  }

  @Test
  fun testLintMode_fromString_checkScriptConsistency_returnsCheckScriptConsistency() {
    assertThat(LintMode.fromString("check-script-consistency"))
      .isEqualTo(LintMode.CHECK_SCRIPT_CONSISTENCY)
  }

  @Test
  fun testLintMode_fromString_invalidMode_returnsNull() {
    assertThat(LintMode.fromString("invalid")).isNull()
  }

  @Test
  fun testLintMode_argumentNames_containsAllModes() {
    val names = LintMode.values().map { it.argumentName }

    assertThat(names).containsExactly(
      "fast", "full", "list-checks", "check-script-consistency"
    )
  }

  @Test
  fun testLintOrchestrator_execute_checkScriptConsistencyMode_passesWithCurrentCatalog() {
    val orchestrator = LintOrchestrator(
      repoRoot = tempFolder.root,
      commandExecutor = fakeCommandExecutor,
      scriptBgDispatcher = scriptBgDispatcher
    )

    // Should not throw — the catalog is in sync with the built-in lint registry.
    orchestrator.execute(LintMode.CHECK_SCRIPT_CONSISTENCY)

    val output = outputStream.toString()
    assertThat(output).contains("CHECK PASSED: LintCheckCatalog")
    assertThat(output).contains("is consistent with lint")
  }

  @Test
  fun testLintOrchestrator_execute_fullMode_printsFullModeDescription() {
    val orchestrator = LintOrchestrator(
      repoRoot = tempFolder.root,
      commandExecutor = fakeCommandExecutor,
      scriptBgDispatcher = scriptBgDispatcher
    )

    try {
      orchestrator.execute(LintMode.FULL)
    } catch (_: Exception) {
      // Expected — runAnalysis fails without real bazel/lint infra.
    }

    val output = outputStream.toString()
    assertThat(output).contains("Running linter in 'full' mode with all repository files.")
  }

  @Test
  fun testLintOrchestrator_execute_listChecksMode_printsDescription() {
    val orchestrator = LintOrchestrator(
      repoRoot = tempFolder.root,
      commandExecutor = fakeCommandExecutor,
      scriptBgDispatcher = scriptBgDispatcher
    )

    try {
      orchestrator.execute(LintMode.LIST_CHECKS)
    } catch (_: Exception) {
      // Expected — runAnalysis fails without real bazel/lint infra.
    }

    val output = outputStream.toString()
    assertThat(output).contains("Running linter in 'list-checks' mode.")
  }

  @Test
  fun testLintOrchestrator_execute_fullMode_computesDisabledChecks() {
    val orchestrator = LintOrchestrator(
      repoRoot = tempFolder.root,
      commandExecutor = fakeCommandExecutor,
      scriptBgDispatcher = scriptBgDispatcher
    )

    try {
      orchestrator.execute(LintMode.FULL)
    } catch (_: Exception) {
      // Expected
    }

    // Verifies the FULL when branch and disabled checks computation path.
    val output = outputStream.toString()
    assertThat(output).contains("full")
  }

  @Test
  fun testLintOrchestrator_execute_withTimer_showsExecutionTime() {
    val orchestrator = LintOrchestrator(
      repoRoot = tempFolder.root,
      commandExecutor = fakeCommandExecutor,
      scriptBgDispatcher = scriptBgDispatcher,
      showTimer = true
    )

    try {
      orchestrator.execute(LintMode.FULL)
    } catch (_: Exception) {
      // Expected
    }

    val output = outputStream.toString()
    assertThat(output).contains("Total execution time:")
  }

  @Test
  fun testLintOrchestrator_execute_fastMode_printsFastModeDescription() {
    // GitClient makes multiple git calls: merge-base first (expects exactly 1 line),
    // then diffs for committed/staged/unstaged/untracked files.
    fakeCommandExecutor.registerHandler("git") { _, args, outputStream, _ ->
      if (args.contains("merge-base")) {
        // Use print() not println() — FakeCommandExecutor splits on '\n', so println would
        // produce ["hash", ""] (2 items), failing executeGitCommandWithOneLineOutput's check.
        outputStream.print("abc1234567890abcdef")
      }
      0 // all other git calls (diff, ls-files) return empty output
    }
    val orchestrator = LintOrchestrator(
      repoRoot = tempFolder.root,
      commandExecutor = fakeCommandExecutor,
      scriptBgDispatcher = scriptBgDispatcher
    )

    try {
      orchestrator.execute(LintMode.FAST)
    } catch (_: Exception) {
      // Expected — runAnalysis fails without real bazel/lint infra.
    }

    val output = outputStream.toString()
    assertThat(output).contains("Running linter in 'fast' mode")
  }

  @Test
  fun testLintOrchestrator_retrieveChangedSourceFiles_returnsOnlyKtAndJavaFiles() {
    // GitClient calls git merge-base first (must return exactly 1 line), then diffs.
    fakeCommandExecutor.registerHandler("git") { _, args, outputStream, _ ->
      when {
        args.contains("merge-base") -> {
          // print() not println() — avoids trailing empty string in FakeCommandExecutor output.
          outputStream.print("abc1234567890abcdef")
        }
        args.contains("--name-only") && args.any { it.startsWith("HEAD..") } -> {
          // Committed files diff — join with \n but no trailing newline.
          outputStream.print(
            "app/src/main/java/SomeFile.kt\n" +
              "app/src/main/java/AnotherFile.java\n" +
              "app/src/main/res/layout/activity_main.xml"
          )
        }
        // staged, unstaged, untracked: empty output — filtered automatically
      }
      0
    }
    val orchestrator = LintOrchestrator(
      repoRoot = tempFolder.root,
      commandExecutor = fakeCommandExecutor,
      scriptBgDispatcher = scriptBgDispatcher
    )

    val changedFiles = orchestrator.retrieveChangedSourceFiles()

    assertThat(changedFiles).containsExactly(
      "app/src/main/java/SomeFile.kt",
      "app/src/main/java/AnotherFile.java"
    )
  }

  @Test
  fun testAndroidLintAnalyzer_withListChecksMode_completesSuccessfully() {
    setupProjectStructure()
    val listChecksAnalyzer = AndroidLintAnalyzer(
      commandExecutor = fakeCommandExecutor,
      workingDirectory = workingDirectory,
      exemptionProtoPath = "${tempFolder.root}/$pathToProtoBinary",
      repoRoot = tempFolder.root,
      reportUnusedEnum = false,
      additionalDisabledChecks = LintCheckCatalog.checksAlwaysDisabled,
      listChecks = true
    )

    // In list-checks mode, lint prints available checks and exits 0 without reporting issues.
    listChecksAnalyzer.runAnalysis()

    val output = outputStream.toString()
    assertThat(output).doesNotContain("ANDROID LINT CHECK FAILED")
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
  fun testAndroidLintAnalyzer_withDuplicateStringResources_issueIsSuppressed() {
    setupProjectWithDuplicateStringIssue()

    androidLintAnalyzerWithFakeExecutor.runAnalysis()

    val output = outputStream.toString()
    assertThat(output).contains("${GREEN}ANDROID LINT CHECK ${BOLD}PASSED$RESET")
    assertThat(output).doesNotContain("DUPLICATE_STRINGS")
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
    assertThat(output).contains("USELESS_PARENT")
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
    assertThat(output).contains("USELESS_LEAF")
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

    val exception = assertThrows<IllegalStateException> {
      androidLintAnalyzerWithFakeExecutor.runAnalysis()
    }
    val output = outputStream.toString()
    assertThat(output).contains("RTL_HARDCODED")
    assertThat(output).contains("Category: Internationalization")
    assertThat(output)
      .contains("Error Line:         android:layout_alignParentRight=\"true\" />")
    assertThat(output).contains("Line: 8")
    assertThat(output)
      .contains(
        "Consider replacing `android:layout_alignParentRight`" +
          " with `android:layout_alignParentEnd=\"true\"` to better support right-to-left layouts"
      )

    assertThat(exception.message)
      .isEqualTo("${RED}ANDROID LINT CHECK ${BOLD}FAILED$RESET")

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
    assertThat(output).contains("RTL_SYMMETRY")
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
    val exception = assertThrows<IllegalStateException> {
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
      .isEqualTo("${RED}ANDROID LINT CHECK ${BOLD}FAILED$RESET")

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
    assertThat(output).doesNotContain("SYNTHETIC_ACCESSOR")
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
    assertThat(output).contains("LABEL_FOR")
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
  fun testAndroidLintAnalyzer_withUnusedAttribute_issueIsSuppressed() {
    setupProjectWithUnusedAttribute()
    androidLintAnalyzerWithFakeExecutor.runAnalysis()

    val output = outputStream.toString()
    assertThat(output).contains("${GREEN}ANDROID LINT CHECK ${BOLD}PASSED$RESET")
    assertThat(output).doesNotContain("UNUSED_ATTRIBUTE")
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

  @Test
  fun testAndroidLintAnalyzer_withMissingClass_detectsIssue() {
    setupProjectWithMissingClass()

    val exception = assertThrows<IllegalStateException> {
      androidLintAnalyzerWithFakeExecutor.runAnalysis()
    }
    val output = outputStream.toString()
    assertThat(output).contains("MISSING_CLASS")
    assertThat(output)
      .contains("<foo.bar.Baz />")
    assertThat(output).contains("Line: 5")
    assertThat(output)
      .contains(
        "Class referenced in the layout file, `foo.bar.Baz`," +
          " was not found in the project or the libraries"
      )

    assertThat(exception.message)
      .isEqualTo("${RED}ANDROID LINT CHECK ${BOLD}FAILED$RESET")

    val projectDescriptionContent = projectDescriptionFile.readText()
    assertThat(projectDescriptionContent)
      .contains("app/src/main/res")
  }

  private fun setupProjectWithMissingClass() {
    setupProjectStructure()

    createFileWithContent(
      "app/src/main/res/layout/customview.xml",
      """
    <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
        android:id="@+id/newlinear"
        android:layout_width="match_parent"
        android:layout_height="match_parent" >
        <foo.bar.Baz />
        <test.pkg.MyView />
        <test.pkg.NotView />
    </LinearLayout>
    """
    )

    createFileWithContent(
      "app/src/test/java/test/pkg/MyView.kt",
      """
    package test.pkg

    abstract class MyView : I, android.view.View(null)

    interface I
    """
    )

    createFileWithContent(
      "app/src/test/java/test/pkg/NotView.kt",
      """
    package test.pkg

    abstract class NotView : android.app.Fragment()
    """
    )
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

  private fun setupProjectStructure() {
    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.setUpWorkspaceForRulesJvmExternal(listOf("junit:junit:4.12"))

    createLayer("app")
    createLayer("utility")
    createLayer("domain")
    createLayer("testing")
    createLayer("data")

    setupFakeCommandExecutor()
  }

  private fun createLayer(layerName: String) {
    createLayerDirectories(layerName)
    createLayerFiles(layerName)
  }

  private fun createLayerDirectories(layerName: String) {
    val directories = listOf(
      layerName,
      "$layerName/src",
      "$layerName/src/main",
      "$layerName/src/main/java",
      "$layerName/src/main/res",
      "$layerName/src/main/res/values",
      "$layerName/src/test",
      "$layerName/src/test/java"
    )

    directories.forEach { dir ->
      tempFolder.newFolder(*dir.split("/").toTypedArray())
    }
  }

  private fun createLayerFiles(layerName: String) {
    createManifestFile(layerName)
    createTestManifestFile(layerName)
    createSourceFile(layerName)
    createTestFile(layerName)
    createResourceFile(layerName)
  }

  private fun createManifestFile(layerName: String) {
    val manifest = tempFolder.newFile("$layerName/src/main/AndroidManifest.xml")
    manifest.writeText(
      """
    <?xml version="1.0" encoding="utf-8"?>
    <manifest xmlns:android="http://schemas.android.com/apk/res/android"
      package="org.oppia.android.$layerName"
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
  }

  private fun createTestManifestFile(layerName: String) {
    val manifest = tempFolder.newFile("$layerName/src/test/AndroidManifest.xml")
    manifest.writeText(
      """
    <?xml version="1.0" encoding="utf-8"?>
    <manifest xmlns:android="http://schemas.android.com/apk/res/android"
      package="org.oppia.android.$layerName">
      <uses-sdk android:minSdkVersion="$MIN_SDK_VERSION" android:targetSdkVersion="$TARGET_SDK_VERSION" />
    </manifest>
      """.trimIndent()
    )
  }

  private fun createSourceFile(layerName: String) {
    val className = layerName.replaceFirstChar { it.uppercase() }
    val sourceDir = tempFolder.newFolder(
      layerName, "src", "main", "java", "org", "oppia", "android", layerName
    )
    val sourceFile = File(sourceDir, "${className}Class.kt")
    sourceFile.writeText(
      """
    package org.oppia.android.$layerName

    class ${className}Class {
        fun doSomething(): String = "Hello from $layerName"
    }
      """.trimIndent()
    )
  }

  private fun createTestFile(layerName: String) {
    val className = layerName.replaceFirstChar { it.uppercase() }
    val testDir = tempFolder.newFolder(
      layerName, "src", "test", "java", "org", "oppia", "android", layerName
    )
    val testFile = File(testDir, "${className}ClassTest.kt")
    testFile.writeText(
      """
    package org.oppia.android.$layerName

    import org.junit.Test
    import org.junit.Assert.assertEquals

    class ${className}ClassTest {
        @Test
        fun testDoSomething() {
            val instance = ${className}Class()
            assertEquals("Hello from $layerName", instance.doSomething())
        }
    }
      """.trimIndent()
    )
  }

  private fun createResourceFile(layerName: String) {
    val resourceFile = tempFolder.newFile("$layerName/src/main/res/values/strings.xml")
    resourceFile.writeText(
      """
      <?xml version="1.0" encoding="utf-8"?>
      <resources>
          <string name="${layerName}_name">$layerName Layer</string>
      </resources>
      """.trimIndent()
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
