package org.oppia.data.backends.gae.api

import org.oppia.data.backends.gae.model.TopicIndexModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ClassRoomDataHandlerService {

  @GET("classroom_data_handler/{classroom_name}")
  fun getTopicsummaryDicts(@Path("classroom_name") classRoomName: String): Call<TopicIndexModel>

}

