package org.oppia.domain

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
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

/** Tests for [UserAppHistoryController]. */
@RunWith(AndroidJUnit4::class)
class UserAppHistoryControllerTest {
  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Rule
  @JvmField
  val executorRule = InstantTaskExecutorRule()

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
  }

  @After
  @ExperimentalCoroutinesApi
  @ObsoleteCoroutinesApi
  fun tearDown() {
    Dispatchers.resetMain()
    testThread.close()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testController_providesInitialLiveData_thatIsPendingBeforeResultIsPosted() = runBlockingTest {
    val userAppHistoryController = UserAppHistoryController(this.coroutineContext)

    // Observe with a paused dispatcher to ensure the actual user app history value is not provided before assertion.
    val appHistory = userAppHistoryController.getUserAppHistory()
    pauseDispatcher()
    appHistory.observeForever(mockAppHistoryObserver)

    verify(mockAppHistoryObserver, atLeastOnce()).onChanged(appHistoryResultCaptor.capture())
    assertThat(appHistoryResultCaptor.value.isPending()).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testController_providesInitialLiveData_thatIndicatesUserHasNotOpenedTheApp() = runBlockingTest {
    val userAppHistoryController = UserAppHistoryController(this.coroutineContext)

    val appHistory = userAppHistoryController.getUserAppHistory()
    advanceUntilIdle()
    appHistory.observeForever(mockAppHistoryObserver)

    verify(mockAppHistoryObserver, atLeastOnce()).onChanged(appHistoryResultCaptor.capture())
    assertThat(appHistoryResultCaptor.value.isSuccess()).isTrue()
    assertThat(appHistoryResultCaptor.value.getOrThrow().alreadyOpenedApp).isFalse()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testControllerObserver_observedBeforeSettingAppOpened_providesLiveData_userDidNotOpenApp() = runBlockingTest {
    val userAppHistoryController = UserAppHistoryController(this.coroutineContext)
    val appHistory = userAppHistoryController.getUserAppHistory()

    appHistory.observeForever(mockAppHistoryObserver)
    advanceUntilIdle()
    userAppHistoryController.markUserOpenedApp()

    // The result should not indicate that the user opened the app because markUserOpenedApp does not notify observers
    // of the change.
    verify(mockAppHistoryObserver, atLeastOnce()).onChanged(appHistoryResultCaptor.capture())
    assertThat(appHistoryResultCaptor.value.isSuccess()).isTrue()
    assertThat(appHistoryResultCaptor.value.getOrThrow().alreadyOpenedApp).isFalse()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testController_observedAfterSettingAppOpened_providesLiveData_userOpenedApp() = runBlockingTest {
    val userAppHistoryController = UserAppHistoryController(this.coroutineContext)
    val appHistory = userAppHistoryController.getUserAppHistory()

    userAppHistoryController.markUserOpenedApp()
    appHistory.observeForever(mockAppHistoryObserver)
    advanceUntilIdle()

    // The app should be considered open since observation began after marking the app as opened.
    verify(mockAppHistoryObserver, atLeastOnce()).onChanged(appHistoryResultCaptor.capture())
    assertThat(appHistoryResultCaptor.value.isSuccess()).isTrue()
    assertThat(appHistoryResultCaptor.value.getOrThrow().alreadyOpenedApp).isTrue()
  }
}
