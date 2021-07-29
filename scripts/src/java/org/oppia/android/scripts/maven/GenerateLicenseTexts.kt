package org.oppia.android.scripts.maven

import org.oppia.android.scripts.maven.data.CopyrightLicense
import org.oppia.android.scripts.maven.data.Dependency
import org.oppia.android.scripts.proto.License
import org.oppia.android.scripts.proto.MavenDependency
import org.oppia.android.scripts.proto.MavenDependencyList
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import java.io.FileInputStream
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

private const val MAVEN_DEPENDENCY_LIST_NOT_UP_TO_DATE =
  "maven_dependencies.textproto is not up-to-date"

/**
 * Script to extract the licenses for the third-party Maven dependencies (direct and indirect both)
 * on which Oppia Android depends.
 *
 * Usage:
 *   bazel run //scripts:generate_license_texts -- <path_to_directory_values>
 *     <path_to_maven_dependenices.pb>
 *
 * Arguments:
 * - path_to_directory_values: directory path to the values folder of the Oppia Android repository.
 * - path_to_maven_dependenices.pb: relative path to the maven_dependencies.pb

 * Example:
 *   bazel run //scripts:generate_license_texts -- $(pwd)/app/src/main/res/values
 *   scripts/assets/maven_dependencies.pb
 */
fun main(args: Array<String>) {
  GenerateLicenseTexts(LicenseFetcherImpl()).main(args)
}

class GenerateLicenseTexts(
  private val licenseFetcher: LicenseFetcher
) {
  fun main(args: Array<String>) {
    if (args.size < 2) {
      println(
        """
        Usage: bazel run //scripts:generate_license_texts -- <path_to_directory_values>
        <path_to_pb_file>  
        """.trimIndent()
      )
      throw Exception("Too few arguments passed.")
    }
    val pathToValuesDirectory = args[0]
    val pathToThirdPartyDependenciesXml = "$pathToValuesDirectory/third_party_dependencies.xml"
    val pathToPbFile = args[1]

    val mavenDependencyList = retrieveMavenDependencyList(pathToPbFile)
    if (mavenDependencyList.isEmpty()) {
      throw Exception(MAVEN_DEPENDENCY_LIST_NOT_UP_TO_DATE)
    }
    val copyrightLicenseSet = retrieveAllLicensesSet(mavenDependencyList)
    val dependencyList =
      retrieveDependencyList(mavenDependencyList)

    val dependencyNamesList = retrieveArtifactsNamesList(dependencyList)
    val dependencyVersionsList = retrieveArtifactsVersionsList(dependencyList)

    writeThirdPartyDependneciesXml(
      pathToThirdPartyDependenciesXml,
      dependencyNamesList,
      dependencyVersionsList,
      copyrightLicenseSet,
      dependencyList
    )

    println("\nScript execution completed successfully.")
  }

  /**
   * Retrieve the list of maven dependencies from maven_dependencies.textproto.
   *
   * @param pathToPbFile path to the pb file to be parsed
   *
   * @return list of [MavenDependency]
   */
  private fun retrieveMavenDependencyList(pathToPbFile: String): List<MavenDependency> {
    return parseTextProto(
      pathToPbFile,
      MavenDependencyList.getDefaultInstance()
    ).mavenDependencyList
  }

  /**
   * Helper function to parse the textproto file to a proto class.
   *
   * @param pathToPbFile path to the pb file to be parsed
   * @param proto instance of the proto class
   *
   * @return proto class from the parsed textproto file
   */
  private fun parseTextProto(
    pathToPbFile: String,
    proto: MavenDependencyList
  ): MavenDependencyList {
    val protoBinaryFile = File(pathToPbFile)
    val builder = proto.newBuilderForType()
    return FileInputStream(protoBinaryFile).use {
      builder.mergeFrom(it)
    }.build() as MavenDependencyList
  }

  private fun retrieveAllLicensesSet(
    mavenDependencyList: List<MavenDependency>
  ): Set<CopyrightLicense> {
    val copyrightLicensesSet = mutableSetOf<CopyrightLicense>()
    mavenDependencyList.forEach { dependency ->
      val licenseList = dependency.licenseList
      if (licenseList.isEmpty()) {
        throw Exception(MAVEN_DEPENDENCY_LIST_NOT_UP_TO_DATE)
      }
      licenseList.forEach { license ->
        val licenseText: String
        val licenseLink: String
        when (license.verifiedLinkCase) {
          License.VerifiedLinkCase.SCRAPABLE_LINK -> {
            licenseText = fetchLicenseText(license.scrapableLink.url)
            licenseLink = license.scrapableLink.url
          }
          License.VerifiedLinkCase.EXTRACTED_COPY_LINK -> {
            licenseText = fetchLicenseText(license.extractedCopyLink.url)
            licenseLink = license.extractedCopyLink.url
          }
          License.VerifiedLinkCase.DIRECT_LINK_ONLY -> {
            licenseText = license.directLinkOnly.url
            licenseLink = license.directLinkOnly.url
          }
          else -> throw Exception(MAVEN_DEPENDENCY_LIST_NOT_UP_TO_DATE)
        }
        copyrightLicensesSet.add(
          CopyrightLicense(
            license.licenseName,
            licenseLink,
            licenseText,
          )
        )
      }
    }
    return copyrightLicensesSet
  }

  private fun retrieveDependencyList(
    mavenDependencyList: List<MavenDependency>
  ): List<Dependency> {
    val dependencyList = mutableListOf<Dependency>()
    mavenDependencyList.forEach { mavenDependency ->
      val licenseList = mavenDependency.licenseList
      if (licenseList.isEmpty()) {
        throw Exception(MAVEN_DEPENDENCY_LIST_NOT_UP_TO_DATE)
      }
      val copyrightLicenseList = mutableListOf<CopyrightLicense>()
      licenseList.forEach { license ->
        val licenseText: String
        val licenseLink: String
        when (license.verifiedLinkCase) {
          License.VerifiedLinkCase.SCRAPABLE_LINK -> {
            licenseText = fetchLicenseText(license.scrapableLink.url)
            licenseLink = license.scrapableLink.url
          }
          License.VerifiedLinkCase.EXTRACTED_COPY_LINK -> {
            licenseText = fetchLicenseText(license.extractedCopyLink.url)
            licenseLink = license.extractedCopyLink.url
          }
          License.VerifiedLinkCase.DIRECT_LINK_ONLY -> {
            licenseText = license.directLinkOnly.url
            licenseLink = license.directLinkOnly.url
          }
          else -> throw Exception(MAVEN_DEPENDENCY_LIST_NOT_UP_TO_DATE)
        }
        copyrightLicenseList.add(
          CopyrightLicense(
            license.licenseName,
            licenseLink,
            licenseText
          )
        )
      }
      dependencyList.add(
        Dependency(
          mavenDependency.artifactName,
          mavenDependency.artifactVersion,
          copyrightLicenseList
        )
      )
    }
    return dependencyList
  }

  private fun retrieveArtifactsNamesList(dependencyList: List<Dependency>): List<String> {
    return dependencyList.map { it.name }
  }

  private fun retrieveArtifactsVersionsList(
    dependencyList: List<Dependency>
  ): List<String> {
    return dependencyList.map { it.version }
  }

  private fun writeThirdPartyDependneciesXml(
    pathToResourceXml: String,
    dependencyNamesList: List<String>,
    dependencyVersionsList: List<String>,
    copyrightLicenseSet: Set<CopyrightLicense>,
    dependencyList: List<Dependency>
  ) {
    val file = File(pathToResourceXml)
    val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    val doc = docBuilder.newDocument()
    val rootResourcesElement = doc.createElement("resources")

    // Create string resources to get dependencies names.
    writeDependenciesNamesXml(doc, dependencyNamesList, rootResourcesElement)

    // Create string resources to get dependencies versions.
    writeDependenciesVersionsXml(doc, dependencyVersionsList, rootResourcesElement)

    // Create string resources to get dependencies's license texts and names.
    writeLicensesNamesAndTextsXml(
      doc = doc,
      copyrightLicenseSet = copyrightLicenseSet,
      rootResourcesElement = rootResourcesElement,
      dependenciesList = dependencyList
    )

    doc.appendChild(rootResourcesElement)
    doc.xmlStandalone = true
    getTransformer().transform(DOMSource(doc), StreamResult(file))
  }

  private fun writeDependenciesNamesXml(
    doc: Document,
    dependencyNamesList: List<String>,
    rootResourcesElement: Element
  ) {
    // Add all dependencies names as string resources.
    writeList(
      itemList = dependencyNamesList,
      namePrefix = "third_party_dependency_name_",
      rootResourcesElement = rootResourcesElement,
      doc = doc
    )

    // Add all dependencies names in a string-array resource.
    val stringArrayElement = createArray(
      arrayTag = "string-array",
      arrayName = "third_party_dependency_names_array",
      itemListSize = dependencyNamesList.size,
      textNodePrefix = "@string/third_party_dependency_name_",
      doc = doc
    )
    rootResourcesElement.appendChild(stringArrayElement)
  }

  private fun writeDependenciesVersionsXml(
    doc: Document,
    dependencyVersionsList: List<String>,
    rootResourcesElement: Element
  ) {

    // Add all dependencies versions as string resources.
    writeList(
      itemList = dependencyVersionsList,
      namePrefix = "third_party_dependency_version_",
      rootResourcesElement = rootResourcesElement,
      doc = doc
    )

    // Add an array of dependencies versions string resources.
    val stringArrayElement = createArray(
      arrayTag = "string-array",
      arrayName = "third_party_dependency_versions_array",
      itemListSize = dependencyVersionsList.size,
      textNodePrefix = "@string/third_party_dependency_version_",
      doc = doc
    )
    rootResourcesElement.appendChild(stringArrayElement)
  }

  private fun writeLicensesNamesAndTextsXml(
    doc: Document,
    copyrightLicenseSet: Set<CopyrightLicense>,
    rootResourcesElement: Element,
    dependenciesList: List<Dependency>
  ) {
    // Add all license texts to third_party_dependencies.xml.
    copyrightLicenseSet.forEachIndexed { index, license ->
      val stringElement = doc.createElement("string")
      stringElement.setAttribute("name", "license_text_$index")
      val licenseText = "\"${license.licenseText}\""
      stringElement.appendChild(doc.createTextNode(licenseText))
      rootResourcesElement.appendChild(stringElement)
    }

    // Add all license names to third_party_dependencies.xml.
    copyrightLicenseSet.forEachIndexed { index, license ->
      val licenseName = license.licenseName
      val stringElement = doc.createElement("string")
      stringElement.setAttribute("name", "license_name_$index")
      stringElement.appendChild(doc.createTextNode(licenseName))
      rootResourcesElement.appendChild(stringElement)
    }

    // Add arrays of license texts and license names for each dependency.
    writeLicenseNamesAndTextsArrays(
      doc,
      copyrightLicenseSet,
      dependenciesList,
      rootResourcesElement
    )
  }

  private fun writeLicenseNamesAndTextsArrays(
    doc: Document,
    copyrightLicenseSet: Set<CopyrightLicense>,
    dependenciesList: List<Dependency>,
    rootResourcesElement: Element
  ) {

    dependenciesList.forEachIndexed { index, dependency ->
      val stringArrayElement = doc.createElement("string-array")
      stringArrayElement.setAttribute("name", "third_party_dependency_license_texts_$index")
      dependency.licenseList.forEach { license ->
        val indexOfLicenseText = copyrightLicenseSet.indexOf(license)
        val stringItemElement = doc.createElement("item")
        stringItemElement.appendChild(
          doc.createTextNode(
            "@string/license_text_$indexOfLicenseText"
          )
        )
        stringArrayElement.appendChild(stringItemElement)
      }
      rootResourcesElement.appendChild(stringArrayElement)
    }

    dependenciesList.forEachIndexed { index, dependency ->
      val stringArrayElement = doc.createElement("string-array")
      stringArrayElement.setAttribute("name", "third_party_dependency_license_names_$index")
      dependency.licenseList.forEach { license ->
        val indexOfLicenseName = copyrightLicenseSet.indexOf(license)
        val stringItemElement = doc.createElement("item")
        stringItemElement.appendChild(
          doc.createTextNode(
            "@string/license_name_$indexOfLicenseName"
          )
        )
        stringArrayElement.appendChild(stringItemElement)
      }
      rootResourcesElement.appendChild(stringArrayElement)
    }

    val arrayOfLicenseNamesArrays = createArray(
      arrayTag = "array",
      arrayName = "third_party_dependency_license_names_arrays",
      itemListSize = dependenciesList.size,
      textNodePrefix = "@array/third_party_dependency_license_names_",
      doc = doc
    )

    rootResourcesElement.appendChild(arrayOfLicenseNamesArrays)

    val arrayOfLicenseTextsArrays = createArray(
      arrayTag = "array",
      arrayName = "third_party_dependency_license_texts_arrays",
      itemListSize = dependenciesList.size,
      textNodePrefix = "@array/third_party_dependency_license_texts_",
      doc = doc
    )

    rootResourcesElement.appendChild(arrayOfLicenseTextsArrays)
  }

  private fun writeList(
    itemList: List<String>,
    namePrefix: String,
    rootResourcesElement: Element,
    doc: Document
  ) {
    itemList.forEachIndexed { index, itemName ->
      val stringElement = doc.createElement("string")
      stringElement.setAttribute("name", "$namePrefix$index")
      stringElement.appendChild(doc.createTextNode(itemName))
      rootResourcesElement.appendChild(stringElement)
    }
  }

  private fun createArray(
    arrayTag: String,
    arrayName: String,
    itemListSize: Int,
    textNodePrefix: String,
    doc: Document
  ): Element {
    val arrayElement = doc.createElement(arrayTag)
    arrayElement.setAttribute("name", arrayName)
    for (index in 0 until itemListSize) {
      val itemElement = doc.createElement("item")
      itemElement.appendChild(doc.createTextNode("$textNodePrefix$index"))
      arrayElement.appendChild(itemElement)
    }
    return arrayElement
  }

  private fun fetchLicenseText(url: String): String {
    return licenseFetcher.scrapeText(url)
  }

  private fun getTransformer(): Transformer {
    val transformer = TransformerFactory.newInstance().newTransformer()
    transformer.setOutputProperty(OutputKeys.INDENT, "yes")
    transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "")
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
    return transformer
  }
}
