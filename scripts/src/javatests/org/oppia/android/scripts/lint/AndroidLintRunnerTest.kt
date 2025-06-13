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
      "Expected: bazel run //scripts:android_lint_check -- <path_to_repository_root>"
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
  fun testPrepareLintArguments_includesRequiredFlags() {
    val lintRunner = AndroidLintRunner()
    val reportPath = "/path/to/report.xml"
    val projectPath = "/path/to/project.xml"

    val method = AndroidLintRunner::class.java.getDeclaredMethod(
      "prepareLintArguments",
      String::class.java,
      String::class.java
    )
    method.isAccessible = true

    val result = method.invoke(lintRunner, reportPath, projectPath) as Array<*>

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
}
