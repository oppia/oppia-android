package org.oppia.android.scripts.lint

import com.squareup.moshi.JsonEncodingException
import com.squareup.moshi.Moshi
import org.oppia.android.scripts.common.BazelClient
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import org.oppia.android.scripts.lint.model.SarifOutput
import org.oppia.android.scripts.lint.model.SarifResult
import org.oppia.android.scripts.lint.model.SarifRun
import java.io.File

/**
 * The main entrypoint for running Java lint checks.
 *
 * This script wraps the Checkstyle (https://github.com/checkstyle/checkstyle) utility for
 * performing basic lint checks on all Java source files in the repository.
 *
 * Usage:
 *   bazel run //scripts:checkstyle -- <path_to_repo_root>
 *
 * Arguments:
 * - path_to_repo_root: directory path to the root of the Oppia Android repository.
 *
 * Example:
 *   bazel run //scripts:checkstyle -- $(pwd)
 */
fun main(vararg args: String) {
  require(args.size == 1) { "Usage: bazel run //scripts:checkstyle -- </path/to/repo_root>" }
  val repoRoot = File(args[0]).absoluteFile.normalize().also {
    check(it.exists() && it.isDirectory) {
      "Expected provided repository root to be an existing directory: ${args[0]}."
    }
  }
  ScriptBackgroundCoroutineDispatcher().use { scriptBgDispatcher ->
    val commandExecutor = CommandExecutorImpl(scriptBgDispatcher)
    val bazelClient = BazelClient(repoRoot, commandExecutor)
    val checkstyleRunner = Checkstyle(repoRoot, bazelClient)
    checkstyleRunner.runCheckstyle()
  }
}

/**
 * Utility for running the Checkstyle utility as part of verifying all .java files under [repoRoot].
 *
 * @property repoRoot the absolute [File] corresponding to the root of the inspected repository
 * @property bazelClient a [BazelClient] configured for a single repository at [repoRoot]
 */
class Checkstyle(private val repoRoot: File, private val bazelClient: BazelClient) {
  /**
   * Performs a lint check on all Java source files in the repository, throwing an exception if any
   * have lint failures.
   */
  fun runCheckstyle() {
    val rootDirPaths = JAVA_ROOTS.map { javaRootPath ->
      File(File(repoRoot, javaRootPath), "src").absoluteFile.normalize().also {
        check(it.exists() && it.isDirectory) {
          "Configured Java root isn't an existing directory with 'src' subdirectory: $javaRootPath."
        }
      }
    }.map { it.toRelativeString(repoRoot) }

    println("Linting Java files under ${rootDirPaths.joinToString()}...")
    val (exitCode, outputLines) = bazelClient.run(
      CHECKSTYLE_BINARY_TARGET,
      "-c=/google_checks.xml",
      "-f=sarif",
      *rootDirPaths.toTypedArray(),
      allowFailures = true
    )
    check(exitCode == 0) {
      "Something failed while trying to run Checkstyle binary:\n\n" +
        "${outputLines.joinToString(separator = "\n")}."
    }

    // Remove extra Bazel output that may be included when the script is run while another Bazel
    // process is blocking. This generally only happens when running the script directly with Java
    // (vs. running it via Bazel).
    val sarifStart = outputLines.indexOfFirst { it.trim() == "{" }.also {
      check(it != -1) { "Expected Checkstyle output to include SARIF JSON." }
    }
    val filteredOutputLines = outputLines.drop(sarifStart)
    val sarifOutput = parseSarif(filteredOutputLines.joinToString(separator = "\n"))
    val allResults = sarifOutput.runs.flatMap(SarifRun::results)
    val groupedResults = allResults.groupBy(SarifResult::level).toSortedMap()
    groupedResults.forEach { (level, results) ->
      println()
      println("Found ${results.size} ${level.humanName}${if (results.size == 1) "" else "s"}:")
      val failures = results.flatMap { result ->
        val message = result.message.text
        result.locations.map { message to it.physicalLocation }
      }.sortedBy { (_, location) -> location }
      failures.forEach { (message, location) ->
        val relativeLocation = File(location.artifactLocation.uri).toRelativeString(repoRoot)
        val line = location.region.startLine
        val column = location.region.startColumn
        println("- $relativeLocation:$line:$column: $message")
      }
    }

    println()
    check(groupedResults.isEmpty()) {
      "Checkstyle command failed. Please fix lint issues found above manually."
    }

    println("No Java lint issues found!")
  }

  private companion object {
    /** The individual Java directory roots in which to run Checkstyle. */
    private val JAVA_ROOTS = listOf("app", "data", "domain", "utility", "testing", "scripts")
    private const val CHECKSTYLE_BINARY_TARGET =
      "//scripts/third_party:checkstyle_binary_deploy.jar"

    private fun parseSarif(rawSarifJson: String): SarifOutput {
      val output = try {
        Moshi.Builder().build().adapter(SarifOutput::class.java).fromJson(rawSarifJson)
      } catch (e: JsonEncodingException) { null }
      return checkNotNull(output) { "Error: provided SARIF output is invalid:\n$rawSarifJson." }
    }
  }
}
