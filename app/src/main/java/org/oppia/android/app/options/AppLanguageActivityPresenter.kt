package org.oppia.android.app.options

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.databinding.AppLanguageActivityBinding
import javax.inject.Inject

/** The presenter for [AppLanguageActivity]. */
@ActivityScope
class AppLanguageActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  private lateinit var oppiaLangauge: OppiaLanguage

  /** Initializes and creates the views for [AppLanguageActivity]. */
  fun handleOnCreate(oppiaLanguage: OppiaLanguage, profileId: Int) {
    val binding: AppLanguageActivityBinding = DataBindingUtil.setContentView(
      activity,
      R.layout.app_language_activity,
    )
    binding.appLanguageToolbar.setNavigationOnClickListener {
      val intent = Intent().apply {
        putExtra(MESSAGE_APP_LANGUAGE_ARGUMENT_KEY, oppiaLanguage)
      }
      (activity as AppLanguageActivity).setResult(REQUEST_CODE_APP_LANGUAGE, intent)
      activity.finish()
    }
    setLanguageSelected(oppiaLanguage)
    if (getAppLanguageFragment() == null) {
      val appLanguageFragment = AppLanguageFragment.newInstance(oppiaLanguage, profileId)
      activity.supportFragmentManager.beginTransaction()
        .add(R.id.app_language_fragment_container, appLanguageFragment).commitNow()
    }
  }

  /** Set the selected language for this Activity. */
  fun setLanguageSelected(oppiaLanguage: OppiaLanguage) {
    this.oppiaLangauge = oppiaLanguage
  }

  /** Return's the selected language for this Activity. */
  fun getLanguageSelected(): OppiaLanguage {
    return oppiaLangauge
  }

  private fun getAppLanguageFragment(): AppLanguageFragment? {
    return activity.supportFragmentManager
      .findFragmentById(R.id.app_language_fragment_container) as AppLanguageFragment?
  }
}
