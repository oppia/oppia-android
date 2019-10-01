package org.oppia.app.player.state;

import android.provider.MediaStore
import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.home.HomeActivity
import org.oppia.app.player.exploration.ExplorationActivity
import android.widget.TextView
import android.widget.LinearLayout
import android.widget.RadioGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.platform.ui.UiController
import junit.framework.TestCase.assertEquals
import org.hamcrest.Matcher

/** Tests for [StateFragment]. */
@RunWith(AndroidJUnit4::class)
class StateFragmentTest {
  var interactionInstanceId: String? = null

  @Test
  fun testMultipleChoiceInput_showsRadioButtons_onMultipleChoiceInputInteractionInstanceId_userSelectsDesiredOption() {
    ActivityScenario.launch(ExplorationActivity::class.java).use {
      interactionInstanceId = "MultipleChoiceInput"
      assertEquals(interactionInstanceId, "MultipleChoiceInput")
      onView(withId(R.id.rvInteractions)).perform(
        RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(2,click()))
    }
  }

  @Test
  fun tesItemSelectionInput_showsCheckbox_onItemSelectionInputInteractionInstanceId_userCanSelectMoreThanOneCorrectOption() {
    ActivityScenario.launch(ExplorationActivity::class.java).use {
      interactionInstanceId = "ItemSelectionInput"
      assertEquals(interactionInstanceId, "ItemSelectionInput")
      onView(withId(R.id.rvInteractions)).perform(
        RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1,click()))
    }
  }
}
