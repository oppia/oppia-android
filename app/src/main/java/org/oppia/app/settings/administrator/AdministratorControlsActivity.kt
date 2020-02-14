package org.oppia.app.settings.administrator

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

const val ADMINISTRATOR_CONTROLS_ACTIVITY_PROFILE_ID_ARGUMENT_KEY = "AdministratorControlsActivity.profile_id"

/** Activity for Administrator Controls. */
class AdministratorControlsActivity : InjectableAppCompatActivity() {
  @Inject lateinit var administratorControlsActivityPresenter: AdministratorControlsActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    administratorControlsActivityPresenter.handleOnCreate()
  }

  companion object {
    fun createAdministratorControlsActivityIntent(context: Context, profileId: Int?): Intent {
      val intent = Intent(context, AdministratorControlsActivity::class.java)
      intent.putExtra(ADMINISTRATOR_CONTROLS_ACTIVITY_PROFILE_ID_ARGUMENT_KEY, profileId)
      return intent
    }

    fun getIntentKey(): String {
      return ADMINISTRATOR_CONTROLS_ACTIVITY_PROFILE_ID_ARGUMENT_KEY
    }
  }
}
