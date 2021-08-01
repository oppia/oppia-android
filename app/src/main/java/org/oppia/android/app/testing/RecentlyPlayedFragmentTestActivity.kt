package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.home.RouteToExplorationListener
import org.oppia.android.app.player.exploration.ExplorationActivity
import javax.inject.Inject

const val RECENTLY_PLAYED_FRAGMENT_TEST_INTERNAL_PROFILE_ID_KEY =
  "RecentlyPlayedFragmentTest.internal_profile_id"

/** Test Activity used for testing [RecentlyPlayedFragment] */
class RecentlyPlayedFragmentTestActivity : InjectableAppCompatActivity(),
  RouteToExplorationListener {

  @Inject
  lateinit var recentlyPlayedFragmentTestActivityPresenter:
    RecentlyPlayedFragmentTestActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    val internalProfileId = intent.getIntExtra(
      RECENTLY_PLAYED_FRAGMENT_TEST_INTERNAL_PROFILE_ID_KEY,
      -1
    )
    recentlyPlayedFragmentTestActivityPresenter.handleOnCreate(internalProfileId)
  }

  companion object {

    fun createRecentlyPlayedFragmentTestActivity(context: Context, internalProfileId: Int?): Intent {
      val intent = Intent(context, RecentlyPlayedFragmentTestActivity::class.java)
      intent.putExtra(RECENTLY_PLAYED_FRAGMENT_TEST_INTERNAL_PROFILE_ID_KEY, internalProfileId)
      return intent
    }
  }

  override fun routeToExploration(
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String,
    backflowScreen: Int?,
    isCheckpointingEnabled: Boolean
  ) {
    startActivity(
      ExplorationActivity.createExplorationActivityIntent(
        this,
        internalProfileId,
        topicId,
        storyId,
        explorationId,
        backflowScreen,
        isCheckpointingEnabled
      )
    )
  }
}
