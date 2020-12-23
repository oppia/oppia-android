package org.oppia.android.app.settings.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

const val PROFILE_RENAME_PROFILE_ID_EXTRA_KEY = "ProfileRenameActivity.profile_rename_profile_id"

/** Activity that allows user to rename a profile. */
class ProfileRenameActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var profileRenameActivityPresenter: ProfileRenameActivityPresenter

  companion object {
    fun createProfileRenameActivity(context: Context, profileId: Int): Intent {
      val intent = Intent(context, ProfileRenameActivity::class.java)
      intent.putExtra(PROFILE_RENAME_PROFILE_ID_EXTRA_KEY, profileId)
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
