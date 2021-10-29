package org.oppia.android.app.policies

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.translation.AppLanguageResourceHandler
import javax.inject.Inject

/** The presenter for [PoliciesActivity]. */
@ActivityScope
class PoliciesActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val resourceHandler: AppLanguageResourceHandler
) {

  private lateinit var toolbar: Toolbar

  /** Handles onCreate() method of the [PoliciesActivity]. */
  fun handleOnCreate(policies: Int) {
    activity.setContentView(R.layout.policies_activity)
    setUpToolbar(policies)
    activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)

    toolbar.setNavigationOnClickListener {
      activity.finish()
    }

    if (getPoliciesFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.policies_fragment_placeholder,
        PoliciesFragment.newInstance(policies)
      ).commitNow()
    }
  }

  private fun setUpToolbar(policies: Int) {
    toolbar = activity.findViewById<View>(R.id.policies_activity_toolbar) as Toolbar

    when (policies) {
      Policies.PRIVACY_POLICY.ordinal ->
        toolbar.title = resourceHandler.getStringInLocale(R.string.privacy_policy_title)
      Policies.TERMS_OF_SERVICE.ordinal ->
        toolbar.title = resourceHandler.getStringInLocale(R.string.terms_of_service_title)
    }
    activity.setSupportActionBar(toolbar)
  }

  private fun getPoliciesFragment(): PoliciesFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(
        R.id.policies_fragment_placeholder
      ) as PoliciesFragment?
  }
}
