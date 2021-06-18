package org.oppia.android.scripts

import java.io.File
import kotlin.test.assertEquals
import org.oppia.android.scripts.XMLSyntaxCheck
import org.junit.Test
import org.junit.Rule
import org.junit.Before
import org.junit.rules.TemporaryFolder
import org.junit.rules.ExpectedException

class XMLSyntaxCheckTest {
  @Rule
  @JvmField
  public var tempFolder: TemporaryFolder = TemporaryFolder()

  @Rule
  @JvmField
  var thrown: ExpectedException = ExpectedException.none()

  @Before
  fun initTestFilesDirectory() {
    val testFilesDirectory = tempFolder.newFolder("testfiles")
  }

  @Test
  fun test_correctXML_scriptCheckShouldPass(){
    val prohibitedContent =
    "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
      "<shape xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
      "  android:shape=\"rectangle\">\n" +
      "  <solid android:color=\"#3333334D\" />\n" +
      "  <size android:height=\"1dp\" />\n" +
      "</shape>"

    val fileContainsSuppotLibraryImport = tempFolder.newFile("testfiles/TestFile.xml")
    fileContainsSuppotLibraryImport.writeText(prohibitedContent)

    runScript()
  }

  @Test
  fun test_brokentXML_scriptCheckShouldFail(){
    val prohibitedContent =
      "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
        "<shape xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
        "  android:shape=\"rectangle\">\n" +
        "  <<solid android:color=\"#3333334D\" />\n" +
        "  <size android:height=\"1dp\" />\n" +
        "</shape>"

    expectScriptFailure()

    val fileContainsSuppotLibraryImport = tempFolder.newFile("testfiles/TestFile.xml")
    fileContainsSuppotLibraryImport.writeText(prohibitedContent)

    runScript()
  }

  fun runScript() {
    XMLSyntaxCheck.main(tempFolder.getRoot().toString(), "testfiles")
  }

  fun expectScriptFailure() {
    thrown.expect(java.lang.Exception::class.java)
    thrown.expectMessage("XML SYNTAX CHECK FAILED")
  }
}
