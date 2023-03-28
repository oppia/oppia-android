package org.oppia.android.scripts.license

import java.net.HttpURLConnection
import java.net.URL

/** Default implementation of [MavenArtifactPropertyFetcher]. */
class MavenArtifactPropertyFetcherImpl : MavenArtifactPropertyFetcher {
  override fun scrapeText(link: String): String {
    return URL(link).openStream().bufferedReader().readText()
  }

  override fun isValidArtifactFileUrl(url: String): Boolean {
    // Partial reference: https://stackoverflow.com/q/19670622/3689782.
    val connection = URL(url).openConnection() as? HttpURLConnection
    val responseCode = try {
      connection?.requestMethod = "HEAD"
      connection?.connect()
      connection?.responseCode
    } finally {
      connection?.disconnect()
    } ?: error("Failed to connect to URL: $url.")
    return responseCode == 200
  }
}
