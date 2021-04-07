package org.oppia.android.data.backends.gae.api

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.data.backends.gae.JsonPrefixNetworkInterceptor
import org.oppia.android.data.backends.gae.NetworkSettings
import org.oppia.android.testing.network.MockClassroomService
import org.robolectric.annotation.LooperMode
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.mock.MockRetrofit
import retrofit2.mock.NetworkBehavior
import javax.inject.Inject

/**
 * Test for [ClassroomService] retrofit instance using [MockClassroomService]
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class ClassroomServiceTest {
  private lateinit var mockRetrofit: MockRetrofit
  private lateinit var retrofit: Retrofit

  @Inject
  lateinit var jsonPrefixNetworkInterceptor: JsonPrefixNetworkInterceptor

  @Before
  fun setUp() {
//    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
    val client = OkHttpClient.Builder()
//    client.addInterceptor()

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
//  }
//
//  // TODO(#89): Move this to a common test application component.
//  @Module
//  class TestModule {
//    @Provides
//    @Singleton
//    fun provideContext(application: Application): Context {
//      return application
//    }
//  }
//
//  // TODO(#89): Move this to a common test application component.
//  @Singleton
//  @Component(
//    modules = [
//      RobolectricModule::class, TestModule::class, TestDispatcherModule::class,
//      TestLogReportingModule::class
//    ]
//  )
//  interface TestApplicationComponent {
//    @Component.Builder
//    interface Builder {
//      @BindsInstance
//      fun setApplication(application: Application): Builder
//      fun build(): TestApplicationComponent
//    }
//
//    fun inject(classroomServiceTest: ClassroomServiceTest)
//  }
//
//  class TestApplication : Application() {
//    private val component: TestApplicationComponent by lazy {
//      DaggerClassroomServiceTest_TestApplicationComponent.builder()
//        .setApplication(this)
//        .build()
//    }
//
//    fun inject(classroomServiceTest: ClassroomServiceTest) {
//      component.inject(classroomServiceTest)
//    }
  }
}
