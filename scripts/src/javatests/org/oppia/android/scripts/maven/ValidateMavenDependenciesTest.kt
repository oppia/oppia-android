package org.oppia.android.scripts.maven

import org.junit.Test

/** Tests for [ValidateMavenDependencies]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
class ValidateMavenDependenciesTest {
  // TODO: Finish tests.
  @Test
  fun test_nothing_yet() {
    runScript("missing", "some", "args")
  }

  private fun runScript(vararg args: String) {
    main(*args)
  }
}
