package org.oppia.android.scripts.license

/** Utility class to extract the privacy policy date text from a given URL. */
interface PrivacyPolicyDateFetcher {
  /** Scrapes and returns the text from a given URL. */
  fun scrapeText(link: String): String
}
