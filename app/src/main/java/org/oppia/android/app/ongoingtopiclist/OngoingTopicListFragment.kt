package org.oppia.android.app.ongoingtopiclist

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

/** Fragment for displaying [OngoingTopicListActivity]. */
class OngoingTopicListFragment : InjectableFragment() {

  companion object {
    // TODO(#1655): Re-restrict access to fields in tests post-Gradle.
    const val ONGOING_TOPIC_LIST_FRAGMENT_TAG = "TAG_ONGOING_TOPIC_LIST_FRAGMENT"

    /** Returns a new [OngoingTopicListFragment] to display corresponding to the specified profile ID. */
    fun newInstance(internalProfileId: Int): OngoingTopicListFragment {
      val ongoingTopicListFragment = OngoingTopicListFragment()
      val args = Bundle()
      val profileId = ProfileId.newBuilder().setLoggedInInternalProfileId(internalProfileId).build()
      args.decorateWithUserProfileId(profileId)
      ongoingTopicListFragment.arguments = args
      return ongoingTopicListFragment
    }
  }

  @Inject
  lateinit var ongoingTopicListFragmentPresenter: OngoingTopicListFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val args =
      checkNotNull(arguments) { "Expected arguments to be passed to OngoingTopicListFragment" }
    val internalProfileId = args.extractCurrentUserProfileId().loggedInInternalProfileId
    return ongoingTopicListFragmentPresenter.handleCreateView(
      inflater,
      container,
      internalProfileId
    )
  }
}
