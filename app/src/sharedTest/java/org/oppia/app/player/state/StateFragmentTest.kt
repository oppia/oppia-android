package org.oppia.app.player.state;

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.player.exploration.ExplorationActivity
import junit.framework.TestCase.assertEquals as assertEquals1

/** Tests for [StateFragment]. */
@RunWith(AndroidJUnit4::class)
class StateFragmentTest {
  var interactionInstanceId: String? = null

  @Test
  fun testMultipleChoiceInput_showsRadioButtons_onMultipleChoiceInputInteractionInstanceId_userSelectsDesiredOption() {
    ActivityScenario.launch(ExplorationActivity::class.java).use {
      interactionInstanceId = "MultipleChoiceInput"
      assertEquals1(interactionInstanceId, "MultipleChoiceInput")
      onView(withId(R.id.rvInteractions)).perform(
        RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(2,click()))
    }
  }

  @Test
  fun tesItemSelectionInput_showsCheckbox_onItemSelectionInputInteractionInstanceId_userCanSelectMoreThanOneCorrectOption() {
    ActivityScenario.launch(ExplorationActivity::class.java).use {
      interactionInstanceId = "ItemSelectionInput"
      assertEquals1(interactionInstanceId, "ItemSelectionInput")
      onView(withId(R.id.rvInteractions)).perform(
        RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1,click()))
    }
  }
}
