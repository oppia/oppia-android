package org.oppia.android.scripts.maven

interface UtilityProvider {
  fun scrapeText(link: String): String

  fun retrieveThirdPartyMavenDependenciesList(rootPath: String): List<String>
}
