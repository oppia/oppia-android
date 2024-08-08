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
import org.oppia.android.app.model.StoryFragmentArguments
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** Fragment for displaying a story. */
class StoryFragment : InjectableFragment(), ExplorationSelectionListener, StoryFragmentScroller {
  companion object {
    /** Arguments key for StoryFragment. */
    const val STORY_FRAGMENT_ARGUMENTS_KEY = "StoryFragment.arguments"

    /** Returns a new [StoryFragment] to display the story corresponding to the specified story ID. */
    fun newInstance(
      internalProfileId: Int,
      classroomId: String,
      topicId: String,
      storyId: String
    ): StoryFragment {

      val profileId = ProfileId.newBuilder().setLoggedInInternalProfileId(internalProfileId).build()
      val args = StoryFragmentArguments.newBuilder().apply {
        this.classroomId = classroomId
        this.topicId = topicId
        this.storyId = storyId
      }.build()
      return StoryFragment().apply {
        arguments = Bundle().apply {
          putProto(STORY_FRAGMENT_ARGUMENTS_KEY, args)
          decorateWithUserProfileId(profileId)
        }
      }
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
    val arguments = checkNotNull(arguments) {
      "Expected arguments to be passed to StoryFragment."
    }
    val args =
      arguments.getProto(STORY_FRAGMENT_ARGUMENTS_KEY, StoryFragmentArguments.getDefaultInstance())

    val internalProfileId = arguments.extractCurrentUserProfileId().loggedInInternalProfileId
    val classroomId =
      checkNotNull(args.classroomId) {
        "Expected classroomId to be passed to StoryFragment."
      }
    val topicId =
      checkNotNull(args.topicId) {
        "Expected topicId to be passed to StoryFragment."
      }
    val storyId =
      checkNotNull(args.storyId) {
        "Expected storyId to be passed to StoryFragment."
      }
    return storyFragmentPresenter.handleCreateView(
      inflater,
      container,
      internalProfileId,
      classroomId,
      topicId,
      storyId
    )
  }

  override fun selectExploration(
    profileId: ProfileId,
    classroomId: String,
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
      classroomId,
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
