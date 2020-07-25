package org.oppia.app.topic.revisioncard

import android.widget.TextView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.parser.RichTextViewMatcher.Companion.containsRichText
import org.oppia.app.topic.revisioncard.RevisionCardActivity.Companion.createRevisionCardActivityIntent
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.domain.topic.FRACTIONS_TOPIC_ID
import org.oppia.domain.topic.SUBTOPIC_TOPIC_ID
import org.oppia.domain.topic.SUBTOPIC_TOPIC_ID_2

/** Tests for [RevisionCardActivity]. */
@RunWith(AndroidJUnit4::class)
class RevisionCardFragmentTest {

  @Before
  fun setUp() {
    FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    Intents.init()
  }

  @Test
  fun testRevisionCardTestActivity_toolbarTitle_fractionSubtopicId1_isDisplayedCorrectly() {
    launch<RevisionCardActivity>(
      createRevisionCardActivityIntent(
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
  fun testRevisionCardTestActivity_fractionSubtopicId2_checkExplanationAreDisplayedSuccessfully() {
    launch<RevisionCardActivity>(
      createRevisionCardActivityIntent(
        ApplicationProvider.getApplicationContext(),
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
        FRACTIONS_TOPIC_ID,
        SUBTOPIC_TOPIC_ID
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          instanceOf(TextView::class.java),
          withParent(withId(R.id.revision_card_toolbar))
        )
      ).check(matches(withText("What is Fraction?")))
    }
  }

  @Test
  fun testRevisionCardTestActivity_configurationChange_fractionSubtopicId2_checkExplanationAreDisplayedSuccessfully() { // ktlint-disable max-line-length
    launch<RevisionCardActivity>(
      createRevisionCardActivityIntent(
        ApplicationProvider.getApplicationContext(),
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
