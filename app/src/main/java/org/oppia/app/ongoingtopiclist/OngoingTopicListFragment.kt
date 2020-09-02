package org.oppia.app.ongoingtopiclist

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import javax.inject.Inject

/** Fragment for displaying [OngoingTopicListActivity]. */
class OngoingTopicListFragment : InjectableFragment() {

  companion object {
    // TODO(#1655): Re-restrict access to fields in tests post-Gradle.
    internal const val TAG = "OngoingTopicListFragment.tag"
    internal const val ONGOING_TOPIC_LIST_FRAGMENT_PROFILE_ID_KEY =
      "OngoingTopicListFragment.profile_id"

    /** Returns a new [OngoingTopicListFragment] to display corresponding to the specified profile ID. */
    fun newInstance(internalProfileId: Int): OngoingTopicListFragment {
      val ongoingTopicListFragment = OngoingTopicListFragment()
      val args = Bundle()
      args.putInt(ONGOING_TOPIC_LIST_FRAGMENT_PROFILE_ID_KEY, internalProfileId)
      ongoingTopicListFragment.arguments = args
      return ongoingTopicListFragment
    }
  }

  @Inject
  lateinit var ongoingTopicListFragmentPresenter: OngoingTopicListFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val args =
      checkNotNull(arguments) { "Expected arguments to be passed to OngoingTopicListFragment" }
    val internalProfileId = args.getInt(
      ONGOING_TOPIC_LIST_FRAGMENT_PROFILE_ID_KEY, -1
    )
    return ongoingTopicListFragmentPresenter.handleCreateView(
      inflater,
      container,
      internalProfileId
    )
  }
}
