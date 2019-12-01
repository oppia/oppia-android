package org.oppia.app.player.state.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.player.state.listener.StateKeyboardButtonListener
import org.oppia.app.player.stopplaying.StopStatePlayingSessionListener
import javax.inject.Inject

internal const val TEST_ACTIVITY_EXPLORATION_ID_EXTRA = "StateFragmentTestActivity.exploration_id"

/** Test Activity used for testing StateFragment */
class StateFragmentTestActivity : InjectableAppCompatActivity(), StopStatePlayingSessionListener, StateKeyboardButtonListener {
  @Inject lateinit var stateFragmentTestActivityPresenter: StateFragmentTestActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    stateFragmentTestActivityPresenter.handleOnCreate()
  }

  override fun stopSession() = stateFragmentTestActivityPresenter.stopExploration()

  override fun onEditorAction(actionCode: Int) {}

  companion object {
    fun createTestActivityIntent(context: Context, explorationId: String): Intent {
      val intent = Intent(context, StateFragmentTestActivity::class.java)
      intent.putExtra(TEST_ACTIVITY_EXPLORATION_ID_EXTRA, explorationId)
      return intent
    }
  }
}
