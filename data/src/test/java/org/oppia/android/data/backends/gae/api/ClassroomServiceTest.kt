package org.oppia.android.data.backends.gae.api

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.data.backends.gae.NetworkInterceptor
import org.oppia.android.data.backends.gae.NetworkSettings
import org.oppia.android.testing.network.MockClassroomService
import org.robolectric.annotation.LooperMode
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.mock.MockRetrofit
import retrofit2.mock.NetworkBehavior

/**
 * Test for [ClassroomService] retrofit instance using [MockClassroomService]
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class ClassroomServiceTest {
  private lateinit var mockRetrofit: MockRetrofit
  private lateinit var retrofit: Retrofit

  @Before
  fun setUp() {
    val client = OkHttpClient.Builder()
    client.addInterceptor(NetworkInterceptor())

    retrofit = retrofit2.Retrofit.Builder()
      .baseUrl(NetworkSettings.getBaseUrl())
      .addConverterFactory(MoshiConverterFactory.create())
      .client(client.build())
      .build()

    val behavior = NetworkBehavior.create()
    mockRetrofit = MockRetrofit.Builder(retrofit)
      .networkBehavior(behavior)
      .build()
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
