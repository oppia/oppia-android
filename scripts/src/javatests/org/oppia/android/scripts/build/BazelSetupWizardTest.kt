package org.oppia.android.scripts.build

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.oppia.android.testing.assertThrows

/** Tests for the bazel_setup_wizard utility. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
class BazelSetupWizardTest {
  // TODO(#4994): Finish the tests for this suite.

  @Test
  fun testMain_noArguments_failsWithError() {
    val error = assertThrows(IllegalArgumentException::class) { runScript(/* No arguments. */) }

    assertThat(error)
      .hasMessageThat()
      .isEqualTo(
        "Usage: bazel run //scripts:bazel_setup_wizard -- <path_to_repo>\n  E.g.: bazel run" +
          " //scripts:bazel_setup_wizard -- \$(pwd)"
      )
  }

  private fun runScript(vararg args: String) {
    main(*args)
  }
}
