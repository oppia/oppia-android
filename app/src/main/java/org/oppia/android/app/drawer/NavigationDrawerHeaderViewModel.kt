package org.oppia.android.app.drawer

import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import org.oppia.android.R
import org.oppia.android.app.model.Profile
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject
import org.oppia.android.app.translation.AppLanguageResourceHandler

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
    // TODO: file an issue to fix this (should be a single string so that translators can properly configure ordering).
    val completedStoryCountText =
      resourceHandler.getQuantityStringInLocale(
        R.plurals.completed_story_count, completedStoryCount, completedStoryCount
      )
    val ongoingTopicCountText =
      resourceHandler.getQuantityStringInLocale(
        R.plurals.ongoing_topic_count, ongoingTopicCount, ongoingTopicCount
      )
    val barSeparator = resourceHandler.getStringInLocale(R.string.bar_separator)
    return "$completedStoryCountText$barSeparator$ongoingTopicCountText"
  }
}
