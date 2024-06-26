package org.oppia.android.scripts.coverage

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
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
   * @return a deferred value that contains the coverage data
   */
  fun runWithCoverageAsync(
    bazelTestTarget: String
  ): Deferred<CoverageReport> {
    return CoroutineScope(scriptBgDispatcher).async {
      val coverageResult = retrieveCoverageResult(bazelTestTarget)
        ?: throw RuntimeException("Failed to retrieve coverage result for $bazelTestTarget")

        parseCoverageData(coverageResult, bazelTestTarget)
    }
  }

  private fun retrieveCoverageResult(
    bazelTestTarget: String
  ): List<String>? {
    return bazelClient.runCoverageForTestTarget(bazelTestTarget)
  }

  private fun parseCoverageData(
    coverageData: List<String>,
    bazelTestTarget: String
  ): CoverageReport {
    var filePath = ""
    var linesFound = 0
    var linesHit = 0
    val coveredLines = mutableListOf<CoveredLine>()

    var parseFile = false
    val extractedFileName = "${extractTargetName(bazelTestTarget)}.kt"

    coverageData.forEach { line ->
      when {
        // SF:<absolute path to the source file>
        line.startsWith("SF:") -> {
          val sourceFilePath = line.substringAfter("SF:")
          if (sourceFilePath.substringAfterLast("/") == extractedFileName) {
            filePath = line.substringAfter("SF:")
            parseFile = true
          } else {
            parseFile = false
          }
        }
        parseFile -> {
          when {
            // DA:<line number>,<execution count>
            line.startsWith("DA:") -> {
              val parts = line.substringAfter("DA:").split(",")
              val lineNumber = parts[0].toInt()
              val hitCount = parts[1].toInt()
              val coverage =
                if (hitCount > 0)
                  Coverage.FULL
                else
                  Coverage.NONE
              coveredLines.add(
                CoveredLine.newBuilder()
                  .setLineNumber(lineNumber)
                  .setCoverage(coverage)
                  .build()
              )
            }
            // LF:<number of instrumented lines>
            line.startsWith("LF:") -> {
              linesFound = line.substringAfter("LF:").toInt()
            }
            // LH:<number of lines with a non-zero execution count>
            line.startsWith("LH:") -> {
              linesHit = line.substringAfter("LH:").toInt()
            }
            line.startsWith("end_of_record") -> {
              parseFile = false
            }
          }
        }
      }
    }

    val file = File(repoRoot, filePath)
    val fileSha1Hash = calculateSha1(file.absolutePath)

    return CoverageReport.newBuilder()
      .setBazelTestTarget(bazelTestTarget)
      .setFilePath(filePath)
      .setFileSha1Hash(fileSha1Hash)
      .addAllCoveredLine(coveredLines)
      .setLinesFound(linesFound)
      .setLinesHit(linesHit)
      .build()
  }
}

private fun extractTargetName(bazelTestTarget: String): String {
  val targetName = bazelTestTarget.substringAfterLast(":").trim()
  return targetName.removeSuffix("Test").removeSuffix("LocalTest")
}

private fun calculateSha1(filePath: String): String {
  val fileBytes = Files.readAllBytes(Paths.get(filePath))
  val digest = MessageDigest.getInstance("SHA-1")
  val hashBytes = digest.digest(fileBytes)
  return hashBytes.joinToString("") { "%02x".format(it) }
}
