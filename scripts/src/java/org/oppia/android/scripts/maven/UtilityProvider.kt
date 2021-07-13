package org.oppia.android.scripts.maven

import java.io.File
import org.oppia.android.scripts.maven.maveninstall.MavenListDependency
import org.oppia.android.scripts.proto.MavenDependencyList

interface UtilityProvider {
  fun scrapeText(link: String): String

  fun runBazelQueryCommand(rootPath:String, vararg args: String): List<String>
}
