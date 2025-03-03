package org.oppia.android.domain.platformparameter.testing

import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.app.model.FeatureFlagDefinition
import org.oppia.android.app.model.FeatureFlagId
import org.oppia.android.app.model.PlatformParameterDefinition
import org.oppia.android.app.model.PlatformParameterId
import org.oppia.android.app.model.PlatformParameterValue
import org.oppia.android.app.model.SupportedFeatureFlags
import org.oppia.android.app.model.SupportedPlatformParameters
import org.oppia.android.domain.platformparameter.PlatformParameterConfigRetriever
import org.oppia.android.domain.platformparameter.PlatformParameterConfigRetrieverProdImpl

/**
 * A test-only variant of [PlatformParameterConfigRetriever] that can be used to orchestrate
 * platform parameter and feature flag overrides in tests.
 *
 * Platform parameter and feature flag overrides must happen statically such as follows:
 *
 * ```kotlin
 * TestPlatformParameterConfigRetriever.setParameterOverride(LOWEST_SUPPORTED_API_LEVEL, 23)
 * TestPlatformParameterConfigRetriever.setFlagOverride(DOWNLOADS_SUPPORT, true)
 * ```
 *
 * After overriding, [PlatformParameterTestInitializer] can be injected to ensure that platform
 * parameters and feature flags are correctly initialized for use in production code executed by the
 * orchestrated tests.
 */
@Singleton
class TestPlatformParameterConfigRetriever @Inject constructor(
  private val prodImpl: PlatformParameterConfigRetrieverProdImpl
): PlatformParameterConfigRetriever {
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

    fun setParameterOverride(id: PlatformParameterId, value: Boolean) {
      check(!isFrozen.get()) { "Cannot override a platform parameter after values are loaded." }
      paramOverrides += id to PlatformParameterValue.newBuilder().setBoolean(value).build()
    }

    fun setParameterOverride(id: PlatformParameterId, value: Int) {
      check(!isFrozen.get()) { "Cannot override a platform parameter after values are loaded." }
      paramOverrides += id to PlatformParameterValue.newBuilder().setInteger(value).build()
    }

    fun setParameterOverride(id: PlatformParameterId, value: String) {
      check(!isFrozen.get()) { "Cannot override a platform parameter after values are loaded." }
      paramOverrides += id to PlatformParameterValue.newBuilder().setString(value).build()
    }

    fun setFlagOverride(id: FeatureFlagId, isEnabled: Boolean) {
      check(!isFrozen.get()) { "Cannot override a feature flag after values are loaded." }
      flagOverrides += id to isEnabled
    }

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
