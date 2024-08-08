package org.oppia.android.app.home.recentlyplayed

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.PromotedStory
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** Fragment that contains all recently played stories. */
class RecentlyPlayedFragment : InjectableFragment(), PromotedStoryClickListener {
  companion object {
    const val TAG_RECENTLY_PLAYED_FRAGMENT = "TAG_RECENTLY_PLAYED_FRAGMENT"

    /** Returns a new [RecentlyPlayedFragment] to display recently played stories. */
    fun newInstance(internalProfileId: Int): RecentlyPlayedFragment {
      val profileId = ProfileId.newBuilder().setLoggedInInternalProfileId(internalProfileId).build()
      return RecentlyPlayedFragment().apply {
        arguments = Bundle().apply {
          decorateWithUserProfileId(profileId)
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
    val profileId =
      arguments.extractCurrentUserProfileId()

    val internalProfileId = profileId.loggedInInternalProfileId
    return recentlyPlayedFragmentPresenter.handleCreateView(inflater, container, internalProfileId)
  }

  override fun promotedStoryClicked(promotedStory: PromotedStory) {
    recentlyPlayedFragmentPresenter.promotedStoryClicked(promotedStory)
  }
}
