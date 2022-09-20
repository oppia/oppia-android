package org.oppia.android.app.testing

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.model.PoliciesActivityParams
import org.oppia.android.app.model.PoliciesFragmentArguments
import org.oppia.android.app.policies.PoliciesFragment
import javax.inject.Inject

/** The presenter for [PoliciesFragmentTestActivity] */
@ActivityScope
class PoliciesFragmentTestActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {

  /** Handles onCreate() method of the [PoliciesFragmentTestActivity]. */
  fun handleOnCreate(policiesActivityParams: PoliciesActivityParams) {
    activity.setContentView(R.layout.policies_fragment_test_activity)
    if (getPoliciesFragment() == null) {
      val policiesFragmentArguments =
        PoliciesFragmentArguments
          .newBuilder()
          .setPolicyPage(policiesActivityParams.policyPage)
          .build()
      val policiesFragment: PoliciesFragment =
        PoliciesFragment.newInstance(policiesFragmentArguments)

      activity
        .supportFragmentManager
        .beginTransaction()
        .add(
          R.id.policies_fragment_placeholder,
          policiesFragment
        ).commitNow()
    }
  }

  private fun getPoliciesFragment(): PoliciesFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(R.id.policies_fragment_placeholder) as PoliciesFragment?
  }
}
