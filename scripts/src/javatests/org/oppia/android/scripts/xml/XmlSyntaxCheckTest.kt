package org.oppia.android.scripts.xml

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.testing.assertion.assertThrows
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/** Tests for [XmlSyntaxCheck]. */
class XmlSyntaxCheckTest {
  private val outContent: ByteArrayOutputStream = ByteArrayOutputStream()
  private val originalOut: PrintStream = System.out
  private val XML_SYNTAX_CHECK_PASSED_OUTPUT_INDICATOR: String = "XML SYNTAX CHECK PASSED"
  private val XML_SYNTAX_CHECK_FAILED_OUTPUT_INDICATOR: String = "XML SYNTAX CHECK FAILED"
  private val syntaxFailureMessage1 =
    "The end-tag for element type \"shape\" must end with a '>' delimiter."
  private val syntaxFailureMessage2 =
    "The content of elements must consist of well-formed character data or markup."
  private val wikiReferenceNote =
    "Refer to https://github.com/oppia/oppia-android/wiki/Static-Analysis-Checks" +
      "#xml-syntax-check for more details on how to fix this."

  @Rule
  @JvmField
  var tempFolder = TemporaryFolder()

  @Before
  fun setUp() {
    tempFolder.newFolder("testfiles")
    System.setOut(PrintStream(outContent))
  }

  @After
  fun restoreStreams() {
    System.setOut(originalOut)
  }

  @Test
  fun testXmlSyntax_validXml_xmlSyntaxIsCorrect() {
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

    runScript()

    assertThat(outContent.toString().trim()).isEqualTo(XML_SYNTAX_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testXmlSyntax_invalidOpeningTag_xmlSyntaxIsIncorrect() {
    val invalidXml =
      """
      <?xml version="1.0" encoding="utf-8"?>
      <shape xmlns:android="http://schemas.android.com/apk/res/android"
        android:shape="rectangle">
        <solid android:color="#3333334D" />
        <size android:height="1dp" />
      </shapes>
      """.trimIndent()
    val tempFile = tempFolder.newFile("testfiles/TestFile.xml")
    tempFile.writeText(invalidXml)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(XML_SYNTAX_CHECK_FAILED_OUTPUT_INDICATOR)
    val failureMessage =
      """
      ${retrieveTestFilesDirectoryPath()}/TestFile.xml:6:8: $syntaxFailureMessage1
      
      $wikiReferenceNote
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testXmlSyntax_multipleFilesHavingInvalidXml_xmlSyntaxIsIncorrect() {
    val invalidXmlForFile1 =
      """
      <?xml version="1.0" encoding="utf-8"?>
      <shape xmlns:android="http://schemas.android.com/apk/res/android"
        android:shape="rectangle">
        <<solid android:color="#3333334D" />
        <size android:height="1dp" />
      </shape>
      """.trimIndent()
    val invalidXmlForFile2 =
      """
      <?xml version="1.0" encoding="utf-8"?>
      <shape xmlns:android="http://schemas.android.com/apk/res/android"
        android:shape="rectangle">
        <solid android:color="#3333334D" />
        <size android:height="1dp" />
      </shapes>
      """.trimIndent()
    val tempFile1 = tempFolder.newFile("testfiles/TestFile1.xml")
    val tempFile2 = tempFolder.newFile("testfiles/TestFile2.xml")
    tempFile1.writeText(invalidXmlForFile1)
    tempFile2.writeText(invalidXmlForFile2)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(XML_SYNTAX_CHECK_FAILED_OUTPUT_INDICATOR)
    val failureMessage =
      """
      ${retrieveTestFilesDirectoryPath()}/TestFile1.xml:4:4: $syntaxFailureMessage2
      ${retrieveTestFilesDirectoryPath()}/TestFile2.xml:6:8: $syntaxFailureMessage1
      
      $wikiReferenceNote
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testXmlSyntax_multipleFilesHavingInvalidXml_logsShouldBeLexicographicallySorted() {
    val invalidXmlForFile1 =
      """
      <?xml version="1.0" encoding="utf-8"?>
      <shape xmlns:android="http://schemas.android.com/apk/res/android"
        android:shape="rectangle">
        <<solid android:color="#3333334D" />
        <size android:height="1dp" />
      </shape>
      """.trimIndent()
    val invalidXmlForFile2 =
      """
      <?xml version="1.0" encoding="utf-8"?>
      <shape xmlns:android="http://schemas.android.com/apk/res/android"
        android:shape="rectangle">
        <solid android:color="#3333334D" />
        <size android:height="1dp" />
      </shapes>
      """.trimIndent()
    val invalidXmlForFile3 =
      """
      <?xml version="1.0" encoding="utf-8"?>
      <shape xmlns:android="http://schemas.android.com/apk/res/android"
        android:shape="rectangle">
        <solid android:color="#3333334D" />
        <size android:height="1dp" />
      </shapes>
      """.trimIndent()
    val tempFile3 = tempFolder.newFile("testfiles/TestFile3.xml")
    val tempFile1 = tempFolder.newFile("testfiles/TestFile1.xml")
    val tempFile2 = tempFolder.newFile("testfiles/TestFile2.xml")
    tempFile1.writeText(invalidXmlForFile1)
    tempFile2.writeText(invalidXmlForFile2)
    tempFile3.writeText(invalidXmlForFile3)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(XML_SYNTAX_CHECK_FAILED_OUTPUT_INDICATOR)
    val failureMessage =
      """
      ${retrieveTestFilesDirectoryPath()}/TestFile1.xml:4:4: $syntaxFailureMessage2
      ${retrieveTestFilesDirectoryPath()}/TestFile2.xml:6:8: $syntaxFailureMessage1
      ${retrieveTestFilesDirectoryPath()}/TestFile3.xml:6:8: $syntaxFailureMessage1
      
      $wikiReferenceNote
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  /** Retrieves the absolute path of testfiles directory. */
  private fun retrieveTestFilesDirectoryPath(): String {
    return "${tempFolder.root}/testfiles"
  }

  /** Runs the xml_syntax_check. */
  private fun runScript() {
    main(retrieveTestFilesDirectoryPath())
  }
}
