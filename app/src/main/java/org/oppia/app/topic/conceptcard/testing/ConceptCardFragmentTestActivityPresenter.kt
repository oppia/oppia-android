package org.oppia.app.topic.conceptcard.testing

import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.concept_card_fragment_test_activity.*
import org.oppia.app.R
import org.oppia.app.topic.conceptcard.ConceptCardFragment
import org.oppia.domain.topic.TEST_SKILL_ID_1
import org.oppia.domain.topic.TEST_SKILL_ID_2
import javax.inject.Inject

private const val TAG_CONCEPT_CARD_DIALOG = "CONCEPT_CARD_DIALOG"

/** The presenter for [ConceptCardFragmentTestActivity] */
class ConceptCardFragmentTestActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  fun handleOnCreate() {
    activity.setContentView(R.layout.concept_card_fragment_test_activity)
    activity.open_dialog_1.setOnClickListener {
      val frag = ConceptCardFragment.newInstance(TEST_SKILL_ID_1)
      frag.showNow(activity.supportFragmentManager, TAG_CONCEPT_CARD_DIALOG)
    }
    activity.open_dialog_2.setOnClickListener {
      val frag = ConceptCardFragment.newInstance(TEST_SKILL_ID_2)
      frag.showNow(activity.supportFragmentManager, TAG_CONCEPT_CARD_DIALOG)
    }
  }
}
