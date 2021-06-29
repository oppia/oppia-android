package org.oppia.android.app.devoptions

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.drawer.NAVIGATION_PROFILE_ID_ARGUMENT_KEY
import javax.inject.Inject

/** Activity for Developer Options. */
class DeveloperOptionsActivity : InjectableAppCompatActivity(), ForceCrashButtonClickListener {
  @Inject
  lateinit var developerOptionsActivityPresenter: DeveloperOptionsActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    developerOptionsActivityPresenter.handleOnCreate()
    title = getString(R.string.developer_options_activity_title)
  }

  companion object {
    /** Function to create intent for DeveloperOptionsActivity */
    fun createDeveloperOptionsActivityIntent(context: Context, internalProfileId: Int): Intent {
      val intent = Intent(context, DeveloperOptionsActivity::class.java)
      intent.putExtra(NAVIGATION_PROFILE_ID_ARGUMENT_KEY, internalProfileId)
      return intent
    }

    fun getIntentKey(): String {
      return NAVIGATION_PROFILE_ID_ARGUMENT_KEY
    }
  }

  override fun forceCrash() {
    developerOptionsActivityPresenter.forceCrash()
  }
}
