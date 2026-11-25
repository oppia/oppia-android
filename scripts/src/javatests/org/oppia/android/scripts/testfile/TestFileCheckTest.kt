package org.oppia.android.scripts.testfile

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.proto.TestFileExemptions
import org.oppia.android.scripts.proto.TestFileExemptions.TestFileExemption
import org.oppia.android.testing.assertThrows
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.nio.file.NoSuchFileException

/** Tests for [TestFileCheck]. */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class TestFileCheckTest {
  private val outContent: ByteArrayOutputStream = ByteArrayOutputStream()
  private val originalOut: PrintStream = System.out
  private val wikiReferenceNote =
    "Refer to https://github.com/oppia/oppia-android/wiki/Static-Analysis-Checks" +
      "#test-file-presence-check for more details on how to fix this."

  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

  @Before
  fun setUp() {
    System.setOut(PrintStream(outContent))
  }

  @After
  fun restoreStreams() {
    System.setOut(originalOut)
  }

  @Test
  fun testCheck_emptyDirectory_passes() {
    val checkPassed = runScript()

    assertThat(checkPassed).isTrue()
  }

  @Test
  fun testCheck_prodFileWithPackageMissing_throwsException() {
    createAppProdFile("demo", "ProdFile.kt")

    val exception = assertThrows<Exception> { runScript() }

    assertThat(exception).hasMessageThat().contains("Failed to find 'package' declaration")
    assertThat(exception)
      .hasMessageThat()
      .contains("app/src/main/java/org/oppia/android/app/demo/ProdFile.kt")
  }

  @Test
  fun testCheck_testFileWithPackageMissing_throwsException() {
    createAppTestFile("demo", "ProdFileTest.kt")

    val exception = assertThrows<Exception> { runScript() }

    assertThat(exception).hasMessageThat().contains("Failed to find 'package' declaration")
    assertThat(exception)
      .hasMessageThat()
      .contains("app/src/test/java/org/oppia/android/app/demo/ProdFileTest.kt")
  }

  @Test
  fun testCheck_prodFileWithInvalidMalformedPackage_throwsException() {
    createAppProdFile("demo", "ProdFile.kt").writeText("package ")

    val exception = assertThrows<Exception> { runScript() }

    assertThat(exception).hasMessageThat().contains("is missing expected package declaration")
    assertThat(exception)
      .hasMessageThat()
      .contains("app/src/main/java/org/oppia/android/app/demo/ProdFile.kt")
  }

  @Test
  fun testCheck_testFileWithInvalidMalformedPackage_throwsException() {
    createAppTestFile("demo", "ProdFileTest.kt").writeText("package ")

    val exception = assertThrows<Exception> { runScript() }

    assertThat(exception).hasMessageThat().contains("is missing expected package declaration")
    assertThat(exception)
      .hasMessageThat()
      .contains("app/src/test/java/org/oppia/android/app/demo/ProdFileTest.kt")
  }

  @Test
  fun testCheck_prodFileWithInvalidPackageStart_throwsException() {
    createAppProdFile("demo", "ProdFile.kt").writeText("package not.org.oppia.android.app.demo")

    val exception = assertThrows<Exception> { runScript() }

    assertThat(exception).hasMessageThat().contains("Invalid package parsed")
    assertThat(exception)
      .hasMessageThat()
      .contains("app/src/main/java/org/oppia/android/app/demo/ProdFile.kt")
    assertThat(exception).hasMessageThat().contains("not.org.oppia.android.app.demo")
  }

  @Test
  fun testCheck_testFileWithInvalidPackageStart_throwsException() {
    createAppTestFile("demo", "ProdFileTest.kt").writeText("package not.org.oppia.android.app.demo")

    val exception = assertThrows<Exception> { runScript() }

    assertThat(exception).hasMessageThat().contains("Invalid package parsed")
    assertThat(exception)
      .hasMessageThat()
      .contains("app/src/test/java/org/oppia/android/app/demo/ProdFileTest.kt")
    assertThat(exception).hasMessageThat().contains("not.org.oppia.android.app.demo")
  }

  @Test
  fun testCheck_prodFileWithInvalidLayerPackage_throwsException() {
    createAppProdFile("demo", "ProdFile.kt").writeText("package org.oppia.android.badlayer.demo")

    val exception = assertThrows<Exception> { runScript() }

    assertThat(exception).hasMessageThat().contains("Invalid layer badlayer")
    assertThat(exception).hasMessageThat().contains("org.oppia.android.badlayer.demo")
    assertThat(exception)
      .hasMessageThat()
      .contains("app/src/main/java/org/oppia/android/app/demo/ProdFile.kt")
  }

  @Test
  fun testCheck_testFileWithInvalidLayerPackage_throwsException() {
    createAppTestFile("demo", "ProdFileTest.kt")
      .writeText("package org.oppia.android.badlayer.demo")

    val exception = assertThrows<Exception> { runScript() }

    assertThat(exception).hasMessageThat().contains("Invalid layer badlayer")
    assertThat(exception).hasMessageThat().contains("org.oppia.android.badlayer.demo")
    assertThat(exception)
      .hasMessageThat()
      .contains("app/src/test/java/org/oppia/android/app/demo/ProdFileTest.kt")
  }

  @Test
  fun testCheck_prodFileInDifferentLocationThanPackage_throwsException() {
    createAppProdFile("demo", "ProdFile.kt").writeText("package org.oppia.android.app.diffpackage")

    val exception = assertThrows<Exception> { runScript() }

    assertThat(exception).hasMessageThat().contains("doesn't match its package")
    assertThat(exception)
      .hasMessageThat()
      .contains("app/src/main/java/org/oppia/android/app/demo/ProdFile.kt")
    assertThat(exception).hasMessageThat().contains("org.oppia.android.app.diffpackage")
    assertThat(exception)
      .hasMessageThat()
      .contains("or it's in the wrong test directory for this layer")
  }

  @Test
  fun testCheck_testFileInDifferentLocationThanPackage_throwsException() {
    createAppTestFile("demo", "ProdFileTest.kt")
      .writeText("package org.oppia.android.app.diffpackage")

    val exception = assertThrows<Exception> { runScript() }

    assertThat(exception).hasMessageThat().contains("doesn't match its package")
    assertThat(exception)
      .hasMessageThat()
      .contains("app/src/test/java/org/oppia/android/app/demo/ProdFileTest.kt")
    assertThat(exception).hasMessageThat().contains("org.oppia.android.app.diffpackage")
    assertThat(exception)
      .hasMessageThat()
      .contains("or it's in the wrong test directory for this layer")
  }

  @Test
  fun testCheck_prodFileWithNoTestFile_failsWithError() {
    createAppProdFile("demo", "ProdFile.kt").writeText(
      """
      package org.oppia.android.app.demo
      class ProdFile
      """.trimIndent()
    )

    val checkPassed = runScript()

    assertThat(checkPassed).isFalse()
    val failureMessage =
      """
      ========== Classes missing test files: 1 ==========
      - File app/src/main/java/org/oppia/android/app/demo/ProdFile.kt has no corresponding test file.

      $wikiReferenceNote
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testCheck_testFileWithNoProdFile_failsWithError() {
    createAppTestFile("demo", "ProdFileTest.kt").writeText(
      """
      package org.oppia.android.app.demo
      class ProdFileTest
      """.trimIndent()
    )

    val checkPassed = runScript()

    assertThat(checkPassed).isFalse()
    val failureMessage =
      """
      ========== Tests missing prod files: 1 ==========
      - Test app/src/test/java/org/oppia/android/app/demo/ProdFileTest.kt has no corresponding prod file. Is it in the wrong package?

      $wikiReferenceNote
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testCheck_prodFileWithTestFileInWrongLocation_failsWithErrorsAndPossibleMatches() {
    createAppProdFile("demo", "ProdFile.kt").writeText(
      """
      package org.oppia.android.app.demo
      class ProdFile
      """.trimIndent()
    )
    createAppTestFile("sample2", "ProdFileTest.kt").writeText(
      """
      package org.oppia.android.app.sample2
      class ProdFileTest
      """.trimIndent()
    )

    val checkPassed = runScript()

    assertThat(checkPassed).isFalse()
    val failureMessage =
      """
      ========== Classes missing test files: 1 ==========
      - File app/src/main/java/org/oppia/android/app/demo/ProdFile.kt has no corresponding test file. Possible matches:
        - app/src/test/java/org/oppia/android/app/sample2/ProdFileTest.kt

      ========== Tests missing prod files: 1 ==========
      - Test app/src/test/java/org/oppia/android/app/sample2/ProdFileTest.kt has no corresponding prod file. Is it in the wrong package? Possible matches:
        - app/src/main/java/org/oppia/android/app/demo/ProdFile.kt

      $wikiReferenceNote
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testCheck_prodFileWithTestFileWithWrongName_failsWithErrorsAndNoPossibleMatches() {
    createAppProdFile("demo", "ProdFile.kt").writeText(
      """
      package org.oppia.android.app.demo
      class ProdFile
      """.trimIndent()
    )
    createAppTestFile("sample", "ProdFile2Test.kt").writeText(
      """
      package org.oppia.android.app.sample
      class ProdFile2Test
      """.trimIndent()
    )

    val checkPassed = runScript()

    assertThat(checkPassed).isFalse()
    val failureMessage =
      """
      ========== Classes missing test files: 1 ==========
      - File app/src/main/java/org/oppia/android/app/demo/ProdFile.kt has no corresponding test file.

      ========== Tests missing prod files: 1 ==========
      - Test app/src/test/java/org/oppia/android/app/sample/ProdFile2Test.kt has no corresponding prod file. Is it in the wrong package?

      $wikiReferenceNote
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testCheck_appLayer_ktProdFile_testFileInTestDir_passes() {
    createAppProdFile("demo", "ProdFile.kt").writeText(
      """
      package org.oppia.android.app.demo
      class ProdFile
      """.trimIndent()
    )
    createAppTestFile("demo", "ProdFileTest.kt").writeText(
      """
      package org.oppia.android.app.demo
      class ProdFileTest
      """.trimIndent()
    )

    val checkPassed = runScript()

    assertThat(checkPassed).isTrue()
  }

  @Test
  fun testCheck_appLayer_ktProdFile_testFileInSharedTestDir_passes() {
    createAppProdFile("demo", "ProdFile.kt").writeText(
      """
      package org.oppia.android.app.demo
      class ProdFile
      """.trimIndent()
    )
    createAppTestFile("demo", "ProdFileTest.kt", testDir = "sharedTest").writeText(
      """
      package org.oppia.android.app.demo
      class ProdFileTest
      """.trimIndent()
    )

    val checkPassed = runScript()

    assertThat(checkPassed).isTrue()
  }

  @Test
  fun testCheck_appLayer_ktProdFile_testFileInBothTestDirs_passes() {
    createAppProdFile("demo", "ProdFile.kt").writeText(
      """
      package org.oppia.android.app.demo
      class ProdFile
      """.trimIndent()
    )
    createAppTestFile("demo", "ProdFileTest.kt", testDir = "test").writeText(
      """
      package org.oppia.android.app.demo
      class ProdFileTest
      """.trimIndent()
    )
    createAppTestFile("demo", "ProdFileTest.kt", testDir = "sharedTest").writeText(
      """
      package org.oppia.android.app.demo
      class ProdFileTest
      """.trimIndent()
    )

    val checkPassed = runScript()

    assertThat(checkPassed).isTrue()
  }

  @Test
  fun testCheck_appLayer_javaProdFile_testFileInTestDir_passes() {
    createAppProdFile("demo", "ProdFile.java").writeText(
      """
      package org.oppia.android.app.demo;
      public final class ProdFile {}
      """.trimIndent()
    )
    createAppTestFile("demo", "ProdFileTest.kt").writeText(
      """
      package org.oppia.android.app.demo
      class ProdFileTest
      """.trimIndent()
    )

    val checkPassed = runScript()

    assertThat(checkPassed).isTrue()
  }

  @Test
  fun testCheck_appLayer_javaProdFile_testFileInSharedTestDir_passes() {
    createAppProdFile("demo", "ProdFile.java").writeText(
      """
      package org.oppia.android.app.demo;
      public final class ProdFile {}
      """.trimIndent()
    )
    createAppTestFile("demo", "ProdFileTest.kt", testDir = "sharedTest").writeText(
      """
      package org.oppia.android.app.demo
      class ProdFileTest
      """.trimIndent()
    )

    val checkPassed = runScript()

    assertThat(checkPassed).isTrue()
  }

  @Test
  fun testCheck_appLayer_javaProdFile_testFileInBothTestDirs_passes() {
    createAppProdFile("demo", "ProdFile.java").writeText(
      """
      package org.oppia.android.app.demo;
      public final class ProdFile {}
      """.trimIndent()
    )
    createAppTestFile("demo", "ProdFileTest.kt", testDir = "test").writeText(
      """
      package org.oppia.android.app.demo
      class ProdFileTest
      """.trimIndent()
    )
    createAppTestFile("demo", "ProdFileTest.kt", testDir = "sharedTest").writeText(
      """
      package org.oppia.android.app.demo
      class ProdFileTest
      """.trimIndent()
    )

    val checkPassed = runScript()

    assertThat(checkPassed).isTrue()
  }

  @Test
  fun testCheck_domainLayer_ktProdFile_testFileInTestDir_passes() {
    createMavenProdFile(layer = "domain", "demo", "ProdFile.kt").writeText(
      """
      package org.oppia.android.domain.demo
      class ProdFile
      """.trimIndent()
    )
    createMavenTestFile(layer = "domain", "demo", "ProdFileTest.kt").writeText(
      """
      package org.oppia.android.domain.demo
      class ProdFileTest
      """.trimIndent()
    )

    val checkPassed = runScript()

    assertThat(checkPassed).isTrue()
  }

  @Test
  fun testCheck_dataLayer_ktProdFile_testFileInTestDir_passes() {
    createMavenProdFile(layer = "data", "demo", "ProdFile.kt").writeText(
      """
      package org.oppia.android.data.demo
      class ProdFile
      """.trimIndent()
    )
    createMavenTestFile(layer = "data", "demo", "ProdFileTest.kt").writeText(
      """
      package org.oppia.android.data.demo
      class ProdFileTest
      """.trimIndent()
    )

    val checkPassed = runScript()

    assertThat(checkPassed).isTrue()
  }

  @Test
  fun testCheck_testingLayer_ktProdFile_testFileInTestDir_passes() {
    createMavenProdFile(layer = "testing", "demo", "ProdFile.kt").writeText(
      """
      package org.oppia.android.testing.demo
      class ProdFile
      """.trimIndent()
    )
    createMavenTestFile(layer = "testing", "demo", "ProdFileTest.kt").writeText(
      """
      package org.oppia.android.testing.demo
      class ProdFileTest
      """.trimIndent()
    )

    val checkPassed = runScript()

    assertThat(checkPassed).isTrue()
  }

  @Test
  fun testCheck_utilityLayer_ktProdFile_testFileInTestDir_passes() {
    // Note that the layer (utility) and the package (util) differ specific for utility tests.
    createMavenProdFile(layer = "util", "demo", "ProdFile.kt", layerDir = "utility").writeText(
      """
      package org.oppia.android.util.demo
      class ProdFile
      """.trimIndent()
    )
    createMavenTestFile(layer = "util", "demo", "ProdFileTest.kt", layerDir = "utility").writeText(
      """
      package org.oppia.android.util.demo
      class ProdFileTest
      """.trimIndent()
    )

    val checkPassed = runScript()

    assertThat(checkPassed).isTrue()
  }

  @Test
  fun testCheck_instrumentationLayer_ktProdFile_testFileInTestDir_passes() {
    createBazelProdFile(layer = "instrumentation", "demo", "ProdFile.kt").writeText(
      """
      package org.oppia.android.instrumentation.demo
      class ProdFile
      """.trimIndent()
    )
    createBazelTestFile(layer = "instrumentation", "demo", "ProdFileTest.kt").writeText(
      """
      package org.oppia.android.instrumentation.demo
      class ProdFileTest
      """.trimIndent()
    )

    val checkPassed = runScript()

    assertThat(checkPassed).isTrue()
  }

  @Test
  fun testCheck_scriptsLayer_ktProdFile_testFileInTestDir_passes() {
    createBazelProdFile(layer = "scripts", "demo", "ProdFile.kt").writeText(
      """
      package org.oppia.android.scripts.demo
      class ProdFile
      """.trimIndent()
    )
    createBazelTestFile(layer = "scripts", "demo", "ProdFileTest.kt").writeText(
      """
      package org.oppia.android.scripts.demo
      class ProdFileTest
      """.trimIndent()
    )

    val checkPassed = runScript()

    assertThat(checkPassed).isTrue()
  }

  @Test
  fun testCheck_prodFileWithExtraTest_noExemptions_failsForUnmatchedExtraTest() {
    createAppProdFile("demo", "ProdFile.java").writeText(
      """
      package org.oppia.android.app.demo;
      public final class ProdFile {}
      """.trimIndent()
    )
    createAppTestFile("demo", "ProdFileTest.kt").writeText(
      """
      package org.oppia.android.app.demo
      class ProdFileTest
      """.trimIndent()
    )
    tempFolder.newFile("app/src/test/java/org/oppia/android/app/demo/ExtraProdFileTest.kt").also {
      it.writeText(
        """
        package org.oppia.android.app.demo
        class ExtraProdFileTest
        """.trimIndent()
      )
    }

    val checkPassed = runScript()

    // A lack of an exemption will result in a failure for the extra test.
    assertThat(checkPassed).isFalse()
    val failureMessage =
      """
      ========== Tests missing prod files: 1 ==========
      - Test app/src/test/java/org/oppia/android/app/demo/ExtraProdFileTest.kt has no corresponding prod file. Is it in the wrong package?

      $wikiReferenceNote
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testCheck_multipleLayerProdAndTestFiles_mixOfMissingAndWrongLocations_failsWithAllProblems() {
    createMavenProdFile(layer = "data", "demo", "ProdFile1.kt").writeText(
      """
      package org.oppia.android.data.demo
      class ProdFile1
      """.trimIndent()
    )
    createMavenProdFile(layer = "util", "demo", "ProdFile2.kt", layerDir = "utility").writeText(
      """
      package org.oppia.android.util.demo
      class ProdFile2
      """.trimIndent()
    )
    createBazelTestFile(layer = "scripts", "demo", "ProdFile3Test.kt").writeText(
      """
      package org.oppia.android.scripts.demo
      class ProdFile3Test
      """.trimIndent()
    )

    val checkPassed = runScript()

    assertThat(checkPassed).isFalse()
    val failureMessage =
      """
      ========== Classes missing test files: 2 ==========
      - File data/src/main/java/org/oppia/android/data/demo/ProdFile1.kt has no corresponding test file.
      - File utility/src/main/java/org/oppia/android/util/demo/ProdFile2.kt has no corresponding test file.
      
      ========== Tests missing prod files: 1 ==========
      - Test scripts/src/javatests/org/oppia/android/scripts/demo/ProdFile3Test.kt has no corresponding prod file. Is it in the wrong package?

      $wikiReferenceNote
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testCheck_exemptions_testFileNotRequired_prodFileMissingTest_passes() {
    createAppProdFile("demo", "ProdFile.kt").writeText(
      """
      package org.oppia.android.app.demo
      class ProdFile
      """.trimIndent()
    )

    val checkPassed = runScript(
      TestFileExemption.newBuilder().apply {
        this.exemptedFilePath = "app/src/main/java/org/oppia/android/app/demo/ProdFile.kt"
        this.testFileNotRequired = true
      }.build()
    )

    assertThat(checkPassed).isTrue()
  }

  @Test
  fun testCheck_exemptions_testFileNotRequired_prodFileWithMainTest_passes() {
    createAppProdFile("demo", "ProdFile.kt").writeText(
      """
      package org.oppia.android.app.demo
      class ProdFile
      """.trimIndent()
    )
    createAppTestFile("demo", "ProdFileTest.kt").writeText(
      """
      package org.oppia.android.app.demo
      class ProdFileTest
      """.trimIndent()
    )

    // The exemption should be ignored if the test actually exists (this may become a failure in
    // future changes to the script).
    val checkPassed = runScript(
      TestFileExemption.newBuilder().apply {
        this.exemptedFilePath = "app/src/main/java/org/oppia/android/app/demo/ProdFile.kt"
        this.testFileNotRequired = true
      }.build()
    )

    assertThat(checkPassed).isTrue()
  }

  @Test
  fun testCheck_exemptions_overrideMinCoverage_prodFileMissingTest_failsWithError() {
    createAppProdFile("demo", "ProdFile.kt").writeText(
      """
      package org.oppia.android.app.demo
      class ProdFile
      """.trimIndent()
    )

    val checkPassed = runScript(
      TestFileExemption.newBuilder().apply {
        this.exemptedFilePath = "app/src/main/java/org/oppia/android/app/demo/ProdFile.kt"
        this.overrideMinCoveragePercentRequired = 50
      }.build()
    )

    // Coverage overrides still require there to be a test for the production file.
    assertThat(checkPassed).isFalse()
    val failureMessage =
      """
      ========== Classes missing test files: 1 ==========
      - File app/src/main/java/org/oppia/android/app/demo/ProdFile.kt has no corresponding test file.

      $wikiReferenceNote
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testCheck_exemptions_sourceFileIsIncompatibleWithCov_prodFileMissingTest_failsWithError() {
    createAppProdFile("demo", "ProdFile.kt").writeText(
      """
      package org.oppia.android.app.demo
      class ProdFile
      """.trimIndent()
    )

    val checkPassed = runScript(
      TestFileExemption.newBuilder().apply {
        this.exemptedFilePath = "app/src/main/java/org/oppia/android/app/demo/ProdFile.kt"
        this.sourceFileIsIncompatibleWithCodeCoverage = true
      }.build()
    )

    // Tests incompatible with code coverage are still expected to exist.
    assertThat(checkPassed).isFalse()
    val failureMessage =
      """
      ========== Classes missing test files: 1 ==========
      - File app/src/main/java/org/oppia/android/app/demo/ProdFile.kt has no corresponding test file.

      $wikiReferenceNote
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testCheck_exemptions_isExtraTest_prodFileWithExtraTest_missingMainTest_failsWithError() {
    createAppProdFile("demo", "ProdFile.kt").writeText(
      """
      package org.oppia.android.app.demo
      class ProdFile
      """.trimIndent()
    )
    createAppTestFile("demo", "ExtraProdFileTest.kt").writeText(
      """
      package org.oppia.android.app.demo
      class ExtraProdFileTest
      """.trimIndent()
    )

    val checkPassed = runScript(
      TestFileExemption.newBuilder().apply {
        this.exemptedFilePath = "app/src/test/java/org/oppia/android/app/demo/ExtraProdFileTest.kt"
        this.isExtraTestFile = true
      }.build()
    )

    // Production files may have extra test files, but only if the production file has a main test.
    assertThat(checkPassed).isFalse()
    val failureMessage =
      """
      ========== Classes missing test files: 1 ==========
      - File app/src/main/java/org/oppia/android/app/demo/ProdFile.kt has no corresponding test file.

      $wikiReferenceNote
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testCheck_exemptions_isExtraTest_prodFileWithExtraTest_withMainTest_passes() {
    createAppProdFile("demo", "ProdFile.kt").writeText(
      """
      package org.oppia.android.app.demo
      class ProdFile
      """.trimIndent()
    )
    createAppTestFile("demo", "ProdFileTest.kt").writeText(
      """
      package org.oppia.android.app.demo
      class ProdFileTest
      """.trimIndent()
    )
    tempFolder.newFile("app/src/test/java/org/oppia/android/app/demo/ExtraProdFileTest.kt").also {
      it.writeText(
        """
        package org.oppia.android.app.demo
        class ExtraProdFileTest
        """.trimIndent()
      )
    }

    val checkPassed = runScript(
      TestFileExemption.newBuilder().apply {
        this.exemptedFilePath = "app/src/test/java/org/oppia/android/app/demo/ExtraProdFileTest.kt"
        this.isExtraTestFile = true
      }.build()
    )

    // The extra production test should be properly accounted for with the exemption.
    assertThat(checkPassed).isTrue()
  }

  @Test
  fun testCheck_exemptions_testFileNotRequired_prodFileDoesNotExist_failsWithError() {
    val checkPassed = runScript(
      TestFileExemption.newBuilder().apply {
        this.exemptedFilePath = "app/src/main/java/org/oppia/android/app/demo/ProdFile.kt"
        this.testFileNotRequired = true
      }.build()
    )

    assertThat(checkPassed).isFalse()
    val failureMessage =
      """
      ========== Test file exemption failures: 1 ==========
      - Exempted file path does not exist: app/src/main/java/org/oppia/android/app/demo/ProdFile.kt.
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testCheck_exemptions_overrideMinCoverage_prodFileDoesNotExist_failsWithError() {
    val checkPassed = runScript(
      TestFileExemption.newBuilder().apply {
        this.exemptedFilePath = "app/src/main/java/org/oppia/android/app/demo/ProdFile.kt"
        this.overrideMinCoveragePercentRequired = 50
      }.build()
    )

    assertThat(checkPassed).isFalse()
    val failureMessage =
      """
      ========== Test file exemption failures: 1 ==========
      - Exempted file path does not exist: app/src/main/java/org/oppia/android/app/demo/ProdFile.kt.
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testCheck_exemptions_sourceFileIsIncompatibleWithCov_prodFileDoesNotExist_failsWithError() {
    val checkPassed = runScript(
      TestFileExemption.newBuilder().apply {
        this.exemptedFilePath = "app/src/main/java/org/oppia/android/app/demo/ProdFile.kt"
        this.sourceFileIsIncompatibleWithCodeCoverage = true
      }.build()
    )

    assertThat(checkPassed).isFalse()
    val failureMessage =
      """
      ========== Test file exemption failures: 1 ==========
      - Exempted file path does not exist: app/src/main/java/org/oppia/android/app/demo/ProdFile.kt.
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testCheck_exemptions_isExtraTest_testFileDoesNotExist_failsWithError() {
    val checkPassed = runScript(
      TestFileExemption.newBuilder().apply {
        this.exemptedFilePath = "app/src/test/java/org/oppia/android/app/demo/ExtraProdFileTest.kt"
        this.isExtraTestFile = true
      }.build()
    )

    assertThat(checkPassed).isFalse()
    val failureMessage =
      """
      ========== Test file exemption failures: 1 ==========
      - Exempted file path does not exist: app/src/test/java/org/oppia/android/app/demo/ExtraProdFileTest.kt.
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testCheck_exemptions_multipleMissing_failsWithListOfErrors() {
    val checkPassed = runScript(
      TestFileExemption.newBuilder().apply {
        this.exemptedFilePath = "app/src/main/java/org/oppia/android/app/demo/ProdFile3.kt"
        this.testFileNotRequired = true
      }.build(),
      TestFileExemption.newBuilder().apply {
        this.exemptedFilePath = "app/src/main/java/org/oppia/android/app/demo/ProdFile2.kt"
        this.overrideMinCoveragePercentRequired = 50
      }.build(),
      TestFileExemption.newBuilder().apply {
        this.exemptedFilePath = "app/src/main/java/org/oppia/android/app/demo/ProdFile2.kt"
        this.sourceFileIsIncompatibleWithCodeCoverage = true
      }.build(),
      TestFileExemption.newBuilder().apply {
        this.exemptedFilePath = "app/src/test/java/org/oppia/android/app/demo/ExtraProdFileTest.kt"
        this.isExtraTestFile = true
      }.build()
    )

    // Exemption-only failures don't include a wiki note.
    assertThat(checkPassed).isFalse()
    val failureMessage =
      """
      ========== Test file exemption failures: 3 ==========
      - Exempted file path does not exist: app/src/main/java/org/oppia/android/app/demo/ProdFile3.kt.
      - Exempted file path does not exist: app/src/main/java/org/oppia/android/app/demo/ProdFile2.kt.
      - Exempted file path does not exist: app/src/test/java/org/oppia/android/app/demo/ExtraProdFileTest.kt.
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testCheck_multipleMissingExemptionsAndTests_failsWithMultipleListsOfErrors() {
    createMavenProdFile(layer = "data", "demo", "ProdFile1.kt").writeText(
      """
      package org.oppia.android.data.demo
      class ProdFile1
      """.trimIndent()
    )
    createMavenProdFile(layer = "util", "demo", "ProdFile2.kt", layerDir = "utility").writeText(
      """
      package org.oppia.android.util.demo
      class ProdFile2
      """.trimIndent()
    )
    createBazelTestFile(layer = "scripts", "demo", "ProdFile3Test.kt").writeText(
      """
      package org.oppia.android.scripts.demo
      class ProdFile3Test
      """.trimIndent()
    )

    val checkPassed = runScript(
      TestFileExemption.newBuilder().apply {
        this.exemptedFilePath = "app/src/main/java/org/oppia/android/app/demo/ProdFile5.kt"
        this.testFileNotRequired = true
      }.build(),
      TestFileExemption.newBuilder().apply {
        this.exemptedFilePath = "app/src/main/java/org/oppia/android/app/demo/ProdFile5.kt"
        this.overrideMinCoveragePercentRequired = 50
      }.build(),
      TestFileExemption.newBuilder().apply {
        this.exemptedFilePath = "app/src/main/java/org/oppia/android/app/demo/ProdFile4.kt"
        this.sourceFileIsIncompatibleWithCodeCoverage = true
      }.build(),
      TestFileExemption.newBuilder().apply {
        this.exemptedFilePath = "app/src/test/java/org/oppia/android/app/demo/ExtraProdFileTest.kt"
        this.isExtraTestFile = true
      }.build()
    )

    // When multiple failures are combined, exemptions can still include a wiki note.
    assertThat(checkPassed).isFalse()
    val failureMessage =
      """
      ========== Classes missing test files: 2 ==========
      - File data/src/main/java/org/oppia/android/data/demo/ProdFile1.kt has no corresponding test file.
      - File utility/src/main/java/org/oppia/android/util/demo/ProdFile2.kt has no corresponding test file.

      ========== Tests missing prod files: 1 ==========
      - Test scripts/src/javatests/org/oppia/android/scripts/demo/ProdFile3Test.kt has no corresponding prod file. Is it in the wrong package?

      ========== Test file exemption failures: 3 ==========
      - Exempted file path does not exist: app/src/main/java/org/oppia/android/app/demo/ProdFile5.kt.
      - Exempted file path does not exist: app/src/main/java/org/oppia/android/app/demo/ProdFile4.kt.
      - Exempted file path does not exist: app/src/test/java/org/oppia/android/app/demo/ExtraProdFileTest.kt.

      $wikiReferenceNote
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testCheckMain_zeroArguments_throwsException() {
    assertThrows<ArrayIndexOutOfBoundsException> { main() }
  }

  @Test
  fun testCheckMain_nonExistentDirectory_throwsException() {
    assertThrows<NoSuchFileException> {
      main(File(tempFolder.root, "fake_subdirectory").absolutePath)
    }
  }

  @Test
  fun testCheckMain_withMissingTests_throwsException() {
    createAppProdFile("demo", "ProdFile.java").writeText(
      """
      package org.oppia.android.app.demo;
      public final class ProdFile {}
      """.trimIndent()
    )

    val exception = assertThrows<Exception> { main(tempFolder.root.absolutePath) }

    // Verify that the failure check can be passed, but the success check can't be easily verified
    // due to the high file environment requirements to pass all exemptions (since the real
    // exemptions file is used when calling into main() directly).
    assertThat(exception).hasMessageThat().isEqualTo("TEST FILE CHECK FAILED")
  }

  private fun runScript(vararg exemptions: TestFileExemption): Boolean {
    val allExemptions =
      TestFileExemptions.newBuilder().addAllTestFileExemption(exemptions.toList()).build()
    val testFileExemptionsProto = File(tempFolder.root, "test_file_exemptions.pb").also {
      it.outputStream().use(allExemptions::writeTo)
    }
    val checker = TestFileCheck(tempFolder.root.absolutePath, testFileExemptionsProto.absolutePath)
    return checker.execute()
  }

  @Suppress("SameParameterValue")
  private fun createAppProdFile(subpkg: String, filename: String): File =
    createMavenProdFile(layer = "app", subpkg, filename)

  private fun createAppTestFile(subpkg: String, filename: String, testDir: String = "test"): File =
    createMavenTestFile(layer = "app", subpkg, filename, testDir)

  private fun createMavenProdFile(
    layer: String,
    subpkg: String,
    filename: String,
    layerDir: String = layer
  ): File {
    tempFolder.newFolder(layerDir, "src", "main", "java", "org", "oppia", "android", layer, subpkg)
    return tempFolder.newFile("$layerDir/src/main/java/org/oppia/android/$layer/$subpkg/$filename")
  }

  private fun createMavenTestFile(
    layer: String,
    subpkg: String,
    filename: String,
    testDir: String = "test",
    layerDir: String = layer
  ): File {
    tempFolder.newFolder(layerDir, "src", testDir, "java", "org", "oppia", "android", layer, subpkg)
    return tempFolder.newFile(
      "$layerDir/src/$testDir/java/org/oppia/android/$layer/$subpkg/$filename"
    )
  }

  @Suppress("SameParameterValue")
  private fun createBazelProdFile(layer: String, subpkg: String, filename: String): File {
    tempFolder.newFolder(layer, "src", "java", "org", "oppia", "android", layer, subpkg)
    return tempFolder.newFile("$layer/src/java/org/oppia/android/$layer/$subpkg/$filename")
  }

  @Suppress("SameParameterValue")
  private fun createBazelTestFile(layer: String, subpkg: String, filename: String): File {
    tempFolder.newFolder(layer, "src", "javatests", "org", "oppia", "android", layer, subpkg)
    return tempFolder.newFile("$layer/src/javatests/org/oppia/android/$layer/$subpkg/$filename")
  }
}
