package org.oppia.app.help

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.R
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.home.KEY_HOME_PROFILE_ID
import javax.inject.Inject

/** The help page activity for users FAQ and feedbacks. */
class HelpActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var helpActivityPresenter: HelpActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    helpActivityPresenter.handleOnCreate()
    title = getString(R.string.menu_help)
  }

  companion object {
    fun createHelpActivityIntent(context: Context, profileId: Int?): Intent {
      val intent = Intent(context, HelpActivity::class.java)
      intent.putExtra(KEY_HOME_PROFILE_ID, profileId)
      return intent
    }

    fun getIntentKey(): String {
      return KEY_HOME_PROFILE_ID
    }
  }
}
