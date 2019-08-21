package org.oppia.data.backends.gae.api

import org.oppia.data.backends.gae.model.Topic
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface TopicService {

  @GET("topic_data_handler/{topic_name}")
  fun getTopicByTopicName(@Path("topic_name") topicName: String): Call<Topic>

}
