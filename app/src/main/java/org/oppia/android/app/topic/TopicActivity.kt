package org.oppia.android.app.topic

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.ActivityIntentFactories
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.home.RouteToExplorationListener
import org.oppia.android.app.model.ExplorationActivityParams
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ScreenName.TOPIC_ACTIVITY
import org.oppia.android.app.model.TopicActivityParams
import org.oppia.android.app.player.exploration.ExplorationActivity
import org.oppia.android.app.resumelesson.ResumeLessonActivity
import org.oppia.android.app.story.StoryActivity
import org.oppia.android.app.topic.questionplayer.QuestionPlayerActivity
import org.oppia.android.app.topic.revisioncard.RevisionCardActivity
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProtoExtra
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** The activity for displaying [TopicFragment]. */
class TopicActivity :
  InjectableAutoLocalizedAppCompatActivity(),
  RouteToQuestionPlayerListener,
  RouteToStoryListener,
  RouteToExplorationListener,
  RouteToResumeLessonListener,
  RouteToRevisionCardListener {

  private lateinit var profileId: ProfileId
  private lateinit var topicId: String
  private lateinit var classroomId: String
  private var storyId: String? = null

  @Inject
  lateinit var topicActivityPresenter: TopicActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    profileId = intent?.extractCurrentUserProfileId() ?: ProfileId.getDefaultInstance()

    val args = intent?.getProtoExtra(
      TOPIC_ACTIVITY_PARAMS_KEY,
      TopicActivityParams.getDefaultInstance()
    )
    classroomId = checkNotNull(args?.classroomId) {
      "Expected classroom ID to be included in intent for TopicActivity."
    }
    topicId = checkNotNull(args?.topicId) {
      "Expected topic ID to be included in intent for TopicActivity."
    }
    storyId = args?.storyId
    topicActivityPresenter.handleOnCreate(profileId, classroomId, topicId, storyId)
  }

  override fun routeToQuestionPlayer(skillIdList: ArrayList<String>) {
    startActivity(
      QuestionPlayerActivity.createQuestionPlayerActivityIntent(
        this,
        skillIdList,
        profileId
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

  override fun routeToRevisionCard(
    profileId: ProfileId,
    topicId: String,
    subtopicId: Int,
    subtopicListSize: Int
  ) {
    startActivity(
      RevisionCardActivity.createRevisionCardActivityIntent(
        this,
        profileId,
        topicId,
        subtopicId,
        subtopicListSize
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

  override fun onBackPressed() {
    finish()
  }

  class TopicActivityIntentFactoryImpl @Inject constructor(
    private val activity: AppCompatActivity
  ) : ActivityIntentFactories.TopicActivityIntentFactory {
    override fun createIntent(profileId: ProfileId, classroomId: String, topicId: String): Intent =
      createTopicActivityIntent(activity, profileId, classroomId, topicId)

    override fun createIntent(
      profileId: ProfileId,
      classroomId: String,
      topicId: String,
      storyId: String
    ): Intent =
      createTopicPlayStoryActivityIntent(
        activity,
        profileId,
        classroomId,
        topicId,
        storyId
      )
  }

  companion object {
    /** Params key for TopicActivity. */
    const val TOPIC_ACTIVITY_PARAMS_KEY = "TopicActivity.params"

    /** Returns a new [Intent] to route to [TopicActivity] for a specified topic ID. */
    fun createTopicActivityIntent(
      context: Context,
      profileId: ProfileId,
      classroomId: String,
      topicId: String
    ): Intent {
      val args = TopicActivityParams.newBuilder().apply {
        this.topicId = topicId
        this.classroomId = classroomId
      }.build()
      return Intent(context, TopicActivity::class.java).apply {
        putProtoExtra(TOPIC_ACTIVITY_PARAMS_KEY, args)
        decorateWithUserProfileId(profileId)
        decorateWithScreenName(TOPIC_ACTIVITY)
      }
    }

    /** Returns a new [Intent] to route to [TopicLessonsFragment] for a specified story ID. */
    fun createTopicPlayStoryActivityIntent(
      context: Context,
      profileId: ProfileId,
      classroomId: String,
      topicId: String,
      storyId: String
    ): Intent {
      return createTopicActivityIntent(context, profileId, classroomId, topicId).apply {
        val args =
          getProtoExtra(TOPIC_ACTIVITY_PARAMS_KEY, TopicActivityParams.getDefaultInstance())
        val updateArg = args.toBuilder().setStoryId(storyId).build()
        putProtoExtra(TOPIC_ACTIVITY_PARAMS_KEY, updateArg)
      }
    }
  }
}
