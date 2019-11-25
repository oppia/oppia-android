package org.oppia.app.settings.profile

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.databinding.ProfileRenameActivityBinding
import javax.inject.Inject

/** The presenter for [ProfileRenameActivity]. */
@ActivityScope
class ProfileRenameActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  fun handleOnCreate() {
    val binding = DataBindingUtil.setContentView<ProfileRenameActivityBinding>(activity, R.layout.profile_rename_activity)
  }
}
