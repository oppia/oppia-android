package org.oppia.android.scripts.xml

import org.xml.sax.ErrorHandler
import org.xml.sax.SAXParseException

/**
 * Custom XML parser error handler which collects syntax errors as they occur for later processing.
 */
class XmlSyntaxErrorHandler : ErrorHandler {
  private val syntaxErrorList = mutableListOf<SAXParseException>()

  override fun warning(e: SAXParseException) {
    syntaxErrorList.add(e)
  }

  override fun error(e: SAXParseException) {
    syntaxErrorList.add(e)
  }

  override fun fatalError(e: SAXParseException) {
    syntaxErrorList.add(e)
  }

  /**
   * Retrieves all the errors collected by the handler.
   *
   * @return a list of all the errors collected by the error handler
   */
  fun retrieveErrorList(): List<SAXParseException> {
    return syntaxErrorList
  }
}
