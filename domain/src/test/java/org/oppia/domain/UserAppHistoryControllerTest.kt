package org.oppia.domain

import android.app.Application
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.app.model.UserAppHistory
import org.oppia.util.data.AsyncResult
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import org.robolectric.annotation.Config
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlin.coroutines.EmptyCoroutineContext

/** Tests for [UserAppHistoryController]. */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class UserAppHistoryControllerTest {
  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Rule
  @JvmField
  val executorRule = InstantTaskExecutorRule()

  @Inject
  lateinit var userAppHistoryController: UserAppHistoryController

  @Inject
  @field:TestDispatcher
  lateinit var testDispatcher: CoroutineDispatcher

  private val coroutineContext by lazy {
    EmptyCoroutineContext + testDispatcher
  }

  @Mock
  lateinit var mockAppHistoryObserver: Observer<AsyncResult<UserAppHistory>>

  @Captor
  lateinit var appHistoryResultCaptor: ArgumentCaptor<AsyncResult<UserAppHistory>>

  // https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-test/
  @ObsoleteCoroutinesApi
  private val testThread = newSingleThreadContext("TestMain")

  @Before
  @ExperimentalCoroutinesApi
  @ObsoleteCoroutinesApi
  fun setUp() {
    Dispatchers.setMain(testThread)
    setUpTestApplicationComponent()
  }

  @After
  @ExperimentalCoroutinesApi
  @ObsoleteCoroutinesApi
  fun tearDown() {
    Dispatchers.resetMain()
    testThread.close()
  }

  private fun setUpTestApplicationComponent() {
    DaggerUserAppHistoryControllerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testController_providesInitialLiveData_thatIndicatesUserHasNotOpenedTheApp() = runBlockingTest(coroutineContext) {
    val appHistory = userAppHistoryController.getUserAppHistory()
    advanceUntilIdle()
    appHistory.observeForever(mockAppHistoryObserver)

    verify(mockAppHistoryObserver, atLeastOnce()).onChanged(appHistoryResultCaptor.capture())
    assertThat(appHistoryResultCaptor.value.isSuccess()).isTrue()
    assertThat(appHistoryResultCaptor.value.getOrThrow().alreadyOpenedApp).isFalse()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testControllerObserver_observedAfterSettingAppOpened_providesLiveData_userDidNotOpenApp()
      = runBlockingTest(coroutineContext) {
    val appHistory = userAppHistoryController.getUserAppHistory()

    appHistory.observeForever(mockAppHistoryObserver)
    userAppHistoryController.markUserOpenedApp()
    advanceUntilIdle()

    // The result should not indicate that the user opened the app because markUserOpenedApp does not notify observers
    // of the change.
    verify(mockAppHistoryObserver, atLeastOnce()).onChanged(appHistoryResultCaptor.capture())
    assertThat(appHistoryResultCaptor.value.isSuccess()).isTrue()
    assertThat(appHistoryResultCaptor.value.getOrThrow().alreadyOpenedApp).isFalse()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testController_settingAppOpened_observedNewController_userOpenedApp()
      = runBlockingTest(coroutineContext) {
    userAppHistoryController.markUserOpenedApp()
    advanceUntilIdle()

    // Create the controller by creating another singleton graph and injecting it (simulating the app being recreated).
    setUpTestApplicationComponent()
    val appHistory = userAppHistoryController.getUserAppHistory()
    appHistory.observeForever(mockAppHistoryObserver)
    advanceUntilIdle()

    // The app should be considered open since a new LiveData instance was observed after marking the app as opened.
    verify(mockAppHistoryObserver, atLeastOnce()).onChanged(appHistoryResultCaptor.capture())
    assertThat(appHistoryResultCaptor.value.isSuccess()).isTrue()
    assertThat(appHistoryResultCaptor.value.getOrThrow().alreadyOpenedApp).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testController_openedApp_cleared_observeNewController_userDidNotOpenApp() = runBlockingTest(coroutineContext) {
    userAppHistoryController.markUserOpenedApp()
    advanceUntilIdle()

    // Clear, then recreate another controller.
    userAppHistoryController.clearUserAppHistory()
    setUpTestApplicationComponent()
    val appHistory = userAppHistoryController.getUserAppHistory()
    appHistory.observeForever(mockAppHistoryObserver)
    advanceUntilIdle()

    // The app should be considered not yet opened since the previous history was cleared.
    verify(mockAppHistoryObserver, atLeastOnce()).onChanged(appHistoryResultCaptor.capture())
    assertThat(appHistoryResultCaptor.value.isSuccess()).isTrue()
    assertThat(appHistoryResultCaptor.value.getOrThrow().alreadyOpenedApp).isFalse()
  }

  @Qualifier annotation class TestDispatcher

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    @ExperimentalCoroutinesApi
    @Singleton
    @Provides
    @TestDispatcher
    fun provideTestDispatcher(): CoroutineDispatcher {
      return TestCoroutineDispatcher()
    }

    @Singleton
    @Provides
    @BackgroundDispatcher
    fun provideBackgroundDispatcher(@TestDispatcher testDispatcher: CoroutineDispatcher): CoroutineDispatcher {
      return testDispatcher
    }

    @Singleton
    @Provides
    @BlockingDispatcher
    fun provideBlockingDispatcher(@TestDispatcher testDispatcher: CoroutineDispatcher): CoroutineDispatcher {
      return testDispatcher
    }
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(modules = [TestModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(userAppHistoryControllerTest: UserAppHistoryControllerTest)
  }
}
