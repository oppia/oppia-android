package org.oppia.android.app.devoptions

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.drawer.KEY_NAVIGATION_PROFILE_ID
import javax.inject.Inject

/** Activity for Developer Options. */
class DeveloperOptionsActivity : InjectableAppCompatActivity() {
  @Inject lateinit var developerOptionsActivityPresenter: DeveloperOptionsActivityPresenter
  private var internalProfileId = -1

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    internalProfileId = intent.getIntExtra(DEVELOPER_OPTIONS_ACTIVITY_PROFILE_ID_KEY, -1)
    developerOptionsActivityPresenter.handleOnCreate(internalProfileId)
    title = getString(R.string.developer_options_title)
  }

  companion object {
    const val DEVELOPER_OPTIONS_ACTIVITY_PROFILE_ID_KEY =
      "DeveloperOptionsActivity.internal_profile_id"

    /** Function to create intent for DeveloperOptionsActivity */
    fun createDeveloperOptionsActivityIntent(context: Context, internalProfileId: Int): Intent {
      val intent = Intent(context, DeveloperOptionsActivity::class.java)
      intent.putExtra(KEY_NAVIGATION_PROFILE_ID, internalProfileId)
      return intent
    }

    fun getIntentKey(): String {
      return KEY_NAVIGATION_PROFILE_ID
    }
  }
}
