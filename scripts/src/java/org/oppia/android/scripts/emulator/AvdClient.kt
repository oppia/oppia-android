package org.oppia.android.scripts.emulator

import de.waldheinz.fs.fat.FatType
import de.waldheinz.fs.fat.SuperFloppyFormatter
import de.waldheinz.fs.util.FileDisk
import java.io.File
import org.anarres.parallelgzip.ParallelGZIPInputStream
import org.anarres.parallelgzip.ParallelGZIPOutputStream
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.FileUtils.createTempDirectory
import org.oppia.android.scripts.common.FileUtils.loadAsProperties
import org.oppia.android.scripts.common.FileUtils.openFile
import org.oppia.android.scripts.common.FileUtils.storePropertiesTo
import org.oppia.android.scripts.common.FileUtils.toAbsoluteNormalizedPath
import org.oppia.android.scripts.common.TaskRunner.executorService
import org.oppia.android.scripts.proto.DeviceHardwareProfile

private const val FAST_BOOT_SNAPSHOT_NAME = "fast_load_snapshot"

class AvdClient(
  private val emulatorBinary: File,
  private val adbClient: AdbClient,
  private val commandExecutorBuilderFactory: CommandExecutor.Builder.Factory,
  private val systemImageDir: File,
  private val sdkRoot: File = createTempDirectory(prefix = "sdk-root-working-dir")
) {
  private val avdHomeRoot by lazy {
    File(sdkRoot, "avd").also {
      check(it.exists() || it.mkdirs()) { "Failed to ensure AVD home exists: ${it.absolutePath}." }
    }
  }
  private val emulatorClient by lazy {
    EmulatorClient(emulatorBinary, adbClient, sdkRoot, commandExecutorBuilderFactory)
  }

  fun createAvd(avdName: String, hardwareProfile: DeviceHardwareProfile) {
    val avdRoot = openFile(avdHomeRoot, avdName, verifyDoesExist = false) {
      "Expected to be passed new AVD root, but received: ${it.absolutePath}."
    }

    println("AVD doesn't exist yet...creating a new one.")
    check(avdRoot.mkdirs()) { "Failed to create AVD root directory: ${avdRoot.absolutePath}." }

    val avd = AndroidVirtualDevice.createFromHardwareProfile(hardwareProfile)
    println("Using the following device profile for the virtual device:")
    println("- Configuration name: ${avd.hardwareName}")
    println("- Diagonal screen size: ${avd.diagonalSizeInches} inches")
    println("- Resolution: ${avd.resolutionWidthPx}x${avd.resolutionHeightPx}")
    println("- Aspect ratio: ${avd.aspectRatio.first}:${avd.aspectRatio.second}")
    println("- Display density: ${avd.displayDensityPpi} ppi")
    println("- Forced density (for emulator compatibility): ${avd.emulatorDensityPpi} ppi")
    println("- Density qualifier bucket (per forced density): ${avd.densityQualifier}")
    println("- SD Card with size: ${avd.sdCardSizeMebibytes} mebibytes (MiB)")
    println("- System image root: ${systemImageDir.path}")
    println("- AVD root: ${avdRoot.absolutePath}")
    println()

    val sourceProperties = openFile(systemImageDir, "source.properties") {
      "Missing source.properties in system image directory: ${it.absolutePath}."
    }.loadAsProperties()
    val imageType = sourceProperties.getValue("SystemImage.TagId").also {
      check(it == "default" || it == "google_apis") {
        "Expected one of: default, google_apis for tag IDs (encountered: $it)."
      }
    }
    val architecture = sourceProperties.getValue("SystemImage.Abi")
    val sdkLevel = sourceProperties.getValue("AndroidVersion.ApiLevel").toInt()

    // Simulate AvdManager by creating the necessary property .ini files needed for emulation.
    // TODO: Maybe customize the product or model values to pass along build graph image info?
    val configProperties = mapOf(
      "PlayStore.enabled" to (imageType == "google_apis").toString(),
      "abi.type" to architecture,
      "avd.ini.encoding" to "UTF-8",
      "hw.arc" to "false",
      "hw.cpu.arch" to architecture,
      "image.sysdir.1" to systemImageDir.absolutePath,
      "image.sysdir.1.rel" to systemImageDir.path,
      "tag.display" to "",
      "tag.id" to imageType,
      "hw.accelerometer" to "yes",
      "hw.audioInput" to "yes",
      "hw.battery" to "yes",
      "hw.dPad" to "no",
      "hw.gps" to "yes",
      "hw.lcd.density" to avd.emulatorDensityPpi.toString(),
      "hw.lcd.width" to avd.resolutionWidthPx.toString(),
      "hw.lcd.height" to avd.resolutionHeightPx.toString(),
      "hw.mainKeys" to "no",
      "hw.sdCard" to "yes",
      "sdcard.size" to "${avd.sdCardSizeMebibytes}M",
      "hw.sensors.orientation" to "yes",
      "hw.sensors.proximity" to "yes",
      "hw.trackBall" to "no"
    )
    val avdProperties = mapOf(
      "avd.ini.encoding" to "UTF-8",
      "path" to avdRoot.absolutePath,
      "target" to "android-$sdkLevel"
    )
    configProperties.storePropertiesTo(File(avdRoot, "config.ini"))
    avdProperties.storePropertiesTo(File(avdHomeRoot, "$avdName.ini"))

    openFile(systemImageDir, "userdata.img") {
      "Missing expected userdata.img in system image directory: ${it.absolutePath}."
    }.copyTo(openFile(avdRoot, "userdata.img", verifyDoesExist = false) {
      "userdata.img unexpectedly already exists in destination: ${it.absolutePath}."
    })

    openFile(avdRoot, "sdcard.img", verifyDoesExist = false) {
      "sdcard.img unexpectedly already exists in destination: ${it.absolutePath}."
    }.also {
      val sdCardSizeBytes = avd.sdCardSizeMebibytes * 1024 * 1024
      val device = FileDisk.create(it, sdCardSizeBytes)
      SuperFloppyFormatter.get(device).setFatType(FatType.FAT32).format()
    }
  }

  fun createAvdEmulatorSnapshot(avdName: String, disableAcceleration: Boolean) {
    println("Starting emulator and waiting for it to fully boot...")
    val emulatorSession = emulatorClient.startEmulator(
      EmulatorClient.EmulatorStartupSettings(
        avdName,
        disableSnapshotLoad = true,
        disableRendering = true,
        disableAcceleration = disableAcceleration
      )
    )

    println("Creating snapshot of the current emulator state (for later fast-loading).")
    emulatorSession.createSnapshot(FAST_BOOT_SNAPSHOT_NAME)

    println("Ending emulator session.")
    emulatorSession.kill()
  }

  fun packAvd(avdName: String, destArchive: File) {
    val avdRoot = openFile(avdHomeRoot, avdName) {
      "Expected to be passed an AVD name that's already been created, per $avdName directory does" +
        " not exist: ${it.absolutePath}."
    }

    // First, remove some unnecessary files for better filesystem management (and faster gzip).
    File(avdRoot, "snapshots/default_boot").deleteRecursively()
    File(avdRoot, "tmpAdbCmds").deleteRecursively()
    avdRoot.listFiles()?.filter { it.extension in listOf("txt", "lock") }?.forEach(File::delete)

    println("Packing AVD into a single file for faster file operations...")
    TarArchiveOutputStream(
      ParallelGZIPOutputStream(destArchive.outputStream(), executorService)
    ).use { output ->
      avdRoot.walk().forEach { file ->
        val entry = output.createArchiveEntry(file, file.toRelativeString(avdRoot))
        output.putArchiveEntry(entry)
        if (file.isFile) {
          file.inputStream().use { fileStream -> fileStream.copyTo(output) }
        }
        output.closeArchiveEntry()
      }
    }
  }

  fun unpackAvd(avdName: String, destArchive: File) {
    val avdRoot = File(avdHomeRoot, avdName)
    if (!avdRoot.exists()) {
      check(avdRoot.mkdirs()) { "Failed to create AVD root directory: ${avdRoot.absolutePath}." }

      println("Unpacking AVD to prepare for emulation...")
      TarArchiveInputStream(ParallelGZIPInputStream(destArchive.inputStream())).use { input ->
        generateSequence { input.nextTarEntry }.filter { !it.isDirectory }.forEach { entry ->
          File(avdRoot, entry.name).also {
            it.normalize().parentFile?.mkdirs()
          }.outputStream().use { input.copyTo(it) }
        }
      }
    }

    println("Updating AVD for relative emulation.")

    // config.ini needs to be updated to use the snapshot & work in the new directory structure.
    val configIni = openFile(avdRoot, "config.ini") {
      "Failed to open AVD's config.ini: ${it.absolutePath}."
    }
    val configProps = configIni.loadAsProperties().toMutableMap()
    configProps["image.sysdir.1"] =
      File(".", configProps.getValue("image.sysdir.1.rel")).toAbsoluteNormalizedPath()
    configProps.storePropertiesTo(configIni)

    // The top-level ini needs to be recreated so that the emulator knows where to look for the AVD.
    val sourceProperties = openFile(systemImageDir, "source.properties") {
      "Missing source.properties in system image directory: ${it.absolutePath}."
    }.loadAsProperties()
    val sdkLevel = sourceProperties.getValue("AndroidVersion.ApiLevel").toInt()
    val avdProperties = mapOf(
      "avd.ini.encoding" to "UTF-8",
      "path" to avdRoot.absolutePath,
      "target" to "android-$sdkLevel"
    )
    avdProperties.storePropertiesTo(File(avdHomeRoot, "$avdName.ini"))
  }

  fun startEmulator(
    avdName: String, disableRendering: Boolean, disableAcceleration: Boolean, fastboot: Boolean
  ): EmulatorClient.EmulatorSession {
    if (disableRendering || disableAcceleration || !fastboot) {
      val specialSettings = listOfNotNull(
        if (disableRendering) "disabled rendering" else null,
        if (disableAcceleration) "disabled hardware acceleration" else null,
        if (!fastboot) "no fastboot" else null
      )
      println("Using special emulator settings: ${specialSettings.joinToString()}")
    }
    return emulatorClient.startEmulator(
      EmulatorClient.EmulatorStartupSettings(
        avdName,
        disableSnapshotSave = true,
        disableRendering = disableRendering,
        disableAcceleration = disableAcceleration,
        snapshotToBoot = if (fastboot) FAST_BOOT_SNAPSHOT_NAME else null
      )
    )
  }

  fun cleanUp() {
    // Delete the SDK root since it's not longer needed once AVDs are packaged.
    check(sdkRoot.deleteRecursively()) {
      "Failed to delete working SDK directory: ${sdkRoot.absolutePath}."
    }
  }
}
