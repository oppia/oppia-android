package org.oppia.android.app.testing

import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.topic.conceptcard.ConceptCardFragment
import org.oppia.android.app.topic.conceptcard.ConceptCardListener
import javax.inject.Inject
import org.oppia.android.app.activity.ActivityComponentImpl

/** Test Activity used for testing ConceptCardFragment */
class ConceptCardFragmentTestActivity : InjectableAppCompatActivity(), ConceptCardListener {

  @Inject
  lateinit var conceptCardFragmentTestActivityController: ConceptCardFragmentTestActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    conceptCardFragmentTestActivityController.handleOnCreate()
  }

  override fun dismissConceptCard() {
    getConceptCardFragment()?.dismiss()
  }

  private fun getConceptCardFragment(): ConceptCardFragment? {
    return supportFragmentManager.findFragmentByTag(TAG_CONCEPT_CARD_DIALOG) as ConceptCardFragment?
  }

  companion object {
    internal const val TAG_CONCEPT_CARD_DIALOG = "CONCEPT_CARD_DIALOG"
  }
}
