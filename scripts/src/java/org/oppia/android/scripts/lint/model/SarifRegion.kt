package org.oppia.android.scripts.lint.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Represents the region within a file/artifact that is being mentioned by a static check result.
 *
 * Note that this class is [Comparable]. Its natural ordering is to first sort by [startLine], then
 * by [startColumn].
 *
 * @property startLine the first line within the file that corresponds to the check's findings
 * @property startColumn the first column within [startLine] that corresponds to findings
 */
@JsonClass(generateAdapter = true)
data class SarifRegion(
  @Json(name = "startLine") val startLine: Int,
  @Json(name = "startColumn") val startColumn: Int
) : Comparable<SarifRegion> {
  override fun compareTo(other: SarifRegion): Int = COMPARATOR.compare(this, other)

  private companion object {
    private val COMPARATOR = compareBy(SarifRegion::startLine).thenBy(SarifRegion::startColumn)
  }
}
