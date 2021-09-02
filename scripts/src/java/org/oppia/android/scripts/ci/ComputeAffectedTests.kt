package org.oppia.android.scripts.ci

import org.oppia.android.scripts.common.BazelClient
import org.oppia.android.scripts.common.GitClient
import org.oppia.android.scripts.common.ProtoStringEncoder.Companion.toCompressedBase64
import org.oppia.android.scripts.proto.AffectedTestsBucket
import java.io.File
import java.util.Locale
import kotlin.system.exitProcess

/**
 * The main entrypoint for computing the list of affected test targets based on changes in the local
 * Oppia Android Git repository.
 *
 * Usage:
 *   bazel run //scripts:compute_affected_tests -- \\
 *     <path_to_directory_root> <path_to_output_file> <base_develop_branch_reference>
 *
 * Arguments:
 * - path_to_directory_root: directory path to the root of the Oppia Android repository.
 * - path_to_output_file: path to the file in which the affected test targets will be printed.
 * - base_develop_branch_reference: the reference to the local develop branch that should be use.
 *     Generally, this is 'origin/develop'.
 *
 * Example:
 *   bazel run //scripts:compute_affected_tests -- $(pwd) /tmp/affected_test_buckets.proto64 \\
 *     origin/develop compute_all_tests=false
 */
fun main(args: Array<String>) {
  ComputeAffectedTests().main(args)
}

private class ComputeAffectedTests {
  companion object {
    private const val COMPUTE_ALL_TESTS_PREFIX = "compute_all_tests="

    private val VALID_TEST_BUCKET_NAMES = listOf(
      "app",
      "data",
      "domain",
      "instrumentation",
      "scripts",
      "testing",
      "utility"
    )

    private val EXTRACT_BUCKET_REGEX = "^//([^(/|:)]+?)[/:].+?\$".toRegex()
  }

  fun main(args: Array<String>) {
    if (args.size < 4) {
      println(
        "Usage: bazel run //scripts:compute_affected_tests --" +
          " <path_to_directory_root> <path_to_output_file> <base_develop_branch_reference>" +
          " <compute_all_tests=true/false>"
      )
      exitProcess(1)
    }

    val pathToRoot = args[0]
    val pathToOutputFile = args[1]
    val baseDevelopBranchReference = args[2]
    val computeAllTestsSetting = args[3].let {
      check(it.startsWith(COMPUTE_ALL_TESTS_PREFIX)) {
        "Expected last argument to start with '$COMPUTE_ALL_TESTS_PREFIX'"
      }
      val computeAllTestsValue = it.removePrefix(COMPUTE_ALL_TESTS_PREFIX)
      return@let computeAllTestsValue.toBooleanStrictOrNull()
        ?: error(
          "Expected last argument to have 'true' or 'false' passed to it, not:" +
            " '$computeAllTestsValue'"
        )
    }
    val rootDirectory = File(pathToRoot).absoluteFile

    check(rootDirectory.isDirectory) { "Expected '$pathToRoot' to be a directory" }
    check(rootDirectory.list().contains("WORKSPACE")) {
      "Expected script to be run from the workspace's root directory"
    }

    println("Running from directory root: $rootDirectory")

    val gitClient = GitClient(rootDirectory, baseDevelopBranchReference)
    val bazelClient = BazelClient(rootDirectory)
    println("Current branch: ${gitClient.currentBranch}")
    println("Most recent common commit: ${gitClient.branchMergeBase}")

    val currentBranch = gitClient.currentBranch.toLowerCase(Locale.getDefault())
    val affectedTestTargets = if (computeAllTestsSetting || currentBranch == "develop") {
      computeAllTestTargets(bazelClient)
    } else computeAffectedTargetsForNonDevelopBranch(gitClient, bazelClient, rootDirectory)

    val filteredTestTargets = filterTargets(affectedTestTargets)
    println()
    println("Affected test targets:")
    println(filteredTestTargets.joinToString(separator = "\n") { "- $it" })

    val affectedTestBuckets = bucketTargets(filteredTestTargets)
    val encodedTestBuckets = affectedTestBuckets.map { it.toCompressedBase64() }
    File(pathToOutputFile).printWriter().use { writer ->
      encodedTestBuckets.forEach(writer::println)
    }
  }

  private fun computeAllTestTargets(bazelClient: BazelClient): List<String> {
    println("Computing all test targets")
    return bazelClient.retrieveAllTestTargets()
  }

  private fun computeAffectedTargetsForNonDevelopBranch(
    gitClient: GitClient,
    bazelClient: BazelClient,
    rootDirectory: File
  ): List<String> {
    // Compute the list of changed files, but exclude files which no longer exist (since bazel query
    // can't handle these well).
    val changedFiles = gitClient.changedFiles.filter { filepath ->
      File(rootDirectory, filepath).exists()
    }
    println("Changed files (per Git): $changedFiles")

    val changedFileTargets = bazelClient.retrieveBazelTargets(changedFiles).toSet()
    println("Changed Bazel file targets: $changedFileTargets")

    val affectedTestTargets = bazelClient.retrieveRelatedTestTargets(changedFileTargets).toSet()
    println("Affected Bazel test targets: $affectedTestTargets")

    // Compute the list of Bazel files that were changed.
    val changedBazelFiles = changedFiles.filter { file ->
      file.endsWith(".bzl", ignoreCase = true) ||
        file.endsWith(".bazel", ignoreCase = true) ||
        file == "WORKSPACE"
    }
    println("Changed Bazel-specific support files: $changedBazelFiles")

    // Compute the list of affected tests based on BUILD/Bazel/WORKSPACE files. These are generally
    // framed as: if a BUILD file changes, run all tests transitively connected to it.
    val transitiveTestTargets = bazelClient.retrieveTransitiveTestTargets(changedBazelFiles)
    println("Affected test targets due to transitive build deps: $transitiveTestTargets")

    return (affectedTestTargets + transitiveTestTargets).toSet().toList()
  }

  private fun filterTargets(testTargets: List<String>): List<String> {
    // Filtering out the targets to be ignored.
    return testTargets.filter { targetPath ->
      !targetPath
        .startsWith(
          "//instrumentation/src/javatests/org/oppia/android/instrumentation/player",
          ignoreCase = true
        )
    }
  }

  private fun bucketTargets(testTargets: List<String>): List<AffectedTestsBucket> {
    return testTargets.map { target ->
      AffectedTestsBucket.newBuilder().apply {
        cacheBucketName = retrieveBucket(target)
        addAffectedTestTargets(target)
      }.build()
    }
  }

  private fun retrieveBucket(target: String): String {
    return EXTRACT_BUCKET_REGEX.matchEntire(target)?.groupValues?.maybeSecond()?.also {
      check(it in VALID_TEST_BUCKET_NAMES) {
        "Invalid bucket name: $it (expected one of: $VALID_TEST_BUCKET_NAMES)"
      }
    } ?: error("Invalid target: $target (could not extract bucket name)")
  }

  private fun <E> List<E>.maybeSecond(): E? = if (size >= 2) this[1] else null

  // Needed since the codebase isn't yet using Kotlin 1.5, so this function isn't available.
  private fun String.toBooleanStrictOrNull(): Boolean? {
    return when (toLowerCase(Locale.getDefault())) {
      "false" -> false
      "true" -> true
      else -> null
    }
  }
}
