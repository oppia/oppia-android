package org.oppia.android.scripts.ci

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.GitClient
import org.oppia.android.scripts.common.ProtoStringEncoder.Companion.mergeFromCompressedBase64
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import org.oppia.android.scripts.proto.ChangedFilesBucket
import org.oppia.android.scripts.testing.TestBazelWorkspace
import org.oppia.android.scripts.testing.TestGitRepository
import org.oppia.android.testing.assertThrows
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream
import java.io.PrintStream
import java.util.concurrent.TimeUnit

/**
 * Tests for the compute_affected_tests utility.
 */
class ComputeChangedFilesTest {
  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

  private val scriptBgDispatcher by lazy { ScriptBackgroundCoroutineDispatcher() }

  private lateinit var commandExecutor: CommandExecutor
  private lateinit var testBazelWorkspace: TestBazelWorkspace
  private lateinit var testGitRepository: TestGitRepository
  private lateinit var pendingOutputStream: ByteArrayOutputStream
  private lateinit var originalStandardOutputStream: OutputStream

  @Before
  fun setUp() {
    commandExecutor = initializeCommandExecutorWithLongProcessWaitTime()
    testBazelWorkspace = TestBazelWorkspace(tempFolder)
    testGitRepository = TestGitRepository(tempFolder, commandExecutor)

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
    println(testGitRepository.status(checkForGitRepository = false))

    scriptBgDispatcher.close()
  }

  @Test
  fun testUtility_noArguments_printsUsageStringAndExits() {
    val exception = assertThrows<SecurityException>() { main(arrayOf()) }

    // Bazel catches the System.exit() call and throws a SecurityException. This is a bit hacky way
    // to verify that System.exit() is called, but it's helpful.
    assertThat(exception).hasMessageThat().contains("System.exit()")
    assertThat(pendingOutputStream.toString()).contains("Usage:")
  }

  @Test
  fun testUtility_invalidArguments_printsUsageStringAndExits() {
    for (argCount in 0..3) {
      val args = Array(argCount) { "arg${it + 1}" }
      val exception = assertThrows<SecurityException> { main(arrayOf(*args)) }

      // Bazel catches the System.exit() call and throws a SecurityException.
      assertThat(exception).hasMessageThat().contains("System.exit()")
      assertThat(pendingOutputStream.toString()).contains("Usage:")
    }
  }

  @Test
  fun testUtility_directoryRootDoesNotExist_throwsException() {
    val exception = assertThrows<IllegalStateException>() {
      main(arrayOf("fake", "alsofake", "andstillfake", "compute_all_files=false"))
    }

    assertThat(exception).hasMessageThat().contains("Expected 'fake' to be a directory")
  }

  @Test
  fun testUtility_invalid_lastArgument_throwsException() {
    val exception = assertThrows<IllegalStateException>() {
      main(arrayOf("fake", "alsofake", "andstillfake", "compute_all_filess=false"))
    }

    assertThat(exception).hasMessageThat()
      .contains("Expected last argument to start with 'compute_all_files='")
  }

  @Test
  fun testUtility_invalid_lastArgumentValue_throwsException() {
    val exception = assertThrows<IllegalStateException>() {
      main(arrayOf("fake", "alsofake", "andstillfake", "compute_all_files=blah"))
    }

    assertThat(exception).hasMessageThat()
      .contains("Expected last argument to have 'true' or 'false' passed to it, not: 'blah'")
  }

  @Test
  fun testUtility_emptyDirectory_throwsException() {
    val exception = assertThrows<IllegalStateException>() { runScript(currentHeadHash = "ad") }

    assertThat(exception).hasMessageThat().contains("run from the workspace's root directory")
  }

  @Test
  fun testUtility_emptyWorkspace_returnsNoTargets() {
    // Need to be on a feature branch since the develop branch expects there to be files.
    initializeEmptyGitRepository()
    switchToFeatureBranch()
    createEmptyWorkspace()

    val reportedFiles = runScript()

    // An empty workspace should yield no files.
    assertThat(reportedFiles).isEmpty()
  }

  @Test
  fun testUtility_bazelWorkspace_developBranch_returnsAllFiles() {
    initializeEmptyGitRepository()
    switchToFeatureBranch()
    createEmptyWorkspace()
    tempFolder.newFolder("app")
    val file = tempFolder.newFile("app/First.kt")

    val changedFiles = listOf(file)
    testGitRepository.stageFilesForCommit(changedFiles)
    testGitRepository.commit(message = "Introduce files.")

    val reportFiles = runScript()

    assertThat(reportFiles.first().changedFilesList).contains("app/First.kt")
//    assertThat(reportFiles).exists()
  }

  private fun runScriptWithTextOutput(
    currentHeadHash: String = computeMergeBase("develop"),
    computeAllFiles: Boolean = false
  ): List<String> {
    val outputLog = tempFolder.newFile("output.log")
    main(
      arrayOf(
        tempFolder.root.absolutePath,
        outputLog.absolutePath,
        currentHeadHash,
        "compute_all_files=$computeAllFiles"
      )
    )
    return outputLog.readLines()
  }

  /**
   * Runs the compute_affected_files utility & returns all of the output lines. Note that the output
   * here is that which is saved directly to the output file, not debug lines printed to the
   * console.
   */
  private fun runScript(
    currentHeadHash: String = computeMergeBase("develop"),
    computeAllFiles: Boolean = false
  ): List<ChangedFilesBucket> {
    return parseOutputLogLines(runScriptWithTextOutput(currentHeadHash, computeAllFiles))
  }

  private fun parseOutputLogLines(outputLogLines: List<String>): List<ChangedFilesBucket> {
    return outputLogLines.map {
      ChangedFilesBucket.getDefaultInstance().mergeFromCompressedBase64(it.split(";")[1])
    }
  }

  private fun createEmptyWorkspace() {
    testBazelWorkspace.initEmptyWorkspace()
  }

  private fun initializeEmptyGitRepository() {
    // Initialize the git repository with a base 'develop' branch & an initial empty commit (so that
    // there's a HEAD commit).
    testGitRepository.init()
    testGitRepository.setUser(email = "test@oppia.org", name = "Test User")
    testGitRepository.checkoutNewBranch("develop")
    testGitRepository.commit(message = "Initial commit.", allowEmpty = true)
  }

  private fun switchToFeatureBranch() {
    testGitRepository.checkoutNewBranch("introduce-feature")
  }

  private fun computeMergeBase(referenceBranch: String): String =
    GitClient(tempFolder.root, referenceBranch, commandExecutor).branchMergeBase

  private fun initializeCommandExecutorWithLongProcessWaitTime(): CommandExecutorImpl {
    return CommandExecutorImpl(
      scriptBgDispatcher, processTimeout = 5, processTimeoutUnit = TimeUnit.MINUTES
    )
  }
}
