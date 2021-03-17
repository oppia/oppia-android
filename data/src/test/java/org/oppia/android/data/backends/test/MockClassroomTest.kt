package org.oppia.android.data.backends.test

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.data.backends.api.MockClassroomService
import org.oppia.android.data.backends.gae.api.ClassroomService
import org.oppia.android.testing.network.MockRetrofitHelper
import org.robolectric.annotation.LooperMode
import retrofit2.mock.MockRetrofit

/**
 * Test for [ClassroomService] retrofit instance using [MockClassroomService]
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class MockClassroomTest {
  private lateinit var mockRetrofit: MockRetrofit

  @Before
  fun setUp() {
    mockRetrofit = MockRetrofitHelper().createMockRetrofit()
  }

  @Test
  fun testClassroomService_usingFakeJson_deserializationSuccessful() {
    val delegate = mockRetrofit.create(ClassroomService::class.java)
    val mockClassroomService = MockClassroomService(delegate)

    val classroom = mockClassroomService.getClassroom("Math")
    val classroomResponse = classroom.execute()

    assertThat(classroomResponse.isSuccessful).isTrue()
    assertThat(classroomResponse.body()!!.topicSummaryDicts?.get(0)?.name).isEqualTo("Math")
  }
}
