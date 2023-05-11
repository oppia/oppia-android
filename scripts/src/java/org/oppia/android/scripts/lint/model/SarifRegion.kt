package org.oppia.android.scripts.lint.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SarifRegion(
  @Json(name = "startLine") val startLine: Int,
  @Json(name = "startColumn") val startColumn: Int
): Comparable<SarifRegion> {
  override fun compareTo(other: SarifRegion): Int = COMPARATOR.compare(this, other)

  private companion object {
    private val COMPARATOR = compareBy(SarifRegion::startLine).thenBy(SarifRegion::startColumn)
  }
}
