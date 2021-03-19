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
import org.oppia.android.data.backends.api.MockTopicService
import org.oppia.android.data.backends.gae.api.TopicService
import org.oppia.android.testing.network.MockRetrofitModule
import org.robolectric.annotation.LooperMode
import retrofit2.mock.MockRetrofit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Test for [TopicService] retrofit instance using [MockTopicService]
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class MockTopicTest {

  @Inject
  lateinit var mockRetrofit: MockRetrofit

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testTopicService_usingFakeJson_deserializationSuccessful() {
    val delegate = mockRetrofit.create(TopicService::class.java)
    val mockTopicService = MockTopicService(delegate)

    val topic = mockTopicService.getTopicByName("Topic1")
    val topicResponse = topic.execute()

    assertThat(topicResponse.isSuccessful).isTrue()
    assertThat(topicResponse.body()!!.topicName).isEqualTo("Topic1")
  }

  private fun setUpTestApplicationComponent() {
    DaggerMockTopicTest_TestApplicationComponent
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

    fun inject(test: MockTopicTest)
  }
}
