package org.oppia.app.story.storyitemviewmodel

import android.view.View
import org.oppia.app.model.ChapterSummary
import org.oppia.app.model.LessonThumbnail
import org.oppia.app.story.StoryActivity

class StoryChapterSummaryViewModel(
  chapterSummary: ChapterSummary
) : StoryItemViewModel() {
  val id: String = chapterSummary.explorationId
  val name: String = chapterSummary.name
  val summary: String = chapterSummary.summary
  val chapterThumbnail: LessonThumbnail = chapterSummary.chapterThumbnail

  fun onChapterClicked(v: View) {
    StoryActivity.routeToExploration(id)
  }
}
