package org.oppia.android.app.profileprogress

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import org.oppia.android.app.home.RouteToRecentlyPlayedListener
import org.oppia.android.app.model.Profile
import org.oppia.android.app.model.RecentlyPlayedActivityTitle
import org.oppia.android.app.translation.AppLanguageResourceHandler

/** Header [ViewModel] for the recycler view in [ProfileProgressFragment]. */
class ProfileProgressHeaderViewModel(
  private val activity: AppCompatActivity,
  fragment: Fragment,
  private val resourceHandler: AppLanguageResourceHandler
) :
  ProfileProgressItemViewModel() {
  private val routeToCompletedStoryListListener = activity as RouteToCompletedStoryListListener
  private val routeToOngoingTopicListListener = activity as RouteToOngoingTopicListListener
  private val routeToRecentlyPlayedActivity = activity as RouteToRecentlyPlayedListener
  private val profilePictureEditListener = fragment as ProfilePictureClickListener

  val profile = ObservableField<Profile>(Profile.getDefaultInstance())
  val ongoingTopicCount = ObservableField(0)
  val completedStoryCount = ObservableField(0)

  private var recentlyPlayedTopicCount: Int = 0

  fun setProfile(currentProfile: Profile) {
    profile.set(currentProfile)
  }

  fun setOngoingTopicCount(topicCount: Int) {
    ongoingTopicCount.set(topicCount)
  }

  fun setCompletedStoryCount(storyCount: Int) {
    completedStoryCount.set(storyCount)
  }

  fun clickOnCompletedStoryCount() {
    routeToCompletedStoryListListener.routeToCompletedStory()
  }

  fun clickOnOngoingTopicCount() {
    routeToOngoingTopicListListener.routeToOngoingTopic()
  }

  fun clickOnViewAll() {
    routeToRecentlyPlayedActivity.routeToRecentlyPlayed(
      RecentlyPlayedActivityTitle.RECENTLY_PLAYED_STORIES
    )
  }

  fun clickOnProfilePicture() {
    profilePictureEditListener.onProfilePictureClicked()
  }

  fun setRecentlyPlayedStoryCount(topicCount: Int) {
    recentlyPlayedTopicCount = topicCount
  }

  /** Returns the visibility for the "View All" button. */
  fun getViewAllButtonVisibility(): Int {
    return if (recentlyPlayedTopicCount > 0) {
      View.VISIBLE
    } else
      View.INVISIBLE
  }

  /** Returns the visibility for the header "Recently-played Stories" text. */
  fun getHeaderTextVisibility(): Int {
    return if (recentlyPlayedTopicCount > 0) {
      View.VISIBLE
    } else
      View.INVISIBLE
  }
}
