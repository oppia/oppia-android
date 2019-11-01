package org.oppia.app.profile

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.databinding.AddProfileActivityBinding
import org.oppia.app.home.HomeActivity
import org.oppia.domain.profile.ProfileManagementController
import javax.inject.Inject

/** The presenter for [AddProfileActivity]. */
@ActivityScope
class AddProfileActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController
) {
  @ExperimentalCoroutinesApi
  fun handleOnCreate() {
    val binding = DataBindingUtil.setContentView<AddProfileActivityBinding>(activity, R.layout.add_profile_activity)
    binding.submitButton.setOnClickListener {
      profileManagementController.addProfile(binding.inputName.text.toString(), "123", null, allowDownloadAccess = false, isAdmin = false).observe(activity, Observer {
        if (it.isSuccess()) {
          val intent = Intent(activity, ProfileActivity::class.java)
          intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
          activity.startActivity(intent)
        }
      })
    }
  }
}
