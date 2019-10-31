package org.oppia.app.topic

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.story.StoryActivity
import org.oppia.app.topic.conceptcard.ConceptCardFragment
import org.oppia.app.topic.questionplayer.QuestionPlayerActivity
import javax.inject.Inject

/** The activity for tabs in Topic. */

class TopicActivity : InjectableAppCompatActivity(), RouteToQuestionPlayerListener, RouteToConceptCardListener,
  RouteToTopicPlayListener, RouteToStoryListener {
  @Inject
  lateinit var topicActivityPresenter: TopicActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    topicActivityPresenter.handleOnCreate()
  }

  override fun routeToQuestionPlayer(skillIdList: ArrayList<String>) {
    startActivity(QuestionPlayerActivity.createQuestionPlayerActivityIntent(this, skillIdList))
  }

  override fun routeToStory(storyId: String) {
    startActivity(StoryActivity.createStoryActivityIntent(this, storyId))
  }

  override fun routeToTopicPlayFragment() {
    // TODO(#135): Change to play tab in this function.
  }

  override fun routeToConceptCard(skillId: String) {
    if (getConceptCardFragment() == null) {
      val conceptCardFragment: ConceptCardFragment = ConceptCardFragment.newInstance(skillId)
      conceptCardFragment.showNow(supportFragmentManager, TAG_CONCEPT_CARD_DIALOG)
    }
  }

  private fun getConceptCardFragment(): ConceptCardFragment? {
    return supportFragmentManager.findFragmentByTag(TAG_CONCEPT_CARD_DIALOG) as ConceptCardFragment?
  }

  companion object {
    internal const val TAG_CONCEPT_CARD_DIALOG = "CONCEPT_CARD_DIALOG"
    internal const val TOPIC_ACTIVITY_TOPIC_ID_ARGUMENT_KEY = "TopicActivity.topic_id"

    /** Returns a new [Intent] to route to [TopicActivity] for a specified topic ID. */
    fun createTopicActivityIntent(context: Context, topicId: String): Intent {
      val intent = Intent(context, TopicActivity::class.java)
      intent.putExtra(TOPIC_ACTIVITY_TOPIC_ID_ARGUMENT_KEY, topicId)
      return intent
    }
  }
}
