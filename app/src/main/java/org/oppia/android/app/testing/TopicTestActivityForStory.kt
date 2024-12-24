package org.oppia.android.app.testing

import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.home.RouteToExplorationListener
import org.oppia.android.app.model.ExplorationActivityParams
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.player.exploration.ExplorationActivity
import org.oppia.android.app.resumelesson.ResumeLessonActivity
import org.oppia.android.app.story.StoryActivity
import org.oppia.android.app.topic.RouteToQuestionPlayerListener
import org.oppia.android.app.topic.RouteToResumeLessonListener
import org.oppia.android.app.topic.RouteToRevisionCardListener
import org.oppia.android.app.topic.RouteToStoryListener
import org.oppia.android.app.topic.TopicActivityPresenter
import org.oppia.android.app.topic.TopicFragment
import org.oppia.android.app.topic.questionplayer.QuestionPlayerActivity
import org.oppia.android.app.topic.revisioncard.RevisionCardActivity
import org.oppia.android.domain.classroom.TEST_CLASSROOM_ID_0
import org.oppia.android.domain.topic.TEST_STORY_ID_0
import org.oppia.android.domain.topic.TEST_TOPIC_ID_0
import javax.inject.Inject

/** The test activity for [TopicFragment] to test displaying story by storyId. */
class TopicTestActivityForStory :
  InjectableAutoLocalizedAppCompatActivity(),
  RouteToQuestionPlayerListener,
  RouteToStoryListener,
  RouteToResumeLessonListener,
  RouteToExplorationListener,
  RouteToRevisionCardListener {

  @Inject
  lateinit var topicActivityPresenter: TopicActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val profileId = ProfileId.newBuilder().setInternalId(0).build()
    (activityComponent as ActivityComponentImpl).inject(this)
    topicActivityPresenter.handleOnCreate(
      profileId = profileId,
      classroomId = TEST_CLASSROOM_ID_0,
      topicId = TEST_TOPIC_ID_0,
      storyId = TEST_STORY_ID_0
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

  override fun routeToResumeLesson(
    profileId: ProfileId,
    classroomId: String,
    topicId: String,
    storyId: String,
    explorationId: String,
    parentScreen: ExplorationActivityParams.ParentScreen,
    explorationCheckpoint: ExplorationCheckpoint
  ) {
    startActivity(
      ResumeLessonActivity.createResumeLessonActivityIntent(
        this,
        profileId,
        classroomId,
        topicId,
        storyId,
        explorationId,
        parentScreen,
        explorationCheckpoint
      )
    )
  }

  override fun routeToExploration(
    profileId: ProfileId,
    classroomId: String,
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
        classroomId,
        topicId,
        storyId,
        explorationId,
        parentScreen,
        isCheckpointingEnabled
      )
    )
  }

  override fun routeToRevisionCard(
    profileId: ProfileId,
    topicId: String,
    subtopicId: Int,
    subtopicListSize: Int
  ) {
    startActivity(
      RevisionCardActivity.createRevisionCardActivityIntent(
        this, profileId, topicId, subtopicId, subtopicListSize
      )
    )
  }
}
