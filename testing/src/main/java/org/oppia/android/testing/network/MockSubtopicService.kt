package org.oppia.android.testing.network

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.oppia.android.data.backends.gae.JsonPrefixNetworkInterceptor
import org.oppia.android.data.backends.gae.NetworkSettings
import org.oppia.android.data.backends.gae.api.SubtopicService
import org.oppia.android.data.backends.gae.model.GaeSubtopic
import retrofit2.Call
import retrofit2.mock.BehaviorDelegate

/**
 * Mock SubtopicService with dummy data from [subtopic.json]
 */
class MockSubtopicService(private val delegate: BehaviorDelegate<SubtopicService>) :
  SubtopicService {
  override fun getSubtopic(topicName: String, subtopicId: String): Call<GaeSubtopic> {
    val subtopic = createMockGaeSubtopic()
    return delegate.returningResponse(subtopic).getSubtopic(topicName, subtopicId)
  }

  /**
   * This function creates a mock GaeSubtopic with data from dummy json.
   * @return GaeSubtopic: GaeSubtopic with mock data
   */
  private fun createMockGaeSubtopic(): GaeSubtopic {
    val networkInterceptor = JsonPrefixNetworkInterceptor()
    var subtopicResponseWithXssiPrefix =
      NetworkSettings.XSSI_PREFIX + ApiMockLoader.getFakeJson("subtopic.json")

    subtopicResponseWithXssiPrefix =
      networkInterceptor.removeXssiPrefix(subtopicResponseWithXssiPrefix)

    val moshi = Moshi.Builder().build()
    val adapter: JsonAdapter<GaeSubtopic> = moshi.adapter(GaeSubtopic::class.java)
    val mockGaeSubtopic = adapter.fromJson(subtopicResponseWithXssiPrefix)

    return mockGaeSubtopic!!
  }
}
