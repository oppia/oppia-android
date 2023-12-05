package org.oppia.android.app.devoptions.markstoriescompleted

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.MarkStoriesCompletedFragmentArguments
import org.oppia.android.app.model.MarkStoriesCompletedFragmentStateBundle
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import javax.inject.Inject

/** Fragment to display all stories and provide functionality to mark them completed. */
class MarkStoriesCompletedFragment : InjectableFragment() {
  @Inject
  lateinit var markStoriesCompletedFragmentPresenter: MarkStoriesCompletedFragmentPresenter

  companion object {
    /** Argument key for MarkStoriesCompletedActivity.. */
    const val MARK_STORIES_COMPLETED_FRAGMENT_ARGUMENTS_KEY = "MarkStoriesCompletedFragment.arguments"

    const val MARK_STORIES_COMPLETED_FRAGMENT_STATE_KEY = "MarkStoriesCompletedFragment.state"

    internal const val PROFILE_ID_ARGUMENT_KEY = "MarkStoriesCompletedFragment.profile_id"

    private const val STORY_ID_LIST_ARGUMENT_KEY = "MarkStoriesCompletedFragment.story_id_list"

    /** Returns a new [MarkStoriesCompletedFragment]. */
    fun newInstance(internalProfileId: Int): MarkStoriesCompletedFragment {
      return MarkStoriesCompletedFragment().apply {
        arguments = Bundle().apply {
          val args = MarkStoriesCompletedFragmentArguments.newBuilder().apply {
            profileId = internalProfileId
          }.build()
          putProto(MARK_STORIES_COMPLETED_FRAGMENT_ARGUMENTS_KEY, args)
        }
      }
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val arguments =
      checkNotNull(arguments) { "Expected arguments to be passed to MarkStoriesCompletedFragment" }

    val args = arguments.getProto(
      MARK_STORIES_COMPLETED_FRAGMENT_ARGUMENTS_KEY,
      MarkStoriesCompletedFragmentArguments.getDefaultInstance()
    )

    val internalProfileId = args?.profileId ?: -1
    var selectedStoryIdList = ArrayList<String>()
    if (savedInstanceState != null) {

      val stateArgs = savedInstanceState.getProto(
        MARK_STORIES_COMPLETED_FRAGMENT_STATE_KEY,
        MarkStoriesCompletedFragmentStateBundle.getDefaultInstance()
      )
      selectedStoryIdList = ArrayList(stateArgs?.storyIdListList)
    }
    return markStoriesCompletedFragmentPresenter.handleCreateView(
      inflater,
      container,
      internalProfileId,
      selectedStoryIdList
    )
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.apply {
      val args = MarkStoriesCompletedFragmentStateBundle.newBuilder().apply {
        addAllStoryIdList(markStoriesCompletedFragmentPresenter.selectedStoryIdList)
      }
        .build()
      putProto(MARK_STORIES_COMPLETED_FRAGMENT_STATE_KEY, args)
    }
  }
}
