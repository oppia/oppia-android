package org.oppia.android.testing.network

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.oppia.android.data.backends.gae.JsonPrefixNetworkInterceptor
import org.oppia.android.data.backends.gae.api.StoryService
import org.oppia.android.data.backends.gae.model.GaeStory
import retrofit2.Call
import retrofit2.mock.BehaviorDelegate

/**
 * Mock StoryService with dummy data from [story.json]
 */
class MockStoryService(
  private val delegate: BehaviorDelegate<StoryService>,
  private val xssiPrefix: String
) : StoryService {
  override fun getStory(storyId: String, userId: String?, user: String?): Call<GaeStory> {
    val story = createMockGaeStory()
    return delegate.returningResponse(story).getStory(storyId, userId, user)
  }

  /**
   * This function creates a mock GaeStory with data from dummy json.
   * @return GaeStory: GaeStory with mock data
   */
  private fun createMockGaeStory(): GaeStory {
    val networkInterceptor = JsonPrefixNetworkInterceptor(xssiPrefix)
    var storyResponseWithXssiPrefix =
      xssiPrefix + ApiMockLoader.getFakeJson("story.json")

    storyResponseWithXssiPrefix = networkInterceptor.removeXssiPrefix(storyResponseWithXssiPrefix)

    val moshi = Moshi.Builder().build()
    val adapter: JsonAdapter<GaeStory> = moshi.adapter(GaeStory::class.java)
    val mockGaeStory = adapter.fromJson(storyResponseWithXssiPrefix)

    return mockGaeStory!!
  }
}
