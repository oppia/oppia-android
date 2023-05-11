package org.oppia.android.scripts.lint

import com.squareup.moshi.Moshi
import java.io.File
import org.oppia.android.scripts.common.BazelClient
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import org.oppia.android.scripts.lint.model.SarifOutput
import org.oppia.android.scripts.lint.model.SarifResult
import org.oppia.android.scripts.lint.model.SarifRun

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

class Checkstyle(private val repoRoot: File, private val bazelClient: BazelClient) {
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

    val sarifOutput = parseSarif(outputLines.joinToString(separator = "\n"))
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
      val moshi = Moshi.Builder().build()
      return checkNotNull(moshi.adapter(SarifOutput::class.java).fromJson(rawSarifJson)) {
        "Error: provided SARIF output is invalid:\n$rawSarifJson."
      }
    }
  }
}
