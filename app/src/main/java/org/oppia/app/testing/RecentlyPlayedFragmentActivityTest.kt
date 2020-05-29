package org.oppia.app.testing

import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.home.RouteToExplorationListener
import org.oppia.app.player.exploration.ExplorationActivity
import javax.inject.Inject

/** Test Activity used for testing [RecentlyPlayedFragment] */
class RecentlyPlayedFragmentActivityTest : InjectableAppCompatActivity(),
  RouteToExplorationListener {

  @Inject
  lateinit var recentlyPlayedFragmentActivityTestPresenter: RecentlyPlayedFragmentActivityTestPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    recentlyPlayedFragmentActivityTestPresenter.handleOnCreate()
  }

  companion object {
    internal const val TAG_RECENTLY_PLAYED_FRAGMENT = "TAG_RECENTLY_PLAYED_FRAGMENT"
  }

  override fun routeToExploration(
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String,
    backflowScreen: Int?
  ) {
    startActivity(
      ExplorationActivity.createExplorationActivityIntent(
        this,
        internalProfileId,
        topicId,
        storyId,
        explorationId,
        backflowScreen
      )
    )
  }
}
