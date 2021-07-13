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

    lateinit var pathToMavenDependenciesTextProto: String
    lateinit var pathToMavenDependenciesProtoBinary: String
    lateinit var dependenciesListFromPom: List<MavenDependency>

    @JvmStatic
    fun main(args: Array<String>) {

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
  }
}

