package org.oppia.android.testing

import android.os.Build
import androidx.test.espresso.accessibility.AccessibilityChecks
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * JUnit rule to enable [AccessibilityChecks] on Espresso.
 * Reference: https://developer.android.com/training/testing/espresso/accessibility-checking
 */
class AccessibilityTestRule : TestRule {
  override fun apply(base: Statement?, description: Description?): Statement {
    return object : Statement() {
      override fun evaluate() {
        val isEnabled = description.isAccessibilityChecksEnabled()
        if (getCurrentPlatform() == TestPlatform.ESPRESSO && isEnabled) {
          AccessibilityChecks.enable().setRunChecksFromRootView(true)
        }
        base?.evaluate()
      }
    }
  }

  private fun getCurrentPlatform(): TestPlatform {
    return if (Build.FINGERPRINT.contains("robolectric", ignoreCase = true)) {
      TestPlatform.ROBOLECTRIC
    } else {
      TestPlatform.ESPRESSO
    }
  }

  private companion object {
    private fun Description?.isAccessibilityChecksEnabled(): Boolean {
      val methodAccessibilityStatus = this?.getAccessibilityStatus()
      val classAccessibilityStatus = this?.testClass?.getAccessibilityStatus()
      return methodAccessibilityStatus ?: classAccessibilityStatus ?: /* defaultValue= */ true
    }

    private fun Description.getAccessibilityStatus(): Boolean? {
      return getAnnotation(EnableAccessibility::class.java)?.isEnabled
    }

    private fun <T> Class<T>.getAccessibilityStatus(): Boolean? {
      return getAnnotation(EnableAccessibility::class.java)?.isEnabled
    }
  }
}
