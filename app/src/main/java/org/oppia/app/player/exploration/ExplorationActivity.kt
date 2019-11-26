package org.oppia.app.player.exploration

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import org.oppia.app.R
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.player.state.StateFragment
import org.oppia.app.player.state.StateFragmentPresenter
import org.oppia.app.player.state.listener.StateKeyboardButtonListener
import org.oppia.app.player.stopexploration.StopExplorationDialogFragment
import org.oppia.app.player.stopexploration.StopExplorationInterface
import javax.inject.Inject

const val EXPLORATION_ACTIVITY_TOPIC_ID_ARGUMENT_KEY = "ExplorationActivity.exploration_id"
private const val TAG_STOP_EXPLORATION_DIALOG = "STOP_EXPLORATION_DIALOG"

/** The starting point for exploration. */
class ExplorationActivity : InjectableAppCompatActivity(), StopExplorationInterface , StateKeyboardButtonListener {


  @Inject
  lateinit var explorationActivityPresenter: ExplorationActivityPresenter
  private lateinit var explorationId: String

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

  override fun onBackPressed() {
    showStopExplorationDialogFragment()
  }

  private fun showStopExplorationDialogFragment() {
    val previousFragment = supportFragmentManager.findFragmentByTag(TAG_STOP_EXPLORATION_DIALOG)
    if (previousFragment != null) {
      supportFragmentManager.beginTransaction().remove(previousFragment).commitNow()
    }
    val dialogFragment = StopExplorationDialogFragment.newInstance()
    dialogFragment.showNow(supportFragmentManager, TAG_STOP_EXPLORATION_DIALOG)
  }

  override fun stopExploration() {
    explorationActivityPresenter.stopExploration()
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.menu_exploration_activity, menu)
    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    when (item!!.itemId) {
      R.id.action_audio_player -> explorationActivityPresenter.audioPlayerIconClicked()
    }
    return super.onOptionsItemSelected(item)
  }

  override fun onEditorAction(actionCode: Int) {
    explorationActivityPresenter.onEditorAction(actionCode)
  }

}
