package org.oppia.android.app.policies

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.model.PoliciesActivityParams
import org.oppia.android.app.model.PolicyPage
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProtoExtra
import javax.inject.Inject

/** Activity for displaying the app policies. */
class PoliciesActivity : InjectableAppCompatActivity() {

  @Inject
  lateinit var policiesActivityPresenter: PoliciesActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)

    policiesActivityPresenter.handleOnCreate(
      intent.getProtoExtra(
        POLICIES_ACTIVITY_POLICY_PAGE_PARAMS_PROTO,
        PoliciesActivityParams.getDefaultInstance()
      )
    )
  }

  companion object {
    /** Argument key for policy page in [PoliciesActivity]. */
    const val POLICIES_ACTIVITY_POLICY_PAGE_PARAMS_PROTO = "PoliciesActivity.policy_page"

    /** Returns the [Intent] for opening [PoliciesActivity] for the specified [policyPage]. */
    fun createPoliciesActivityIntent(context: Context, policyPage: PolicyPage): Intent {
      val policiesActivityParams =
        PoliciesActivityParams
          .newBuilder()
          .setPolicyPage(policyPage)
          .build()
      return Intent(context, PoliciesActivity::class.java).also {
        it.putProtoExtra(POLICIES_ACTIVITY_POLICY_PAGE_PARAMS_PROTO, policiesActivityParams)
      }
    }
  }
}
