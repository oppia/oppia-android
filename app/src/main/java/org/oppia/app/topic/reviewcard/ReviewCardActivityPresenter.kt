package org.oppia.app.topic.reviewcard

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import javax.inject.Inject

/** The presenter for [ReviewCardActivity]. */
@ActivityScope
class ReviewCardActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  fun handleOnCreate() {
    activity.setContentView(R.layout.review_card_activity)
    if (getReviewCardFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.review_card_fragment_placeholder,
        ReviewCardFragment()
      ).commitNow()
    }
  }

  private fun getReviewCardFragment(): ReviewCardFragment? {
    return activity.supportFragmentManager.findFragmentById(R.id.review_card_fragment_placeholder) as ReviewCardFragment?
  }
}
