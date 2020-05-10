package org.oppia.app.testing

import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** Test Activity used for testing AudioFragment */
class AudioFragmentTestActivity : InjectableAppCompatActivity() {

  @Inject lateinit var audioFragmentTestActivityController: AudioFragmentTestActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    audioFragmentTestActivityController.handleOnCreate()
  }
}
