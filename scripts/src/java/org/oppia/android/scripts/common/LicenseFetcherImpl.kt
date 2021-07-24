package org.oppia.android.scripts.maven

import java.net.URL

/** Default implementation of [LicenseFetcher]. */
class LicenseFetcherImpl() : LicenseFetcher {

  override fun scrapeText(link: String): String {
    return URL(link).openStream().bufferedReader().readText()
  }
}
