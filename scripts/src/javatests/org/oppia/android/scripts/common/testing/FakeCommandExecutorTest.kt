package org.oppia.android.scripts.common.testing

import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.testing.assertThrows
import java.io.IOException

/** Tests for [FakeCommandExecutor]. */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class FakeCommandExecutorTest {
  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

  @Test
  fun testRegisterCommand_doesNotThrowException() {
    val commandExecutor = FakeCommandExecutor()

    commandExecutor.registerHandler("test") { _, _, _, _ -> 0 }

    // The verification is that no exception is thrown.
  }

  @Test
  fun testRegisterTwoCommands_doesNotThrowException() {
    val commandExecutor = FakeCommandExecutor()
    commandExecutor.registerHandler("test1") { _, _, _, _ -> 0 }

    // Register a second, different command.
    commandExecutor.registerHandler("test2") { _, _, _, _ -> 1 }

    // The verification is that no exception is thrown.
  }

  @Test
  fun testRegisterCommandTwice_doesNotThrowException() {
    val commandExecutor = FakeCommandExecutor()
    commandExecutor.registerHandler("test1") { _, _, _, _ -> 0 }

    // Register the same command a second time.
    commandExecutor.registerHandler("test1") { _, _, _, _ -> 1 }

    // The verification is that no exception is thrown.
  }

  @Test
  fun testExecuteCommand_unregisteredCommand_throwsIoException() {
    val commandExecutor = FakeCommandExecutor()

    val exception = assertThrows<IOException>() {
      commandExecutor.executeCommand(tempFolder.root, "test")
    }

    assertThat(exception).hasMessageThat().contains("Command doesn't exist.")
  }

  @Test
  fun testExecuteCommand_diffCaseFromRegistered_throwsIoException() {
    val commandExecutor = FakeCommandExecutor()
    commandExecutor.registerHandler("test") { _, _, _, _ -> 0 }

    val exception = assertThrows<IOException>() {
      commandExecutor.executeCommand(tempFolder.root, "TEST")
    }

    // Commands are case-sensitive.
    assertThat(exception).hasMessageThat().contains("Command doesn't exist.")
  }

  @Test
  fun testExecuteCommand_registeredCommand_noArgs_returnsResultWithCorrectCommandLine() {
    val commandExecutor = FakeCommandExecutor()
    commandExecutor.registerHandler("test") { _, _, _, _ -> 0 }

    val result = commandExecutor.executeCommand(tempFolder.root, "test")

    assertThat(result.command).containsExactly("test").inOrder()
  }

  @Test
  fun testExecuteCommand_registeredCommand_oneArg_returnsResultWithCorrectCommandLine() {
    val commandExecutor = FakeCommandExecutor()
    commandExecutor.registerHandler("test") { _, _, _, _ -> 0 }

    val result = commandExecutor.executeCommand(tempFolder.root, "test", "arg1")

    assertThat(result.command).containsExactly("test", "arg1").inOrder()
  }

  @Test
  fun testExecuteCommand_registeredCommand_multipleArgs_returnsResultWithCorrectCommandLine() {
    val commandExecutor = FakeCommandExecutor()
    commandExecutor.registerHandler("test") { _, _, _, _ -> 0 }

    val result = commandExecutor.executeCommand(tempFolder.root, "test", "arg1", "arg3", "arg2")

    assertThat(result.command).containsExactly("test", "arg1", "arg3", "arg2").inOrder()
  }

  @Test
  fun testExecuteCommand_registeredCommand_returnsResultWithCorrectExitCode() {
    val commandExecutor = FakeCommandExecutor()
    commandExecutor.registerHandler("test") { _, _, _, _ -> 0 }

    val result = commandExecutor.executeCommand(tempFolder.root, "test")

    assertThat(result.exitCode).isEqualTo(0)
  }

  @Test
  fun testExecuteCommand_registeredCommand_noArgs_returnsCorrectExitCodeUsingArgs() {
    val commandExecutor = FakeCommandExecutor()
    commandExecutor.registerHandler("test") { _, args, _, _ -> 1 + args.sumOf { it.length } }

    val result = commandExecutor.executeCommand(tempFolder.root, "test")

    // No args passed.
    assertThat(result.exitCode).isEqualTo(1)
  }

  @Test
  fun testExecuteCommand_registeredCommand_oneArg_returnsCorrectExitCodeUsingArgs() {
    val commandExecutor = FakeCommandExecutor()
    commandExecutor.registerHandler("test") { _, args, _, _ -> 1 + args.sumOf { it.length } }

    val result = commandExecutor.executeCommand(tempFolder.root, "test", "aa")

    // One arg contributes to the exit code value.
    assertThat(result.exitCode).isEqualTo(3)
  }

  @Test
  fun testExecuteCommand_registeredCommand_multipleArgs_returnsCorrectExitCodeUsingArgs() {
    val commandExecutor = FakeCommandExecutor()
    commandExecutor.registerHandler("test") { _, args, _, _ -> 1 + args.sumOf { it.length } }

    val result = commandExecutor.executeCommand(tempFolder.root, "test", "aa", "b", "ccc")

    // All args contribute to the exit code value.
    assertThat(result.exitCode).isEqualTo(7)
  }

  @Test
  fun testExecuteCommand_registeredCommand_returnsResultWithCorrectStandardOutput() {
    val commandExecutor = FakeCommandExecutor()
    commandExecutor.registerHandler("test") { _, _, standardStream, errorStream ->
      standardStream.println("Line one")
      errorStream.println("Error line one")
      standardStream.println("Line two")
      errorStream.println("Error line two")
      errorStream.println("Error line three")
      return@registerHandler 0
    }

    val result = commandExecutor.executeCommand(tempFolder.root, "test")

    assertThat(result.output).hasSize(3)
    assertThat(result.output[0]).isEqualTo("Line one")
    assertThat(result.output[1]).isEqualTo("Line two")
    assertThat(result.output[2]).isEmpty()
  }

  @Test
  fun testExecuteCommand_registeredCommand_returnsResultWithCorrectErrorOutput() {
    val commandExecutor = FakeCommandExecutor()
    commandExecutor.registerHandler("test") { _, _, standardStream, errorStream ->
      standardStream.println("Line one")
      errorStream.println("Error line one")
      standardStream.println("Line two")
      errorStream.println("Error line two")
      errorStream.println("Error line three")
      return@registerHandler 0
    }

    val result = commandExecutor.executeCommand(tempFolder.root, "test")

    assertThat(result.errorOutput).hasSize(4)
    assertThat(result.errorOutput[0]).isEqualTo("Error line one")
    assertThat(result.errorOutput[1]).isEqualTo("Error line two")
    assertThat(result.errorOutput[2]).isEqualTo("Error line three")
    assertThat(result.errorOutput[3]).isEmpty()
  }

  @Test
  fun testExecuteCommand_registeredCommand_includeErrorOutput_returnsResultWithErrorOutput() {
    val commandExecutor = FakeCommandExecutor()
    commandExecutor.registerHandler("test") { _, _, standardStream, errorStream ->
      standardStream.println("Line one")
      errorStream.println("Error line one")
      standardStream.println("Line two")
      errorStream.println("Error line two")
      errorStream.println("Error line three")
      return@registerHandler 0
    }

    val result = commandExecutor.executeCommand(tempFolder.root, "test", includeErrorOutput = true)

    // The result should include error output.
    assertThat(result.errorOutput).hasSize(4)
    assertThat(result.errorOutput[0]).isEqualTo("Error line one")
    assertThat(result.errorOutput[1]).isEqualTo("Error line two")
    assertThat(result.errorOutput[2]).isEqualTo("Error line three")
    assertThat(result.errorOutput[3]).isEmpty()
    assertThat(result.output).hasSize(3)
    assertThat(result.output[0]).isEqualTo("Line one")
    assertThat(result.output[1]).isEqualTo("Line two")
    assertThat(result.output[2]).isEmpty()
  }

  @Test
  fun testExecuteCommand_registeredCommand_doNotIncludeErrorOutput_returnsResultWithNoErrorLines() {
    val commandExecutor = FakeCommandExecutor()
    commandExecutor.registerHandler("test") { _, _, standardStream, errorStream ->
      standardStream.println("Line one")
      errorStream.println("Error line one")
      standardStream.println("Line two")
      errorStream.println("Error line two")
      errorStream.println("Error line three")
      return@registerHandler 0
    }

    val result = commandExecutor.executeCommand(tempFolder.root, "test", includeErrorOutput = false)

    // The result should include no error output.
    assertThat(result.errorOutput).isEmpty()
    assertThat(result.output).hasSize(3)
    assertThat(result.output[0]).isEqualTo("Line one")
    assertThat(result.output[1]).isEqualTo("Line two")
    assertThat(result.output[2]).isEmpty()
  }

  @Test
  fun testExecuteCommand_secondRegisteredCommand_returnsCorrectCommandLine() {
    val commandExecutor = FakeCommandExecutor()
    commandExecutor.registerHandler("test1") { _, _, _, _ -> 0 }
    commandExecutor.registerHandler("test2") { _, _, _, _ -> 1 }

    val result = commandExecutor.executeCommand(tempFolder.root, "test2", "arg1", "arg3", "arg2")

    assertThat(result.command).containsExactly("test2", "arg1", "arg3", "arg2").inOrder()
  }

  @Test
  fun testExecuteCommand_secondRegisteredCommand_returnsCorrectExitCode() {
    val commandExecutor = FakeCommandExecutor()
    commandExecutor.registerHandler("test1") { _, _, _, _ -> 0 }
    commandExecutor.registerHandler("test2") { _, _, _, _ -> 1 }

    val result = commandExecutor.executeCommand(tempFolder.root, "test2", "arg1", "arg3", "arg2")

    assertThat(result.exitCode).isEqualTo(1)
  }

  @Test
  fun testExecuteCommand_replacedRegisteredCommand_returnsCorrectCommandLine() {
    val commandExecutor = FakeCommandExecutor()
    commandExecutor.registerHandler("test1") { _, _, _, _ -> 0 }
    commandExecutor.registerHandler("test1") { _, _, _, _ -> 1 }

    val result = commandExecutor.executeCommand(tempFolder.root, "test1", "arg1", "arg3", "arg2")

    assertThat(result.command).containsExactly("test1", "arg1", "arg3", "arg2").inOrder()
  }

  @Test
  fun testExecuteCommand_replacedRegisteredCommand_returnsCorrectExitCode() {
    val commandExecutor = FakeCommandExecutor()
    commandExecutor.registerHandler("test1") { _, _, _, _ -> 0 }
    commandExecutor.registerHandler("test1") { _, _, _, _ -> 1 }

    val result = commandExecutor.executeCommand(tempFolder.root, "test1", "arg1", "arg3", "arg2")

    // The replaced command handler should be used, instead.
    assertThat(result.exitCode).isEqualTo(1)
  }
}
