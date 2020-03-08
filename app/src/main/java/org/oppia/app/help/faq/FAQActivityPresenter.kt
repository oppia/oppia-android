package org.oppia.app.help.faq

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import javax.inject.Inject

/** The presenter for [FAQActivity]. */
@ActivityScope
class FAQActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  fun handleOnCreate() {
    activity.setContentView(R.layout.faq_activity)
    setUpToolbar()
    activity.supportFragmentManager.beginTransaction().add(
      R.id.faq_fragment_placeholder,
      FAQFragment()
    ).commitNow()
  }

  private fun setUpToolbar() {
    val toolbar = activity.findViewById<View>(R.id.faq_activity_toolbar) as Toolbar
    activity.setSupportActionBar(toolbar)
    activity.supportActionBar!!.setDisplayShowHomeEnabled(true)
    activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true);
  }
}
