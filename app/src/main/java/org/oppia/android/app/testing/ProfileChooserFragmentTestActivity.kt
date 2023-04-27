package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** Test Activity used for testing [ProfileChooserFragment] */
class ProfileChooserFragmentTestActivity : InjectableAppCompatActivity() {

  @Inject
  lateinit var profileChooserFragmentTestActivityPresenter:
    ProfileChooserFragmentTestActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as Injector).inject(this)
    profileChooserFragmentTestActivityPresenter.handleOnCreate()
  }

  interface Injector {
    fun inject(activity: ProfileChooserFragmentTestActivity)
  }

  companion object {
    // TODO(#1655): Re-restrict access to fields in tests post-Gradle.
    const val TAG_PROFILE_CHOOSER_FRAGMENT = "TAG_PROFILE_CHOOSER_FRAGMENT"

    fun createIntent(context: Context): Intent =
      Intent(context, ProfileChooserFragmentTestActivity::class.java)
  }
}
