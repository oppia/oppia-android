package org.oppia.android.app.topic.info

import org.oppia.android.app.model.StorySummary
import org.oppia.android.app.viewmodel.ObservableViewModel

/** [ObservableViewModel] for the story recycler view of Topic Info Tab. */
class TopicInfoStoryItemViewModel(
  val storySummary: StorySummary,
  val topicStoryChapterList: ArrayList<TopicInfoChapterItemViewModel>
) : ObservableViewModel()
