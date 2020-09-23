package org.oppia.android.app.testing

import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.home.RouteToExplorationListener
import org.oppia.android.app.player.exploration.ExplorationActivity
import org.oppia.android.app.story.StoryActivity
import org.oppia.android.app.topic.RouteToQuestionPlayerListener
import org.oppia.android.app.topic.RouteToRevisionCardListener
import org.oppia.android.app.topic.RouteToStoryListener
import org.oppia.android.app.topic.TopicActivityPresenter
import org.oppia.android.app.topic.TopicFragment
import org.oppia.android.app.topic.questionplayer.QuestionPlayerActivity
import org.oppia.android.app.topic.revisioncard.RevisionCardActivity
import org.oppia.android.domain.topic.TEST_STORY_ID_1
import org.oppia.android.domain.topic.TEST_TOPIC_ID_0
import javax.inject.Inject

/** The test activity for [TopicFragment] to test displaying story by storyId. */
class TopicTestActivityForStory :
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
      storyId = TEST_STORY_ID_1
    )
  }

  override fun routeToQuestionPlayer(skillIdList: ArrayList<String>) {
    startActivity(
      QuestionPlayerActivity.createQuestionPlayerActivityIntent(
        this, skillIdList
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
    profileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String,
    backflowScreen: Int?
  ) {
    startActivity(
      ExplorationActivity.createExplorationActivityIntent(
        this,
        profileId,
        topicId,
        storyId,
        explorationId,
        backflowScreen
      )
    )
  }

  override fun routeToRevisionCard(internalProfileId: Int, topicId: String, subtopicId: Int) {
    startActivity(
      RevisionCardActivity.createRevisionCardActivityIntent(
        this, internalProfileId, topicId, subtopicId
      )
    )
  }
}
