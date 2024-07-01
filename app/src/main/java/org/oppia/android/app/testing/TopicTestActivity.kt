package org.oppia.android.app.testing

import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.home.RouteToExplorationListener
import org.oppia.android.app.model.ExplorationActivityParams
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.player.exploration.ExplorationActivity
import org.oppia.android.app.story.StoryActivity
import org.oppia.android.app.topic.RouteToQuestionPlayerListener
import org.oppia.android.app.topic.RouteToRevisionCardListener
import org.oppia.android.app.topic.RouteToStoryListener
import org.oppia.android.app.topic.TopicActivityPresenter
import org.oppia.android.app.topic.TopicFragment
import org.oppia.android.app.topic.questionplayer.QuestionPlayerActivity
import org.oppia.android.app.topic.revisioncard.RevisionCardActivity
import org.oppia.android.domain.classroom.TEST_CLASSROOM_ID_0
import org.oppia.android.domain.topic.TEST_TOPIC_ID_0
import javax.inject.Inject

/** The activity for testing [TopicFragment]. */
class TopicTestActivity :
  InjectableAutoLocalizedAppCompatActivity(),
  RouteToQuestionPlayerListener,
  RouteToStoryListener,
  RouteToExplorationListener,
  RouteToRevisionCardListener {

  @Inject
  lateinit var topicActivityPresenter: TopicActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    topicActivityPresenter.handleOnCreate(
      internalProfileId = 0,
      classroomId = TEST_CLASSROOM_ID_0,
      topicId = TEST_TOPIC_ID_0,
      storyId = ""
    )
  }

  override fun routeToQuestionPlayer(skillIdList: ArrayList<String>) {
    startActivity(
      QuestionPlayerActivity.createQuestionPlayerActivityIntent(
        this, skillIdList, ProfileId.getDefaultInstance()
      )
    )
  }

  override fun routeToStory(
    internalProfileId: Int,
    classroomId: String,
    topicId: String,
    storyId: String
  ) {
    startActivity(
      StoryActivity.createStoryActivityIntent(
        this,
        internalProfileId,
        classroomId,
        topicId,
        storyId
      )
    )
  }

  override fun routeToExploration(
    profileId: ProfileId,
    topicId: String,
    storyId: String,
    explorationId: String,
    parentScreen: ExplorationActivityParams.ParentScreen,
    isCheckpointingEnabled: Boolean
  ) {
    startActivity(
      ExplorationActivity.createExplorationActivityIntent(
        this,
        profileId,
        topicId,
        storyId,
        explorationId,
        parentScreen,
        isCheckpointingEnabled
      )
    )
  }

  override fun routeToRevisionCard(
    internalProfileId: Int,
    topicId: String,
    subtopicId: Int,
    subtopicListSize: Int
  ) {
    startActivity(
      RevisionCardActivity.createRevisionCardActivityIntent(
        this, internalProfileId, topicId, subtopicId, subtopicListSize
      )
    )
  }
}
