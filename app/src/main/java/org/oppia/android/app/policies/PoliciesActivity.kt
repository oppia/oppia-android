package org.oppia.android.app.policies

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.model.PoliciesActivityArguments
import org.oppia.android.app.model.PolicyPage
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProto
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
        POLICIES_ACTIVITY_POLICY_PAGE_ARGUMENT_PROTO,
        PoliciesActivityArguments.getDefaultInstance()
      )
    )
  }

  companion object {
    /** Argument key for policy page in [PoliciesActivity] */
    const val POLICIES_ACTIVITY_POLICY_PAGE_ARGUMENT_PROTO = "PoliciesActivity.policy_page"

    /** Returns the [Intent] for opening [PoliciesActivity]. */
    fun createPoliciesActivityIntent(context: Context, policyPage: PolicyPage): Intent {
      val intent = Intent(context, PoliciesActivity::class.java)
      val policiesActivityArguments =
        PoliciesActivityArguments
          .newBuilder()
          .setPolicyPage(policyPage)
          .build()
      val args = Bundle()
      args.putProto(POLICIES_ACTIVITY_POLICY_PAGE_ARGUMENT_PROTO, policiesActivityArguments)
      intent.putExtras(args)
      return intent
    }
  }
}
