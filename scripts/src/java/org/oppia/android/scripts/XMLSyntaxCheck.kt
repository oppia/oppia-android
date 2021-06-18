package org.oppia.android.scripts

import javax.xml.parsers.DocumentBuilderFactory
import java.io.File
import org.xml.sax.SAXParseException
import org.xml.sax.ErrorHandler

class XMLSyntaxCheck {
  companion object {
    @JvmStatic
    fun main(vararg args: String) {
      val repoPath = args[0] + "/"

      val allowedDirectories: MutableList<String> = ArrayList()

      for (i in 1 until args.size) {
        allowedDirectories.add(args[i])
      }

      val searchFiles = collectSearchFiles(repoPath, allowedDirectories)

      val builderFactory = DocumentBuilderFactory.newInstance()

      val docBuilder = builderFactory.newDocumentBuilder()

      docBuilder.setErrorHandler(SyntaxErrorHandler())

      var scriptFailedFlag = false

      searchFiles.forEach {
        val filePath = it.toString().removePrefix(repoPath)
        try {
          docBuilder.parse(File(it.toString()))
        } catch (e: SAXParseException) {
          println("File: [ROOT]/$filePath\n")
          scriptFailedFlag = true
        }
      }

      if (scriptFailedFlag) {
        throw Exception("XML SYNTAX CHECK FAILED")
      } else {
        println("XML SYNTAX CHECK PASSED")
      }
    }

    /**
     * Collects the paths of all the files which are needed to be checked
     *
     * @param repoPath the path of the repo.
     * @param allowedDirectories a list of all the directories which needs to be checked.
     * @param exemptionList a list of files which needs to be exempted for this check
     * @return [Sequence<File>] all files which needs to be checked.
     */
    fun collectSearchFiles(
      repoPath: String,
      allowedDirectories: MutableList<String>,
      exemptionList: Array<String> = arrayOf<String>()
    ): Sequence<File> {
      val validPaths = File(repoPath).walk().filter { it ->
        checkIfAllowedDirectory(
          it.toString().removePrefix(repoPath),
          allowedDirectories)
          && it.isFile
          && it.name.endsWith(".xml")
          && it.name !in exemptionList
      }
      return validPaths
    }

    fun checkIfAllowedDirectory(
      pathString: String,
      allowedDirectories: MutableList<String>
    ): Boolean {
      allowedDirectories.forEach {
        if (pathString.startsWith(it))
          return true
      }
      return false
    }

    fun logCheckFailure(e: SAXParseException) {
      println(
        "XML syntax error: ${e.message}\n" +
          "lineNumber: ${e.getLineNumber()}\n" +
          "columnNumber: ${e.getColumnNumber()}"
      )
    }
  }

  class SyntaxErrorHandler : ErrorHandler {
    override fun warning(e: SAXParseException) {
      logCheckFailure(e)
    }

    override fun error(e: SAXParseException) {
      logCheckFailure(e)
    }

    override fun fatalError(e: SAXParseException) {
      logCheckFailure(e)
    }
  }
}
