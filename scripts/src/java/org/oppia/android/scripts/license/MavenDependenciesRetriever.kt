package org.oppia.android.scripts.license

import com.google.protobuf.TextFormat
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.oppia.android.scripts.common.BazelClient
import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import org.oppia.android.scripts.maven.model.MavenInstallJson
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

/** Helper to compile the third-party Maven dependencies list for Oppia Android. */
class MavenDependenciesRetriever(
  private val rootPath: String,
  private val mavenArtifactPropertyFetcher: MavenArtifactPropertyFetcher,
  private val coroutineDispatcher: ScriptBackgroundCoroutineDispatcher,
  private val commandExecutor: CommandExecutor
) {
  private val bazelClient by lazy { BazelClient(File(rootPath), commandExecutor) }
  private val coroutineScope by lazy { CoroutineScope(coroutineDispatcher) }

  /** Returns the list of third-party dependency names per Bazel. */
  fun retrieveThirdPartyMavenDependenciesList(): List<String> {
    return bazelClient
      .retrieveThirdPartyMavenDepsListForBinary("//:oppia")
      .map { it.removePrefix(MAVEN_PREFIX) }
  }

  /**
   * Merges manual updates from the textproto file to the list of dependencies
   * compiled with the help of Bazel Query and POM file links.
   *
   * @param dependencyListFromPom list of dependencies updated with the help of Bazel Query and
   *    POM file links
   * @param dependencyListFromProto instance of the proto class
   * @return list of dependencies where some dependencies contains manually provided updates
   */
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

  /** Returns the set of licenses whose `original_link` has been verified manually. */
  fun retrieveManuallyUpdatedLicensesSet(
    mavenDependenciesList: List<MavenDependency>
  ): Set<License> {
    return mavenDependenciesList.flatMap { dependency ->
      dependency.licenseList.filter { license ->
        license.verifiedLinkCase != License.VerifiedLinkCase.VERIFIEDLINK_NOT_SET
      }
    }.toSet()
  }

  /**
   * Helper function to update all dependencies' licenses that have been verified manually.
   *
   * @param latestDependenciesList list of dependencies that has some dependencies with verified
   *    licenses
   * @param manuallyUpdatedLicenses set of licenses that have been updated manually
   * @return list of dependencies where all dependencies' licenses contain manually provided
   *    updates
   */
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

  /**
   * Writes the list of final list of dependencies to the maven_dependencies.textproto file.
   *
   * @param pathToTextProto path to the maven_dependencies.textproto file
   * @param mavenDependencyList final list of dependencies
   */
  fun writeTextProto(
    pathToTextProto: String,
    mavenDependencyList: MavenDependencyList
  ) {
    File(pathToTextProto).outputStream().bufferedWriter().use { writer ->
      TextFormat.printer().print(mavenDependencyList, writer)
    }
  }

  /**
   * Returns the set of licenses that do not have verified_link set and there original link is
   * not set to be invalid by the developers.
   */
  fun getAllBrokenLicenses(
    mavenDependenciesList: List<MavenDependency>
  ): Set<License> {
    // Here broken licenses are those licenses that do not have verified_link set and
    // there original link is not set to be invalid by the developers.
    return mavenDependenciesList.flatMap { dependency ->
      dependency.licenseList.filter { license ->
        license.verifiedLinkCase.equals(License.VerifiedLinkCase.VERIFIEDLINK_NOT_SET) &&
          !license.isOriginalLinkInvalid
      }
    }.toSet()
  }

  /**
   * Generates a map that maps broken licenses to the first dependency that should be updated
   * manually in order to update all occurences of this license.
   *
   * @param mavenDependenciesList final list of dependencies
   * @param brokenLicenses set of licenses that do not have verified_link set and there original
   *    link is not set to be invalid by the developers.
   * @return map that maps a broken license to the first dependency in maven_dependencies.textproto
   */
  fun findFirstDependenciesWithBrokenLicenses(
    mavenDependenciesList: List<MavenDependency>,
    brokenLicenses: Set<License>
  ): Map<License, String> {
    return brokenLicenses.associateWith { license ->
      mavenDependenciesList.first { dependency -> license in dependency.licenseList }.artifactName
    }
  }

  /**
   * Returns the set of dependencies whose license list is empty or some of their license link was
   * found to be invalid.
   */
  fun getDependenciesThatNeedIntervention(
    mavenDependenciesList: List<MavenDependency>
  ): Set<MavenDependency> {
    // The dependencies whose license list is empty or some of their license link was found to
    // be invalid need further intervention. In this case, the developer needs to manually fill in
    // the license links for each of these dependencies.
    return mavenDependenciesList.filter { dependency ->
      dependency.licenseList.isEmpty() || dependency.licenseList.any { license ->
        license.verifiedLinkCase.equals(License.VerifiedLinkCase.VERIFIEDLINK_NOT_SET) &&
          license.isOriginalLinkInvalid
      }
    }.toSet()
  }

  /**
   * Retrieves the list of Maven dependencies from maven_dependencies.textproto.
   *
   * @param pathToPbFile path to the pb file to be parsed
   * @return list of dependencies
   */
  fun retrieveMavenDependencyList(pathToPbFile: String): List<MavenDependency> {
    return parseTextProto(
      pathToPbFile,
      MavenDependencyList.getDefaultInstance()
    ).mavenDependencyList
  }

  /**
   * Extracts the license names and license links of the dependencies from their corresponding POM
   * files.
   *
   * @param finalDependenciesList list of dependencies that is obtained by the intersection of
   *    the list generated by Bazel Query and the list generated from the Maven install manifest
   * @return mavenDependencyList that has dependencies with licenses extracted from their POM files
   */
  fun retrieveDependencyListFromPom(
    finalDependenciesList: List<MavenListDependency>
  ): MavenDependencyList {
    val mavenDependencyList = finalDependenciesList.map {
      val repoBaseUrl = it.repoUrls.firstOrNull() ?: error("No repo URL found for artifact: $it.")
      val pomFileUrl = it.coord.computePomUrl(repoBaseUrl)
      val pomFile = mavenArtifactPropertyFetcher.scrapeText(pomFileUrl)
      val mavenDependency = MavenDependency.newBuilder().apply {
        this.artifactName = it.coord.reducedCoordinateString
        this.artifactVersion = it.coord.version
        this.addAllLicense(extractLicenseLinksFromPom(pomFile))
      }
      mavenDependency.build()
    }
    return MavenDependencyList.newBuilder().addAllMavenDependency(mavenDependencyList).build()
  }

  /**
   * Parses the Maven install manifest file to compile the list of Maven dependencies.
   *
   * @param pathToMavenInstall path to the Maven install manifest file
   * @param bazelQueryDepsNames list of dependency names obtained from the bazel query
   * @return list of [MavenListDependency]s that contains the artifact name and a URL that is used
   *    to obtain the URL of the POM file of the dependency
   */
  suspend fun generateDependenciesListFromMavenInstall(
    pathToMavenInstall: String,
    bazelQueryDepsNames: List<String>
  ): List<MavenListDependency> {
    return computeMavenDependencies(pathToMavenInstall).filter { dep ->
      dep.coord.bazelTarget in bazelQueryDepsNames
    }
  }

  /**
   * Parses the text proto file to a proto class.
   *
   * @param pathToPbFile path to the pb file to be parsed
   * @param proto instance of the proto class
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

  private fun extractLicenseLinksFromPom(
    pomText: String
  ): List<License> {
    val licenseList = mutableListOf<License>()
    val builderFactory = DocumentBuilderFactory.newInstance()
    val docBuilder = builderFactory.newDocumentBuilder()
    val doc = docBuilder.parse(InputSource(pomText.byteInputStream()))

    val licenses = doc.getElementsByTagName("license")
    for (i in 0 until licenses.length) {
      if (licenses.item(0).nodeType == Node.ELEMENT_NODE) {
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

  private suspend fun computeMavenDependencies(
    pathToMavenInstall: String
  ): List<MavenListDependency> {
    val mavenInstallJson = parseMavenInstallJson(pathToMavenInstall)
    val artifactPartialCoordToRepoUrls =
      mavenInstallJson.repositories.entries.flatMap { (repoBaseUrl, arifactCoordStrs) ->
        arifactCoordStrs.map { it to repoBaseUrl }
      }.groupBy { (artifactCoordStr, _) ->
        artifactCoordStr
      }.mapValues { (_, coordUrlToRepoUrlPairs) -> coordUrlToRepoUrlPairs.map { it.second } }

    val coordToPossibleUrls = mavenInstallJson.artifacts.map { (partialCoord, artifact) ->
      val coord = MavenCoordinate.parseFrom("$partialCoord:${artifact.version}")
      return@map coord to artifactPartialCoordToRepoUrls.getValue(partialCoord)
    }
    val filteredCoordToPossibleUrls = coordToPossibleUrls.map { (coord, possibleUrls) ->
      coroutineScope.async {
        coord to possibleUrls.filter {
          // Run blocking I/O operations on the I/O thread pool.
          withContext(Dispatchers.IO) {
            mavenArtifactPropertyFetcher.isValidArtifactFileUrl(coord.computeArtifactUrl(it))
          }
        }
      }
    }
    // Wait for all repo URL checks to finish (parallely).
    return runBlocking {
      filteredCoordToPossibleUrls.awaitAll().map { (coord, validRepoUrls) ->
        MavenListDependency(coord, validRepoUrls)
      }
    }
  }

  private fun parseMavenInstallJson(pathToMavenInstall: String): MavenInstallJson {
    val mavenInstallJsonText =
      File(pathToMavenInstall).inputStream().bufferedReader().use { it.readText() }
    val moshi = Moshi.Builder().build()
    val adapter = moshi.adapter(MavenInstallJson::class.java)
    return adapter.fromJson(mavenInstallJsonText) ?: error("Failed to parse $pathToMavenInstall.")
  }

  private fun getNodeValue(tag: String, element: Element): String {
    val nodeList = element.getElementsByTagName(tag)
    val node = nodeList.item(0)
    if (node != null) {
      if (node.hasChildNodes()) {
        val child = node.firstChild
        while (child != null) {
          if (child.nodeType == Node.TEXT_NODE) {
            return child.nodeValue
          }
        }
      }
    }
    return ""
  }

  private fun replaceHttpWithHttps(url: String): String = url.replaceFirst("http://", "https://")

  /**
   * Represents a single downloadable maven_install_json dependency.
   *
   * @property coord the [MavenCoordinate] of the dependency
   * @property repoUrls a list of Maven repository URLs from which the dependency may be downloaded.
   *     Note that these repositories have been confirmed to include this specific dependency.
   */
  data class MavenListDependency(val coord: MavenCoordinate, val repoUrls: List<String>)
}
