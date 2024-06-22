package org.oppia.android.scripts.maven

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.oppia.android.testing.assertThrows

/** Tests for [ValidateMavenDependencies]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
class ValidateMavenDependenciesTest {
  // TODO(#4973): Finish the tests for this suite.

  @Test
  fun testMain_noArguments_failsWithError() {
    val error = assertThrows<IllegalStateException> { runScript(/* No arguments. */) }

    assertThat(error).hasMessageThat().contains("Usage: bazel run")
  }

  private fun runScript(vararg args: String) {
    main(*args)
  }
}
