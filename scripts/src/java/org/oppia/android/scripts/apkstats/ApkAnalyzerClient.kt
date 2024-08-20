package org.oppia.android.scripts.apkstats

import com.android.tools.apk.analyzer.AndroidApplicationInfo
import com.android.tools.apk.analyzer.ApkSizeCalculator
import com.android.tools.apk.analyzer.Archives
import com.android.tools.apk.analyzer.dex.DexFileStats
import com.android.tools.apk.analyzer.dex.DexFiles
import com.android.tools.apk.analyzer.internal.ApkDiffEntry
import com.android.tools.apk.analyzer.internal.ApkFileByFileDiffParser
import java.io.File
import java.util.zip.ZipFile
import javax.swing.tree.DefaultMutableTreeNode

/**
 * General utility for interfacing with apkanalyzer's internal library. This implementation
 * generally behaves like the apkanalyzer CLI implementation with some simplification. Note that the
 * actual apkanalyzer tool can't be used since it isn't exported by android_sdk_repository.
 *
 * @property aapt2Client the [Aapt2Client] needed to access aapt2 by some routines
 */
class ApkAnalyzerClient(private val aapt2Client: Aapt2Client) {
  private val apkSizeCalculator by lazy { ApkSizeCalculator.getDefault() }

  // CLI reference: https://developer.android.com/studio/command-line/apkanalyzer.

  /** Returns the file size of the specified APK as similarly reported by apkanalyzer. */
  fun computeFileSize(inputApkPath: String): Long =
    apkSizeCalculator.getFullApkRawSize(File(inputApkPath).toPath())

  /**
   * Returns the estimated download size of the specified APK as similarly reported by apkanalyzer.
   */
  fun computeDownloadSize(inputApkPath: String): Long =
    apkSizeCalculator.getFullApkDownloadSize(File(inputApkPath).toPath())

  /**
   * Returns the list of required features of the specified APK as similarly reported by
   * apkanalyzer.
   */
  fun computeFeatures(inputApkPath: String): List<String> {
    val apkInfo = AndroidApplicationInfo.parseBadging(aapt2Client.dumpBadging(inputApkPath))
    return apkInfo.usesFeature.keys.toList()
  }

  /**
   * Returns a full comparison of the two specified APKs, similar to apkanalyzer. Note that each
   * entry of the returned list represents a single line (the list is in the order of the comparison
   * output).
   */
  fun compare(inputApkPath1: String, inputApkPath2: String): List<String> {
    return Archives.open(File(inputApkPath1).toPath()).use { apkArchive1 ->
      Archives.open(File(inputApkPath2).toPath()).use { apkArchive2 ->
        walkParseTree(ApkFileByFileDiffParser.createTreeNode(apkArchive1, apkArchive2))
      }
    }
  }

  /**
   * Returns the map of dex files to method counts contained within the specified APK file, as
   * similarly reported by apkanalyzer.
   */
  fun computeDexReferencesList(inputApkPath: String): Map<String, Int> {
    return collectZipEntries(inputApkPath) {
      it.endsWith(".dex")
    }.mapValues { (_, dexFileContents) ->
      DexFileStats.create(listOf(DexFiles.getDexFile(dexFileContents))).referencedMethodCount
    }
  }

  // Based on the apkanalyzer CLI implementation.
  private fun walkParseTree(node: DefaultMutableTreeNode?): List<String> {
    return node?.let {
      (node.userObject as? ApkDiffEntry)?.let { entry ->
        val path = entry.path.toString()
        when {
          // Encountered a folder. Recurse down it in case its sizes aren't accurate (descendants
          // may have changed).
          path.endsWith("/") -> {
            (0 until node.getChildCount())
              .map { node.getChildAt(it) }
              .flatMap { walkParseTree(it as? DefaultMutableTreeNode) }
          }
          // Only record the entry if there's a difference.
          entry.oldSize != entry.newSize -> {
            listOf("${entry.oldSize}\t${entry.newSize}\t${entry.size}\t${entry.path}")
          }
          else -> null
        }
      }
    } ?: listOf()
  }

  // #ApkAnalyzerClient for github actions change
  private fun collectZipEntries(
    inputZipFile: String,
    namePredicate: (String) -> Boolean
  ): Map<String, ByteArray> {
    return ZipFile(inputZipFile).use { zipFile ->
      zipFile.entries()
        .asSequence()
        .filter { namePredicate(it.name) }
        .associateBy { it.name }
        .mapValues { (_, entry) -> zipFile.getInputStream(entry).readBytes() }
    }
  }
}
