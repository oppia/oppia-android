package org.oppia.android.testing.network

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import java.io.File
import java.io.InputStream

/** Utility for loading mock data for tests. */
class ApiMockLoader {

  companion object {
    private const val GRADLE_ASSET_PATH = "../data/src/test/assets/api_mocks"
    private const val BAZEL_ASSET_PATH = "api_mocks"

    /** Returns the JSON content for the API mock data corresponding to the specified path. */
    fun getFakeJson(jsonPath: String): String {
      return openAssetInputStream(jsonPath).bufferedReader().use { it.readText() }
    }

    private fun openAssetInputStream(jsonPath: String): InputStream {
      // TODO(#59): Only use Bazel path instead of checking.
      val gradleAssetFile = File(GRADLE_ASSET_PATH, jsonPath)
      return if (gradleAssetFile.exists()) {
        gradleAssetFile.inputStream()
      } else {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return context.assets.open("$BAZEL_ASSET_PATH/$jsonPath")
      }
    }
  }
}
