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
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.ViewAction
import androidx.test.platform.ui.UiController
import junit.framework.TestCase.assertEquals
import org.hamcrest.Matcher

/** Tests for [StateFragmentPresenter]. */
@RunWith(AndroidJUnit4::class)
class StateFragmentPresenterTest {

  // MultipleSelection  input interaction views
  @Test
  fun testMultipleSelectionInputUnteraction() {
    ActivityScenario.launch(ExplorationActivity::class.java).use {

      onView(withId(R.id.interactionRadioGroup)).perform(object : ViewAction {

        override fun getConstraints(): Matcher<View> {
          return isDisplayed()
        }
        override fun getDescription(): String {
          return "Performing click"
        }
        override fun perform(uiController: androidx.test.espresso.UiController?, view: View?) {
          //The view which we got in argument is the same view which Espresso found using onView(withId(R.id.parent_container))
          val parentRadioGroup = view as RadioGroup
          //Get the Radiogroup inside the LinearLayout
          val linearLayout = parentRadioGroup.getChildAt(0) as RadioGroup
          //Get the Button inside the inner LinearLayout
          val radioButton = linearLayout.getChildAt(0) as CustomRadioButton
          radioButton.performClick()
        }
      })
    }
  }

  // ItemSelection input interaction views
  @Test
  fun testItemSelectionInputUnteraction() {
    ActivityScenario.launch(ExplorationActivity::class.java).use {

      onView(withId(R.id.interactionContainer)).perform(object : ViewAction {

        override fun getConstraints(): Matcher<View> {
          return isDisplayed()
        }
        override fun getDescription(): String {
          return "Performing click"
        }
        override fun perform(uiController: androidx.test.espresso.UiController?, view: View?) {
          //The view which we got in argument is the same view which Espresso found using onView(withId(R.id.parent_container))
          val parentLinearLayout = view as LinearLayout
//          //Get the checkbox inside the inner LinearLayout
          val checkbox = parentLinearLayout.getChildAt(1) as CustomCheckbox
          checkbox.performClick()
        }
      })
    }
  }
}
