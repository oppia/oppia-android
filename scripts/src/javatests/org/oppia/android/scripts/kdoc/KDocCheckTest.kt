package org.oppia.android.scripts.kdoc

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.testing.assertThrows
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/** Tests for [KDocCheck]. */
class KDocCheckTest {
  private val outContent: ByteArrayOutputStream = ByteArrayOutputStream()
  private val originalOut: PrintStream = System.out
  private val KDOC_CHECK_PASSED_OUTPUT_INDICATOR = "KDOC CHECK PASSED"
  private val KDOC_CHECK_FAILED_OUTPUT_INDICATOR = "KDOC CHECK FAILED"

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
  fun testKDoc_functionWithKDoc_checkShouldPass() {
    val testContent =
      """
      /** 
       * Returns the string corresponding to this error's string resources, or null if there 
       * is none.
       */
      fun getErrorMessageFromStringRes(context: Context): String? {
        return error?.let(context::getString)
      }
      """.trimIndent()
    val tempFile = tempFolder.newFile("testfiles/TempFile.kt")
    tempFile.writeText(testContent)

    runScript()

    assertThat(outContent.toString().trim()).isEqualTo(KDOC_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testKDoc_privateMemberWithoutKDoc_checkShouldPass() {
    val testContent =
      """
      private fun getErrorMessageFromStringRes(context: Context): String? {
        return error?.let(context::getString)
      }
      """.trimIndent()
    val tempFile = tempFolder.newFile("testfiles/TempFile.kt")
    tempFile.writeText(testContent)

    runScript()

    assertThat(outContent.toString().trim()).isEqualTo(KDOC_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testKDoc_memberWithOverrideModifierWithoutKDoc_checkShouldPass() {
    val testContent =
      """
      override fun getErrorMessageFromStringRes(context: Context): String? {
        return error?.let(context::getString)
      }
      """.trimIndent()
    val tempFile = tempFolder.newFile("testfiles/TempFile.kt")
    tempFile.writeText(testContent)

    runScript()

    assertThat(outContent.toString().trim()).isEqualTo(KDOC_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testKDoc_functionWithTestAnnotationWithoutKDoc_checkShouldPass() {
    val testContent =
      """
      @Test
      fun getErrorMessageFromStringRes(context: Context): String? {
        return error?.let(context::getString)
      }
      """.trimIndent()
    val tempFile = tempFolder.newFile("testfiles/TempFile.kt")
    tempFile.writeText(testContent)

    runScript()

    assertThat(outContent.toString().trim()).isEqualTo(KDOC_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testKDoc_functionWithBeforeAnnotationWithoutKDoc_checkShouldPass() {
    val testContent =
      """
      @Before
      fun getErrorMessageFromStringRes(context: Context): String? {
        return error?.let(context::getString)
      }
      """.trimIndent()
    val tempFile = tempFolder.newFile("testfiles/TempFile.kt")
    tempFile.writeText(testContent)

    runScript()

    assertThat(outContent.toString().trim()).isEqualTo(KDOC_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testKDoc_fieldWithInjectAnnotationWithoutKDoc_checkShouldPass() {
    val testContent =
      """
      @Inject
      lateinit var administratorControlsViewModel: AdministratorControlsViewModel
      """.trimIndent()
    val tempFile = tempFolder.newFile("testfiles/TempFile.kt")
    tempFile.writeText(testContent)

    runScript()

    assertThat(outContent.toString().trim()).isEqualTo(KDOC_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testKDoc_constructorWithInjectAnnotationWithoutKDoc_checkShouldPass() {
    val testContent =
      """
      /** Test Class. */
      class SelectionFragmentModel{

          @Inject
          constructor(channelInfosRepository: ChannelInfosRepository) : super() {
              this.channelInfosRepository = channelInfosRepository
          }
      }
      """.trimIndent()
    val tempFile = tempFolder.newFile("testfiles/TempFile.kt")
    tempFile.writeText(testContent)

    runScript()

    assertThat(outContent.toString().trim()).isEqualTo(KDOC_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testKDoc_classMissingKDoc_checkShouldFail() {
    val testContent =
      """
      class TestClass {}  
      """.trimIndent()
    val tempFile = tempFolder.newFile("testfiles/TempFile.kt")
    tempFile.writeText(testContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(KDOC_CHECK_FAILED_OUTPUT_INDICATOR)
    val failureMessage =
      """
      ${retrieveTestFilesDirectoryPath()}/TempFile.kt:1: missing KDoc
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testKDoc_nestedClassMissingKDoc_checkShouldFail() {
    val testContent =
      """
      /** Test KDoc 1. */
      class TestClass {
        /** Test KDoc 2. */
        class NestedClass {
          class NestedLevel2Class {}
        }
      }  
      """.trimIndent()
    val tempFile = tempFolder.newFile("testfiles/TempFile.kt")
    tempFile.writeText(testContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(KDOC_CHECK_FAILED_OUTPUT_INDICATOR)
    val failureMessage =
      """
      ${retrieveTestFilesDirectoryPath()}/TempFile.kt:5: missing KDoc
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testKDoc_classMembersMissingKDoc_checkShouldFail() {
    val testContent =
      """
      /** Test KDoc 1. */
      class TestClass {
        val testVal = "test"
        
        fun testFunc(){}
      }  
      """.trimIndent()
    val tempFile = tempFolder.newFile("testfiles/TempFile.kt")
    tempFile.writeText(testContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(KDOC_CHECK_FAILED_OUTPUT_INDICATOR)
    val failureMessage =
      """
      ${retrieveTestFilesDirectoryPath()}/TempFile.kt:3: missing KDoc
      ${retrieveTestFilesDirectoryPath()}/TempFile.kt:5: missing KDoc
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testKDoc_fieldsMissingKDoc_checkShouldFail() {
    val testContent =
      """
      val testVal = "dddd"
      var testVar = true
      """.trimIndent()
    val tempFile = tempFolder.newFile("testfiles/TempFile.kt")
    tempFile.writeText(testContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(KDOC_CHECK_FAILED_OUTPUT_INDICATOR)
    val failureMessage =
      """
      ${retrieveTestFilesDirectoryPath()}/TempFile.kt:1: missing KDoc
      ${retrieveTestFilesDirectoryPath()}/TempFile.kt:2: missing KDoc
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testKDoc_functionMissingKDoc_checkShouldFail() {
    val testContent =
      """
      fun getErrorMessageFromStringRes(context: Context): String? {
        return error?.let(context::getString)
      }  
      """.trimIndent()
    val tempFile = tempFolder.newFile("testfiles/TempFile.kt")
    tempFile.writeText(testContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(KDOC_CHECK_FAILED_OUTPUT_INDICATOR)
    val failureMessage =
      """
      ${retrieveTestFilesDirectoryPath()}/TempFile.kt:1: missing KDoc
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testKDoc_constructorMissingKDoc_checkShouldFail() {
    val testContent =
      """
      /** Test KDoc. */
      class TestClass {
        constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
      }
      """.trimIndent()
    val tempFile = tempFolder.newFile("testfiles/TempFile.kt")
    tempFile.writeText(testContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(KDOC_CHECK_FAILED_OUTPUT_INDICATOR)
    val failureMessage =
      """
      ${retrieveTestFilesDirectoryPath()}/TempFile.kt:3: missing KDoc
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testKDoc_enumMissingKDoc_checkShouldFail() {
    val testContent =
      """
      enum class WalkthroughPages(val value: Int) {
        WELCOME(0),
        TOPIC_LIST(1),
        FINAL(2)
      }  
      """.trimIndent()
    val tempFile = tempFolder.newFile("testfiles/TempFile.kt")
    tempFile.writeText(testContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(KDOC_CHECK_FAILED_OUTPUT_INDICATOR)
    val failureMessage =
      """
      ${retrieveTestFilesDirectoryPath()}/TempFile.kt:1: missing KDoc
      ${retrieveTestFilesDirectoryPath()}/TempFile.kt:2: missing KDoc
      ${retrieveTestFilesDirectoryPath()}/TempFile.kt:3: missing KDoc
      ${retrieveTestFilesDirectoryPath()}/TempFile.kt:4: missing KDoc
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testKDoc_interfaceMissingKDoc_checkShouldFail() {
    val testContent =
      """
      interface ChapterSelector {
        
        fun chapterSelected(chapterIndex: Int, nextStoryIndex: Int, explorationId: String)
      
        fun chapterUnselected(chapterIndex: Int, nextStoryIndex: Int)
      }  
      """.trimIndent()
    val tempFile = tempFolder.newFile("testfiles/TempFile.kt")
    tempFile.writeText(testContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(KDOC_CHECK_FAILED_OUTPUT_INDICATOR)
    val failureMessage =
      """
      ${retrieveTestFilesDirectoryPath()}/TempFile.kt:1: missing KDoc
      ${retrieveTestFilesDirectoryPath()}/TempFile.kt:3: missing KDoc
      ${retrieveTestFilesDirectoryPath()}/TempFile.kt:5: missing KDoc
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testKDoc_companionObjectMissingKDoc_checkShouldFail() {
    val testContent =
      """
      /** Test KDoc. */
      class TestClass {
        companion object {
          val pos = 1
          
          fun incrementedPosition(position: Int): Int {
            return position+1
          }
          
          fun decrementedPosition(position: Int): Int {
            return position-1
          }
        }
      }
      """.trimIndent()
    val tempFile = tempFolder.newFile("testfiles/TempFile.kt")
    tempFile.writeText(testContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(KDOC_CHECK_FAILED_OUTPUT_INDICATOR)
    val failureMessage =
      """
      ${retrieveTestFilesDirectoryPath()}/TempFile.kt:4: missing KDoc
      ${retrieveTestFilesDirectoryPath()}/TempFile.kt:6: missing KDoc
      ${retrieveTestFilesDirectoryPath()}/TempFile.kt:10: missing KDoc
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testKDoc_annotationMissingKDoc_checkShouldFail() {
    val testContent =
      """
      import javax.inject.Qualifier
      
      @Qualifier
      annotation class DelayShowAdditionalHintsFromWrongAnswerMillis
      """.trimIndent()
    val tempFile = tempFolder.newFile("testfiles/TempFile.kt")
    tempFile.writeText(testContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(KDOC_CHECK_FAILED_OUTPUT_INDICATOR)
    val failureMessage =
      """
      ${retrieveTestFilesDirectoryPath()}/TempFile.kt:4: missing KDoc
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testKDoc_multipleFilesMissingKDoc_multipleFailuresShouldBeReported() {
    val testContent1 =
      """
      import javax.inject.Qualifier
      
      @Qualifier
      annotation class DelayShowAdditionalHintsFromWrongAnswerMillis
      """.trimIndent()
    val testContent2 =
      """
      class TestClass {
        fun testFunc(){}
        
        private val testVal = "test"
        
        val testVal2 = "test2"
      }
      """.trimIndent()
    val tempFile1 = tempFolder.newFile("testfiles/TempFile1.kt")
    val tempFile2 = tempFolder.newFile("testfiles/TempFile2.kt")
    tempFile1.writeText(testContent1)
    tempFile2.writeText(testContent2)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(KDOC_CHECK_FAILED_OUTPUT_INDICATOR)
    val failureMessage =
      """
      ${retrieveTestFilesDirectoryPath()}/TempFile2.kt:1: missing KDoc
      ${retrieveTestFilesDirectoryPath()}/TempFile2.kt:2: missing KDoc
      ${retrieveTestFilesDirectoryPath()}/TempFile2.kt:6: missing KDoc
      ${retrieveTestFilesDirectoryPath()}/TempFile1.kt:4: missing KDoc
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testKDoc_exemptedFileMissingKDocs_checkShouldPass() {
    val testContent =
      """
      fun getErrorMessageFromStringRes(context: Context): String? {
        return error?.let(context::getString)
      }
      """.trimIndent()
    tempFolder.newFolder(
      "testfiles", "app", "src", "main", "java", "org", "oppia", "android", "app", "activity"
    )
    val exemptedFile = tempFolder.newFile(
      "testfiles/app/src/main/java/org/oppia/android/app/activity/ActivityComponent.kt"
    )
    exemptedFile.writeText(testContent)

    runScript()

    assertThat(outContent.toString().trim()).isEqualTo(KDOC_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  /** Retrieves the absolute path of testfiles directory. */
  private fun retrieveTestFilesDirectoryPath(): String {
    return "${tempFolder.root}/testfiles"
  }

  /** Runs the kdoc_check. */
  private fun runScript() {
    main(retrieveTestFilesDirectoryPath())
  }
}
