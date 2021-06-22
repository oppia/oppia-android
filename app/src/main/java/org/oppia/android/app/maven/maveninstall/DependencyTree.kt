package org.oppia.android.app.maven.maveninstall

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** Helper Data class to parse the list of dependencies from maven_install.json. */
@JsonClass(generateAdapter = true)
data class DependencyTree(
  @Json(name = "dependency_tree") val dependencies: Dependencies
)