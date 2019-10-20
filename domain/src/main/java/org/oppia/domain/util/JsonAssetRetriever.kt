package org.oppia.domain.util

import android.content.Context
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject

/** Utility that helps retrieving JSON assets and converting them to a JSON object. */
class JsonAssetRetriever @Inject constructor(private val context: Context) {

  /** Loads the JSON string from an asset and converts it to a JSONObject */
  @Throws(IOException::class)
  fun loadJsonFromAsset(assetName: String): JSONObject? {
    val assetManager = context.assets
    val jsonContents = assetManager.open(assetName).bufferedReader().use { it.readText() }
    return JSONObject(jsonContents)
  }
}