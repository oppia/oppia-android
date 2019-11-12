package org.oppia.app.topic.questionplayer

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.domain.question.QuestionTrainingController
import javax.inject.Inject

/** The presenter for [QuestionPlayerActivity]. */
@ActivityScope
class QuestionPlayerActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val questionTrainingController: QuestionTrainingController
) {
  fun handleOnCreate() {
    activity.setContentView(R.layout.question_player_activity)
    if (getQuestionPlayerFragment() == null) {
      val skillIds = activity.intent.getStringArrayListExtra(QUESTION_PLAYER_ACTIVITY_SKILL_ID_LIST_ARGUMENT_KEY)
      questionTrainingController.startQuestionTrainingSession(skillIds).observe(activity, Observer {
        if (it.isSuccess()) {
          activity.supportFragmentManager.beginTransaction().add(
            R.id.question_player_fragment_placeholder,
            QuestionPlayerFragment()
          ).commitNow()
        }
      })
    }
  }

  private fun getQuestionPlayerFragment(): QuestionPlayerFragment? {
    return activity.supportFragmentManager.findFragmentById(R.id.question_player_fragment_placeholder) as QuestionPlayerFragment?
  }
}
