package org.oppia.app.topic.conceptcard.testing

import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

class ConceptCardFragmentTestActivity : InjectableAppCompatActivity() {

  @Inject lateinit var conceptCardFragmentTestActivityController: ConceptCardFragmentTestActivityController

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    conceptCardFragmentTestActivityController.handleOnCreate()
  }
}
