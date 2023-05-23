package org.oppia.android.scripts.ci

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.oppia.android.testing.assertThrows

/** Tests for the pre_push_checks utility. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
class PrePushChecksTest {
  // TODO(#4994): Finish the tests for this suite.

  @Test
  fun testMain_noArguments_failsWithError() {
    val error = assertThrows(IllegalArgumentException::class) { runScript(/* No arguments. */) }

    assertThat(error)
      .hasMessageThat()
      .isEqualTo("Usage: bazel run //scripts:pre_push_checks -- </path/to/repo_root>")
  }

  private fun runScript(vararg args: String) {
    main(*args)
  }
}
