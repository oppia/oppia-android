package org.oppia.app.story.storyitemviewmodel

import org.oppia.app.model.ChapterSummary
import org.oppia.app.model.LessonThumbnail

class ChapterSummaryViewModel(
  chapterSummary: ChapterSummary
): StoryItemViewModel() {
  val name: String = chapterSummary.name
  val summary: String = chapterSummary.summary
  val chapterThumbnail: LessonThumbnail = chapterSummary.chapterThumbnail
}