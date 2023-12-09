package org.oppia.android.app.settings.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.ProfileEditActivityArguments
import org.oppia.android.app.model.ScreenName.PROFILE_EDIT_ACTIVITY
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProtoExtra
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** Argument key for the Profile Id in [ProfileEditActivity]. */
const val PROFILE_EDIT_PROFILE_ID_EXTRA_KEY = "ProfileEditActivity.profile_edit_profile_id"

/** Argument key for the Multipane in tablet mode for [ProfileEditActivity]. */
const val IS_MULTIPANE_EXTRA_KEY = "ProfileEditActivity.is_multipane"

/** Argument key for the Profile deletion confirmation in [ProfileEditActivity]. */
const val IS_PROFILE_DELETION_DIALOG_VISIBLE_KEY =
  "ProfileEditActivity.is_profile_deletion_dialog_visible"

/** Activity that allows admins to edit a profile. */
class ProfileEditActivity : InjectableAutoLocalizedAppCompatActivity() {
  @Inject
  lateinit var profileEditActivityPresenter: ProfileEditActivityPresenter

  companion object {

    /** Arguments key for ProfileEditActivity. */
    const val PROFILE_EDIT_ACTIVITY_ARGUMENTS_KEY = "ProfileEditActivity.arguments"

    /** Returns an [Intent] for opening the [ProfileEditActivity]. */
    fun createProfileEditActivity(
      context: Context,
      profileId: Int,
      isMultipane: Boolean = false
    ): Intent {
      val args = ProfileEditActivityArguments.newBuilder().apply {
        this.internalProfileId = profileId
        this.isMultipane = isMultipane
      }.build()
      return Intent(context, ProfileEditActivity::class.java).apply {
        putProtoExtra(PROFILE_EDIT_ACTIVITY_ARGUMENTS_KEY, args)
        decorateWithScreenName(PROFILE_EDIT_ACTIVITY)
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    profileEditActivityPresenter.handleOnCreate()
  }

  override fun onBackPressed() {
    val args = intent.getProtoExtra(
      PROFILE_EDIT_ACTIVITY_ARGUMENTS_KEY,
      ProfileEditActivityArguments.getDefaultInstance()
    )
    val isMultipane = args?.isMultipane ?: false
    if (isMultipane) {
      super.onBackPressed()
    } else {
      val intent = Intent(this, ProfileListActivity::class.java)
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
      startActivity(intent)
    }
  }
}
