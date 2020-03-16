package org.oppia.app.walkthrough

import android.content.Intent
import androidx.test.core.app.ActivityScenario.*
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.model.ProfileId
import org.oppia.app.onboarding.OnboardingActivity
import org.oppia.domain.profile.ProfileTestHelper
import javax.inject.Inject

/** Tests for [WalkthroughWelcomeFragment]. */
@RunWith(AndroidJUnit4::class)
class WalkthroughWelcomeFragmentTest {

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  private val internalProfileId = 0

  private lateinit var profileId: ProfileId

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles()
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  private fun setUpTestApplicationComponent() {
    DaggerProfileProgressFragmentTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  private fun createWalkthrouhgActivityIntent(profileId: Int): Intent {
    return WalkthroughActivity.createWalkthroughActivityIntent(
      ApplicationProvider.getApplicationContext(),
      profileId
    )
  }

  @Test
  fun testWalkthoughWelcomeFragment_checkDescription_isCorrect() {
    launch<OnboardingActivity>(createWalkthrouhgActivityIntent(0)).use {
      Espresso.onView(
        CoreMatchers.allOf(
          withId(R.id.walkthrough_welcome_description_text_view),
          isCompletelyDisplayed()
        )
      ).check(ViewAssertions.matches(withText(R.string.walkthrough_welcome_description)))
    }
  }

  @Test
  fun testWalkthoughWelcomeFragment_checkProfileName_isCorrect() {
    launch<OnboardingActivity>(createWalkthrouhgActivityIntent(0)).use {
      Espresso.onView(
        CoreMatchers.allOf(
          withId(R.id.walkthrough_welcome_title_text_view),
          isCompletelyDisplayed()
        )
      ).check(ViewAssertions.matches(withText("Welcome Sean!")))
    }
  }
}
