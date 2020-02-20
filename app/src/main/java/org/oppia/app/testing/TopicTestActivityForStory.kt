package org.oppia.app.testing

import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.home.RouteToExplorationListener
import org.oppia.app.player.exploration.ExplorationActivity
import org.oppia.app.story.StoryActivity
import org.oppia.app.topic.RouteToQuestionPlayerListener
import org.oppia.app.topic.RouteToReviewCardListener
import org.oppia.app.topic.RouteToStoryListener
import org.oppia.app.topic.TopicActivityPresenter
import org.oppia.app.topic.TopicFragment
import org.oppia.app.topic.questionplayer.QuestionPlayerActivity
import org.oppia.app.topic.reviewcard.ReviewCardActivity
import org.oppia.domain.topic.TEST_STORY_ID_1
import org.oppia.domain.topic.TEST_TOPIC_ID_0
import javax.inject.Inject

/** The test activity for [TopicFragment] to test displaying story by storyId. */
class TopicTestActivityForStory : InjectableAppCompatActivity(), RouteToQuestionPlayerListener,
  RouteToStoryListener, RouteToExplorationListener, RouteToReviewCardListener {

  @Inject lateinit var topicActivityPresenter: TopicActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    topicActivityPresenter.handleOnCreate(topicId = TEST_TOPIC_ID_0, storyId = TEST_STORY_ID_1)
  }

  override fun routeToQuestionPlayer(skillIdList: ArrayList<String>) {
    startActivity(QuestionPlayerActivity.createQuestionPlayerActivityIntent(this, skillIdList))
  }

  override fun routeToStory(storyId: String) {
    startActivity(StoryActivity.createStoryActivityIntent(this, storyId))
  }

  override fun routeToExploration(explorationId: String, topicId: String?) {
    startActivity(ExplorationActivity.createExplorationActivityIntent(this, explorationId, topicId))
  }

  override fun routeToReviewCard(topicId: String, subtopicId: String) {
    startActivity(ReviewCardActivity.createReviewCardActivityIntent(this, topicId, subtopicId))
  }
}
