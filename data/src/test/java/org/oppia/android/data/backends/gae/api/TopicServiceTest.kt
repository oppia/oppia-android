package org.oppia.android.data.backends.gae.api

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.data.backends.gae.NetworkConfigModule
import org.oppia.android.data.backends.gae.NetworkModule
import org.oppia.android.data.backends.gae.XssiPrefix
import org.oppia.android.testing.network.MockTopicService
import org.oppia.android.testing.network.RetrofitTestModule
import org.robolectric.annotation.LooperMode
import retrofit2.mock.MockRetrofit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Test for [TopicService] retrofit instance using [MockTopicService]
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class TopicServiceTest {

  @Inject
  lateinit var mockRetrofit: MockRetrofit

  @Inject
  @XssiPrefix
  lateinit var xssiPrefix: String

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testTopicService_usingFakeJson_deserializationSuccessful() {
    val delegate = mockRetrofit.create(TopicService::class.java)
    val mockTopicService = MockTopicService(delegate, xssiPrefix)

    val topic = mockTopicService.getTopicByName("Topic1")
    val topicResponse = topic.execute()

    assertThat(topicResponse.isSuccessful).isTrue()
    assertThat(topicResponse.body()!!.topicName).isEqualTo("Topic1")
  }

  private fun setUpTestApplicationComponent() {
    DaggerTopicServiceTest_TestApplicationComponent
      .builder()
      .setApplication(ApplicationProvider.getApplicationContext()).build().inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, NetworkModule::class,
      RetrofitTestModule::class, NetworkConfigModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(test: TopicServiceTest)
  }
}
