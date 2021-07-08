package org.oppia.android.scripts.maven

import org.oppia.android.scripts.proto.LicenseDetails
import org.oppia.android.scripts.proto.MavenDependency
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import java.net.URL
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

fun main(args: Array<String>) {
  val pathToNamesXml = args[0]
  val pathToVersionsXml = args[1]
  val pathToDependenciesXml = args[0]
  val pathToLicensesXml = args[0]
  // Generate Maven Dependencies Names List by reading textproto.
  // Generate Maven Dependencies Versions List by reading textproto.
  // Generate Licenses List by reading textproto.
  writeDependenciesNamesXml(pathToNamesXml, listOf("Abhay", "Apache", "MIT"))
  writeDependenciesVersionsXml(pathToVersionsXml, listOf("4.5.0", "3.4.5", "9.0.8"))
}

// fun getMavenDependenciesList(
//  pathToFile: String
// ) : List<MavenDependency> {
//
// }

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

fun writeDependenciesLicensesXml(
  path: String,
  copyrightLicenseList: Set<LicenseDetails>,
  mavenDependenciesList: List<MavenDependency>
  // Final list of MavenDependency data type that will contain a list of dependencies read
  // from maven_dependencies.textproto.
) {
  // Create a map that maps a license name to its transformed named in xml.
  // e.g -  "Apache License 2.0" -> "third_party_dependency_license_5"
  val licenseMap = hashMapOf<String, String>()
  val file = File(path)

  val docBuilder: DocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
  val doc: Document = docBuilder.newDocument()
  val rootResourcesElement: Element = doc.createElement("resources")

  // Write all licenses names as string resources.
  for (index in copyrightLicenseList.indices) {
    val licenseName = copyrightLicenseList.elementAt(index).name
    val stringElement = doc.createElement("string")
    stringElement.setAttribute("name", "license_$index")
    stringElement.appendChild(doc.createTextNode(licenseName))
    rootResourcesElement.appendChild(stringElement)
    licenseMap[licenseName] = "license_$index"
  }

  // Write all license texts, names with license_0 etc.

  // Write all licenses corresponding to each dependency.

  // Write an array of dependencies names.
  val stringArrayElement = doc.createElement("string-array")
  stringArrayElement.setAttribute("name", "third_party_dependencies_versions_array")
  for (index in copyrightLicenseList.indices) {
    val itemElement = doc.createElement("item")
    itemElement.appendChild(doc.createTextNode("@string/third_party_dependency_version_$index"))
    stringArrayElement.appendChild(itemElement)
  }

  rootResourcesElement.appendChild(stringArrayElement)
  doc.appendChild(rootResourcesElement)
  doc.xmlStandalone = true

  getTransformer().transform(DOMSource(doc), StreamResult(file))
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
