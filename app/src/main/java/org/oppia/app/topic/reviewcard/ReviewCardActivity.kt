package org.oppia.app.topic.reviewcard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

const val TOPIC_ID_ARGUMENT_KEY = "TOPIC_ID_"
const val SUBTOPIC_ID_ARGUMENT_KEY = "SUBTOPIC_ID"

/** Activity for review card. */
class ReviewCardActivity : InjectableAppCompatActivity() {

  @Inject lateinit var reviewCardActivityPresenter: ReviewCardActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    reviewCardActivityPresenter.handleOnCreate()
  }

  companion object {
    /** Returns a new [Intent] to route to [ReviewCardActivity]. */
    fun createReviewCardActivityIntent(context: Context, topicId: String, subtopicId: String): Intent {
      val intent = Intent(context, ReviewCardActivity::class.java)
      intent.putExtra(TOPIC_ID_ARGUMENT_KEY, topicId)
      intent.putExtra(SUBTOPIC_ID_ARGUMENT_KEY, subtopicId)
      return intent
    }
  }
}
