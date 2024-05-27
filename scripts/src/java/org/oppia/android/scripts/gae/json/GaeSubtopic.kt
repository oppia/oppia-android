package org.oppia.android.scripts.gae.json

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GaeSubtopic(
  @Json(name = "id") val id: Int,
  @Json(name = "title") val title: String,
  @Json(name = "skill_ids") val skillIds: List<String>,
  @Json(name = "thumbnail_filename") val thumbnailFilename: String?,
  @Json(name = "thumbnail_bg_color") val thumbnailBgColor: String?,
  @Json(name = "thumbnail_size_in_bytes") val thumbnailSizeInBytes: Int?,
  @Json(name = "url_fragment") val urlFragment: String
)
