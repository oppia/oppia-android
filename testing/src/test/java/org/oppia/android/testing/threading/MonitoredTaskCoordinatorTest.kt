package org.oppia.android.testing.threading

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.concurrent.TimeUnit

/** Tests for the small amount of logic built into [MonitoredTaskCoordinator]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config
class MonitoredTaskCoordinatorTest {
  @Test
  fun testDispatcher_defaultTimeout_robolectric_noDebugger_isTenSeconds() {
    simulateIntellijDebuggerEnabled(false)

    val defaultTime = MonitoredTaskCoordinator.DEFAULT_TIMEOUT_SECONDS
    val defaultTimeUnit = MonitoredTaskCoordinator.DEFAULT_TIMEOUT_UNIT

    // NB: this cannot be reliably tested on Espresso due to how the timeout is computed.
    assertThat(defaultTime).isEqualTo(10)
    assertThat(defaultTimeUnit).isEqualTo(TimeUnit.SECONDS)
  }

  @Test
  fun testDispatcher_defaultTimeout_robolectric_withIntellijDebugger_isOneHour() {
    simulateIntellijDebuggerEnabled(true)

    val defaultTime = MonitoredTaskCoordinator.DEFAULT_TIMEOUT_SECONDS
    val defaultTimeUnit = MonitoredTaskCoordinator.DEFAULT_TIMEOUT_UNIT
    val defaultTimeMinutes = TimeUnit.MINUTES.convert(defaultTime, defaultTimeUnit)

    // NB: this cannot be reliably tested on Espresso due to how the timeout is computed.
    assertThat(defaultTimeMinutes).isEqualTo(60)
  }

  private fun simulateIntellijDebuggerEnabled(enabled: Boolean) {
    System.setProperty("intellij.debug.agent", enabled.toString())
  }
}
