package org.oppia.android.domain.help

import javax.inject.Inject
import org.oppia.android.app.model.LargeLicenseHashMap
import org.oppia.android.util.caching.AssetRepository


/** Controller for retrieving all aspects of a large text. */
class HelpController @Inject constructor(
  private val assetRepository: AssetRepository,
) {
   fun getLargeProtoAssetFileData(): LargeLicenseHashMap {
    return assetRepository.loadProtoFromLocalAssets(
      "large_strings",
      LargeLicenseHashMap.getDefaultInstance()
    )
  }
}