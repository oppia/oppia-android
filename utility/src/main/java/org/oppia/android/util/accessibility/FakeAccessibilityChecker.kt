package org.oppia.android.util.accessibility

import android.view.View
import javax.inject.Inject
import javax.inject.Singleton

/** Fake implementation of [AccessibilityChecker] which should be used in tests. */
@Singleton
class FakeAccessibilityChecker @Inject constructor() : AccessibilityChecker {
  private var isScreenReaderEnabled = false
  private var announcement: CharSequence = ""

  override fun isScreenReaderEnabled(): Boolean = isScreenReaderEnabled

  /**
   * Returns latest announcement. Announcement gets overwritten each time
   * announceForAccessibilityForView is called.
   */
  fun getLatestAnnouncement(): CharSequence = announcement

  /**
   * Sets whether a screen reader should be considered currently enabled. This will change the
   * return value of [isScreenReaderEnabled].
   */
  fun setScreenReaderEnabled(isEnabled: Boolean) {
    isScreenReaderEnabled = isEnabled
  }

  override fun announceForAccessibilityForView(view: View, text: CharSequence) {
    announcement = text
  }
}
