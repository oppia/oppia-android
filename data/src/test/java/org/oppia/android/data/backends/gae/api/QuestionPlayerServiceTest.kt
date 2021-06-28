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
import org.oppia.android.data.backends.gae.NetworkModule
import org.oppia.android.testing.network.MockQuestionPlayerService
import org.oppia.android.testing.network.RetrofitTestModule
import org.robolectric.annotation.LooperMode
import retrofit2.mock.MockRetrofit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Test for [QuestionPlayerService] retrofit instance using [MockQuestionPlayerService]
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class QuestionPlayerServiceTest {

  @Inject
  lateinit var mockRetrofit: MockRetrofit

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testQuestionPlayerService_usingFakeJson_deserializationSuccessful() {
    val delegate = mockRetrofit.create(QuestionPlayerService::class.java)
    val mockQuestionPlayerService = MockQuestionPlayerService(delegate)

    val skillIdList = ArrayList<String>()
    skillIdList.add("1")
    skillIdList.add("2")
    skillIdList.add("3")
    val skillIds = skillIdList.joinToString(separator = ", ")
    val questionPlayer = mockQuestionPlayerService.getQuestionPlayerBySkillIds(skillIds, 10)
    val questionPlayerResponse = questionPlayer.execute()

    assertThat(questionPlayerResponse.isSuccessful).isTrue()
    assertThat(questionPlayerResponse.body()!!.questions!!.size).isEqualTo(1)
  }

  private fun setUpTestApplicationComponent() {
    DaggerQuestionPlayerServiceTest_TestApplicationComponent
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
  @Component(modules = [TestModule::class, NetworkModule::class, RetrofitTestModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(test: QuestionPlayerServiceTest)
  }
}
