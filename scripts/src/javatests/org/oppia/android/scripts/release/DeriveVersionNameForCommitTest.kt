package org.oppia.android.scripts.release

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import org.oppia.android.testing.assertThrows

/** Tests for [DeriveVersionNameForCommit]. */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class DeriveVersionNameForCommitTest {
  @field:[Rule JvmField]
  var tempFolder = TemporaryFolder()

  private val scriptBgDispatcher by lazy { ScriptBackgroundCoroutineDispatcher() }
  private val commandExecutor by lazy { CommandExecutorImpl(scriptBgDispatcher) }
  private lateinit var deriver: DeriveVersionNameForCommit

  @Before
  fun setUp() {
    deriver = DeriveVersionNameForCommit(
      repoRoot = tempFolder.root.absolutePath,
      scriptBgDispatcher = scriptBgDispatcher
    )
    initGitRepo()
  }

  @After
  fun tearDown() {
    scriptBgDispatcher.close()
  }

  @Test
  fun testComputeBinaryName_validCommitBetaFlavor_returnsCorrectName() {
    val sha = createAndCommitVersionBzl(major = 0, minor = 17)
    val shortSha = getShortSha(sha)

    val result = deriver.computeBinaryName(sha, "beta", "01")

    assertThat(result).isEqualTo("oppia-android-0.17-rc01-beta-$shortSha.aab")
  }

  @Test
  fun testComputeBinaryName_validCommitAlphaFlavor_includesAlphaInName() {
    val sha = createAndCommitVersionBzl(major = 0, minor = 17)
    val shortSha = getShortSha(sha)

    val result = deriver.computeBinaryName(sha, "alpha", "01")

    assertThat(result).isEqualTo("oppia-android-0.17-rc01-alpha-$shortSha.aab")
  }

  @Test
  fun testComputeBinaryName_validCommitGaFlavor_includesGaInName() {
    val sha = createAndCommitVersionBzl(major = 0, minor = 17)
    val shortSha = getShortSha(sha)

    val result = deriver.computeBinaryName(sha, "ga", "01")

    assertThat(result).isEqualTo("oppia-android-0.17-rc01-ga-$shortSha.aab")
  }

  @Test
  fun testComputeBinaryName_differentVersion_reflectsCorrectVersion() {
    val sha = createAndCommitVersionBzl(major = 1, minor = 5)
    val shortSha = getShortSha(sha)

    val result = deriver.computeBinaryName(sha, "beta", "01")

    assertThat(result).isEqualTo("oppia-android-1.5-rc01-beta-$shortSha.aab")
  }

  @Test
  fun testComputeBinaryName_singleDigitRcNumber_isPaddedToTwoDigits() {
    val sha = createAndCommitVersionBzl(major = 0, minor = 17)
    val shortSha = getShortSha(sha)

    val result = deriver.computeBinaryName(sha, "beta", "1")

    assertThat(result).isEqualTo("oppia-android-0.17-rc01-beta-$shortSha.aab")
  }

  @Test
  fun testComputeBinaryName_twoDigitRcNumber_isNotPadded() {
    val sha = createAndCommitVersionBzl(major = 0, minor = 17)
    val shortSha = getShortSha(sha)

    val result = deriver.computeBinaryName(sha, "beta", "10")

    assertThat(result).isEqualTo("oppia-android-0.17-rc10-beta-$shortSha.aab")
  }

  @Test
  fun testComputeBinaryName_branchRef_resolvesCorrectly() {
    createAndCommitVersionBzl(major = 0, minor = 17)
    runGit("checkout", "-b", "release-0.17")
    val shortSha = getShortSha("release-0.17")

    val result = deriver.computeBinaryName("release-0.17", "beta", "01")

    assertThat(result).isEqualTo("oppia-android-0.17-rc01-beta-$shortSha.aab")
  }

  @Test
  fun testComputeBinaryName_nonExistentCommit_throwsInvalidCommitException() {
    createAndCommitVersionBzl(major = 0, minor = 17)

    val exception = assertThrows<IllegalStateException> {
      deriver.computeBinaryName("nonexistentcommitsha123", "beta", "01")
    }

    assertThat(exception).hasMessageThat().contains("InvalidCommitException")
    assertThat(exception).hasMessageThat().contains("Commit does not exist")
  }

  @Test
  fun testComputeBinaryName_versionBzlMissingAtCommit_throwsVersionParseException() {
    val readmeFile = tempFolder.newFile("README.md")
    readmeFile.writeText("test")
    runGit("add", "README.md")
    runGit("commit", "-m", "Initial commit without version.bzl")
    val sha = getHeadSha()

    val exception = assertThrows<IllegalStateException> {
      deriver.computeBinaryName(sha, "beta", "01")
    }

    assertThat(exception).hasMessageThat().contains("VersionParseException")
    assertThat(exception).hasMessageThat().contains("version.bzl does not exist at commit")
  }

  @Test
  fun testComputeBinaryName_malformedVersionBzl_throwsVersionParseException() {
    val versionBzlFile = tempFolder.newFile("version.bzl")
    versionBzlFile.writeText("NOT_A_VERSION_FILE = 0")
    runGit("add", "version.bzl")
    runGit("commit", "-m", "Add malformed version.bzl")
    val sha = getHeadSha()

    val exception = assertThrows<IllegalStateException> {
      deriver.computeBinaryName(sha, "beta", "01")
    }

    assertThat(exception).hasMessageThat().contains("VersionParseException")
    assertThat(exception).hasMessageThat()
      .contains("Could not parse MAJOR_VERSION or MINOR_VERSION")
  }

  @Test
  fun testComputeBinaryName_missingMinorVersion_throwsVersionParseException() {
    val versionBzlFile = tempFolder.newFile("version.bzl")
    versionBzlFile.writeText("MAJOR_VERSION = 0")
    runGit("add", "version.bzl")
    runGit("commit", "-m", "Add version.bzl without MINOR_VERSION")
    val sha = getHeadSha()

    val exception = assertThrows<IllegalStateException> {
      deriver.computeBinaryName(sha, "beta", "01")
    }

    assertThat(exception).hasMessageThat().contains("VersionParseException")
  }

  @Test
  fun testMain_wrongNumberOfArgs_throwsIllegalArgumentException() {
    val exception = assertThrows<IllegalArgumentException> {
      main("only_one_arg")
    }

    assertThat(exception).hasMessageThat().contains(
      "Usage: bazel run //scripts:derive_version_name_for_commit"
    )
  }

  // region helpers

  private fun initGitRepo() {
    runGit("init")
    runGit("config", "user.email", "test@test.com")
    runGit("config", "user.name", "Test User")
  }

  private fun createAndCommitVersionBzl(major: Int, minor: Int): String {
    val versionBzlFile = tempFolder.newFile("version.bzl")
    versionBzlFile.writeText(
      """
      MAJOR_VERSION = $major
      MINOR_VERSION = $minor
      """.trimIndent()
    )
    runGit("add", "version.bzl")
    runGit("commit", "-m", "Add version.bzl for $major.$minor")
    return getHeadSha()
  }

  private fun getHeadSha(): String =
    commandExecutor.executeCommand(tempFolder.root, "git", "rev-parse", "HEAD")
      .output.first().trim()

  private fun getShortSha(ref: String): String =
    commandExecutor.executeCommand(tempFolder.root, "git", "rev-parse", "--short=10", ref)
      .output.first().trim()

  private fun runGit(vararg args: String) {
    commandExecutor.executeCommand(tempFolder.root, "git", *args)
  }

  // endregion
}
