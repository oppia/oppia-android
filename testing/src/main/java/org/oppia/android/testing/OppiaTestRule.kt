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
import org.oppia.android.domain.platformparameter.PlatformParameterModule

private const val DEFAULT_ACCESSIBILITY_CHECKS_ENABLED_STATE = true

/** JUnit rule to enable [RunOn] test targeting. */
class OppiaTestRule : TestRule {

  override fun apply(base: Statement?, description: Description?): Statement {
    return object : Statement() {
      override fun evaluate() {
        val areAccessibilityChecksEnabled = description.areAccessibilityChecksEnabled()
        val targetPlatforms = description.getTargetPlatforms()
        val targetEnvironments = description.getTargetEnvironments()
        val currentPlatform = getCurrentPlatform()
        val currentEnvironment = getCurrentBuildEnvironment()

        val enabledFeatureFlags = extractParametersAndFeatureFlags(
          description?.testClass?.annotations?.toList(), EnableFeatureFlag::class.java
        ) +
          extractParametersAndFeatureFlags(description?.annotations, EnableFeatureFlag::class.java)

        val disabledFeatureFlags = extractParametersAndFeatureFlags(
          description?.testClass?.annotations?.toList(), DisableFeatureFlag::class.java
        ) +
          extractParametersAndFeatureFlags(description?.annotations, DisableFeatureFlag::class.java)

        val overriddenBoolParameters = extractParametersAndFeatureFlags(
          description?.testClass?.annotations?.toList(),
          OverrideBoolParameter::class.java
        ) +
          extractParametersAndFeatureFlags(
            description?.annotations,
            OverrideBoolParameter::class.java
          )

        val overriddenIntParameters = extractParametersAndFeatureFlags(
          description?.testClass?.annotations?.toList(),
          OverrideIntParameter::class.java
        ) +
          extractParametersAndFeatureFlags(
            description?.annotations,
            OverrideIntParameter::class.java
          )

        val overriddenStringParameters = extractParametersAndFeatureFlags(
          description?.testClass?.annotations?.toList(),
          OverrideStringParameter::class.java
        ) +
          extractParametersAndFeatureFlags(
            description?.annotations,
            OverrideStringParameter::class.java
          )

        val resetFeatureFlagToDefault = extractParametersAndFeatureFlags(
          description?.annotations, ResetFeatureFlagToDefault::class.java
        )

        val resetParameterToDefault = extractParametersAndFeatureFlags(
          description?.annotations, ResetParameterToDefault::class.java
        )

        try {
          applyOverrides(
            enabledFeatureFlags,
            disabledFeatureFlags,
            overriddenBoolParameters,
            overriddenIntParameters,
            overriddenStringParameters,
            resetFeatureFlagToDefault,
            resetParameterToDefault
          )

          when {
            currentPlatform in targetPlatforms && currentEnvironment in targetEnvironments -> {
              // Only run this test if it's targeting the current platform & environment.
              if (currentPlatform == TestPlatform.ESPRESSO && areAccessibilityChecksEnabled) {
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
        } finally {
          PlatformParameterModule.clearAllParameterOverrides()
        }
      }
    }
  }

  private fun applyOverrides(
    enabledFeatureFlags: List<EnableFeatureFlag>?,
    disabledFeatureFlags: List<DisableFeatureFlag>?,
    overriddenBoolParameters: List<OverrideBoolParameter>?,
    overriddenIntParameters: List<OverrideIntParameter>?,
    overriddenStringParameters: List<OverrideStringParameter>?,
    resetFeatureFlagToDefault: List<ResetFeatureFlagToDefault>?,
    resetParameterToDefault: List<ResetParameterToDefault>?
  ) {
    enabledFeatureFlags?.forEach { flag ->
      PlatformParameterModule.overrideFeatureFlags(flag.name, true)
    }

    disabledFeatureFlags?.forEach { flag ->
      PlatformParameterModule.overrideFeatureFlags(flag.name, false)
    }

    resetFeatureFlagToDefault?.forEach { resetFeatureFlag ->
      PlatformParameterModule.resetFeatureFlagToDefault(resetFeatureFlag.name)
    }

    overriddenBoolParameters?.forEach { _ ->
//      PlatformParameterModule.overrideParameter(overriddenValue.name, overriddenValue.value)
    }
    overriddenIntParameters?.forEach { _ ->
//      PlatformParameterModule.overrideParameter(overriddenValue.name, overriddenValue.value)
    }
    overriddenStringParameters?.forEach { _ ->
//      PlatformParameterModule.overrideParameter(overriddenValue.name, overriddenValue.value)
    }

    resetParameterToDefault?.forEach { _ ->
//      PlatformParameterModule.resetParameterToDefault(resetParameter.name)
    }
  }

  private fun getCurrentPlatform(): TestPlatform {
    /** To handle cases where Build.FINGERPRINT is unavailable */
    val fingerprint = try {
      Build.FINGERPRINT
    } catch (e: Exception) {
      null
    } ?: return TestPlatform.ROBOLECTRIC

    return if (fingerprint.contains("robolectric", ignoreCase = true)) {
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
      return methodAccessibilityStatus ?: classAccessibilityStatus
        ?: DEFAULT_ACCESSIBILITY_CHECKS_ENABLED_STATE
    }

    private fun Description.areAccessibilityTestsEnabledForMethod(): Boolean {
      return getAnnotation(DisableAccessibilityChecks::class.java) == null
    }

    private fun <T> Class<T>.areAccessibilityTestsEnabledForClass(): Boolean {
      return getAnnotation(DisableAccessibilityChecks::class.java) == null
    }

    inline fun <reified T : Annotation> extractParametersAndFeatureFlags(
      annotations: Collection<Annotation>?,
      featureFlagClass: Class<T>
    ): List<T> {
      return annotations?.flatMap { annotation ->
        when (annotation) {
          is T -> listOf(annotation)
          else -> {
            val containerClass = annotation.annotationClass.java
            if (containerClass.simpleName == "Container") {
              val valueMethod = containerClass.getDeclaredMethod("value")
              val flagsArray = valueMethod.invoke(annotation) as Array<*>
              flagsArray.filterIsInstance(featureFlagClass)
            } else {
              emptyList()
            }
          }
        }
      } ?: emptyList()
    }
  }
}
