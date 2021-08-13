package org.oppia.android.scripts.xml

import org.oppia.android.scripts.common.RepositoryFile
import org.xml.sax.SAXParseException
import java.io.File
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Script for ensuring that all XML files in the repo are syntactically correct.
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
  val repoPath = "${args[0]}/"

  // A list of all XML files in the repo to be analyzed.
  val searchFiles = RepositoryFile.collectSearchFiles(
    repoPath = repoPath,
    expectedExtension = ".xml"
  )

  // Builder factory which provides the builder to parse the XMl.
  val builderFactory = DocumentBuilderFactory.newInstance()

  val allErrorsList = searchFiles.flatMap { file ->
    val docBuilder = builderFactory.newDocumentBuilder()
    val xmlSyntaxErrorHandler = XmlSyntaxErrorHandler()
    docBuilder.setErrorHandler(xmlSyntaxErrorHandler)
    parseXml(docBuilder, file)
    val fileErrorList = xmlSyntaxErrorHandler.retrieveErrorList()
    fileErrorList.map { error -> Pair(error, file) }
  }

  // Check if the repo has any syntactically incorrect XML.
  val hasXmlSyntaxFailure = allErrorsList.isNotEmpty()

  logXmlSyntaxFailures(allErrorsList)

  if (hasXmlSyntaxFailure) {
    println(
      "Refer to https://github.com/oppia/oppia-android/wiki/Static-Analysis-Checks for more" +
        " details on how to fix this.\n"
    )
  }

  if (hasXmlSyntaxFailure) {
    throw Exception("XML SYNTAX CHECK FAILED")
  } else {
    println("XML SYNTAX CHECK PASSED")
  }
}

/**
 * Parses a given XML file.
 *
 * @param docBuilder the builder which will parse the XML file
 * @param file the file to be checked for
 */
private fun parseXml(docBuilder: DocumentBuilder, file: File) {
  try {
    docBuilder.parse(file)
  } catch (e: SAXParseException) {
    // For any syntax error in the XML file, if the custom error handler does not throws any
    // exception then the default error handler throws a [SaxParseException]. In order to prevent
    // the script check from getting terminated in between, we need to catch and ignore the
    // exception.
  }
}

/**
 * Logs the failures for XML syntax validation.
 *
 * @param errorList a list of all the errors collected by the error handler
 */
private fun logXmlSyntaxFailures(errorList: List<Pair<SAXParseException, File>>) {
  if (errorList.isNotEmpty()) {
    errorList.forEach { errorPair ->
      val error = errorPair.first
      val errorFile = errorPair.second
      val failureMessage =
        "$errorFile:${error.getLineNumber()}:${error.getColumnNumber()}: ${error.message}"
      println(failureMessage)
    }
    println()
  }
}
