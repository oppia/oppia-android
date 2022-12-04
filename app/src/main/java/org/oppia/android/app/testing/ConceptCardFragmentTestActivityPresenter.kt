package org.oppia.android.app.testing

import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.testing.ConceptCardFragmentTestActivity.Companion.TAG_CONCEPT_CARD_DIALOG
import org.oppia.android.app.topic.conceptcard.ConceptCardFragment
import org.oppia.android.domain.topic.TEST_SKILL_ID_0
import org.oppia.android.domain.topic.TEST_SKILL_ID_1
import javax.inject.Inject
import org.oppia.android.app.topic.conceptcard.ConceptCardFactory

/** The presenter for [ConceptCardFragmentTestActivity] */
class ConceptCardFragmentTestActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val conceptCardFactory: ConceptCardFactory
) {
  fun handleOnCreate(profileId: ProfileId) {
    activity.setContentView(R.layout.concept_card_fragment_test_activity)
    activity.findViewById<Button>(R.id.open_dialog_0).setOnClickListener {
      val frag = conceptCardFactory.createCard(TEST_SKILL_ID_0, profileId)
      frag?.showNow(activity.supportFragmentManager, TAG_CONCEPT_CARD_DIALOG)
    }
    activity.findViewById<Button>(R.id.open_dialog_1).setOnClickListener {
      val frag = conceptCardFactory.createCard(TEST_SKILL_ID_1, profileId)
      frag?.showNow(activity.supportFragmentManager, TAG_CONCEPT_CARD_DIALOG)
    }
  }
}
