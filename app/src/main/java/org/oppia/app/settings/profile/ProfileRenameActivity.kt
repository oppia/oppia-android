package org.oppia.app.settings.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

const val KEY_PROFILE_RENAME_PROFILE_ID = "KEY_PROFILE_RENAME_PROFILE_ID"

/** Activity that allows user to rename a profile. */
class ProfileRenameActivity : InjectableAppCompatActivity() {
  @Inject lateinit var profileRenameActivityPresenter: ProfileRenameActivityPresenter

  companion object {
    fun createProfileRenameActivity(context: Context, profileId: Int): Intent {
      val intent = Intent(context, ProfileRenameActivity::class.java)
      intent.putExtra(KEY_PROFILE_RENAME_PROFILE_ID, profileId)
      return intent
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    profileRenameActivityPresenter.handleOnCreate()
  }

  override fun onSupportNavigateUp(): Boolean {
    finish()
    return false
  }
}
