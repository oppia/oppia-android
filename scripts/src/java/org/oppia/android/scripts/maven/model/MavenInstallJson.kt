package org.oppia.android.scripts.maven.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Represents a parsable a maven_install_json file.
 *
 * @property artifacts a map of artifact coordinate to [MavenArtifact]
 * @property repositories a map of repository URL to a list of [MavenArtifact] coordinates
 */
@JsonClass(generateAdapter = true)
data class MavenInstallJson(
  @Json(name = "artifacts") val artifacts: Map<String, MavenArtifact>,
  @Json(name = "repositories") val repositories: Map<String, List<String>>
)
