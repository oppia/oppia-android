package org.oppia.android.domain.platformparameter

@Singleton
class PlatformParameterProcessState @Inject constructor() {
  private lateinit var platformParameters: Map<PlatformParameterId, PlatformParameterValue>
  private lateinit var featureFlags: Map<FeatureFlagId, Boolean>

  fun initializePlatformParameters(states: Map<PlatformParameterId, PlatformParameterValue>) {
    check(!::platformParameters.isInitialized) {
      "Attempting to initialize platform parameter states twice."
    }
  }

  fun initializeFeatureFlags(states: Map<FeatureFlagId, Boolean>) {
    check(!::featureFlags.isInitialized) { "Attempting to initialize feature flag states twice." }
  }

  fun retrievePlatformParameterBooleanState(id: PlatformParameterId): Boolean {
    val value = platformParameters.getValue(id)
    check(value.valueTypeCase == PlatformParameterValue.ValueTypeCase.BOOLEAN) {
      "Expected a value of type boolean for parameter $id, but found: $value."
    }
    return value.boolean
  }

  fun retrievePlatformParameterIntegerState(id: PlatformParameterId): Int {
    val value = platformParameters.getValue(id)
    check(value.valueTypeCase == PlatformParameterValue.ValueTypeCase.INTEGER) {
      "Expected a value of type integer for parameter $id, but found: $value."
    }
    return value.integer
  }

  fun retrievePlatformParameterStringState(id: PlatformParameterId): String {
    val value = platformParameters.getValue(id)
    check(value.valueTypeCase == PlatformParameterValue.ValueTypeCase.STRING) {
      "Expected a value of type string for parameter $id, but found: $value."
    }
    return value.string
  }

  fun retrieveFeatureFlagState(id: FeatureFlagId): Boolean = featureFlags.getValue(id)
}
