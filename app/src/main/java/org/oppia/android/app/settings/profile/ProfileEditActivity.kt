package org.oppia.android.app.settings.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

const val KEY_PROFILE_EDIT_PROFILE_ID = "KEY_PROFILE_EDIT_PROFILE_ID"
const val KEY_IS_MULTIPANE = "KEY_IS_MULTIPANE"

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
      intent.putExtra(KEY_PROFILE_EDIT_PROFILE_ID, profileId)
      intent.putExtra(KEY_IS_MULTIPANE, isMultipane)
      return intent
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    profileEditActivityPresenter.handleOnCreate()
  }

  override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
    super.onRestoreInstanceState(savedInstanceState)
    profileEditActivityPresenter.handleOnRestoreSavedInstanceState()
  }

  override fun onSupportNavigateUp(): Boolean {
    val isMultipane = intent.extras!!.getBoolean(KEY_IS_MULTIPANE, false)
    if (isMultipane) {
      super.onBackPressed()
    } else {
      val intent = Intent(this, ProfileListActivity::class.java)
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
      startActivity(intent)
    }
    return false
  }

  override fun onBackPressed() {
    val isMultipane = intent.extras!!.getBoolean(KEY_IS_MULTIPANE, false)
    if (isMultipane) {
      super.onBackPressed()
    } else {
      val intent = Intent(this, ProfileListActivity::class.java)
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
      startActivity(intent)
    }
  }
}
