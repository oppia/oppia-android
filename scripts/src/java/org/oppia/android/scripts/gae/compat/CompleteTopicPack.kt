package org.oppia.android.scripts.gae.compat

import org.oppia.android.scripts.gae.json.GaeSkill
import org.oppia.android.scripts.gae.json.GaeStory
import org.oppia.android.scripts.gae.json.GaeSubtopicPage
import org.oppia.android.scripts.gae.json.GaeTopic
import org.oppia.proto.v1.structure.LanguageType
import org.oppia.proto.v1.structure.SubtopicPageIdDto

data class CompleteTopicPack(
  val topic: GaeTopic,
  val subtopicPages: Map<SubtopicPageIdDto, GaeSubtopicPage>,
  val stories: Map<String, GaeStory>,
  val explorations: Map<String, CompleteExploration>,
  val referencedSkills: Map<String, GaeSkill>,
  val defaultLanguage: LanguageType
)
