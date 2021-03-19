package org.oppia.android.data.backends.test

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.data.backends.api.MockClassroomService
import org.oppia.android.data.backends.gae.api.ClassroomService
import org.oppia.android.testing.network.MockRetrofitModule
import org.robolectric.annotation.LooperMode
import retrofit2.mock.MockRetrofit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Test for [ClassroomService] retrofit instance using [MockClassroomService]
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class MockClassroomTest {

  @Inject
  lateinit var mockRetrofit: MockRetrofit

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
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

  private fun setUpTestApplicationComponent() {
    DaggerMockClassroomTest_TestApplicationComponent
      .builder()
      .setApplication(ApplicationProvider.getApplicationContext()).build().inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(modules = [MockRetrofitModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(test: MockClassroomTest)
  }
}
