package org.oppia.android.app.settings.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.ProfileEditActivityParams
import org.oppia.android.app.model.ScreenName.PROFILE_EDIT_ACTIVITY
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProtoExtra
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** Activity that allows admins to edit a profile. */
class ProfileEditActivity : InjectableAutoLocalizedAppCompatActivity() {
  @Inject
  lateinit var profileEditActivityPresenter: ProfileEditActivityPresenter

  companion object {

    /** Params key for ProfileEditActivity. */
    const val PROFILE_EDIT_ACTIVITY_PARAMS_KEY = "ProfileEditActivity.params"

    /** Returns an [Intent] for opening the [ProfileEditActivity]. */
    fun createProfileEditActivity(
      context: Context,
      profileId: Int,
      isMultipane: Boolean = false
    ): Intent {
      val args = ProfileEditActivityParams.newBuilder().apply {
        this.internalProfileId = profileId
        this.isMultipane = isMultipane
      }.build()
      return Intent(context, ProfileEditActivity::class.java).apply {
        putProtoExtra(PROFILE_EDIT_ACTIVITY_PARAMS_KEY, args)
        decorateWithScreenName(PROFILE_EDIT_ACTIVITY)
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    profileEditActivityPresenter.handleOnCreate()
    handleBackPress()
  }

  private fun handleBackPress() {
    onBackPressedDispatcher.addCallback(
      this@ProfileEditActivity,
      object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
          val isMultipane: Boolean = intent.extras!!.getBoolean(IS_MULTIPANE_EXTRA_KEY, false)
          if (isMultipane) {
            onBackPressedDispatcher.onBackPressed()
          } else {
            val intent = Intent(this@ProfileEditActivity, ProfileListActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
          }
        }
      }
    )
  }
}
