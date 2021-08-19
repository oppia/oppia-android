package org.oppia.android.scripts.maven.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** Data class to parse the list of maven dependencies from maven_install.json. */
@JsonClass(generateAdapter = true)
data class MavenListDependencyTree(
  /**
   * Parses the `dependeny_tree` key of the maven_install.json file that maps to an array of
   * maven dependencies.
   */
  @Json(name = "dependency_tree") val mavenListDependencies: MavenListDependencies
)
