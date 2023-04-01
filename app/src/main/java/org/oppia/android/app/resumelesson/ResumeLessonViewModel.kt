package org.oppia.android.app.resumelesson

import androidx.databinding.ObservableField
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ChapterSummary
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.util.parser.html.ExplorationHtmlParserEntityType
import javax.inject.Inject

/**
 * The viewModel that provides a [ChapterSummary] and [ExplorationCheckpoint] for the exploration
 * being resumed.
 */
@FragmentScope
class ResumeLessonViewModel @Inject constructor(
  @ExplorationHtmlParserEntityType val entityType: String
) : ObservableViewModel() {

  /** The chapter summary for the exploration that may be resumed. */
  val chapterSummary = ObservableField(ChapterSummary.getDefaultInstance())

  /** The title of the chapter/exploration being resumed. */
  val chapterTitle = ObservableField<String>()

  /** The [ExplorationCheckpoint] that may be used to resume the exploration. */
  val explorationCheckpoint = ObservableField(ExplorationCheckpoint.getDefaultInstance())
}
