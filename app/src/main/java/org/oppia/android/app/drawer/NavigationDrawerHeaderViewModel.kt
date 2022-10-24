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
  val profileProgressText: ObservableField<String> = ObservableField(computeProfileProgressText())

  fun onHeaderClicked() {
    routeToProfileProgressListener.routeToProfileProgress(profile.get()!!.id.internalId)
  }

  fun setOngoingTopicProgress(ongoingTopicCount: Int) {
    this.ongoingTopicCount = ongoingTopicCount
    profileProgressText.set(computeProfileProgressText())
  }

  fun setCompletedStoryProgress(completedStoryCount: Int) {
    this.completedStoryCount = completedStoryCount
    profileProgressText.set(computeProfileProgressText())
  }

  private fun computeProfileProgressText(): String {
    // TODO(#3843): Either combine these strings into one or use separate views to display them.
    val completedStoryCountText =
      resourceHandler.getQuantityStringInLocaleWithWrapping(
        R.plurals.completed_story_count, completedStoryCount, completedStoryCount.toString()
      )
    val ongoingTopicCountText =
      resourceHandler.getQuantityStringInLocaleWithWrapping(
        R.plurals.ongoing_topic_count, ongoingTopicCount, ongoingTopicCount.toString()
      )
    val barSeparator = resourceHandler.getStringInLocale(R.string.navigation_drawer_activity_bar_separator)
    return "$completedStoryCountText$barSeparator$ongoingTopicCountText"
  }
}
