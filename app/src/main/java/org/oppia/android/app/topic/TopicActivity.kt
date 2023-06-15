package org.oppia.android.app.topic

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.ActivityIntentFactories
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.drawer.NAVIGATION_PROFILE_ID_ARGUMENT_KEY
import org.oppia.android.app.home.RouteToExplorationListener
import org.oppia.android.app.model.ExplorationActivityParams
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ScreenName.TOPIC_ACTIVITY
import org.oppia.android.app.player.exploration.ExplorationActivity
import org.oppia.android.app.resumelesson.ResumeLessonActivity
import org.oppia.android.app.story.StoryActivity
import org.oppia.android.app.topic.questionplayer.QuestionPlayerActivity
import org.oppia.android.app.topic.revisioncard.RevisionCardActivity
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

private const val TOPIC_ACTIVITY_TOPIC_ID_ARGUMENT_KEY = "TopicActivity.topic_id"
private const val TOPIC_ACTIVITY_STORY_ID_ARGUMENT_KEY = "TopicActivity.story_id"

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
  private var storyId: String? = null

  @Inject
  lateinit var topicActivityPresenter: TopicActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    profileId = intent.extractCurrentUserProfileId()
    topicId = checkNotNull(intent?.getStringExtra(TOPIC_ACTIVITY_TOPIC_ID_ARGUMENT_KEY)) {
      "Expected topic ID to be included in intent for TopicActivity."
    }
    storyId = intent?.getStringExtra(TOPIC_ACTIVITY_STORY_ID_ARGUMENT_KEY)
    topicActivityPresenter.handleOnCreate(profileId, topicId, storyId)
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

  override fun routeToStory(profileId: ProfileId, topicId: String, storyId: String) {
    startActivity(
      StoryActivity.createStoryActivityIntent(
        this,
        profileId,
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

  override fun routeToResumeLesson(
    profileId: ProfileId,
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
    override fun createIntent(profileId: ProfileId, topicId: String): Intent =
      createTopicActivityIntent(activity, profileId, topicId)

    override fun createIntent(profileId: ProfileId, topicId: String, storyId: String): Intent =
      createTopicPlayStoryActivityIntent(activity, profileId, topicId, storyId)
  }

  companion object {

    fun getTopicIdKey(): String {
      return TOPIC_ACTIVITY_TOPIC_ID_ARGUMENT_KEY
    }

    fun getStoryIdKey(): String {
      return TOPIC_ACTIVITY_STORY_ID_ARGUMENT_KEY
    }

    /** Returns a new [Intent] to route to [TopicActivity] for a specified topic ID. */
    fun createTopicActivityIntent(
      context: Context,
      profileId: ProfileId,
      topicId: String
    ): Intent {
      return Intent(context, TopicActivity::class.java).apply {
        decorateWithUserProfileId(profileId)
        putExtra(TOPIC_ACTIVITY_TOPIC_ID_ARGUMENT_KEY, topicId)
        decorateWithScreenName(TOPIC_ACTIVITY)
      }
    }

    /** Returns a new [Intent] to route to [TopicLessonsFragment] for a specified story ID. */
    fun createTopicPlayStoryActivityIntent(
      context: Context,
      profileId: ProfileId,
      topicId: String,
      storyId: String
    ): Intent {
      return createTopicActivityIntent(context, profileId, topicId).apply {
        putExtra(TOPIC_ACTIVITY_STORY_ID_ARGUMENT_KEY, storyId)
      }
    }
  }
}
