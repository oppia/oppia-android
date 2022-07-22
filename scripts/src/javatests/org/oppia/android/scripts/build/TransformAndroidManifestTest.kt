package org.oppia.android.scripts.build

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.testing.TestGitRepository
import org.oppia.android.testing.assertThrows
import java.io.File

/**
 * Tests for the transform_android_manifest utility.
 *
 * Note that this test suite makes use of real Git utilities on the local system. As a result, these
 * tests could be affected by unexpected environment issues (such as inconsistencies across
 * dependency versions or changes in behavior across different filesystems).
 */
// PrivatePropertyName: it's valid to have private vals in constant case if they're true constants.
// FunctionName: test names are conventionally named with underscores.
@Suppress("PrivatePropertyName", "FunctionName")
class TransformAndroidManifestTest {
  private val USAGE_STRING =
    "Usage: bazel run //scripts:transform_android_manifest -- </absolute/path/to/repo/root:Path> " +
      "</absolute/path/to/input/AndroidManifest.xml:Path> " +
      "</absolute/path/to/output/AndroidManifest.xml:Path> " +
      "<build_flavor:String> <major_app_version:Int> <minor_app_version:Int> <version_code:Int> " +
      "<application_relative_qualified_class:String> <base_develop_branch_reference:String>"

  private val TEST_MANIFEST_FILE_NAME = "AndroidManifest.xml"
  private val TRANSFORMED_MANIFEST_FILE_NAME = "TransformedAndroidManifest.xml"
  private val TEST_MANIFEST_CONTENT_WITHOUT_VERSIONS_AND_APPLICATION =
    """
    <?xml version="1.0" encoding="utf-8"?>
    <manifest xmlns:android="http://schemas.android.com/apk/res/android"
      xmlns:tools="http://schemas.android.com/tools"
      package="org.oppia.android" />
    """.trimIndent()
  private val TEST_MANIFEST_CONTENT_WITHOUT_VERSIONS =
    """
    <?xml version="1.0" encoding="utf-8"?>
    <manifest xmlns:android="http://schemas.android.com/apk/res/android"
      xmlns:tools="http://schemas.android.com/tools"
      package="org.oppia.android">
      <application
        android:name=".different.CustomApplication" />
    </manifest>
    """.trimIndent()

  private val BUILD_FLAVOR = "beta"
  private val MAJOR_VERSION = "1"
  private val MINOR_VERSION = "3"
  private val VERSION_CODE = "23"
  private val APPLICATION_RELATIVE_QUALIFIED_CLASS = ".example.CustomApplication"

  @Rule
  @JvmField
  var tempFolder = TemporaryFolder()

  private lateinit var testGitRepository: TestGitRepository

  @Before
  fun setUp() {
    testGitRepository = TestGitRepository(tempFolder, CommandExecutorImpl())
  }

  @Test
  fun testUtility_noArgs_failsWithUsageString() {
    initializeEmptyGitRepository()

    val exception = assertThrows(IllegalStateException::class) { runScript() }

    assertThat(exception).hasMessageThat().contains(USAGE_STRING)
  }

  @Test
  fun testUtility_oneArg_failsWithUsageString() {
    initializeEmptyGitRepository()

    val exception = assertThrows(IllegalStateException::class) {
      runScript(tempFolder.root.absolutePath)
    }

    assertThat(exception).hasMessageThat().contains(USAGE_STRING)
  }

  @Test
  fun testUtility_twoArgs_failsWithUsageString() {
    initializeEmptyGitRepository()
    val manifestFile = tempFolder.newFile(TEST_MANIFEST_FILE_NAME)

    val exception = assertThrows(IllegalStateException::class) {
      runScript(tempFolder.root.absolutePath, manifestFile.absolutePath)
    }

    assertThat(exception).hasMessageThat().contains(USAGE_STRING)
  }

  @Test
  fun testUtility_threeAgs_failsWithUsageString() {
    initializeEmptyGitRepository()
    val manifestFile = tempFolder.newFile(TEST_MANIFEST_FILE_NAME)

    val exception = assertThrows(IllegalStateException::class) {
      runScript(
        tempFolder.root.absolutePath,
        manifestFile.absolutePath,
        File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).absolutePath
      )
    }

    assertThat(exception).hasMessageThat().contains(USAGE_STRING)
  }

  @Test
  fun testUtility_fourAgs_failsWithUsageString() {
    initializeEmptyGitRepository()
    val manifestFile = tempFolder.newFile(TEST_MANIFEST_FILE_NAME)

    val exception = assertThrows(IllegalStateException::class) {
      runScript(
        tempFolder.root.absolutePath,
        manifestFile.absolutePath,
        File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).absolutePath,
        BUILD_FLAVOR
      )
    }

    assertThat(exception).hasMessageThat().contains(USAGE_STRING)
  }

  @Test
  fun testUtility_fiveAgs_failsWithUsageString() {
    initializeEmptyGitRepository()
    val manifestFile = tempFolder.newFile(TEST_MANIFEST_FILE_NAME)

    val exception = assertThrows(IllegalStateException::class) {
      runScript(
        tempFolder.root.absolutePath,
        manifestFile.absolutePath,
        File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).absolutePath,
        BUILD_FLAVOR,
        MAJOR_VERSION
      )
    }

    assertThat(exception).hasMessageThat().contains(USAGE_STRING)
  }

  @Test
  fun testUtility_sixAgs_failsWithUsageString() {
    initializeEmptyGitRepository()
    val manifestFile = tempFolder.newFile(TEST_MANIFEST_FILE_NAME)

    val exception = assertThrows(IllegalStateException::class) {
      runScript(
        tempFolder.root.absolutePath,
        manifestFile.absolutePath,
        File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).absolutePath,
        BUILD_FLAVOR,
        MAJOR_VERSION,
        MINOR_VERSION
      )
    }

    assertThat(exception).hasMessageThat().contains(USAGE_STRING)
  }

  @Test
  fun testUtility_sevenAgs_failsWithUsageString() {
    initializeEmptyGitRepository()
    val manifestFile = tempFolder.newFile(TEST_MANIFEST_FILE_NAME)

    val exception = assertThrows(IllegalStateException::class) {
      runScript(
        tempFolder.root.absolutePath,
        manifestFile.absolutePath,
        File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).absolutePath,
        BUILD_FLAVOR,
        MAJOR_VERSION,
        MINOR_VERSION,
        VERSION_CODE
      )
    }

    assertThat(exception).hasMessageThat().contains(USAGE_STRING)
  }

  @Test
  fun testUtility_eightAgs_failsWithUsageString() {
    initializeEmptyGitRepository()
    val manifestFile = tempFolder.newFile(TEST_MANIFEST_FILE_NAME)

    val exception = assertThrows(IllegalStateException::class) {
      runScript(
        tempFolder.root.absolutePath,
        manifestFile.absolutePath,
        File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).absolutePath,
        BUILD_FLAVOR,
        MAJOR_VERSION,
        MINOR_VERSION,
        VERSION_CODE,
        APPLICATION_RELATIVE_QUALIFIED_CLASS
      )
    }

    assertThat(exception).hasMessageThat().contains(USAGE_STRING)
  }

  @Test
  fun testUtility_allArgs_nonIntMajorVersion_failsWithUsageString() {
    initializeEmptyGitRepository()
    val manifestFile = tempFolder.newFile(TEST_MANIFEST_FILE_NAME)

    val exception = assertThrows(IllegalStateException::class) {
      runScript(
        tempFolder.root.absolutePath,
        manifestFile.absolutePath,
        File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).absolutePath,
        BUILD_FLAVOR,
        "major_version",
        MINOR_VERSION,
        VERSION_CODE,
        APPLICATION_RELATIVE_QUALIFIED_CLASS,
        "develop"
      )
    }

    assertThat(exception).hasMessageThat().contains(USAGE_STRING)
  }

  @Test
  fun testUtility_allArgs_nonIntMinorVersion_failsWithUsageString() {
    initializeEmptyGitRepository()
    val manifestFile = tempFolder.newFile(TEST_MANIFEST_FILE_NAME)

    val exception = assertThrows(IllegalStateException::class) {
      runScript(
        tempFolder.root.absolutePath,
        manifestFile.absolutePath,
        File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).absolutePath,
        BUILD_FLAVOR,
        MAJOR_VERSION,
        "minor_version",
        VERSION_CODE,
        APPLICATION_RELATIVE_QUALIFIED_CLASS,
        "develop"
      )
    }

    assertThat(exception).hasMessageThat().contains(USAGE_STRING)
  }

  @Test
  fun testUtility_allArgs_nonIntVersionCode_failsWithUsageString() {
    initializeEmptyGitRepository()
    val manifestFile = tempFolder.newFile(TEST_MANIFEST_FILE_NAME)

    val exception = assertThrows(IllegalStateException::class) {
      runScript(
        tempFolder.root.absolutePath,
        manifestFile.absolutePath,
        File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).absolutePath,
        BUILD_FLAVOR,
        MAJOR_VERSION,
        MINOR_VERSION,
        "version_code",
        APPLICATION_RELATIVE_QUALIFIED_CLASS,
        "develop"
      )
    }

    assertThat(exception).hasMessageThat().contains(USAGE_STRING)
  }

  @Test
  fun testUtility_allArgs_rootDoesNotExist_failsWithError() {
    initializeEmptyGitRepository()
    val manifestFile = tempFolder.newFile(TEST_MANIFEST_FILE_NAME)

    val exception = assertThrows(IllegalStateException::class) {
      runScript(
        "nowhere",
        manifestFile.absolutePath,
        File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).absolutePath,
        BUILD_FLAVOR,
        MAJOR_VERSION,
        MINOR_VERSION,
        VERSION_CODE,
        APPLICATION_RELATIVE_QUALIFIED_CLASS,
        "develop"
      )
    }

    assertThat(exception).hasMessageThat().contains("File doesn't exist: nowhere")
  }

  @Test
  fun testUtility_allArgs_manifestDoesNotExist_failsWithError() {
    initializeEmptyGitRepository()

    val exception = assertThrows(IllegalStateException::class) {
      runScript(
        tempFolder.root.absolutePath,
        "fake_manifest_file",
        File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).absolutePath,
        BUILD_FLAVOR,
        MAJOR_VERSION,
        MINOR_VERSION,
        VERSION_CODE,
        APPLICATION_RELATIVE_QUALIFIED_CLASS,
        "develop"
      )
    }

    assertThat(exception).hasMessageThat().contains("File doesn't exist: fake_manifest_file")
  }

  @Test
  fun testUtility_allArgsCorrect_manifestMissingApplicationTag_throwsException() {
    initializeEmptyGitRepository()
    val manifestFile = tempFolder.newFile(TEST_MANIFEST_FILE_NAME).apply {
      writeText(TEST_MANIFEST_CONTENT_WITHOUT_VERSIONS_AND_APPLICATION)
    }

    val exception = assertThrows(IllegalStateException::class) {
      runScript(
        tempFolder.root.absolutePath,
        manifestFile.absolutePath,
        File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).absolutePath,
        BUILD_FLAVOR,
        MAJOR_VERSION,
        MINOR_VERSION,
        VERSION_CODE,
        APPLICATION_RELATIVE_QUALIFIED_CLASS,
        "develop"
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Failed to find an 'application' tag in manifest")
  }

  @Test
  fun testUtility_allArgsCorrect_outputsNewManifestWithVersionNameAndCodeAndCustomApplication() {
    initializeEmptyGitRepository()
    val manifestFile = tempFolder.newFile(TEST_MANIFEST_FILE_NAME).apply {
      writeText(TEST_MANIFEST_CONTENT_WITHOUT_VERSIONS)
    }

    runScript(
      tempFolder.root.absolutePath,
      manifestFile.absolutePath,
      File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).absolutePath,
      BUILD_FLAVOR,
      MAJOR_VERSION,
      MINOR_VERSION,
      VERSION_CODE,
      APPLICATION_RELATIVE_QUALIFIED_CLASS,
      "develop"
    )

    val transformedManifest = File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).readText()
    assertThat(transformedManifest).containsMatch("android:versionCode=\"$VERSION_CODE\"")
    assertThat(transformedManifest)
      .containsMatch(
        "android:versionName=\"$MAJOR_VERSION\\.$MINOR_VERSION" +
          "-$BUILD_FLAVOR-[a-f0-9]{10}\""
      )
    assertThat(transformedManifest)
      .containsMatch("<application android:name=\"$APPLICATION_RELATIVE_QUALIFIED_CLASS\"")
  }

  /** Runs the transform_android_manifest utility. */
  private fun runScript(vararg args: String) {
    main(args.toList().toTypedArray())
  }

  private fun initializeEmptyGitRepository() {
    // Initialize the git repository with a base 'develop' branch & an initial empty commit (so that
    // there's a HEAD commit).
    testGitRepository.init()
    testGitRepository.setUser(email = "test@oppia.org", name = "Test User")
    testGitRepository.checkoutNewBranch("develop")
    testGitRepository.commit(message = "Initial commit.", allowEmpty = true)
  }
}
