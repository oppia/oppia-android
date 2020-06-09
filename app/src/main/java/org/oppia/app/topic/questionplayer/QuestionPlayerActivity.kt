package org.oppia.app.topic.questionplayer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

const val QUESTION_PLAYER_ACTIVITY_SKILL_ID_LIST_ARGUMENT_KEY =
  "QuestionPlayerActivity.skill_id_list"

/** Activity for QuestionPlayer in practice mode. */
class QuestionPlayerActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var questionPlayerActivityPresenter: QuestionPlayerActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    questionPlayerActivityPresenter.handleOnCreate()
  }

  companion object {
    // TODO(#159): Use this skillList from TopicPracticeFragment to fetch questions and start practice mode.
    /** Returns a new [Intent] to route to [QuestionPlayerActivity] for a specified skill ID list. */
    fun createQuestionPlayerActivityIntent(
      context: Context,
      skillIdList: ArrayList<String>
    ): Intent {
      val intent = Intent(context, QuestionPlayerActivity::class.java)
      intent.putExtra(QUESTION_PLAYER_ACTIVITY_SKILL_ID_LIST_ARGUMENT_KEY, skillIdList)
      return intent
    }

    fun getIntentKey(): String {
      return QUESTION_PLAYER_ACTIVITY_SKILL_ID_LIST_ARGUMENT_KEY
    }
  }
}
