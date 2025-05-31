package org.oppia.android.domain.platformparameter

import org.oppia.android.app.model.SupportedFeatureFlags
import org.oppia.android.app.model.SupportedPlatformParameters
import org.oppia.android.util.caching.AssetRepository
import javax.inject.Inject

/** Retriever for platform parameter and feature flag definitions from the app's embedded assets. */
class PlatformParameterConfigRetrieverProdImpl @Inject constructor(
  private val assetRepository: AssetRepository
) : PlatformParameterConfigRetriever {
  override fun loadSupportedPlatformParameters(): SupportedPlatformParameters {
    return assetRepository.tryLoadProtoFromLocalAssets(
      "platform_parameters", SupportedPlatformParameters.getDefaultInstance()
    )
  }

  override fun loadSupportedFeatureFlags(): SupportedFeatureFlags {
    val featureFlagOverrides = assetRepository.tryLoadProtoFromLocalAssets(
      "feature_flags_overrides", SupportedFeatureFlags.getDefaultInstance()
    )
    return assetRepository.tryLoadProtoFromLocalAssets(
      "feature_flags", SupportedFeatureFlags.getDefaultInstance()
    ).toBuilder().mergeFrom(featureFlagOverrides).build()
  }
}
