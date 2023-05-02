package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.home.RouteToExplorationListener
import org.oppia.android.app.model.ExplorationActivityParams
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.player.exploration.ExplorationActivity
import org.oppia.android.app.utility.SplitScreenManager
import javax.inject.Inject

/** The activity for testing [ExplorationActivity]. */
class ExplorationTestActivity : InjectableAppCompatActivity(), RouteToExplorationListener {
  @Inject
  lateinit var presenter: ExplorationTestActivityPresenter

  /**
   * Exposes the [SplitScreenManager] corresponding to the fragment under test for tests to interact
   * with.
   */
  val splitScreenManager: SplitScreenManager
    get() = getTestFragment().splitScreenManager

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as Injector).inject(this)
    presenter.handleOnCreate()
  }

  override fun routeToExploration(
    profileId: ProfileId,
    topicId: String,
    storyId: String,
    explorationId: String,
    parentScreen: ExplorationActivityParams.ParentScreen,
    isCheckpointingEnabled: Boolean
  ) {
    startActivity(
      ExplorationActivity.createIntent(
        this,
        profileId,
        topicId,
        storyId,
        explorationId,
        parentScreen,
        isCheckpointingEnabled
      )
    )
  }

  private fun getTestFragment() = checkNotNull(presenter.getTestFragment()) {
    "Expected TestFragment to be present in inflated test activity. Did you try to retrieve the" +
      " screen manager too early in the test?"
  }

  interface Injector {
    fun inject(activity: ExplorationTestActivity)
  }

  companion object {
    fun createIntent(context: Context): Intent =
      Intent(context, ExplorationTestActivity::class.java)
  }
}
