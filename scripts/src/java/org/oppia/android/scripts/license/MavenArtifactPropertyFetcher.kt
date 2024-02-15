package org.oppia.android.scripts.license

/** Utility class to extract specific Maven artifact properties from remote sources. */
interface MavenArtifactPropertyFetcher {
  // TODO: Update docs.
  /** Scrapes and returns the text from a given URL. */
  fun scrapeText(link: String): String?

  /** Returns whether the specified URL corresponds to a real Maven artifact file. */
  fun isValidArtifactFileUrl(url: String): Boolean
}
