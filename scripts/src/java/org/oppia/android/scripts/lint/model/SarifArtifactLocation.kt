package org.oppia.android.scripts.lint.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Represents the location of a specific artifact mentioned in the output of a static check result.
 *
 * Note that this class is [Comparable]. Its natural order is that of its [uri] (see
 * [String.compareTo]).
 *
 * @property uri the plain-text URI corresponding to the artifact
 */
@JsonClass(generateAdapter = true)
data class SarifArtifactLocation(
  @Json(name = "uri") val uri: String
) : Comparable<SarifArtifactLocation> {
  override fun compareTo(other: SarifArtifactLocation): Int = COMPARATOR.compare(this, other)

  private companion object {
    private val COMPARATOR = compareBy(SarifArtifactLocation::uri)
  }
}
