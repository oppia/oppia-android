package org.oppia.util.accessibility

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeAccessibilityManager @Inject constructor() : CustomAccessibilityManager {
  private var talkbackEnabled = true

  override fun isScreenReaderEnabled(): Boolean {
    return talkbackEnabled
  }

  fun setTalkbackEnabled(status: Boolean) {
    talkbackEnabled = status
  }
}
