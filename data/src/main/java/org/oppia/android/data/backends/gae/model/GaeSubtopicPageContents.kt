package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for SubtopicPageContents model
 * @link https://github.com/oppia/oppia/blob/develop/core/domain/subtopic_page_domain.py#L80
 */
@JsonClass(generateAdapter = true)
data class GaeSubtopicPageContents(

  @Json(name = "subtitled_html") val content: GaeSubtitledHtml?,
  @Json(name = "recorded_voiceovers") val recordedVoiceovers: GaeRecordedVoiceovers?,
  @Json(name = "written_translations") val writtenTranslations: GaeWrittenTranslations?

)
