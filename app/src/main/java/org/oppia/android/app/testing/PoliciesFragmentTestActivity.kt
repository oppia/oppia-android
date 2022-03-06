package org.oppia.android.app.testing

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

/** Test Activity used for testing PoliciesFragment */
class PoliciesFragmentTestActivity : InjectableAppCompatActivity() {

  @Inject
  lateinit var policiesFragmentTestActivityPresenter: PoliciesFragmentTestActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)

    policiesFragmentTestActivityPresenter.handleOnCreate(
      intent.getProtoExtra(
        POLICIES_FRAGMENT_TEST_POLICY_PAGE_PARAMS_PROTO,
        PoliciesActivityParams.getDefaultInstance()
      )
    )
  }

  companion object {
    /** Argument key for policy page in [PoliciesFragmentTestActivity]. */
    const val POLICIES_FRAGMENT_TEST_POLICY_PAGE_PARAMS_PROTO =
      "PoliciesFragmentTestActivity.policy_page"

    /** Returns the [Intent] for opening [PoliciesFragmentTestActivity] for the specified [policyPage]. */
    fun createPoliciesFragmentTestActivity(context: Context, policyPage: PolicyPage): Intent {
      val policiesActivityParams =
        PoliciesActivityParams
          .newBuilder()
          .setPolicyPage(policyPage)
          .build()
      return Intent(context, PoliciesFragmentTestActivity::class.java).also {
        it.putProtoExtra(POLICIES_FRAGMENT_TEST_POLICY_PAGE_PARAMS_PROTO, policiesActivityParams)
      }
    }
  }
}
