package org.oppia.android.testing

import android.os.Build
import org.junit.AssumptionViolatedException
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/** JUnit rule to enable [RunOn] test targeting. */
class OppiaTestRule : TestRule {
  override fun apply(base: Statement?, description: Description?): Statement {
    return object : Statement() {
      override fun evaluate() {
        val targetPlatforms = description.getTargetPlatforms()
        val currentPlatform = getCurrentPlatform()
        if (currentPlatform in targetPlatforms) {
          // Only run this test if it's targeting the current platform.
          base?.evaluate()
        } else {
          // See https://github.com/junit-team/junit4/issues/116 for context.
          throw AssumptionViolatedException(
            "Test targeting ${targetPlatforms.toPluralDescription()} ignored on $currentPlatform"
          )
        }
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
    private fun Array<out TestPlatform>.toPluralDescription(): String {
      return if (size > 1) "platforms ${this.joinToString()}" else "platform ${this.first()}"
    }

    private fun Description?.getTargetPlatforms(): Array<out TestPlatform> {
      val methodTargetPlatforms = this?.getTargetTestPlatforms()
      val classTargetPlatforms = this?.testClass?.getTargetTestPlatforms()
      return methodTargetPlatforms ?: classTargetPlatforms ?: TestPlatform.values()
    }

    private fun Description.getTargetTestPlatforms(): Array<out TestPlatform>? {
      return getAnnotation(RunOn::class.java)?.testPlatforms
    }

    private fun <T> Class<T>.getTargetTestPlatforms(): Array<out TestPlatform>? {
      return getAnnotation(RunOn::class.java)?.testPlatforms
    }
  }
}
