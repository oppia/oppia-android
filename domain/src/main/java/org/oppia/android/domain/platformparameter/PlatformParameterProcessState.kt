package org.oppia.android.domain.platformparameter

import org.oppia.android.app.model.FeatureFlagId
import org.oppia.android.app.model.PlatformParameterId
import org.oppia.android.app.model.PlatformParameterValue
import org.oppia.android.app.model.SyncStatus

class PlatformParameterProcessState {
  private lateinit var platformParameters: Map<PlatformParameterId, PlatformParameterValue>
  private lateinit var featureFlags: Map<FeatureFlagId, Boolean>
  private lateinit var featureFlagSyncStatuses: Map<FeatureFlagId, SyncStatus>

  fun initializePlatformParameters(states: Map<PlatformParameterId, PlatformParameterValue>) {
    check(!::platformParameters.isInitialized) {
      "Attempting to initialize platform parameter states twice."
    }
    platformParameters = states
  }

  fun initializeFeatureFlags(states: Map<FeatureFlagId, Boolean>) {
    check(!::featureFlags.isInitialized) { "Attempting to initialize feature flag states twice." }
    featureFlags = states
  }

  fun initializeFeatureFlagSyncStatuses(syncStatuses: Map<FeatureFlagId, SyncStatus>) {
    check(!::featureFlagSyncStatuses.isInitialized) {
      "Attempting to initialize feature flag sync statuses twice."
    }
    featureFlagSyncStatuses = syncStatuses
  }

  fun retrievePlatformParameterBooleanState(id: PlatformParameterId): Boolean {
    // TODO(#5835): Update this & the other init error messages below to reference OppiaTestRule.
    check(::platformParameters.isInitialized) {
      "Attempting to access platform parameter $id before initialization." +
        " If this is a test, is it using TestPlatformParameterModule?"
    }
    val value = platformParameters.getValue(id)
    check(value.valueTypeCase == PlatformParameterValue.ValueTypeCase.BOOLEAN) {
      "Expected a value of type boolean for parameter $id, but found: $value."
    }
    return value.boolean
  }

  fun retrievePlatformParameterIntegerState(id: PlatformParameterId): Int {
    check(::platformParameters.isInitialized) {
      "Attempting to access platform parameter $id before initialization." +
        " If this is a test, is it using TestPlatformParameterModule?"
    }
    val value = platformParameters.getValue(id)
    check(value.valueTypeCase == PlatformParameterValue.ValueTypeCase.INTEGER) {
      "Expected a value of type integer for parameter $id, but found: $value."
    }
    return value.integer
  }

  fun retrievePlatformParameterStringState(id: PlatformParameterId): String {
    check(::platformParameters.isInitialized) {
      "Attempting to access platform parameter $id before initialization." +
        " If this is a test, is it using TestPlatformParameterModule?"
    }
    val value = platformParameters.getValue(id)
    check(value.valueTypeCase == PlatformParameterValue.ValueTypeCase.STRING) {
      "Expected a value of type string for parameter $id, but found: $value."
    }
    return value.string
  }

  fun retrieveFeatureFlagState(id: FeatureFlagId): Boolean {
    check(::featureFlags.isInitialized) {
      "Attempting to access feature flag $id before initialization." +
        " If this is a test, is it using TestPlatformParameterModule?"
    }
    return featureFlags.getValue(id)
  }

  fun retrieveFeatureFlagSyncStatus(id: FeatureFlagId): SyncStatus {
    check(::featureFlagSyncStatuses.isInitialized) {
      "Attempting to access feature flag $id sync status before initialization." +
        " If this is a test, is it using TestPlatformParameterModule?"
    }
    return featureFlagSyncStatuses.getValue(id)
  }
}
