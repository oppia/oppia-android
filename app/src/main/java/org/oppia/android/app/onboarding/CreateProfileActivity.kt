package org.oppia.android.app.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.CreateProfileActivityParams
import org.oppia.android.app.model.ScreenName.CREATE_PROFILE_ACTIVITY
import org.oppia.android.util.extensions.putProtoExtra
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** Activity for displaying a new learner profile creation flow. */
class CreateProfileActivity : InjectableAutoLocalizedAppCompatActivity() {
  @Inject
  lateinit var learnerProfileActivityPresenter: CreateProfileActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)

    learnerProfileActivityPresenter.handleOnCreate()
  }

  companion object {
    /** Params key for [CreateProfileActivity]. */
    const val CREATE_PROFILE_ACTIVITY_PARAMS_KEY = "CreateProfileActivity.params"

    /** Returns a new [Intent] open a [CreateProfileActivity] with the specified params. */
    fun createProfileActivityIntent(context: Context, colorRgb: Int): Intent {
      val args = CreateProfileActivityParams.newBuilder().setColorRgb(colorRgb).build()
      return Intent(context, CreateProfileActivity::class.java).apply {
        putProtoExtra(CREATE_PROFILE_ACTIVITY_PARAMS_KEY, args)
        decorateWithScreenName(CREATE_PROFILE_ACTIVITY)
      }
    }
  }
}
