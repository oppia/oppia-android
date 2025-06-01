package org.oppia.android.scripts.lint

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.testing.assertThrows
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream

/** Tests for [AndroidLintRunner]. */
// Same parameter value: helpers reduce test context, even if they are used by 1 test.
// Function name: test names are conventionally named with underscores.
@Suppress("SameParameterValue", "FunctionName")
class AndroidLintRunnerTest {
  @field:[Rule JvmField] var tempFolder = TemporaryFolder()

  private lateinit var mockRepoRoot: File
  private lateinit var outputStream: ByteArrayOutputStream

  @Before
  fun setUp() {
    mockRepoRoot = tempFolder.root
    outputStream = ByteArrayOutputStream()
    System.setOut(PrintStream(outputStream))
  }

  @Test
  fun testMain_noArguments_throwsException() {
    val exception = assertThrows<IllegalArgumentException> {
      main()
    }

    assertThat(exception).hasMessageThat().contains(
      "Usage: bazel run //scripts:android_lint_check -- <path_to_repository_root>"
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
  fun testMain_multipleArguments_usesFirstArgument() {
    val validPath = mockRepoRoot.absolutePath
    val invalidPath = "invalid_path"

    try {
      main(validPath, invalidPath, "extra_arg")
    } catch (e: Exception) {
      assertThat(e).isNotInstanceOf(IllegalArgumentException::class.java)
    }
  }

  @Test
  fun testAndroidLintRunner_runLint_createsTemporaryDirectory() {
    val lintRunner = AndroidLintRunner()
    lintRunner.runLint()

    val output = outputStream.toString()
    assertThat(output).contains("Using")
    assertThat(output).contains("lint_analysis_")
    assertThat(output).contains("as an intermediary working directory")
  }
}
