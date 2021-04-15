package org.oppia.android.app.mydownloads

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.drawer.KEY_NAVIGATION_PROFILE_ID
import org.oppia.android.app.help.HelpActivity
import org.oppia.android.app.home.HomeActivity
import org.oppia.android.app.mydownloads.downloads.DownloadsFragment
import org.oppia.android.app.mydownloads.updates.UpdatesFragment
import javax.inject.Inject

/** The activity for displaying [DownloadsFragment] and [UpdatesFragment] in a tab layout. */
class MyDownloadsActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var myDownloadsActivityPresenter: MyDownloadsActivityPresenter
  private var internalProfileId: Int = -1

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    val isFromNavigationDrawer = intent.getBooleanExtra(
      BOOL_IS_FROM_NAVIGATION_DRAWER_EXTRA_KEY,
      /* defaultValue= */ false
    )
    internalProfileId = intent.getIntExtra(KEY_NAVIGATION_PROFILE_ID, -1)
    myDownloadsActivityPresenter.handleOnCreate(internalProfileId, isFromNavigationDrawer)
  }

  companion object {

    internal const val BOOL_IS_FROM_NAVIGATION_DRAWER_EXTRA_KEY =
      "BOOL_IS_FROM_NAVIGATION_DRAWER_EXTRA_KEY"

    fun createMyDownloadsActivityIntent(
      context: Context,
      internalProfileId: Int?,
      isFromNavigationDrawer: Boolean
    ): Intent {
      val intent = Intent(context, MyDownloadsActivity::class.java)
      intent.putExtra(KEY_NAVIGATION_PROFILE_ID, internalProfileId)
      intent.putExtra(HelpActivity.BOOL_IS_FROM_NAVIGATION_DRAWER_EXTRA_KEY, isFromNavigationDrawer)
      return intent
    }
  }

  override fun onBackPressed() {
    val intent = HomeActivity.createHomeActivity(this, internalProfileId)
    startActivity(intent)
    finish()
  }
}
