package org.oppia.android.scripts.license

import java.net.HttpURLConnection
import java.net.URL
import java.net.ConnectException
import java.net.SocketTimeoutException

/** Default implementation of [MavenArtifactPropertyFetcher]. */
class MavenArtifactPropertyFetcherImpl : MavenArtifactPropertyFetcher {
  override fun scrapeText(link: String): String? {
    return try {
      URL(link).openStream().bufferedReader().readText()
    } catch (e: ConnectException) { null }
  }

  override fun isValidArtifactFileUrl(url: String): Boolean {
    // Partial reference: https://stackoverflow.com/q/19670622/3689782.
    val connection = URL(url).openConnection() as? HttpURLConnection
    val responseCode = try {
      connection?.requestMethod = "HEAD"
      connection?.setConnectTimeout(5_000)
      connection?.connect()
      connection?.responseCode
    } catch (e: ConnectException) {
      return false
    } catch (e: SocketTimeoutException) {
      return false
    } finally {
      connection?.disconnect()
    } ?: error("Failed to connect to URL: $url.")
    return responseCode == 200
  }
}
