package org.oppia.android.scripts.maven

import com.google.protobuf.TextFormat
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.oppia.android.scripts.maven.maveninstall.MavenListDependency
import org.oppia.android.scripts.maven.maveninstall.MavenListDependencyTree
import org.oppia.android.scripts.proto.License
import org.oppia.android.scripts.proto.MavenDependency
import org.oppia.android.scripts.proto.MavenDependencyList
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

/**
 * This class is just a wrapper for the main function so that it can be called via
 * [GenerateMavenDependenciesListWriter.kt].
 */
class MavenDependenciesListWriter() {
  companion object {
    private val MAVEN_PREFIX = "@maven//:"
    private val WAIT_PROCESS_TIMEOUT_MS = 60_000L

    private val LICENSES_TAG = "<licenses>"
    private val LICENSES_CLOSE_TAG = "</licenses>"
    private val LICENSE_TAG = "<license>"
    private val NAME_TAG = "<name>"
    private val URL_TAG = "<url>"

    lateinit var networkAndBazelUtils: NetworkAndBazelUtils

    @JvmStatic
    fun main(args: Array<String>) {
      if (args.size < 2) {
        throw Exception("Too few Arguments passed")
      }
      val pathToRoot = args[0]
      val pathToMavenInstall = "$pathToRoot/${args[1]}"
      val pathToMavenDependenciesTextProto =
        "$pathToRoot/scripts/assets/maven_dependencies.textproto"

      val bazelQueryDepsList = retrieveThirdPartyMavenDependenciesList(pathToRoot)
      val mavenInstallDepsList = getDependencyListFromMavenInstall(
        pathToMavenInstall,
        bazelQueryDepsList
      )

      val dependenciesListFromPom =
        retrieveDependencyListFromPom(mavenInstallDepsList).mavenDependencyList

      val dependenciesListFromTextproto = retrieveMavenDependencyList(pathToRoot)

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
      if (licensesToBeFixed.isNotEmpty()) {
        println("\nPlease complete all the details for the following licenses manually:")
        licensesToBeFixed.forEach {
          println("\nlicense_name: ${it.licenseName}")
          println("original_link: ${it.originalLink}")
          println("verified_link_case: ${it.verifiedLinkCase}")
          println("is_original_link_invalid: ${it.isOriginalLinkInvalid}")
        }
        throw Exception("Licenses details are not completed.")
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
        throw Exception(
          """
          There does not exist any license links (or the extracted license links are invalid) 
          for some dependencies.
          """.trimIndent()
        )
      }
      println("\nScript executed succesfully: maven_dependencies.textproto updated successfully.")
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
          !license.verifiedLinkCase.equals(License.VerifiedLinkCase.VERIFIEDLINK_NOT_SET)
        }
      }.toSet()
    }

    private fun getDependenciesThatNeedIntervention(
      mavenDependenciesList: List<MavenDependency>
    ): Set<MavenDependency> {
      return mavenDependenciesList.filter { dependency ->
        dependency.licenseList.isEmpty() ||
          dependency.licenseList.filter { license ->
            license.verifiedLinkCase.equals(License.VerifiedLinkCase.VERIFIEDLINK_NOT_SET) &&
              license.isOriginalLinkInvalid == true
          }.isNotEmpty()
      }.toSet()
    }

    private fun getAllBrokenLicenses(
      mavenDependenciesList: List<MavenDependency>
    ): Set<License> {
      return mavenDependenciesList.flatMap { dependency ->
        dependency.licenseList.filter { license ->
          license.verifiedLinkCase.equals(License.VerifiedLinkCase.VERIFIEDLINK_NOT_SET) &&
            license.isOriginalLinkInvalid == false
        }
      }.toSet()
    }

    private fun updateMavenDependenciesList(
      latestDependenciesList: List<MavenDependency>,
      manuallyUpdatedLicenses: Set<License>
    ): List<MavenDependency> {
      val finalUpdatedList = mutableListOf<MavenDependency>()
      latestDependenciesList.forEach { mavenDependency ->
        val updateLicenseList = mutableListOf<License>()
        mavenDependency.licenseList.forEach { license ->
          val updatedLicense = manuallyUpdatedLicenses.find {
            it.originalLink == license.originalLink && it.licenseName == license.licenseName
          }
          if (updatedLicense != null) {
            updateLicenseList.add(updatedLicense)
          } else {
            updateLicenseList.add(license)
          }
        }
        val dependency = MavenDependency.newBuilder().apply {
          this.artifactName = mavenDependency.artifactName
          this.artifactVersion = mavenDependency.artifactVersion
          this.addAllLicense(updateLicenseList)
        }.build()
        finalUpdatedList.add(dependency)
      }
      return finalUpdatedList
    }

    /** Retrieves the list of [MavenDependency] from maven_dependencies.textproto. */
    private fun retrieveMavenDependencyList(pathToRoot: String): List<MavenDependency> {
      return getProto(
        pathToRoot,
        MavenDependencyList.getDefaultInstance()
      ).mavenDependencyList
    }

    /**
     * Helper function to parse the textproto file to a proto class.
     *
     * @param pathToRoot name of the textproto file to be parsed
     * @param proto instance of the proto class
     * @return proto class from the parsed textproto file
     */
    private fun getProto(
      pathToRoot: String,
      proto: MavenDependencyList
    ): MavenDependencyList {
      return FileInputStream(File("$pathToRoot/scripts/assets/maven_dependnecies.pb")).use {
        proto.newBuilderForType().mergeFrom(it)
      }.build() as MavenDependencyList
    }

    private fun omitVersionAndReplaceColonsHyphensPeriods(artifactName: String): String {
//      val numberOfColons = artifactName.filter { it == ':' }.count()
//      if (numberOfColons != 2) {
//        throw Exception("Couldn't parse the version for the artifact \'$artifactName\'")
//      }
      val lastColonIndex = artifactName.lastIndexOf(':')
      return artifactName.substring(0, lastColonIndex).replace('.', '_').replace(':', '_')
        .replace('-', '_')
    }

    private fun genearateDependenciesListFromMavenInstall(
      pathToMavenInstall: String,
      bazelQueryDepsNames: List<String>
    ): List<MavenListDependency> {
      val mavenInstallJson = File(pathToMavenInstall)
      val mavenInstallJsonText =
        mavenInstallJson.inputStream().bufferedReader().use { it.readText() }
      val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
      val adapter = moshi.adapter(MavenListDependencyTree::class.java)
      val dependencyTree = adapter.fromJson(mavenInstallJsonText)
      val mavenInstallDependencyList = dependencyTree?.mavenListDependencies?.dependencyList
      return mavenInstallDependencyList?.filter { dep ->
        bazelQueryDepsNames.contains(omitVersionAndReplaceColonsHyphensPeriods(dep.coord))
      } ?: listOf<MavenListDependency>()
    }

    private fun writeTextProto(
      pathToTextProto: String,
      mavenDependencyList: MavenDependencyList
    ) {
      File(pathToTextProto).outputStream().bufferedWriter().use { writer ->
        TextFormat.printer().print(mavenDependencyList, writer)
      }
    }

    private fun retrieveDependencyListFromPom(
      finalDependenciesList: List<MavenListDependency>
    ): MavenDependencyList {
      val mavenDependencyList = mutableListOf<MavenDependency>()
      finalDependenciesList.forEach {
        // Remove ".jar" or ".aar" or any other extension from the specified url.
        val pomFileUrl = "${it.url?.dropLast(3)}pom"
        val artifactName = it.coord
        val artifactVersion = StringBuilder()
        var lastIndex = artifactName.length - 1
        while (lastIndex >= 0 && artifactName[lastIndex] != ':') {
          artifactVersion.append(artifactName[lastIndex])
          lastIndex--
        }
        artifactVersion.reverse()
        val pomFile = networkAndBazelUtils.scrapeText(pomFileUrl)
        val mavenDependency = MavenDependency.newBuilder().apply {
          this.artifactName = it.coord
          this.artifactVersion = artifactVersion.toString()
          this.addAllLicense(extractLicenseLinksFromPom(pomFile))
        }
        mavenDependencyList.add(mavenDependency.build())
      }
      return MavenDependencyList.newBuilder().addAllMavenDependency(mavenDependencyList).build()
    }

    private fun retrieveThirdPartyMavenDependenciesList(
      rootPath: String
    ): List<String> {
      return networkAndBazelUtils.retrieveThirdPartyMavenDependenciesList(rootPath).map { dep ->
        if (dep.startsWith(MAVEN_PREFIX)) dep.substring(MAVEN_PREFIX.length, dep.length) else dep
      }
    }

    private fun getDependencyListFromMavenInstall(
      pathToMavenInstall: String,
      bazelQueryDepsNames: List<String>
    ): List<MavenListDependency> {
      val mavenInstallJsonText =
        File(pathToMavenInstall).inputStream().bufferedReader().use { it.readText() }
      val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
      val adapter = moshi.adapter(MavenListDependencyTree::class.java)
      val dependencyTree = adapter.fromJson(mavenInstallJsonText)
      return dependencyTree?.mavenListDependencies?.dependencyList?.filter { dep ->
        bazelQueryDepsNames.contains(
          omitVersionAndReplaceColonsHyphensPeriods(dep.coord)
        )
      } ?: listOf<MavenListDependency>()
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
      var httpsUrl = url
      if (!url.startsWith("https")) {
        httpsUrl = url.replace("http", "https")
      }
      return httpsUrl
    }

    private fun extractLicenseLinksFromPom(
      pomText: String
    ): List<License> {
      val licenseList = mutableListOf<License>()
      val builderFactory = DocumentBuilderFactory.newInstance()
      val docBuilder = builderFactory.newDocumentBuilder()
      val doc: Document = docBuilder.parse(
        InputSource(ByteArrayInputStream(pomText.toByteArray(Charsets.UTF_8)))
      )

      val licenses = doc.getElementsByTagName("license")
      if (licenses.length > 0) {
        for (i in 0 until licenses.getLength()) {
          if (licenses.item(0).getNodeType().equals(Node.ELEMENT_NODE)) {
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
      }
      return licenseList.toList()

//      var cursor = -1
//      if (pomText.length > 11) {
//        for (index in 0..(pomText.length - 11)) {
//          if (pomText.substring(index, index + 10) == LICENSES_TAG) {
//            cursor = index + 9
//            break
//          }
//        }
//        if (cursor != -1) {
//          var cursor2 = cursor
//          while (cursor2 < (pomText.length - 12)) {
//            if (pomText.substring(cursor2, cursor2 + 9) == LICENSE_TAG) {
//              cursor2 += 9
//              while (cursor2 < pomText.length - 6 &&
//                pomText.substring(
//                  cursor2,
//                  cursor2 + 6
//                ) != NAME_TAG
//              ) {
//                ++cursor2
//              }
//              cursor2 += 6
//              val licenseUrlBuilder = StringBuilder()
//              val licenseNameBuilder = StringBuilder()
//              while (pomText[cursor2] != '<') {
//                licenseNameBuilder.append(pomText[cursor2])
//                ++cursor2
//              }
//              while (cursor2 < pomText.length - 4 &&
//                pomText.substring(
//                  cursor2,
//                  cursor2 + 5
//                ) != URL_TAG
//              ) {
//                ++cursor2
//              }
//              cursor2 += 5
//              while (pomText[cursor2] != '<') {
//                licenseUrlBuilder.append(pomText[cursor2])
//                ++cursor2
//              }
//              val httpUrl = replaceHttpWithHttps(licenseUrlBuilder)
//              licenseList.add(
//                License.newBuilder().apply {
//                  this.licenseName = licenseNameBuilder.toString()
//                  this.originalLink = httpUrl
//                }.build()
//              )
//            } else if (pomText.substring(cursor2, cursor2 + 12) == LICENSES_CLOSE_TAG) {
//              break
//            }
//            ++cursor2
//          }
//        }
//      }
//      return licenseList
    }
  }
}
