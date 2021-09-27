package org.oppia.android.scripts.license

import java.net.URL

/** Default implementation of [PrivacyPolicyDateFetcher]. */
class PrivacyPolicyDateFetcherImpl() : PrivacyPolicyDateFetcher {

  override fun scrapeText(link: String): String {
    return URL(link).openStream().bufferedReader().readText()
  }
}
