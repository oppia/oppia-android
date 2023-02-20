package org.oppia.android.app.story

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.ExplorationActivityParams
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.ProfileId
import org.oppia.android.util.extensions.getStringFromBundle
import org.oppia.android.util.profile.CurrentUserProfileIdDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdDecorator.extractCurrentUserProfileId
import javax.inject.Inject

private const val KEY_TOPIC_ID_ARGUMENT = "TOPIC_ID"
private const val KEY_STORY_ID_ARGUMENT = "STORY_ID"

/** Fragment for displaying a story. */
class StoryFragment : InjectableFragment(), ExplorationSelectionListener, StoryFragmentScroller {
  companion object {
    /** Returns a new [StoryFragment] to display the story corresponding to the specified story ID. */
    fun newInstance(profileId: ProfileId, topicId: String, storyId: String): StoryFragment {
      val storyFragment = StoryFragment()
      val args = Bundle()
      args.decorateWithUserProfileId(profileId)
      args.putString(KEY_TOPIC_ID_ARGUMENT, topicId)
      args.putString(KEY_STORY_ID_ARGUMENT, storyId)
      storyFragment.arguments = args
      return storyFragment
    }
  }

  @Inject
  lateinit var storyFragmentPresenter: StoryFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val args = checkNotNull(arguments) {
      "Expected arguments to be passed to StoryFragment"
    }
    val profileId = args.extractCurrentUserProfileId()
    val topicId =
      checkNotNull(args.getStringFromBundle(KEY_TOPIC_ID_ARGUMENT)) {
        "Expected topicId to be passed to StoryFragment"
      }
    val storyId =
      checkNotNull(args.getStringFromBundle(KEY_STORY_ID_ARGUMENT)) {
        "Expected storyId to be passed to StoryFragment"
      }
    return storyFragmentPresenter.handleCreateView(
      inflater,
      container,
      profileId,
      topicId,
      storyId
    )
  }

  override fun selectExploration(
    profileId: ProfileId,
    topicId: String,
    storyId: String,
    explorationId: String,
    canExplorationBeResumed: Boolean,
    canHavePartialProgressSaved: Boolean,
    parentScreen: ExplorationActivityParams.ParentScreen,
    explorationCheckpoint: ExplorationCheckpoint
  ) {
    storyFragmentPresenter.handleSelectExploration(
      profileId,
      topicId,
      storyId,
      explorationId,
      canExplorationBeResumed,
      canHavePartialProgressSaved,
      parentScreen,
      explorationCheckpoint
    )
  }

  override fun smoothScrollToPosition(position: Int) {
    storyFragmentPresenter.smoothScrollToPosition(position)
  }
}
