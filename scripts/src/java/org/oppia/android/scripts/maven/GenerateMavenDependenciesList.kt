package org.oppia.android.scripts.maven

/**
 * Script to compile the list of the third-party Maven dependencies (direct and indirect both)
 * on which Oppia Android depends.
 *
 * Usage:
 *   bazel run //scripts:generate_maven_dependencies_list  -- <path_to_directory_root>
 *   <path_to_maven_install_json> <path_to_maven_dependencies_textproto>
 *
 * Arguments:
 * - path_to_directory_root: directory path to the root of the Oppia Android repository.
 * - path_to_maven_install_json: relative path to the maven_install.json file.
 * Example:
 *   bazel run //scripts:generate_maven_dependencies_list -- $(pwd)
 *   third_party/maven_install.json
 */
fun main(args: Array<String>) {

  MavenDependenciesListWriter.networkAndBazelUtils = NetworkAndBazelUtilsImpl()

  MavenDependenciesListWriter.main(args)
}
