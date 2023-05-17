package org.oppia.android.scripts.build

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.oppia.android.testing.assertThrows

/** Tests for the verify_file_targets utility. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
class VerifyFileTargetsTest {
  // TODO: Finish the tests for this suite.

  @Test
  fun testMain_noArguments_failsWithError() {
    val error = assertThrows(IllegalArgumentException::class) { runScript(/* No arguments. */) }

    assertThat(error)
      .hasMessageThat()
      .isEqualTo(
        "Expected usage: bazel run //scripts:verify_file_targets -- <root_directory>" +
          " [rel/path/filter]"
      )
  }

  private fun runScript(vararg args: String) {
    main(*args)
  }
}
