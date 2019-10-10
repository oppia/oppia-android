package org.oppia.app.story

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import javax.inject.Inject

private const val KEY_STORY_ID = "STORY_ID"

/** A fragment that contains stories*/
class StoryFragment : InjectableFragment() {

  companion object {
    /**
     * Creates a new instance of story fragment.
     * @param storyId Used in TopicController to get correct story information.
     * @return [StoryFragment]
     */
    fun newInstace(storyId: String): StoryFragment {
      val storyFragment = StoryFragment()
      val args = Bundle()
      args.putString(KEY_STORY_ID, storyId)
      storyFragment.arguments = args
      return storyFragment
    }
  }
  @Inject lateinit var storyFragmentPresenter: StoryFragmentPresenter

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val args = checkNotNull(arguments) { "Expected arguments to be passed to StoryFragment" }
    val storyId = checkNotNull(args.getString(KEY_STORY_ID)) { "Expected storyId to be passed to StoryFragment" }
    return storyFragmentPresenter.handleCreateView(inflater, container, storyId)
  }
}
