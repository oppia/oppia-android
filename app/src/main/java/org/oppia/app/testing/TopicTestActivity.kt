package org.oppia.app.testing

import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.home.RouteToExplorationListener
import org.oppia.app.player.exploration.ExplorationActivity
import org.oppia.app.story.StoryActivity
import org.oppia.app.topic.RouteToQuestionPlayerListener
import org.oppia.app.topic.RouteToRevisionCardListener
import org.oppia.app.topic.RouteToStoryListener
import org.oppia.app.topic.TopicActivityPresenter
import org.oppia.app.topic.TopicFragment
import org.oppia.app.topic.questionplayer.QuestionPlayerActivity
import org.oppia.app.topic.revisioncard.RevisionCardActivity
import org.oppia.domain.topic.TEST_TOPIC_ID_0
import javax.inject.Inject

/** The activity for testing [TopicFragment]. */
class TopicTestActivity :
  InjectableAppCompatActivity(),
  RouteToQuestionPlayerListener,
  RouteToStoryListener,
  RouteToExplorationListener,
  RouteToRevisionCardListener {

  @Inject
  lateinit var topicActivityPresenter: TopicActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    topicActivityPresenter.handleOnCreate(
      internalProfileId = 0,
      topicId = TEST_TOPIC_ID_0,
      storyId = ""
    )
  }

  override fun routeToQuestionPlayer(skillIdList: ArrayList<String>) {
    startActivity(
      QuestionPlayerActivity
        .createQuestionPlayerActivityIntent(
          this,
          skillIdList
        )
    )
  }

  override fun routeToStory(internalProfileId: Int, topicId: String, storyId: String) {
    startActivity(
      StoryActivity.createStoryActivityIntent(
        this,
        internalProfileId,
        topicId,
        storyId
      )
    )
  }

  override fun routeToExploration(
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String,
    backflowScreen: Int?
  ) {
    startActivity(
      ExplorationActivity.createExplorationActivityIntent(
        this,
        internalProfileId,
        topicId,
        storyId,
        explorationId,
        backflowScreen
      )
    )
  }

  override fun routeToRevisionCard(topicId: String, subtopicId: String) {
    startActivity(
      RevisionCardActivity
        .createRevisionCardActivityIntent(
          this,
          topicId,
          subtopicId
        )
    )
  }
}
