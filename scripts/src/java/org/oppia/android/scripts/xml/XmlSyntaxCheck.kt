package org.oppia.android.scripts.xml

import org.oppia.android.scripts.common.RepositoryFile
import org.oppia.android.scripts.common.XML_SYNTAX_CHECK_FAILED_OUTPUT_INDICATOR
import org.oppia.android.scripts.common.XML_SYNTAX_CHECK_PASSED_OUTPUT_INDICATOR
import org.xml.sax.SAXParseException
import java.io.File
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Script for ensuring that all Xml files in the repo are syntactically correct.
 *
 * Usage:
 *   bazel run //scripts:xml_syntax_check -- <path_to_directory_root>
 *
 * Arguments:
 * - path_to_directory_root: directory path to the root of the Oppia Android repository.
 *
 * Example:
 *   bazel run //scripts:xml_syntax_check -- $(pwd)
 */
fun main(vararg args: String) {
  // Path of the repo to be analyzed.
  val repoPath = args[0] + "/"

  // A list of all XML files in the repo to be analyzed.
  val searchFiles = RepositoryFile.collectSearchFiles(
    repoPath = repoPath,
    expectedExtension = ".xml"
  )

  val xmlSyntaxErrorHandler = XmlSyntaxErrorHandler()

  // Builder factory which provides the builder to parse the XMl.
  val builderFactory = DocumentBuilderFactory.newInstance()

  // Document builder which parses the XMl.
  val docBuilder = builderFactory.newDocumentBuilder()

  docBuilder.setErrorHandler(xmlSyntaxErrorHandler)

  searchFiles.forEach { file ->
    parseXml(docBuilder, file)
  }

  // Check if the repo has any syntactically incorrect XML.
  val hasXmlSyntaxFailure = xmlSyntaxErrorHandler.retrieveErrorList().isNotEmpty()

  logXmlSyntaxFailures(xmlSyntaxErrorHandler.retrieveErrorList())

  if (hasXmlSyntaxFailure) {
    throw Exception(XML_SYNTAX_CHECK_FAILED_OUTPUT_INDICATOR)
  } else {
    println(XML_SYNTAX_CHECK_PASSED_OUTPUT_INDICATOR)
  }
}

/**
 * Parses a given Xml file.
 *
 * @param docBuilder the builder which will parse the XML file
 * @param file the file to be checked for
 */
private fun parseXml(docBuilder: DocumentBuilder, file: File) {
  try {
    docBuilder.parse(file)
  } catch (e: SAXParseException) { }
}

/**
 * Logs the failures for Xml syntax validation.
 *
 * @param errorList a list of all the errors collected by the error handler
 */
private fun logXmlSyntaxFailures(errorList: List<SAXParseException>) {
  if (errorList.isNotEmpty()) {
    errorList.forEach { error ->
      val failureMessage =
        "${error.getSystemId().removePrefix("file:")}:${error.getLineNumber()}:${error.getColumnNumber()}: ${error.message}"
      println(failureMessage)
    }
    println()
  }
}
