package org.oppia.android.data.gae.gae.api

import org.oppia.android.data.gae.gae.model.GaeClassroom
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

/** Service that provides access to classroom endpoints. */
interface ClassroomService {

  @GET("classroom_data_handler/{classroom_name}")
  fun getClassroom(@Path("classroom_name") classroomName: String): Call<GaeClassroom>
}
