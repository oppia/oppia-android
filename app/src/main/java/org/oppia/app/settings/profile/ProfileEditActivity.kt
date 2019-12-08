package org.oppia.app.settings.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

const val KEY_PROFILE_EDIT_PROFILE_ID = "KEY_PROFILE_EDIT_PROFILE_ID"

/** Activity that allows user to select a profile to edit from settings. */
class ProfileEditActivity : InjectableAppCompatActivity() {
  @Inject lateinit var profileEditActivityPresenter: ProfileEditActivityPresenter

  companion object {
    fun createProfileEditActivity(context: Context, profileId: Int): Intent {
      val intent = Intent(context, ProfileEditActivity::class.java)
      intent.putExtra(KEY_PROFILE_EDIT_PROFILE_ID, profileId)
      return intent
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    profileEditActivityPresenter.handleOnCreate()
  }
}
