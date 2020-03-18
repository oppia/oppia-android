package org.oppia.app.profileprogress

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableField
import org.oppia.app.home.RouteToRecentlyPlayedListener
import org.oppia.app.model.Profile

/** Header [ViewModel] for the recycler view in [ProfileProgressFragment]. */
class ProfileProgressHeaderViewModel(activity: AppCompatActivity) : ProfileProgressItemViewModel() {
  private val routeToCompletedStoryListListener = activity as RouteToCompletedStoryListListener
  private val routeToOngoingTopicListListener = activity as RouteToOngoingTopicListListener
  private val routeToRecentlyPlayedActivity = activity as RouteToRecentlyPlayedListener
  private val profilePictureEditListener = activity as ProfilePictureClickListener

  val profile = ObservableField<Profile>(Profile.getDefaultInstance())
  val ongoingTopicCount = ObservableField(0)
  val completedStoryCount = ObservableField(0)

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
    routeToRecentlyPlayedActivity.routeToRecentlyPlayed()
  }

  fun clickOnProfilePicture() {
    profilePictureEditListener.onProfilePictureClicked()
  }
}
