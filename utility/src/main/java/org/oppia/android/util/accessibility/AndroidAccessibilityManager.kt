package org.oppia.util.accessibility

import android.view.accessibility.AccessibilityManager

/** Accessibility Manager for providing [AccessibilityManager]. */
class AndroidAccessibilityManager(
  private val accessibilityManager: AccessibilityManager
) : CustomAccessibilityManager {

  /** return the current status of accessibility whether it is enabled or not. */
  override fun isScreenReaderEnabled(): Boolean {
    return accessibilityManager.isEnabled
  }
}
