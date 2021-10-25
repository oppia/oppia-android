package org.oppia.android.app.TermsOfServicetermsofservice

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import javax.inject.Inject

/** The presenter for [TermsOfServiceActivity]. */
@ActivityScope
class TermsOfServiceActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
) {

  private lateinit var toolbar: Toolbar

  /** Handles onCreate() method of the [TermsOfServiceActivity]. */
  fun handleOnCreate() {
    activity.setContentView(R.layout.terms_of_service_activity)
    setUpToolbar()
    activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)

    toolbar.setNavigationOnClickListener {
      activity.finish()
    }

    if (geTermsOfServiceFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.terms_of_service_fragment_placeholder,
        TermsOfServiceFragment()
      ).commitNow()
    }
  }

  private fun setUpToolbar() {
    toolbar = activity.findViewById<View>(R.id.terms_of_service_activity_toolbar) as Toolbar
    activity.setSupportActionBar(toolbar)
  }

  private fun geTermsOfServiceFragment(): TermsOfServiceFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(
        R.id.terms_of_service_fragment_placeholder
      ) as TermsOfServiceFragment?
  }
}
