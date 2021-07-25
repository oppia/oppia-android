package org.oppia.android.scripts.ci

import org.oppia.android.scripts.common.LicenseFetcherImpl
import org.oppia.android.scripts.common.MavenDependenciesListGenerator
import org.oppia.android.scripts.proto.MavenDependency

/**
 * The main entrypoint for verifying the list of third-party maven dependencies in
 * maven_dependencies.textproto is up-to-date.
 *
 * Usage:
 *   bazel run //scripts:verify_maven_dependencies_list_is_up_to_date -- <path_to_root>
 *   <path_to_maven_install_json> <path_to_maven_dependencies_pb>
 *
 *
 * @param [args]: Array of [String] containg different paths required by the script
 * - path_to_root: directory path to the root of the Oppia Android repository.
 * - path_to_maven_install_json: relative path to the maven_install.json file.
 * - path_to_maven_dependencies_pb: relative path to the maven_dependencies.pb file.
 *
 * Example:
 *   bazel run //scripts:generate_maven_dependencies_list -- $(pwd)
 *   third_party/maven_install.json scripts/assets/maven_dependencies.textproto
 *   scripts/assets/maven_dependencies.pb
 */
fun main(args: Array<String>) {
  val pathToRoot = args[0]
  val pathToMavenInstall = "$pathToRoot/${args[1]}"
  val pathToProtoBinary = args[2]
  val licenseFetcher = LicenseFetcherImpl()
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

  val dependenciesListFromTextProto = mavenDependenciesListGenerator
    .retrieveMavenDependencyList(pathToProtoBinary)

  val updatedDependneciesList = mavenDependenciesListGenerator.addChangesFromTextProto(
    dependenciesListFromPom,
    dependenciesListFromTextProto
  )

  val manuallyUpdatedLicenses = mavenDependenciesListGenerator
    .retrieveManuallyUpdatedLicensesSet(updatedDependneciesList)

  val finalDependenciesList = mavenDependenciesListGenerator.updateMavenDependenciesList(
    updatedDependneciesList,
    manuallyUpdatedLicenses
  )

  val brokenLicenses = mavenDependenciesListGenerator
    .getAllBrokenLicenses(finalDependenciesList)

  if (brokenLicenses.isNotEmpty()) {
    val licenseToDependencyMap = mavenDependenciesListGenerator
      .findFirstDependenciesWithBrokenLicenses(
        finalDependenciesList,
        brokenLicenses
      )
    println(
      """
      Some licenses do not have their 'original_link' verified. To verify a license link, click
      on the original link of the license and check if the link points to any valid license or
      not. If the link does not point to a valid license (e.g - https://fabric.io/terms),  
      set the 'is_original_link_invalid' field of the license to 'true'.
       
      e.g - 
      license {
        license_name: "Terms of Service for Firebase Services"
        original_link: "https://fabric.io/terms"
        is_original_link_invalid: true
      }

      If the link does point to a valid license then choose the most appropriate category for 
      the link:
      
      1. scrapable_link: If the license text is plain text and the URL mentioned can be scraped
      directly from the original_link of the license. e.g - 
      https://www.apache.org/licenses/LICENSE-2.0.txt
      
      2. extracted_copy_link: If the license text is plain text but it can not be scraped 
      directly from the original_link of the license. e.g -
      https://www.opensource.org/licenses/bsd-license
      
      3. direct_link_only: If the license text is not plain text, it's best to display only the
      link of the license. e.g - https://developer.android.com/studio/terms.html
      
      After identifying the category of the license, modify the license to include one of the
      above mentioned 'url'. 
      
      e.g - 
      license {
        license_name: "The Apache Software License, Version 2.0"
        original_link: "https://www.apache.org/licenses/LICENSE-2.0.txt"
        scrapable_link {
          url: "https://www.apache.org/licenses/LICENSE-2.0.txt"
        }
      }
      
      Please verify the license link(s) for the following license(s) manually in 
      maven_dependencies.textproto. Note that only the first dependency that contains the license 
      needs to be updated and also re-run the script to update the license details at all places.
      """.trimIndent() + "\n"
    )
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

  val dependenciesWithoutAnyLinks = mavenDependenciesListGenerator
    .getDependenciesThatNeedIntervention(finalDependenciesList)

  if (dependenciesWithoutAnyLinks.isNotEmpty()) {
    println(
      """
      Please remove all the invalid links (if any) from maven_dependencies.textproto for the 
      below mentioned dependencies and provide the valid license links manually.
      e.g - 
      
      maven_dependency {
        artifact_name: "com.google.guava:failureaccess:1.0.1"
        artifact_version: "1.0.1"
      }
      
      ***** changes to *****
      
      maven_dependency {
        artifact_name: "com.google.guava:failureaccess:1.0.1"
        artifact_version: "1.0.1"
        license {
          license_name: "The Apache Software License, Version 2.0"
          scrapable_link {
            url: "https://www.apache.org/licenses/LICENSE-2.0.txt"
          }
        }
      }
      
      Dependencies with invalid or no license links:
      """.trimIndent() + "\n"
    )
    dependenciesWithoutAnyLinks.forEach { dependency ->
      println(dependency)
    }
    throw Exception("License links are invalid or not available for some dependencies")
  }

  val redundantDependencies = findRedundantDependencies(
    finalDependenciesList,
    dependenciesListFromTextProto
  )
  val missindDependencies = findMissingDependencies(
    finalDependenciesList,
    dependenciesListFromTextProto
  )
  if (redundantDependencies.isNotEmpty()) {
    println("Please remove these redundant dependencies from maven_dependencies.textproto\n")
    redundantDependencies.forEach {
      println(it)
    }
  }
  if (missindDependencies.isNotEmpty()) {
    println("Please add these missing dependencies to maven_dependencies.textproto\n")
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
