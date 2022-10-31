package org.oppia.android.scripts.common

import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.testing.assertThrows
import java.io.File
import java.io.IOException
import java.lang.IllegalStateException
import java.util.concurrent.TimeUnit
import org.oppia.android.scripts.common.CommandExecutor.OutputRedirectionStrategy.TRACK_AS_OUTPUT

/**
 * Tests for [CommandExecutorImpl].
 *
 * Note that this test executes real commands on the local filesystem & requires being run in an
 * environment which have echo and rmdir commands.
 */
// FunctionName: test names are conventionally named with underscores.
// SameParameterValue: tests should have specific context included/excluded for readability.
@Suppress("FunctionName", "SameParameterValue")
class CommandExecutorImplTest {
  @Rule
  @JvmField
  var tempFolder = TemporaryFolder()

  @Test
  fun testExecute_echo_oneArgument_succeedsWithOutput() {
    val commandExecutor = createCommandExecutor(tempFolder.root)

    val result = commandExecutor.executeCommandInForeground("echo", "value")

    assertThat(result.exitCode).isEqualTo(0)
    assertThat(result.output).containsExactly("value")
  }

  @Test
  fun testExecute_echo_invalidDirectory_throwsException() {
    val exception = assertThrows(IllegalStateException::class) {
      createCommandExecutor(File("invaliddirectory"))
    }

    assertThat(exception).hasMessageThat().contains("working directory to be an actual directory")
  }

  @Test
  fun testExecute_echo_largeOutput_insufficientTimeout_throwsException() {
    val commandExecutor = createCommandExecutor(tempFolder.root, processTimeoutMillis = 1L)

    // Produce a large output so that echo takes a bit longer to reduce the likelihood of this test
    // flaking on faster machines.
    val largeOutput = "a".repeat(100_000)
    val exception = assertThrows(IllegalStateException::class) {
      commandExecutor.executeCommandInForeground("echo", largeOutput)
    }

    // Verify that processes that take too long are killed & result in a failure.
    assertThat(exception).hasMessageThat().contains("Timed out waiting for")
  }

  @Test
  fun testExecute_nonexistentCommand_throwsException() {
    val commandExecutor = createCommandExecutor(tempFolder.root)

    val exception = assertThrows(IOException::class) {
      commandExecutor.executeCommandInForeground("commanddoesnotexist")
    }

    assertThat(exception).hasMessageThat().contains("commanddoesnotexist")
  }

  @Test
  fun testExecute_echo_multipleArguments_succeedsWithOutput() {
    val commandExecutor = createCommandExecutor(tempFolder.root)

    val result = commandExecutor.executeCommandInForeground("echo", "first", "second", "third")

    assertThat(result.exitCode).isEqualTo(0)
    assertThat(result.output).containsExactly("first second third")
  }

  @Test
  fun testExecute_echo_multipleArguments_resultHasCorrectCommand() {
    val commandExecutor = createCommandExecutor(tempFolder.root)

    val result = commandExecutor.executeCommandInForeground("echo", "first", "second", "third")

    assertThat(result.command).containsExactly("echo", "first", "second", "third")
  }

  @Test
  fun testExecute_trackErrorAsOutput_rmdir_failed_failsWithCombinedOutput() {
    val commandExecutor = createCommandExecutor(tempFolder.root)

    val result =
      commandExecutor.executeCommandInForeground(
        "rmdir", "filethatdoesnotexist", stderrRedirection = TRACK_AS_OUTPUT
      )

    assertThat(result.exitCode).isNotEqualTo(0)
    assertThat(result.output).hasSize(1)
    assertThat(result.output.first()).contains("filethatdoesnotexist")
    assertThat(result.errorOutput).isEmpty()
  }

  @Test
  fun testExecute_splitErrorOutput_rmdir_failed_failsWithErrorOutput() {
    val commandExecutor = createCommandExecutor(tempFolder.root)

    val result = commandExecutor.executeCommandInForeground("rmdir", "filethatdoesnotexist")

    assertThat(result.exitCode).isNotEqualTo(0)
    assertThat(result.errorOutput).hasSize(1)
    assertThat(result.errorOutput.first()).contains("filethatdoesnotexist")
    assertThat(result.output).isEmpty()
  }

  @Test
  fun testExecute_removeDirectoryInLocalDirectory_succeeds() {
    val newFolder = tempFolder.newFolder("newfolder")
    val commandExecutor = createCommandExecutor(tempFolder.root)

    val result = commandExecutor.executeCommandInForeground("rmdir", "./newfolder")

    // Verify that the command succeeds & the directory is missing. This demonstrates local
    // directory referencing is relative to the directory passed to executeCommand.
    assertThat(result.exitCode).isEqualTo(0)
    assertThat(newFolder.exists()).isFalse()
  }

  @Test
  fun testExecute_removeUnknownDirectoryInOtherDirectory_fails() {
    val newFolder = tempFolder.newFolder("newfolder")
    val alternateRoot = tempFolder.newFolder("alternateroot")
    val commandExecutor = createCommandExecutor(alternateRoot)

    val result = commandExecutor.executeCommandInForeground("rmdir", "./newfolder")

    // Trying to delete the folder somewhere should fail if it doesn't exist there since the command
    // executes relative to the provided directory.
    assertThat(result.exitCode).isNotEqualTo(0)
    assertThat(newFolder.exists()).isTrue()
  }

  private fun createCommandExecutor(workingDir: File) =
    CommandExecutorImpl.BuilderImpl.FactoryImpl().createBuilder().create(workingDir)

  private fun createCommandExecutor(workingDir: File, processTimeoutMillis: Long): CommandExecutor {
    val builder = CommandExecutorImpl.BuilderImpl.FactoryImpl().createBuilder()
    return builder.setProcessTimeout(processTimeoutMillis, TimeUnit.MILLISECONDS).create(workingDir)
  }
}
