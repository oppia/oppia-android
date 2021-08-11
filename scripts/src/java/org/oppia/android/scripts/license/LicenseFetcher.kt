package org.oppia.android.scripts.license

/** Utility class to extract the license text from a given URL. */
interface LicenseFetcher {
  /** Scrapes and returns the text from a given URL. */
  fun scrapeText(link: String): String
}
