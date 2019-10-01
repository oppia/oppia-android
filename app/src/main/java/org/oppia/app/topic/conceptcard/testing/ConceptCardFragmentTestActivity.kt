package org.oppia.app.topic.conceptcard.testing

import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** Test Activity used for testing ConceptCardFragment */
class ConceptCardFragmentTestActivity : InjectableAppCompatActivity() {

  @Inject lateinit var conceptCardFragmentTestActivityController: ConceptCardFragmentTestActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    conceptCardFragmentTestActivityController.handleOnCreate()
  }
}
