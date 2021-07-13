package org.oppia.android.scripts.maven

import java.io.File
import java.net.URL
import org.oppia.android.scripts.common.BazelClient

class UtilityProviderImpl() : UtilityProvider {

  override fun scrapeText(link: String): String {
    return URL(link).openStream().bufferedReader().readText()
  }

  override fun retrieveThirdPartyMavenDependenciesList(rootPath: String): List<String> {
    return BazelClient(File(rootPath).absoluteFile).retrieveThirdPartyMavenDependenciesList()
  }
}