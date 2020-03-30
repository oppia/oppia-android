package org.oppia.app.testing

import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.profile.ProfileActivityPresenter
import javax.inject.Inject

/** Test Activity used for testing [ProfileChooserFragment] */
class ProfileChooserFragmentTestActivity : InjectableAppCompatActivity() {

  @Inject lateinit var profileActivityPresenter: ProfileActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    profileActivityPresenter.handleOnCreate()
  }

  companion object {
    internal const val TAG_PROFILE_CHOOSER_FRAGMENT = "TAG_PROFILE_CHOOSER_FRAGMENT"
  }
}
