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

  private fun initializeCommandExecutorWithLongProcessWaitTime(): CommandExecutorImpl {
    return CommandExecutorImpl(
      scriptBgDispatcher, processTimeout = 5, processTimeoutUnit = TimeUnit.MINUTES
    )
  }
}
