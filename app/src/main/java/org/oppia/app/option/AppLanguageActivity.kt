package org.oppia.app.option

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** The activity to change the language of the app. */
class AppLanguageActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var appLanguageActivityPresenter: AppLanguageActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    appLanguageActivityPresenter.handleOnCreate()
  }

  companion object {
    /** Returns a new [Intent] to route to [AppLanguageActivity]. */
    fun createAppLanguageActivityIntent(context: Context): Intent {
      val intent = Intent(context, AppLanguageActivity::class.java)
      return intent
    }
  }
}
