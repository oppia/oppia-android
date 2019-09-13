package org.oppia.data.backends.api

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.oppia.data.backends.ApiUtils
import org.oppia.data.backends.gae.NetworkInterceptor
import org.oppia.data.backends.gae.NetworkSettings
import org.oppia.data.backends.gae.api.SubtopicService
import org.oppia.data.backends.gae.model.GaeSubtopic
import retrofit2.Call
import retrofit2.mock.BehaviorDelegate

/**
 * Mock SubtopicService with dummy data from [subtopic.json]
 */
class MockSubtopicService(private val delegate: BehaviorDelegate<SubtopicService>) :
  SubtopicService {
  override fun getSubtopic(topic_name: String, subtopic_id: String): Call<GaeSubtopic> {
    val subtopic = createMockGaeSubtopic()
    return delegate.returningResponse(subtopic).getSubtopic(topic_name, subtopic_id)
  }

  /**
   * This function creates a mock GaeSubtopic with data from dummy json.
   * This function also tests the removeXSSIPrefix function from [NetworkInterceptor]
   * @return GaeSubtopic: GaeSubtopic with mock data
   */
  private fun createMockGaeSubtopic(): GaeSubtopic {
    val networkInterceptor = NetworkInterceptor()
    var subtopicResponseWithXssiPrefix =
      NetworkSettings.XSSI_PREFIX + ApiUtils.getFakeJson("subtopic.json")

    subtopicResponseWithXssiPrefix =
      networkInterceptor.removeXSSIPrefix(subtopicResponseWithXssiPrefix)

    val moshi = Moshi.Builder().build()
    val adapter: JsonAdapter<GaeSubtopic> = moshi.adapter(GaeSubtopic::class.java)
    val mockGaeSubtopic = adapter.fromJson(subtopicResponseWithXssiPrefix)

    return mockGaeSubtopic!!
  }
}
