package org.oppia.android.scripts.apkstats

import java.io.File
import java.io.PrintStream
import java.nio.file.Files
import java.util.Locale
import java.util.Properties
import java.util.StringTokenizer
import java.util.zip.ZipFile
import org.oppia.android.scripts.apkstats.ComputeAabDifferences.Companion.AabProfile

// TODO(#1719): Add support for showing count & itemization of modified files/resources (vs. just
//  new/removed).

fun main(vararg args: String) {
  val androidSdkPath = args[0]
  val outputSummaryFilePath = args[1]
  val outputFullSummaryFilePath = args[2]

  val sdkProperties = Properties().also { props ->
    File("./scripts/sdk_info.properties").inputStream().use { props.load(it) }
  }

  val remainingArgCount = args.size - 3
  check(remainingArgCount > 0 && (remainingArgCount % 4) == 0) {
    "Expected at least 1 quartet entry of the form: <build_flavor> <old_aab_path> <new_aab_path>" +
      " <full_diff_output_file_path>"
  }
  val profiles =
    args.drop(3).chunked(4).map { (flavor, aabNoChangesPath, aabWithChangesPath, outputPath) ->
      AabProfile(
        buildFlavor = flavor,
        oldAabFilePath = aabNoChangesPath,
        newAabFilePath = aabWithChangesPath,
        fullDifferenceOutputFilePath = outputPath
      )
    }

  println(
    "NOTE: Using Android SDK path: $androidSdkPath. Computing ${profiles.size} build flavor stats" +
      " profiles."
  )

  // The working directory must be the current directory since the classpath will be relative to the
  // current directory (per how Bazel sets it up). This technically doesn't matter for apkanalyzer
  // or aapt2, but they use the same working directory for predictability & consistency.
  val aapt2Client =
    Aapt2Client(
      workingDirectoryPath = ".", androidSdkPath, sdkProperties.getProperty("build_tools_version")
    )
  val apkAnalyzerClient = ApkAnalyzerClient(workingDirectoryPath = ".", androidSdkPath)
  val bundleToolClient = BundleToolClient(workingDirectoryPath = ".")
  val computer = ComputeAabDifferences(aapt2Client, apkAnalyzerClient, bundleToolClient)
  val buildStats = computer.computeBuildStats(*profiles.toTypedArray())
  PrintStream(outputSummaryFilePath).use { stream ->
    buildStats.writeSummariesTo(stream, longSummary = false)
  }
  PrintStream(outputFullSummaryFilePath).use { stream ->
    buildStats.writeSummariesTo(stream, longSummary = true)
  }
  profiles.forEach { profile ->
    PrintStream(profile.fullDifferenceOutputFilePath).use { stream ->
      buildStats.writeCompleteFileDiffsTo(profile.buildFlavor, stream)
    }
  }
}

class ComputeAabDifferences(
  private val aapt2Client: Aapt2Client,
  private val apkAnalyzerClient: ApkAnalyzerClient,
  private val bundleToolClient: BundleToolClient
) {
  fun computeBuildStats(vararg aabProfiles: AabProfile): BuildStats {
    val aabStats = aabProfiles.map { profile ->
      profile.buildFlavor to computeAabStats(profile.oldAabFilePath, profile.newAabFilePath)
    }.toMap()
    return BuildStats(aabStats)
  }

  private fun computeAabStats(aabWithoutChangesPath: String, aabWithChangesPath: String): AabStats {
    println(
      "Computing AAB stats for AABs: $aabWithoutChangesPath (old) and $aabWithChangesPath (new)..."
    )
    val parentDestDir = Files.createTempDirectory("apk_diff_stats_").toFile()
    println("Using ${parentDestDir.absolutePath} as an intermediary working directory")
    val destWithoutChanges = parentDestDir.newDirectory("without_changes").absolutePath
    val destWithChanges = parentDestDir.newDirectory("with_changes").absolutePath

    val universalApkWithoutChanges = computeUniversalApk(aabWithoutChangesPath, destWithoutChanges)
    val universalApkWithChanges = computeUniversalApk(aabWithChangesPath, destWithChanges)

    val (masterApkWithoutChanges, splitApksWithoutChanges) =
      computeApksList(aabWithoutChangesPath, destWithoutChanges)
    val (masterApkWithChanges, splitApksWithChanges) =
      computeApksList(aabWithChangesPath, destWithChanges)
    val splitApkStats = combineMaps(
      splitApksWithoutChanges, splitApksWithChanges
    ) { fileWithoutChangesPath, fileWithChangesPath ->
      computeConfigurationStats(fileWithoutChangesPath, fileWithChangesPath)
    }
    val configurationsList =
      DiffList(splitApksWithoutChanges.keys.toList(), splitApksWithChanges.keys.toList())

    return AabStats(
      universalApkStats = computeConfigurationStats(
        universalApkWithoutChanges, universalApkWithChanges
      ),
      masterSplitApkStats = computeConfigurationStats(
        masterApkWithoutChanges, masterApkWithChanges
      ),
      splitApkStats = splitApkStats,
      configurationsList = configurationsList
    )
  }

  private fun computeUniversalApk(inputAabPath: String, destDir: String): String {
    println("Generating universal APK for: $inputAabPath")
    val universalApkPath = File(destDir, "universal.apk").absolutePath
    val universalApk = bundleToolClient.buildUniversalApk(inputAabPath, universalApkPath)
    return universalApk.absolutePath
  }

  private fun computeApksList(
    inputAabPath: String, destDir: String
  ): Pair<String, Map<String, String>> {
    println("Generating APK list for: $inputAabPath")
    val apksListPath = File(destDir, "list.apks").absolutePath
    val apkFiles = bundleToolClient.buildApks(inputAabPath, apksListPath, destDir)
    val masterFilePath = apkFiles.first { "master" in it.name }.absolutePath
    val apkFilePathMap =
      apkFiles.filter { "master" !in it.name }
        .associate { file ->
          file.name.substringAfter("base-").substringBefore('.') to file.absolutePath
        }
    return masterFilePath to apkFilePathMap
  }

  private fun computeConfigurationStats(
    apkWithoutChangesPath: String?, apkWithChangesPath: String?
  ): ApkConfigurationStats {
    println("Comparing APKs: $apkWithoutChangesPath and $apkWithChangesPath")
    val fullComparison = if (apkWithoutChangesPath != null && apkWithChangesPath != null) {
      apkAnalyzerClient.compare(apkWithoutChangesPath, apkWithChangesPath)
    } else null
    return ApkConfigurationStats(
      fileSizeStats = computeFileSizeStats(apkWithoutChangesPath, apkWithChangesPath),
      dexStats = computeDexStats(apkWithoutChangesPath, apkWithChangesPath),
      manifestStats = computeManifestStats(apkWithoutChangesPath, apkWithChangesPath),
      resourceStats = computeResourceStats(apkWithoutChangesPath, apkWithChangesPath),
      assetStats = computeAssetStats(apkWithoutChangesPath, apkWithChangesPath),
      completeFileDiff = fullComparison
    )
  }

  private fun computeFileSizeStats(
    apkWithoutChangesPath: String?, apkWithChangesPath: String?
  ): FileSizeStats {
    println("Computing file size for: $apkWithoutChangesPath and $apkWithChangesPath")
    val (fileSizeWithoutChanges, downloadSizeWithoutChanges) = if (apkWithoutChangesPath != null) {
      apkAnalyzerClient.computeFileSize(apkWithoutChangesPath).toLong() to
        apkAnalyzerClient.computeDownloadSize(apkWithoutChangesPath).toLong()
    } else 0L to 0L

    println("Computing estimated download size for: $apkWithoutChangesPath and $apkWithChangesPath")
    val (fileSizeWithChanges, downloadSizeWithChanges) = if (apkWithChangesPath != null) {
      apkAnalyzerClient.computeFileSize(apkWithChangesPath).toLong() to
        apkAnalyzerClient.computeDownloadSize(apkWithChangesPath).toLong()
    } else 0L to 0L

    return FileSizeStats(
      fileSize = DiffLong(oldValue = fileSizeWithoutChanges, newValue = fileSizeWithChanges),
      downloadSize = DiffLong(
        oldValue = downloadSizeWithoutChanges, newValue = downloadSizeWithChanges
      )
    )
  }

  private fun computeDexStats(
    apkWithoutChangesPath: String?, apkWithChangesPath: String?
  ): DexStats {
    println("Computing dex method counts for: $apkWithoutChangesPath and $apkWithChangesPath")
    val methodCountWithoutChanges = apkWithoutChangesPath?.let { apkPath ->
      countMethods(apkAnalyzerClient.computeDexReferencesList(apkPath))
    } ?: 0L
    val methodCountWithChanges = apkWithChangesPath?.let { apkPath ->
      countMethods(apkAnalyzerClient.computeDexReferencesList(apkPath))
    } ?: 0L
    return DexStats(
      DiffLong(oldValue = methodCountWithoutChanges, newValue = methodCountWithChanges)
    )
  }

  private fun computeManifestStats(
    apkWithoutChangesPath: String?, apkWithChangesPath: String?
  ): ManifestStats {
    println("Computing feature and permissions for: $apkWithoutChangesPath and $apkWithChangesPath")
    val (featuresWithoutChanges, permissionsWithoutChanges) = apkWithoutChangesPath?.let { path ->
      val rawFeatures = apkAnalyzerClient.computeFeatures(path)
      val rawPermissions = aapt2Client.dumpPermissions(path)
      return@let extractFeatures(rawFeatures) to extractPermissions(rawPermissions)
    } ?: listOf<String>() to listOf()
    val (featuresWithChanges, permissionsWithChanges) = apkWithChangesPath?.let { path ->
      val rawFeatures = apkAnalyzerClient.computeFeatures(path)
      val rawPermissions = aapt2Client.dumpPermissions(path)
      return@let extractFeatures(rawFeatures) to extractPermissions(rawPermissions)
    } ?: listOf<String>() to listOf()

    return ManifestStats(
      features = DiffList(featuresWithoutChanges, featuresWithChanges),
      permissions = DiffList(permissionsWithoutChanges, permissionsWithChanges)
    )
  }

  private fun computeResourceStats(
    apkWithoutChangesPath: String?, apkWithChangesPath: String?
  ): ResourceStats {
    println("Computing resource maps for: $apkWithoutChangesPath and $apkWithChangesPath")
    val resourcesWithoutChanges = apkWithoutChangesPath?.let { apkPath ->
      extractResources(aapt2Client.dumpResources(apkPath))
    } ?: mapOf()
    val resourcesWithChanges = apkWithChangesPath?.let { apkPath ->
      extractResources(aapt2Client.dumpResources(apkPath))
    } ?: mapOf()
    return ResourceStats(combineResourceMaps(resourcesWithoutChanges, resourcesWithChanges))
  }

  private fun computeAssetStats(
    apkWithoutChangesPath: String?, apkWithChangesPath: String?
  ): AssetStats {
    // Only consider top-level files in the assets/ folder.
    println("Computing asset stats for: $apkWithoutChangesPath and $apkWithChangesPath")
    return AssetStats(
      DiffList(
        File(apkWithoutChangesPath).extractAssetFileNamesFromApk(),
        File(apkWithChangesPath).extractAssetFileNamesFromApk()
      )
    )
  }

  companion object {
    private fun File.newDirectory(name: String): File {
      return File(this, name).also { it.mkdir() }
    }

    private fun <K: Any?, IV: Any, OV: Any> combineMaps(
      oldMap: Map<K, IV>, newMap: Map<K, IV>, combineValue: (IV?, IV?) -> OV
    ): Map<K, OV> {
      val allKeys = oldMap.keys + newMap.keys
      return allKeys.map { key ->
        return@map key to combineValue(oldMap[key], newMap[key])
      }.toMap()
    }

    private fun countMethods(rawMethodCountDump: List<String>): Long {
      return rawMethodCountDump.filter { ".dex" in it }
        .map { it.substringAfter(".dex").trim().toLong() }
        .sum()
    }

    private fun extractFeatures(featuresDump: List<String>): List<String> {
      return featuresDump.filter(String::isNotBlank).map { it.substringBefore(' ') }
    }

    private fun extractPermissions(permissionDump: List<String>): List<String> {
      return permissionDump.filter { line ->
        "name=" in line
      }.map { line ->
        line.substringAfter("name='").substringBefore('\'')
      }
    }

    private fun combineResourceMaps(
      oldResourceMap: Map<String, List<String>>,
      newResourceMap: Map<String, List<String>>
    ): Map<String, DiffList<String>> {
      return combineMaps(oldResourceMap, newResourceMap) { oldResources, newResources ->
        DiffList(oldResources ?: listOf(), newResources ?: listOf())
      }
    }

    private fun extractResources(resourceDump: List<String>): Map<String, List<String>> {
      val resourceMap = mutableMapOf<String, List<String>>()
      lateinit var currentResourceList: MutableList<String>
      for (line in resourceDump) {
        val tokenizer = StringTokenizer(line)
        if (!tokenizer.hasMoreTokens()) continue
        when (tokenizer.nextToken()) {
          "type" -> {
            val typeName = tokenizer.nextToken()
            currentResourceList = mutableListOf()
            resourceMap[typeName] = currentResourceList
          }
          "resource" -> {
            tokenizer.nextToken() // Skip the ID.
            val resourceName = tokenizer.nextToken()
            currentResourceList.add(resourceName)
          }
          // Otherwise, skip it since it's details about the previous resource or top-level
          // information.
        }
      }
      return resourceMap
    }

    private fun File.extractAssetFileNamesFromApk(): List<String> {
      return ZipFile(this).use { zipFile ->
        zipFile.entries().asSequence().filter { entry ->
          "asset/" in entry.name && "/" !in entry.name.substringAfter("asset/")
        }.map { it.name.substringAfter("asset/") }.toList()
      }
    }

    private fun DiffList<String>.writeTo(
      stream: PrintStream,
      linePrefix: String,
      itemize: Boolean,
      itemLimit: Int,
      listIndentation: Int = 0
    ) {
      val indent = " ".repeat(listIndentation)
      stream.print(
        "$linePrefix: $oldCount (old), $newCount (new), **$countDifference** (difference)"
      )
      if (itemize && hasDifference()) {
        stream.println(":")
        val newOldAssets = filter { it.type != DiffList.DiffType.SAME_ENTRY }
        newOldAssets.take(itemLimit).forEach { (type, assetName) ->
          stream.println("$indent- $assetName (${type.humanReadableName})")
        }
        if (newOldAssets.size > itemLimit) {
          val remaining = newOldAssets.size - itemLimit
          stream.println("$indent- And $remaining other${if (remaining > 1) "s" else ""}")
        }
      } else stream.println()
    }

    private fun DiffLong.writeCountTo(stream: PrintStream, linePrefix: String) {
      stream.println("$linePrefix: $oldValue (old), $newValue (new), **$difference** (difference)")
    }

    private fun DiffLong.writeBytesTo(stream: PrintStream, linePrefix: String) {
      stream.println(
        "$linePrefix: ${oldValue.formatAsBytes()} (old), ${newValue.formatAsBytes()} (new)," +
          " **${difference.formatAsBytes()}** (difference)"
      )
    }

    private fun Long.formatAsBytes(): String {
      return when {
        this < 10_000L -> "$this bytes"
        this < 10_000_000L -> "${this / 1024} KiB"
        this < 10_000_000_000L -> "${this / (1024 * 1024)} MiB"
        else -> "${this / (1024 * 1024 * 1024)} GiB"
      }
    }

    data class AabProfile(
      val buildFlavor: String,
      val oldAabFilePath: String,
      val newAabFilePath: String,
      val fullDifferenceOutputFilePath: String
    )

    data class BuildStats(private val aabStats: Map<String, AabStats>) {
      fun writeSummariesTo(stream: PrintStream, longSummary: Boolean) {
        stream.println("# APK & AAB differences analysis")
        if (!longSummary) {
          stream.println(
            "Note that this is a summarized snapshot. See the CI artifacts for detailed" +
              " differences."
          )
        }
        val itemLimit = if (!longSummary) 5 else Int.MAX_VALUE
        aabStats.forEach { (buildFlavor, stats) ->
          stream.println()
          stats.writeSummaryTo(stream, buildFlavor, itemLimit, longSummary)
        }
      }

      fun writeCompleteFileDiffsTo(whichFlavor: String, stream: PrintStream) {
        aabStats.getValue(whichFlavor).writeCompleteFileDiffsTo(stream)
      }
    }

    data class AabStats(
      val universalApkStats: ApkConfigurationStats,
      val masterSplitApkStats: ApkConfigurationStats,
      val splitApkStats: Map<String, ApkConfigurationStats>,
      val configurationsList: DiffList<String>
    ) {
      fun writeSummaryTo(
        stream: PrintStream, buildFlavor: String, itemLimit: Int, longSummary: Boolean
      ) {
        stream.println("## ${buildFlavor.capitalize(Locale.US)}")
        stream.println()

        stream.println("### Universal APK")
        universalApkStats.writeSummaryTo(stream, itemize = true, itemLimit)

        stream.println("### AAB differences")
        if (!longSummary) {
          stream.println("<details><summary>Expand to see AAB specifics</summary>")
          stream.println()
        }
        stream.println("Supported configurations:")
        configurationsList.forEach { (type, configuration) ->
          stream.println("- $configuration (${type.humanReadableName})")
        }
        stream.println()

        stream.println("#### Base APK")
        masterSplitApkStats.writeSummaryTo(stream, itemize = longSummary, itemLimit)

        splitApkStats.forEach { (configuration, stats) ->
          stream.println()
          stream.println("#### Configuration $configuration")
          stats.writeSummaryTo(stream, itemize = longSummary, itemLimit)
        }
        if (!longSummary) stream.println("</details>")
      }

      fun writeCompleteFileDiffsTo(stream: PrintStream) {
        universalApkStats.writeCompleteFileDiffTo(stream)
      }
    }

    data class ApkConfigurationStats(
      val fileSizeStats: FileSizeStats,
      val dexStats: DexStats,
      val manifestStats: ManifestStats,
      val resourceStats: ResourceStats,
      val assetStats: AssetStats,
      val completeFileDiff: List<String>?
    ) {
      fun writeSummaryTo(stream: PrintStream, itemize: Boolean, itemLimit: Int) {
        fileSizeStats.writeTo(stream, itemize)
        if (itemize) stream.println()

        dexStats.writeTo(stream, itemize)
        if (itemize) stream.println()

        manifestStats.writeTo(stream, itemize, itemLimit)
        if (itemize) stream.println()

        // Resources stats always has an extra blank newline (if there's anything to write) to
        // ensure that following lines aren't included as part of the list that's always written for
        // resources.
        if (resourceStats.writeTo(stream, itemize, itemLimit)) {
          stream.println()
        }

        assetStats.writeTo(stream, itemize, itemLimit)
        if (itemize) stream.println()
      }

      fun writeCompleteFileDiffTo(stream: PrintStream) {
        if (completeFileDiff != null) {
          completeFileDiff.forEach(stream::println)
        } else stream.println("APK doesn't exist.")
      }
    }

    data class FileSizeStats(val fileSize: DiffLong, val downloadSize: DiffLong) {
      fun writeTo(stream: PrintStream, itemize: Boolean) {
        fileSize.writeBytesTo(stream, "APK file size")
        if (itemize) {
          stream.println()
        }
        downloadSize.writeBytesTo(stream, "APK download size (estimated)")
      }
    }

    data class DexStats(val methodCount: DiffLong) {
      fun writeTo(stream: PrintStream, itemize: Boolean) {
        if (itemize || methodCount.hasDifference()) {
          methodCount.writeCountTo(stream, "Method count")
        }
      }
    }

    data class ManifestStats(
      val features: DiffList<String>, val permissions: DiffList<String>
    ) {
      fun writeTo(stream: PrintStream, itemize: Boolean, itemLimit: Int) {
        if (itemize || features.hasDifference()) {
          features.writeTo(stream, "Features", itemize, itemLimit)
          if (itemize) {
            stream.println()
          }
        }
        if (itemize || permissions.hasDifference()) {
          permissions.writeTo(stream, "Permissions", itemize, itemLimit)
        }
      }
    }

    data class ResourceStats(
      val resources: Map<String, DiffList<String>>
    ) {
      fun writeTo(stream: PrintStream, itemize: Boolean, itemLimit: Int): Boolean {
        val totalOldCount = resources.values.map { it.oldCount }.sum()
        val totalNewCount = resources.values.map { it.newCount }.sum()
        val totalDifference = totalNewCount - totalOldCount
        if (itemize || totalDifference != 0) {
          stream.println(
            "Resources: $totalOldCount (old), $totalNewCount (new), **$totalDifference** (difference)"
          )
          resources.forEach { (typeName, resourcesList) ->
            if (itemize || resourcesList.hasDifference()) {
              stream.print("- ")
              resourcesList.writeTo(
                stream, typeName.capitalize(Locale.US), itemize, itemLimit, listIndentation = 2
              )
            }
          }
          return true
        }
        return false
      }
    }

    data class AssetStats(val assets: DiffList<String>) {
      fun writeTo(stream: PrintStream, itemize: Boolean, itemLimit: Int) {
        if (itemize || assets.hasDifference()) {
          assets.writeTo(stream, "Lesson assets", itemize, itemLimit)
        }
      }
    }

    data class DiffLong(val oldValue: Long, val newValue: Long) {
      val difference: Long by lazy { newValue - oldValue }

      fun hasDifference(): Boolean = difference != 0L
    }

    class DiffList<T>(
      private val oldList: List<T>,
      private val newList: List<T>
    ): AbstractList<DiffList.DiffEntry<T>>() {
      private val oldSet by lazy { oldList.toSet() }
      private val newSet by lazy { newList.toSet() }
      private val combined by lazy { oldSet + newSet }
      private val processedEntries by lazy { processDiffs() }

      val oldCount: Int by lazy { oldList.size }
      val newCount: Int by lazy { newList.size }
      val countDifference: Int by lazy { newCount - oldCount }

      fun hasDifference(): Boolean = countDifference != 0

      override val size: Int
        get() = processedEntries.size

      override fun get(index: Int): DiffEntry<T> = processedEntries[index]

      private fun processDiffs(): List<DiffEntry<T>> {
        return combined.map { consideredValue ->
          val inOldList = consideredValue in oldSet
          val inNewList = consideredValue in newSet
          val diffType = if (!inOldList && inNewList) {
            DiffType.NEW_ENTRY
          } else if (inOldList && !inNewList) {
            DiffType.REMOVED_ENTRY
          } else DiffType.SAME_ENTRY
          return@map DiffEntry(diffType, consideredValue)
        }
      }

      enum class DiffType(val humanReadableName: String) {
        SAME_ENTRY("same"),
        NEW_ENTRY("added"),
        REMOVED_ENTRY("removed")
      }

      data class DiffEntry<T>(val type: DiffType, val value: T)
    }
  }
}
