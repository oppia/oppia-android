package org.oppia.app.testing

import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
//import kotlinx.android.synthetic.main.concept_card_fragment_test_activity.*
import org.oppia.app.ui.R
import org.oppia.app.testing.ConceptCardFragmentTestActivity.Companion.TAG_CONCEPT_CARD_DIALOG
import org.oppia.app.topic.conceptcard.ConceptCardFragment
import org.oppia.domain.topic.TEST_SKILL_ID_0
import org.oppia.domain.topic.TEST_SKILL_ID_1
import javax.inject.Inject

/** The presenter for [ConceptCardFragmentTestActivity] */
class ConceptCardFragmentTestActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  fun handleOnCreate() {
    activity.setContentView(R.layout.concept_card_fragment_test_activity)
    activity.findViewById<Button>(R.id.open_dialog_0).setOnClickListener {
      val frag = ConceptCardFragment.newInstance(TEST_SKILL_ID_0)
      frag.showNow(activity.supportFragmentManager, TAG_CONCEPT_CARD_DIALOG)
    }
    activity.findViewById<Button>(R.id.open_dialog_1).setOnClickListener {
      val frag = ConceptCardFragment.newInstance(TEST_SKILL_ID_1)
      frag.showNow(activity.supportFragmentManager, TAG_CONCEPT_CARD_DIALOG)
    }
  }
}
