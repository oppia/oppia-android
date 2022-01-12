package org.oppia.android.scripts.license

import com.google.protobuf.TextFormat
import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.proto.MavenDependency

/**
 * The main entrypoint for verifying the list of third-party Maven dependencies in
 * maven_dependencies.textproto is up-to-date.
 *
 * Usage:
 *   bazel run //scripts:maven_dependencies_list_check -- <path_to_root>
 *   <path_to_maven_install_json> <path_to_maven_dependencies_pb>
 *
 *
 * Arguments:
 * - path_to_root: directory path to the root of the Oppia Android repository.
 * - path_to_maven_install_json: relative path to the Maven installation manifest file.
 * - path_to_maven_dependencies_pb: relative path to the maven_dependencies.pb file.
 *
 * Example:
 *   bazel run //scripts:maven_dependencies_list_check -- $(pwd)
 *   third_party/maven_prod_install.json scripts/assets/maven_dependencies.pb
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
   * Verifies that the list of third-party Maven dependencies in maven_dependnecies.textproto is
   * up-to-date.
   */
  fun main(args: Array<String>) {
    val pathToRoot = args[0]
    val pathToMavenInstallJson = "$pathToRoot/${args[1]}"
    val pathToMavenDependenciesPb = args[2]

    val mavenDependenciesRetriever = MavenDependenciesRetriever(
      pathToRoot,
      licenseFetcher,
      commandExecutor
    )

    val bazelQueryDepsList =
      mavenDependenciesRetriever.retrieveThirdPartyMavenDependenciesList()
    val mavenInstallDepsList = mavenDependenciesRetriever.getDependencyListFromMavenInstall(
      pathToMavenInstallJson,
      bazelQueryDepsList
    )

    val dependenciesListFromPom =
      mavenDependenciesRetriever
        .retrieveDependencyListFromPom(mavenInstallDepsList)
        .mavenDependencyList

    val dependenciesListFromTextProto =
      mavenDependenciesRetriever
        .retrieveMavenDependencyList(pathToMavenDependenciesPb)

    val updatedDependenciesList =
      mavenDependenciesRetriever.addChangesFromTextProto(
        dependenciesListFromPom,
        dependenciesListFromTextProto
      )

    val manuallyUpdatedLicenses =
      mavenDependenciesRetriever
        .retrieveManuallyUpdatedLicensesSet(updatedDependenciesList)

    val finalDependenciesList =
      mavenDependenciesRetriever.updateMavenDependenciesList(
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

    if (redundantDependencies.isNotEmpty() || missindDependencies.isNotEmpty()) {
      printErrorMessage()
      if (redundantDependencies.isNotEmpty()) {
        println("Redundant dependencies that need to be removed:\n")
        printDependenciesList(redundantDependencies)
      }
      if (missindDependencies.isNotEmpty()) {
        println("Missing dependencies that need to be added:\n")
        printDependenciesList(missindDependencies)
      }
      println(
        """
        Refer to https://github.com/oppia/oppia-android/wiki/Updating-Maven-Dependencies for more details.
        """.trimIndent()
      )
    }

    if (redundantDependencies.isNotEmpty() && missindDependencies.isNotEmpty()) {
      throw Exception("Redundant and missing dependencies in maven_dependencies.textproto")
    } else if (redundantDependencies.isNotEmpty()) {
      throw Exception("Redundant dependencies in maven_dependencies.textproto")
    } else if (missindDependencies.isNotEmpty()) {
      throw Exception("Missing dependencies in maven_dependencies.textproto")
    }

    val brokenLicenses = mavenDependenciesRetriever.getAllBrokenLicenses(finalDependenciesList)

    if (brokenLicenses.isNotEmpty()) {
      val licenseToDependencyMap =
        mavenDependenciesRetriever
          .findFirstDependenciesWithBrokenLicenses(
            finalDependenciesList,
            brokenLicenses
          )
      printErrorMessage()
      println("Licenses that need to be updated:\n")
      brokenLicenses.forEach {
        println(
          """
          license_name: ${it.licenseName}
          original_link: ${it.originalLink}
          verified_link_case: ${it.verifiedLinkCase}
          is_original_link_invalid: ${it.isOriginalLinkInvalid}
          First dependency that should be updated with the license: ${licenseToDependencyMap[it]}
          """.trimIndent() + "\n\n"
        )
      }
      throw Exception("Licenses details are not completed")
    }

    val dependenciesWithoutAnyLinks =
      mavenDependenciesRetriever.getDependenciesThatNeedIntervention(finalDependenciesList)

    if (dependenciesWithoutAnyLinks.isNotEmpty()) {
      printErrorMessage()
      println("Dependencies with invalid or no license links:\n")
      dependenciesWithoutAnyLinks.forEach { dependency ->
        println(dependency)
      }
      throw Exception("License links are invalid or not available for some dependencies")
    }

    println("\nmaven_dependencies.textproto is up-to-date.")
  }

  private fun printErrorMessage() {
    println(
      "Errors were encountered. Please run script GenerateMavenDependenciesList.kt to fix.\n"
    )
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

  private fun printDependenciesList(dependencyList: List<MavenDependency>) {
    dependencyList.forEach { dep ->
      TextFormat.printer().print(dep, System.out)
      println()
    }
  }
}
