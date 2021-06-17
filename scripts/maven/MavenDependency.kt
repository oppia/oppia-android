package scripts

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** Data class for Storing details of a Maven Dependency. */
@JsonClass(generateAdapter = true)
data class MavenDependency(
  @Json(name = "index") val index: Int,
  @Json(name = "artifact_name") val artifactName: String,
  @Json(name = "artifact_version") val artifact_version: String,
  @Json(name = "license_names") val licenseNames: List<String>,
  @Json(name = "license_links") val licenseLinks: List<String>
)