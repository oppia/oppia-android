package org.oppia.android.scripts.xml

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.xml.sax.SAXParseException
import java.io.File
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

/** Tests for [XmlSyntaxErrorHandler]. */
class XmlSyntaxErrorHandlerTest {
  private val builderFactory = DocumentBuilderFactory.newInstance()

  @Rule
  @JvmField
  var tempFolder = TemporaryFolder()

  @Before
  fun setUp() {
    tempFolder.newFolder("testfiles")
  }

  @Test
  fun testXmlErrorHandler_validXml_noErrorShouldBeCollected() {
    val validXml =
      """
      <?xml version="1.0" encoding="utf-8"?>
      <shape xmlns:android="http://schemas.android.com/apk/res/android"
        android:shape="rectangle">
        <solid android:color="#3333334D" />
        <size android:height="1dp" />
      </shape>
      """.trimIndent()
    val tempFile = tempFolder.newFile("testfiles/TestFile.xml")
    tempFile.writeText(validXml)
    val docBuilder = builderFactory.newDocumentBuilder()
    val xmlSyntaxErrorHandler = XmlSyntaxErrorHandler()
    docBuilder.setErrorHandler(xmlSyntaxErrorHandler)

    parseXml(docBuilder = docBuilder, file = tempFile)

    val errorList = xmlSyntaxErrorHandler.retrieveErrorList()
    assertThat(errorList).isEmpty()
  }

  @Test
  fun testXmlErrorHandler_invalidXml_errorShouldBeCollected() {
    val invalidXml =
      """
      <?xml version="1.0" encoding="utf-8"?>
      <shape xmlns:android="http://schemas.android.com/apk/res/android"
        android:shape="rectangle">
        <solid android:color="#3333334D" />
        <size android:height="1dp" />
      </shape>>
      """.trimIndent()
    val tempFile = tempFolder.newFile("testfiles/TestFile.xml")
    tempFile.writeText(invalidXml)
    val docBuilder = builderFactory.newDocumentBuilder()
    val xmlSyntaxErrorHandler = XmlSyntaxErrorHandler()
    docBuilder.setErrorHandler(xmlSyntaxErrorHandler)

    parseXml(docBuilder = docBuilder, file = tempFile)

    val errorList = xmlSyntaxErrorHandler.retrieveErrorList()
    assertThat(errorList).hasSize(1)
    assertThat(errorList.first().message).isEqualTo(
      "Content is not allowed in trailing section."
    )
    assertThat(errorList.first().getLineNumber()).isEqualTo(6)
    assertThat(errorList.first().getColumnNumber()).isEqualTo(9)
  }

  @Test
  fun testXmlErrorHandler_invokeWarningCase_errorShouldBeCollected() {
    val xmlSyntaxErrorHandler = XmlSyntaxErrorHandler()
    xmlSyntaxErrorHandler.warning(SAXParseException("test_error_message", "", "", 1, 1))
    val errorList = xmlSyntaxErrorHandler.retrieveErrorList()
    assertThat(errorList).hasSize(1)
    assertThat(errorList.first().message).isEqualTo("test_error_message")
    assertThat(errorList.first().getLineNumber()).isEqualTo(1)
    assertThat(errorList.first().getColumnNumber()).isEqualTo(1)
  }

  @Test
  fun testXmlErrorHandler_invokeErrorCase_errorShouldBeCollected() {
    val xmlSyntaxErrorHandler = XmlSyntaxErrorHandler()
    xmlSyntaxErrorHandler.error(SAXParseException("test_error_message", "", "", 1, 1))
    val errorList = xmlSyntaxErrorHandler.retrieveErrorList()
    assertThat(errorList).hasSize(1)
    assertThat(errorList.first().message).isEqualTo("test_error_message")
    assertThat(errorList.first().getLineNumber()).isEqualTo(1)
    assertThat(errorList.first().getColumnNumber()).isEqualTo(1)
  }

  @Test
  fun testXmlErrorHandler_invokeFatalErrorCase_errorShouldBeCollected() {
    val xmlSyntaxErrorHandler = XmlSyntaxErrorHandler()
    xmlSyntaxErrorHandler.fatalError(SAXParseException("test_error_message", "", "", 1, 1))
    val errorList = xmlSyntaxErrorHandler.retrieveErrorList()
    assertThat(errorList).hasSize(1)
    assertThat(errorList.first().message).isEqualTo("test_error_message")
    assertThat(errorList.first().getLineNumber()).isEqualTo(1)
    assertThat(errorList.first().getColumnNumber()).isEqualTo(1)
  }

  @Test
  fun testXmlErrorHandler_multipleErrors_allErrorsShouldBeCollected() {
    val invalidXml =
      """
      <?xml version="1.0" encoding="utf-8"?>
      <shape xmlns:android="http://schemas.android.com/apk/res/android"
        android:shape="rectangle">
        <solid android:color="#3333334D" />
        <size android:height="1dp" />
      </shape>>
      """.trimIndent()
    val tempFile = tempFolder.newFile("testfiles/TestFile.xml")
    tempFile.writeText(invalidXml)
    val docBuilder = builderFactory.newDocumentBuilder()
    val xmlSyntaxErrorHandler = XmlSyntaxErrorHandler()
    docBuilder.setErrorHandler(xmlSyntaxErrorHandler)
    xmlSyntaxErrorHandler.warning(SAXParseException("error1", "", "", 1, 1))
    xmlSyntaxErrorHandler.error(SAXParseException("error2", "", "", 2, 2))
    parseXml(docBuilder = docBuilder, file = tempFile)

    val errorList = xmlSyntaxErrorHandler.retrieveErrorList()
    assertThat(errorList).hasSize(3)
    assertThat(errorList.elementAt(0).message).isEqualTo("error1")
    assertThat(errorList.elementAt(0).getLineNumber()).isEqualTo(1)
    assertThat(errorList.elementAt(0).getColumnNumber()).isEqualTo(1)
    assertThat(errorList.elementAt(1).message).isEqualTo("error2")
    assertThat(errorList.elementAt(1).getLineNumber()).isEqualTo(2)
    assertThat(errorList.elementAt(1).getColumnNumber()).isEqualTo(2)
    assertThat(errorList.elementAt(2).message).isEqualTo(
      "Content is not allowed in trailing section."
    )
    assertThat(errorList.elementAt(2).getLineNumber()).isEqualTo(6)
    assertThat(errorList.elementAt(2).getColumnNumber()).isEqualTo(9)
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
}
