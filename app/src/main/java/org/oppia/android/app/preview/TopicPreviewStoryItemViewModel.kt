package org.oppia.android.app.preview

import org.oppia.android.app.model.StorySummary
import org.oppia.android.app.viewmodel.ObservableViewModel

/** [ObservableViewModel] for the story recycler view of Topic Preview. */
class TopicPreviewStoryItemViewModel(
  val storySummary: StorySummary,
  val topicStoryChapterList: ArrayList<TopicPreviewChapterItemViewModel>
) : ObservableViewModel()
