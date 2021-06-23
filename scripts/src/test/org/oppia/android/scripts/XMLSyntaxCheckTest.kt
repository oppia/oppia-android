package org.oppia.android.scripts

import java.io.File
import org.oppia.android.scripts.XMLSyntaxCheck
import org.junit.Test
import org.junit.Rule
import org.junit.Before
import org.junit.After
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.ScriptResultConstants
import com.google.common.truth.Truth.assertThat
import org.oppia.android.testing.assertThrows
import java.io.PrintStream
import java.io.ByteArrayOutputStream

class XMLSyntaxCheckTest {
  @Rule
  @JvmField
  public var tempFolder: TemporaryFolder = TemporaryFolder()

  @Before
  fun initTestFilesDirectory() {
    tempFolder.newFolder("testfiles")
  }

  private val outContent: ByteArrayOutputStream = ByteArrayOutputStream()
  private val originalOut: PrintStream = java.lang.System.out

  @Before
  fun setUpStreams() {
    java.lang.System.setOut(PrintStream(outContent))
  }

  @After
  fun restoreStreams() {
    java.lang.System.setOut(originalOut)
  }

  @Test
  fun testXmlSyntax_validXML_xmlSyntaxIsCorrect(){
    val validXML =
    "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
      "<shape xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
      "  android:shape=\"rectangle\">\n" +
      "  <solid android:color=\"#3333334D\" />\n" +
      "  <size android:height=\"1dp\" />\n" +
      "</shape>"

    val tempFile = tempFolder.newFile("testfiles/TestFile.xml")
    tempFile.writeText(validXML)

    runScript()

    assertThat(outContent.toString().trim()).isEqualTo(
      ScriptResultConstants.XML_SYNTAX_CHECK_PASSED
    )
  }

  @Test
  fun testXmlSyntax_invalidOpeningTag_xmlSyntaxIsInCorrect(){
    val invalidXML =
      "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
        "<shape xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
        "  android:shape=\"rectangle\">\n" +
        "  <<solid android:color=\"#3333334D\" />\n" +
        "  <size android:height=\"1dp\" />\n" +
        "</shape>"
    val tempFile = tempFolder.newFile("testfiles/TestFile.xml")
    tempFile.writeText(invalidXML)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(ScriptResultConstants.XML_SYNTAX_CHECK_FAILED)
    assertThat(outContent.toString().trim()).isEqualTo(
    "XML syntax error: The content of elements must consist of well-formed" +
      " character data or markup.\n" +
      "lineNumber: 4\n" +
      "columnNumber: 4\n" +
      "File: [ROOT]/testfiles/TestFile.xml"
    )
  }

  @Test
  fun testXmlSyntax_wrongClosingTag_xmlSyntaxIsInCorrect(){
    val invalidXML =
      "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
        "<shape xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
        "  android:shape=\"rectangle\">\n" +
        "  <solid android:color=\"#3333334D\" />\n" +
        "  <size android:height=\"1dp\" />\n" +
        "</shapes>"
    val tempFile = tempFolder.newFile("testfiles/TestFile.xml")
    tempFile.writeText(invalidXML)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(ScriptResultConstants.XML_SYNTAX_CHECK_FAILED)
    assertThat(outContent.toString().trim()).isEqualTo(
      "XML syntax error: The end-tag for element type \"shape\" must end" +
        " with a '>' delimiter.\n" +
        "lineNumber: 6\n" +
        "columnNumber: 8\n" +
        "File: [ROOT]/testfiles/TestFile.xml"
    )
  }

  @Test
  fun testXmlSyntax_multipleFilesHavingInvalidXml_xmlSyntaxIsInCorrect(){
    val invalidXMLForFile1 =
      "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
        "<shape xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
        "  android:shape=\"rectangle\">\n" +
        "  <<solid android:color=\"#3333334D\" />\n" +
        "  <size android:height=\"1dp\" />\n" +
        "</shape>"
    val invalidXMLForFile2 =
      "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
        "<shape xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
        "  android:shape=\"rectangle\">\n" +
        "  <solid android:color=\"#3333334D\" />\n" +
        "  <size android:height=\"1dp\" />\n" +
        "</shapes>"
    val tempFile1 = tempFolder.newFile("testfiles/TestFile1.xml")
    val tempFile2 = tempFolder.newFile("testfiles/TestFile2.xml")
    tempFile1.writeText(invalidXMLForFile1)
    tempFile2.writeText(invalidXMLForFile2)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(ScriptResultConstants.XML_SYNTAX_CHECK_FAILED)
    assertThat(outContent.toString().trim()).isEqualTo(
      "XML syntax error: The end-tag for element type \"shape\" must end" +
        " with a '>' delimiter.\n" +
        "lineNumber: 6\n" +
        "columnNumber: 8\n" +
        "File: [ROOT]/testfiles/TestFile2.xml\n\n"+
      "XML syntax error: The content of elements must consist of well-formed" +
        " character data or markup.\n" +
        "lineNumber: 4\n" +
        "columnNumber: 4\n" +
        "File: [ROOT]/testfiles/TestFile1.xml"
    )
  }

  private fun runScript() {
    XMLSyntaxCheck.main(tempFolder.getRoot().toString(), "testfiles")
  }
}
