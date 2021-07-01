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
  fun setUpTests() {
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
  fun testXmlErrorHandler_invalidXml_errorListShouldBeEmpty() {
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
    assertThat(errorList.size).isEqualTo(1)
    assertThat(errorList.first().message).isEqualTo(
      "Content is not allowed in trailing section."
    )
    assertThat(errorList.first().getLineNumber()).isEqualTo(6)
    assertThat(errorList.first().getColumnNumber()).isEqualTo(9)
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
    } catch (e: SAXParseException) { }
  }
}
