package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.utility.activity.ActivityComponentImpl
import org.oppia.android.app.utility.activity.InjectableAppCompatActivity
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.topic.conceptcard.ConceptCardFragment
import org.oppia.android.app.topic.conceptcard.ConceptCardListener
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProtoExtra
import javax.inject.Inject

/** Test Activity used for testing ConceptCardFragment */
class ConceptCardFragmentTestActivity : InjectableAppCompatActivity(), ConceptCardListener {

  @Inject
  lateinit var conceptCardFragmentTestActivityController: ConceptCardFragmentTestActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    conceptCardFragmentTestActivityController.handleOnCreate(
      intent.getProtoExtra(TEST_ACTIVITY_PROFILE_ID_ARGUMENT_KEY, ProfileId.getDefaultInstance())
    )
  }

  override fun dismissConceptCard() {
    getConceptCardFragment()?.dismiss()
  }

  private fun getConceptCardFragment(): ConceptCardFragment? {
    return supportFragmentManager.findFragmentByTag(TAG_CONCEPT_CARD_DIALOG) as ConceptCardFragment?
  }

  companion object {
    private const val TEST_ACTIVITY_PROFILE_ID_ARGUMENT_KEY =
      "ConceptCardFragmentTestActivity.profile_id"

    internal const val TAG_CONCEPT_CARD_DIALOG = "CONCEPT_CARD_DIALOG"

    fun createIntent(context: Context, profileId: ProfileId): Intent {
      return Intent(context, ConceptCardFragmentTestActivity::class.java).also {
        it.putProtoExtra(TEST_ACTIVITY_PROFILE_ID_ARGUMENT_KEY, profileId)
      }
    }
  }
}
