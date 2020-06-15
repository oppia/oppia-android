package org.oppia.app.topic.revisioncard

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import javax.inject.Inject

/** The presenter for [RevisionCardActivity]. */
@ActivityScope
class RevisionCardActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  fun handleOnCreate() {
    activity.setContentView(R.layout.revision_card_activity)
    if (getReviewCardFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.revision_card_fragment_placeholder,
        RevisionCardFragment()
      ).commitNow()
    }
  }

  private fun getReviewCardFragment(): RevisionCardFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(
        R.id.revision_card_fragment_placeholder
      ) as RevisionCardFragment?
  }
}
