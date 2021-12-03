package org.oppia.android.scripts.ci

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.common.ProtoStringEncoder.Companion.toCompressedBase64
import org.oppia.android.scripts.proto.AffectedTestsBucket
import org.oppia.android.testing.assertThrows
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream
import java.io.PrintStream

/** Tests for the retrieve_affected_tests utility. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
class RetrieveAffectedTestsTest {
  @Rule
  @JvmField
  var tempFolder = TemporaryFolder()

  private lateinit var pendingOutputStream: ByteArrayOutputStream
  private lateinit var originalStandardOutputStream: OutputStream

  @Before
  fun setUp() {
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
  }

  @Test
  fun testUtility_noArguments_printsUsageStringAndExits() {
    val exception = assertThrows(SecurityException::class) { runScript() }

    // Bazel catches the System.exit() call and throws a SecurityException. This is a bit hacky way
    // to verify that System.exit() is called, but it's helpful.
    assertThat(exception).hasMessageThat().contains("System.exit()")
    assertThat(pendingOutputStream.toString()).contains("Usage:")
  }

  @Test
  fun testUtility_oneArgument_printsUsageStringAndExits() {
    val exception = assertThrows(SecurityException::class) { runScript("arg1") }

    // Bazel catches the System.exit() call and throws a SecurityException. This is a bit hacky way
    // to verify that System.exit() is called, but it's helpful.
    assertThat(exception).hasMessageThat().contains("System.exit()")
    assertThat(pendingOutputStream.toString()).contains("Usage:")
  }

  @Test
  fun testUtility_twoArguments_printsUsageStringAndExits() {
    val exception = assertThrows(SecurityException::class) { runScript("arg1", "arg2") }

    // Bazel catches the System.exit() call and throws a SecurityException. This is a bit hacky way
    // to verify that System.exit() is called, but it's helpful.
    assertThat(exception).hasMessageThat().contains("System.exit()")
    assertThat(pendingOutputStream.toString()).contains("Usage:")
  }

  @Test
  fun testUtility_invalidBase64_throwsException() {
    assertThrows(IllegalArgumentException::class) { runScript("badbase64", "file1", "file2") }
  }

  @Test
  fun testUtility_validBase64_oneTest_writesCacheNameFile() {
    val cacheNameFilePath = tempFolder.getNewTempFilePath("cache_name")
    val testTargetFilePath = tempFolder.getNewTempFilePath("test_target_list")
    val base64String = computeBase64String(
      AffectedTestsBucket.newBuilder().apply {
        cacheBucketName = "example"
        addAffectedTestTargets("//example/to/a/test:DemonstrationTest")
      }.build()
    )

    runScript(base64String, cacheNameFilePath, testTargetFilePath)

    assertThat(File(cacheNameFilePath).readText().trim()).isEqualTo("example")
  }

  @Test
  fun testUtility_validBase64_oneTest_writesTestTargetFileWithCorrectTarget() {
    val cacheNameFilePath = tempFolder.getNewTempFilePath("cache_name")
    val testTargetFilePath = tempFolder.getNewTempFilePath("test_target_list")
    val base64String = computeBase64String(
      AffectedTestsBucket.newBuilder().apply {
        cacheBucketName = "example"
        addAffectedTestTargets("//example/to/a/test:DemonstrationTest")
      }.build()
    )

    runScript(base64String, cacheNameFilePath, testTargetFilePath)

    assertThat(File(testTargetFilePath).readText().trim()).isEqualTo(
      "//example/to/a/test:DemonstrationTest"
    )
  }

  @Test
  fun testUtility_validBase64_multipleTests_writesTestTargetFileWithCorrectTargets() {
    val cacheNameFilePath = tempFolder.getNewTempFilePath("cache_name")
    val testTargetFilePath = tempFolder.getNewTempFilePath("test_target_list")
    val base64String = computeBase64String(
      AffectedTestsBucket.newBuilder().apply {
        cacheBucketName = "example"
        addAffectedTestTargets("//example/to/a/test:FirstDemonstrationTest")
        addAffectedTestTargets("//example/to/b/test:SecondDemonstrationTest")
      }.build()
    )

    runScript(base64String, cacheNameFilePath, testTargetFilePath)

    assertThat(File(testTargetFilePath).readText().trim()).isEqualTo(
      "//example/to/a/test:FirstDemonstrationTest //example/to/b/test:SecondDemonstrationTest"
    )
  }

  private fun runScript(vararg args: String) {
    main(args.toList().toTypedArray())
  }

  private fun computeBase64String(affectedTestsBucket: AffectedTestsBucket): String =
    affectedTestsBucket.toCompressedBase64()

  /**
   * Returns the absolute file path of a new file that can be written under this [TemporaryFolder]
   * (but does not create the file).
   */
  private fun TemporaryFolder.getNewTempFilePath(name: String) =
    File(tempFolder.root, name).absolutePath
}
