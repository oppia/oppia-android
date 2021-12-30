package org.oppia.android.app.accessibility

import android.view.View
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.mockito.Mockito.mock
import org.oppia.android.util.accessibility.FakeAccessibilityChecker

/** Tests for [FakeAccessibilityChecker]. */
class FakeAccessibilityCheckerTest {
  private var accessibilityManager = FakeAccessibilityChecker()

  @Test
  fun testInitialState() {
    assertThat(accessibilityManager.isScreenReaderEnabled()).isFalse()
    assertThat(accessibilityManager.getLatestAnnouncement()).isNull()
  }

  @Test
  fun testSetScreenReaderEnabledTrue() {
    accessibilityManager.setScreenReaderEnabled(true)
    assertThat(accessibilityManager.isScreenReaderEnabled()).isTrue()
  }

  @Test
  fun testSetScreenReaderEnabledFalse() {
    accessibilityManager.setScreenReaderEnabled(false)
    assertThat(accessibilityManager.isScreenReaderEnabled()).isFalse()
  }

  @Test
  fun testAnnounceForAccessibilityForView() {
    accessibilityManager.announceForAccessibilityForView(mock(View::class.java), "test")
    assertThat(accessibilityManager.getLatestAnnouncement()).isEqualTo("test")
  }

  @Test
  fun testResetLatestAnnouncement() {
    accessibilityManager.resetLatestAnnouncement()
    assertThat(accessibilityManager.getLatestAnnouncement()).isNull()
  }
}
