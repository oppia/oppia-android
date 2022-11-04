package org.oppia.android.domain.help

import org.oppia.android.app.model.LargeLicenseHashMap
import org.oppia.android.util.caching.AssetRepository
import javax.inject.Inject
import javax.inject.Singleton

/** Controller for retrieving all aspects of a large text. */
@Singleton
class HelpController @Inject constructor(
  private val assetRepository: AssetRepository,
) {
  private fun getLargeProtoAssetFileData(): String {
    return assetRepository.loadProtoFromLocalAssets(
      "large_strings",
      LargeLicenseHashMap.getDefaultInstance()
    )
  }
}
