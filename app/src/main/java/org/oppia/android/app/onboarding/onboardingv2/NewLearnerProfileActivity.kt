package org.oppia.android.app.onboarding.onboardingv2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.ScreenName
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** Activity for displaying a new learner profile creation flow. */
class NewLearnerProfileActivity : InjectableAutoLocalizedAppCompatActivity() {
  @Inject
  lateinit var learnerProfileActivityPresenter: NewLearnerProfileActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)

    learnerProfileActivityPresenter.handleOnCreate()
  }

  companion object {
    /** Returns a new [Intent] open a [NewLearnerProfileActivity] with the specified params. */
    fun createNewLearnerProfileActivity(context: Context): Intent {
      return Intent(context, NewLearnerProfileActivity::class.java).apply {
        decorateWithScreenName(ScreenName.CREATE_NEW_LEARNER_PROFILE_ACTIVITY)
      }
    }
  }
}
