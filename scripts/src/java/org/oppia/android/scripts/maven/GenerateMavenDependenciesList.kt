package org.oppia.android.scripts.maven

import com.google.protobuf.TextFormat
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.oppia.android.scripts.common.BazelClient
import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.maven.data.MavenListDependency
import org.oppia.android.scripts.maven.data.MavenListDependencyTree
import org.oppia.android.scripts.proto.License
import org.oppia.android.scripts.proto.MavenDependency
import org.oppia.android.scripts.proto.MavenDependencyList
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.InputSource
import java.io.File
import java.io.FileInputStream
import javax.xml.parsers.DocumentBuilderFactory

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

    val bazelQueryDepsList = retrieveThirdPartyMavenDependenciesList(pathToRoot)
    val mavenInstallDepsList = getDependencyListFromMavenInstall(
      pathToMavenInstall,
      bazelQueryDepsList
    )

    val dependenciesListFromPom =
      retrieveDependencyListFromPom(mavenInstallDepsList).mavenDependencyList

    val dependenciesListFromTextproto = retrieveMavenDependencyList(pathToProtoBinary)

    val updatedDependneciesList = addChangesFromTextProto(
      dependenciesListFromPom,
      dependenciesListFromTextproto
    )

    val manuallyUpdatedLicenses = retrieveManuallyUpdatedLicensesSet(updatedDependneciesList)

    val finalDependenciesList = updateMavenDependenciesList(
      updatedDependneciesList,
      manuallyUpdatedLicenses
    )
    writeTextProto(
      pathToMavenDependenciesTextProto,
      MavenDependencyList.newBuilder().addAllMavenDependency(finalDependenciesList).build()
    )

    val licensesToBeFixed = getAllBrokenLicenses(finalDependenciesList)

    // TODO(#3486): Update GenerateMavenDependenciesList.kt to call out first coordinate name
    // that should be updated to update all occurrences of the license.
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

    val dependenciesWithoutAnyLinks = getDependenciesThatNeedIntervention(finalDependenciesList)
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

  private fun retrieveThirdPartyMavenDependenciesList(
    rootPath: String
  ): List<String> {
    return BazelClient(File(rootPath), commandExecutor)
      .retrieveThirdPartyMavenDepsListForBinary("//:oppia")
      .map { dep ->
        dep.removePrefix(MAVEN_PREFIX)
      }
  }

  private fun addChangesFromTextProto(
    dependencyListFromPom: List<MavenDependency>,
    dependencyListFromProto: List<MavenDependency>
  ): List<MavenDependency> {
    return dependencyListFromPom.map { dependency ->
      dependencyListFromProto.find {
        it.artifactName == dependency.artifactName
      } ?: dependency
    }
  }

  private fun retrieveManuallyUpdatedLicensesSet(
    mavenDependenciesList: List<MavenDependency>
  ): Set<License> {
    return mavenDependenciesList.flatMap { dependency ->
      dependency.licenseList.filter { license ->
        license.verifiedLinkCase != License.VerifiedLinkCase.VERIFIEDLINK_NOT_SET
      }
    }.toSet()
  }

  private fun updateMavenDependenciesList(
    latestDependenciesList: List<MavenDependency>,
    manuallyUpdatedLicenses: Set<License>
  ): List<MavenDependency> {
    return latestDependenciesList.map { mavenDependency ->
      val updatedLicenseList = mavenDependency.licenseList.map { license ->
        manuallyUpdatedLicenses.find {
          it.originalLink == license.originalLink && it.licenseName == license.licenseName
        } ?: license
      }
      MavenDependency.newBuilder().apply {
        this.artifactName = mavenDependency.artifactName
        this.artifactVersion = mavenDependency.artifactVersion
        this.addAllLicense(updatedLicenseList)
      }.build()
    }
  }

  private fun writeTextProto(
    pathToTextProto: String,
    mavenDependencyList: MavenDependencyList
  ) {
    File(pathToTextProto).outputStream().bufferedWriter().use { writer ->
      TextFormat.printer().print(mavenDependencyList, writer)
    }
  }

  // TODO(#3486): Update GenerateMavenDependenciesList.kt to call out first coordinate name
  // that should be updated to update all occurrences of the license.
  private fun getAllBrokenLicenses(
    mavenDependenciesList: List<MavenDependency>
  ): Set<License> {
    // Here broken licenses are those licenses that do not have verified_link set or
    // there original link is found to be invalid by the developers.
    return mavenDependenciesList.flatMap { dependency ->
      dependency.licenseList.filter { license ->
        license.verifiedLinkCase.equals(License.VerifiedLinkCase.VERIFIEDLINK_NOT_SET) &&
          !license.isOriginalLinkInvalid
      }
    }.toSet()
  }

  private fun getDependenciesThatNeedIntervention(
    mavenDependenciesList: List<MavenDependency>
  ): Set<MavenDependency> {
    // The dependencies whose license list is empty or some of their license link was found to
    // be invalid need intervention. In this case, the developer needs to manually fill in
    // the license links for each of these dependencies.
    return mavenDependenciesList.filter { dependency ->
      dependency.licenseList.isEmpty() ||
        dependency.licenseList.filter { license ->
          license.verifiedLinkCase.equals(License.VerifiedLinkCase.VERIFIEDLINK_NOT_SET) &&
            license.isOriginalLinkInvalid
        }.isNotEmpty()
    }.toSet()
  }

  /** Retrieves the list of [MavenDependency] from maven_dependencies.textproto. */
  private fun retrieveMavenDependencyList(pathToPbFile: String): List<MavenDependency> {
    return getProto(
      pathToPbFile,
      MavenDependencyList.getDefaultInstance()
    ).mavenDependencyList
  }

  /**
   * Helper function to parse the textProto file to a proto class.
   *
   * @param pathToPbFile path to the pb file to be parsed
   * @param proto instance of the proto class
   * @return proto class from the parsed textProto file
   */
  private fun getProto(
    pathToPbFile: String,
    proto: MavenDependencyList
  ): MavenDependencyList {
    return FileInputStream(File(pathToPbFile)).use {
      proto.newBuilderForType().mergeFrom(it)
    }.build() as MavenDependencyList
  }

  private fun genearateDependenciesListFromMavenInstall(
    pathToMavenInstall: String,
    bazelQueryDepsNames: List<String>
  ): List<MavenListDependency> {
    val dependencyTree = retrieveDependencyTree(pathToMavenInstall)
    val mavenInstallDependencyList = dependencyTree.mavenListDependencies.dependencyList
    return mavenInstallDependencyList.filter { dep ->
      bazelQueryDepsNames.contains(omitVersionAndReplaceColonsHyphensPeriods(dep.coord))
    }
  }

  private fun omitVersionAndReplaceColonsHyphensPeriods(artifactName: String): String {
    return artifactName.substring(0, artifactName.lastIndexOf(':'))
      .replace('.', '_')
      .replace(':', '_')
      .replace('-', '_')
  }

  private fun retrieveDependencyListFromPom(
    finalDependenciesList: List<MavenListDependency>
  ): MavenDependencyList {
    val mavenDependencyList = finalDependenciesList.map { it ->
      // Remove ".jar" or ".aar" or any other extension from the specified url.
      val pomFileUrl = "${it.url?.substringBeforeLast('.')}.pom"
      val artifactName = it.coord
      val artifactVersion = artifactName.substringAfterLast(':')
      val pomFile = licenseFetcher.scrapeText(pomFileUrl)
      val mavenDependency = MavenDependency.newBuilder().apply {
        this.artifactName = it.coord
        this.artifactVersion = artifactVersion.toString()
        this.addAllLicense(extractLicenseLinksFromPom(pomFile))
      }
      mavenDependency.build()
    }
    return MavenDependencyList.newBuilder().addAllMavenDependency(mavenDependencyList).build()
  }

  private fun extractLicenseLinksFromPom(
    pomText: String
  ): List<License> {
    val licenseList = mutableListOf<License>()
    val builderFactory = DocumentBuilderFactory.newInstance()
    val docBuilder = builderFactory.newDocumentBuilder()
    val doc = docBuilder.parse(InputSource(pomText.byteInputStream()))

    val licenses = doc.getElementsByTagName("license")
    for (i in 0 until licenses.getLength()) {
      if (licenses.item(0).getNodeType() == Node.ELEMENT_NODE) {
        val element = licenses.item(i) as Element
        val licenseName = getNodeValue("name", element)
        val licenseLink = replaceHttpWithHttps(getNodeValue("url", element))
        licenseList.add(
          License.newBuilder().apply {
            this.licenseName = licenseName
            this.originalLink = licenseLink
          }.build()
        )
      }
    }
    return licenseList
  }

  private fun getDependencyListFromMavenInstall(
    pathToMavenInstall: String,
    bazelQueryDepsNames: List<String>
  ): List<MavenListDependency> {
    val dependencyTree = retrieveDependencyTree(pathToMavenInstall)
    return dependencyTree.mavenListDependencies.dependencyList.filter { dep ->
      omitVersionAndReplaceColonsHyphensPeriods(dep.coord) in bazelQueryDepsNames
    }
  }

  private fun retrieveDependencyTree(pathToMavenInstall: String): MavenListDependencyTree {
    val mavenInstallJsonText =
      File(pathToMavenInstall).inputStream().bufferedReader().use { it.readText() }
    val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    val adapter = moshi.adapter(MavenListDependencyTree::class.java)
    return adapter.fromJson(mavenInstallJsonText)
      ?: throw Exception("Failed to parse $pathToMavenInstall")
  }

  private fun getNodeValue(tag: String, element: Element): String {
    val nodeList = element.getElementsByTagName(tag)
    val node = nodeList.item(0)
    if (node != null) {
      if (node.hasChildNodes()) {
        val child = node.getFirstChild()
        while (child != null) {
          if (child.getNodeType() === Node.TEXT_NODE) {
            return child.getNodeValue()
          }
        }
      }
    }
    return ""
  }

  private fun replaceHttpWithHttps(
    url: String
  ): String {
    return url.replaceFirst("http://", "https://")
  }
}
