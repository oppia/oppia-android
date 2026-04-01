package org.oppia.android.util.system

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/** Tests for [TerminalControllerImpl]. */
@RunWith(JUnit4::class)
class TerminalControllerImplTest {
  @Test
  fun testTerminalControllerImpl_exists() {
    val controller = TerminalControllerImpl()
    assertThat(controller).isNotNull()
  }
}
