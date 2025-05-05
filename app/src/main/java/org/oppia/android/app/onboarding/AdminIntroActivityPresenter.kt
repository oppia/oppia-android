package org.oppia.android.app.onboarding

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.ui.R
import javax.inject.Inject

/** The presenter for [AdminIntroActivity]. */
@ActivityScope
class AdminIntroActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {

  /** Creates the view for [AdminIntroActivity]. */
  fun handleOnCreate(profileId: ProfileId) {
    activity.setContentView(R.layout.admin_intro_activity)
  }
}
