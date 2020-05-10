package org.oppia.app.story

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import javax.inject.Inject

private const val STORY_FRAGMENT_INTERNAL_PROFILE_ID_KEY = "StoryActivity.internal_profile_id"
private const val STORY_FRAGMENT_TOPIC_ID_KEY = "StoryActivity.topic_id"
private const val STORY_FRAGMENT_STORY_ID_KEY = "StoryActivity.story_id"

/** Fragment for displaying a story. */
class StoryFragment : InjectableFragment(), ExplorationSelectionListener {
  companion object {
    /** Returns a new [StoryFragment] to display the story corresponding to the specified story ID. */
    fun newInstance(internalProfileId: Int, topicId: String, storyId: String): StoryFragment {
      val storyFragment = StoryFragment()
      val args = Bundle()
      args.putInt(STORY_FRAGMENT_INTERNAL_PROFILE_ID_KEY, internalProfileId)
      args.putString(STORY_FRAGMENT_TOPIC_ID_KEY, topicId)
      args.putString(STORY_FRAGMENT_STORY_ID_KEY, storyId)
      storyFragment.arguments = args
      return storyFragment
    }
  }

  @Inject lateinit var storyFragmentPresenter: StoryFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val args = checkNotNull(arguments) { "Expected arguments to be passed to StoryFragment" }
    val internalProfileId = args.getInt(STORY_FRAGMENT_INTERNAL_PROFILE_ID_KEY, -1)
    val topicId =
      checkNotNull(args.getString(STORY_FRAGMENT_TOPIC_ID_KEY)) { "Expected topicId to be passed to StoryFragment" }
    val storyId =
      checkNotNull(args.getString(STORY_FRAGMENT_STORY_ID_KEY)) { "Expected storyId to be passed to StoryFragment" }
    return storyFragmentPresenter.handleCreateView(inflater, container, internalProfileId, topicId, storyId)
  }

  override fun selectExploration(internalProfileId: Int, topicId: String, storyId: String, explorationId: String) {
    storyFragmentPresenter.handleSelectExploration(internalProfileId, topicId, storyId, explorationId)
  }

  fun smoothScrollToPosition(position: Int) {
    storyFragmentPresenter.smoothScrollToPosition(position)
  }
}
