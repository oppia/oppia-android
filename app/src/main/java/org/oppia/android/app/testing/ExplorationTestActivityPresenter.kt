package org.oppia.android.app.testing

import android.content.Context
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.home.RouteToExplorationListener
import org.oppia.android.app.model.ExplorationActivityParams
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.utility.SplitScreenManager
import org.oppia.android.domain.classroom.TEST_CLASSROOM_ID_0
import org.oppia.android.domain.exploration.ExplorationDataController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_2
import org.oppia.android.domain.topic.TEST_STORY_ID_0
import org.oppia.android.domain.topic.TEST_TOPIC_ID_0
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

private const val INTERNAL_PROFILE_ID = 0
private const val CLASSROOM_ID = TEST_CLASSROOM_ID_0
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
    explorationDataController.stopPlayingExploration(isCompletion = false)
    explorationDataController.replayExploration(
      INTERNAL_PROFILE_ID,
      CLASSROOM_ID,
      TOPIC_ID,
      STORY_ID,
      EXPLORATION_ID
    ).toLiveData().observe(
      activity,
      Observer<AsyncResult<Any?>> { result ->
        when (result) {
          is AsyncResult.Pending ->
            oppiaLogger.d(TAG_EXPLORATION_TEST_ACTIVITY, "Loading exploration")
          is AsyncResult.Failure ->
            oppiaLogger.e(TAG_EXPLORATION_TEST_ACTIVITY, "Failed to load exploration", result.error)
          is AsyncResult.Success -> {
            oppiaLogger.d(TAG_EXPLORATION_TEST_ACTIVITY, "Successfully loaded exploration")
            routeToExplorationListener.routeToExploration(
              ProfileId.newBuilder().apply { loggedInInternalProfileId = INTERNAL_PROFILE_ID }.build(),
              CLASSROOM_ID,
              TOPIC_ID,
              STORY_ID,
              EXPLORATION_ID,
              parentScreen = ExplorationActivityParams.ParentScreen.PARENT_SCREEN_UNSPECIFIED,
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

  class TestFragment : InjectableFragment() {
    @Inject lateinit var splitScreenManager: SplitScreenManager

    override fun onAttach(context: Context) {
      super.onAttach(context)
      (fragmentComponent as FragmentComponentImpl).inject(this)
    }
  }
}
