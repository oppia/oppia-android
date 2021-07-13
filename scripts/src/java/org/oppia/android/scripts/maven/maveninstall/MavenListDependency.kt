package org.oppia.android.scripts.maven.maveninstall

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class that contains all the details relevant to a dependency that is present in
 * maven_install.json.
 */
@JsonClass(generateAdapter = true)
data class MavenListDependency(
  @Json(name = "coord") val coord: String,
  @Json(name = "url") val url: String?
)
