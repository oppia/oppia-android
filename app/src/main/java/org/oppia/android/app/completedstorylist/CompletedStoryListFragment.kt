package org.oppia.android.app.completedstorylist

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.ProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** Fragment for displaying completed stories. */
class CompletedStoryListFragment : InjectableFragment() {
  companion object {
    // TODO(#1655): Re-restrict access to fields in tests post-Gradle.
    const val COMPLETED_STORY_LIST_FRAGMENT_TAG = "COMPLETED_STORY_LIST_FRAGMENT_TAG"

    /** Returns a new [CompletedStoryListFragment] to display corresponding to the specified profile ID. */
    fun newInstance(profileId: ProfileId): CompletedStoryListFragment {
      val completedStoryListFragment = CompletedStoryListFragment()
      val args = Bundle()
      args.decorateWithUserProfileId(profileId)
      completedStoryListFragment.arguments = args
      return completedStoryListFragment
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
    val args = checkNotNull(arguments) {
      "Expected arguments to be passed to CompletedStoryListFragment"
    }
    val profileId = args.extractCurrentUserProfileId()
    return completedStoryListFragmentPresenter.handleCreateView(
      inflater,
      container,
      profileId
    )
  }
}
