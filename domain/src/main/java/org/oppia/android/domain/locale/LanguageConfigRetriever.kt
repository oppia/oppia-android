package org.oppia.android.domain.locale

import javax.inject.Inject
import org.oppia.android.app.model.SupportedLanguages
import org.oppia.android.app.model.SupportedRegions
import org.oppia.android.util.caching.AssetRepository

class LanguageConfigRetriever @Inject constructor(private val assetRepository: AssetRepository) {
  fun loadSupportedLanguages(): SupportedLanguages {
    return assetRepository.tryLoadProtoFromLocalAssets(
      "supported_languages", SupportedLanguages.getDefaultInstance()
    )
  }

  fun loadSupportedRegions(): SupportedRegions {
    return assetRepository.tryLoadProtoFromLocalAssets(
      "supported_regions", SupportedRegions.getDefaultInstance()
    )
  }
}
