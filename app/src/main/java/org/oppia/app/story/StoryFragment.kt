package org.oppia.app.story

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import javax.inject.Inject

private const val KEY_STORY_ID_ARGUMENT = "STORY_ID"

/** Fragment for displaying a story. */
class StoryFragment : InjectableFragment(), ExplorationSelectionListener {
  companion object {
    /** Returns a new [StoryFragment] to display the story corresponding to the specified story ID. */
    fun newInstance(storyId: String): StoryFragment {
      val storyFragment = StoryFragment()
      val args = Bundle()
      args.putString(KEY_STORY_ID_ARGUMENT, storyId)
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
    val storyId =
      checkNotNull(args.getString(KEY_STORY_ID_ARGUMENT)) { "Expected storyId to be passed to StoryFragment" }
    return storyFragmentPresenter.handleCreateView(inflater, container, storyId)
  }

  override fun selectExploration(explorationId: String) {
    storyFragmentPresenter.handleSelectExploration(explorationId)
  }

  fun smoothScrollToPosition(position: Int) {
    storyFragmentPresenter.smoothScrollToPosition(position)
  }
}
