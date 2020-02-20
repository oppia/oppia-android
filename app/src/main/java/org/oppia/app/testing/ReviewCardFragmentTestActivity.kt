package org.oppia.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.topic.reviewcard.SUBTOPIC_ID_ARGUMENT_KEY
import org.oppia.app.topic.reviewcard.TOPIC_ID_ARGUMENT_KEY
import javax.inject.Inject

/** Test activity for recent stories. */
class ReviewCardFragmentTestActivity : InjectableAppCompatActivity() {
  @Inject lateinit var reviewCardFragmentTestActivityPresenter: ReviewCardFragmentTestActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    reviewCardFragmentTestActivityPresenter.handleOnCreate()
  }

  companion object {
    /** Returns a new [Intent] to route to [ContinuePlayingActivity]. */
    fun createReviewCardActivityIntent(context: Context, topicId: String, subtopicId: String): Intent {
      val intent = Intent(context, ReviewCardFragmentTestActivity::class.java)
      intent.putExtra(TOPIC_ID_ARGUMENT_KEY, topicId)
      intent.putExtra(SUBTOPIC_ID_ARGUMENT_KEY, subtopicId)
      return intent
    }
  }
}
