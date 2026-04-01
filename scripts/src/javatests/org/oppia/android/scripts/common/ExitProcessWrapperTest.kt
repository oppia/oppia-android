package org.oppia.android.scripts.common

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/** Tests for [ExitProcessWrapper]. */
@RunWith(JUnit4::class)
class ExitProcessWrapperTest {
  @Test
  fun testExitProcessWrapper_exists() {
    val wrapper = ExitProcessWrapper()
    assertThat(wrapper).isNotNull()
  }
}
