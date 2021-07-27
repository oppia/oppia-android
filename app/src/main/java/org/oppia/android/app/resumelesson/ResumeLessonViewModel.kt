package org.oppia.android.app.resumelesson

import androidx.databinding.ObservableField
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ChapterSummary
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.util.parser.html.ExplorationHtmlParserEntityType
import javax.inject.Inject

@FragmentScope
class ResumeLessonViewModel @Inject constructor(
  @ExplorationHtmlParserEntityType val entityType: String
) : ObservableViewModel() {

  val chapterSummary = ObservableField(ChapterSummary.getDefaultInstance())
  val explorationCheckpoint = ObservableField(ExplorationCheckpoint.getDefaultInstance())
}
