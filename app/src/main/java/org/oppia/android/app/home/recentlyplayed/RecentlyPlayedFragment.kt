package org.oppia.android.app.home.recentlyplayed

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.PromotedStory
import org.oppia.android.app.model.RecentlyPlayedFragmentArguments
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import javax.inject.Inject

private const val RECENTLY_PLAYED_FRAGMENT_INTERNAL_PROFILE_ID_KEY =
  "RecentlyPlayedFragment.internal_profile_id"

/** Fragment that contains all recently played stories. */
class RecentlyPlayedFragment : InjectableFragment(), PromotedStoryClickListener {
  companion object {
    const val TAG_RECENTLY_PLAYED_FRAGMENT = "TAG_RECENTLY_PLAYED_FRAGMENT"

    const val RECENTLY_PLAYED_FRAGMENT_ARGUMENTS_KEY = "RecentlyPlayedFragment.arguments"

    /** Returns a new [RecentlyPlayedFragment] to display recently played stories. */
    fun newInstance(internalProfileId: Int): RecentlyPlayedFragment {
      val args =
        RecentlyPlayedFragmentArguments.newBuilder().setInternalProfileId(internalProfileId).build()
      return RecentlyPlayedFragment().apply {
        arguments = Bundle().apply {
          putProto(RECENTLY_PLAYED_FRAGMENT_ARGUMENTS_KEY, args)
        }
      }
    }
  }

  @Inject
  lateinit var recentlyPlayedFragmentPresenter: RecentlyPlayedFragmentPresenter

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
      checkNotNull(arguments) { "Expected arguments to be passed to RecentlyPlayedFragment" }
    val args =
      arguments.getProto(
        RECENTLY_PLAYED_FRAGMENT_ARGUMENTS_KEY,
        RecentlyPlayedFragmentArguments.getDefaultInstance()
      )

    val internalProfileId = args?.internalProfileId ?: -1
    return recentlyPlayedFragmentPresenter.handleCreateView(inflater, container, internalProfileId)
  }

  override fun promotedStoryClicked(promotedStory: PromotedStory) {
    recentlyPlayedFragmentPresenter.promotedStoryClicked(promotedStory)
  }
}
