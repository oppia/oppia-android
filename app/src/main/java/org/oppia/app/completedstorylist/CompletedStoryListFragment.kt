package org.oppia.app.completedstorylist

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import javax.inject.Inject

/** Fragment for displaying completed stories. */
class CompletedStoryListFragment : InjectableFragment() {
  companion object {
    internal const val COMPLETED_STORY_LIST_FRAGMENT_PROFILE_ID_KEY = "CompletedStoryListFragment.profile_id"

    /** Returns a new [CompletedStoryListFragment] to display corresponding to the specified profile ID. */
    fun newInstance(internalProfileId: Int): CompletedStoryListFragment {
      val completedStoryListFragment = CompletedStoryListFragment()
      val args = Bundle()
      args.putInt(COMPLETED_STORY_LIST_FRAGMENT_PROFILE_ID_KEY, internalProfileId)
      completedStoryListFragment.arguments = args
      return completedStoryListFragment
    }
  }

  @Inject lateinit var completedStoryListFragmentPresenter: CompletedStoryListFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val args = checkNotNull(arguments) { "Expected arguments to be passed to CompletedStoryListFragment" }
    val internalProfileId = args.getInt(COMPLETED_STORY_LIST_FRAGMENT_PROFILE_ID_KEY, -1)
    return completedStoryListFragmentPresenter.handleCreateView(inflater, container, internalProfileId)
  }
}
