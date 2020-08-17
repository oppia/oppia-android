package org.oppia.app.testing

import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** Test Activity used for testing [ProfileChooserFragment] */
class ProfileChooserFragmentTestActivity : InjectableAppCompatActivity() {

  @Inject
  lateinit var profileChooserFragmentTestActivityPresenter:
    ProfileChooserFragmentTestActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    profileChooserFragmentTestActivityPresenter.handleOnCreate()
  }

  companion object {
    // TODO(#1655): Re-restrict access to fields in tests post-Gradle.
    const val TAG_PROFILE_CHOOSER_FRAGMENT = "TAG_PROFILE_CHOOSER_FRAGMENT"
  }
}
