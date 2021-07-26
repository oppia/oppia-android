package org.oppia.android.scripts.common

import com.google.protobuf.TextFormat
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
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

private const val MAVEN_PREFIX = "@maven//:"

class MavenDependenciesListGenerator(
  val licenseFetcher: LicenseFetcher,
  val commandExecutor: CommandExecutor = CommandExecutorImpl()
) {

  fun retrieveThirdPartyMavenDependenciesList(
    rootPath: String
  ): List<String> {
    return BazelClient(File(rootPath), commandExecutor)
      .retrieveThirdPartyMavenDepsListForBinary("//:oppia")
      .map { dep ->
        dep.removePrefix(MAVEN_PREFIX)
      }
  }

  fun addChangesFromTextProto(
    dependencyListFromPom: List<MavenDependency>,
    dependencyListFromProto: List<MavenDependency>
  ): List<MavenDependency> {
    return dependencyListFromPom.map { dependency ->
      dependencyListFromProto.find {
        it.artifactName == dependency.artifactName
      } ?: dependency
    }
  }

  fun retrieveManuallyUpdatedLicensesSet(
    mavenDependenciesList: List<MavenDependency>
  ): Set<License> {
    return mavenDependenciesList.flatMap { dependency ->
      dependency.licenseList.filter { license ->
        license.verifiedLinkCase != License.VerifiedLinkCase.VERIFIEDLINK_NOT_SET
      }
    }.toSet()
  }

  fun updateMavenDependenciesList(
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

  fun writeTextProto(
    pathToTextProto: String,
    mavenDependencyList: MavenDependencyList
  ) {
    File(pathToTextProto).outputStream().bufferedWriter().use { writer ->
      TextFormat.printer().print(mavenDependencyList, writer)
    }
  }

  fun getAllBrokenLicenses(
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

  fun findFirstDependenciesWithBrokenLicenses(
    mavenDependenciesList: List<MavenDependency>,
    brokenLicenses: Set<License>
  ): Map<License, String> {
    return brokenLicenses.associateTo(mutableMapOf<License, String>()) { license ->
      license to mavenDependenciesList.first { dependency ->
        dependency.licenseList.contains(license)
      }.artifactName
    }
  }

  fun getDependenciesThatNeedIntervention(
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

  /**
   * Retrieve the list of maven dependencies from maven_dependencies.textproto.
   *
   * @param pathToPbFile path to the pb file to be parsed
   *
   * @return list of [MavenDependency]
   */
  fun retrieveMavenDependencyList(pathToPbFile: String): List<MavenDependency> {
    return parseTextProto(
      pathToPbFile,
      MavenDependencyList.getDefaultInstance()
    ).mavenDependencyList
  }

  fun getDependencyListFromMavenInstall(
    pathToMavenInstall: String,
    bazelQueryDepsNames: List<String>
  ): List<MavenListDependency> {
    val dependencyTree = retrieveDependencyTree(pathToMavenInstall)
    return dependencyTree.mavenListDependencies.dependencyList.filter { dep ->
      omitVersionAndReplaceColonsHyphensPeriods(dep.coord) in bazelQueryDepsNames
    }
  }

  fun retrieveDependencyListFromPom(
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

  fun genearateDependenciesListFromMavenInstall(
    pathToMavenInstall: String,
    bazelQueryDepsNames: List<String>
  ): List<MavenListDependency> {
    val dependencyTree = retrieveDependencyTree(pathToMavenInstall)
    val mavenInstallDependencyList = dependencyTree.mavenListDependencies.dependencyList
    return mavenInstallDependencyList.filter { dep ->
      bazelQueryDepsNames.contains(omitVersionAndReplaceColonsHyphensPeriods(dep.coord))
    }
  }

  /**
   * Helper function to parse the text proto file to a proto class.
   *
   * @param pathToPbFile path to the pb file to be parsed
   * @param proto instance of the proto class
   *
   * @return proto class from the parsed text proto file
   */
  private fun parseTextProto(
    pathToPbFile: String,
    proto: MavenDependencyList
  ): MavenDependencyList {
    return FileInputStream(File(pathToPbFile)).use {
      proto.newBuilderForType().mergeFrom(it)
    }.build() as MavenDependencyList
  }

  private fun omitVersionAndReplaceColonsHyphensPeriods(artifactName: String): String {
    return artifactName.substring(0, artifactName.lastIndexOf(':'))
      .replace('.', '_')
      .replace(':', '_')
      .replace('-', '_')
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
