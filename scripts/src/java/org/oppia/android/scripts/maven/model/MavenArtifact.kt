package org.oppia.android.scripts.maven.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Represents a single artifact contained within a maven_install_json file.
 *
 * @property version the self-reported version of the artifact
 */
@JsonClass(generateAdapter = true)
data class MavenArtifact(@Json(name = "version") val version: String)
