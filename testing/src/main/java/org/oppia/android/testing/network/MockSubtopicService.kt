package org.oppia.android.testing.network

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.oppia.android.data.gae.gae.NetworkInterceptor
import org.oppia.android.data.gae.gae.NetworkSettings
import org.oppia.android.data.gae.gae.api.SubtopicService
import org.oppia.android.data.gae.gae.model.GaeSubtopic
import org.oppia.android.testing.ApiUtils
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
