package org.oppia.app.help

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.R
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.drawer.KEY_NAVIGATION_PROFILE_ID
import org.oppia.app.help.faq.FAQListActivity
import javax.inject.Inject

/** The help page activity for FAQs and feedback. */
class HelpActivity : InjectableAppCompatActivity(), RouteToFAQListListener {
  @Inject lateinit var helpActivityPresenter: HelpActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    helpActivityPresenter.handleOnCreate()
    title = getString(R.string.menu_help)
  }

  companion object {
    fun createHelpActivityIntent(context: Context, profileId: Int?): Intent {
      val intent = Intent(context, HelpActivity::class.java)
      intent.putExtra(KEY_NAVIGATION_PROFILE_ID, profileId)
      return intent
    }

    fun getIntentKey(): String {
      return KEY_NAVIGATION_PROFILE_ID
    }
  }

  override fun onRouteToFAQList() {
    val intent = FAQListActivity.createFAQListActivityIntent(this)
    startActivity(intent)
  }
}
