package org.oppia.app.option

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

private const val PREFERENCE_KEY ="PREFERENCE_KEY"

/** The activity to change the language of the app. */
class AppLanguageActivity : InjectableAppCompatActivity() {
  var pref_summary_value: String = ""
  @Inject
  lateinit var appLanguageActivityPresenter: AppLanguageActivityPresenter
  private  var  pref_key: String = ""
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    pref_key = intent.getStringExtra(PREFERENCE_KEY)
    pref_summary_value = intent.getStringExtra(PREFERENCE_SUMMARY_VALUE)
    appLanguageActivityPresenter.handleOnCreate(pref_key,pref_summary_value)
  }

  companion object {
    /** Returns a new [Intent] to route to [AppLanguageActivity]. */
    fun createAppLanguageActivityIntent(
      context: Context,
      pref_key: String,
      summaryValue: String
    ): Intent {
      val intent = Intent(context, AppLanguageActivity::class.java)
      intent.putExtra(PREFERENCE_KEY, pref_key)
      intent.putExtra(PREFERENCE_SUMMARY_VALUE,summaryValue)
      return intent
    }
  }
}
