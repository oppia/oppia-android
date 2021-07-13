package org.oppia.android.scripts.maven

import org.oppia.android.scripts.common.BazelClient
import java.io.File
import java.net.URL

/** Default Implementation of [NetworkAndBazelUtils]. */
class NetworkAndBazelUtilsImpl() : NetworkAndBazelUtils {

  override fun scrapeText(link: String): String {
    return URL(link).openStream().bufferedReader().readText()
  }

  override fun retrieveThirdPartyMavenDependenciesList(rootPath: String): List<String> {
    return BazelClient(File(rootPath).absoluteFile).retrieveThirdPartyMavenDependenciesList()
  }
}
