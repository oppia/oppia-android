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
    assertThat(errorList.isEmpty()).isEqualTo(true)
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
    assertThat(errorList.elementAt(0).message).isEqualTo(
      "Content is not allowed in trailing section."
    )
    assertThat(errorList.elementAt(0).getLineNumber()).isEqualTo(6)
    assertThat(errorList.elementAt(0).getColumnNumber()).isEqualTo(9)
    assertThat(errorList.elementAt(0).getSystemId()).isEqualTo(
      "file:${retrieveTestFilesDirectoryPath()}/TestFile.xml"
    )
  }

  @Test
  fun testXmlErrorHandler_multipleinvalidXmlFiles_errorHandlerShouldCollectAllErrors() {
    val invalidXml1 =
      """
      <?xml version="1.0" encoding="utf-8"?>
      <shape xmlns:android="http://schemas.android.com/apk/res/android"
        android:shape="rectangle">
        <solid android:color="#3333334D" />
        <size android:height="1dp" />
      </shape>>
      """.trimIndent()
    val invalidXml2 =
      """
      <?xml version="1.0" encoding="utf-8"?>
      shape xmlns:android="http://schemas.android.com/apk/res/android"
        android:shape="rectangle">
        <solid android:color="#3333334D" />
        <size android:height="1dp" />
      </shape>
      """.trimIndent()
    val tempFile1 = tempFolder.newFile("testfiles/TestFile1.xml")
    val tempFile2 = tempFolder.newFile("testfiles/TestFile2.xml")
    tempFile1.writeText(invalidXml1)
    tempFile2.writeText(invalidXml2)
    val docBuilder = builderFactory.newDocumentBuilder()
    val xmlSyntaxErrorHandler = XmlSyntaxErrorHandler()
    docBuilder.setErrorHandler(xmlSyntaxErrorHandler)

    parseXml(docBuilder = docBuilder, file = tempFile1)
    parseXml(docBuilder = docBuilder, file = tempFile2)

    val errorList = xmlSyntaxErrorHandler.retrieveErrorList()
    assertThat(errorList.size).isEqualTo(2)
    assertThat(errorList.elementAt(0).message).isEqualTo(
      "Content is not allowed in trailing section."
    )
    assertThat(errorList.elementAt(0).getLineNumber()).isEqualTo(6)
    assertThat(errorList.elementAt(0).getColumnNumber()).isEqualTo(9)
    assertThat(errorList.elementAt(0).getSystemId()).isEqualTo(
      "file:${retrieveTestFilesDirectoryPath()}/TestFile1.xml"
    )
    assertThat(errorList.elementAt(1).message).isEqualTo(
      "Content is not allowed in prolog."
    )
    assertThat(errorList.elementAt(1).getLineNumber()).isEqualTo(2)
    assertThat(errorList.elementAt(1).getColumnNumber()).isEqualTo(1)
    assertThat(errorList.elementAt(1).getSystemId()).isEqualTo(
      "file:${retrieveTestFilesDirectoryPath()}/TestFile2.xml"
    )
  }

  /** Retrieves the absolute path of testfiles directory. */
  private fun retrieveTestFilesDirectoryPath(): String {
    return "${tempFolder.root}/testfiles"
  }

  /**
   * Parses a given Xml file.
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
