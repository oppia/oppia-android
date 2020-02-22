package org.oppia.app.mydownloads

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.R
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.drawer.KEY_NAVIGATION_PROFILE_ID
import org.oppia.app.help.HelpActivity
import javax.inject.Inject

/** The activity for displaying [MyDownloadsFragment]. */
class MyDownloadsActivity : InjectableAppCompatActivity() {
  @Inject lateinit var myDownloadsActivityPresenter: MyDownloadsActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    myDownloadsActivityPresenter.handleOnCreate()
    title = getString(R.string.menu_my_downloads)
  }

  companion object {
    fun createMyDownloadsActivityIntent(context: Context, profileId: Int?): Intent {
      val intent = Intent(context, MyDownloadsActivity::class.java)
      intent.putExtra(KEY_NAVIGATION_PROFILE_ID, profileId)
      return intent
    }

    fun getIntentKey(): String {
      return KEY_NAVIGATION_PROFILE_ID
    }
  }
}
