package org.oppia.android.scripts.ci

import org.oppia.android.scripts.common.LicenseFetcherImpl
import org.oppia.android.scripts.common.MavenDependenciesListGenerator
import org.oppia.android.scripts.proto.MavenDependency

/**
 * The main entrypoint for verifying the list of third-party maven dependencies in
 * maven_dependencies.textproto is up-to-date.
 *
 * Usage:
 *   bazel run //scripts:compute_affected_tests --
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
  val pathToRoot = args[0]
  val pathToMavenInstall = "$pathToRoot/${args[1]}"
  val pathToMavenDependenciesTextProto = "$pathToRoot/${args[2]}"
  val pathToProtoBinary = args[3]
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

  val licensesToBeFixed =
    mavenDependenciesListGenerator.getAllBrokenLicenses(finalDependenciesList)

  if (licensesToBeFixed.isNotEmpty()) {
    val licenseToDependencyMap = mavenDependenciesListGenerator
      .findFirstDependenciesWithBrokenLicenses(
        finalDependenciesList,
        licensesToBeFixed
      )
    println(
      """
      Some licenses do not have their 'original_link' verified. To verify a license link, click
      on the original link of the license and check if the link points to any valid license or
      not. If the link does not point to a valid license (e.g - https://fabric.io/terms), set 
      the 'is_original_link_invalid' field of the license to 'true'.
       
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
      maven_dependencies.textproto. Note that only first dependency that contains the license 
      needs to be updated and also re-run the script to update the license details at all places.
      """.trimIndent()
    )
    licensesToBeFixed.forEach {
      println("\nlicense_name: ${it.licenseName}")
      println("original_link: ${it.originalLink}")
      println("verified_link_case: ${it.verifiedLinkCase}")
      println("is_original_link_invalid: ${it.isOriginalLinkInvalid}")
      println(
        "First dependency that should be updated with the license: " +
          "${licenseToDependencyMap[it]}\n"
      )
    }
    throw Exception("Licenses details are not completed")
  }

  val dependenciesWithoutAnyLinks =
    mavenDependenciesListGenerator.getDependenciesThatNeedIntervention(finalDependenciesList)
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

  val redundantDependencies =
    findRedundantDependencies(finalDependenciesList, dependenciesListFromTextProto)
  val missindDependencies =
    findMissingDependencies(finalDependenciesList, dependenciesListFromTextProto)
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
