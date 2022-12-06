package org.oppia.android.scripts.emulator

import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit
import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.FileUtils.toAbsoluteNormalizedFile
import org.oppia.android.scripts.common.FileUtils.toAbsoluteNormalizedPath

// TODO: Move to common?

class AdbClient(
  private val adbBinary: File,
  private val commandExecutorBuilderFactory: CommandExecutor.Builder.Factory
) {
  private val adbBinaryPath by lazy { adbBinary.toAbsoluteNormalizedPath() }

  private val commandExecutor by lazy {
    // The working directory doesn't generally matter for ADB commands.
    commandExecutorBuilderFactory.createBuilder()
      .setProcessTimeout(timeout = 15, timeoutUnit = TimeUnit.MINUTES)
      .create(workingDirectory = File("."))
  }

  fun findOpenEmulatorPort(): Int? {
    return (AVAILABLE_EMULATOR_PORTS - listEmulators().map { it.port }.toSet()).minOrNull()
  }

  fun findEmulatorWithPort(port: Int): AndroidDevice.Emulator? =
    listEmulators().find { it.port == port }

  fun listEmulators(): List<AndroidDevice.Emulator> =
    listDevices().filterIsInstance<AndroidDevice.Emulator>()

  fun listDevices(): List<AndroidDevice> {
    return runAdbCommand("devices", "-l").also {
      check(it.first() == "List of devices attached") {
        "ADB command's first line was unexpected: ${it.first()}"
      }
    }.drop(1).filter { it.trim().isNotEmpty() }.map { parseAndroidDeviceLine(it) }
  }

  fun findDeviceWithSerial(serialName: String): AndroidDevice? =
    listDevices().find { it.serialName == serialName }

  fun runShellCommand(
    androidDevice: AndroidDevice,
    shellCommand: String,
    vararg commandArgs: String,
    commandWrapper: ShellCommandWrapper? = null,
    allowToRunInAnyState: Boolean = false
  ): List<String> {
    if (!allowToRunInAnyState && commandWrapper !is ShellCommandWrapper.WaitForProcess) {
      require(androidDevice.connectionState == ConnectionState.CONNECTED) {
        "Can only run shells command on a fully connected device, not: $androidDevice."
      }
    }
    return when (commandWrapper) {
      is ShellCommandWrapper.AppProcess -> {
        runAdbCommandOn(
          androidDevice,
          "shell",
          "app_process",
          "-cp",
          commandWrapper.classpath,
          "/unused",
          commandWrapper.mainClass,
          shellCommand,
          *commandArgs
        )
      }
      ShellCommandWrapper.WaitForProcess ->
        runAdbCommandOn(androidDevice, "wait-for-device", "shell", shellCommand, *commandArgs)
      null -> runAdbCommandOn(androidDevice, "shell", shellCommand, *commandArgs)
    }
  }

  fun getProperty(androidDevice: AndroidDevice, name: String): String =
    runShellCommand(androidDevice, "getprop", name).single()

  fun setProperty(androidDevice: AndroidDevice, name: String, value: String) {
    runShellCommand(androidDevice, "setprop", name, value)
  }

  fun fetchInstalledPackages(androidDevice: AndroidDevice): List<String> {
    return runPackageManagerCommand(androidDevice, "list", "packages").filter {
      it.startsWith("package:")
    }.map { it.removePrefix("package:") }
  }

  fun runPackageManagerCommand(
    androidDevice: AndroidDevice, pmCommand: String, vararg commandArgs: String
  ): List<String> = runShellCommand(androidDevice, "pm", pmCommand, *commandArgs)

  fun runActivityManagerCommand(
    androidDevice: AndroidDevice,
    amCommand: String,
    vararg commandArgs: String,
    commandWrapper: ShellCommandWrapper? = null
  ): List<String> =
    runShellCommand(androidDevice, "am", amCommand, *commandArgs, commandWrapper = commandWrapper)

  fun runInstrumentation(
    androidDevice: AndroidDevice,
    testOptions: Map<String, String>,
    testPackageName: String,
    testRunnerQualifiedClassName: String,
    waitForResult: Boolean = true,
    commandWrapper: ShellCommandWrapper? = null
  ): List<String> {
    val args = listOfNotNull(if (waitForResult) "-w" else null) +
      testOptions.flatMap { listOf("-e", it.key, it.value) }
    return runActivityManagerCommand(
      androidDevice,
      "instrument",
      *args.toTypedArray(),
      "$testPackageName/$testRunnerQualifiedClassName",
      commandWrapper = commandWrapper
    )
  }

  fun runEmulatorCommand(
    emulatorDevice: AndroidDevice.Emulator, command: String, vararg arguments: String
  ): List<String> = runAdbCommandOn(emulatorDevice, "emu", command, *arguments)

  fun installApk(device: AndroidDevice, apkFile: File, replaceApk: Boolean = false) {
    val args = listOfNotNull("install", if (replaceApk) "-r" else null, apkFile.absolutePath)
    runAdbCommandOn(device, *args.toTypedArray())
  }

  fun forceInstallApk(device: AndroidDevice, appPackage: String, apkFile: File) {
    if (isApkInstalled(device, appPackage)) {
      uninstallApk(device, appPackage)
    }
    installApk(device, apkFile, replaceApk = true)
  }

  fun uninstallApk(device: AndroidDevice, appPackage: String) {
    runAdbCommandOn(device, "uninstall", appPackage)
  }

  fun isApkInstalled(device: AndroidDevice, appPackage: String): Boolean =
    appPackage in fetchInstalledPackages(device)

  // TODO: Implement script for piecewise screen recording & pulling.
  fun recordScreen(androidDevice: AndroidDevice, timeLimit: Long, timeLimitUnit: TimeUnit) {
    val randomFileName = "${UUID.randomUUID()}.mp4"
    val tempVideoFile = File.createTempFile(/* prefix= */ "screen_recording_", /* suffix= */ ".mp4")
    val timeLimitSeconds = timeLimitUnit.toSeconds(timeLimit)
    require(timeLimitSeconds in 1..30) {
      "Can only record for between 1 and 30 seconds (inclusively), not: $timeLimitSeconds."
    }
    val onDeviceRecordedFile = "/sdcard/$randomFileName"

    // Record for as long as requested, then pull the file.
    runShellCommand(
      androidDevice,
      "screenrecord",
      "--time-limit",
      timeLimitSeconds.toString(),
      onDeviceRecordedFile
    )
    pullFile(androidDevice, onDeviceRecordedFile, tempVideoFile)

    // Delete the file on the device, and locally, once the data is retrieved.
    val videoDataBlob = tempVideoFile.readBytes()
    runShellCommand(androidDevice, "rm", onDeviceRecordedFile)
    check(tempVideoFile.delete()) { "Failed to delete pulled video file: $tempVideoFile." }
  }

  fun pullFile(androidDevice: AndroidDevice, devicePath: String, destinationFile: File) {
    runAdbCommandOn(androidDevice, "pull", devicePath, destinationFile.toAbsoluteNormalizedPath())
  }

  private fun parseAndroidDeviceLine(line: String): AndroidDevice {
    val parts = line.split(" +".toRegex()).toMutableList()
    val serialName = parts.removeFirst()
    val connectionState = when (val stateName = parts.removeFirst()) {
      "device" -> ConnectionState.CONNECTED
      "offline" -> ConnectionState.OFFLINE
      "no" -> {
        check(parts.removeFirstOrNull() == "permissions") {
          "Expected 'no permissions' state for device line: '$line'."
        }
        ConnectionState.NO_PERMISSIONS
      }
      else -> error("Unexpected state in device line: '$line' (parsed state: $stateName).")
    }
    // TODO: Use these properties.
    val properties = parts.associate {
      val (key, value) = it.split(':')
      return@associate key to value
    }
    return if (serialName.startsWith("emulator")) {
      val port =
        checkNotNull(EMULATOR_NAME_PATTERN.matchEntire(serialName)) {
          "Invalid emulator name in device line: '$line'."
        }.groupValues[1].toInt()
      AndroidDevice.Emulator(connectionState, port)
    } else AndroidDevice.RealDevice(serialName, connectionState)
  }

  private fun runAdbCommandOn(device: AndroidDevice, vararg arguments: String): List<String> =
    runAdbCommand("-s", device.serialName, *arguments)

  private fun runAdbCommand(vararg arguments: String): List<String> {
    val result = commandExecutor.executeCommandInForeground(
      adbBinaryPath,
      *arguments,
      stderrRedirection = CommandExecutor.OutputRedirectionStrategy.TRACK_AS_OUTPUT
    )
    check(result.exitCode == 0) {
      "ADB command had unexpected exit code: ${result.exitCode}\nCommand:" +
        " ${result.commandLine}\nOutput:\n${result.outputLines}"
    }
    return result.output
  }

  sealed class ShellCommandWrapper {
    object WaitForProcess: ShellCommandWrapper()

    data class AppProcess(val classpath: String, val mainClass: String): ShellCommandWrapper()
  }

  sealed class AndroidDevice {
    abstract val serialName: String
    abstract val connectionState: ConnectionState

    data class RealDevice(
      override val serialName: String, override val connectionState: ConnectionState
    ): AndroidDevice()

    // TODO: Add identifier extraction (maybe needs an Oppia-specific emulator subclass?).
    data class Emulator(
      override val connectionState: ConnectionState,
      val port: Int
    ): AndroidDevice() {
      override val serialName: String
        get() = "emulator-$port"
    }
  }

  enum class ConnectionState {
    OFFLINE,
    CONNECTED,
    NO_PERMISSIONS
  }

  private companion object {
    private val EMULATOR_NAME_PATTERN = "emulator-(\\d+)".toRegex()

    // TODO: Compute this?
    private val AVAILABLE_EMULATOR_PORTS =
      setOf(
        5554,
        5556,
        5558,
        5560,
        5562,
        5564,
        5566,
        5568,
        5570,
        5572,
        5574,
        5576,
        5578,
        5580,
        5582,
        5584
      )
  }
}
