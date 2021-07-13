package org.oppia.android.scripts.maven

/**
 * Utility class to manage networking and Bazel query components for updating
 * maven_dependencies.textproto that contains the list of third-party dependencies and their
 * license link details.
 */
interface NetworkAndBazelUtils {
  /** Scrapes and returns the text from a given URL. */
  fun scrapeText(link: String): String

  /** Returns the list of third-party dependencies on which Oppia Android depends. */
  fun retrieveThirdPartyMavenDependenciesList(rootPath: String): List<String>
}
