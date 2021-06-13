package org.oppia.android.app.settings.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

const val PROFILE_EDIT_PROFILE_ID_EXTRA_KEY = "ProfileEditActivity.profile_edit_profile_id"
const val IS_MULTIPANE_EXTRA_KEY = "ProfileEditActivity.is_multipane"
const val IS_PROFILE_DELETION_DIALOG_VISIBLE_KEY =
  "ProfileEditActivity.is_profile_deletion_dialog_visible"

/** Activity that allows user to edit a profile. */
class ProfileEditActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var profileEditActivityPresenter: ProfileEditActivityPresenter

  companion object {
    fun createProfileEditActivity(
      context: Context,
      profileId: Int,
      isMultipane: Boolean = false
    ): Intent {
      val intent = Intent(context, ProfileEditActivity::class.java)
      intent.putExtra(PROFILE_EDIT_PROFILE_ID_EXTRA_KEY, profileId)
      intent.putExtra(IS_MULTIPANE_EXTRA_KEY, isMultipane)
      return intent
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    profileEditActivityPresenter.handleOnCreate()
  }

  override fun onBackPressed() {
    val isMultipane = intent.extras!!.getBoolean(IS_MULTIPANE_EXTRA_KEY, false)
    if (isMultipane) {
      super.onBackPressed()
    } else {
      val intent = Intent(this, ProfileListActivity::class.java)
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
      startActivity(intent)
    }
  }
}
