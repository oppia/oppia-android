package org.oppia.android.scripts.maven.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Represents a parsable a maven_install_json file.
 *
 * @property artifacts a map of artifact coordinate to [MavenArtifact]
 * @property dependencies a map of artifact coordinates to a list of artifact coordinates upon which
 *     that artifact depends
 * @property repositories a map of repository URL to a list of [MavenArtifact] coordinates
 */
@JsonClass(generateAdapter = true)
data class MavenInstallJson(
  @Json(name = "conflict_resolution") val conflictResolutions: Map<String, String>?,
  @Json(name = "artifacts") val artifacts: Map<String, MavenArtifact>,
  @Json(name = "dependencies") val dependencies: Map<String, List<String>>,
  @Json(name = "repositories") val repositories: Map<String, List<String>>
)
