package org.oppia.domain.util

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

/** Utility that retrieves JSON assets and converts them to JSON objects. */
class JsonAssetRetriever @Inject constructor(private val context: Context) {

  /** Loads the JSON string from an asset and converts it to a JSONObject */
  fun loadJsonFromAsset(assetName: String): JSONObject? {
    val assetManager = context.assets
    val jsonContents = assetManager.open(assetName).bufferedReader().use { it.readText() }
    return JSONObject(jsonContents)
  }

  /** Returns the on-disk size of the specified asset, in bytes. */
  fun getAssetSize(assetName: String): Int {
    // Unfortunately, the entire file needs to be read to retrieve the asset size since JSON files are compressed in the
    // apk. See: https://stackoverflow.com/a/6187097.
    // TODO(#386): Use an asset retriever to prefetch and cache these to avoid needing to keep re-reading them.
    return context.assets.open(assetName).use { it.readBytes() }.size
  }

  fun getStringsFromJSONArray(jsonData: JSONArray): List<String> {
    val stringList = mutableListOf<String>()
    for (i in 0 until jsonData.length()) {
      stringList.add(jsonData.getString(i))
    }
    return stringList
  }
}
