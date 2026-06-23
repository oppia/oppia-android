package org.oppia.android.scripts.release

import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandResult
import org.oppia.android.testing.assertThrows
import java.io.File
import java.io.FileNotFoundException

/**
 * Tests for [CloudKmsSigner].
 *
 * These tests cover only the precondition-failure paths that do not require real GCP credentials
 * or a Cloud KMS keystore. The actual KMS signing path is exercised at runtime in CI via the
 * `sign_release_binary` binary and is validated by the `build_and_sign.yml` workflow.
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

  // region sign() — precondition checks

  @Test
  fun testSign_withNonExistentAabPath_throwsFileNotFoundWithAabPath() {
    val signer = CloudKmsSigner(
      kmsKeyResourceName = "projects/p/locations/global/keyRings/r/cryptoKeys/k/cryptoKeyVersions/1",
      commandExecutor = unusedCommandExecutor
    )
    val nonExistentAab = tempFolder.root.toPath().resolve("missing.aab")
    val cert = tempFolder.newFile("cert.pem").toPath()
    val output = tempFolder.root.toPath().resolve("signed.aab")

    val exception = assertThrows<FileNotFoundException> {
      signer.sign(nonExistentAab, cert, output)
    }

    assertThat(exception).hasMessageThat().contains("missing.aab")
  }

  @Test
  fun testSign_withExistingAabAndMissingGcpToken_throwsIllegalStateException() {
    // Bazel's sandbox does not pass GCP_ACCESS_TOKEN through, so checkNotNull fails here.
    val signer = CloudKmsSigner(
      kmsKeyResourceName = "projects/p/locations/global/keyRings/r/cryptoKeys/k/cryptoKeyVersions/1",
      commandExecutor = unusedCommandExecutor
    )
    val unsignedAab = tempFolder.newFile("unsigned.aab")
    val cert = tempFolder.newFile("cert.pem")
    val output = tempFolder.root.toPath().resolve("signed.aab")

    val exception = assertThrows<IllegalStateException> {
      signer.sign(unsignedAab.toPath(), cert.toPath(), output)
    }

    assertThat(exception).hasMessageThat().contains("GCP_ACCESS_TOKEN")
  }

  // endregion
}
