package org.oppia.app.topic.questionplayer

import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** Activity for QuestionPlayer in train mode. */
class QuestionPlayerActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var questionPlayerActivityPresenter: QuestionPlayerActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    questionPlayerActivityPresenter.handleOnCreate()

    // TODO(#159): Use this skillList from TopicTrainFragment to fetch questions and start train mode.
    val skillList: ArrayList<String> = getSkillList()
  }

  private fun getSkillList(): ArrayList<String> {
    val topicTrainIntent = intent
    return topicTrainIntent.getStringArrayListExtra("SKILL_LIST")
  }
}
