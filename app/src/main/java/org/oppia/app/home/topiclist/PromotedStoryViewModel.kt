package org.oppia.app.home.topiclist

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.home.HomeActivity
import org.oppia.app.model.PromotedStory
import org.oppia.app.viewmodel.ObservableViewModel
import javax.inject.Inject

// TODO(#283): Add download status information to promoted-story-card.

/** [ViewModel] for displaying a promoted story. */
@FragmentScope
class PromotedStoryViewModel @Inject constructor(
  private val activity: AppCompatActivity
  ) : ObservableViewModel() {
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
    (activity as HomeActivity).routeToStory(promotedStoryObservable.get()!!.storyId)
  }

  fun clickOnViewAll(@Suppress("UNUSED_PARAMETER") v: View) {
    // TODO(#282): Route to TopicPlayFragment with correct story-id and expand the chapter-list in that item.
  }
}
