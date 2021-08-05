package org.oppia.android.scripts.ci

import org.oppia.android.scripts.common.BazelClient
import org.oppia.android.scripts.common.GitClient
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
 *   bazel run //scripts:compute_affected_tests -- $(pwd) /tmp/affected_tests.log origin/develop
 */
fun main(args: Array<String>) {
  if (args.size < 3) {
    println(
      "Usage: bazel run //scripts:compute_affected_tests --" +
        " <path_to_directory_root> <path_to_output_file> <base_develop_branch_reference>"
    )
    exitProcess(1)
  }

  val pathToRoot = args[0]
  val pathToOutputFile = args[1]
  val baseDevelopBranchReference = args[2]
  val rootDirectory = File(pathToRoot).absoluteFile
  val outputFile = File(pathToOutputFile).absoluteFile

  check(rootDirectory.isDirectory) { "Expected '$pathToRoot' to be a directory" }
  check(rootDirectory.list().contains("WORKSPACE")) {
    "Expected script to be run from the workspace's root directory"
  }

  println("Running from directory root: $rootDirectory")
  println("Saving results to file: $outputFile")

  val gitClient = GitClient(rootDirectory, baseDevelopBranchReference)
  val bazelClient = BazelClient(rootDirectory)
  println("Current branch: ${gitClient.currentBranch}")
  println("Most recent common commit: ${gitClient.branchMergeBase}")
  when (gitClient.currentBranch.toLowerCase(Locale.getDefault())) {
    "develop" -> computeAffectedTargetsForDevelopBranch(bazelClient, outputFile)
    else ->
      computeAffectedTargetsForNonDevelopBranch(gitClient, bazelClient, rootDirectory, outputFile)
  }
}

private fun computeAffectedTargetsForDevelopBranch(bazelClient: BazelClient, outputFile: File) {
  // Compute & print all test targets since this is the develop branch.
  println("Computing all test targets for the develop branch")

  val allTestTargets = bazelClient.retrieveAllTestTargets()
  println()
  println(
    "Affected test targets:" +
      "\n${allTestTargets.joinToString(separator = "\n") { "- $it" }}"
  )
  outputFile.printWriter().use { writer -> allTestTargets.forEach { writer.println(it) } }
}

private fun computeAffectedTargetsForNonDevelopBranch(
  gitClient: GitClient,
  bazelClient: BazelClient,
  rootDirectory: File,
  outputFile: File
) {
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

  // The list of Bazel files to be ignored in the CI.
  val filteredBazelFiles = changedBazelFiles.filter { file ->
    !file.startsWith("instrumentation/", ignoreCase = true)
  }

  println("Changed Bazel-specific support files: $filteredBazelFiles")

  // Compute the list of affected tests based on BUILD/Bazel/WORKSPACE files. These are generally
  // framed as: if a BUILD file changes, run all tests transitively connected to it.
  val transitiveTestTargets = bazelClient.retrieveTransitiveTestTargets(filteredBazelFiles)
  println("Affected test targets due to transitive build deps: $transitiveTestTargets")

  val allAffectedTestTargets = (affectedTestTargets + transitiveTestTargets).toSet()
  println()
  println(
    "Affected test targets:" +
      "\n${allAffectedTestTargets.joinToString(separator = "\n") { "- $it" }}"
  )
  outputFile.printWriter().use { writer -> allAffectedTestTargets.forEach { writer.println(it) } }
}
