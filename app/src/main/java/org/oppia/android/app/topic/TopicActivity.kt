package org.oppia.android.app.topic

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.activity.ActivityIntentFactories
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.activity.route.ActivityRouter
import org.oppia.android.app.drawer.NAVIGATION_PROFILE_ID_ARGUMENT_KEY
import org.oppia.android.app.home.RouteToExplorationListener
import org.oppia.android.app.model.DestinationScreen
import org.oppia.android.app.model.ExplorationActivityParams
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.QuestionPlayerActivityParams
import org.oppia.android.app.model.ResumeLessonActivityParams
import org.oppia.android.app.model.RevisionCardActivityParams
import org.oppia.android.app.model.ScreenName.TOPIC_ACTIVITY
import org.oppia.android.app.model.StoryActivityParams
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

private const val TOPIC_ACTIVITY_TOPIC_ID_ARGUMENT_KEY = "TopicActivity.topic_id"
private const val TOPIC_ACTIVITY_STORY_ID_ARGUMENT_KEY = "TopicActivity.story_id"

/** The activity for displaying [TopicFragment]. */
class TopicActivity :
  InjectableAppCompatActivity(),
  RouteToQuestionPlayerListener,
  RouteToStoryListener,
  RouteToExplorationListener,
  RouteToResumeLessonListener,
  RouteToRevisionCardListener {
  @Inject lateinit var topicActivityPresenter: TopicActivityPresenter
  @Inject lateinit var activityRouter: ActivityRouter

  private var internalProfileId: Int = -1
  private lateinit var topicId: String
  private var storyId: String? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as Injector).inject(this)
    internalProfileId = intent?.getIntExtra(NAVIGATION_PROFILE_ID_ARGUMENT_KEY, -1)!!
    topicId = checkNotNull(intent?.getStringExtra(TOPIC_ACTIVITY_TOPIC_ID_ARGUMENT_KEY)) {
      "Expected topic ID to be included in intent for TopicActivity."
    }
    storyId = intent?.getStringExtra(TOPIC_ACTIVITY_STORY_ID_ARGUMENT_KEY)
    topicActivityPresenter.handleOnCreate(internalProfileId, topicId, storyId)
  }

  override fun routeToQuestionPlayer(skillIdList: ArrayList<String>) {
    activityRouter.routeToScreen(
      DestinationScreen.newBuilder().apply {
        questionPlayerActivityParams = QuestionPlayerActivityParams.newBuilder().apply {
          addAllSkillIds(skillIdList)
          this.profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
        }.build()
      }.build()
    )
  }

  override fun routeToStory(internalProfileId: Int, topicId: String, storyId: String) {
    activityRouter.routeToScreen(
      DestinationScreen.newBuilder().apply {
        storyActivityParams = StoryActivityParams.newBuilder().apply {
          this.internalProfileId = internalProfileId
          this.topicId = topicId
          this.storyId = storyId
        }.build()
      }.build()
    )
  }

  override fun routeToRevisionCard(
    internalProfileId: Int,
    topicId: String,
    subtopicId: Int,
    subtopicListSize: Int
  ) {
    activityRouter.routeToScreen(
      DestinationScreen.newBuilder().apply {
        revisionCardActivityParams = RevisionCardActivityParams.newBuilder().apply {
          this.internalProfileId = internalProfileId
          this.topicId = topicId
          this.subtopicIndex = subtopicId
          this.subtopicListSize = subtopicListSize
        }.build()
      }.build()
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
    activityRouter.routeToScreen(
      DestinationScreen.newBuilder().apply {
        explorationActivityParams = ExplorationActivityParams.newBuilder().apply {
          this.profileId = profileId
          this.topicId = topicId
          this.storyId = storyId
          this.explorationId = explorationId
          this.parentScreen = parentScreen
          this.isCheckpointingEnabled = isCheckpointingEnabled
        }.build()
      }.build()
    )
  }

  override fun routeToResumeLesson(
    profileId: ProfileId,
    topicId: String,
    storyId: String,
    explorationId: String,
    parentScreen: ExplorationActivityParams.ParentScreen,
    explorationCheckpoint: ExplorationCheckpoint
  ) {
    activityRouter.routeToScreen(
      DestinationScreen.newBuilder().apply {
        resumeLessonActivityParams = ResumeLessonActivityParams.newBuilder().apply {
          this.profileId = profileId
          this.topicId = topicId
          this.storyId = storyId
          this.explorationId = explorationId
          this.parentScreen = parentScreen
          this.checkpoint = explorationCheckpoint
        }.build()
      }.build()
    )
  }

  override fun onBackPressed() {
    finish()
  }

  class TopicActivityIntentFactoryImpl @Inject constructor(
    private val activity: AppCompatActivity
  ) : ActivityIntentFactories.TopicActivityIntentFactory {
    override fun createIntent(profileId: ProfileId, topicId: String): Intent =
      Companion.createIntent(activity, profileId.internalId, topicId)

    override fun createIntent(profileId: ProfileId, topicId: String, storyId: String): Intent =
      Companion.createIntent(activity, profileId.internalId, topicId, storyId)
  }

  interface Injector {
    fun inject(activity: TopicActivity)
  }

  companion object {

    fun getProfileIdKey(): String {
      return NAVIGATION_PROFILE_ID_ARGUMENT_KEY
    }

    fun getTopicIdKey(): String {
      return TOPIC_ACTIVITY_TOPIC_ID_ARGUMENT_KEY
    }

    fun getStoryIdKey(): String {
      return TOPIC_ACTIVITY_STORY_ID_ARGUMENT_KEY
    }

    /** Returns a new [Intent] to route to [TopicActivity] for a specified topic ID. */
    fun createIntent(
      context: Context,
      internalProfileId: Int,
      topicId: String
    ): Intent {
      return Intent(context, TopicActivity::class.java).apply {
        putExtra(NAVIGATION_PROFILE_ID_ARGUMENT_KEY, internalProfileId)
        putExtra(TOPIC_ACTIVITY_TOPIC_ID_ARGUMENT_KEY, topicId)
        decorateWithScreenName(TOPIC_ACTIVITY)
      }
    }

    /** Returns a new [Intent] to route to [TopicLessonsFragment] for a specified story ID. */
    fun createIntent(
      context: Context,
      internalProfileId: Int,
      topicId: String,
      storyId: String
    ): Intent {
      return createIntent(context, internalProfileId, topicId).apply {
        putExtra(TOPIC_ACTIVITY_STORY_ID_ARGUMENT_KEY, storyId)
      }
    }
  }
}
