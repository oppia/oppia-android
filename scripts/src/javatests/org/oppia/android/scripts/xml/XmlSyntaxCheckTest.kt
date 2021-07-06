package org.oppia.android.scripts.xml

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.common.XML_SYNTAX_CHECK_FAILED_OUTPUT_INDICATOR
import org.oppia.android.scripts.common.XML_SYNTAX_CHECK_PASSED_OUTPUT_INDICATOR
import org.oppia.android.testing.assertThrows
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/** Tests for [XmlSyntaxCheck]. */
class XmlSyntaxCheckTest {
  private val outContent: ByteArrayOutputStream = ByteArrayOutputStream()
  private val originalOut: PrintStream = System.out

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
    assertThat(outContent.toString()).contains("${retrieveTestFilesDirectoryPath()}/TestFile.xml")
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
    assertThat(outContent.toString()).contains("${retrieveTestFilesDirectoryPath()}/TestFile2.xml")
    assertThat(outContent.toString()).contains("${retrieveTestFilesDirectoryPath()}/TestFile1.xml")
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
