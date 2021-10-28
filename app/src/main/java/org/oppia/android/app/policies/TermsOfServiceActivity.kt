package org.oppia.android.app.policies

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** The activity for displaying the app's terms of service.  */
class TermsOfServiceActivity : InjectableAppCompatActivity() {

  @Inject
  lateinit var termsOfServiceActivityPresenter: TermsOfServiceActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    intent.getStringExtra(TERMS_OF_SERVICE_ACTIVITY)
    termsOfServiceActivityPresenter.handleOnCreate()
  }

  companion object {
    private const val TERMS_OF_SERVICE_ACTIVITY = "TermsOfServiceActivity"

    /** Returns [Intent] for [TermsOfServiceActivity]. */
    fun createTermsOfServiceActivityIntent(context: Context): Intent {
      return Intent(context, TermsOfServiceActivity::class.java)
    }
  }
}
