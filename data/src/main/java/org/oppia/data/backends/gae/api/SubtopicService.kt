package org.oppia.data.backends.gae.api

import org.oppia.data.backends.gae.model.GaeSubtopic
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

/** Service that provides access to subtopic endpoints. */
interface SubtopicService {

  @GET("subtopic_data_handler/{topic_name}/{subtopic_id}")
  fun getSubtopic(
    @Path("topic_name") topicName: String,
    @Path("subtopic_id") subtopicId: String
  ): Call<GaeSubtopic>
}
