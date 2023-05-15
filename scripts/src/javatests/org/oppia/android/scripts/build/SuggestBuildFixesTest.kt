package org.oppia.android.scripts.build

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.oppia.android.testing.assertThrows

/** Tests for the suggest_build_fixes utility. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
class SuggestBuildFixesTest {
  // TODO(#4977): Finish the tests for this suite.

  @Test
  fun testMain_noArguments_failsWithError() {
    val error = assertThrows(IllegalArgumentException::class) { runScript(/* No arguments. */) }

    assertThat(error)
      .hasMessageThat()
      .isEqualTo(
        "Usage: bazel run //scripts:suggest_build_fixes -- <root_directory> <mode=deltas/fix>" +
          " <bazel_target_exp:String> ..."
      )
  }

  private fun runScript(vararg args: String) {
    main(*args)
  }
}
