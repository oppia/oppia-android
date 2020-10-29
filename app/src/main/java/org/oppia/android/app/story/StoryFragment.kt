package org.oppia.android.app.story

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject

private const val INTERNAL_PROFILE_ID_ARGUMENT_KEY = "StoryFragment.internal_profile_id"
private const val TOPIC_ID_ARGUMENT_KEY = "StoryFragment.topic_id"
private const val STORY_ID_ARGUMENT_KEY = "StoryFragment.story_id"

/** Fragment for displaying a story. */
class StoryFragment : InjectableFragment(), ExplorationSelectionListener, StoryFragmentScroller {
  companion object {
    /** Returns a new [StoryFragment] to display the story corresponding to the specified story ID. */
    fun newInstance(internalProfileId: Int, topicId: String, storyId: String): StoryFragment {
      val storyFragment = StoryFragment()
      val args = Bundle()
      args.putInt(INTERNAL_PROFILE_ID_ARGUMENT_KEY, internalProfileId)
      args.putString(TOPIC_ID_ARGUMENT_KEY, topicId)
      args.putString(STORY_ID_ARGUMENT_KEY, storyId)
      storyFragment.arguments = args
      return storyFragment
    }
  }

  @Inject
  lateinit var storyFragmentPresenter: StoryFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val args = checkNotNull(arguments) {
      "Expected arguments to be passed to StoryFragment"
    }
    val internalProfileId = args.getInt(INTERNAL_PROFILE_ID_ARGUMENT_KEY, -1)
    val topicId =
      checkNotNull(args.getString(TOPIC_ID_ARGUMENT_KEY)) {
        "Expected topicId to be passed to StoryFragment"
      }
    val storyId =
      checkNotNull(args.getString(STORY_ID_ARGUMENT_KEY)) {
        "Expected storyId to be passed to StoryFragment"
      }
    return storyFragmentPresenter.handleCreateView(
      inflater,
      container,
      internalProfileId,
      topicId,
      storyId
    )
  }

  override fun selectExploration(
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String,
    backflowScreen: Int?
  ) {
    storyFragmentPresenter.handleSelectExploration(
      internalProfileId,
      topicId,
      storyId,
      explorationId,
      backflowScreen
    )
  }

  override fun smoothScrollToPosition(position: Int) {
    storyFragmentPresenter.smoothScrollToPosition(position)
  }
}
