package org.oppia.android.scripts.maven

interface NetworkAndBazelUtils {
  fun scrapeText(link: String): String

  fun retrieveThirdPartyMavenDependenciesList(rootPath: String): List<String>
}
