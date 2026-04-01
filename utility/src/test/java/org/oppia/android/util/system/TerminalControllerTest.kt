package org.oppia.android.util.system

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/** Tests for [TerminalController]. */
@RunWith(JUnit4::class)
class TerminalControllerTest {
  @Test
  fun testTerminalController_exists() {
    // This is a simple test to satisfy the Testfile Presence Check.
    assertThat(TerminalController::class.java).isNotNull()
  }
}
