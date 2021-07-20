package org.oppia.android.scripts.maven.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class that contains all the details relevant to a dependency that is present in
 * maven_install.json.
 */
@JsonClass(generateAdapter = true)
data class MavenListDependency(
  /** The name of the maven dependency. */
  @Json(name = "coord") val coord: String,

  /** The url that points to the .jar or .aar file of the dependeny. */
  @Json(name = "url") val url: String?
)
