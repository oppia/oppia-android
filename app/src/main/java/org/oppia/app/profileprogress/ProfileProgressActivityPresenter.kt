package org.oppia.app.profileprogress

import android.content.Intent
import android.provider.MediaStore
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.model.ProfileId
import org.oppia.app.profile.GALLERY_INTENT_RESULT_CODE
import org.oppia.domain.profile.ProfileManagementController
import javax.inject.Inject

private const val TAG_PROFILE_PICTURE_EDIT_DIALOG = "PROFILE_PICTURE_EDIT_DIALOG"

/** The presenter for [ProfileProgressActivity]. */
@ActivityScope
class ProfileProgressActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController
) {
  private lateinit var profileId: ProfileId

  fun handleOnCreate(internalProfileId: Int) {
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
    activity.setContentView(R.layout.profile_progress_activity)
    if (getProfileProgressFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.profile_progress_fragment_placeholder,
        ProfileProgressFragment.newInstance(internalProfileId)
      ).commitNow()
    }
    setUpNavigationDrawer()
  }

  private fun setUpNavigationDrawer() {
    val toolbar = activity.findViewById<View>(
      R.id.profile_progress_activity_toolbar
    ) as Toolbar
    activity.setSupportActionBar(toolbar)
    activity.supportActionBar!!.setTitle(R.string.profile)
    activity.supportActionBar!!.setDisplayShowHomeEnabled(true)
    activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    toolbar.setNavigationOnClickListener {
      activity.finish()
    }
  }

  private fun getProfileProgressFragment(): ProfileProgressFragment? {
    return activity.supportFragmentManager.findFragmentById(
      R.id.profile_progress_fragment_placeholder
    ) as ProfileProgressFragment?
  }

  fun showPictureEditDialog() {
    val previousFragment =
      activity.supportFragmentManager.findFragmentByTag(TAG_PROFILE_PICTURE_EDIT_DIALOG)
    if (previousFragment != null) {
      activity.supportFragmentManager.beginTransaction().remove(previousFragment).commitNow()
    }
    val dialogFragment = ProfilePictureEditDialogFragment.newInstance()
    dialogFragment.showNow(activity.supportFragmentManager, TAG_PROFILE_PICTURE_EDIT_DIALOG)
  }

  fun openGalleryIntent() {
    val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    activity.startActivityForResult(galleryIntent, GALLERY_INTENT_RESULT_CODE)
  }

  fun handleOnActivityResult(intent: Intent?) {
    intent?.let {
      profileManagementController.updateProfileAvatar(
        profileId,
        intent.data, /* colorRgb= */
        -10710042
      )
    }
  }
}
