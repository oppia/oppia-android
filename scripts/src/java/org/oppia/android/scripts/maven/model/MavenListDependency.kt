package org.oppia.android.scripts.maven.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class that contains all the details relevant to a dependency that is present in
 * the Maven install manifest.
 */
@JsonClass(generateAdapter = true)
data class MavenListDependency(
  /** The name of the Maven dependency. */
  @Json(name = "coord") val coord: String,

  /** The url that points to the .jar or .aar file of the dependency. */
  @Json(name = "url") val url: String?
)
