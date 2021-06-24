package org.oppia.android.scripts

import org.xml.sax.ErrorHandler
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

      // builder factory which provides the builder to parse the XMl.
      val builderFactory = DocumentBuilderFactory.newInstance()

      // document builder which parses the XMl.
      val docBuilder = builderFactory.newDocumentBuilder()

      docBuilder.setErrorHandler(SyntaxErrorHandler(repoPath))

      // check if the repo has any syntactically incorrect XML.
      val hasXmlSyntaxFailure = searchFiles.fold(false) { isFailing, file ->
        val fileResult = checkIfFileHasBrokenXml(docBuilder, file)

        isFailing || fileResult
      }

      if (hasXmlSyntaxFailure) {
        throw Exception(ScriptResultConstants.XML_SYNTAX_CHECK_FAILED)
      } else {
        println(ScriptResultConstants.XML_SYNTAX_CHECK_PASSED)
      }
    }

    /** Check if a file has a broken XML syntax.
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

    /** Logs the failures for XML syntax validation.
     *
     * @param e the parsing exception thrown by the parser
     * @param repoPath the path of the repo to be analyzed
     * @return log the failure
     */
    private fun logCheckFailure(e: SAXParseException, repoPath: String) {
      val failureMessage =
        """
          XML syntax error: ${e.message}
          lineNumber: ${e.getLineNumber()}
          columnNumber: ${e.getColumnNumber()}
          ${e.getSystemId().replace("file:$repoPath", "File: [ROOT]/")}
        """.trimIndent()
      println(failureMessage)
      println()
    }
  }

  /**
   * Class for custom error handling of the parse exception thrown
   * by the parser, to log the failure message in a cleaner way.
   */
  private class SyntaxErrorHandler(val repoPath: String) : ErrorHandler {
    override fun warning(e: SAXParseException) {
      logCheckFailure(e, repoPath)
    }

    override fun error(e: SAXParseException) {
      logCheckFailure(e, repoPath)
    }

    override fun fatalError(e: SAXParseException) {
      logCheckFailure(e, repoPath)
    }
  }
}
