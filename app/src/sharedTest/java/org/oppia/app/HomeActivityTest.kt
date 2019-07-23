package org.oppia.app

import android.widget.TextView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Tests for [HomeActivity]. */
@RunWith(AndroidJUnit4::class)
class HomeActivityTest {
  @get:Rule
  val homeActivityScenarioRule = ActivityScenarioRule(HomeActivity::class.java)

  @Test
  fun testMainActivity_hasWelcomeString_usingTruth() {
    homeActivityScenarioRule.scenario.onActivity {
      val welcomeTextVew: TextView = it.findViewById(R.id.welcome_text_view)
      assertThat(welcomeTextVew.text).isEqualTo("Welcome to Oppia!")
    }
  }

  @Test
  fun testMainActivity_hasWelcomeString_usingEspressoMatchers() {
    onView(withId(R.id.welcome_text_view)).check(matches(withText("Welcome to Oppia!")))
  }
}
