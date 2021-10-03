package org.oppia.android.app.privacypolicytermsofservice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.TermsOfServicetermsofservice.TermsOfServiceSingleActivityPresenter
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** The Terms of Service page activity for placement of single Terms_Of_Service. */
class TermsOfServiceSingleActivity : InjectableAppCompatActivity() {

  @Inject
  lateinit var termsOfServiceSingleActivityPresenter: TermsOfServiceSingleActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    intent.getStringExtra(TERMS_OF_SERVICE_SINGLE_ACTIVITY)
    termsOfServiceSingleActivityPresenter.handleOnCreate()
  }

  companion object {
    private const val TERMS_OF_SERVICE_SINGLE_ACTIVITY = "TermsOfServiceSingleActivity"

    /** Returns [Intent] for [TermsOfServiceSingleActivity]. */
    fun createTermsOfServiceSingleActivityIntent(context: Context): Intent {
      return Intent(context, TermsOfServiceSingleActivity::class.java)
    }
  }
}
