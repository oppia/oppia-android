package org.oppia.app.activity

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.model.InteractionObject
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Matchers.instanceOf
import org.junit.After
import org.oppia.app.customview.inputInteractionView.NumericInputInteractionView
import org.oppia.app.customview.inputInteractionView.TextInputInteractionView

/**
 * Tests for [InputInteractionViewTestActivity].
 * it tests [NumericInputInteractionView] and [TextInputInteractionView].
 */
@RunWith(AndroidJUnit4::class)
class InputInteractionViewTestActivityTest {
  private lateinit var launchedActivity: Activity
  @get:Rule
  var activityTestRule: ActivityTestRule<InputInteractionViewTestActivity> = ActivityTestRule(
    InputInteractionViewTestActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )

  @Test
  fun testNumericInputInteractionView_withNoInputText_hasCorrectPendingAnswerType() {
    val activityScenario = ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    activityScenario.onActivity { activity ->
      val textAnswerRetriever =
        activity.findViewById(R.id.test_number_input_interaction_view) as NumericInputInteractionView
      assertThat(textAnswerRetriever.getPendingAnswer(), instanceOf(InteractionObject::class.java))
    }
  }

  @Test
  fun testNumericInputInteractionView_withInputtedText_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_number_input_interaction_view)).perform(ViewActions.clearText(), ViewActions.typeText("9"))
    activityScenario.onActivity { activity ->
      val textAnswerRetriever =
        activity.findViewById(R.id.test_number_input_interaction_view) as NumericInputInteractionView
      assertEquals(textAnswerRetriever.getPendingAnswer().objectTypeCase, InteractionObject.ObjectTypeCase.REAL)
      assertThat(textAnswerRetriever.getPendingAnswer().real, `is`("9".toDouble()))
    }
  }

  @Test
  fun testNumericInputInteractionView_withInputtedText_hasCorrectPendingAnswerWithDecimalValues() {
    val activityScenario = ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_number_input_interaction_view)).perform(
      ViewActions.clearText(),
      ViewActions.typeText("9.5")
    )
    activityScenario.onActivity { activity ->
      val textAnswerRetriever =
        activity.findViewById(R.id.test_number_input_interaction_view) as NumericInputInteractionView
      assertEquals(textAnswerRetriever.getPendingAnswer().objectTypeCase, InteractionObject.ObjectTypeCase.REAL)
      assertThat(textAnswerRetriever.getPendingAnswer().real, `is`(9.5))
    }
  }

  @Test
  fun testNumericInputInteractionView_withInputtedText_onConfigurationChange_hasCorrectPendingAnswer() {
    Intents.init()
    val intent = Intent(Intent.ACTION_PICK)
    launchedActivity = activityTestRule.launchActivity(intent)
    onView(withId(R.id.test_number_input_interaction_view)).perform(ViewActions.clearText(), ViewActions.typeText("9"))
    activityTestRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    activityTestRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    onView(withId(R.id.test_number_input_interaction_view)).check(matches(isDisplayed())).check(matches(withText("9")))
    Intents.release()
  }

  @Test
  fun testTextInputInteractionView_withNoInputText_hasCorrectPendingAnswerType() {
    val activityScenario = ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    activityScenario.onActivity { activity ->
      val textAnswerRetriever =
        activity.findViewById(R.id.test_text_input_interaction_view) as TextInputInteractionView
      assertThat(textAnswerRetriever.getPendingAnswer(), instanceOf(InteractionObject::class.java))
    }
  }

  @Test
  fun testTextInputInteractionView_withInputtedText_hasCorrectPendingAnswer() {
    val activityScenario = ActivityScenario.launch(InputInteractionViewTestActivity::class.java)
    onView(withId(R.id.test_text_input_interaction_view)).perform(ViewActions.clearText(), ViewActions.typeText("abc"))
    activityScenario.onActivity { activity ->
      val textAnswerRetriever =
        activity.findViewById(R.id.test_text_input_interaction_view) as TextInputInteractionView
      assertEquals(
        textAnswerRetriever.getPendingAnswer().objectTypeCase,
        InteractionObject.ObjectTypeCase.NORMALIZED_STRING
      )
      assertThat(textAnswerRetriever.getPendingAnswer().normalizedString, `is`("abc"))
    }
  }

  @Test
  fun testTextInputInteractionView_withInputtedText_onConfigurationChange_hasCorrectPendingAnswer() {
    Intents.init()
    val intent = Intent(Intent.ACTION_PICK)
    launchedActivity = activityTestRule.launchActivity(intent)
    onView(withId(R.id.test_text_input_interaction_view)).perform(ViewActions.clearText(), ViewActions.typeText("abc"))
    activityTestRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    activityTestRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    onView(withId(R.id.test_text_input_interaction_view)).check(matches(isDisplayed())).check(matches(withText("abc")))
  }

  @After
  fun tearDown() {
    Intents.release()
  }
}
