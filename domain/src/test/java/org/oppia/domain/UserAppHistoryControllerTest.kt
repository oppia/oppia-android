package org.oppia.domain

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.oppia.app.model.UserAppHistory
import org.oppia.test.TestUserAppHistoryControllerActivity
import org.oppia.test.TestUserAppHistoryControllerActivity.TestFragment
import org.oppia.util.data.AsyncResult

/** Tests for [UserAppHistoryController]. */
@RunWith(AndroidJUnit4::class)
class UserAppHistoryControllerTest {
  @Rule
  @JvmField
  val mockitoRule = MockitoJUnit.rule()

  @Rule
  @JvmField
  val executorRule = InstantTaskExecutorRule()

  @get:Rule
  val testActivityScenarioRule = ActivityScenarioRule(TestUserAppHistoryControllerActivity::class.java)

  @Mock
  lateinit var mockAppHistoryObserver: Observer<AsyncResult<UserAppHistory>>

  @Captor
  lateinit var appHistoryResultCaptor: ArgumentCaptor<AsyncResult<UserAppHistory>>

  // https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-test/
  private val testThread = newSingleThreadContext("UI thread")

  @Before
  fun setUp() {
    Dispatchers.setMain(testThread)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    testThread.close()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testController_providesInitialLiveData_thatIndicatesUserHasNotOpenedTheApp() = runBlockingTest {
    val userAppHistoryController = UserAppHistoryController(this.coroutineContext)

    testActivityScenarioRule.scenario.onActivity { activity ->
      getTestFragment(activity).observeUserAppHistory(userAppHistoryController.getUserAppHistory())
    }

    testActivityScenarioRule.scenario.moveToState(Lifecycle.State.RESUMED)
    advanceUntilIdle()

//    val testFragment = getTestFragment(activity)
//    val expectedOutput = activity.getString(org.oppia.test.R.string.test_output_result, false)
//    onView(withId(testFragment.getOutputTextView().id)).check(matches(withText(expectedOutput)))

    testActivityScenarioRule.scenario.onActivity { activity ->
      val appHistoryResult = getTestFragment(activity).userAppHistoryResult
      assertThat(appHistoryResult!!.getOrThrow().alreadyOpenedApp).isFalse()
    }
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testController_afterSettingAppOpened_providesLiveData_thatIndicatesUserHasOpenedTheApp() = runBlockingTest {
    val userAppHistoryController = UserAppHistoryController(testThread)

    val appHistory = userAppHistoryController.getUserAppHistory()
    appHistory.observeForever(mockAppHistoryObserver)

    userAppHistoryController.markUserOpenedApp()
    advanceUntilIdle()

    verify(mockAppHistoryObserver).onChanged(appHistoryResultCaptor.capture())
    assertThat(appHistoryResultCaptor.value.isSuccess()).isTrue()
    assertThat(appHistoryResultCaptor.value.getOrThrow().alreadyOpenedApp).isTrue()
  }

  private fun getTestFragment(testActivity: TestUserAppHistoryControllerActivity): TestFragment {
    return testActivity.supportFragmentManager.findFragmentByTag("test_fragment") as TestFragment
  }
}