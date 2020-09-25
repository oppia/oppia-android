package org.oppia.android.data.backends.gae.api

import org.oppia.android.data.backends.gae.model.GaeStory
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** Service that provides access to story endpoints. */
interface StoryService {

  @GET("story_data_handler/{story_id}")
  fun getStory(
    @Path("story_id") storyId: String,
    @Query("user_id") userId: String?,
    @Query("user") user: String?
  ): Call<GaeStory>
}
