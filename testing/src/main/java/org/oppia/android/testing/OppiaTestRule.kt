package org.oppia.android.testing

import android.os.Build
import androidx.test.espresso.accessibility.AccessibilityChecks
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils.matchesCheckNames
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils.matchesViews
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.endsWith
import org.junit.AssumptionViolatedException
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

private const val DEFAULT_ENABLED_STATE = true

/** JUnit rule to enable [RunOn] test targeting. */
class OppiaTestRule : TestRule {
  override fun apply(base: Statement?, description: Description?): Statement {
    return object : Statement() {
      override fun evaluate() {
        val isEnabled = description.areAccessibilityChecksEnabled()
        val targetPlatforms = description.getTargetPlatforms()
        val targetEnvironments = description.getTargetEnvironments()
        val currentPlatform = getCurrentPlatform()
        val currentEnvironment = getCurrentBuildEnvironment()
        if(currentPlatform == TestPlatform.ESPRESSO && isEnabled) {
          AccessibilityChecks.enable().apply {
            // Suppressing failures for all views which matches with below conditions as we do not
            // want to change the UI to pass these failures as it will change the expected behaviour
            // for learner.
            setSuppressingResultMatcher(
              allOf(
                matchesCheckNames(`is`("TouchTargetSizeViewCheck")),
                matchesViews(withContentDescription("More options")),
                matchesViews(withClassName(endsWith("OverflowMenuButton")))
              )
            )
          }.setRunChecksFromRootView(true)
        }
        when {
          currentPlatform in targetPlatforms && currentEnvironment in targetEnvironments -> {
            // Only run this test if it's targeting the current platform & environment.
            base?.evaluate()
          }
          currentPlatform !in targetPlatforms -> {
            // See https://github.com/junit-team/junit4/issues/116 for context.
            throw AssumptionViolatedException(
              "Test targeting ${targetPlatforms.toPluralPlatformDescription()} ignored on" +
                " $currentPlatform"
            )
          }
          currentEnvironment !in targetEnvironments -> {
            throw AssumptionViolatedException(
              "Test targeting ${targetEnvironments.toPluralEnvironmentDescription()} ignored on" +
                " $currentEnvironment"
            )
          }
          else -> throw AssertionError("Reached impossible state in test rule")
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

  private fun getCurrentBuildEnvironment(): BuildEnvironment {
    val command = System.getProperty("sun.java.command") ?: ""
    return if (command.contains("bazel", ignoreCase = true)) {
      BuildEnvironment.BAZEL
    } else {
      BuildEnvironment.GRADLE
    }
  }

  private companion object {
    private fun List<TestPlatform>.toPluralPlatformDescription(): String {
      return if (size > 1) "platforms ${this.joinToString()}" else "platform ${this.first()}"
    }

    private fun Description?.getTargetPlatforms(): List<TestPlatform> {
      val methodTargetPlatforms = this?.getTargetTestPlatforms()
      val classTargetPlatforms = this?.testClass?.getTargetTestPlatforms()
      return methodTargetPlatforms ?: classTargetPlatforms ?: TestPlatform.values().toList()
    }

    private fun Description.getTargetTestPlatforms(): List<TestPlatform>? {
      return getAnnotation(RunOn::class.java)?.testPlatforms?.toList()
    }

    private fun <T> Class<T>.getTargetTestPlatforms(): List<TestPlatform>? {
      return getAnnotation(RunOn::class.java)?.testPlatforms?.toList()
    }

    private fun List<BuildEnvironment>.toPluralEnvironmentDescription(): String {
      return if (size > 1) "environments ${this.joinToString()}" else "environment ${this.first()}"
    }

    private fun Description?.getTargetEnvironments(): List<BuildEnvironment> {
      val methodBuildEnvironments = this?.getTargetBuildEnvironments()
      val classBuildEnvironments = this?.testClass?.getTargetBuildEnvironments()
      return methodBuildEnvironments ?: classBuildEnvironments ?: BuildEnvironment.values().toList()
    }

    private fun Description.getTargetBuildEnvironments(): List<BuildEnvironment>? {
      return getAnnotation(RunOn::class.java)?.buildEnvironments?.toList()
    }

    private fun <T> Class<T>.getTargetBuildEnvironments(): List<BuildEnvironment>? {
      return getAnnotation(RunOn::class.java)?.buildEnvironments?.toList()
    }

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
