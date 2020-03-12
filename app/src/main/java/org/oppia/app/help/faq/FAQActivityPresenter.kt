package org.oppia.app.help.faq

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import javax.inject.Inject

/** The presenter for [FAQActivity]. */
@ActivityScope
class FAQActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  fun handleOnCreate() {
    activity.setContentView(R.layout.faq_activity)
    activity.supportFragmentManager.beginTransaction().add(
      R.id.faq_fragment_placeholder,
      FAQFragment()
    ).commitNow()
  }
}
