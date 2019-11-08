package org.oppia.app.player.exploration

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import javax.inject.Inject

/** The Presenter for [ExplorationActivity]. */
@ActivityScope
class ExplorationActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  fun handleOnCreate(explorationId: String) {
    activity.setContentView(R.layout.exploration_activity)

    activity.setSupportActionBar(activity.findViewById(R.id.exploration_toolbar))

    if (getExplorationFragment() == null) {
      val explorationFragment = ExplorationFragment()
      val args = Bundle()
      args.putString(EXPLORATION_ACTIVITY_TOPIC_ID_ARGUMENT_KEY, explorationId)
      explorationFragment.arguments = args
      activity.supportFragmentManager.beginTransaction().add(
        R.id.exploration_fragment_placeholder,
        explorationFragment
      ).commitNow()
    }

    activity.findViewById<View>(R.id.enable_audio_playback_button).setOnClickListener {
      getExplorationFragment()?.handlePlayAudio()
    }
  }

  private fun getExplorationFragment(): ExplorationFragment? {
    return activity.supportFragmentManager.findFragmentById(
      R.id.exploration_fragment_placeholder
    ) as ExplorationFragment?
  }
}
