package org.oppia.android.scripts.xml

import org.oppia.android.scripts.common.RepositoryFile
import javax.xml.parsers.DocumentBuilderFactory

fun main(vararg args: String) {
  // Path of the repo to be analyzed.
  val repoPath = "${args[0]}/"

  // A list of all XML files in the repo to be analyzed.
  val searchFiles = RepositoryFile.collectSearchFiles(
    repoPath = repoPath,
    expectedExtension = ".xml"
  )

  val builderFactory = DocumentBuilderFactory.newInstance()

  val allErrorsList = searchFiles.flatMap { file ->
    val docBuilder = builderFactory.newDocumentBuilder()
    val xmlSyntaxErrorHandler = XmlSyntaxErrorHandler()
    docBuilder.setErrorHandler(xmlSyntaxErrorHandler)
    val xmlDocument = docBuilder.parse(file.toFile())
    val list = xmlDocument.fileErrorList.map
  }
}
