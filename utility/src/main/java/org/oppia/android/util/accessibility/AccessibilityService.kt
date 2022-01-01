package org.oppia.android.util.accessibility

import android.view.View

/** Utility for determining various properties of the system's accessibility manager(s). */
interface AccessibilityService {
  /** Returns whether a screen reader (such as TalkBack) is currently enabled. */
  fun isScreenReaderEnabled(): Boolean

  /**
   * Suggests to the screen reader (if any is installed/enabled) to announce the specified text for
   * the given view. This does not guarantee the text will actually be read, and it may interrupt
   * existing text being spoken.
   */
  fun announceForAccessibilityForView(view: View, text: CharSequence)
}
