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
import org.oppia.android.data.backends.api.MockSubtopicService
import org.oppia.android.data.backends.gae.api.SubtopicService
import org.oppia.android.testing.network.MockRetrofitModule
import org.robolectric.annotation.LooperMode
import retrofit2.mock.MockRetrofit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Test for [SubtopicService] retrofit instance using [MockSubtopicService]
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class MockSubtopicTest {

  @Inject
  lateinit var mockRetrofit: MockRetrofit

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testSubtopicService_usingFakeJson_deserializationSuccessful() {
    val delegate = mockRetrofit.create(SubtopicService::class.java)
    val mockSubtopicService = MockSubtopicService(delegate)

    val subtopic = mockSubtopicService.getSubtopic("Subtopic 1", "randomId")
    val subtopicResponse = subtopic.execute()

    assertThat(subtopicResponse.isSuccessful).isTrue()
    assertThat(subtopicResponse.body()!!.subtopicTitle).isEqualTo("Subtopic 1")
  }

  private fun setUpTestApplicationComponent() {
    DaggerMockSubtopicTest_TestApplicationComponent
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

    fun inject(test: MockSubtopicTest)
  }
}
