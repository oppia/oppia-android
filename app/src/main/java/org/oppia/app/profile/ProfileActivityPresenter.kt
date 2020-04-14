package org.oppia.app.profile

import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.model.AppLanguage
import org.oppia.app.model.AudioLanguage
import org.oppia.app.model.StoryTextSize
import org.oppia.app.testing.ProfileChooserFragmentTestActivity
import org.oppia.domain.profile.ProfileManagementController
import org.oppia.domain.topic.TopicAssetController
import javax.inject.Inject

const val PERMISSIONS_WRITE_EXTERNAL_STORAGE = 0

/** The presenter for [ProfileActivity]. */
@ActivityScope
class ProfileActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController,
  private val topicAssetController: TopicAssetController
) {
  /** Adds [ProfileChooserFragment] to view. */
  fun handleOnCreate() {
    // TODO(#482): Ensures that an admin profile is present. Remove when there is proper admin account creation.
    profileManagementController.addProfile(
      name = "Sean",
      pin = "",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = -10710042,
      isAdmin = true,
      storyTextSize = StoryTextSize.SMALL_TEXT_SIZE,
      appLanguage = AppLanguage.ENGLISH_APP_LANGUAGE,
      audioLanguage = AudioLanguage.ENGLISH_AUDIO_LANGUAGE
    )
    activity.setContentView(R.layout.profile_activity)
    if (ContextCompat.checkSelfPermission(
        activity,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
      ) != PackageManager.PERMISSION_GRANTED
    ) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        ActivityCompat.requestPermissions(
          activity,
          arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
          PERMISSIONS_WRITE_EXTERNAL_STORAGE
        )
      }
    } else {
      permissionGranted()
    }
    if (getProfileChooserFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.profile_chooser_fragment_placeholder,
        ProfileChooserFragment(),
        ProfileChooserFragmentTestActivity.TAG_PROFILE_CHOOSER_FRAGMENT
      ).commitNow()
    }
  }

  private fun getProfileChooserFragment(): ProfileChooserFragment? {
    return activity.supportFragmentManager.findFragmentById(R.id.profile_chooser_fragment_placeholder) as ProfileChooserFragment?
  }

  private fun showMessageOKCancel(message: String, okListener: DialogInterface.OnClickListener) {
    AlertDialog.Builder(activity)
      .setMessage(message)
      .setPositiveButton(R.string.audio_language_select_dialog_okay_button, okListener)
      .setNegativeButton(R.string.audio_language_select_dialog_cancel_button, null)
      .create()
      .show()
  }

  fun permissionGranted() {
    topicAssetController.copyAllAssetImagesToInternalStorage(activity)
  }
}
