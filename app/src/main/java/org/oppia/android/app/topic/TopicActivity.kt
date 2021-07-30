package org.oppia.android.app.topic

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.drawer.NAVIGATION_PROFILE_ID_ARGUMENT_KEY
import org.oppia.android.app.home.RouteToExplorationListener
import org.oppia.android.app.player.exploration.ExplorationActivity
import org.oppia.android.app.story.StoryActivity
import org.oppia.android.app.topic.questionplayer.QuestionPlayerActivity
import org.oppia.android.app.topic.revisioncard.RevisionCardActivity
import javax.inject.Inject

private const val TOPIC_ACTIVITY_TOPIC_ID_ARGUMENT_KEY = "TopicActivity.topic_id"
private const val TOPIC_ACTIVITY_STORY_ID_ARGUMENT_KEY = "TopicActivity.story_id"

/** The activity for displaying [TopicFragment]. */
class TopicActivity :
  InjectableAppCompatActivity(),
  RouteToQuestionPlayerListener,
  RouteToStoryListener,
  RouteToExplorationListener,
  RouteToRevisionCardListener {

  private var internalProfileId: Int = -1
  private lateinit var topicId: String
  private var storyId: String? = null

  @Inject
  lateinit var topicActivityPresenter: TopicActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    internalProfileId = intent?.getIntExtra(NAVIGATION_PROFILE_ID_ARGUMENT_KEY, -1)!!
    topicId = checkNotNull(intent?.getStringExtra(TOPIC_ACTIVITY_TOPIC_ID_ARGUMENT_KEY)) {
      "Expected topic ID to be included in intent for TopicActivity."
    }
    storyId = intent?.getStringExtra(TOPIC_ACTIVITY_STORY_ID_ARGUMENT_KEY)
    topicActivityPresenter.handleOnCreate(internalProfileId, topicId, storyId)
  }

  override fun routeToQuestionPlayer(skillIdList: ArrayList<String>) {
    startActivity(
      QuestionPlayerActivity.createQuestionPlayerActivityIntent(
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

  override fun routeToRevisionCard(internalProfileId: Int, topicId: String, subtopicId: Int) {
    startActivity(
      RevisionCardActivity.createRevisionCardActivityIntent(
        this,
        internalProfileId,
        topicId,
        subtopicId
      )
    )
  }

  override fun routeToExploration(
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String,
    backflowScreen: Int?,
    isCheckpointingEnabled: Boolean
  ) {
    startActivity(
      ExplorationActivity.createExplorationActivityIntent(
        this,
        internalProfileId,
        topicId,
        storyId,
        explorationId,
        backflowScreen,
        isCheckpointingEnabled
      )
    )
  }

  override fun onBackPressed() {
    finish()
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
    fun createTopicActivityIntent(
      context: Context,
      internalProfileId: Int,
      topicId: String
    ): Intent {
      val intent = Intent(context, TopicActivity::class.java)
      intent.putExtra(NAVIGATION_PROFILE_ID_ARGUMENT_KEY, internalProfileId)
      intent.putExtra(TOPIC_ACTIVITY_TOPIC_ID_ARGUMENT_KEY, topicId)
      return intent
    }

    /** Returns a new [Intent] to route to [TopicLessonsFragment] for a specified story ID. */
    fun createTopicPlayStoryActivityIntent(
      context: Context,
      internalProfileId: Int,
      topicId: String,
      storyId: String
    ): Intent {
      val intent = Intent(context, TopicActivity::class.java)
      intent.putExtra(NAVIGATION_PROFILE_ID_ARGUMENT_KEY, internalProfileId)
      intent.putExtra(TOPIC_ACTIVITY_TOPIC_ID_ARGUMENT_KEY, topicId)
      intent.putExtra(TOPIC_ACTIVITY_STORY_ID_ARGUMENT_KEY, storyId)
      return intent
    }
  }
}
