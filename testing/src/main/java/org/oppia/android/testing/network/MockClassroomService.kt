package org.oppia.android.testing.network

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.oppia.android.data.backends.gae.JsonPrefixNetworkInterceptor
import org.oppia.android.data.backends.gae.api.ClassroomService
import org.oppia.android.data.backends.gae.model.GaeClassroom
import retrofit2.Call
import retrofit2.mock.BehaviorDelegate

/**
 * Mock ClassroomService with dummy data from [classroom.json]
 */
class MockClassroomService(
  private val delegate: BehaviorDelegate<ClassroomService>,
  private val xssiPrefix: String
) : ClassroomService {
  override fun getClassroom(classroomName: String): Call<GaeClassroom> {
    val classroom = createMockGaeClassroom()
    return delegate.returningResponse(classroom).getClassroom(classroomName)
  }

  /**
   * This function creates a mock GaeClassroom with data from dummy json.
   * @return GaeClassroom: GaeClassroom with mock data
   */
  private fun createMockGaeClassroom(): GaeClassroom {
    val networkInterceptor = JsonPrefixNetworkInterceptor(xssiPrefix)
    var classroomResponseWithXssiPrefix =
      xssiPrefix + ApiMockLoader.getFakeJson("classroom.json")
    classroomResponseWithXssiPrefix =
      networkInterceptor.removeXssiPrefix(classroomResponseWithXssiPrefix)

    val moshi = Moshi.Builder().build()
    val adapter: JsonAdapter<GaeClassroom> = moshi.adapter(GaeClassroom::class.java)
    val mockGaeClassroom = adapter.fromJson(classroomResponseWithXssiPrefix)

    return mockGaeClassroom!!
  }
}
