package org.oppia.android.scripts.apkstats

import org.oppia.android.scripts.common.AndroidBuildSdkProperties
import java.io.File
import java.io.PrintStream
import java.nio.file.Files
import java.util.Locale
import java.util.StringTokenizer
import java.util.zip.ZipFile

// TODO(#1719): Add support for showing count & itemization of modified files/resources (vs. just
//  new/removed).

/**
 * The main entrypoint for analyzing different builds of the app and computing stat differences.
 *
 * Usage:
 *   bazel run //scripts:compute_aab_differences -- \\
 *     <path_to_brief_summary_output_file> <path_to_full_summary_output_file> \\
 *     [ <build_flavor> <path_to_flavor_input_without_changes.aab> \\
 *       <path_to_flavor_input_with_changes.aab> ] ...
 *
 * Arguments:
 * - path_to_brief_summary_output_file: path to the file that will contain a brief difference
 *     summary.
 * - path_to_full_summary_output_file: path to the file that will contain a more detailed difference
 *     summary.
 * - One or more triplets containing:
 *   - build_flavor: the flavor of the build corresponding to this quartet (e.g. alpha).
 *   - path_to_flavor_input_without_changes: path to the built AAB for this flavor that doesn't
 *       contain the changes to analyze (e.g. built on develop).
 *   - path_to_flavor_input_with_changes: path to the built AAB for this flavor that includes the
 *       changes to analyze.
 *
 * Example:
 *   bazel run //scripts:compute_aab_differences -- \\
 *     $(pwd)/brief_build_summary.log $(pwd)/full_build_summary.log \\
 *     dev $(pwd)/dev_no_changes.aab $(pwd)/dev_with_changes.aab \\
 *     alpha $(pwd)/alpha_no_changes.aab $(pwd)/alpha_with_changes.aab
 */
fun main(vararg args: String) {
  val outputSummaryFilePath = args[0]
  val outputFullSummaryFilePath = args[1]

  val remainingArgCount = args.size - 2
  check(remainingArgCount > 0 && (remainingArgCount % 3) == 0) {
    "Expected at least 1 triplet entry of the form: <build_flavor> <old_aab_path> <new_aab_path>"
  }
  val profiles =
    args.drop(2).chunked(3).map { (flavor, aabNoChangesPath, aabWithChangesPath) ->
      ComputeAabDifferences.AabProfile(
        buildFlavor = flavor,
        oldAabFilePath = aabNoChangesPath,
        newAabFilePath = aabWithChangesPath
      )
    }

  println("NOTE: Computing ${profiles.size} build flavor stats profiles.")

  val workingDirectoryPath = "."
  val sdkProperties = AndroidBuildSdkProperties()
  val aapt2Client = Aapt2Client(workingDirectoryPath, sdkProperties.buildToolsVersion)
  val apkAnalyzerClient = ApkAnalyzerClient(aapt2Client)
  val bundleToolClient = BundleToolClient(workingDirectoryPath)
  val computer = ComputeAabDifferences(aapt2Client, apkAnalyzerClient, bundleToolClient)
  val buildStats = computer.computeBuildStats(*profiles.toTypedArray())
  PrintStream(outputSummaryFilePath).use { stream ->
    buildStats.writeSummariesTo(stream, longSummary = false)
  }
  PrintStream(outputFullSummaryFilePath).use { stream ->
    buildStats.writeSummariesTo(stream, longSummary = true)
  }
}

/** Utility to compute the build differences between sets of AABs. */
class ComputeAabDifferences(
  private val aapt2Client: Aapt2Client,
  private val apkAnalyzerClient: ApkAnalyzerClient,
  private val bundleToolClient: BundleToolClient
) {
  /**
   * Returns the [BuildStats] for the provided set of [AabProfile]s. All profiles will be
   * represented in the returned stats.
   */
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
    inputAabPath: String,
    destDir: String
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
    apkWithoutChangesPath: String?,
    apkWithChangesPath: String?
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
    apkWithoutChangesPath: String?,
    apkWithChangesPath: String?
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
    apkWithoutChangesPath: String?,
    apkWithChangesPath: String?
  ): DexStats {
    println("Computing dex method counts for: $apkWithoutChangesPath and $apkWithChangesPath")
    val methodCountWithoutChanges = apkWithoutChangesPath?.let { apkPath ->
      apkAnalyzerClient.computeDexReferencesList(apkPath).values.sum().toLong()
    } ?: 0L
    val methodCountWithChanges = apkWithChangesPath?.let { apkPath ->
      apkAnalyzerClient.computeDexReferencesList(apkPath).values.sum().toLong()
    } ?: 0L
    return DexStats(
      DiffLong(oldValue = methodCountWithoutChanges, newValue = methodCountWithChanges)
    )
  }

  private fun computeManifestStats(
    apkWithoutChangesPath: String?,
    apkWithChangesPath: String?
  ): ManifestStats {
    println("Computing feature and permissions for: $apkWithoutChangesPath and $apkWithChangesPath")
    val (featuresWithoutChanges, permissionsWithoutChanges) = apkWithoutChangesPath?.let { path ->
      val features = apkAnalyzerClient.computeFeatures(path)
      val rawPermissions = aapt2Client.dumpPermissions(path)
      return@let features to extractPermissions(rawPermissions)
    } ?: listOf<String>() to listOf()
    val (featuresWithChanges, permissionsWithChanges) = apkWithChangesPath?.let { path ->
      val features = apkAnalyzerClient.computeFeatures(path)
      val rawPermissions = aapt2Client.dumpPermissions(path)
      return@let features to extractPermissions(rawPermissions)
    } ?: listOf<String>() to listOf()

    return ManifestStats(
      features = DiffList(featuresWithoutChanges, featuresWithChanges),
      permissions = DiffList(permissionsWithoutChanges, permissionsWithChanges)
    )
  }

  private fun computeResourceStats(
    apkWithoutChangesPath: String?,
    apkWithChangesPath: String?
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
    apkWithoutChangesPath: String?,
    apkWithChangesPath: String?
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

  /**
   * Represents the AABs corresponding to a single build flavor.
   *
   * @property buildFlavor the name of the build flavor
   * @property oldAabFilePath the path to the AAB build of this flavor that doesn't include changes
   *     to analyze
   * @property newAabFilePath the path to the AAB build of this flavor that includes changes to
   *     analyze
   */
  data class AabProfile(
    val buildFlavor: String,
    val oldAabFilePath: String,
    val newAabFilePath: String
  )

  /**
   * Represents the computed build stats for multiple build flavors.
   *
   * @property aabStats a map from build flavor to [AabStats] corresponding to a list of
   *     [AabProfile]s from and for which build stats were computed
   */
  data class BuildStats(val aabStats: Map<String, AabStats>) {
    /**
     * Writes the build stats summary to [stream] with a configurable length using [longSummary].
     */
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
  }

  /**
   * Difference stats between two AABs for a single build flavor.
   *
   * @property universalApkStats stats corresponding to the before & after universal APKs built for
   *     the considered build flavor
   * @property masterSplitApkStats stats corresponding to the base APK of the compared AABs for the
   *     considered build flavor
   * @property splitApkStats a map from configuration name to the compared stats for each split APK
   *     of the compared AABs for the considered build flavor
   * @property configurationsList a difference list to compare the available AAB configurations for
   *     both AABs of the considered build flavor
   */
  data class AabStats(
    val universalApkStats: ApkConfigurationStats,
    val masterSplitApkStats: ApkConfigurationStats,
    val splitApkStats: Map<String, ApkConfigurationStats>,
    val configurationsList: DiffList<String>
  ) {
    /**
     * Writes the stats summary between two AABs to [stream].
     *
     * @param buildFlavor the build flavor corresponding to the compared AABs
     * @param itemLimit the max number of items to include in expanded lists (only used if
     *     [longSummary] is true)
     * @param longSummary whether to print a more detailed summary
     */
    fun writeSummaryTo(
      stream: PrintStream,
      buildFlavor: String,
      itemLimit: Int,
      longSummary: Boolean
    ) {
      stream.println("## ${buildFlavor.capitalize(Locale.US)}")
      stream.println()

      if (!longSummary) {
        stream.println("<details><summary>Expand to see flavor specifics</summary>")
        stream.println()
      }

      stream.println("### Universal APK")
      universalApkStats.writeSummaryTo(stream, itemize = true, longSummary, itemLimit)

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
      masterSplitApkStats.writeSummaryTo(stream, itemize = longSummary, longSummary, itemLimit)

      splitApkStats.forEach { (configuration, stats) ->
        stream.println()
        stream.println("#### Configuration $configuration")
        stats.writeSummaryTo(stream, itemize = longSummary, longSummary, itemLimit)
      }
      if (!longSummary) stream.println("</details></details>")
    }
  }

  /**
   * Enumerated stats demonstrating the high-level differences between two APKs.
   *
   * @property fileSizeStats file stats comparison between the two APKs
   * @property dexStats dex stats comparison between the two APKs
   * @property manifestStats manifest stats comparison between the two APKs
   * @property resourceStats resource stats comparison between the two APKs
   * @property assetStats asset stats comparison between the two APKs
   * @property completeFileDiff the lines of the full comparison output between the two APKs, or
   *     null if none is computed for this APK
   */
  data class ApkConfigurationStats(
    val fileSizeStats: FileSizeStats,
    val dexStats: DexStats,
    val manifestStats: ManifestStats,
    val resourceStats: ResourceStats,
    val assetStats: AssetStats,
    val completeFileDiff: List<String>?
  ) {
    /**
     * Writes the stats summary between two APKs to [stream].
     *
     * @param itemLimit the max number of items to include in expanded lists
     * @param longSummary whether extra details should be included in the summary
     * @param itemize whether to expand lists of items
     */
    fun writeSummaryTo(
      stream: PrintStream,
      itemize: Boolean,
      longSummary: Boolean,
      itemLimit: Int
    ) {
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

      if (longSummary) {
        stream.println("*Detailed file differences:*")
        if (completeFileDiff != null) {
          completeFileDiff.forEach(stream::println)
        } else stream.println("APK doesn't exist.")
        stream.println()
      }
    }
  }

  /**
   * File size stats between two APKs.
   *
   * @property fileSize the difference in file size between the two APKs
   * @property downloadSize the difference in estimated download size between the two APKs. Note
   *     that the download size for an AAB is likely the base APK download size + the matching
   *     configuration split APK download size (though this is again an estimate and may vary).
   */
  data class FileSizeStats(val fileSize: DiffLong, val downloadSize: DiffLong) {
    /**
     * Writes the file stats summary between two APKs to [stream].
     *
     * @param itemize whether to expand lists of items
     */
    fun writeTo(stream: PrintStream, itemize: Boolean) {
      fileSize.writeBytesTo(stream, "APK file size")
      if (itemize) {
        stream.println()
      }
      downloadSize.writeBytesTo(stream, "APK download size (estimated)")
    }
  }

  /**
   * Dex stats between two APKs.
   *
   * @property methodCount the difference in total dex methods between the two APks. These
   *     correspond to declared methods in Kotlin that are being compiled and included in the APK's
   *     dex files.
   */
  data class DexStats(val methodCount: DiffLong) {
    /**
     * Writes the dex stats summary between two APKs to [stream].
     *
     * @param itemize whether to expand lists of items
     */
    fun writeTo(stream: PrintStream, itemize: Boolean) {
      if (itemize || methodCount.hasDifference()) {
        methodCount.writeCountTo(stream, "Method count")
      }
    }
  }

  /**
   * Manifest stats between two APks.
   *
   * @property features the difference list of features between the two APks. These correspond to
   *     properties that affect who has access to install the app from the Play Store.
   * @property permissions the difference list of permissions required between the two APKs. This
   *     will include both permissions automatically granted by the system and those that require
   *     user consent on L+ devices.
   */
  data class ManifestStats(
    val features: DiffList<String>,
    val permissions: DiffList<String>
  ) {
    /**
     * Writes the manifest stats summary between two APKs to [stream].
     *
     * @param itemLimit the max number of items to include in expanded lists
     * @param itemize whether to expand lists of items
     */
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

  /**
   * Resource stats between two APKs. Note that resources include anything defined by Android as a
   * resource, and may not necessarily correspond to specific files (e.g. dimensions and strings are
   * considered separate resources despite generally being grouped in a few files).
   *
   * @property resources map from resource type to difference lists of the resource names compared
   *     between the two APKs. Note that if one APK is missing a certain resource type, the other
   *     APK will have an empty list within its [DiffList].
   */
  data class ResourceStats(
    val resources: Map<String, DiffList<String>>
  ) {
    /**
     * Writes the resource stats summary between two APKs to [stream].
     *
     * @param itemLimit the max number of items to include in expanded lists
     * @param itemize whether to expand lists of items
     */
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

  /**
   * Asset stats between two APKs. Note that for reporting simplicity, only immediate assets are
   * considered (i.e. those that are direct children of the assets/ directory).
   *
   * @property assets the difference list between the assets from both APKs
   */
  data class AssetStats(val assets: DiffList<String>) {
    /**
     * Writes the asset stats summary between two APKs to [stream].
     *
     * @param itemLimit the max number of items to include in expanded lists
     * @param itemize whether to expand lists of items
     */
    fun writeTo(stream: PrintStream, itemize: Boolean, itemLimit: Int) {
      if (itemize || assets.hasDifference()) {
        assets.writeTo(stream, "Lesson assets", itemize, itemLimit)
      }
    }
  }

  /**
   * A difference between two long values.
   *
   * @property oldValue the [Long] value corresponding to the build without changes
   * @property newValue the [Long] value corresponding to the build with changes
   */
  data class DiffLong(val oldValue: Long, val newValue: Long) {
    /** The difference between the two values. */
    val difference: Long by lazy { newValue - oldValue }

    /** Returns whether the two values represented by this difference are actually different. */
    fun hasDifference(): Boolean = difference != 0L
  }

  /**
   * A difference between two [List]s.
   *
   * Note that rather than storing a list of [T] values, this stores a list of [DiffEntry]s to
   * catalog similarities between the two lists. Note that this list's order is not guaranteed
   * relative to the orders of either [oldList] or [newList].
   *
   * Finally, this list may be used in cases when the values can guarantee equivalence (such a
   * strings), but may not represent cases where those values correspond to files or properties
   * whose values have changed (such as string resources).
   *
   * @property oldList the [List] of values corresponding to the build without changes
   * @property newList the [List] of values corresponding to the build with changes
   */
  class DiffList<T>(
    private val oldList: List<T>,
    private val newList: List<T>
  ) : AbstractList<DiffList.DiffEntry<T>>() {
    private val oldSet by lazy { oldList.toSet() }
    private val newSet by lazy { newList.toSet() }
    private val combined by lazy { oldSet + newSet }
    private val processedEntries by lazy { processDiffs() }

    /** The number of tracked elements corresponding to the build without changes. */
    val oldCount: Int by lazy { oldSet.size }

    /** The number of tracked elements corresponding to the build with changes. */
    val newCount: Int by lazy { newSet.size }

    /** The difference in element count between the two tracked lists. */
    val countDifference: Int by lazy { newCount - oldCount }

    /** Returns whether the two tracked lists have different counts. */
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

    /**
     * Represents a difference between an old & new element in [DiffList].
     *
     * @property humanReadableName the human-readable name corresponding to this type
     */
    enum class DiffType(val humanReadableName: String) {
      /** Represents two entries that are present in both lists. */
      SAME_ENTRY("same"),

      /** Represents an entry only present in the new list. */
      NEW_ENTRY("added"),

      /** Represents an entry only present in the old list. */
      REMOVED_ENTRY("removed")
    }

    /**
     * Corresponds to an entry within [DiffList].
     *
     * @property type the [DiffType] corresponding to this value
     * @property value the value that's present in one or both lists (depending on [type])
     */
    data class DiffEntry<T>(val type: DiffType, val value: T)
  }

  private companion object {
    private fun File.newDirectory(name: String): File {
      return File(this, name).also { it.mkdir() }
    }

    private fun <K : Any?, IV : Any, OV : Any> combineMaps(
      oldMap: Map<K, IV>,
      newMap: Map<K, IV>,
      combineValue: (IV?, IV?) -> OV
    ): Map<K, OV> {
      val allKeys = oldMap.keys + newMap.keys
      return allKeys.map { key ->
        return@map key to combineValue(oldMap[key], newMap[key])
      }.toMap()
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
          "assets/" in entry.name && "/" !in entry.name.substringAfter("assets/")
        }.map { it.name.substringAfter("assets/") }.toList()
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
  }
}
