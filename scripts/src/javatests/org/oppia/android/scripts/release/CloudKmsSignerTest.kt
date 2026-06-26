package org.oppia.android.scripts.release

import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandResult
import org.oppia.android.testing.assertThrows
import java.io.File

/**
 * Tests for [CloudKmsSigner].
 *
 * Covers the precondition-failure paths that do not require real GCP credentials: missing AAB
 * file and missing GCP access token.
 *
 * TODO(#<issue>): Expand test coverage to validate that `jarsigner` is invoked with the expected
 *   arguments by injecting a [CommandExecutor] that captures command invocations.
 */
class CloudKmsSignerTest {
  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

  /**
   * A [CommandExecutor] stub that fails loudly if called. The tests in this file target
   * code paths that exit before [CommandExecutor.executeCommand] is ever reached.
   */
  private val unusedCommandExecutor = object : CommandExecutor {
    override fun executeCommand(
      workingDir: File,
      command: String,
      vararg arguments: String,
      includeErrorOutput: Boolean,
      inputLines: Sequence<String>
    ): CommandResult = error("CommandExecutor should not be called in these tests.")
  }

  @Test
  fun testSign_withNonExistentAabPath_throwsIllegalStateWithAabPath() {
    val signer = CloudKmsSigner(
      kmsKeyResourceName = "projects/p/locations/gl/keyRings/r/cryptoKeys/k/cryptoKeyVersions/1",
      gcpAccessToken = "fake-token".toCharArray(),
      commandExecutor = unusedCommandExecutor
    )
    val nonExistentAab = tempFolder.root.toPath().resolve("missing.aab")
    val cert = tempFolder.newFile("cert.pem").toPath()
    val output = tempFolder.root.toPath().resolve("signed.aab")

    val exception = assertThrows<IllegalStateException> {
      signer.sign(nonExistentAab, cert, output)
    }

    assertThat(exception).hasMessageThat().contains("missing.aab")
  }
}
