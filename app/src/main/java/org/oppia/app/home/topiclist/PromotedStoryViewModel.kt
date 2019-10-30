package org.oppia.app.home.topiclist

import android.util.Log
import android.view.View
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.PromotedStory
import org.oppia.app.model.TopicList
import org.oppia.app.viewmodel.ObservableViewModel
import org.oppia.domain.topic.TopicListController
import org.oppia.util.data.AsyncResult
import javax.inject.Inject

/** [ViewModel] for displaying a promoted story. */
@FragmentScope
class PromotedStoryViewModel @Inject constructor() : ObservableViewModel() {
  /**
   * The retrieved [LiveData] for retrieving topic summaries. This model should ensure only one
   * [LiveData] is used for all subsequent processed data to ensure the transformed [LiveData]s are
   * always in sync.
   */

  var promotedStoryObservable = ObservableField<PromotedStory>()

  fun setPromotedStory(promotedStory: PromotedStory) {
    promotedStoryObservable.set(promotedStory)
  }

  fun clickOnStoryTile(@Suppress("UNUSED_PARAMETER") v: View) {
    Log.d("TAG", "clickOnStoryTile")
  }
}
