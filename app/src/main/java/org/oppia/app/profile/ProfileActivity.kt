package org.oppia.app.profile

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** Activity that controls profile creation and selection. */
class ProfileActivity : InjectableAppCompatActivity() {
  @Inject lateinit var profileActivityPresenter: ProfileActivityPresenter

  companion object {
    fun createProfileActivity(context: Context): Intent {
      val intent = Intent(context, ProfileActivity::class.java)
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
      return intent
    }
  }

  @ExperimentalCoroutinesApi
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    profileActivityPresenter.handleOnCreate()
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray
  ) {
    if (requestCode == PERMISSIONS_WRITE_EXTERNAL_STORAGE) {
      if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
        profileActivityPresenter.permissionGranted()
      }
      return
    }
  }
}
