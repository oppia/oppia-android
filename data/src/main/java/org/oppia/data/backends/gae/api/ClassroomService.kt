package org.oppia.data.backends.gae.api

import org.oppia.data.backends.gae.model.GaeClassroom
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ClassroomService {

  @GET("classroom_data_handler/{classroom_name}")
  fun getClassroomTopicSummaryDicts(@Path("classroom_name") classRoomName: String): Call<GaeClassroom>

}

