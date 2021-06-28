package org.oppia.android.scripts

import org.xml.sax.ErrorHandler
import org.xml.sax.SAXParseException

/**
 * Class for custom error handling of the parse exception thrown
 * by the parser, to log the failure message in a cleaner way.
 */
class SyntaxErrorHandler(val repoPath: String) : ErrorHandler {
  override fun warning(e: SAXParseException) {
    logCheckFailure(e, repoPath)
  }

  override fun error(e: SAXParseException) {
    logCheckFailure(e, repoPath)
  }

  override fun fatalError(e: SAXParseException) {
    logCheckFailure(e, repoPath)
  }

  /**
   * Logs the failures for XML syntax validation.
   *
   * @param e the parsing exception thrown by the parser
   * @param repoPath the path of the repo to be analyzed
   * @return log the failure
   */
  private fun logCheckFailure(parseException: SAXParseException, repoPath: String) {
    val failureMessage =
      """
          XML syntax error: ${parseException.message}
          lineNumber: ${parseException.getLineNumber()}
          columnNumber: ${parseException.getColumnNumber()}
          ${parseException.getSystemId().replace("file:$repoPath", "File: [ROOT]/")}
      """.trimIndent()
    println(failureMessage)
    println()
  }
}
