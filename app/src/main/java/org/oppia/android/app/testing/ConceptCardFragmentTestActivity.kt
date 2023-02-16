package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.topic.conceptcard.ConceptCardFragment
import org.oppia.android.app.topic.conceptcard.ConceptCardListener
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** Test Activity used for testing ConceptCardFragment */
class ConceptCardFragmentTestActivity : InjectableAppCompatActivity(), ConceptCardListener {

  @Inject
  lateinit var conceptCardFragmentTestActivityController: ConceptCardFragmentTestActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    conceptCardFragmentTestActivityController.handleOnCreate(
      intent.extractCurrentUserProfileId()
    )
  }

  override fun dismissConceptCard() {
    getConceptCardFragment()?.dismiss()
  }

  private fun getConceptCardFragment(): ConceptCardFragment? {
    return supportFragmentManager.findFragmentByTag(TAG_CONCEPT_CARD_DIALOG) as ConceptCardFragment?
  }

  companion object {

    internal const val TAG_CONCEPT_CARD_DIALOG = "CONCEPT_CARD_DIALOG"

    fun createIntent(context: Context, profileId: ProfileId): Intent {
      return Intent(context, ConceptCardFragmentTestActivity::class.java).also {
        it.decorateWithUserProfileId(profileId)
      }
    }
  }
}
