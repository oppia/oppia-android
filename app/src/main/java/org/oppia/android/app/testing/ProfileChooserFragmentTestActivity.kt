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

  /** Dagger injector for [ProfileChooserFragmentTestActivity]. */
  interface Injector {
    /** Injects dependencies into the [activity]. */
    fun inject(activity: ProfileChooserFragmentTestActivity)
  }

  companion object {
    /** Returns an [Intent] for opening new instances of [ProfileChooserFragmentTestActivity]. */
    fun createIntent(context: Context): Intent =
      Intent(context, ProfileChooserFragmentTestActivity::class.java)
  }
}
