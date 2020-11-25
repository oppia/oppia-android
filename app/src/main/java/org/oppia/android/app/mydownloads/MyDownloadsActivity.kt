package org.oppia.android.app.mydownloads

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.drawer.NAVIGATION_PROFILE_ID_ARGUMENT_KEY
import org.oppia.android.app.home.HomeActivity
import javax.inject.Inject

/** The activity for displaying [MyDownloadsFragment]. */
class MyDownloadsActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var myDownloadsActivityPresenter: MyDownloadsActivityPresenter
  private var internalProfileId: Int = -1

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    myDownloadsActivityPresenter.handleOnCreate()
    internalProfileId = intent.getIntExtra(NAVIGATION_PROFILE_ID_ARGUMENT_KEY, -1)
  }

  companion object {
    fun createMyDownloadsActivityIntent(context: Context, profileId: Int?): Intent {
      val intent = Intent(context, MyDownloadsActivity::class.java)
      intent.putExtra(NAVIGATION_PROFILE_ID_ARGUMENT_KEY, profileId)
      return intent
    }

    fun getIntentKey(): String {
      return NAVIGATION_PROFILE_ID_ARGUMENT_KEY
    }
  }

  override fun onBackPressed() {
    val intent = HomeActivity.createHomeActivity(this, internalProfileId)
    startActivity(intent)
    finish()
  }
}
