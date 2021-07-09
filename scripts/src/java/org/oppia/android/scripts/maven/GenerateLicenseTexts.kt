package org.oppia.android.scripts.maven

import org.oppia.android.scripts.proto.CopyrightLicense
import org.oppia.android.scripts.proto.MavenDependency
import org.oppia.android.scripts.proto.MavenDependencyList
import org.oppia.android.scripts.proto.PrimaryLinkType
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import java.io.FileInputStream
import java.net.URL
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

private const val MAVEN_DEPENDENCY_LIST_INCOMPLETE = "maven_dependencies.textproto is incomplete."
private const val MAVEN_DEPENDENCY_LIST_NEED_MANUAL_WORK =
  "maven_dependencies.textproto still needs some manual work."

/**
 * Script to extract the licenses for the third-party Maven dependencies (direct and indirect both)
 * on which Oppia Android depends.
 *
 * Usage:
 *   bazel run //scripts:generate_license_texts -- <path_to_directory_root>
 *   <path_to_maven_install_json> <path_to_maven_dependencies_textproto>
 *
 * Arguments:
 * - path_to_directory_root: directory path to the root of the Oppia Android repository.
 * - path_to_maven_install_json: absolute path to the maven_install.json file.
 * - path_to_maven_dependencies_textproto: absoulte path to the maven_dependencies.textproto
 * that stores the list of maven dependencies compiled through the script.
 * Example:
 *   bazel run //scripts:generate_maven_dependencies_list -- $(pwd)
 *   $(pwd)/third_party/maven_install.json $(pwd)/scripts/assets/maven_dependencies.textproto
 */
fun main(args: Array<String>) {
  val pathToNamesXml = args[0]
  val pathToVersionsXml = args[1]
  val pathToLicensesXml = args[2]

  val mavenDependencyList = retrieveMavenDependencyList()
  val copyrightLicenseSet = retrieveAllLicensesSet(mavenDependencyList)

  writeDependenciesNamesXml(pathToNamesXml, retrieveArtifactsNamesList(mavenDependencyList))
  writeDependenciesVersionsXml(
    pathToVersionsXml,
    retrieveArtifactsVersionsList(mavenDependencyList)
  )
  writeDependenciesLicensesXml(
    pathToLicensesXml,
    copyrightLicenseSet,
    mavenDependencyList
  )
  // Generate Licenses List by reading textproto.
}

private fun retrieveArtifactsNamesList(mavenDependencyList: List<MavenDependency>): List<String> {
  val artifactNamesList = mutableListOf<String>()
  mavenDependencyList.forEach { dependency ->
    artifactNamesList.add(dependency.artifactName)
  }
  return artifactNamesList.toList()
}

private fun retrieveArtifactsVersionsList(
  mavenDependencyList: List<MavenDependency>
): List<String> {
  val artifactVersionsList = mutableListOf<String>()
  mavenDependencyList.forEach { dependency ->
    artifactVersionsList.add(dependency.artifactVersion)
  }
  return artifactVersionsList.toList()
}

/** Retrieves the list of [MavenDependency] from maven_dependencies.textproto. */
private fun retrieveMavenDependencyList(): List<MavenDependency> {
  return getProto(
    "maven_dependencies.pb",
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
  textProtoFileName: String,
  proto: MavenDependencyList
): MavenDependencyList {
  val protoBinaryFile = File("scripts/assets/$textProtoFileName")
  val builder = proto.newBuilderForType()
  val protoObject = FileInputStream(protoBinaryFile).use {
    builder.mergeFrom(it)
  }.build() as MavenDependencyList
  return protoObject
}

fun writeDependenciesNamesXml(
  path: String,
  dependencyNamesList: List<String>
) {
  val file = File(path)
  val docBuilder: DocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
  val doc: Document = docBuilder.newDocument()
  val rootResourcesElement: Element = doc.createElement("resources")

  // Write all dependencies names as string resources.
  for (index in dependencyNamesList.indices) {
    val name = dependencyNamesList[index]
    val stringElement = doc.createElement("string")
    stringElement.setAttribute("name", "third_party_dependency_name_$index")
    stringElement.appendChild(doc.createTextNode(name))
    rootResourcesElement.appendChild(stringElement)
  }

  // Write an array of dependencies names.
  val stringArrayElement = doc.createElement("string-array")
  stringArrayElement.setAttribute("name", "third_party_dependencies_names_array")
  for (index in dependencyNamesList.indices) {
    val itemElement = doc.createElement("item")
    itemElement.appendChild(doc.createTextNode("@string/third_party_dependency_name_$index"))
    stringArrayElement.appendChild(itemElement)
  }
  rootResourcesElement.appendChild(stringArrayElement)
  doc.appendChild(rootResourcesElement)

  doc.xmlStandalone = true
  getTransformer().transform(DOMSource(doc), StreamResult(file))
}

fun writeDependenciesVersionsXml(
  path: String,
  dependencyVersionsList: List<String>
) {
  val file = File(path)

  val docBuilder: DocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
  val doc: Document = docBuilder.newDocument()
  val rootResourcesElement: Element = doc.createElement("resources")

  // Write all dependencies versions as string resources.
  for (index in dependencyVersionsList.indices) {
    val version = dependencyVersionsList[index]
    val stringElement = doc.createElement("string")
    stringElement.setAttribute("name", "third_party_dependency_version_$index")
    stringElement.appendChild(doc.createTextNode(version))
    rootResourcesElement.appendChild(stringElement)
  }

  // Write an array of dependencies versions.
  val stringArrayElement = doc.createElement("string-array")
  stringArrayElement.setAttribute("name", "third_party_dependencies_versions_array")
  for (index in dependencyVersionsList.indices) {
    val itemElement = doc.createElement("item")
    itemElement.appendChild(doc.createTextNode("@string/third_party_dependency_version_$index"))
    stringArrayElement.appendChild(itemElement)
  }

  rootResourcesElement.appendChild(stringArrayElement)
  doc.appendChild(rootResourcesElement)
  doc.xmlStandalone = true
  getTransformer().transform(DOMSource(doc), StreamResult(file))
}

private fun writeDependenciesLicensesXml(
  path: String,
  copyrightLicenseList: Set<CopyrightLicense>,
  mavenDependenciesList: List<MavenDependency>
): HashMap<String, String> {
  val file = File(path)

  val licenseMap = hashMapOf<String, String>()

  val docBuilder: DocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
  val doc: Document = docBuilder.newDocument()
  val rootResourcesElement: Element = doc.createElement("resources")

  for (index in copyrightLicenseList.indices) {
    val licenseLink = copyrightLicenseList.elementAt(index).licenseLink
    val licenseText = "<![CDATA[${copyrightLicenseList.elementAt(index).licenseText}]]>"
    val stringElement = doc.createElement("string")
    stringElement.setAttribute("name", "license_$index")
    stringElement.appendChild(doc.createTextNode(licenseText))
    rootResourcesElement.appendChild(stringElement)
    licenseMap[licenseLink] = "license_$index"
    println(licenseLink)
  }

  doc.appendChild(rootResourcesElement)
  doc.xmlStandalone = true

  getTransformer().transform(DOMSource(doc), StreamResult(file))
  return licenseMap
}

private fun writeDependenciesLicenseTextArray(
  path: String,
  licenseLinksToIndexMap: HashMap<String, String>,
  mavenDependenciesList: List<MavenDependency>
) {
  val file = File(path)

  val docBuilder: DocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
  val doc: Document = docBuilder.newDocument()
  val rootResourcesElement: Element = doc.createElement("resources")

  // Write all dependencies versions as string resources.
//  for (index in mavenDependenciesList.indices) {
//    val dependency = mavenDependenciesList[index]
//    val stringArrayElement = doc.createElement("string-array")
//    stringArrayElement.setAttribute("name", "third_party_dependency_licenses_$index")
//    dependency.licenseList.forEach {
//      val licenseLink = dependency.licenseList[j].primarylicenseLink
//      val indexOfLicenseText = licenseLinksToIndexMap[licenseLink]
//      val stringItemElement = doc.createElement("item")
//      stringItemElement.appendChild("@string/license$indexOfLicenseText")
//      stringArrayElement.appendChild(stringItemElement)
//    }
//    rootResourcesElement.appendChild(stringArrayElement)
//  }

  // Write an array of dependencies versions.
  val arrayElement = doc.createElement("array")
  arrayElement.setAttribute("name", "third_party_dependency_license_texts_array")
  for (index in mavenDependenciesList.indices) {
    val itemElement = doc.createElement("item")
    itemElement.appendChild(doc.createTextNode("@array/third_party_dependency_licenses_$index"))
    arrayElement.appendChild(itemElement)
  }

  rootResourcesElement.appendChild(arrayElement)
  doc.appendChild(rootResourcesElement)
  doc.xmlStandalone = true
  getTransformer().transform(DOMSource(doc), StreamResult(file))
}

private fun writeDependenciesLicenseNamesArray(
  path: String,
  licenseLinksToIndexMap: HashMap<String, String>,
  mavenDependenciesList: List<MavenDependency>
) {
}

fun retrieveAllLicensesSet(
  mavenDependencyList: List<MavenDependency>
): Set<CopyrightLicense> {
  val copyrightLicensesSet = mutableSetOf<CopyrightLicense>()
  mavenDependencyList.forEach { dependency ->
    val licenseList = dependency.licenseList
    if (licenseList.isEmpty()) {
      throw Exception(MAVEN_DEPENDENCY_LIST_INCOMPLETE)
    }
    licenseList.forEach { license ->
      var licenseText: String = ""
      var licenseLink = ""
      when (license.primaryLinkType) {
        PrimaryLinkType.SCRAPE_DIRECTLY -> {
          licenseText = scrapeLicenseText(license.primaryLink)
          licenseLink = license.primaryLink
        }
        PrimaryLinkType.SCRAPE_FROM_LOCAL_COPY -> {
          licenseText = scrapeLicenseText(license.alternativeLink)
          licenseLink = license.alternativeLink
        }
        PrimaryLinkType.SHOW_LINK_ONLY -> {
          licenseText = license.primaryLink
          licenseLink = license.primaryLink
        }
        else -> throw Exception(MAVEN_DEPENDENCY_LIST_NEED_MANUAL_WORK)
      }
      copyrightLicensesSet.add(
        CopyrightLicense
          .newBuilder()
          .setLicenseName(license.licenseName)
          .setLicenseText(licenseText)
          .setLicenseLink(licenseLink)
          .build()
      )
    }
  }
  return copyrightLicensesSet
}

fun scrapeLicenseText(url: String): String {
  return URL(url).openStream().bufferedReader().readText()
}

private fun getTransformer(): Transformer {
  val transformer = TransformerFactory.newInstance().newTransformer()
  transformer.setOutputProperty(OutputKeys.INDENT, "yes")
  transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "")
  transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
  return transformer
}
