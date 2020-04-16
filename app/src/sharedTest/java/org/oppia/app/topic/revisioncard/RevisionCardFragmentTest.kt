package org.oppia.app.topic.revisioncard

import android.widget.TextView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.parser.RichTextViewMatcher.Companion.containsRichText
import org.oppia.app.topic.revisioncard.RevisionCardActivity.Companion.createReviewCardActivityIntent
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.domain.topic.FRACTIONS_TOPIC_ID
import org.oppia.domain.topic.SUBTOPIC_TOPIC_ID

/** Tests for [RevisionCardActivity]. */
@RunWith(AndroidJUnit4::class)
class RevisionCardFragmentTest {

  @Before
  fun setUp() {
    Intents.init()
  }

  @Test
  fun testReviewCardTestActivity_toolbarTitle_fractionSubtopicId1_isDisplayedCorrectly() {
    launch<RevisionCardActivity>(
      createReviewCardActivityIntent(
        ApplicationProvider.getApplicationContext(),
        FRACTIONS_TOPIC_ID,
        SUBTOPIC_TOPIC_ID
      )
    ).use {
      onView(
        allOf(
          instanceOf(TextView::class.java),
          withParent(withId(R.id.revision_card_toolbar))
        )
      ).check(matches(withText("What is Fraction?")))
    }
  }

  @Test
  fun testReviewCardTestActivity_fractionSubtopicId1_checkExplanationAreDisplayedSuccessfully() {
    launch<RevisionCardActivity>(
      createReviewCardActivityIntent(
        ApplicationProvider.getApplicationContext(),
        FRACTIONS_TOPIC_ID,
        SUBTOPIC_TOPIC_ID
      )
    ).use {
      onView(withId(R.id.revision_card_explanation_text)).check(matches(withText("Description of subtopic is here.")))
      onView(withId(R.id.revision_card_explanation_text)).check(matches(not(containsRichText())))
    }
  }

  @Test
  fun testReviewCardTestActivity_fractionSubtopicId1_checkReturnToTopicButtonIsDisplayedSuccessfully() {
    launch<RevisionCardActivity>(
      createReviewCardActivityIntent(
        ApplicationProvider.getApplicationContext(),
        FRACTIONS_TOPIC_ID,
        SUBTOPIC_TOPIC_ID
      )
    ).use {
      onView(withId(R.id.revision_card_return_button)).check(matches(withText(R.string.return_to_topic)))
    }
  }

  @Test
  fun testReviewCardTestActivity_configurationChange_toolbarTitle_fractionSubtopicId1_isDisplayedCorrectly() {
    launch<RevisionCardActivity>(
      createReviewCardActivityIntent(
        ApplicationProvider.getApplicationContext(),
        FRACTIONS_TOPIC_ID,
        SUBTOPIC_TOPIC_ID
      )
    ).use {
      onView(ViewMatchers.isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          instanceOf(TextView::class.java),
          withParent(withId(R.id.revision_card_toolbar))
        )
      ).check(matches(withText("What is Fraction?")))
    }
  }

  @Test
  fun testReviewCardTestActivity_configurationChange_fractionSubtopicId1_checkExplanationAreDisplayedSuccessfully() {
    launch<RevisionCardActivity>(
      createReviewCardActivityIntent(
        ApplicationProvider.getApplicationContext(),
        FRACTIONS_TOPIC_ID,
        SUBTOPIC_TOPIC_ID
      )
    ).use {
      onView(ViewMatchers.isRoot()).perform(orientationLandscape())
      onView(withId(R.id.revision_card_explanation_text)).check(matches(withText("Description of subtopic is here.")))
      onView(withId(R.id.revision_card_explanation_text)).check(matches(not(containsRichText())))
    }
  }

  @Test
  fun testReviewCardTestActivity_configurationChange_fractionSubtopicId1_checkReturnToTopicButtonIsDisplayedSuccessfully() {
    launch<RevisionCardActivity>(
      createReviewCardActivityIntent(
        ApplicationProvider.getApplicationContext(),
        FRACTIONS_TOPIC_ID,
        SUBTOPIC_TOPIC_ID
      )
    ).use {
      onView(ViewMatchers.isRoot()).perform(orientationLandscape())
      onView(withId(R.id.revision_card_return_button)).check(matches(withText(R.string.return_to_topic)))
    }
  }

  @After
  fun tearDown() {
    Intents.release()
  }
}
