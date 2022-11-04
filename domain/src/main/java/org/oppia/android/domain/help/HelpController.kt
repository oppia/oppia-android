package org.oppia.android.domain.help

import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.app.model.LargeLicenseHashMap
import org.oppia.android.util.caching.AssetRepository

/** Controller for retrieving all aspects of a large text. */
@Singleton
class TopicController @Inject constructor(
  private val assetRepository: AssetRepository,
) {
  private fun getLargeProtoAssetFileData(): String {
    return assetRepository.loadProtoFromLocalAssets(
      "large_strings",
      LargeLicenseHashMap.getDefaultInstance()
    )
  }
}