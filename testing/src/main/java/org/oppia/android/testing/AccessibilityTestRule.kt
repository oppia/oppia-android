package org.oppia.android.testing

import android.os.Build
import androidx.test.espresso.accessibility.AccessibilityChecks
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

// TODO(#3351): Merge AccessibilityTestRule with OppiaTestRule.
private const val DEFAULT_ENABLED_STATE = true
/**
 * JUnit rule to enable [AccessibilityChecks] on Espresso. This does not work when run on Robolectric.
 *
 * Reference: https://developer.android.com/training/testing/espresso/accessibility-checking
 */
class AccessibilityTestRule : TestRule {
  override fun apply(base: Statement?, description: Description?): Statement {
    return object : Statement() {
      override fun evaluate() {
        val isEnabled = description.areAccessibilityChecksEnabled()
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
    private fun Description?.areAccessibilityChecksEnabled(): Boolean {
      val methodAccessibilityStatus = this?.areAccessibilityTestsEnabledForMethod()
      val classAccessibilityStatus = this?.testClass?.areAccessibilityTestsEnabledForClass()
      return methodAccessibilityStatus ?: classAccessibilityStatus ?: DEFAULT_ENABLED_STATE
    }

    private fun Description.areAccessibilityTestsEnabledForMethod(): Boolean {
      return getAnnotation(DisableAccessibilityChecks::class.java) == null
    }

    private fun <T> Class<T>.areAccessibilityTestsEnabledForClass(): Boolean {
      return getAnnotation(DisableAccessibilityChecks::class.java) == null
    }
  }
}
