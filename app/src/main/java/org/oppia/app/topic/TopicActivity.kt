package org.oppia.app.topic

import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.topic.questionplayer.QuestionPlayerActivity
import javax.inject.Inject

/** The activity for tabs in Topic. */
class TopicActivity : InjectableAppCompatActivity(), RouteToQuestionPlayerListener, RouteToStoryListener {

  @Inject lateinit var topicActivityPresenter: TopicActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    topicActivityPresenter.handleOnCreate()
  }

  override fun routeToQuestionPlayer(skillIdList: ArrayList<String>) {
    startActivity(QuestionPlayerActivity.createQuestionPlayerActivityIntent(this, skillIdList))
  }
  override fun routeToStory(storyId: String) {
    // TODO(135): Once StoryActivity is introduced from PR #195, intent this to that activity.
  }
}
