package org.oppia.android.domain.platformparameter.testing

import org.oppia.android.app.model.FeatureFlagDefinition
import org.oppia.android.app.model.FeatureFlagId
import org.oppia.android.app.model.PlatformParameterDefinition
import org.oppia.android.app.model.PlatformParameterId
import org.oppia.android.app.model.PlatformParameterValue
import org.oppia.android.app.model.SupportedFeatureFlags
import org.oppia.android.app.model.SupportedPlatformParameters
import org.oppia.android.domain.platformparameter.PlatformParameterConfigRetriever
import org.oppia.android.domain.platformparameter.PlatformParameterConfigRetrieverProdImpl
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A test-only variant of [PlatformParameterConfigRetriever] that can be used to orchestrate
 * platform parameter and feature flag overrides in tests.
 *
 * Platform parameter and feature flag overrides must happen statically, e.g.:
 *
 * ```kotlin
 * TestPlatformParameterConfigRetriever.setParameterOverride(LOWEST_SUPPORTED_API_LEVEL, 23)
 * TestPlatformParameterConfigRetriever.setFlagOverride(DOWNLOADS_SUPPORT, true)
 * ```
 *
 * After overriding, parameters can be properly synchronized ahead of attempting parameter or flag
 * injection to ensure that the overridden values are properly prepared for production code use.
 *
 * This class requires the presence of
 * [org.oppia.android.testing.platformparameter.PlatformParameterTestModule] in order to function
 * correctly.
 */
@Singleton
class TestPlatformParameterConfigRetriever @Inject constructor(
  private val prodImpl: PlatformParameterConfigRetrieverProdImpl
) : PlatformParameterConfigRetriever {
  // TODO(#5835): Add tests for this class.

  override fun loadSupportedPlatformParameters(): SupportedPlatformParameters {
    isFrozen.set(true)
    val baseParams = prodImpl.loadSupportedPlatformParameters()
    val overrides = computeParameterOverrideProto(baseParams)
    return baseParams.toBuilder().mergeFrom(overrides).build()
  }

  override fun loadSupportedFeatureFlags(): SupportedFeatureFlags {
    isFrozen.set(true)
    val baseFlags = prodImpl.loadSupportedFeatureFlags()
    val overrides = computeFlagOverrideProto(baseFlags)
    return baseFlags.toBuilder().mergeFrom(overrides).build()
  }

  companion object {
    private val paramOverrides =
      CopyOnWriteArrayList<Pair<PlatformParameterId, PlatformParameterValue>>()
    private val flagOverrides = CopyOnWriteArrayList<Pair<FeatureFlagId, Boolean>>()
    private val isFrozen = AtomicBoolean()

    /**
     * Sets an override value of [value] for the platform parameter corresponding to [id].
     *
     * This method throws an exception if it's called more than once for the same parameter until
     * [reset] is called.
     *
     * This cannot be called after platform parameters have been loaded.
     */
    fun setParameterOverride(id: PlatformParameterId, value: Boolean) {
      check(!isFrozen.get()) { "Cannot override a platform parameter after values are loaded." }
      paramOverrides += id to PlatformParameterValue.newBuilder().setBoolean(value).build()
    }

    /**
     * Sets an override value of [value] for the platform parameter corresponding to [id].
     *
     * This method throws an exception if it's called more than once for the same parameter until
     * [reset] is called.
     *
     * This cannot be called after platform parameters have been loaded.
     */
    fun setParameterOverride(id: PlatformParameterId, value: Int) {
      check(!isFrozen.get()) { "Cannot override a platform parameter after values are loaded." }
      paramOverrides += id to PlatformParameterValue.newBuilder().setInteger(value).build()
    }

    /**
     * Sets an override value of [value] for the platform parameter corresponding to [id].
     *
     * This method throws an exception if it's called more than once for the same parameter until
     * [reset] is called.
     *
     * This cannot be called after platform parameters have been loaded.
     */
    fun setParameterOverride(id: PlatformParameterId, value: String) {
      check(!isFrozen.get()) { "Cannot override a platform parameter after values are loaded." }
      paramOverrides += id to PlatformParameterValue.newBuilder().setString(value).build()
    }

    /**
     * Sets an override state of [isEnabled] for the feature flag corresponding to [id].
     *
     * This method throws an exception if it's called more than once for the same flag until [reset]
     * is called.
     *
     * This cannot be called after feature flags have been loaded.
     */
    fun setFlagOverride(id: FeatureFlagId, isEnabled: Boolean) {
      check(!isFrozen.get()) { "Cannot override a feature flag after values are loaded." }
      flagOverrides += id to isEnabled
    }

    /**
     * Clears all platform parameter and feature flag overrides, reenabling more overrides to happen
     * again.
     *
     * Note that it's possible to call this after parameters and flags have been loaded. Doing so
     * may have unpredictable effects in tests, and thus should only be done carefully and when
     * necessary (such as when testing code in a 'previous' application instance).
     */
    fun reset() {
      paramOverrides.clear()
      flagOverrides.clear()
      isFrozen.set(false)
    }

    private fun computeParameterOverrideProto(
      baseParams: SupportedPlatformParameters
    ): SupportedPlatformParameters {
      val paramsById = baseParams.platformParameterDefinitionList.associateBy { it.id }
      val overridesById = paramOverrides.groupBy { (id, _) -> id }.mapValues { (id, pairs) ->
        val (_, value) = checkNotNull(pairs.singleOrNull()) {
          "Expected exactly one override for parameter: $id."
        }
        return@mapValues value
      }
      return SupportedPlatformParameters.newBuilder().apply {
        val definitions = overridesById.map { (id, value) ->
          val baseParam = paramsById[id]
          checkNotNull(baseParam) { "Attempting to override unknown parameter: $id." }
          val expectedType = baseParam.defaultValue.valueTypeCase
          check(expectedType == value.valueTypeCase) {
            "Using type ${value.valueTypeCase} for parameter $id but expected type: $expectedType."
          }
          PlatformParameterDefinition.newBuilder().apply {
            this.id = id
            this.defaultValue = value
          }.build()
        }
        addAllPlatformParameterDefinition(definitions)
      }.build()
    }

    private fun computeFlagOverrideProto(baseFlags: SupportedFeatureFlags): SupportedFeatureFlags {
      val flagsById = baseFlags.featureFlagDefinitionList.associateBy { it.id }
      val overridesById = flagOverrides.groupBy { (id, _) -> id }.mapValues { (id, pairs) ->
        val (_, value) = checkNotNull(pairs.singleOrNull()) {
          "Expected exactly one override for flag: $id."
        }
        return@mapValues value
      }
      return SupportedFeatureFlags.newBuilder().apply {
        val definitions = overridesById.map { (id, value) ->
          val baseFlag = flagsById[id]
          checkNotNull(baseFlag) { "Attempting to override unknown flag: $id." }
          FeatureFlagDefinition.newBuilder().apply {
            this.id = id
            this.defaultIsEnabled = value
          }.build()
        }
        addAllFeatureFlagDefinition(definitions)
      }.build()
    }
  }
}
