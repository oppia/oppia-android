package org.oppia.app.topic.review

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

private const val REVIEW_ACTIVITY_SKILL_ID_ARGUMENT_KEY = "ReviewActivity.skill_id"

/** Activity for skill review from Review tab. */
class ReviewActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var reviewActivityPresenter: ReviewActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    reviewActivityPresenter.handleOnCreate()
  }

  companion object {
    // TODO(#159): Use this skillId from TopicReviewFragment.
    /** Returns a new [Intent] to route to [ReviewActivity] for a specified skill ID list. */
    fun createReviewActivityIntent(context: Context, skillId: String): Intent {
      val intent = Intent(context, ReviewActivity::class.java)
      intent.putExtra(REVIEW_ACTIVITY_SKILL_ID_ARGUMENT_KEY, skillId)
      return intent
    }

    @Suppress("unused")
    fun getIntentKey():String{
      return REVIEW_ACTIVITY_SKILL_ID_ARGUMENT_KEY
    }
  }
}
