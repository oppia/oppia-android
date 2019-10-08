package org.oppia.app.topic

import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.topic.questionplayer.QuestionPlayerActivity
import javax.inject.Inject

/** The activity for tabs in Topic. */
class TopicActivity : InjectableAppCompatActivity(), RouteToQuestionPlayerListener {
  @Inject lateinit var topicActivityPresenter: TopicActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    topicActivityPresenter.handleOnCreate()
  }

  override fun routeToQuestionPlayer(skillIdList: ArrayList<String>) {
    startActivity(QuestionPlayerActivity.createQuestionPlayerActivityIntent(this, skillIdList))
  }
}
