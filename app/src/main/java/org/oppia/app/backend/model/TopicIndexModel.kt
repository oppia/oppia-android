package org.oppia.app.backend.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TopicIndexModel(

        @Json(name = "thumbnail_bg_color") val thumbnail_bg_color: String?,
        @Json(name = "thumbnail_icon_url") val thumbnail_icon_url: String?,
        @Json(name = "language_code") val language_code: String?,
        @Json(name = "id") val id: String?,
        @Json(name = "category") val category: String?,
        @Json(name = "title") val title: String?,
        @Json(name = "objective") val objective: String?,
        @Json(name = "num_of_lessons") val num_of_lessons: String?,
        @Json(name = "last_updated_msec") val last_updated_msec : Double?

)
