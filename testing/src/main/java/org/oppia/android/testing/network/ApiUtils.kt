package org.oppia.android.testing.network

import org.json.JSONException
import org.json.JSONObject
import java.io.FileInputStream

/** A class that loads json responses for test cases */
class ApiUtils {

  companion object {
    fun getFakeJson(jsonPath: String): String {
      val assetsPath: String = "../testing/src/test/assets/network_mocks/$jsonPath"
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
