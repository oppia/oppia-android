package org.oppia.android.app.drawer

import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import org.oppia.android.R
import org.oppia.android.app.model.Profile
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

private const val DEFAULT_ONGOING_TOPIC_COUNT = 0
private const val DEFAULT_COMPLETED_STORY_COUNT = 0

/** [ViewModel] for displaying User profile details in navigation header. */
class NavigationDrawerHeaderViewModel @Inject constructor(
  fragment: Fragment,
  private val resourceHandler: AppLanguageResourceHandler
) : ObservableViewModel() {
  private var routeToProfileProgressListener = fragment as RouteToProfileProgressListener

  val profile = ObservableField(Profile.getDefaultInstance())
  private var ongoingTopicCount = DEFAULT_ONGOING_TOPIC_COUNT
  private var completedStoryCount = DEFAULT_COMPLETED_STORY_COUNT
  val profileTopicProgressText: ObservableField<String> =
    ObservableField(computeProfileTopicProgressText())
  val profileStoryProgressText: ObservableField<String> =
    ObservableField(computeProfileStoryProgressText())

  fun onHeaderClicked() {
    routeToProfileProgressListener.routeToProfileProgress(profile.get()!!.id.loggedInInternalProfileId)
  }

  fun setOngoingTopicProgress(ongoingTopicCount: Int) {
    this.ongoingTopicCount = ongoingTopicCount
    profileTopicProgressText.set(computeProfileTopicProgressText())
  }

  fun setCompletedStoryProgress(completedStoryCount: Int) {
    this.completedStoryCount = completedStoryCount
    profileStoryProgressText.set(computeProfileStoryProgressText())
  }

  private fun computeProfileStoryProgressText(): String {
    return resourceHandler.getQuantityStringInLocaleWithWrapping(
      R.plurals.completed_story_count,
      completedStoryCount,
      completedStoryCount.toString()
    )
  }

  fun getBarSeparator() = resourceHandler.getStringInLocale(R.string.bar_separator)

  private fun computeProfileTopicProgressText(): String {
    return resourceHandler.getQuantityStringInLocaleWithWrapping(
      R.plurals.ongoing_topic_count,
      ongoingTopicCount,
      ongoingTopicCount.toString()
    )
  }
}
