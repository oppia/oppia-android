package org.oppia.android.app.testing

import android.content.Context
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.home.RouteToExplorationListener
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.domain.exploration.ExplorationDataController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_2
import org.oppia.android.domain.topic.TEST_STORY_ID_0
import org.oppia.android.domain.topic.TEST_TOPIC_ID_0
import org.oppia.android.util.data.AsyncResult
import javax.inject.Inject
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.utility.SplitScreenManager

private const val INTERNAL_PROFILE_ID = 0
private const val TOPIC_ID = TEST_TOPIC_ID_0
private const val STORY_ID = TEST_STORY_ID_0
private const val EXPLORATION_ID = TEST_EXPLORATION_ID_2
private const val TAG_EXPLORATION_TEST_ACTIVITY = "ExplorationTestActivity"
private const val TEST_FRAGMENT_TAG = "ExplorationTestActivity.TestFragment"

/** The presenter for [ExplorationTestActivityPresenter]. */
@ActivityScope
class ExplorationTestActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val explorationDataController: ExplorationDataController,
  private val oppiaLogger: OppiaLogger
) {

  private val routeToExplorationListener = activity as RouteToExplorationListener

  fun handleOnCreate() {
    activity.setContentView(R.layout.exploration_test_activity)
    activity.supportFragmentManager.beginTransaction().apply {
      add(R.id.exploration_test_fragment_placeholder, TestFragment(), TEST_FRAGMENT_TAG)
    }.commitNow()
    activity.findViewById<Button>(R.id.play_exploration_button).setOnClickListener {
      playExplorationButton()
    }
  }

  private fun playExplorationButton() {
    explorationDataController.stopPlayingExploration()
    explorationDataController.startPlayingExploration(
      INTERNAL_PROFILE_ID,
      TOPIC_ID,
      STORY_ID,
      EXPLORATION_ID,
      shouldSavePartialProgress = false,
      explorationCheckpoint = ExplorationCheckpoint.getDefaultInstance()
    ).observe(
      activity,
      Observer<AsyncResult<Any?>> { result ->
        when {
          result.isPending() -> oppiaLogger.d(TAG_EXPLORATION_TEST_ACTIVITY, "Loading exploration")
          result.isFailure() -> oppiaLogger.e(
            TAG_EXPLORATION_TEST_ACTIVITY,
            "Failed to load exploration",
            result.getErrorOrNull()!!
          )
          else -> {
            oppiaLogger.d(TAG_EXPLORATION_TEST_ACTIVITY, "Successfully loaded exploration")
            routeToExplorationListener.routeToExploration(
              INTERNAL_PROFILE_ID,
              TOPIC_ID,
              STORY_ID,
              EXPLORATION_ID,
              /* backflowScreen= */ null,
              isCheckpointingEnabled = false
            )
          }
        }
      }
    )
  }

  fun getTestFragment(): TestFragment? {
    return activity.supportFragmentManager.findFragmentByTag(TEST_FRAGMENT_TAG) as? TestFragment
  }

  class TestFragment: InjectableFragment() {
    @Inject lateinit var splitScreenManager: SplitScreenManager

    override fun onAttach(context: Context) {
      super.onAttach(context)
      (fragmentComponent as FragmentComponentImpl).inject(this)
    }
  }
}
