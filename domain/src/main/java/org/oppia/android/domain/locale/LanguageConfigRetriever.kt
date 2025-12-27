package org.oppia.android.domain.locale

import org.oppia.android.app.model.SupportedLanguages
import org.oppia.android.app.model.SupportedRegions
import org.oppia.android.util.caching.AssetRepository
import javax.inject.Inject

/** Retriever for language configurations from the app's embedded assets. */
class LanguageConfigRetriever @Inject constructor(private val assetRepository: AssetRepository) {
  /** Returns the [SupportedLanguages] configuration for the app, or default instance if none. */
  fun loadSupportedLanguages(): SupportedLanguages {
    return assetRepository.tryLoadProtoFromLocalAssets(
      "supported_languages", SupportedLanguages.getDefaultInstance()
    )
  }

  /** Returns the [SupportedRegions] configuration for the app, or default instance if none. */
  fun loadSupportedRegions(): SupportedRegions {
    return assetRepository.tryLoadProtoFromLocalAssets(
      "supported_regions", SupportedRegions.getDefaultInstance()
    )
  }
}
