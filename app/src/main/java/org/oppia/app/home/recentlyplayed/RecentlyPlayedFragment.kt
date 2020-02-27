package org.oppia.app.home.recentlyplayed

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import org.oppia.app.model.PromotedStory
import javax.inject.Inject

/** Fragment that contains all recently played stories. */
class RecentlyPlayedFragment : InjectableFragment(), OngoingStoryClickListener {
  @Inject lateinit var recentlyPlayedFragmentPresenter: RecentlyPlayedFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return recentlyPlayedFragmentPresenter.handleCreateView(inflater, container)
  }

  override fun onOngoingStoryClicked(promotedStory: PromotedStory) {
    recentlyPlayedFragmentPresenter.onOngoingStoryClicked(promotedStory)
  }
}
