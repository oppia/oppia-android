package org.oppia.data.backends.gae.api

import org.oppia.data.backends.gae.model.TopicIndexModel
import retrofit2.Call
import retrofit2.http.GET

interface TopicIndexHandlerService {

  @GET("topicindexhandler")
  fun getTopicIndex(): Call<TopicIndexModel>

}

