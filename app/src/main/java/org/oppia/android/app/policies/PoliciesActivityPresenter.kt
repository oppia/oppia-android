package org.oppia.android.app.policies

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.model.PoliciesActivityArguments
import org.oppia.android.app.model.PolicyPage
import org.oppia.android.app.translation.AppLanguageResourceHandler
import javax.inject.Inject

/** The presenter for [PoliciesActivity]. */
@ActivityScope
class PoliciesActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val resourceHandler: AppLanguageResourceHandler
) {

  /** Handles onCreate() method of the [PoliciesActivity]. */
  fun handleOnCreate(policiesActivityArguments: PoliciesActivityArguments) {
    activity.setContentView(R.layout.policies_activity)
    val toolbar = setUpToolbar(policiesActivityArguments.policyPage)
    activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)

    toolbar.setNavigationOnClickListener {
      activity.finish()
    }

    if (getPoliciesFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.policies_fragment_placeholder,
        PoliciesFragment.newInstance(policiesActivityArguments)
      ).commitNow()
    }
  }

  private fun setUpToolbar(policyPage: PolicyPage): Toolbar {
    val toolbar = activity.findViewById<View>(R.id.policies_activity_toolbar) as Toolbar

    toolbar.title = when (policyPage) {
      PolicyPage.PRIVACY_POLICY ->
        resourceHandler.getStringInLocale(R.string.privacy_policy_title)
      PolicyPage.TERMS_OF_SERVICE ->
        resourceHandler.getStringInLocale(R.string.terms_of_service_title)
      else -> ""
    }
    activity.setSupportActionBar(toolbar)
    return toolbar
  }

  private fun getPoliciesFragment(): PoliciesFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(
        R.id.policies_fragment_placeholder
      ) as PoliciesFragment?
  }
}
