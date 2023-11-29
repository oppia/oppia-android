package org.oppia.android.app.completedstorylist

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.CompletedStoryListFragmentArguments
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import javax.inject.Inject

/** Fragment for displaying completed stories. */
class CompletedStoryListFragment : InjectableFragment() {
  companion object {
    // TODO(#1655): Re-restrict access to fields in tests post-Gradle.
    /** Key for accessing [CompletedStoryListFragment]. */
    const val COMPLETED_STORY_LIST_FRAGMENT_TAG = "COMPLETED_STORY_LIST_FRAGMENT_TAG"

    /** [String] key for mapping internalProfileId in [Bundle]. */
    internal const val COMPLETED_STORY_LIST_FRAGMENT_PROFILE_ID_KEY =
      "CompletedStoryListFragment.profile_id"

    /** Argument key for [CompletedStoryListFragment]. */
    const val COMPLETED_STORY_LIST_FRAGMENT_ARGUMENT_KEY =
      "COMPLETED_STORY_LIST_FRAGMENT_ARGUMENT_KEY"

    /** Returns a new [CompletedStoryListFragment] to display corresponding to the specified profile ID. */
    fun newInstance(internalProfileId: Int): CompletedStoryListFragment {
      return CompletedStoryListFragment().apply {
        arguments = Bundle().apply {
          val args = CompletedStoryListFragmentArguments.newBuilder().apply {
            this.profileId = internalProfileId
          }.build()
          putProto(COMPLETED_STORY_LIST_FRAGMENT_ARGUMENT_KEY, args)
        }
      }
    }
  }

  @Inject
  lateinit var completedStoryListFragmentPresenter: CompletedStoryListFragmentPresenter

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
      "Expected arguments to be passed to CompletedStoryListFragment"
    }
    val args = arguments.getProto(
      COMPLETED_STORY_LIST_FRAGMENT_ARGUMENT_KEY,
      CompletedStoryListFragmentArguments.getDefaultInstance()
    )
    val internalProfileId = args?.profileId ?: -1
    return completedStoryListFragmentPresenter.handleCreateView(
      inflater,
      container,
      internalProfileId
    )
  }
}
