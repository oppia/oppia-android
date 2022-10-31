package org.oppia.android.scripts.emulator

import java.io.File
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.required
import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.FileUtils.computeCommonBase
import org.oppia.android.scripts.common.FileUtils.openFile
import org.oppia.android.scripts.common.FileUtils.toAbsoluteNormalizedFile

fun main(args: Array<String>) {
  // TODO: Maybe consolidate options with other emulator scripts?
  RunEmulator(CommandExecutorImpl.BuilderImpl.FactoryImpl()).run {
    parse(args)
    runEmulator()
  }
}

class RunEmulator(
  private val commandExecutorBuilderFactory: CommandExecutor.Builder.Factory
): ArgParser("//scripts:run_emulator") {
  private val avdBundlePath by option(
    ArgType.String,
    fullName = "avd-bundle-path",
    description = "Specifies the location of the bundled AVD to load."
  ).required()

  private val workingPath by option(
    ArgType.String,
    fullName = "working-path",
    description = "Specifies the working directory for the emulator's expanded AVD files."
  ).required()

  private val systemImagePaths by option(
    ArgType.String,
    fullName = "system-image-paths",
    description = "Specifies the comma-separated list of the system image files that should be" +
      " included for emulation."
  ).required()

  private val emulatorBinaryPath by option(
    ArgType.String,
    fullName = "emulator-binary-path",
    description = "Specifies the location of the emulator executable included as part of the" +
      " Android SDK."
  ).required()

  private val adbBinaryPath by option(
    ArgType.String,
    fullName = "adb-binary-path",
    description = "Specifies the location of the ADB executable included as part of the" +
      " Android SDK."
  ).required()

  private val disabledRendering by option(
    ArgType.Boolean,
    fullName = "disable-rendering",
    description = "If present, indicates that the emulator should not render to an active window."
  ).default(false)

  private val disableAcceleration by option(
    ArgType.Boolean,
    fullName = "disable-acceleration",
    description = "If present, indicates that the emulator should not use hardware acceleration."
  ).default(false)

  private val disableFastboot by option(
    ArgType.Boolean,
    fullName = "disable-fastboot",
    description = "If present, disables fast-booting from a restored snapshot."
  ).default(false)

  private val systemImageDir by lazy {
    systemImagePaths.split(',').map { File(it) }.computeCommonBase()
  }

  private val emulatorBinary by lazy {
    openFile(emulatorBinaryPath) { "No emulator at path: ${it.absolutePath}." }
  }
  private val adbBinary by lazy {
    openFile(adbBinaryPath) { "No ADB at path: ${it.absolutePath}." }
  }
  private val adbClient by lazy { AdbClient(adbBinary, commandExecutorBuilderFactory) }
  private val avdClient by lazy {
    AvdClient(
      emulatorBinary,
      adbClient,
      commandExecutorBuilderFactory,
      systemImageDir,
      sdkRoot = File(workingPath)
    )
  }
  private val avdBundle by lazy {
    openFile(avdBundlePath) {
      "No AVD bundle at path: ${it.absolutePath}."
    }.toAbsoluteNormalizedFile()
  }
  private val avdName by lazy { avdBundle.nameWithoutExtension }

  fun runEmulator() {
    avdClient.unpackAvd(avdName, avdBundle)

    println("Starting emulator and waiting for it to start...")
    val emulatorSession =
      avdClient.startEmulator(
        avdName, disabledRendering, disableAcceleration, fastboot = !disableFastboot
      )

    println("Emulator should now be started on port ${emulatorSession.device.port}.")

    // Note that avdClient isn't cleaned up since the emulator will still be running, so the SDK
    // files can't be deleted.
  }
}
