package org.oppia.android.scripts.maven.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** Data class to parse the list of Maven dependencies from the Maven install manifest file. */
@JsonClass(generateAdapter = true)
data class MavenListDependencyTree(
  /**
   * Parses the `dependency_tree` key of the Maven install manifest file that maps to an array of
   * Maven dependencies.
   */
  @Json(name = "dependency_tree") val mavenListDependencies: MavenListDependencies
)
