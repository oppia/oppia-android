package org.oppia.android.util.accessibility

import javax.inject.Inject
import javax.inject.Singleton

/** Fake implementation of [AccessibilityChecker] which should be used in tests. */
@Singleton
class FakeAccessibilityChecker @Inject constructor() : AccessibilityChecker {
  private var isScreenReaderEnabled = false

  override fun isScreenReaderEnabled(): Boolean = isScreenReaderEnabled

  /**
   * Sets whether a screen reader should be considered currently enabled. This will change the
   * return value of [isScreenReaderEnabled].
   */
  fun setScreenReaderEnabled(isEnabled: Boolean) {
    isScreenReaderEnabled = isEnabled
  }
}
