package org.oppia.app.topic.review

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import javax.inject.Inject

/** The presenter for [ReviewActivity]. */
@ActivityScope
class ReviewActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  fun handleOnCreate() {
    activity.setContentView(R.layout.review_activity)
    if (getReviewFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.review_fragment_placeholder,
        ReviewFragment()
      ).commitNow()
    }
  }

  private fun getReviewFragment(): ReviewFragment? {
    return activity.supportFragmentManager.findFragmentById(R.id.review_fragment_placeholder) as ReviewFragment?
  }
}
