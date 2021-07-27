package org.oppia.android.app.resumelesson

import androidx.databinding.ObservableField
import javax.inject.Inject
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ChapterSummary
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.util.parser.html.ExplorationHtmlParserEntityType

@FragmentScope
class ResumeLessonViewModel @Inject constructor(
  @ExplorationHtmlParserEntityType val entityType: String
): ObservableViewModel() {

  val chapterSummary = ObservableField(ChapterSummary.getDefaultInstance())
  val explorationCheckpoint = ObservableField(ExplorationCheckpoint.getDefaultInstance())
}