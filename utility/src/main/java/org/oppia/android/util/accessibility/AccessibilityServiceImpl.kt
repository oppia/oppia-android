package org.oppia.android.util.accessibility

import android.content.Context
import android.view.View
import android.view.accessibility.AccessibilityEvent
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
    if (!accessibilityManager.isEnabled) return

    // TODO(#5927): Migrate to the proper SDK 35+ APIs.
    @Suppress("DEPRECATION")
    // TYPE_ANNOUNCEMENT itself isn't deprecated, only announceForAccessibility()
    val event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    view.onInitializeAccessibilityEvent(event)
    event.text.add(text)
    event.contentDescription = null

    val parent = view.parent
    if (parent != null) {
      parent.requestSendAccessibilityEvent(view, event)
    } else {
      accessibilityManager.sendAccessibilityEvent(event)
    }
  }
}
