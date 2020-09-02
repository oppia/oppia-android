package org.oppia.app.topic.revisioncard

import android.content.Context
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.help.HelpActivity
import org.oppia.app.options.OptionsActivity
import org.oppia.app.parser.RichTextViewMatcher.Companion.containsRichText
import org.oppia.app.player.exploration.ExplorationActivity
import org.oppia.app.topic.revisioncard.RevisionCardActivity.Companion.createRevisionCardActivityIntent // ktlint-disable max-line-length
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.domain.topic.FRACTIONS_TOPIC_ID
import org.oppia.domain.topic.SUBTOPIC_TOPIC_ID
import org.oppia.domain.topic.SUBTOPIC_TOPIC_ID_2
import org.robolectric.annotation.LooperMode
import javax.inject.Inject

/** Tests for [RevisionCardActivity]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class RevisionCardFragmentTest {

  private val internalProfileId = 1

  @Inject
  lateinit var context: Context

  @Before
  fun setUp() {
    Intents.init()
    context = ApplicationProvider.getApplicationContext()
    FirebaseApp.initializeApp(context)
  }

  @Test
  fun testRevisionCardTest_overflowMenu_isDisplayedSuccessfully() {
    launch<ExplorationActivity>(
      createRevisionCardActivityIntent(
        ApplicationProvider.getApplicationContext(),
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        SUBTOPIC_TOPIC_ID
      )
    ).use {
      openActionBarOverflowOrOptionsMenu(context)
      onView(withText(context.getString(R.string.menu_options))).check(matches(isDisplayed()))
      onView(withText(context.getString(R.string.help))).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testRevisionCardTest_openOverflowMenu_selectHelpInOverflowMenu_opensHelpActivity() {
    launch<ExplorationActivity>(
      createRevisionCardActivityIntent(
        ApplicationProvider.getApplicationContext(),
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        SUBTOPIC_TOPIC_ID
      )
    ).use {
      openActionBarOverflowOrOptionsMenu(context)
      onView(withText(context.getString(R.string.help))).perform(ViewActions.click())
      intended(hasComponent(HelpActivity::class.java.name))
      intended(
        hasExtra(
          HelpActivity.IS_FROM_NAVIGATION_DRAWER_EXTRA_KEY,
          /* value= */ false
        )
      )
    }
  }

  @Test
  fun testRevisionCardTest_openOverflowMenu_selectOptionsInOverflowMenu_opensOptionsActivity() {
    launch<ExplorationActivity>(
      createRevisionCardActivityIntent(
        ApplicationProvider.getApplicationContext(),
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        SUBTOPIC_TOPIC_ID
      )
    ).use {
      openActionBarOverflowOrOptionsMenu(context)
      onView(withText(context.getString(R.string.menu_options))).perform(ViewActions.click())
      intended(hasComponent(OptionsActivity::class.java.name))
      intended(
        hasExtra(
          OptionsActivity.IS_FROM_NAVIGATION_DRAWER_EXTRA_KEY,
          /* value= */ false
        )
      )
    }
  }

  @Test
  fun testRevisionCardTestActivity_toolbarTitle_fractionSubtopicId1_isDisplayedCorrectly() {
    launch<RevisionCardActivity>(
      createRevisionCardActivityIntent(
        ApplicationProvider.getApplicationContext(),
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        SUBTOPIC_TOPIC_ID
      )
    ).use {
      onView(withId(R.id.revision_card_toolbar_title))
        .check(matches(withText("What is Fraction?")))
    }
  }

  @Test
  fun testRevisionCardTestActivity_fractionSubtopicId2_checkExplanationAreDisplayedSuccessfully() {
    launch<RevisionCardActivity>(
      createRevisionCardActivityIntent(
        ApplicationProvider.getApplicationContext(),
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        SUBTOPIC_TOPIC_ID_2
      )
    ).use {
      onView(withId(R.id.revision_card_explanation_text))
        .check(
          matches(
            withText(
              "Description of subtopic is here."
            )
          )
        )
      onView(withId(R.id.revision_card_explanation_text))
        .check(
          matches(
            not(
              containsRichText()
            )
          )
        )
    }
  }

  @Test
  fun testRevisionCardTestActivity_fractionSubtopicId1_checkReturnToTopicButtonIsDisplayedSuccessfully() { // ktlint-disable max-line-length
    launch<RevisionCardActivity>(
      createRevisionCardActivityIntent(
        ApplicationProvider.getApplicationContext(),
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        SUBTOPIC_TOPIC_ID
      )
    ).use {
      onView(withId(R.id.revision_card_return_button))
        .check(
          matches(
            withText(
              R.string.return_to_topic
            )
          )
        )
    }
  }

  @Test
  fun testRevisionCardTestActivity_configurationChange_toolbarTitle_fractionSubtopicId1_isDisplayedCorrectly() { // ktlint-disable max-line-length
    launch<RevisionCardActivity>(
      createRevisionCardActivityIntent(
        ApplicationProvider.getApplicationContext(),
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        SUBTOPIC_TOPIC_ID
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.revision_card_toolbar_title))
        .check(matches(withText("What is Fraction?")))
    }
  }

  @Test
  fun testRevisionCardTestActivity_configurationChange_fractionSubtopicId2_checkExplanationAreDisplayedSuccessfully() { // ktlint-disable max-line-length
    launch<RevisionCardActivity>(
      createRevisionCardActivityIntent(
        ApplicationProvider.getApplicationContext(),
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        SUBTOPIC_TOPIC_ID_2
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.revision_card_explanation_text))
        .check(
          matches(
            withText(
              "Description of subtopic is here."
            )
          )
        )
      onView(withId(R.id.revision_card_explanation_text))
        .check(
          matches(
            not(
              containsRichText()
            )
          )
        )
    }
  }

  @Test
  fun testRevisionCardTestActivity_configurationChange_fractionSubtopicId1_checkReturnToTopicButtonIsDisplayedSuccessfully() { // ktlint-disable max-line-length
    launch<RevisionCardActivity>(
      createRevisionCardActivityIntent(
        ApplicationProvider.getApplicationContext(),
        internalProfileId,
        FRACTIONS_TOPIC_ID,
        SUBTOPIC_TOPIC_ID
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.revision_card_return_button))
        .check(
          matches(
            withText(
              R.string.return_to_topic
            )
          )
        )
    }
  }

  @After
  fun tearDown() {
    Intents.release()
  }
}
