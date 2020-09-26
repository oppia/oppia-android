package org.oppia.android.data.backends.gae.api

import org.oppia.android.data.backends.gae.model.GaeTopic
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

/** Service that provides access to topic endpoints. */
interface TopicService {

  @GET("topic_data_handler/{topic_name}")
  fun getTopicByName(@Path("topic_name") topicName: String): Call<GaeTopic>
}
