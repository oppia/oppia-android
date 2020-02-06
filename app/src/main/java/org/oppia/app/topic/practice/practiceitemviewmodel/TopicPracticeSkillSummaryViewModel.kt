package org.oppia.app.topic.practice.practiceitemviewmodel

import org.oppia.app.model.SkillSummary
import org.oppia.app.topic.practice.TopicPracticeFragment

/** Skill summary view model for the recycler view in [TopicPracticeFragment]. */
class TopicPracticeSkillSummaryViewModel(
  val skillSummary: SkillSummary
) : TopicPracticeItemViewModel()
