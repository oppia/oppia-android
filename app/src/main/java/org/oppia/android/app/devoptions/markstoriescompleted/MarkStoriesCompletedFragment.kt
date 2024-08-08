package org.oppia.android.app.devoptions.markstoriescompleted

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.MarkStoriesCompletedFragmentStateBundle
import org.oppia.android.app.model.ProfileId
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** Fragment to display all stories and provide functionality to mark them completed. */
class MarkStoriesCompletedFragment : InjectableFragment() {
  @Inject
  lateinit var markStoriesCompletedFragmentPresenter: MarkStoriesCompletedFragmentPresenter

  companion object {

    const val MARK_STORIES_COMPLETED_FRAGMENT_STATE_KEY = "MarkStoriesCompletedFragment.state"

    internal const val PROFILE_ID_ARGUMENT_KEY = "MarkStoriesCompletedFragment.profile_id"

    /** Returns a new [MarkStoriesCompletedFragment]. */
    fun newInstance(internalProfileId: Int): MarkStoriesCompletedFragment {
      val profileId = ProfileId.newBuilder().setLoggedInInternalProfileId(internalProfileId).build()
      return MarkStoriesCompletedFragment().apply {
        arguments = Bundle().apply {
          decorateWithUserProfileId(profileId)
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

    val profileId = arguments.extractCurrentUserProfileId()
    val internalProfileId = profileId.loggedInInternalProfileId

    var selectedStoryIdList = ArrayList<String>()
    if (savedInstanceState != null) {

      val stateArgs = savedInstanceState.getProto(
        MARK_STORIES_COMPLETED_FRAGMENT_STATE_KEY,
        MarkStoriesCompletedFragmentStateBundle.getDefaultInstance()
      )
      selectedStoryIdList = stateArgs?.storyIdsList?.let { ArrayList(it) } ?: ArrayList()
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
    val args = MarkStoriesCompletedFragmentStateBundle.newBuilder().apply {
      addAllStoryIds(markStoriesCompletedFragmentPresenter.selectedStoryIdList)
    }
      .build()
    outState.apply {
      putProto(MARK_STORIES_COMPLETED_FRAGMENT_STATE_KEY, args)
    }
  }
}
