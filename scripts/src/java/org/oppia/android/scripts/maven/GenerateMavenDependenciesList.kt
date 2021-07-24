package org.oppia.android.scripts.maven

import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.MavenDependenciesListGenerator
import org.oppia.android.scripts.proto.MavenDependencyList

/**
 * Script to compile the list of the third-party Maven dependencies (direct and indirect both)
 * on which Oppia Android depends.
 *
 * Usage:
 *   bazel run //scripts:generate_maven_dependencies_list  -- <path_to_directory_root>
 *   <path_to_maven_install_json> <path_to_maven_dependencies_textproto>
 *   <path_to_maven_dependencies_pb>
 *
 * Arguments:
 * - path_to_directory_root: directory path to the root of the Oppia Android repository.
 * - path_to_maven_install_json: relative path to the maven_install.json file.
 * - path_to_maven_dependencies_textproto: relative path to the maven_dependencies.textproto
 * - path_to_maven_dependencies_pb: relative path to the maven_dependencies.pb file.
 * Example:
 *   bazel run //scripts:generate_maven_dependencies_list -- $(pwd)
 *   third_party/maven_install.json scripts/assets/maven_dependencies.textproto
 *   scripts/assets/maven_dependencies.pb
 */
fun main(args: Array<String>) {
  GenerateMavenDependenciesList(LicenseFetcherImpl()).main(args)
}

/** Wrapper class to pass dependencies to be utilized by the the main method. */
class GenerateMavenDependenciesList(
  private val licenseFetcher: LicenseFetcher,
  private val commandExecutor: CommandExecutor = CommandExecutorImpl()
) {
  private val MAVEN_PREFIX = "@maven//:"

  /**
   * Compiles a list of third-party maven dependencies along with their license links on
   * which Oppia Android depends and write them in maven_dependencies.textproto.
   */
  fun main(args: Array<String>) {
    if (args.size < 4) {
      throw Exception("Too few Arguments passed")
    }
    val pathToRoot = args[0]
    val pathToMavenInstall = "$pathToRoot/${args[1]}"
    val pathToMavenDependenciesTextProto =
      "$pathToRoot/${args[2]}"
    val pathToProtoBinary = args[3]

    val mavenDependenciesListGenerator = MavenDependenciesListGenerator(licenseFetcher)

    val bazelQueryDepsList =
      mavenDependenciesListGenerator.retrieveThirdPartyMavenDependenciesList(pathToRoot)
    val mavenInstallDepsList = mavenDependenciesListGenerator.getDependencyListFromMavenInstall(
      pathToMavenInstall,
      bazelQueryDepsList
    )

    val dependenciesListFromPom = mavenDependenciesListGenerator
      .retrieveDependencyListFromPom(mavenInstallDepsList)
      .mavenDependencyList

    val dependenciesListFromTextProto =
      mavenDependenciesListGenerator.retrieveMavenDependencyList(pathToProtoBinary)

    val updatedDependneciesList = mavenDependenciesListGenerator.addChangesFromTextProto(
      dependenciesListFromPom,
      dependenciesListFromTextProto
    )

    val manuallyUpdatedLicenses =
      mavenDependenciesListGenerator.retrieveManuallyUpdatedLicensesSet(updatedDependneciesList)

    val finalDependenciesList = mavenDependenciesListGenerator.updateMavenDependenciesList(
      updatedDependneciesList,
      manuallyUpdatedLicenses
    )
    mavenDependenciesListGenerator.writeTextProto(
      pathToMavenDependenciesTextProto,
      MavenDependencyList.newBuilder().addAllMavenDependency(finalDependenciesList).build()
    )

    val licensesToBeFixed =
      mavenDependenciesListGenerator.getAllBrokenLicenses(finalDependenciesList)

    if (licensesToBeFixed.isNotEmpty()) {
      println("\nPlease verify the license link(s) for the following license(s) manually:")
      licensesToBeFixed.forEach {
        println("\nlicense_name: ${it.licenseName}")
        println("original_link: ${it.originalLink}")
        println("verified_link_case: ${it.verifiedLinkCase}")
        println("is_original_link_invalid: ${it.isOriginalLinkInvalid}")
      }
      throw Exception("Licenses details are not completed")
    }

    val dependenciesWithoutAnyLinks =
      mavenDependenciesListGenerator.getDependenciesThatNeedIntervention(finalDependenciesList)
    if (dependenciesWithoutAnyLinks.isNotEmpty()) {
      println(
        """
        Please remove all the invalid links (if any) for the below mentioned dependencies
        and provide the valid license links manually:
        """.trimIndent()
      )
      dependenciesWithoutAnyLinks.forEach { dependency ->
        println(dependency)
      }
      throw Exception("License links are invalid or not available for some dependencies")
    }
    println("\nScript executed succesfully: maven_dependencies.textproto updated successfully.")
  }
}
