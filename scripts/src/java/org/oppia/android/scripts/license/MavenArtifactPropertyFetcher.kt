package org.oppia.android.scripts.license

/** Utility class to extract specific Maven artifact properties from remote sources. */
interface MavenArtifactPropertyFetcher {
  /**
   * Scrapes and returns the text from a given URL, or null if the link failed to be resolved. Note
   * that a failure in this case can indicate a network flake and doesn't necessarily mean that the
   * link is invalid.
   */
  fun scrapeText(link: String): String?

  /** Returns whether the specified URL corresponds to a real Maven artifact file. */
  fun isValidArtifactFileUrl(url: String): Boolean
}
