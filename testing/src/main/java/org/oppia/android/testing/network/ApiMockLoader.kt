package org.oppia.android.testing.network

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import java.io.InputStream

/** Utility for loading mock data for tests. */
class ApiMockLoader {

  companion object {
    /** Returns the JSON content for the API mock data corresponding to the specified path. */
    fun getFakeJson(jsonPath: String): String {
      val context = ApplicationProvider.getApplicationContext<Context>()
      return context.assets.open("api_mocks/$jsonPath").bufferedReader().use { it.readText() }
    }
  }
}
