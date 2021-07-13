package org.oppia.android.scripts.maven

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.oppia.android.scripts.maven.maveninstall.MavenListDependency
import org.oppia.android.scripts.maven.maveninstall.MavenListDependencyTree
import org.oppia.android.scripts.proto.License
import org.oppia.android.scripts.proto.MavenDependency
import org.oppia.android.scripts.proto.MavenDependencyList
import org.oppia.android.scripts.proto.OriginOfLicenses
import org.oppia.android.scripts.proto.PrimaryLinkType
import java.io.File
import java.io.FileInputStream
import java.net.URL

class MavenDependenciesListWriter() {
  companion object {

    val MAVEN_PREFIX_LENGTH = "@maven//:".length
    val WAIT_PROCESS_TIMEOUT_MS = 60_000L

    val LICENSES_TAG = "<licenses>"
    val LICENSES_CLOSE_TAG = "</licenses>"
    val LICENSE_TAG = "<license>"
    val NAME_TAG = "<name>"
    val URL_TAG = "<url>"

    lateinit var utilityProvider: UtilityProvider

    @JvmStatic
    fun main(args: Array<String>) {

      val pathToRoot = args[0]
      val pathToMavenInstall = "$pathToRoot/${args[1]}"
      val pathToMavenDependenciesTextProto = "$pathToRoot/${args[2]}"
      val pathToMavenDependenciesProtoBinary = args[3]

      val bazelQueryDepsList = retrieveThirdPartyMavenDependenciesList(pathToRoot)
      val mavenInstallDepsList = getDependencyListFromMavenInstall(
          pathToMavenInstall,
          bazelQueryDepsList
        )

      val dependenciesListFromPom =
        provideDependencyListFromPom(mavenInstallDepsList).mavenDependencyList

      val dependenciesListFromTextproto = retrieveMavenDependencyList(
        pathToMavenDependenciesProtoBinary
      )

      val updatedDependneciesList = addChangesFromTextProto(
        dependenciesListFromPom,
        dependenciesListFromTextproto
      )

      val finalLicensesSet = retrieveUpdatedLicensesSet(
        updatedDependneciesList
      )

      val finalDependenciesList = updateMavenDependenciesList(
        updatedDependneciesList,
        finalLicensesSet
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
          println("primary_link: ${it.primaryLink}")
          println("primary_link_type: ${it.primaryLinkType}")
          println("alternative_link: ${it.alternativeLink}")
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
      val updatedDepdenciesList = mutableListOf<MavenDependency>()
      dependencyListFromPom.forEach { dependency ->
        val updatedDependency =
          dependencyListFromProto.find { it.artifactName == dependency.artifactName }
        if (updatedDependency != null) {
          updatedDepdenciesList.add(updatedDependency)
        } else {
          updatedDepdenciesList.add(dependency)
        }
      }
      return updatedDepdenciesList.toList()
    }

    private fun retrieveUpdatedLicensesSet(
      mavenDependenciesList: List<MavenDependency>
    ): Set<License> {
      val licenseSet = mutableSetOf<License>()
      mavenDependenciesList.forEach { dependency ->
        dependency.licenseList.forEach { license ->
          val foundLicense: License? = licenseSet.find { it.primaryLink == license.primaryLink }
          if (foundLicense == null) {
            licenseSet.add(license)
          } else if (
            license.primaryLinkType != PrimaryLinkType.PRIMARY_LINK_TYPE_UNSPECIFIED &&
            license.primaryLinkType != PrimaryLinkType.UNRECOGNIZED
          ) {
            licenseSet.remove(foundLicense)
            licenseSet.add(license)
          }
        }
      }
      return licenseSet
    }

    private fun getDependenciesThatNeedIntervention(
      mavenDependenciesList: List<MavenDependency>
    ): Set<MavenDependency> {
      return mavenDependenciesList.filter { dependency ->
        dependency.licenseList.isEmpty() ||
          dependency.licenseList.filter { license ->
            license.primaryLinkType == PrimaryLinkType.NEEDS_INTERVENTION
          }.isNotEmpty()
      }.toSet()
    }

    private fun getAllBrokenLicenses(
      mavenDependenciesList: List<MavenDependency>
    ): Set<License> {
      return mavenDependenciesList.flatMap { dependency ->
        dependency.licenseList.filter { license ->
          license.primaryLinkType == PrimaryLinkType.PRIMARY_LINK_TYPE_UNSPECIFIED ||
            license.primaryLinkType == PrimaryLinkType.UNRECOGNIZED ||
            (
              license.primaryLinkType == PrimaryLinkType.SCRAPE_FROM_LOCAL_COPY &&
                license.alternativeLink.isEmpty()
              )
        }
      }.toSet()
    }

    private fun updateMavenDependenciesList(
      latestDependenciesList: List<MavenDependency>,
      finalLicensesSet: Set<License>
    ): MutableList<MavenDependency> {
      val finalUpdatedList = mutableListOf<MavenDependency>()

      latestDependenciesList.forEach { mavenDependency ->
        val updateLicenseList = mutableListOf<License>()
        var numberOfLicensesThatRequireHumanEffort = 0
        val origin = mavenDependency.originOfLicense
        mavenDependency.licenseList.forEach { license ->
          val updatedLicense = finalLicensesSet.find { it.primaryLink == license.primaryLink }
          if (updatedLicense != null) {
            updateLicenseList.add(updatedLicense)
            if (
              updatedLicense.primaryLinkType == PrimaryLinkType.NEEDS_INTERVENTION ||
              updatedLicense.primaryLinkType == PrimaryLinkType.SCRAPE_FROM_LOCAL_COPY
            ) {
              numberOfLicensesThatRequireHumanEffort++
            }
          } else {
            if (license.primaryLinkType == PrimaryLinkType.NEEDS_INTERVENTION) {
              numberOfLicensesThatRequireHumanEffort++
            }
            updateLicenseList.add(license)
          }
        }
        val dependency = MavenDependency
          .newBuilder()
          .setArtifactName(mavenDependency.artifactName)
          .setArtifactVersion(mavenDependency.artifactVersion)
          .addAllLicense(updateLicenseList)
        if (origin != OriginOfLicenses.UNKNOWN) {
          dependency.setOriginOfLicense(origin)
        } else {
          if (updateLicenseList.isNotEmpty()) {
            if (numberOfLicensesThatRequireHumanEffort == updateLicenseList.size) {
              dependency.originOfLicense = OriginOfLicenses.MANUAL
            } else if (numberOfLicensesThatRequireHumanEffort == 0) {
              dependency.originOfLicense = OriginOfLicenses.ENTIRELY_FROM_POM
            } else if (numberOfLicensesThatRequireHumanEffort != updateLicenseList.size) {
              dependency.originOfLicense = OriginOfLicenses.PARTIALLY_FROM_POM
            }
          }
        }
        finalUpdatedList.add(dependency.build())
      }
      return finalUpdatedList
    }

    /** Retrieves the list of [MavenDependency] from maven_dependencies.textproto. */
    private fun retrieveMavenDependencyList(pathToProtoBinary: String): List<MavenDependency> {
      return getProto(
        pathToProtoBinary,
        MavenDependencyList.getDefaultInstance()
      ).mavenDependencyList.toList()
    }

    /**
     * Helper function to parse the textproto file to a proto class.
     *
     * @param textProtoFileName name of the textproto file to be parsed
     * @param proto instance of the proto class
     * @return proto class from the parsed textproto file
     */
    private fun getProto(
      pathToTextProto: String,
      proto: MavenDependencyList
    ): MavenDependencyList {
      val protoBinaryFile = File(pathToTextProto)
      val builder = proto.newBuilderForType()
      val protoObject = FileInputStream(protoBinaryFile).use {
        builder.mergeFrom(it)
      }.build() as MavenDependencyList
      return protoObject
    }

    private fun parseArtifactName(artifactName: String): String {
      var colonIndex = artifactName.length - 1
      while (artifactName.isNotEmpty() && artifactName[colonIndex] != ':') {
        colonIndex--
      }
      val artifactNameWithoutVersion = artifactName.substring(0, colonIndex)
      val parsedArtifactNameBuilder = StringBuilder()
      for (index in artifactNameWithoutVersion.indices) {
        if (artifactNameWithoutVersion[index] == '.' || artifactNameWithoutVersion[index] == ':' ||
          artifactNameWithoutVersion[index] == '-'
        ) {
          parsedArtifactNameBuilder.append(
            '_'
          )
        } else {
          parsedArtifactNameBuilder.append(artifactNameWithoutVersion[index])
        }
      }
      return parsedArtifactNameBuilder.toString()
    }

    private fun readMavenInstall(
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
      val finalDependenciesList = mutableListOf<MavenListDependency>()
      mavenInstallDependencyList?.forEach { dep ->
        val artifactName = dep.coord
        val parsedArtifactName = parseArtifactName(artifactName)
        if (bazelQueryDepsNames.contains(parsedArtifactName)) {
          finalDependenciesList.add(dep)
        }
      }
      return finalDependenciesList
    }

    private fun writeTextProto(
      pathToTextProto: String,
      mavenDependencyList: MavenDependencyList
    ) {
      File(pathToTextProto).printWriter().use { out ->
        out.println(mavenDependencyList)
      }
    }

    private fun provideDependencyListFromPom(
      finalDependenciesList: List<MavenListDependency>
    ): MavenDependencyList {
      val mavenDependencyList = arrayListOf<MavenDependency>()
      finalDependenciesList.forEach {
        val url = it.url
        val pomFileUrl = "${url?.substring(0, url.length - 3)}pom"
        val artifactName = it.coord
        val artifactVersion = StringBuilder()
        var lastIndex = artifactName.length - 1
        while (lastIndex >= 0 && artifactName[lastIndex] != ':') {
          artifactVersion.append(artifactName[lastIndex])
          lastIndex--
        }
        artifactVersion.reverse()
        val pomFile = utilityProvider.scrapeText(pomFileUrl)
        val mavenDependency = MavenDependency
          .newBuilder()
          .setArtifactName(it.coord)
          .setArtifactVersion(artifactVersion.toString())
          .addAllLicense(extractLicenseLinksFromPom(pomFile))
          .setOriginOfLicense(OriginOfLicenses.UNKNOWN)

        mavenDependencyList.add(mavenDependency.build())
      }
      return MavenDependencyList.newBuilder().addAllMavenDependency(mavenDependencyList).build()
    }

    private fun retrieveThirdPartyMavenDependenciesList(
      rootPath:String
    ): List<String> {
      val bazelQueryDepsNames = mutableListOf<String>()
      val output = utilityProvider.retrieveThirdPartyMavenDependenciesList(
        rootPath
      )
      output.forEach { dep ->
        bazelQueryDepsNames.add(dep.substring(MAVEN_PREFIX_LENGTH, dep.length))
      }
      bazelQueryDepsNames.sort()
      return bazelQueryDepsNames.toList()
    }

    private fun getDependencyListFromMavenInstall(
      pathToMavenInstall: String,
      bazelQueryDepsNames: List<String>
    ): List<MavenListDependency> {
      val mavenInstallJson = File(pathToMavenInstall)
      val mavenInstallJsonText = mavenInstallJson.inputStream().bufferedReader().use { it.readText() }
      val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
      val adapter = moshi.adapter(MavenListDependencyTree::class.java)
      val dependencyTree = adapter.fromJson(mavenInstallJsonText)
      val mavenInstallDependencyList = dependencyTree?.mavenListDependencies?.dependencyList
      val finalDependenciesList = mutableListOf<MavenListDependency>()
      mavenInstallDependencyList?.forEach { dep ->
        val artifactName = dep.coord
        val parsedArtifactName = parseArtifactName(artifactName)
        if (bazelQueryDepsNames.contains(parsedArtifactName)) {
          finalDependenciesList.add(dep)
        }
      }
      return finalDependenciesList
    }

    private fun replaceHttpWithHttps(
      urlBuilder: StringBuilder
    ): String {
      var url = urlBuilder.toString()
      if (url.substring(0, 5) != "https") {
        url = "https${url.substring(4, url.length)}"
      }
      return url
    }

    private fun extractLicenseLinksFromPom(
      pomText: String
    ): List<License> {
      val licenseList = arrayListOf<License>()
      var cursor = -1
      if (pomText.length > 11) {
        for (index in 0..(pomText.length - 11)) {
          if (pomText.substring(index, index + 10) == LICENSES_TAG) {
            cursor = index + 9
            break
          }
        }
        if (cursor != -1) {
          var cursor2 = cursor
          while (cursor2 < (pomText.length - 12)) {
            if (pomText.substring(cursor2, cursor2 + 9) == LICENSE_TAG) {
              cursor2 += 9
              while (cursor2 < pomText.length - 6 &&
                pomText.substring(
                  cursor2,
                  cursor2 + 6
                ) != NAME_TAG
              ) {
                ++cursor2
              }
              cursor2 += 6
              val licenseUrlBuilder = StringBuilder()
              val licenseNameBuilder = StringBuilder()
              while (pomText[cursor2] != '<') {
                licenseNameBuilder.append(pomText[cursor2])
                ++cursor2
              }
              while (cursor2 < pomText.length - 4 &&
                pomText.substring(
                  cursor2,
                  cursor2 + 5
                ) != URL_TAG
              ) {
                ++cursor2
              }
              cursor2 += 5
              while (pomText[cursor2] != '<') {
                licenseUrlBuilder.append(pomText[cursor2])
                ++cursor2
              }
              val httpUrl = replaceHttpWithHttps(licenseUrlBuilder)
              licenseList.add(
                License
                  .newBuilder()
                  .setLicenseName(licenseNameBuilder.toString())
                  .setPrimaryLink(httpUrl)
                  .setPrimaryLinkType(PrimaryLinkType.PRIMARY_LINK_TYPE_UNSPECIFIED)
                  .build()
              )
            } else if (pomText.substring(cursor2, cursor2 + 12) == LICENSES_CLOSE_TAG) {
              break
            }
            ++cursor2
          }
        }
      }
      return licenseList
    }
  }
}

