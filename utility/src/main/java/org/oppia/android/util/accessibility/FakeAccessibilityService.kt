package org.oppia.android.util.accessibility

import android.view.View
import javax.inject.Inject
import javax.inject.Singleton

/** Fake implementation of [AccessibilityService] which should be used in tests. */
@Singleton
class FakeAccessibilityService @Inject constructor() : AccessibilityService {
  private var isScreenReaderEnabled = false
  private var announcement: CharSequence? = null

  override fun isScreenReaderEnabled(): Boolean = isScreenReaderEnabled

  /**
   * Returns latest announcement. Note that the announcement gets overwritten each time
   * announceForAccessibilityForView is called.
   */
  fun getLatestAnnouncement(): CharSequence? = announcement

  /** Resets latest announcement. */
  fun resetLatestAnnouncement() {
    announcement = null
  }

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
