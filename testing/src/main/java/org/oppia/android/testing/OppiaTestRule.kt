package org.oppia.android.testing

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
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
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.domain.platformparameter.testing.PlatformParameterInitializationInjectorProvider
import org.oppia.android.domain.platformparameter.testing.TestPlatformParameterConfigRetriever
import org.robolectric.annotation.Config

private const val DEFAULT_ACCESSIBILITY_CHECKS_ENABLED_STATE = true

/**
 * The primary JUnit rule for enabling most test-only functionality for Oppia tests.
 *
 * Including this rule enables two sets of functionality:
 * - The ability to use [RunOn] for selectively disabling tests in certain conditions.
 * - The ability to override platform parameters and feature flags.
 *
 * Note that if you run into an error like: 'Attempting to access feature flag ... before
 * initialization' then you need to include this rule in order to ensure platform parameters and
 * feature flags are correctly initialized for production code.
 */
class OppiaTestRule : TestRule {
  private lateinit var currentDescription: Description

  override fun apply(base: Statement?, description: Description?): Statement {
    return object : Statement() {
      override fun evaluate() {
        currentDescription = checkNotNull(description) { "Failed to receive description for test." }

        val areAccessibilityChecksEnabled = description.areAccessibilityChecksEnabled()
        val targetPlatforms = description.getTargetPlatforms()
        val targetEnvironments = description.getTargetEnvironments()
        val currentPlatform = getCurrentPlatform()
        val currentEnvironment = getCurrentBuildEnvironment()

        // Note that the order is important here: flag and parameter overrides must happen before
        // any injection-level initialization occurs.
        overridePlatformParameterAnnotations(description)
        initializeTestForPlatformParameterOverrides(ApplicationProvider.getApplicationContext())

        try {
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
          TestPlatformParameterConfigRetriever.reset()
        }
      }
    }
  }

  // TODO: Replace all other execute/simulate in previous app calls with this one.
  /**
   * Creates a separate test application component and executes the specified block in a way that
   * complies with existing platform parameter and feature flag overrides for the current test.
   *
   * This can be used to simulate arranging state in a "prior" run of the app.
   *
   * This should be called before any primary application setup to avoid undefined behavior in
   * production code (i.e. it should be run at the very beginning of the test).
   *
   * Note that only dependencies fetched from the specified [C] application component should be
   * used, not any class-level injected dependencies as these won't yet be defined.
   */
  inline fun <reified C : ApplicationComponent> executeInPreviousAppInstance(block: (C) -> Unit) {
    val testClass = getCurrentTestClass()
    val applicationClass = checkNotNull(testClass.getAnnotation(Config::class.java)) {
      "Expected test class to include Robolectric configuration: ${testClass.name}."
    }.application.java
    val testApplication = applicationClass.getDeclaredConstructor().newInstance()

    // The true application is hooked as a base context. This is to make sure the new application
    // can behave like a real Android application class (per Robolectric) without having a shared
    // Dagger dependency graph with the application under test.
    val attachBaseContext =
      applicationClass.getDeclaredMethod("attachBaseContext", Context::class.java)
    // Force attachBaseContext to be accessible to avoid tests needing to do it manually.
    attachBaseContext.isAccessible = true
    attachBaseContext.invoke(testApplication, ApplicationProvider.getApplicationContext())

    // The test application already creates its own component. Reuse that to avoid creating multiple
    // copies of the Dagger graph since only two should exist: one for the "previous application"
    // run and one for the main part of the test.
    val initializationProvider = testApplication as PlatformParameterInitializationInjectorProvider
    val applicationComponent =
      initializationProvider.getPlatformParameterInitializationInjector() as C

    // Ensure overrides are initialized, then run the block that should be run in a "prior app run."
    initializeTestForPlatformParameterOverrides(testApplication)
    block(applicationComponent)
  }

  fun getCurrentTestClass(): Class<*> =
    checkNotNull(currentDescription.testClass) { "Expected test class from JUnit description." }

  fun initializeTestForPlatformParameterOverrides(application: Application) {
    check(application is PlatformParameterInitializationInjectorProvider) {
      "Application class needs to implement PlatformParameterInitializationInjectorProvider:" +
        " ${application.javaClass.name}."
    }

    // Wait for parameters to successfully load. Note that this is particularly ordered to avoid a
    // race condition on priming the underlying platform parameter database and trying to load
    // parameters too quickly (which can cause a redundant initialization of
    // PlatformParameterProcessState).
    val injector = application.getPlatformParameterInitializationInjector()
    val paramsProvider = injector.getPlatformParameterController().loadParameters()
    injector.getTestCoroutineDispatchers().runCurrent()
    injector.getDataProviderTestMonitorFactory()
      .createMonitor(paramsProvider)
      .waitForNextSuccessResult()
  }

  private fun getCurrentPlatform(): TestPlatform {
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

    private fun Description.getTargetPlatforms(): List<TestPlatform> {
      val methodTargetPlatforms = this.getTargetTestPlatforms()
      val classTargetPlatforms = this.testClass?.getTargetTestPlatforms()
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

    private fun Description.getTargetEnvironments(): List<BuildEnvironment> {
      val methodBuildEnvironments = this.getTargetBuildEnvironments()
      val classBuildEnvironments = this.testClass?.getTargetBuildEnvironments()
      return methodBuildEnvironments ?: classBuildEnvironments ?: BuildEnvironment.values().toList()
    }

    private fun Description.getTargetBuildEnvironments(): List<BuildEnvironment>? {
      return getAnnotation(RunOn::class.java)?.buildEnvironments?.toList()
    }

    private fun <T> Class<T>.getTargetBuildEnvironments(): List<BuildEnvironment>? {
      return getAnnotation(RunOn::class.java)?.buildEnvironments?.toList()
    }

    private fun Description.areAccessibilityChecksEnabled(): Boolean {
      return areAccessibilityTestsEnabledForMethod() ||
        testClass.areAccessibilityTestsEnabledForClass() ||
        DEFAULT_ACCESSIBILITY_CHECKS_ENABLED_STATE
    }

    private fun Description.areAccessibilityTestsEnabledForMethod(): Boolean {
      return getAnnotation(DisableAccessibilityChecks::class.java) == null
    }

    private fun <T> Class<T>?.areAccessibilityTestsEnabledForClass(): Boolean {
      return this?.getAnnotation(DisableAccessibilityChecks::class.java) == null
    }

    private fun overridePlatformParameterAnnotations(description: Description) {
      val enabledClassLevelFeatureFlags = extractParametersAndFeatureFlags(
        description.testClass?.annotations?.toList(),
        EnableFeatureFlag::class.java
      )
      val disabledClassLevelFeatureFlags = extractParametersAndFeatureFlags(
        description.testClass?.annotations?.toList(),
        DisableFeatureFlag::class.java
      )
      validatePlatformParameterConflicts(
        enabledFeatureFlags = enabledClassLevelFeatureFlags,
        disabledFeatureFlags = disabledClassLevelFeatureFlags
      )

      val enabledMethodLevelFeatureFlags = extractParametersAndFeatureFlags(
        description.annotations,
        EnableFeatureFlag::class.java
      )
      val disabledMethodLevelFeatureFlags = extractParametersAndFeatureFlags(
        description.annotations,
        DisableFeatureFlag::class.java
      )
      val resetFeatureFlagToDefault = extractParametersAndFeatureFlags(
        description.annotations,
        ResetFeatureFlagToDefault::class.java
      )
      validatePlatformParameterConflicts(
        enabledFeatureFlags = enabledMethodLevelFeatureFlags,
        disabledFeatureFlags = disabledMethodLevelFeatureFlags,
        resetFeatureFlags = resetFeatureFlagToDefault
      )

      val overriddenClassLevelBoolParameters = extractParametersAndFeatureFlags(
        description.testClass?.annotations?.toList(),
        OverrideBoolParameter::class.java
      )
      val overriddenClassLevelIntParameters = extractParametersAndFeatureFlags(
        description.testClass?.annotations?.toList(),
        OverrideIntParameter::class.java
      )
      val overriddenClassLevelStringParameters = extractParametersAndFeatureFlags(
        description.testClass?.annotations?.toList(),
        OverrideStringParameter::class.java
      )
      validatePlatformParameterConflicts(
        overriddenBoolParameters = overriddenClassLevelBoolParameters,
        overriddenIntParameters = overriddenClassLevelIntParameters,
        overriddenStringParameters = overriddenClassLevelStringParameters
      )
      validateResetFeatureFlagConfiguration(
        enabledClassLevelFeatureFlags = enabledClassLevelFeatureFlags,
        disabledClassLevelFeatureFlags = disabledClassLevelFeatureFlags,
        resetFeatureFlags = resetFeatureFlagToDefault
      )

      val overriddenMethodLevelBoolParameters = extractParametersAndFeatureFlags(
        description.annotations,
        OverrideBoolParameter::class.java
      )
      val overriddenMethodLevelIntParameters = extractParametersAndFeatureFlags(
        description.annotations,
        OverrideIntParameter::class.java
      )
      val overriddenMethodLevelStringParameters = extractParametersAndFeatureFlags(
        description.annotations,
        OverrideStringParameter::class.java
      )
      validatePlatformParameterConflicts(
        overriddenBoolParameters = overriddenMethodLevelBoolParameters,
        overriddenIntParameters = overriddenMethodLevelIntParameters,
        overriddenStringParameters = overriddenMethodLevelStringParameters
      )

      applyFeatureOverrides(
        enabledClassLevelFeatureFlags,
        disabledClassLevelFeatureFlags,
        enabledMethodLevelFeatureFlags,
        disabledMethodLevelFeatureFlags,
        resetFeatureFlagToDefault
      )
      applyPlatformParameterOverrides(
        overriddenClassLevelBoolParameters,
        overriddenClassLevelIntParameters,
        overriddenClassLevelStringParameters,
        overriddenMethodLevelBoolParameters,
        overriddenMethodLevelIntParameters,
        overriddenMethodLevelStringParameters
      )
    }

    private fun applyFeatureOverrides(
      enabledClassLevelFlags: List<EnableFeatureFlag>,
      disabledClassLevelFlags: List<DisableFeatureFlag>,
      enabledMethodLevelFlags: List<EnableFeatureFlag>,
      disabledMethodLevelFlags: List<DisableFeatureFlag>,
      resetFeatureFlagToDefault: List<ResetFeatureFlagToDefault>
    ) {
      val classFlagEnables = enabledClassLevelFlags.associate { it.id to true }
      val classFlagDisables = disabledClassLevelFlags.associate { it.id to false }
      val methodFlagEnables = enabledMethodLevelFlags.associate { it.id to true }
      val methodFlagDisables = disabledMethodLevelFlags.associate { it.id to false }
      val flagResets = resetFeatureFlagToDefault.mapTo(mutableSetOf()) { it.id }

      // Class-level and method-level should be unique for a given ID.
      val classFlagValues = classFlagEnables + classFlagDisables
      val methodFlagValues = methodFlagEnables + methodFlagDisables

      // Method-level values take precedence over class-level, and adding replaces values. Resets
      // should not perform any overriding, so they are resolved last.
      val flagOverrideValues = (classFlagValues + methodFlagValues) - flagResets

      flagOverrideValues.forEach(TestPlatformParameterConfigRetriever.Companion::setFlagOverride)
    }

    private fun applyPlatformParameterOverrides(
      overriddenClassLevelBoolParams: List<OverrideBoolParameter>,
      overriddenClassLevelIntParams: List<OverrideIntParameter>,
      overriddenClassLevelStringParams: List<OverrideStringParameter>,
      overriddenMethodLevelBoolParams: List<OverrideBoolParameter>,
      overriddenMethodLevelIntParams: List<OverrideIntParameter>,
      overriddenMethodLevelStringParams: List<OverrideStringParameter>
    ) {
      val classBoolOverrides = overriddenClassLevelBoolParams.associate { it.id to it.value }
      val methodBoolOverrides = overriddenMethodLevelBoolParams.associate { it.id to it.value }
      val classIntOverrides = overriddenClassLevelIntParams.associate { it.id to it.value }
      val methodIntOverrides = overriddenMethodLevelIntParams.associate { it.id to it.value }
      val classStrOverrides = overriddenClassLevelStringParams.associate { it.id to it.value }
      val methodStrOverrides = overriddenMethodLevelStringParams.associate { it.id to it.value }

      // Method-level values take precedence over class-level, and adding replaces values.
      val boolOverrides = classBoolOverrides + methodBoolOverrides
      val intOverrides = classIntOverrides + methodIntOverrides
      val strOverrides = classStrOverrides + methodStrOverrides

      boolOverrides.forEach(TestPlatformParameterConfigRetriever.Companion::setParameterOverride)
      intOverrides.forEach(TestPlatformParameterConfigRetriever.Companion::setParameterOverride)
      strOverrides.forEach(TestPlatformParameterConfigRetriever.Companion::setParameterOverride)
    }

    private fun validatePlatformParameterConflicts(
      enabledFeatureFlags: List<EnableFeatureFlag> = emptyList(),
      disabledFeatureFlags: List<DisableFeatureFlag> = emptyList(),
      resetFeatureFlags: List<ResetFeatureFlagToDefault> = emptyList(),
      overriddenBoolParameters: List<OverrideBoolParameter> = emptyList(),
      overriddenIntParameters: List<OverrideIntParameter> = emptyList(),
      overriddenStringParameters: List<OverrideStringParameter> = emptyList(),
    ) {
      val combinedPlatformParameters = (
        enabledFeatureFlags.map { it.id } +
          disabledFeatureFlags.map { it.id } +
          resetFeatureFlags.map { it.id } +
          overriddenBoolParameters.map { it.id } +
          overriddenIntParameters.map { it.id } +
          overriddenStringParameters.map { it.id }
        ).groupingBy { it }
        .eachCount()

      val conflictingPlatformParameters = combinedPlatformParameters.filter { it.value > 1 }.keys
      if (conflictingPlatformParameters.isNotEmpty()) {
        error(
          "Conflicting feature flag annotations found: $conflictingPlatformParameters. " +
            "A test class or method cannot have multiple annotations for the same feature flag."
        )
      }
    }

    private fun validateResetFeatureFlagConfiguration(
      enabledClassLevelFeatureFlags: List<EnableFeatureFlag>,
      disabledClassLevelFeatureFlags: List<DisableFeatureFlag>,
      resetFeatureFlags: List<ResetFeatureFlagToDefault>
    ) {
      val classLevelFeatureFlags = (
        enabledClassLevelFeatureFlags.map { it.id } +
          disabledClassLevelFeatureFlags.map { it.id }
        )
      val invalidResets = resetFeatureFlags.map { it.id }
        .filterNot { it in classLevelFeatureFlags }
      if (invalidResets.isNotEmpty()) {
        error(
          "Invalid reset feature flag annotations found: $invalidResets. " +
            "A reset annotation must have a corresponding class-level declaration."
        )
      }
    }

    private inline fun <reified T : Annotation> extractParametersAndFeatureFlags(
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
