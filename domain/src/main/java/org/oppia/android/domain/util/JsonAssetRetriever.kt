package org.oppia.android.domain.util

import org.json.JSONArray
import org.json.JSONObject
import org.oppia.android.util.caching.AssetRepository
import javax.inject.Inject

/** Utility that retrieves JSON assets and converts them to JSON objects. */
class JsonAssetRetriever @Inject constructor(private val assetRepository: AssetRepository) {

  /** Loads the JSON string from an asset and converts it to a JSONObject */
  fun loadJsonFromAsset(assetName: String): JSONObject? {
    return JSONObject(assetRepository.loadTextFileFromLocalAssets(assetName))
  }

  /** Returns the on-disk size of the specified asset, in bytes. */
  fun getAssetSize(assetName: String): Int {
    // Unfortunately, the entire file needs to be read to retrieve the asset size since JSON files are compressed in the
    // apk. See: https://stackoverflow.com/a/6187097.
    return assetRepository.loadTextFileFromLocalAssets(assetName).toByteArray(Charsets.UTF_8).size
  }

  fun getStringsFromJSONArray(jsonData: JSONArray): List<String> {
    val stringList = mutableListOf<String>()
    for (i in 0 until jsonData.length()) {
      stringList.add(jsonData.getString(i))
    }
    return stringList
  }
}
