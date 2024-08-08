package org.oppia.android.scripts.ci

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ProtoStringEncoder.Companion.toCompressedBase64
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import org.oppia.android.scripts.proto.ChangedFilesBucket
import org.oppia.android.scripts.testing.TestBazelWorkspace
import org.oppia.android.testing.assertThrows
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream
import java.io.PrintStream
import java.util.concurrent.TimeUnit

/** Tests for the retrieve_changed_files utility. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
class RetrieveChangedFilesTest {
  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

  private val scriptBgDispatcher by lazy { ScriptBackgroundCoroutineDispatcher() }
  private lateinit var commandExecutor: CommandExecutor
  private lateinit var testBazelWorkspace: TestBazelWorkspace
  private lateinit var pendingOutputStream: ByteArrayOutputStream
  private lateinit var originalStandardOutputStream: OutputStream

  @Before
  fun setUp() {
    commandExecutor = initializeCommandExecutorWithLongProcessWaitTime()
    testBazelWorkspace = TestBazelWorkspace(tempFolder)

    // Redirect script output for testing purposes.
    pendingOutputStream = ByteArrayOutputStream()
    originalStandardOutputStream = System.out
    System.setOut(PrintStream(pendingOutputStream))
  }

  @After
  fun tearDown() {
    // Reinstate test output redirection.
    System.setOut(PrintStream(pendingOutputStream))

    // Print the status of the git repository to help with debugging in the cases of test failures
    // and to help manually verify the expect git state at the end of each test.
    println("git status (at end of test):")

    scriptBgDispatcher.close()
  }

  @Test
  fun testUtility_noArguments_printsUsageStringAndExits() {
    val exception = assertThrows<SecurityException>() { runScript() }

    // Bazel catches the System.exit() call and throws a SecurityException. This is a bit hacky way
    // to verify that System.exit() is called, but it's helpful.
    assertThat(exception).hasMessageThat().contains("System.exit()")
    assertThat(pendingOutputStream.toString()).contains("Usage:")
  }

  @Test
  fun testUtility_invalidArguments_printsUsageStringAndExits() {
    for (argCount in 0..4) {
      val args = Array(argCount) { "arg${it + 1}" }
      val exception = assertThrows<SecurityException> { runScript(*args) }

      // Bazel catches the System.exit() call and throws a SecurityException.
      assertThat(exception).hasMessageThat().contains("System.exit()")
      assertThat(pendingOutputStream.toString()).contains("Usage:")
    }
  }

  @Test
  fun testUtility_invalidBase64_throwsException() {
    assertThrows<IllegalArgumentException>() { runScript("${tempFolder.root}", "badbase64", "file1", "file2", "file3") }
  }

  @Test
  fun testUtility_validBase64_oneFile_writesCacheNameFile() {
    val cacheNameFilePath = tempFolder.getNewTempFilePath("cache_name")
    val changedFilePath = tempFolder.getNewTempFilePath("changed_file_list")
    val testTargetFilePath = tempFolder.getNewTempFilePath("test_target_list")
    val base64String = computeBase64String(
      ChangedFilesBucket.newBuilder().apply {
        cacheBucketName = "example"
        addChangedFiles("//example/to/a/file/Demonstration.kt")
      }.build()
    )

    runScript(tempFolder.root.absolutePath, base64String, cacheNameFilePath, changedFilePath, testTargetFilePath)

    assertThat(File(cacheNameFilePath).readText().trim()).isEqualTo("example")
  }

  @Test
  fun testUtility_validBase64_oneFile_writesChangedFilePathWithCorrectFile() {
    val cacheNameFilePath = tempFolder.getNewTempFilePath("cache_name")
    val changedFilePath = tempFolder.getNewTempFilePath("changed_file_list")
    val testTargetFilePath = tempFolder.getNewTempFilePath("test_target_list")
    val base64String = computeBase64String(
      ChangedFilesBucket.newBuilder().apply {
        cacheBucketName = "example"
        addChangedFiles("//example/to/a/file/Demonstration.kt")
      }.build()
    )

    runScript(tempFolder.root.absolutePath, base64String, cacheNameFilePath, changedFilePath, testTargetFilePath)

    assertThat(File(changedFilePath).readText().trim()).isEqualTo(
      "//example/to/a/file/Demonstration.kt"
    )
  }

  @Test
  fun testUtility_validBase64_multipleFiles_writesChangedFilePathWithCorrectFile() {
    val cacheNameFilePath = tempFolder.getNewTempFilePath("cache_name")
    val changedFilePath = tempFolder.getNewTempFilePath("changed_file_list")
    val testTargetFilePath = tempFolder.getNewTempFilePath("test_target_list")
    val base64String = computeBase64String(
      ChangedFilesBucket.newBuilder().apply {
        cacheBucketName = "example"
        addChangedFiles("//example/to/a/file/FirstDemonstration.kt")
        addChangedFiles("//example/to/a/file/SecondDemonstration.kt")
      }.build()
    )

    runScript(tempFolder.root.absolutePath, base64String, cacheNameFilePath, changedFilePath, testTargetFilePath)

    assertThat(File(changedFilePath).readText().trim()).isEqualTo(
      "//example/to/a/file/FirstDemonstration.kt //example/to/a/file/SecondDemonstration.kt"
    )
  }

  @Test
  fun testUtility_validBase64_oneFile_writesCorrectTestTargetForFile() {
    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.addSourceAndTestFileWithContent(
      filename = "Source",
      testFilename = "SourceTest",
      sourceContent = "class Source()",
      testContent = "class SourceTest()",
      sourceSubpackage = "coverage/main/java/com/example",
      testSubpackage = "coverage/test/java/com/example"
    )

    val cacheNameFilePath = tempFolder.getNewTempFilePath("cache_name")
    val changedFilePath = tempFolder.getNewTempFilePath("changed_file_list")
    val testTargetFilePath = tempFolder.getNewTempFilePath("test_target_list")
    val base64String = computeBase64String(
      ChangedFilesBucket.newBuilder().apply {
        cacheBucketName = "example"
        addChangedFiles("coverage/main/java/com/example/Source.kt")
      }.build()
    )

    runScript(tempFolder.root.absolutePath, base64String, cacheNameFilePath, changedFilePath, testTargetFilePath)

    assertThat(File(testTargetFilePath).readText().trim()).isEqualTo(
      "//coverage/test/java/com/example:SourceTest"
    )
  }

  @Test
  fun testUtility_validBase64_multipleFiles_writesCorrectTestTargetsForFiles() {
    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.addSourceAndTestFileWithContent(
      filename = "Source1",
      testFilename = "Source1Test",
      sourceContent = "class Source1()",
      testContent = "class Source1Test()",
      sourceSubpackage = "coverage/main/java/com/example",
      testSubpackage = "coverage/test/java/com/example"
    )
    testBazelWorkspace.addSourceAndTestFileWithContent(
      filename = "Source2",
      testFilename = "Source2Test",
      sourceContent = "class Source2()",
      testContent = "class Source2Test()",
      sourceSubpackage = "coverage/main/java/com/example",
      testSubpackage = "coverage/test/java/com/example"
    )

    val cacheNameFilePath = tempFolder.getNewTempFilePath("cache_name")
    val changedFilePath = tempFolder.getNewTempFilePath("changed_file_list")
    val testTargetFilePath = tempFolder.getNewTempFilePath("test_target_list")
    val base64String = computeBase64String(
      ChangedFilesBucket.newBuilder().apply {
        cacheBucketName = "example"
        addChangedFiles("coverage/main/java/com/example/Source1.kt")
        addChangedFiles("coverage/main/java/com/example/Source2.kt")
      }.build()
    )

    runScript(tempFolder.root.absolutePath, base64String, cacheNameFilePath, changedFilePath, testTargetFilePath)

    assertThat(File(testTargetFilePath).readText().trim()).isEqualTo(
      "//coverage/test/java/com/example:Source1Test //coverage/test/java/com/example:Source2Test"
    )
  }

  private fun runScript(vararg args: String) {
    main(args.toList().toTypedArray())
  }

  private fun computeBase64String(changedFilesBucket: ChangedFilesBucket): String =
    changedFilesBucket.toCompressedBase64()

  /**
   * Returns the absolute file path of a new file that can be written under this [TemporaryFolder]
   * (but does not create the file).
   */
  private fun TemporaryFolder.getNewTempFilePath(name: String) =
    File(tempFolder.root, name).absolutePath

  private fun initializeCommandExecutorWithLongProcessWaitTime(): CommandExecutorImpl {
    return CommandExecutorImpl(
      scriptBgDispatcher, processTimeout = 5, processTimeoutUnit = TimeUnit.MINUTES
    )
  }
}
