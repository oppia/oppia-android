package org.oppia.android.scripts.testfile

/**
 * Script for ensuring that all production files have test files present.
 *
 * Usage:
 *   bazel run //scripts:privacy_policy_check -- <path_to_directory_root>
 *
 * Arguments:
 * - path_to_directory_root: directory path to the root of the Oppia Android repository.
 *
 * Example:
 *   bazel run //scripts:test_file_check -- $(pwd)
 */
fun main(vararg args: String) {
  // Path of the repo to be analyzed.
  val repoPath = "${args[0]}/"

  for (k in 1..10) {
    println("Kotlin script")
    println("Kotlin script" + repoPath)
  }
}
