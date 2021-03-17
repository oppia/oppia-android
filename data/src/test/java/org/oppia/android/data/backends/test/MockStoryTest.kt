package org.oppia.android.data.backends.test

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.data.backends.api.MockStoryService
import org.oppia.android.data.backends.gae.api.StoryService
import org.oppia.android.testing.network.MockRetrofitHelper
import org.robolectric.annotation.LooperMode
import retrofit2.mock.MockRetrofit

/**
 * Test for [StoryService] retrofit instance using [MockStoryService]
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class MockStoryTest {
  private lateinit var mockRetrofit: MockRetrofit

  @Before
  fun setUp() {
    mockRetrofit = MockRetrofitHelper().createMockRetrofit()
  }

  @Test
  fun testStoryService_usingFakeJson_deserializationSuccessful() {
    val delegate = mockRetrofit.create(StoryService::class.java)
    val mockStoryService = MockStoryService(delegate)

    val story = mockStoryService.getStory("1", "randomUserId", "rt4914")
    val storyResponse = story.execute()

    assertThat(storyResponse.isSuccessful).isTrue()
    assertThat(storyResponse.body()!!.storyTitle).isEqualTo("Story 1")
  }
}
