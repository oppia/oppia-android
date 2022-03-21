package org.oppia.android.scripts.maven.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class that stores the list of dependencies present in `dependencies` array in
 * the Maven install manifest file.
 */
@JsonClass(generateAdapter = true)
data class MavenListDependencies(
  /**
   * The list of dependencies parsed from the Maven install manifest file where each dependency
   * would contain the full name of the dependency, and a url that refers to the .aar or
   * .jar file of the dependency. This url can also take us to the POM file of the dependency
   * by just repplacing the extension in the url to .pom.
   */
  @Json(name = "dependencies") val dependencyList: List<MavenListDependency>
)
