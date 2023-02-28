package org.oppia.android.scripts.gae.json

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GaeHint(@Json(name = "hint_content") val hintContent: GaeSubtitledHtml)
