package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for SubtopicPageContents model
 * https://github.com/oppia/oppia/blob/b33aa9/core/domain/subtopic_page_domain.py#L112
 */
@JsonClass(generateAdapter = true)
data class GaeSubtopicPageContents(

  @Json(name = "subtitled_html") val content: GaeSubtitledHtml?,
  @Json(name = "recorded_voiceovers") val recordedVoiceovers: GaeRecordedVoiceovers?,
  @Json(name = "written_translations") val writtenTranslations: GaeWrittenTranslations?

)
