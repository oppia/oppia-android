package org.oppia.android.scripts.coverage

import org.oppia.android.scripts.common.BazelClient
import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import org.oppia.android.scripts.proto.Coverage
import org.oppia.android.scripts.proto.CoverageReport
import org.oppia.android.scripts.proto.CoveredLine
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest

/**
 * Class responsible for running coverage analysis asynchronously.
 *
 * @param repoRoot the root directory of the repository
 * @param scriptBgDispatcher the [ScriptBackgroundCoroutineDispatcher] to be used for running the coverage command
 * @param commandExecutor executes the specified command in the specified working directory
 */
class CoverageRunner(
  private val repoRoot: File,
  private val scriptBgDispatcher: ScriptBackgroundCoroutineDispatcher,
  private val commandExecutor: CommandExecutor
) {
  private val bazelClient by lazy { BazelClient(repoRoot, commandExecutor) }

  /**
   * Runs coverage analysis asynchronously for the Bazel test target.
   *
   * @param bazelTestTarget Bazel test target to analyze coverage
   * //@return a deferred value that contains the coverage data
   * @return a value that contains the coverage data
   */
  fun retrieveCoverageDataForTestTarget(
    bazelTestTarget: String
  ): CoverageReport {
      val coverageResult = retrieveCoverageResult(bazelTestTarget)
        ?: return generateFailedCoverageReport()

      return coverageDataFileLines(coverageResult, bazelTestTarget)
  }

  private fun retrieveCoverageResult(
    bazelTestTarget: String
  ): List<String>? {
    return bazelClient.runCoverageForTestTarget(bazelTestTarget)
  }

  private fun coverageDataFileLines(
    coverageData: List<String>,
    bazelTestTarget: String
  ): CoverageReport {
    val extractedFileName = "${extractTargetName(bazelTestTarget)}.kt"

    val sfStartIdx = coverageData.indexOfFirst {
      it.startsWith("SF:") && it.substringAfter("SF:").substringAfterLast("/") == extractedFileName
    }
    if (sfStartIdx == -1) return generateFailedCoverageReport()
    val eofIdx = coverageData.subList(sfStartIdx, coverageData.size).indexOfFirst {
      it.startsWith("end_of_record")
    }
    if (eofIdx == -1) return generateFailedCoverageReport()

    val fileSpecificCovDatLines = coverageData.subList(sfStartIdx, sfStartIdx + eofIdx + 1)

    val coverageDataProps = fileSpecificCovDatLines.groupBy { line ->
      line.substringBefore(":")
    }.mapValues { (_, lines) ->
      lines.map { line ->
        line.substringAfter(":").split(",")
      }
    }

    val filePath = coverageDataProps["SF"]?.firstOrNull()?.get(0)
    val linesFound = coverageDataProps["LF"]?.singleOrNull()?.single()?.toInt() ?: 0
    val linesHit = coverageDataProps["LH"]?.singleOrNull()?.single()?.toInt() ?: 0

    val coveredLines = coverageDataProps["DA"]?.map { (lineNumStr, hitCountStr) ->
      CoveredLine.newBuilder().apply {
        this.lineNumber = lineNumStr.toInt()
        this.coverage = if (hitCountStr.toInt() > 0) Coverage.FULL else Coverage.NONE
      }.build()
    }.orEmpty()

    val file = File(repoRoot, filePath)
    val fileSha1Hash = calculateSha1(file.absolutePath)

    return CoverageReport.newBuilder()
      .setBazelTestTarget(bazelTestTarget)
      .setFilePath(filePath)
      .setFileSha1Hash(fileSha1Hash)
      .addAllCoveredLine(coveredLines)
      .setLinesFound(linesFound)
      .setLinesHit(linesHit)
      .setIsGenerated(true)
      .build()
  }
}

private fun generateFailedCoverageReport(): CoverageReport {
  return CoverageReport.newBuilder()
    .setIsGenerated(false)
    .build()
}

private fun extractTargetName(bazelTestTarget: String): String {
  val targetName = bazelTestTarget
    .substringAfterLast("/")
    .substringAfterLast(":")
    .trim()
  return targetName.removeSuffix("LocalTest").removeSuffix("Test")
}

private fun calculateSha1(filePath: String): String {
  val fileBytes = Files.readAllBytes(Paths.get(filePath))
  val digest = MessageDigest.getInstance("SHA-1")
  val hashBytes = digest.digest(fileBytes)
  return hashBytes.joinToString("") { "%02x".format(it) }
}
