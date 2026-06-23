package org.oppia.android.scripts.build

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import org.oppia.android.scripts.testing.TestGitRepository
import org.oppia.android.testing.assertThrows
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
      "<build_flavor:String> <major_app_version:Int> <minor_app_version:Int> " +
      "<application_relative_qualified_class:String> <enable_firebase_analytics:Boolean> " +
      "<enable_app_expiration:Boolean>"

  private val TEST_MANIFEST_FILE_NAME = "AndroidManifest.xml"
  private val TRANSFORMED_MANIFEST_FILE_NAME = "TransformedAndroidManifest.xml"
  private val TEST_MANIFEST_CONTENT_WITHOUT_MANIFEST =
    """
    <?xml version="1.0" encoding="utf-8"?>
    <otherelement xmlns:android="http://schemas.android.com/apk/res/android" />
    """.trimIndent()
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
    <!-- Comment that should be ignored. -->
    <manifest xmlns:android="http://schemas.android.com/apk/res/android"
      xmlns:tools="http://schemas.android.com/tools"
      package="org.oppia.android">
      <application
        android:name=".different.CustomApplication">
        <meta-data android:name="firebase_analytics_collection_deactivated" android:value="true" />
        <meta-data android:name="firebase_crashlytics_collection_enabled" android:value="false" />
        <meta-data android:name="automatic_app_expiration_enabled" android:value="false" />
        <meta-data android:name="expiration_date" android:value="2020-09-01" />
      </application>
    </manifest>
    """.trimIndent()

  private val BUILD_FLAVOR = "beta"
  private val MAJOR_VERSION = "1"
  private val MINOR_VERSION = "3"
  private val APPLICATION_RELATIVE_QUALIFIED_CLASS = ".example.CustomApplication"

  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

  private val scriptBgDispatcher by lazy { ScriptBackgroundCoroutineDispatcher() }
  private val commandExecutor by lazy { CommandExecutorImpl(scriptBgDispatcher) }
  private lateinit var testGitRepository: TestGitRepository

  @Before
  fun setUp() {
    testGitRepository = TestGitRepository(tempFolder, commandExecutor)
  }

  @After
  fun tearDown() {
    scriptBgDispatcher.close()
  }

  @Test
  fun testUtility_noArgs_failsWithUsageString() {
    initializeGitRepositoryWithHistory()

    val exception = assertThrows<IllegalStateException>() { runScript() }

    assertThat(exception).hasMessageThat().contains(USAGE_STRING)
  }

  @Test
  fun testUtility_oneArg_failsWithUsageString() {
    initializeGitRepositoryWithHistory()

    val exception = assertThrows<IllegalStateException>() {
      runScript(tempFolder.root.absolutePath)
    }

    assertThat(exception).hasMessageThat().contains(USAGE_STRING)
  }

  @Test
  fun testUtility_twoArgs_failsWithUsageString() {
    initializeGitRepositoryWithHistory()
    val manifestFile = tempFolder.newFile(TEST_MANIFEST_FILE_NAME)

    val exception = assertThrows<IllegalStateException>() {
      runScript(tempFolder.root.absolutePath, manifestFile.absolutePath)
    }

    assertThat(exception).hasMessageThat().contains(USAGE_STRING)
  }

  @Test
  fun testUtility_threeArgs_failsWithUsageString() {
    initializeGitRepositoryWithHistory()
    val manifestFile = tempFolder.newFile(TEST_MANIFEST_FILE_NAME)

    val exception = assertThrows<IllegalStateException>() {
      runScript(
        tempFolder.root.absolutePath,
        manifestFile.absolutePath,
        File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).absolutePath
      )
    }

    assertThat(exception).hasMessageThat().contains(USAGE_STRING)
  }

  @Test
  fun testUtility_fourArgs_failsWithUsageString() {
    initializeGitRepositoryWithHistory()
    val manifestFile = tempFolder.newFile(TEST_MANIFEST_FILE_NAME)

    val exception = assertThrows<IllegalStateException>() {
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
  fun testUtility_fiveArgs_failsWithUsageString() {
    initializeGitRepositoryWithHistory()
    val manifestFile = tempFolder.newFile(TEST_MANIFEST_FILE_NAME)

    val exception = assertThrows<IllegalStateException>() {
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
  fun testUtility_sixArgs_failsWithUsageString() {
    initializeGitRepositoryWithHistory()
    val manifestFile = tempFolder.newFile(TEST_MANIFEST_FILE_NAME)

    val exception = assertThrows<IllegalStateException>() {
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
  fun testUtility_sevenArgs_failsWithUsageString() {
    initializeGitRepositoryWithHistory()
    val manifestFile = tempFolder.newFile(TEST_MANIFEST_FILE_NAME)

    val exception = assertThrows<IllegalStateException>() {
      runScript(
        tempFolder.root.absolutePath,
        manifestFile.absolutePath,
        File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).absolutePath,
        BUILD_FLAVOR,
        MAJOR_VERSION,
        MINOR_VERSION,
        APPLICATION_RELATIVE_QUALIFIED_CLASS
      )
    }

    assertThat(exception).hasMessageThat().contains(USAGE_STRING)
  }

  @Test
  fun testUtility_eightArgs_failsWithUsageString() {
    initializeGitRepositoryWithHistory()
    val manifestFile = tempFolder.newFile(TEST_MANIFEST_FILE_NAME)

    val exception = assertThrows<IllegalStateException>() {
      runScript(
        tempFolder.root.absolutePath,
        manifestFile.absolutePath,
        File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).absolutePath,
        BUILD_FLAVOR,
        MAJOR_VERSION,
        MINOR_VERSION,
        APPLICATION_RELATIVE_QUALIFIED_CLASS,
        "false"
      )
    }

    assertThat(exception).hasMessageThat().contains(USAGE_STRING)
  }

  @Test
  fun testUtility_allArgs_nonIntMajorVersion_failsWithUsageString() {
    initializeGitRepositoryWithHistory()
    val manifestFile = tempFolder.newFile(TEST_MANIFEST_FILE_NAME)

    val exception = assertThrows<IllegalStateException>() {
      runScript(
        tempFolder.root.absolutePath,
        manifestFile.absolutePath,
        File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).absolutePath,
        BUILD_FLAVOR,
        "major_version",
        MINOR_VERSION,
        APPLICATION_RELATIVE_QUALIFIED_CLASS,
        "false",
        "false"
      )
    }

    assertThat(exception).hasMessageThat().contains(USAGE_STRING)
  }

  @Test
  fun testUtility_allArgs_nonIntMinorVersion_failsWithUsageString() {
    initializeGitRepositoryWithHistory()
    val manifestFile = tempFolder.newFile(TEST_MANIFEST_FILE_NAME)

    val exception = assertThrows<IllegalStateException>() {
      runScript(
        tempFolder.root.absolutePath,
        manifestFile.absolutePath,
        File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).absolutePath,
        BUILD_FLAVOR,
        MAJOR_VERSION,
        "minor_version",
        APPLICATION_RELATIVE_QUALIFIED_CLASS,
        "false",
        "false"
      )
    }

    assertThat(exception).hasMessageThat().contains(USAGE_STRING)
  }

  @Test
  fun testUtility_allArgs_invalidFlavor_failsWithError() {
    initializeGitRepositoryWithHistory()
    val manifestFile = tempFolder.newFile(TEST_MANIFEST_FILE_NAME)

    val exception = assertThrows<IllegalStateException>() {
      runScript(
        tempFolder.root.absolutePath,
        manifestFile.absolutePath,
        File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).absolutePath,
        "invalid_flavor",
        MAJOR_VERSION,
        MINOR_VERSION,
        APPLICATION_RELATIVE_QUALIFIED_CLASS,
        "false",
        "false"
      )
    }

    assertThat(exception).hasMessageThat().contains("Unknown build flavor: invalid_flavor")
  }

  @Test
  fun testUtility_allArgs_rootDoesNotExist_failsWithError() {
    initializeGitRepositoryWithHistory()
    val manifestFile = tempFolder.newFile(TEST_MANIFEST_FILE_NAME)

    val exception = assertThrows<IllegalStateException>() {
      runScript(
        "nowhere",
        manifestFile.absolutePath,
        File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).absolutePath,
        BUILD_FLAVOR,
        MAJOR_VERSION,
        MINOR_VERSION,
        APPLICATION_RELATIVE_QUALIFIED_CLASS,
        "false",
        "false"
      )
    }

    assertThat(exception).hasMessageThat().contains("File doesn't exist: nowhere")
  }

  @Test
  fun testUtility_allArgs_manifestDoesNotExist_failsWithError() {
    initializeGitRepositoryWithHistory()

    val exception = assertThrows<IllegalStateException>() {
      runScript(
        tempFolder.root.absolutePath,
        "fake_manifest_file",
        File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).absolutePath,
        BUILD_FLAVOR,
        MAJOR_VERSION,
        MINOR_VERSION,
        APPLICATION_RELATIVE_QUALIFIED_CLASS,
        "false",
        "false"
      )
    }

    assertThat(exception).hasMessageThat().contains("File doesn't exist: fake_manifest_file")
  }

  @Test
  fun testUtility_allArgsCorrect_manifestMissingManifestElement_throwsException() {
    initializeGitRepositoryWithHistory()
    val manifestFile = tempFolder.newFile(TEST_MANIFEST_FILE_NAME).apply {
      writeText(TEST_MANIFEST_CONTENT_WITHOUT_MANIFEST)
    }

    val exception = assertThrows<IllegalStateException>() {
      runScript(
        tempFolder.root.absolutePath,
        manifestFile.absolutePath,
        File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).absolutePath,
        BUILD_FLAVOR,
        MAJOR_VERSION,
        MINOR_VERSION,
        APPLICATION_RELATIVE_QUALIFIED_CLASS,
        "false",
        "false"
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Failed to find top-level 'manifest' element in manifest file")
  }

  @Test
  fun testUtility_allArgsCorrect_manifestMissingApplicationElement_throwsException() {
    initializeGitRepositoryWithHistory()
    val manifestFile = tempFolder.newFile(TEST_MANIFEST_FILE_NAME).apply {
      writeText(TEST_MANIFEST_CONTENT_WITHOUT_VERSIONS_AND_APPLICATION)
    }

    val exception = assertThrows<IllegalStateException>() {
      runScript(
        tempFolder.root.absolutePath,
        manifestFile.absolutePath,
        File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).absolutePath,
        BUILD_FLAVOR,
        MAJOR_VERSION,
        MINOR_VERSION,
        APPLICATION_RELATIVE_QUALIFIED_CLASS,
        "false",
        "false"
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Failed to find an 'application' element in manifest")
  }

  @Test
  fun testUtility_developBranch_gaFlavor_calculatesCorrectVersionCodeAndName() {
    initializeGitRepositoryWithHistory()
    val latestCommit = getMostRecentCommitOnCurrentBranch()

    runScript(
      tempFolder.root.absolutePath,
      tempFolder.newFile(TEST_MANIFEST_FILE_NAME).apply {
        writeText(TEST_MANIFEST_CONTENT_WITHOUT_VERSIONS)
      }.absolutePath,
      File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).absolutePath,
      "ga",
      MAJOR_VERSION,
      MINOR_VERSION,
      APPLICATION_RELATIVE_QUALIFIED_CLASS,
      "false",
      "false"
    )

    val transformedManifest = File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).readText()
    assertThat(transformedManifest).containsMatch("android:versionCode=\"300\"")
    assertThat(transformedManifest)
      .containsMatch("android:versionName=\"1\\.03-rc01-ga-${latestCommit.take(10)}\"")
  }

  @Test
  fun testUtility_developBranch_betaFlavor_calculatesCorrectVersionCodeAndName() {
    initializeGitRepositoryWithHistory()
    val latestCommit = getMostRecentCommitOnCurrentBranch()

    runScript(
      tempFolder.root.absolutePath,
      tempFolder.newFile(TEST_MANIFEST_FILE_NAME).apply {
        writeText(TEST_MANIFEST_CONTENT_WITHOUT_VERSIONS)
      }.absolutePath,
      File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).absolutePath,
      "beta",
      MAJOR_VERSION,
      MINOR_VERSION,
      APPLICATION_RELATIVE_QUALIFIED_CLASS,
      "false",
      "false"
    )

    val transformedManifest = File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).readText()
    assertThat(transformedManifest).containsMatch("android:versionCode=\"301\"")
    assertThat(transformedManifest)
      .containsMatch("android:versionName=\"1\\.03-rc01-beta-${latestCommit.take(10)}\"")
  }

  @Test
  fun testUtility_developBranch_alphaFlavor_calculatesCorrectVersionCodeAndName() {
    initializeGitRepositoryWithHistory()
    val latestCommit = getMostRecentCommitOnCurrentBranch()

    runScript(
      tempFolder.root.absolutePath,
      tempFolder.newFile(TEST_MANIFEST_FILE_NAME).apply {
        writeText(TEST_MANIFEST_CONTENT_WITHOUT_VERSIONS)
      }.absolutePath,
      File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).absolutePath,
      "alpha",
      MAJOR_VERSION,
      MINOR_VERSION,
      APPLICATION_RELATIVE_QUALIFIED_CLASS,
      "false",
      "false"
    )

    val transformedManifest = File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).readText()
    assertThat(transformedManifest).containsMatch("android:versionCode=\"302\"")
    assertThat(transformedManifest)
      .containsMatch("android:versionName=\"1\\.03-rc01-alpha-${latestCommit.take(10)}\"")
  }

  @Test
  fun testUtility_developBranch_devFlavor_calculatesCorrectVersionCodeAndName() {
    initializeGitRepositoryWithHistory()
    val latestCommit = getMostRecentCommitOnCurrentBranch()

    runScript(
      tempFolder.root.absolutePath,
      tempFolder.newFile(TEST_MANIFEST_FILE_NAME).apply {
        writeText(TEST_MANIFEST_CONTENT_WITHOUT_VERSIONS)
      }.absolutePath,
      File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).absolutePath,
      "dev",
      MAJOR_VERSION,
      MINOR_VERSION,
      APPLICATION_RELATIVE_QUALIFIED_CLASS,
      "false",
      "false"
    )

    val transformedManifest = File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).readText()
    assertThat(transformedManifest).containsMatch("android:versionCode=\"303\"")
    assertThat(transformedManifest)
      .containsMatch("android:versionName=\"1\\.03-rc01-dev-${latestCommit.take(10)}\"")
  }

  @Test
  fun testUtility_developBranchWithLocalCommits_pinsVersionCodeToOriginDevelop() {
    initializeGitRepositoryWithHistory()

    testGitRepository.commit(message = "Local commit 1", allowEmpty = true)
    testGitRepository.commit(message = "Local commit 2", allowEmpty = true)
    val latestCommit = getMostRecentCommitOnCurrentBranch()

    runScript(
      tempFolder.root.absolutePath,
      tempFolder.newFile(TEST_MANIFEST_FILE_NAME).apply {
        writeText(TEST_MANIFEST_CONTENT_WITHOUT_VERSIONS)
      }.absolutePath,
      File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).absolutePath,
      "dev",
      MAJOR_VERSION,
      MINOR_VERSION,
      APPLICATION_RELATIVE_QUALIFIED_CLASS,
      "false",
      "false"
    )

    val transformedManifest = File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).readText()

    // The version code should be the same as if there were no commits.
    assertThat(transformedManifest).containsMatch("android:versionCode=\"303\"")
    assertThat(transformedManifest)
      .containsMatch("android:versionName=\"1\\.03-rc01-dev-${latestCommit.take(10)}\"")
  }

  @Test
  fun testUtility_featureBranch_calculatesVersionBasedOnMergeBase() {
    initializeGitRepositoryWithHistory()
    testGitRepository.checkoutNewBranch("introduce-feature")
    testGitRepository.commit(message = "Feature commit 1", allowEmpty = true)
    testGitRepository.commit(message = "Feature commit 2", allowEmpty = true)
    val latestCommit = getMostRecentCommitOnCurrentBranch()

    runScript(
      tempFolder.root.absolutePath,
      tempFolder.newFile(TEST_MANIFEST_FILE_NAME).apply {
        writeText(TEST_MANIFEST_CONTENT_WITHOUT_VERSIONS)
      }.absolutePath,
      File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).absolutePath,
      "dev",
      MAJOR_VERSION,
      MINOR_VERSION,
      APPLICATION_RELATIVE_QUALIFIED_CLASS,
      "false",
      "false"
    )

    // Feature branches always behave as though they're a single develop commit.
    val transformedManifest = File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).readText()
    assertThat(transformedManifest).containsMatch("android:versionCode=\"303\"")
    assertThat(transformedManifest)
      .containsMatch("android:versionName=\"1\\.03-rc01-dev-${latestCommit.take(10)}\"")
  }

  @Test
  fun testUtility_releaseBranch_withNoCommits_calculatesCorrectVersionCodeAndName() {
    initializeGitRepositoryWithHistory()
    val developHash = getMostRecentCommitOnCurrentBranch()
    testGitRepository.checkoutNewBranch("release-v1.0")

    runScript(
      tempFolder.root.absolutePath,
      tempFolder.newFile(TEST_MANIFEST_FILE_NAME).apply {
        writeText(TEST_MANIFEST_CONTENT_WITHOUT_VERSIONS)
      }.absolutePath,
      File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).absolutePath,
      "beta",
      MAJOR_VERSION,
      MINOR_VERSION,
      APPLICATION_RELATIVE_QUALIFIED_CLASS,
      "false",
      "false"
    )

    // With no additional commits, the initial version matches the corresponding develop commit.
    val transformedManifest = File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).readText()
    assertThat(transformedManifest).containsMatch("android:versionCode=\"301\"")
    assertThat(transformedManifest)
      .containsMatch("android:versionName=\"1\\.03-rc01-beta-${developHash.take(10)}\"")
  }

  @Test
  fun testUtility_releaseBranch_withOneCommit_calculatesVersionCodeAndNameForReleaseCandidate() {
    initializeGitRepositoryWithHistory()
    testGitRepository.checkoutNewBranch("release-v1.0")
    testGitRepository.commit(message = "Release CP 1", allowEmpty = true)
    val latestCommit = getMostRecentCommitOnCurrentBranch()

    runScript(
      tempFolder.root.absolutePath,
      tempFolder.newFile(TEST_MANIFEST_FILE_NAME).apply {
        writeText(TEST_MANIFEST_CONTENT_WITHOUT_VERSIONS)
      }.absolutePath,
      File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).absolutePath,
      "beta",
      MAJOR_VERSION,
      MINOR_VERSION,
      APPLICATION_RELATIVE_QUALIFIED_CLASS,
      "false",
      "false"
    )

    // One commit means that the release branch has a second release candidate.
    val transformedManifest = File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).readText()
    assertThat(transformedManifest).containsMatch("android:versionCode=\"326\"")
    assertThat(transformedManifest)
      .containsMatch("android:versionName=\"1\\.03-rc02-beta-${latestCommit.take(10)}\"")
  }

  @Test
  fun testUtility_tooManyReleaseCandidates_throwsException() {
    initializeGitRepositoryWithHistory()
    testGitRepository.checkoutNewBranch("release-v1.0")
    // Commit 40 times to release branch since the maximum is 40 release candidates.
    for (i in 1..40) {
      testGitRepository.commit(message = "Release CP $i", allowEmpty = true)
    }

    val exception = assertThrows<IllegalStateException>() {
      runScript(
        tempFolder.root.absolutePath,
        tempFolder.newFile(TEST_MANIFEST_FILE_NAME).apply {
          writeText(TEST_MANIFEST_CONTENT_WITHOUT_VERSIONS)
        }.absolutePath,
        File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).absolutePath,
        "beta",
        MAJOR_VERSION,
        MINOR_VERSION,
        APPLICATION_RELATIVE_QUALIFIED_CLASS,
        "false",
        "false"
      )
    }

    assertThat(exception).hasMessageThat().contains("Too many release candidates: 41. Max is 40.")
  }

  @Test
  fun testUtility_enableAnalyticsFalse_analyticsDisabledInManifest() {
    initializeGitRepositoryWithHistory()
    runScript(
      tempFolder.root.absolutePath,
      tempFolder.newFile(TEST_MANIFEST_FILE_NAME).apply {
        writeText(TEST_MANIFEST_CONTENT_WITHOUT_VERSIONS)
      }.absolutePath,
      File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).absolutePath,
      BUILD_FLAVOR,
      MAJOR_VERSION,
      MINOR_VERSION,
      APPLICATION_RELATIVE_QUALIFIED_CLASS,
      "false",
      "false"
    )

    val transformedManifest = File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).readText()
    assertThat(transformedManifest).containsMatch(
      "<meta-data android:name=\"firebase_analytics_collection_deactivated\" android:value=\"true\""
    )
    assertThat(transformedManifest).containsMatch(
      "<meta-data android:name=\"firebase_crashlytics_collection_enabled\" android:value=\"false\""
    )
  }

  @Test
  fun testUtility_enableAnalyticsTrue_analyticsEnabledInManifest() {
    initializeGitRepositoryWithHistory()
    runScript(
      tempFolder.root.absolutePath,
      tempFolder.newFile(TEST_MANIFEST_FILE_NAME).apply {
        writeText(TEST_MANIFEST_CONTENT_WITHOUT_VERSIONS)
      }.absolutePath,
      File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).absolutePath,
      BUILD_FLAVOR,
      MAJOR_VERSION,
      MINOR_VERSION,
      APPLICATION_RELATIVE_QUALIFIED_CLASS,
      "true",
      "false"
    )

    val transformedManifest = File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).readText()
    assertThat(transformedManifest).containsMatch(
      "<meta-data android:name=\"firebase_analytics_collection_deactivated\"" +
        " android:value=\"false\""
    )
    assertThat(transformedManifest).containsMatch(
      "<meta-data android:name=\"firebase_crashlytics_collection_enabled\" android:value=\"true\""
    )
  }

  @Test
  fun testUtility_enableExpirationFalse_expirationDisabledInManifest() {
    initializeGitRepositoryWithHistory()
    runScript(
      tempFolder.root.absolutePath,
      tempFolder.newFile(TEST_MANIFEST_FILE_NAME).apply {
        writeText(TEST_MANIFEST_CONTENT_WITHOUT_VERSIONS)
      }.absolutePath,
      File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).absolutePath,
      BUILD_FLAVOR,
      MAJOR_VERSION,
      MINOR_VERSION,
      APPLICATION_RELATIVE_QUALIFIED_CLASS,
      "false",
      "false"
    )

    val transformedManifest = File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).readText()
    assertThat(transformedManifest).containsMatch(
      "<meta-data android:name=\"automatic_app_expiration_enabled\" android:value=\"false\""
    )
  }

  @Test
  fun testUtility_enableExpirationTrue_expirationEnabledAndDefinedInManifest() {
    initializeGitRepositoryWithHistory()
    runScript(
      tempFolder.root.absolutePath,
      tempFolder.newFile(TEST_MANIFEST_FILE_NAME).apply {
        writeText(TEST_MANIFEST_CONTENT_WITHOUT_VERSIONS)
      }.absolutePath,
      File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).absolutePath,
      BUILD_FLAVOR,
      MAJOR_VERSION,
      MINOR_VERSION,
      APPLICATION_RELATIVE_QUALIFIED_CLASS,
      "false",
      "true"
    )

    val transformedManifest = File(tempFolder.root, TRANSFORMED_MANIFEST_FILE_NAME).readText()
    assertThat(transformedManifest).containsMatch(
      "<meta-data android:name=\"automatic_app_expiration_enabled\" android:value=\"true\""
    )

    val expectedExpirationDate = LocalDate.now().plusMonths(12)
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val expectedDateString = expectedExpirationDate.format(formatter)
    assertThat(transformedManifest).containsMatch(
      "<meta-data android:name=\"expiration_date\" android:value=\"$expectedDateString\""
    )
  }

  /** Runs the transform_android_manifest utility. */
  private fun runScript(vararg args: String) {
    main(args.toList().toTypedArray())
  }

  private fun initializeGitRepositoryWithHistory() {
    // Initialize the git repository with a base 'develop' branch & an initial empty commit (so that
    // there's a HEAD commit).
    testGitRepository.init()
    testGitRepository.setUser(email = "test@oppia.org", name = "Test User")
    testGitRepository.initializeHistoricalCommits(commitCount = 2280)
    testGitRepository.createRemoteBranchRef("origin/develop")
  }

  private fun getMostRecentCommitOnCurrentBranch(): String {
    // See https://stackoverflow.com/a/949391 for a reference to validate that this is correct.
    return commandExecutor.executeCommand(
      tempFolder.root, "git", "rev-parse", "HEAD"
    ).output.last()
  }
}
