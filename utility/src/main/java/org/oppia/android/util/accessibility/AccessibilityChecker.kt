package org.oppia.android.util.accessibility

import android.view.View

/** Utility for determining various properties of the system's accessibility manager(s). */
interface AccessibilityChecker {
  /** Returns whether a screen reader (such as TalkBack) is currently enabled. */
  fun isScreenReaderEnabled(): Boolean
  /** Calls view.announceForAccessibility(text). */
  fun announceForAccessibilityForView(view: View, text: CharSequence?)
}
