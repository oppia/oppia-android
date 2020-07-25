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
  @Inject
  lateinit var helpActivityPresenter: HelpActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    val isFromNavigationDrawer = intent.getBooleanExtra(
      BOOL_IS_FROM_NAVIGATION_DRAWER_EXTRA_KEY,
      /* defaultValue= */ false
    )
    helpActivityPresenter.handleOnCreate(isFromNavigationDrawer)
    title = getString(R.string.menu_help)
  }

  companion object {

    internal const val BOOL_IS_FROM_NAVIGATION_DRAWER_EXTRA_KEY =
      "BOOL_IS_FROM_NAVIGATION_DRAWER_EXTRA_KEY"

    fun createHelpActivityIntent(
      context: Context,
      profileId: Int?,
      isFromNavigationDrawer: Boolean
    ): Intent {
      val intent = Intent(context, HelpActivity::class.java)
      intent.putExtra(KEY_NAVIGATION_PROFILE_ID, profileId)
      intent.putExtra(BOOL_IS_FROM_NAVIGATION_DRAWER_EXTRA_KEY, isFromNavigationDrawer)
      return intent
    }
  }

  override fun onRouteToFAQList() {
    val intent = FAQListActivity.createFAQListActivityIntent(this)
    startActivity(intent)
  }
}
