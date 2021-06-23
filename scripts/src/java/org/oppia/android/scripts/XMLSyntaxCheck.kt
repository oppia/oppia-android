package org.oppia.android.scripts

import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.DocumentBuilder
import java.io.File
import org.xml.sax.SAXParseException
import org.xml.sax.ErrorHandler
import org.oppia.android.scripts.ScriptResultConstants

class XMLSyntaxCheck {
  companion object {
    @JvmStatic
    fun main(vararg args: String) {
      /** path of the repo to be analyzed. */
      val repoPath = args[0] + "/"

      /** a list of all allowed directories in the repo to be analyzed. */
      val allowedDirectories = args.drop(1)

      /** a list of all files in the repo to be analyzed. */
      val searchFiles = RepoFile.collectSearchFiles(
        repoPath = repoPath,
        allowedDirectories = allowedDirectories
      )

      /** builder factory which provides the builder to parse the XMl. */
      val builderFactory = DocumentBuilderFactory.newInstance()

      /** document builder which parses the XMl. */
      val docBuilder = builderFactory.newDocumentBuilder()

      docBuilder.setErrorHandler(SyntaxErrorHandler(repoPath = repoPath))

      /** check if the repo has any syntactically incorrect XML. */
      val hasXmlSyntaxFailure = searchFiles.fold(initial = false) { isFailing, file->
        val fileResult = checkIfFileHasBrokenXml(docBuilder, file)
        return@fold isFailing || fileResult
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
      println(
        "XML syntax error: ${e.message}\n" +
          "lineNumber: ${e.getLineNumber()}\n" +
          "columnNumber: ${e.getColumnNumber()}\n" +
          "${e.getSystemId().replace("file:$repoPath", "File: [ROOT]/")}\n"
      )
    }
  }

  /** Class for custom error handling of the parse exception thrown
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

  /** helper class which contains all the file related helper methods. */
  private class RepoFile() {
    companion object {
      /**
       * Collects the paths of all the files which are needed to be checked.
       *
       * @param repoPath the path of the repo
       * @param allowedDirectories a list of all the directories which needs to be checked
       * @return all files which needs to be checked
       */
      fun collectSearchFiles(
        repoPath: String,
        allowedDirectories: List<String>)
        : List<File> {
        return File(repoPath).walk().filter { it ->
          checkIfAllowedDirectory(
            retrieveFilePath(it, repoPath),
            allowedDirectories)
            && it.isFile
            && it.name.endsWith(".xml")
        }.toList()
      }

      /**
       * Checks if a layer is allowed to be analyzed for the check or not.
       * It only allows the layers listed in allowedDirectories (which is
       * specified from the command line arguments) to be analyzed.
       *
       * @param pathString the path of the repo
       * @param allowedDirectories a list of all the files which needs to be checked
       * @return check if path is  allowed to be analyzed or not
       */
      fun checkIfAllowedDirectory(
        pathString: String,
        allowedDirectories: List<String>
      ): Boolean {
        return allowedDirectories.any { pathString.startsWith(it) }
      }

      /**
       * Retrieves the file path relative to the root repository.
       *
       * @param file the file whose whose path is to be retrieved
       * @param repoPath the path of the repo to be analyzed
       * @return path relative to root repository
       */
      fun retrieveFilePath(file: File, repoPath: String): String {
        return file.toString().removePrefix(repoPath)
      }
    }
  }
}
