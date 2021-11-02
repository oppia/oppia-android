package org.oppia.android.app.policies

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** Activity for displaying the app policies. */
class PoliciesActivity : InjectableAppCompatActivity() {

  @Inject
  lateinit var policiesActivityPresenter: PoliciesActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    policiesActivityPresenter.handleOnCreate(intent.getIntExtra(POLICIES_ACTIVITY, -1))
  }

  companion object {
    private const val POLICIES_ACTIVITY = "PoliciesActivity"

    /** Returns the [Intent] for opening [PoliciesActivity]. */
    fun createPoliciesActivityIntent(context: Context, policies: Int): Intent {
      val intent = Intent(context, PoliciesActivity::class.java)
      intent.putExtra(POLICIES_ACTIVITY, policies)
      return intent
    }
  }
}
