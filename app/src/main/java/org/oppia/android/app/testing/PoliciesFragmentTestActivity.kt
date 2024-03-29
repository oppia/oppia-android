package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.model.PoliciesActivityParams
import org.oppia.android.app.model.PolicyPage
import org.oppia.android.app.policies.RouteToPoliciesListener
import org.oppia.android.app.testing.activity.TestActivity
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProtoExtra
import javax.inject.Inject

/** Test Activity used for testing [PoliciesFragment]. */
class PoliciesFragmentTestActivity : TestActivity(), RouteToPoliciesListener {

  @Inject
  lateinit var policiesFragmentTestActivityPresenter: PoliciesFragmentTestActivityPresenter
  /**
   * [RouteToPoliciesListener] that must be initialized by the test, and is presumed to be a
   * Mockito mock (though this is not, strictly speaking, required).
   *
   * This listener will be used as the callback for the dialog in response to UI operations.
   */
  lateinit var mockCallbackListener: RouteToPoliciesListener

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

  override fun onRouteToPolicies(policyPage: PolicyPage) {
    mockCallbackListener.onRouteToPolicies(PolicyPage.PRIVACY_POLICY)
  }
}
