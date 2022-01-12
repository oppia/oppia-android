package org.oppia.android.scripts.maven

import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.license.LicenseFetcher
import org.oppia.android.scripts.license.LicenseFetcherImpl
import org.oppia.android.scripts.license.MavenDependenciesRetriever
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
 * - path_to_maven_install_json: relative path to the Maven install manifest file.
 * - path_to_maven_dependencies_textproto: relative path to the maven_dependencies.textproto
 * - path_to_maven_dependencies_pb: relative path to the maven_dependencies.pb file.
 * Example:
 *   bazel run //scripts:generate_maven_dependencies_list -- $(pwd)
 *   third_party/maven_prod_install.json scripts/assets/maven_dependencies.textproto
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
  /**
   * Compiles a list of third-party Maven dependencies along with their license links on
   * which Oppia Android depends and write them in maven_dependencies.textproto.
   */
  fun main(args: Array<String>) {
    if (args.size < 4) {
      throw Exception("Too few Arguments passed")
    }
    val pathToRoot = args[0]
    val pathToMavenInstallJson = "$pathToRoot/${args[1]}"
    val pathToMavenDependenciesTextProto = "$pathToRoot/${args[2]}"
    val pathToMavenDependenciesPb = args[3]

    val MavenDependenciesRetriever = MavenDependenciesRetriever(pathToRoot, licenseFetcher)

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
      MavenDependenciesRetriever.retrieveMavenDependencyList(pathToMavenDependenciesPb)

    val updatedDependenciesList = MavenDependenciesRetriever.addChangesFromTextProto(
      dependenciesListFromPom,
      dependenciesListFromTextProto
    )

    val manuallyUpdatedLicenses =
      MavenDependenciesRetriever.retrieveManuallyUpdatedLicensesSet(updatedDependenciesList)

    val finalDependenciesList = MavenDependenciesRetriever.updateMavenDependenciesList(
      updatedDependenciesList,
      manuallyUpdatedLicenses
    )
    MavenDependenciesRetriever.writeTextProto(
      pathToMavenDependenciesTextProto,
      MavenDependencyList.newBuilder().addAllMavenDependency(finalDependenciesList).build()
    )

    val licensesToBeFixed =
      MavenDependenciesRetriever.getAllBrokenLicenses(finalDependenciesList)

    if (licensesToBeFixed.isNotEmpty()) {
      val licenseToDependencyMap = MavenDependenciesRetriever
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
      MavenDependenciesRetriever.getDependenciesThatNeedIntervention(finalDependenciesList)
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
    println("\nScript executed succesfully: maven_dependencies.textproto updated successfully.")
  }
}
