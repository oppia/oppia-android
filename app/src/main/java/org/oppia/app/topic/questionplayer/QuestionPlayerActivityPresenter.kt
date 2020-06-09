package org.oppia.app.topic.questionplayer

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import javax.inject.Inject

/** The presenter for [QuestionPlayerActivity]. */
@ActivityScope
class QuestionPlayerActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  fun handleOnCreate() {
    activity.setContentView(R.layout.question_player_activity)
    if (getQuestionPlayerFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.question_player_fragment_placeholder,
        QuestionPlayerFragment()
      ).commitNow()
    }
  }

  private fun getQuestionPlayerFragment(): QuestionPlayerFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(R.id.question_player_fragment_placeholder) as QuestionPlayerFragment?
  }
}
