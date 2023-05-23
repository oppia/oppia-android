package org.oppia.android.scripts.lint.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Represents the physical location of a file mentioned in the results of a static check.
 *
 * Note that this class is [Comparable]. Its natural ordering is to first sort by its
 * [artifactLocation], then by its [region].
 *
 * @property artifactLocation the [SarifArtifactLocation] of the affected file
 * @property region the specific [SarifRegion] mentioned in the check results
 */
@JsonClass(generateAdapter = true)
data class SarifPhysicalLocation(
  @Json(name = "artifactLocation") val artifactLocation: SarifArtifactLocation,
  @Json(name = "region") val region: SarifRegion
) : Comparable<SarifPhysicalLocation> {
  override fun compareTo(other: SarifPhysicalLocation): Int = COMPARATOR.compare(this, other)

  private companion object {
    private val COMPARATOR = compareBy(SarifPhysicalLocation::artifactLocation).thenBy { it.region }
  }
}
