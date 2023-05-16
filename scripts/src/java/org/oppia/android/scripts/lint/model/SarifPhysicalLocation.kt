package org.oppia.android.scripts.lint.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

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
