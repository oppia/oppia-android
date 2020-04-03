package org.oppia.app.player.exploration

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import org.oppia.app.R
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.player.audio.AudioButtonListener
import org.oppia.app.player.state.listener.StateKeyboardButtonListener
import org.oppia.app.player.stopexploration.StopExplorationDialogFragment
import org.oppia.app.player.stopexploration.StopExplorationInterface
import javax.inject.Inject

private const val TAG_STOP_EXPLORATION_DIALOG = "STOP_EXPLORATION_DIALOG"

/** The starting point for exploration. */
class ExplorationActivity : InjectableAppCompatActivity(), StopExplorationInterface, StateKeyboardButtonListener,
  AudioButtonListener {
  @Inject lateinit var explorationActivityPresenter: ExplorationActivityPresenter
  private var internalProfileId: Int = -1
  private lateinit var topicId: String
  private lateinit var storyId: String
  private lateinit var explorationId: String

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    internalProfileId = intent.getIntExtra(EXPLORATION_ACTIVITY_PROFILE_ID_ARGUMENT_KEY, -1)
    topicId = intent.getStringExtra(EXPLORATION_ACTIVITY_TOPIC_ID_ARGUMENT_KEY)
    storyId = intent.getStringExtra(EXPLORATION_ACTIVITY_STORY_ID_ARGUMENT_KEY)
    explorationId = intent.getStringExtra(EXPLORATION_ACTIVITY_EXPLORATION_ID_ARGUMENT_KEY)
    explorationActivityPresenter.handleOnCreate(internalProfileId, topicId, storyId, explorationId)
  }

  companion object {
    /** Returns a new [Intent] to route to [ExplorationActivity] for a specified exploration. */

    internal const val EXPLORATION_ACTIVITY_PROFILE_ID_ARGUMENT_KEY = "ExplorationActivity.profile_id"
    internal const val EXPLORATION_ACTIVITY_TOPIC_ID_ARGUMENT_KEY = "ExplorationActivity.topic_id"
    internal const val EXPLORATION_ACTIVITY_STORY_ID_ARGUMENT_KEY = "ExplorationActivity.story_id"
    internal const val EXPLORATION_ACTIVITY_EXPLORATION_ID_ARGUMENT_KEY = "ExplorationActivity.exploration_id"
    fun createExplorationActivityIntent(
      context: Context,
      profileId: Int,
      topicId: String,
      storyId: String,
      explorationId: String
    ): Intent {
      val intent = Intent(context, ExplorationActivity::class.java)
      intent.putExtra(EXPLORATION_ACTIVITY_PROFILE_ID_ARGUMENT_KEY, profileId)
      intent.putExtra(EXPLORATION_ACTIVITY_TOPIC_ID_ARGUMENT_KEY, topicId)
      intent.putExtra(EXPLORATION_ACTIVITY_STORY_ID_ARGUMENT_KEY, storyId)
      intent.putExtra(EXPLORATION_ACTIVITY_EXPLORATION_ID_ARGUMENT_KEY, explorationId)
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

  override fun showAudioButton() = explorationActivityPresenter.showAudioButton()

  override fun hideAudioButton() = explorationActivityPresenter.hideAudioButton()

  override fun showAudioStreamingOn() = explorationActivityPresenter.showAudioStreamingOn()

  override fun showAudioStreamingOff() = explorationActivityPresenter.showAudioStreamingOff()

  override fun setAudioBarVisibility(isVisible: Boolean) {
    explorationActivityPresenter.setAudioBarVisibility(isVisible)
  }

  override fun scrollToTop() {
    explorationActivityPresenter.scrollToTop()
  }

  override fun onEditorAction(actionCode: Int) {
    explorationActivityPresenter.onKeyboardAction(actionCode)
  }
}
