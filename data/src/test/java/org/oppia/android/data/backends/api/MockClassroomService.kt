package org.oppia.android.data.backends.api

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.oppia.android.data.backends.ApiUtils
import org.oppia.android.data.backends.gae.NetworkInterceptor
import org.oppia.android.data.backends.gae.NetworkSettings
import org.oppia.android.data.backends.gae.api.ClassroomService
import org.oppia.android.data.backends.gae.model.GaeClassroom
import retrofit2.Call
import retrofit2.mock.BehaviorDelegate

/**
 * Mock ClassroomService with dummy data from [classroom.json]
 */
class MockClassroomService(private val delegate: BehaviorDelegate<ClassroomService>) :
  ClassroomService {
  override fun getClassroom(classroomName: String): Call<GaeClassroom> {
    val classroom = createMockGaeClassroom()
    return delegate.returningResponse(classroom).getClassroom(classroomName)
  }

  /**
   * This function creates a mock GaeClassroom with data from dummy json.
   * @return GaeClassroom: GaeClassroom with mock data
   */
  private fun createMockGaeClassroom(): GaeClassroom {
    val networkInterceptor = NetworkInterceptor()
    var classroomResponseWithXssiPrefix =
      NetworkSettings.XSSI_PREFIX + ApiUtils.getFakeJson("classroom.json")
    classroomResponseWithXssiPrefix =
      networkInterceptor.removeXSSIPrefix(classroomResponseWithXssiPrefix)

    val moshi = Moshi.Builder().build()
    val adapter: JsonAdapter<GaeClassroom> = moshi.adapter(GaeClassroom::class.java)
    val mockGaeClassroom = adapter.fromJson(classroomResponseWithXssiPrefix)

    return mockGaeClassroom!!
  }
}
