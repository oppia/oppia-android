package org.oppia.android.testing.network

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.oppia.android.data.gae.gae.NetworkInterceptor
import org.oppia.android.data.gae.gae.NetworkSettings
import org.oppia.android.data.gae.gae.api.TopicService
import org.oppia.android.data.gae.gae.model.GaeTopic
import org.oppia.android.testing.ApiUtils
import retrofit2.Call
import retrofit2.mock.BehaviorDelegate

/**
 * Mock TopicService with dummy data from [topic.json]
 */
class MockTopicService(private val delegate: BehaviorDelegate<TopicService>) : TopicService {
  override fun getTopicByName(topicName: String): Call<GaeTopic> {
    val topic = createMockGaeTopic()
    return delegate.returningResponse(topic).getTopicByName(topicName)
  }

  /**
   * This function creates a mock GaeTopic with data from dummy json.
   * @return GaeTopic: GaeTopic with mock data
   */
  private fun createMockGaeTopic(): GaeTopic {
    val networkInterceptor = NetworkInterceptor()
    var topicResponseWithXssiPrefix =
      NetworkSettings.XSSI_PREFIX + ApiUtils.getFakeJson("topic.json")

    topicResponseWithXssiPrefix = networkInterceptor.removeXSSIPrefix(topicResponseWithXssiPrefix)

    val moshi = Moshi.Builder().build()
    val adapter: JsonAdapter<GaeTopic> = moshi.adapter(GaeTopic::class.java)
    val mockGaeTopic = adapter.fromJson(topicResponseWithXssiPrefix)

    return mockGaeTopic!!
  }
}
