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
import org.oppia.android.data.backends.gae.NetworkConfigProdModule
import org.oppia.android.data.backends.gae.NetworkModule
import org.oppia.android.data.backends.gae.XssiPrefix
import org.oppia.android.testing.network.MockStoryService
import org.oppia.android.testing.network.RetrofitTestModule
import org.robolectric.annotation.LooperMode
import retrofit2.mock.MockRetrofit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Test for [StoryService] retrofit instance using [MockStoryService]
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class StoryServiceTest {

  @Inject
  lateinit var mockRetrofit: MockRetrofit

  @field:[Inject XssiPrefix]
  lateinit var xssiPrefix: String

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testStoryService_usingFakeJson_deserializationSuccessful() {
    val delegate = mockRetrofit.create(StoryService::class.java)
    val mockStoryService = MockStoryService(delegate, xssiPrefix)

    val story = mockStoryService.getStory("1", "randomUserId", "rt4914")
    val storyResponse = story.execute()

    assertThat(storyResponse.isSuccessful).isTrue()
    assertThat(storyResponse.body()!!.storyTitle).isEqualTo("Story 1")
  }

  private fun setUpTestApplicationComponent() {
    DaggerStoryServiceTest_TestApplicationComponent
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
      RetrofitTestModule::class, NetworkConfigProdModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(test: StoryServiceTest)
  }
}
