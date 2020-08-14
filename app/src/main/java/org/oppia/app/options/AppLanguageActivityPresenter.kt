package org.oppia.app.options

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import javax.inject.Inject

/** The presenter for [AppLanguageActivity]. */
@ActivityScope
class AppLanguageActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  private lateinit var prefSummaryValue: String

  fun handleOnCreate(prefKey: String, prefSummaryValue: String) {
    activity.setContentView(R.layout.app_language_activity)
    setLanguageSelected(prefSummaryValue)
    if (getAppLanguageFragment() == null) {
      val appLanguageFragment = AppLanguageFragment.newInstance(prefKey, prefSummaryValue)
      activity.supportFragmentManager.beginTransaction()
        .add(R.id.app_language_fragment_container, appLanguageFragment).commitNow()
    }
  }

  fun setLanguageSelected(appLanguage: String) {
    this.prefSummaryValue = appLanguage
  }

  fun getLanguageSelected(): String {
    return prefSummaryValue
  }

  private fun getAppLanguageFragment(): AppLanguageFragment? {
    return activity.supportFragmentManager
      .findFragmentById(R.id.app_language_fragment_container) as AppLanguageFragment?
  }
}
