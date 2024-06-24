package org.oppia.android.scripts.coverage

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import org.oppia.android.scripts.common.BazelClient
import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import org.oppia.android.scripts.proto.BranchCoverage
import org.oppia.android.scripts.proto.Coverage
import org.oppia.android.scripts.proto.CoverageReport
import org.oppia.android.scripts.proto.CoveredFile
import org.oppia.android.scripts.proto.CoveredLine
import org.oppia.android.scripts.proto.FunctionCoverage
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
    val branchCoverage = mutableListOf<BranchCoverage>()
    val functionCoverage = mutableListOf<FunctionCoverage>()

    var functionsFound = 0
    var functionsHit = 0
    var branchesFound = 0
    var branchesHit = 0

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
            // BRDA:<line number>,<block number>,<branch number>,<taken>
            line.startsWith("BRDA:") -> {
              val parts = line.substringAfter("BRDA:").split(",")
              val lineNumber = parts[0].toInt()
              val blockNumber = parts[1].toInt()
              val branchNumber = parts[2].toInt()
              val hitCount = parts[3].toInt()
              val coverage =
                if (hitCount > 0)
                  Coverage.FULL
                else
                  Coverage.NONE
              branchCoverage.add(
                BranchCoverage.newBuilder()
                  .setLineNumber(lineNumber)
                  .setBlockNumber(blockNumber)
                  .setBranchNumber(branchNumber)
                  .setHitCount(hitCount)
                  .setCoverage(coverage)
                  .build()
              )
            }
            // FN:<line number of function start>,<function name>
            line.startsWith("FN:") -> {
              val parts = line.substringAfter("FN:").split(",")
              val currentFunctionLineNumber = parts[0].toInt()
              val functionName = parts[1]
              functionCoverage.add(
                FunctionCoverage.newBuilder()
                  .setLineNumber(currentFunctionLineNumber)
                  .setFunctionName(functionName)
                  .setExecutionCount(0)
                  .setCoverage(Coverage.NONE)
                  .build()
              )
            }
            // FNDA:<execution count>,<function name>
            line.startsWith("FNDA:") -> {
              val parts = line.substringAfter("FNDA:").split(",")
              val executionCount = parts[0].toInt()
              val functionName = parts[1]
              val index = functionCoverage.indexOfFirst { it.functionName == functionName }
              if (index != -1) {
                val updatedFunctionCoverage = functionCoverage[index].toBuilder()
                  .setExecutionCount(executionCount)
                  .setCoverage(
                    if (executionCount > 0)
                      Coverage.FULL
                    else
                      Coverage.NONE
                  )
                  .build()
                functionCoverage[index] = updatedFunctionCoverage
              }
            }
            // FNF:<number of functions found>
            line.startsWith("FNF:") -> {
              functionsFound = line.substringAfter("FNF:").toInt()
            }
            // FNH:<number of function hit>
            line.startsWith("FNH:") -> {
              functionsHit = line.substringAfter("FNH:").toInt()
            }
            // BRF:<number of branches found>
            line.startsWith("BRF:") -> {
              branchesFound = line.substringAfter("BRF:").toInt()
            }
            // BRH:<number of branches hit>
            line.startsWith("BRH:") -> {
              branchesHit = line.substringAfter("BRH:").toInt()
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

    val coveredFile = CoveredFile.newBuilder()
      .setFilePath(filePath)
      .setFileSha1Hash(fileSha1Hash)
      .addAllCoveredLine(coveredLines)
      .addAllBranchCoverage(branchCoverage)
      .addAllFunctionCoverage(functionCoverage)
      .setFunctionsFound(functionsFound)
      .setFunctionsHit(functionsHit)
      .setBranchesFound(branchesFound)
      .setBranchesHit(branchesHit)
      .setLinesFound(linesFound)
      .setLinesHit(linesHit)
      .build()

    return CoverageReport.newBuilder()
      .setBazelTestTarget(bazelTestTarget)
      .addCoveredFile(coveredFile)
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
