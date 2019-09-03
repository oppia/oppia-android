package org.oppia.data

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.oppia.data.backends.gae.api.TopicService
import org.oppia.data.backends.gae.model.GaeTopic
import retrofit2.Call
import retrofit2.mock.BehaviorDelegate

/**
 * Mock TopicService with dummy data from [FakeJsonResponse]
 */
class MockTopicService(private val delegate: BehaviorDelegate<TopicService>) : TopicService {
  override fun getTopicByName(topicName: String): Call<GaeTopic> {
    val topic = MockGaeTopic()
    return delegate.returningResponse(topic).getTopicByName(topicName)
  }

  private fun MockGaeTopic(): GaeTopic {
    val jsonResponse: String = FakeJsonResponse.TOPIC_SERVICE_RESPONSE

    val moshi = Moshi.Builder().build()
    val adapter: JsonAdapter<GaeTopic> = moshi.adapter(GaeTopic::class.java)
    val mockGaeTopic = adapter.fromJson(jsonResponse)

    return mockGaeTopic!!
  }
}
