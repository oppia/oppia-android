package org.oppia.android.scripts.license

import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.proto.MavenDependency

/**
 * The main entrypoint for verifying the list of third-party maven dependencies in
 * maven_dependencies.textproto is up-to-date.
 *
 * Usage:
 *   bazel run //scripts:maven_dependencies_list_check -- <path_to_root>
 *   <path_to_maven_install_json> <path_to_maven_dependencies_pb>
 *
 *
 * Arguments:
 * - path_to_root: directory path to the root of the Oppia Android repository.
 * - path_to_maven_install_json: relative path to the maven_install.json file.
 * - path_to_maven_dependencies_pb: relative path to the maven_dependencies.pb file.
 *
 * Example:
 *   bazel run //scripts:maven_dependencies_list_check -- $(pwd)
 *   third_party/maven_install.json scripts/assets/maven_dependencies.pb
 */
fun main(args: Array<String>) {
  MavenDependenciesListCheck(LicenseFetcherImpl()).main(args)
}

/**
 * Wrapper class to pass [LicenseFetcher] and [CommandExecutor] to be utilized by the the main
 * method.
 */
class MavenDependenciesListCheck(
  private val licenseFetcher: LicenseFetcher,
  private val commandExecutor: CommandExecutor = CommandExecutorImpl()
) {

  /**
   * Verifies that the list of third-party maven dependencies in maven_dependnecies.textproto is
   * up-to-date.
   */
  fun main(args: Array<String>) {
    val pathToRoot = args[0]
    val pathToMavenInstallJson = "$pathToRoot/${args[1]}"
    val pathToMavenDependenciesPb = args[2]

    val MavenDependenciesRetriever = MavenDependenciesRetriever(
      pathToRoot,
      licenseFetcher,
      commandExecutor
    )

    val bazelQueryDepsList =
      MavenDependenciesRetriever.retrieveThirdPartyMavenDependenciesList()
    val mavenInstallDepsList = MavenDependenciesRetriever.getDependencyListFromMavenInstall(
      pathToMavenInstallJson,
      bazelQueryDepsList
    )

    val dependenciesListFromPom =
      MavenDependenciesRetriever
        .retrieveDependencyListFromPom(mavenInstallDepsList)
        .mavenDependencyList

    val dependenciesListFromTextProto =
      MavenDependenciesRetriever
        .retrieveMavenDependencyList(pathToMavenDependenciesPb)

    val updatedDependenciesList =
      MavenDependenciesRetriever.addChangesFromTextProto(
        dependenciesListFromPom,
        dependenciesListFromTextProto
      )

    val manuallyUpdatedLicenses =
      MavenDependenciesRetriever
        .retrieveManuallyUpdatedLicensesSet(updatedDependenciesList)

    val finalDependenciesList =
      MavenDependenciesRetriever.updateMavenDependenciesList(
        updatedDependenciesList,
        manuallyUpdatedLicenses
      )

    val redundantDependencies = findRedundantDependencies(
      finalDependenciesList,
      dependenciesListFromTextProto
    )
    val missindDependencies = findMissingDependencies(
      finalDependenciesList,
      dependenciesListFromTextProto
    )
    if (redundantDependencies.isNotEmpty()) {
      println(
        """
        Please remove these redundant dependencies from maven_dependencies.textproto. Note that 
        running the script scripts/src/java/org/oppia/android/scripts/maven/GenerateMavenDependenciesList.kt 
        may fix this.
        Refer to https://github.com/oppia/oppia-android/wiki/Updating-Maven-Dependencies to learn 
        more.
        """.trimIndent() + "\n"
      )
      redundantDependencies.forEach {
        println(it)
      }
    }
    if (missindDependencies.isNotEmpty()) {
      println(
        """
        Please add these missing dependencies to maven_dependencies.textproto. Note that running
        the script scripts/src/java/org/oppia/android/scripts/maven/GenerateMavenDependenciesList.kt 
        may fix this.
        Refer to https://github.com/oppia/oppia-android/wiki/Updating-Maven-Dependencies to learn 
        more.
        """.trimIndent() + "\n"
      )
      missindDependencies.forEach {
        println(it)
      }
    }
    if (redundantDependencies.isNotEmpty() && missindDependencies.isNotEmpty()) {
      throw Exception("Redundant and missing dependencies in maven_dependencies.textproto")
    } else if (redundantDependencies.isNotEmpty()) {
      throw Exception("Redundant dependencies in maven_dependencies.textproto")
    } else if (missindDependencies.isNotEmpty()) {
      throw Exception("Missing dependencies in maven_dependencies.textproto")
    }

    println("\nmaven_dependencies.textproto is up-to-date.")
  }

  private fun findRedundantDependencies(
    dependenciesList: List<MavenDependency>,
    updatedDependenciesList: List<MavenDependency>
  ): List<MavenDependency> {
    return updatedDependenciesList - dependenciesList
  }

  private fun findMissingDependencies(
    dependenciesList: List<MavenDependency>,
    updatedDependenciesList: List<MavenDependency>
  ): List<MavenDependency> {
    return dependenciesList - updatedDependenciesList
  }
}
