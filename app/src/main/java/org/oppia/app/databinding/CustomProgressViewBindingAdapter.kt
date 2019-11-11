package org.oppia.app.databinding

import androidx.databinding.BindingAdapter
import org.oppia.app.customview.CustomProgressView

/**
 * BindingAdapter for [CustomProgressView].
 */
@BindingAdapter("app:totalChapters", "app:chaptersFinished")
fun storyProgress(storyProgressView: CustomProgressView, totalChaptersCount: Int?, chaptersFinishedCount: Int?) {
  if (totalChaptersCount != null && chaptersFinishedCount != null && totalChaptersCount != 0) {
    storyProgressView.setStoryChapterDetails(totalChaptersCount, chaptersFinishedCount)
  }
}
