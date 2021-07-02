package org.oppia.android.app.maven

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.oppia.android.app.maven.license.License

/** Data class for Storing details of a third-party Maven dependency. */
@JsonClass(generateAdapter = true)
data class MavenDependency(
  @Json(name = "index") val index: Int,
  @Json(name = "artifact_name") val artifactName: String,
  @Json(name = "artifact_version") val artifact_version: String,
  @Json(name = "licenses") val licensesList: List<License> = arrayListOf()
)