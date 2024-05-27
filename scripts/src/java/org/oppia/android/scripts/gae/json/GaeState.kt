package org.oppia.android.scripts.gae.json

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GaeState(
  @Json(name = "content") val content: GaeSubtitledHtml,
  @Json(name = "param_changes") val paramChanges: List<GaeParamChange>,
  @Json(name = "interaction") val interaction: GaeInteractionInstance,
  @Json(name = "classifier_model_id") val classifierModelId: String?,
  @Json(name = "linked_skill_id") val linkedSkillId: String?,
  @Json(name = "recorded_voiceovers") val recordedVoiceovers: GaeRecordedVoiceovers,
  @Json(name = "solicit_answer_details") val solicitAnswerDetails: Boolean,
  @Json(name = "card_is_checkpoint") val cardIsCheckpoint: Boolean
) {
  fun computeReferencedSkillIds(): List<String> =
    listOfNotNull(linkedSkillId) + interaction.computeReferencedSkillIds()
}
