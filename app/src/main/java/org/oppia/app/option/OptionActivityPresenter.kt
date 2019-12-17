package org.oppia.app.option

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import javax.inject.Inject

/** The presenter for [OptionActivity]. */
@ActivityScope
class OptionActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  fun handleOnCreate() {
    activity.setContentView(R.layout.option_activity)
    if (getOptionFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.option_fragment_placeholder,
        OptionsFragment()
      ).commitNow()
    }
  }

  private fun getOptionFragment(): OptionsFragment? {
    return activity.supportFragmentManager.findFragmentById(R.id.option_fragment_placeholder) as OptionsFragment?
  }
}
