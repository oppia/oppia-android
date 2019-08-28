package org.oppia.app.backend.api

import org.oppia.app.backend.model.TopicIndexHandler
import retrofit2.Call
import retrofit2.http.GET

interface TopicIndexHandlerService {

  @GET("bins/6ztun")  // replace endpoint to topicindexhandler
  fun getTopicIndex(): Call<TopicIndexHandler>

}

