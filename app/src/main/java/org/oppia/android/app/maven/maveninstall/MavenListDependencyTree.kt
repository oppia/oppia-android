package org.oppia.android.app.maven.maveninstall

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** Data class to parse the list of mavenListDependencies from maven_install.json. */
@JsonClass(generateAdapter = true)
data class MavenListDependencyTree(

  @Json(name = "dependency_tree") val mavenListDependencies: MavenListDependencies

)
