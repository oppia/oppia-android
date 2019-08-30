package org.oppia.data

import org.oppia.data.backends.gae.api.TopicService
import org.oppia.data.backends.gae.model.GaeTopic
import retrofit2.Call
import retrofit2.mock.BehaviorDelegate

/**
 * Mock TopicService with dummy data
 */
class MockTopicService(private val delegate: BehaviorDelegate<TopicService>) : TopicService {
  override fun getTopicByName(topicName: String): Call<GaeTopic> {
    val topic = GaeTopic(
      "1",
      "Test Topic",
      null,
      null,
      null,
      null,
      null,
      null
    )
    return delegate.returningResponse(topic).getTopicByName("Test Topic")
  }
}

