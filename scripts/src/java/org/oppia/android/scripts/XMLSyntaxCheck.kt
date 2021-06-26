package org.oppia.android.scripts

import org.xml.sax.SAXParseException
import java.io.File
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Script for ensuring that all the XML files
 * in the repo are syntactically correct.
 */
class XMLSyntaxCheck {
  companion object {
    @JvmStatic
    fun main(vararg args: String) {
      // path of the repo to be analyzed.
      val repoPath = args[0] + "/"

      // a list of all allowed directories in the repo to be analyzed.
      // args[0] is the repoPath, the allowed directories are specified
      // after it. Hence, we have to start from the 1st index.
      val allowedDirectories = args.drop(1)

      // a list of all XML files in the repo to be analyzed.
      val searchFiles = RepoFile.collectSearchFiles(
        repoPath,
        allowedDirectories,
        ".xml"
      )

      // class object which is needed to acess the helper methods
      val xmlSyntaxCheck: XMLSyntaxCheck = XMLSyntaxCheck()

      // builder factory which provides the builder to parse the XMl.
      val builderFactory = DocumentBuilderFactory.newInstance()

      // document builder which parses the XMl.
      val docBuilder = builderFactory.newDocumentBuilder()

      docBuilder.setErrorHandler(SyntaxErrorHandler(repoPath))

      // check if the repo has any syntactically incorrect XML.
      val hasXmlSyntaxFailure = searchFiles.fold(false) { isFailing, file ->
        val fileResult = xmlSyntaxCheck.checkIfFileHasBrokenXml(docBuilder, file)
        isFailing || fileResult
      }

      if (hasXmlSyntaxFailure) {
        throw Exception(ScriptResultConstants.XML_SYNTAX_CHECK_FAILED)
      } else {
        println(ScriptResultConstants.XML_SYNTAX_CHECK_PASSED)
      }
    }
  }

  /**
   * Check if a file has a broken XML syntax.
   *
   * @param docBuilder the builder which will parse the XML file
   * @param file the file to be checked for
   * @return whether a file is syntactically correct or not
   */
  private fun checkIfFileHasBrokenXml(docBuilder: DocumentBuilder, file: File): Boolean {
    try {
      docBuilder.parse(file)
    } catch (e: SAXParseException) {
      return true
    }
    return false
  }
}
