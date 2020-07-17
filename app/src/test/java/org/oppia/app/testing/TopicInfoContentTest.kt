package org.oppia.app.testing

import android.widget.TextView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.topic.info.TopicInfoFragment

@RunWith(AndroidJUnit4::class)
class TopicInfoContentTest {

  @Before
  @ExperimentalCoroutinesApi
  fun setUp() {
    Intents.init()
    FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  private fun getTopicDescriptionTextView(activity: TopicInfoTestActivity): TextView? {
    val topicInfoFragment =
      activity.supportFragmentManager.findFragmentByTag(TopicInfoFragment.TOPIC_INFO_FRAGMENT_TAG)
    return topicInfoFragment?.view?.findViewById(R.id.topic_description_text_view)
  }

  private fun getSeeMoreTextView(activity: TopicInfoTestActivity): TextView? {
    val topicInfoFragment =
      activity.supportFragmentManager.findFragmentByTag(TopicInfoFragment.TOPIC_INFO_FRAGMENT_TAG)
    return topicInfoFragment?.view?.findViewById(R.id.see_more_text_view)
  }

  @Test
  fun test() {
    launch(TopicInfoTestActivity::class.java).use {
      it.onActivity { activity ->
        val seeMoreTextView = getSeeMoreTextView(activity)
        val descriptionTextView = getTopicDescriptionTextView(activity)
        assertThat(seeMoreTextView?.text.toString()).isEqualTo("See More")
        assertThat(descriptionTextView?.maxLines).isEqualTo(5)
        seeMoreTextView?.performClick()
        assertThat(descriptionTextView?.maxLines).isEqualTo(12)
        assertThat(seeMoreTextView?.text.toString()).isEqualTo("See Less")
      }
    }
  }
}
