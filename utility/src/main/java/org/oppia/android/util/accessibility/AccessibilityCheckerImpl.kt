package org.oppia.android.util.accessibility

import android.content.Context
import android.view.accessibility.AccessibilityManager
import javax.inject.Inject

/** Implementation of [AccessibilityChecker]. */
class AccessibilityCheckerImpl @Inject constructor(
  private val context: Context
) : AccessibilityChecker {
  private val accessibilityManager by lazy {
    context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
  }

  override fun isScreenReaderEnabled(): Boolean {
    return accessibilityManager.isEnabled
  }
}
