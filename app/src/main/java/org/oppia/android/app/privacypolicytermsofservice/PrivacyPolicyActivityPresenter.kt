package org.oppia.android.app.privacypolicytermsofservice

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import javax.inject.Inject

/** The presenter for [PrivacyPolicyActivity]. */
@ActivityScope
class PrivacyPolicyActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
) {

  private lateinit var toolbar: Toolbar

  /** Handles onCreate() method of the [PrivacyPolicyActivity]. */
  fun handleOnCreate() {
    activity.setContentView(R.layout.privacy_policy_activity)
    setUpToolbar()
    activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)

    toolbar.setNavigationOnClickListener {
      activity.finish()
    }

    if (gePrivacyPolicyFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.privacy_policy_fragment_placeholder,
        PrivacyPolicyFragment()
      ).commitNow()
    }
  }

  private fun setUpToolbar() {
    toolbar = activity.findViewById<View>(R.id.privacy_policy_activity_toolbar) as Toolbar
    activity.setSupportActionBar(toolbar)
  }

  private fun gePrivacyPolicyFragment(): PrivacyPolicyFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(
        R.id.privacy_policy_fragment_placeholder
      ) as PrivacyPolicyFragment?
  }
}
