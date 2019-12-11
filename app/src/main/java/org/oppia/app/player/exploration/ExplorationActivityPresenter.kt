package org.oppia.app.player.exploration

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.model.Exploration
import org.oppia.app.topic.TopicActivity
import org.oppia.domain.exploration.ExplorationDataController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

private const val TAG_EXPLORATION_FRAGMENT = "TAG_EXPLORATION_FRAGMENT"

/** The Presenter for [ExplorationActivity]. */
@ActivityScope
class ExplorationActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val explorationDataController: ExplorationDataController,
  private val logger: Logger
) {

  private lateinit var toolbar: Toolbar
  private var topicId: String? = null

  fun handleOnCreate(explorationId: String, topicId: String?) {
    activity.setContentView(R.layout.exploration_activity)

    toolbar = activity.findViewById(R.id.exploration_toolbar)
    activity.setSupportActionBar(toolbar)

    updateToolbarTitle(explorationId)
    this.topicId = topicId

    if (getExplorationFragment() == null) {
      val explorationFragment = ExplorationFragment()
      val args = Bundle()
      args.putString(EXPLORATION_ACTIVITY_EXPLORATION_ID_ARGUMENT_KEY, explorationId)
      explorationFragment.arguments = args
      activity.supportFragmentManager.beginTransaction().add(
        R.id.exploration_fragment_placeholder,
        explorationFragment,
        TAG_EXPLORATION_FRAGMENT
      ).commitNow()
    }
  }

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
          if (topicId != null) {
            val intent = Intent(activity, TopicActivity::class.java)
            intent.putExtra(TopicActivity.TOPIC_ACTIVITY_TOPIC_ID_ARGUMENT_KEY, topicId)
            intent.addFlags(FLAG_ACTIVITY_REORDER_TO_FRONT)
            activity.startActivity(intent)
          }
          (activity as ExplorationActivity).finish()
        }
      }
    })
  }

  fun audioPlayerIconClicked() {
    getExplorationFragment()?.handlePlayAudio()
  }

  private fun updateToolbarTitle(explorationId: String) {
    subscribeToExploration(explorationDataController.getExplorationById(explorationId))
  }

  private fun subscribeToExploration(explorationResultLiveData: LiveData<AsyncResult<Exploration>>) {
    val explorationLiveData = getExploration(explorationResultLiveData)
    explorationLiveData.observe(activity, Observer<Exploration> {
      toolbar.title = it.title
    })
  }

  /** Helper for subscribeToExploration. */
  private fun getExploration(exploration: LiveData<AsyncResult<Exploration>>): LiveData<Exploration> {
    return Transformations.map(exploration, ::processExploration)
  }

  /** Helper for subscribeToExploration. */
  private fun processExploration(ephemeralStateResult: AsyncResult<Exploration>): Exploration {
    if (ephemeralStateResult.isFailure()) {
      logger.e("StateFragment", "Failed to retrieve answer outcome", ephemeralStateResult.getErrorOrNull()!!)
    }
    return ephemeralStateResult.getOrDefault(Exploration.getDefaultInstance())
  }

  fun onKeyboardAction(actionCode: Int) {
    if (actionCode == EditorInfo.IME_ACTION_DONE) {
      val explorationFragment =
        activity.supportFragmentManager.findFragmentByTag(TAG_EXPLORATION_FRAGMENT) as ExplorationFragment
      explorationFragment.onKeyboardAction()
    }
  }
}
