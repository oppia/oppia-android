package org.oppia.app.testing

import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.profile.ProfileActivityPresenter
import org.oppia.app.profile.ProfileChooserFragment
import org.oppia.app.topic.conceptcard.ConceptCardListener
import javax.inject.Inject

/** Test Activity used for testing ConceptCardFragment */
class ProfileChooserFragmentTestActivity : InjectableAppCompatActivity() {

  @Inject lateinit var profileActivityPresenter: ProfileActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    profileActivityPresenter.handleOnCreate()
  }

   fun getProfileChooserFragment(): ProfileChooserFragment? {
    return supportFragmentManager.findFragmentByTag(TAG_PROFILE_CHOOSER_FRAGMENT) as ProfileChooserFragment?
  }

  companion object {
    internal const val TAG_PROFILE_CHOOSER_FRAGMENT = "TAG_PROFILE_CHOOSER_FRAGMENT"
  }
}
