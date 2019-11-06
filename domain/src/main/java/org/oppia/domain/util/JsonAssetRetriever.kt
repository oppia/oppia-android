package org.oppia.domain.util

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject

/** Utility that retrieves JSON assets and converts them to JSON objects. */
class JsonAssetRetriever @Inject constructor(private val context: Context) {

  /** Loads the JSON string from an asset and converts it to a JSONObject */
  fun loadJsonFromAsset(assetName: String): JSONObject? {
    val assetManager = context.assets
    val jsonContents = assetManager.open(assetName).bufferedReader().use { it.readText() }
    return JSONObject(jsonContents)
  }

  fun getStringsFromJSONArray(jsonData: JSONArray): List<String> {
    val stringList = mutableListOf<String>()
    for (i in 0 until jsonData.length()) {
      stringList.add(jsonData.getString(i))
    }
    return stringList
  }
}
