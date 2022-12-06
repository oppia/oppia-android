package org.oppia.android.scripts.emulator

import java.io.File
import java.lang.Exception
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandResult
import org.oppia.android.scripts.common.FileUtils.toAbsoluteNormalizedPath
import org.oppia.android.scripts.common.TaskRunner

class EmulatorClient(
  private val emulatorBinary: File,
  private val adbClient: AdbClient,
  private val virtualSdkRoot: File,
  private val commandExecutorBuilderFactory: CommandExecutor.Builder.Factory
) {
  private val emulatorBinaryPath by lazy { emulatorBinary.toAbsoluteNormalizedPath() }

  private val commandExecutor by lazy {
    // The working directory doesn't generally matter for ADB commands.
    commandExecutorBuilderFactory.createBuilder()
      .setEnvironmentVariable(
        name = "ANDROID_SDK_HOME", value = virtualSdkRoot.toAbsoluteNormalizedPath()
      )
      .setProcessTimeout(timeout = 15, timeoutUnit = TimeUnit.MINUTES)
      .create(workingDirectory = File("."))
  }

  fun startEmulator(startupSettings: EmulatorStartupSettings): EmulatorSession {
    val openPort = adbClient.findOpenEmulatorPort()
    checkNotNull(openPort) {
      "Could not find an open port to start the emulator (this means all 16 available slots are" +
        " currently being used)."
    }
    val emulatorArgs = listOfNotNull(
      "-avd",
      startupSettings.avdName,
      if (startupSettings.disableBootAnimation) "-no-boot-anim" else null,
      if (startupSettings.disableSnapshotLoad) "-no-snapshot-load" else null,
      if (startupSettings.disableSnapshotSave) "-no-snapshot-save" else null,
      if (startupSettings.disableAudio) "-no-audio" else null,
      "-port",
      openPort.toString(),
      if (startupSettings.disableRendering) "-no-window" else null,
      if (startupSettings.disableAcceleration) "-no-accel" else null
    )

    val deferred = runEmulatorCommandAsync(*emulatorArgs.toTypedArray())
    val emulator = try {
      waitForEmulator(startupSettings, openPort)
    } catch (e: Exception) {
      deferred.cancel()
      throw e
    }
    return EmulatorSession(emulator)
  }

  private fun waitForEmulator(
    startupSettings: EmulatorStartupSettings, openPort: Int
  ): AdbClient.AndroidDevice.Emulator {
    val partiallyBooted = waitForPartiallyAvailableEmulator(startupSettings, openPort)
    val snapshotName = startupSettings.snapshotToBoot
    if (snapshotName != null) {
      EmulatorSession(partiallyBooted).loadSnapshot(snapshotName)
    }
    return waitForEmulatorToBeBooted(partiallyBooted, startupSettings, openPort)
  }

  private fun waitForPartiallyAvailableEmulator(
    startupSettings: EmulatorStartupSettings, openPort: Int
  ): AdbClient.AndroidDevice.Emulator {
    // Wait for the device to be available in a 'device' state.
    println("Waiting for emulator to start on port: $openPort...")
    return runBlocking(TaskRunner.coroutineDispatcher) {
      // Wait no more than 5 seconds for the emulator to at least be available.
      withTimeoutOrNull(timeMillis = 5_000L) {
        // Check every 50ms for the emulator to be started.
        var emulator = adbClient.findEmulatorWithPort(openPort)
        while (emulator == null) {
          delay(50L)
          emulator = adbClient.findEmulatorWithPort(openPort)
        }
        return@withTimeoutOrNull emulator
      } ?: error(
        "Expected emulator to visible to ADB after startup, for port $openPort and settings:" +
          " $startupSettings."
      )
    }
  }

  private fun waitForEmulatorToBeBooted(
    partiallyBooted: AdbClient.AndroidDevice.Emulator,
    startupSettings: EmulatorStartupSettings,
    openPort: Int
  ): AdbClient.AndroidDevice.Emulator {
    // Wait for the device to be fully booted. See https://stackoverflow.com/a/45991252 and
    // https://stackoverflow.com/a/46316745 for specifics. Note that the device doesn't need to be
    // fully connected in order to run this command.
    adbClient.runShellCommand(
      partiallyBooted,
      "while [[ -z $(getprop sys.boot_completed) ]]; do sleep 1; done",
      commandWrapper = AdbClient.ShellCommandWrapper.WaitForProcess
    )

    return checkNotNull(adbClient.findEmulatorWithPort(openPort)) {
      "Emulator unexpectedly died immediately after startup,, for port $openPort and settings:" +
        " $startupSettings."
    }
  }

  private fun runEmulatorCommandAsync(vararg arguments: String): Deferred<CommandResult> {
    return commandExecutor.executeCommandInBackgroundAsync(emulatorBinaryPath, *arguments)
  }

  inner class EmulatorSession internal constructor(
    val device: AdbClient.AndroidDevice.Emulator
  ) {
    fun recordScreen() {
      // TODO: Coalesce & return full recording? Also, this probably needs to be more of a 'start'
      //  and 'stop' since it'll span multiple processes.
      adbClient.recordScreen(device, timeLimit = 30, TimeUnit.SECONDS)
    }

    fun createSnapshot(snapshotName: String) {
      runSnapshotCommand("save", snapshotName).verifyAvdCommandSucceeded {
        "Encountered an issue when saving snapshot $snapshotName: $it."
      }
    }

    fun loadSnapshot(snapshotName: String) {
      runSnapshotCommand("load", snapshotName).verifyAvdCommandSucceeded {
        "Encountered an issue when loading snapshot $snapshotName: $it."
      }
    }

    fun kill() {
      adbClient.runEmulatorCommand(device, "kill")

      // Wait up to 5 seconds for the emulator to be fully killed.
      runBlocking(TaskRunner.coroutineDispatcher) {
        withTimeoutOrNull(timeMillis = 5_000L) {
          while (adbClient.listEmulators().any { it.port == device.port }) delay(50)
        } ?: error("Expected emulator to be shutdown within 5 seconds: ${device.serialName}.")
      }
    }

    private fun runSnapshotCommand(action: String, vararg arguments: String) =
      adbClient.runEmulatorCommand(device, "avd", "snapshot", action, *arguments)

    private fun List<String>.verifyAvdCommandSucceeded(
      createFailureMessage: (List<String>) -> String
    ) = check(last() == "OK") { createFailureMessage(this) }
  }

  data class EmulatorStartupSettings(
    val avdName: String,
    val disableBootAnimation: Boolean = true,
    val disableSnapshotLoad: Boolean = false,
    val disableSnapshotSave: Boolean = false,
    val disableAudio: Boolean = true,
    val disableRendering: Boolean = false,
    val disableAcceleration: Boolean = false,
    val snapshotToBoot: String? = null
  )
}
