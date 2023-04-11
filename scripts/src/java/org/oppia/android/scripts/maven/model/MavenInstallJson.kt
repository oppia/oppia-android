package org.oppia.android.scripts.maven.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Represents a parsable a maven_install_json file.
 *
 * @property conflictResolutions an optional map of version-qualified artifact coordinates to
 *     version-qualified artifact coordinates. The keys are requested coordinates and the values are
 *     resolved coordinates, where the latter are expected to be higher versions than requested (due
 *     to competing version dependencies elsewhere in the dependency graph). This is ``null`` when
 *     there are no conflict resolutions (i.e. that all requested versions can be requested
 *     directly).
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
