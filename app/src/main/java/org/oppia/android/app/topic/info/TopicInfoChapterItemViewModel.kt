package org.oppia.android.app.topic.info

import org.oppia.android.app.viewmodel.ObservableViewModel

/** [ObservableViewModel] for the chapter recycler view of Topic Info Tab. */
class TopicInfoChapterItemViewModel(
  val index: Int,
  val chapterName: String
) : ObservableViewModel()
