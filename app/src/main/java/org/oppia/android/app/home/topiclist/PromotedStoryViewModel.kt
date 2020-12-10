package org.oppia.android.app.home.topiclist

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.view.ViewGroup
import android.view.animation.Transformation
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.R
import org.oppia.android.app.home.RouteToTopicPlayStoryListener
import org.oppia.android.app.model.PromotedStory
import org.oppia.android.app.shim.IntentFactoryShim
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.databinding.PromotedStoryCardBinding

// TODO(#283): Add download status information to promoted-story-card.

/** [ViewModel] for displaying a promoted story. */
class PromotedStoryViewModel(
  private val activity: AppCompatActivity,
  private val internalProfileId: Int,
  val entityType: String,
  private val IntentFactoryShim: IntentFactoryShim
) :
  ObservableViewModel(),
  RouteToTopicPlayStoryListener {

  /**
   * The retrieved [LiveData] for retrieving topic summaries. This model should ensure only one
   * [LiveData] is used for all subsequent processed data to ensure the transformed [LiveData]s are
   * always in sync.
   */
  var story : PromotedStory = PromotedStory.getDefaultInstance()
  private val orientation = Resources.getSystem().configuration.orientation
  var totalStoryCount = -1

  fun setPromotedStory(promotedStory: PromotedStory) {
    this.story = promotedStory
  }

  fun setStoryCount(newCount: Int) {
    this.totalStoryCount = newCount
  }

  fun computeLayoutWidth(): Int {
    if (totalStoryCount > 1) {
      if (orientation == Configuration.ORIENTATION_PORTRAIT) {
        return ViewGroup.LayoutParams.MATCH_PARENT
      } else {
        return (activity as Context).resources.getDimensionPixelSize(R.dimen.promoted_story_card_width)
      }
    } else {
      return ViewGroup.LayoutParams.MATCH_PARENT
    }
  }

  fun clickOnStoryTile() {
    routeToTopicPlayStory(
      internalProfileId,
      story.topicId,
      story.storyId
    )
  }

  override fun routeToTopicPlayStory(internalProfileId: Int, topicId: String, storyId: String) {
    val intent = IntentFactoryShim.createTopicPlayStoryActivityIntent(
      activity.applicationContext,
      internalProfileId,
      topicId,
      storyId
    )
    activity.startActivity(intent)
  }
}
