package org.oppia.app.backend.api

import com.example.myapplication.backend.model.TopicSummaryHandler
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TopicIndexHandlerService {

//  https://api.myjson.com/bins/6ztun
  @GET("bins/6ztun")
  fun setTopicIndex(): Call<TopicSummaryHandler>

  @GET("bins/6ztun")  // replace endpoint to topicindexhandler
  fun getTopicIndex(): Call<TopicSummaryHandler>

}

