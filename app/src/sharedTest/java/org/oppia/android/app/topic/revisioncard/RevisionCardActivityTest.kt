package org.oppia.android.app.topic.revisioncard

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.ActivityTestRule
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.oppia.android.R
import org.oppia.android.domain.topic.TEST_TOPIC_ID_0
import javax.inject.Inject

class RevisionCardActivityTest {

  @Inject
  lateinit var context: Context

  private val internalProfileId = 0

  private val subTopicId = 0

  private fun createRevisionCardActivityIntent(
    internalProfileId: Int,
    topicId: String,
    subTopicId: Int
  ): Intent {
    return RevisionCardActivity.createRevisionCardActivityIntent(
      ApplicationProvider.getApplicationContext(),
      internalProfileId,
      topicId,
      subTopicId
    )
  }

  @get:Rule
  val activityTestRule: ActivityTestRule<RevisionCardActivity> = ActivityTestRule(
    RevisionCardActivity::class.java,
    /* initialTouchMode= */ true,
    /* launchActivity= */ false
  )

  @Test
  fun testRevisionCardActivity_hasCorrectLabel() {
    activityTestRule.launchActivity(
      createRevisionCardActivityIntent(
        internalProfileId = internalProfileId,
        topicId = TEST_TOPIC_ID_0,
        subTopicId = subTopicId
      )
    )
    val title = activityTestRule.activity.title

    // Verify that the activity label is correct as a proxy to verify TalkBack will announce the
    // correct string when it's read out.
    assertThat(title).isEqualTo(context.getString(R.string.revision_card_activity_title))
  }
}
