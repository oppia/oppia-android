package org.oppia.android.scripts.common

import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.testing.assertion.assertThrows
import java.io.File
import java.io.IOException
import java.lang.IllegalStateException
import java.util.concurrent.TimeUnit

/**
 * Tests for [CommandExecutorImpl].
 *
 * Note that this test executes real commands on the local filesystem & requires being run in an
 * environment which have echo and rmdir commands.
 */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class CommandExecutorImplTest {
  @Rule
  @JvmField
  var tempFolder = TemporaryFolder()

  @Test
  fun testExecute_echo_oneArgument_succeedsWithOutput() {
    val commandExecutor = CommandExecutorImpl()

    val result = commandExecutor.executeCommand(tempFolder.root, "echo", "value")

    assertThat(result.exitCode).isEqualTo(0)
    assertThat(result.output).containsExactly("value")
  }

  @Test
  fun testExecute_echo_invalidDirectory_throwsException() {
    val commandExecutor = CommandExecutorImpl()

    val exception = assertThrows(IllegalStateException::class) {
      commandExecutor.executeCommand(File("invaliddirectory"), "echo", "value")
    }

    assertThat(exception).hasMessageThat().contains("working directory to be an actual directory")
  }

  @Test
  fun testExecute_echo_largeOutput_insufficientTimeout_throwsException() {
    val commandExecutor = CommandExecutorImpl(
      processTimeout = 0L, processTimeoutUnit = TimeUnit.MILLISECONDS
    )

    // Produce a large output so that echo takes a bit longer to reduce the likelihood of this test
    // flaking on faster machines.
    val largeOutput = "a".repeat(100_000)
    val exception = assertThrows(IllegalStateException::class) {
      commandExecutor.executeCommand(tempFolder.root, "echo", largeOutput)
    }

    // Verify that processes that take too long are killed & result in a failure.
    assertThat(exception).hasMessageThat().contains("Process did not finish within")
  }

  @Test
  fun testExecute_nonexistentCommand_throwsException() {
    val commandExecutor = CommandExecutorImpl()

    val exception = assertThrows(IOException::class) {
      commandExecutor.executeCommand(tempFolder.root, "commanddoesnotexist")
    }

    assertThat(exception).hasMessageThat().contains("commanddoesnotexist")
  }

  @Test
  fun testExecute_echo_multipleArguments_succeedsWithOutput() {
    val commandExecutor = CommandExecutorImpl()

    val result = commandExecutor.executeCommand(tempFolder.root, "echo", "first", "second", "third")

    assertThat(result.exitCode).isEqualTo(0)
    assertThat(result.output).containsExactly("first second third")
  }

  @Test
  fun testExecute_echo_multipleArguments_resultHasCorrectCommand() {
    val commandExecutor = CommandExecutorImpl()

    val result = commandExecutor.executeCommand(tempFolder.root, "echo", "first", "second", "third")

    assertThat(result.command).containsExactly("echo", "first", "second", "third")
  }

  @Test
  fun testExecute_defaultErrorOutput_rmdir_failed_failsWithCombinedOutput() {
    val commandExecutor = CommandExecutorImpl()

    val result = commandExecutor.executeCommand(tempFolder.root, "rmdir", "filethatdoesnotexist")

    assertThat(result.exitCode).isNotEqualTo(0)
    assertThat(result.output).hasSize(1)
    assertThat(result.output.first()).contains("filethatdoesnotexist")
    assertThat(result.errorOutput).isEmpty()
  }

  @Test
  fun testExecute_splitErrorOutput_rmdir_failed_failsWithErrorOutput() {
    val commandExecutor = CommandExecutorImpl()

    val result =
      commandExecutor.executeCommand(
        tempFolder.root, "rmdir", "filethatdoesnotexist", includeErrorOutput = false
      )

    assertThat(result.exitCode).isNotEqualTo(0)
    assertThat(result.errorOutput).hasSize(1)
    assertThat(result.errorOutput.first()).contains("filethatdoesnotexist")
    assertThat(result.output).isEmpty()
  }

  @Test
  fun testExecute_removeDirectoryInLocalDirectory_succeeds() {
    val newFolder = tempFolder.newFolder("newfolder")
    val commandExecutor = CommandExecutorImpl()

    val result = commandExecutor.executeCommand(tempFolder.root, "rmdir", "./newfolder")

    // Verify that the command succeeds & the directory is missing. This demonstrates local
    // directory referencing is relative to the directory passed to executeCommand.
    assertThat(result.exitCode).isEqualTo(0)
    assertThat(newFolder.exists()).isFalse()
  }

  @Test
  fun testExecute_removeUnknownDirectoryInOtherDirectory_fails() {
    val newFolder = tempFolder.newFolder("newfolder")
    val alternateRoot = tempFolder.newFolder("alternateroot")
    val commandExecutor = CommandExecutorImpl()

    val result = commandExecutor.executeCommand(alternateRoot, "rmdir", "./newfolder")

    // Trying to delete the folder somewhere should fail if it doesn't exist there since the command
    // executes relative to the provided directory.
    assertThat(result.exitCode).isNotEqualTo(0)
    assertThat(newFolder.exists()).isTrue()
  }
}
