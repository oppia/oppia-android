package org.oppia.android.domain.platformparameter

import org.oppia.android.app.model.FeatureFlagId
import org.oppia.android.app.model.PlatformParameterId
import org.oppia.android.app.model.PlatformParameterValue
import org.oppia.android.app.model.SyncStatus

/**
 * The represented state of all platform parameters and feature flags for the lifetime of the
 * current Oppia Android application process.
 *
 * This class is ultimately managed by implementations of [PlatformParameterController] and
 * leveraged by bridge Dagger modules for providing actual injectable values for production and test
 * code. It is never expected to be used outside of the platform parameter package.
 *
 * Only one instance of this class should exist and be used for the lifetime of the application. It
 * also enforces strong guarantees that a flag and parameter cannot change its value for the
 * lifetime of the application. It also guarantees that in configurable environments (such as tests)
 * that flags and parameters cannot be used before they're properly initialized (to avoid test state
 * synchronization race conditions).
 */
class PlatformParameterProcessState {
  // TODO(#5835): Add tests for this class.

  private lateinit var platformParameters: Map<PlatformParameterId, PlatformParameterValue>
  private lateinit var featureFlags: Map<FeatureFlagId, Boolean>
  private lateinit var featureFlagSyncStatuses: Map<FeatureFlagId, SyncStatus>

  /**
   * Initializes platform parameter states for the lifetime of the application.
   *
   * The provided values should already consider remote values, if any are present.
   *
   * This method can only be called once and must be called before attempting to retrieve any
   * parameter values.
   */
  fun initializePlatformParameters(states: Map<PlatformParameterId, PlatformParameterValue>) {
    check(!::platformParameters.isInitialized) {
      "Attempting to initialize platform parameter states twice."
    }
    platformParameters = states
  }

  /**
   * Initializes feature flag states for the lifetime of the application.
   *
   * The provided values should already consider remote values, if any are present.
   *
   * This method can only be called once and must be called before attempting to retrieve any
   * feature flag enabled states.
   */
  fun initializeFeatureFlags(states: Map<FeatureFlagId, Boolean>) {
    check(!::featureFlags.isInitialized) { "Attempting to initialize feature flag states twice." }
    featureFlags = states
  }

  /**
   * Initializes feature flag sync statuses for the lifetime of the application.
   *
   * This method can only be called once and must be called before attempting to retrieve any
   * feature flag sync statuses.
   */
  fun initializeFeatureFlagSyncStatuses(syncStatuses: Map<FeatureFlagId, SyncStatus>) {
    check(!::featureFlagSyncStatuses.isInitialized) {
      "Attempting to initialize feature flag sync statuses twice."
    }
    featureFlagSyncStatuses = syncStatuses
  }

  /**
   * Returns the boolean value for the specified platform parameter given by [id].
   *
   * The value returned by this method is guaranteed to be constant for the lifetime of the
   * application. Also, an exception will be thrown if any of the following occur:
   * - This method is called before [initializePlatformParameters].
   * - If the specified [id] is invalid.
   * - If the specified [id] was missing during initialization (which would indicate an
   *   initialization issue).
   * - If the specified [id] does not correspond to a boolean platform parameter.
   */
  fun retrievePlatformParameterBooleanState(id: PlatformParameterId): Boolean {
    // TODO(#5835): Update this & the other init error messages below to reference OppiaTestRule.
    check(::platformParameters.isInitialized) {
      "Attempting to access platform parameter $id before initialization." +
        " If this is a test, is it using PlatformParameterTestModule?"
    }
    val value = platformParameters.getValue(id)
    check(value.valueTypeCase == PlatformParameterValue.ValueTypeCase.BOOLEAN) {
      "Expected a value of type boolean for parameter $id, but found: $value."
    }
    return value.boolean
  }

  /**
   * Returns the integer value for the specified platform parameter given by [id].
   *
   * This method provides the same guarantees as [retrievePlatformParameterBooleanState] except it
   * enforces that the platform parameter corresponding to [id] must be an integer parameter type.
   */
  fun retrievePlatformParameterIntegerState(id: PlatformParameterId): Int {
    check(::platformParameters.isInitialized) {
      "Attempting to access platform parameter $id before initialization." +
        " If this is a test, is it using PlatformParameterTestModule?"
    }
    val value = platformParameters.getValue(id)
    check(value.valueTypeCase == PlatformParameterValue.ValueTypeCase.INTEGER) {
      "Expected a value of type integer for parameter $id, but found: $value."
    }
    return value.integer
  }

  /**
   * Returns the string value for the specified platform parameter given by [id].
   *
   * This method provides the same guarantees as [retrievePlatformParameterBooleanState] except it
   * enforces that the platform parameter corresponding to [id] must be an string parameter type.
   */
  fun retrievePlatformParameterStringState(id: PlatformParameterId): String {
    check(::platformParameters.isInitialized) {
      "Attempting to access platform parameter $id before initialization." +
        " If this is a test, is it using PlatformParameterTestModule?"
    }
    val value = platformParameters.getValue(id)
    check(value.valueTypeCase == PlatformParameterValue.ValueTypeCase.STRING) {
      "Expected a value of type string for parameter $id, but found: $value."
    }
    return value.string
  }

  /**
   * Returns whether the feature flag given by [id] is currently enabled.
   *
   * The value returned by this method is guaranteed to be constant for the lifetime of the
   * application. Also, an exception will be thrown if any of the following occur:
   * - This method is called before [initializeFeatureFlags].
   * - If the specified [id] is invalid.
   * - If the specified [id] was missing during initialization (which would indicate an
   *   initialization issue).
   */
  fun retrieveFeatureFlagState(id: FeatureFlagId): Boolean {
    check(::featureFlags.isInitialized) {
      "Attempting to access feature flag $id before initialization." +
        " If this is a test, is it using PlatformParameterTestModule?"
    }
    return featureFlags.getValue(id)
  }

  /**
   * Returns the [SyncStatus] corresponding to the feature flag given by [id].
   *
   * The value returned by this method is guaranteed to be constant for the lifetime of the
   * application. Also, an exception will be thrown if any of the following occur:
   * - This method is called before [initializeFeatureFlagSyncStatuses].
   * - If the specified [id] is invalid.
   * - If the specified [id] was missing during initialization (which would indicate an
   *   initialization issue).
   */
  fun retrieveFeatureFlagSyncStatus(id: FeatureFlagId): SyncStatus {
    check(::featureFlagSyncStatuses.isInitialized) {
      "Attempting to access feature flag $id sync status before initialization." +
        " If this is a test, is it using PlatformParameterTestModule?"
    }
    return featureFlagSyncStatuses.getValue(id)
  }
}
