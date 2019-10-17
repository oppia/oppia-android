package org.oppia.app.player.exploration

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

const val EXPLORATION_ACTIVITY_TOPIC_ID_ARGUMENT_KEY = "ExplorationActivity.exploration_id"

/** The starting point for exploration. */
class ExplorationActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var explorationActivityPresenter: ExplorationActivityPresenter
  private lateinit var explorationId : String

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    explorationId = intent.getStringExtra(EXPLORATION_ACTIVITY_TOPIC_ID_ARGUMENT_KEY)
    explorationActivityPresenter.handleOnCreate(explorationId)
  }

  companion object {
    /** Returns a new [Intent] to route to [ExplorationActivity] for a specified topic ID. */
    fun createExplorationActivityIntent(context: Context, explorationId: String): Intent {
      val intent = Intent(context, ExplorationActivity::class.java)
      intent.putExtra(EXPLORATION_ACTIVITY_TOPIC_ID_ARGUMENT_KEY, explorationId)
      return intent
    }
  }
}
