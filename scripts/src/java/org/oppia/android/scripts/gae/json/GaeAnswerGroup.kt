package org.oppia.android.scripts.gae.json

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GaeAnswerGroup(
  @Json(name = "rule_specs") val ruleSpecs: List<GaeRuleSpec>,
  @Json(name = "outcome") val outcome: GaeOutcome,
  @Json(name = "training_data")
  @GaeInteractionObject.SolutionInteractionAnswer // TODO: Document that this is wrong (and can fail if Oppia ever uses it).
  val trainingData: List<@JvmSuppressWildcards GaeInteractionObject>,
  @Json(name = "tagged_skill_misconception_id") val taggedSkillMisconceptionId: String?
) {
  fun computeReferencedSkillIds(): List<String> {
    val referencedSkillId = taggedSkillMisconceptionId?.substringBefore('-')
    return listOfNotNull(referencedSkillId, outcome.missingPrerequisiteSkillId)
  }
}
