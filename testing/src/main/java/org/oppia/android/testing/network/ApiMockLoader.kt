package org.oppia.android.testing.network

import java.io.FileInputStream

/** Utility for loading mock data for tests. */
class ApiMockLoader {

  companion object {
    /** Returns the JSON content for the API mock data corresponding to the specified path. */
    fun getFakeJson(jsonPath: String): String {
      val assetsPath = "../data/src/test/assets/api_mocks/$jsonPath"
      val inputStream = FileInputStream(assetsPath)
      return inputStream.bufferedReader().use { it.readText() }
    }
  }
}
