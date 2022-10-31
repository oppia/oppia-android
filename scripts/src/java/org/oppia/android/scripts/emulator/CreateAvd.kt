package org.oppia.android.scripts.emulator

import java.io.File
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.required
import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.FileUtils.computeCommonBase
import org.oppia.android.scripts.common.FileUtils.loadProtoFile
import org.oppia.android.scripts.common.FileUtils.openFile
import org.oppia.android.scripts.proto.DeviceHardwareProfiles

fun main(args: Array<String>) {
  // TODO: Maybe consolidate options with other emulator scripts?
  CreateAvd(CommandExecutorImpl.BuilderImpl.FactoryImpl()).run {
    parse(args)
    createAvd()
  }
}

class CreateAvd(
  private val commandExecutorBuilderFactory: CommandExecutor.Builder.Factory
): ArgParser("//scripts:create_avd") {
  private val avdBundlePath by option(
    ArgType.String,
    fullName = "avd-bundle-path",
    description = "Specifies the location for saving the packaged version of the newly created AVD."
  ).required()

  private val systemImagePaths by option(
    ArgType.String,
    fullName = "system-image-paths",
    description = "Specifies the comma-separated list of the system image files that should be" +
      " included for emulation."
  ).required()

  private val hardwareProfilesProtoPath by option(
    ArgType.String,
    fullName = "hardware-profiles-proto",
    description = "Specifies the path to the hardware profiles definition binary proto file."
  ).required()

  private val hardwareProfileName by option(
    ArgType.String,
    fullName = "hardware-profile",
    description = "Specifies which device configuration hardware profile should be used."
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

  private val disableAcceleration by option(
    ArgType.Boolean,
    fullName = "disable-acceleration",
    description = "If present, indicates that the emulator should not use hardware acceleration."
  ).default(false)

  private val avdName by lazy { File(avdBundlePath).nameWithoutExtension }

  private val systemImageDir by lazy {
    systemImagePaths.split(',').map { File(it) }.computeCommonBase()
  }

  private val hardwareProfiles by lazy {
    loadProtoFile(
      hardwareProfilesProtoPath, DeviceHardwareProfiles.getDefaultInstance()
    ).deviceHardwareProfileList.associateBy { it.name }
  }

  private val emulatorBinary by lazy {
    openFile(emulatorBinaryPath) { "No emulator at path: ${it.absolutePath}." }
  }
  private val adbBinary by lazy {
    openFile(adbBinaryPath) { "No ADB at path: ${it.absolutePath}." }
  }
  private val adbClient by lazy { AdbClient(adbBinary, commandExecutorBuilderFactory) }
  private val avdClient by lazy {
    AvdClient(emulatorBinary, adbClient, commandExecutorBuilderFactory, systemImageDir)
  }

  fun createAvd() {
    println(
      "Creating a new AVD (Android Virtual Device) for emulation" +
        "${if (disableAcceleration) " (without hardware acceleration)" else ""}."
    )
    val hardwareProfile = checkNotNull(hardwareProfiles[hardwareProfileName]) {
      "Invalid profile: $hardwareProfileName (expected one of: ${hardwareProfiles.keys})."
    }
    try {
      avdClient.createAvd(avdName, hardwareProfile)
      avdClient.createAvdEmulatorSnapshot(avdName, disableAcceleration)
      avdClient.packAvd(avdName, File(avdBundlePath))
    } finally {
      avdClient.cleanUp()
    }
    println("AVD has been fully created and is packaged for fast-load emulation.")
  }
}
