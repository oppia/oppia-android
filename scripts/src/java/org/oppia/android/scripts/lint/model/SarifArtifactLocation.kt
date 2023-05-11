package org.oppia.android.scripts.lint.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SarifArtifactLocation(
  @Json(name = "uri") val uri: String
): Comparable<SarifArtifactLocation> {
  override fun compareTo(other: SarifArtifactLocation): Int = COMPARATOR.compare(this, other)

  private companion object {
    private val COMPARATOR = compareBy(SarifArtifactLocation::uri)
  }
}
