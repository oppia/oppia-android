package org.oppia.domain.util

import android.content.Context
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
}
