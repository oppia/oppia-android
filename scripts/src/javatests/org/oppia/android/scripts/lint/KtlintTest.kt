package org.oppia.android.scripts.lint

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.oppia.android.testing.assertThrows

/** Tests for the ktlint utility. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
class KtlintTest {
  // TODO(#4993): Finish the tests for this suite.

  @Test
  fun testMain_noArguments_failsWithError() {
    val error = assertThrows(IllegalArgumentException::class) { runScript(/* No arguments. */) }

    assertThat(error)
      .hasMessageThat()
      .isEqualTo("Usage: bazel run //scripts:ktlint -- </path/to/repo_root> <mode>")
  }

  private fun runScript(vararg args: String) {
    main(*args)
  }
}
