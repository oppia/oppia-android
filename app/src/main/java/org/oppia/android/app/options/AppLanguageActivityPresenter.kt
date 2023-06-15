package org.oppia.android.app.options

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.ProfileId
import org.oppia.android.databinding.AppLanguageActivityBinding
import javax.inject.Inject

/** The presenter for [AppLanguageActivity]. */
@ActivityScope
class AppLanguageActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  private lateinit var oppiaLanguage: OppiaLanguage

  /** Initializes and creates the views for [AppLanguageActivity]. */
  fun handleOnCreate(oppiaLanguage: OppiaLanguage, profileId: ProfileId) {
    val binding: AppLanguageActivityBinding = DataBindingUtil.setContentView(
      activity,
      R.layout.app_language_activity,
    )
    binding.appLanguageToolbar.setNavigationOnClickListener { activity.finish() }
    setLanguageSelected(oppiaLanguage)
    if (getAppLanguageFragment() == null) {
      val appLanguageFragment = AppLanguageFragment.newInstance(oppiaLanguage, profileId)
      activity.supportFragmentManager.beginTransaction()
        .add(R.id.app_language_fragment_container, appLanguageFragment).commitNow()
    }
  }

  /** Set the selected language for this Activity. */
  fun setLanguageSelected(oppiaLanguage: OppiaLanguage) {
    this.oppiaLanguage = oppiaLanguage
  }

  /** Return's the selected language for this Activity. */
  fun getLanguageSelected(): OppiaLanguage = oppiaLanguage

  private fun getAppLanguageFragment(): AppLanguageFragment? {
    return activity.supportFragmentManager
      .findFragmentById(R.id.app_language_fragment_container) as AppLanguageFragment?
  }
}
