package org.oppia.android.util.accessibility

import android.content.Context
import android.view.View
import android.view.accessibility.AccessibilityManager
import javax.inject.Inject

/** Implementation of [AccessibilityService]. */
class AccessibilityServiceImpl @Inject constructor(
  private val context: Context
) : AccessibilityService {
  private val accessibilityManager by lazy {
    context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
  }

  override fun isScreenReaderEnabled(): Boolean {
    return accessibilityManager.isEnabled
  }

  override fun announceForAccessibilityForView(view: View, text: CharSequence) {
    view.announceForAccessibility(text)
  }
}
