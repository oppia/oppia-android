package org.oppia.app.player.exploration

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.domain.exploration.ExplorationDataController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

/** The Presenter for [ExplorationActivity]. */
@ActivityScope
class ExplorationActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val explorationDataController: ExplorationDataController,
  private val logger: Logger
) {
  private lateinit var audioButton: ImageView

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

    audioButton = activity.findViewById(R.id.enable_audio_playback_button)
    audioButton.setOnClickListener {
      getExplorationFragment()?.handlePlayAudio()
    }
  }

  /** Makes audio button visible. */
  fun showAudioButton() {
    audioButton.visibility = View.VISIBLE
  }
  /** Makes audio button gone. */
  fun hideAudioButton() {
    audioButton.visibility = View.GONE
  }

  /** Sets audio button drawable to volume off. */
  fun showVolumeOff() = audioButton.setImageResource(R.drawable.ic_volume_off_48dp)
  /** Sets audio button drawable to volume on. */
  fun showVolumeOn() = audioButton.setImageResource(R.drawable.ic_play_icon_24dp)
  
  private fun getExplorationFragment(): ExplorationFragment? {
    return activity.supportFragmentManager.findFragmentById(
      R.id.exploration_fragment_placeholder
    ) as ExplorationFragment?
  }

  fun stopExploration() {
    explorationDataController.stopPlayingExploration().observe(activity, Observer<AsyncResult<Any?>> {
      when {
        it.isPending() -> logger.d("ExplorationActivity", "Stopping exploration")
        it.isFailure() -> logger.e("ExplorationActivity", "Failed to stop exploration", it.getErrorOrNull()!!)
        else -> {
          logger.d("ExplorationActivity", "Successfully stopped exploration")
          (activity as ExplorationActivity).finish()
        }
      }
    })
  }
}
