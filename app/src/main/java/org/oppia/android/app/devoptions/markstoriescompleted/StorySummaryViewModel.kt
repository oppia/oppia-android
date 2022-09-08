package org.oppia.android.app.devoptions.markstoriescompleted

import androidx.lifecycle.ViewModel
import org.oppia.android.app.model.EphemeralStorySummary
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.translation.TranslationController

/** [ViewModel] for displaying a story summary for the recyclerView in [MarkStoriesCompletedFragment]. */
class StorySummaryViewModel(
  ephemeralStorySummary: EphemeralStorySummary,
  val isCompleted: Boolean,
  val topicId: String,
  translationController: TranslationController
) : ObservableViewModel() {
  /** The summary of the story being displayed. */
  val storySummary = ephemeralStorySummary.storySummary

  /** The localized title of the story being displayed. */
  val storyTitle by lazy {
    translationController.extractString(
      storySummary.storyTitle, ephemeralStorySummary.writtenTranslationContext
    )
  }
}
