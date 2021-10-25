package org.oppia.android.app.privacypolicytermsofservice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.TermsOfServicetermsofservice.TermsOfServiceActivityPresenter
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** The Terms of Service page activity for placement of single Terms_Of_Service. */
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
