package org.oppia.data.backends

import org.json.JSONObject
import org.json.JSONException
import java.io.FileInputStream

/** A class that loads json responses for test cases */
class ApiUtils {

  companion object {
    fun getFakeJson(jsonPath: String): String {
      val assetsPath: String = "../data/src/test/assets/api_mocks/$jsonPath"
      val inputStream = FileInputStream(assetsPath)
      val json: String = inputStream.bufferedReader().use { it.readText() }
      return try {
        JSONObject(json).toString()
      } catch (e: JSONException) {
        return json
      }
    }
  }
}
