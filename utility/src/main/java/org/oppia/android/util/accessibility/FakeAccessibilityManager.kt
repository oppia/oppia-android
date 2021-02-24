package org.oppia.android.util.accessibility

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeAccessibilityManager @Inject constructor() : CustomAccessibilityManager {
  private var talkbackEnabled = false

  override fun isScreenReaderEnabled(): Boolean {
    return talkbackEnabled
  }

  fun setTalkbackEnabled(status: Boolean) {
    talkbackEnabled = status
  }
}
