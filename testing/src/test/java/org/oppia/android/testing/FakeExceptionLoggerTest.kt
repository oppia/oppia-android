package org.oppia.android.testing

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
import org.oppia.android.util.logging.ExceptionLogger
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class FakeExceptionLoggerTest {

  @Inject
  lateinit var fakeExceptionLogger: FakeExceptionLogger

  @Inject
  lateinit var exceptionLogger: ExceptionLogger

  private val exception1 = IllegalStateException("First Exception")
  private val exception2 = NullPointerException("Second Exception")

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testFakeExceptionLogger_logException_returnsException() {
    exceptionLogger.logException(IllegalStateException("Test Exception"))
    val exception = fakeExceptionLogger.getMostRecentException()

    assertThat(exception).isInstanceOf(IllegalStateException::class.java)
    assertThat(exception).hasMessageThat().contains("Test Exception")
  }

  @Test
  fun testFakeExceptionLogger_logExceptionTwice_returnsLatestException() {
    exceptionLogger.logException(exception1)
    exceptionLogger.logException(exception2)
    val exception = fakeExceptionLogger.getMostRecentException()

    assertThat(exception).isInstanceOf(NullPointerException::class.java)
    assertThat(exception).hasMessageThat().contains("Second Exception")
  }

  @Test
  fun testFakeExceptionLogger_logExceptionTwice_withFailureSet_throwsExceptionForBoth() {
    fakeExceptionLogger.setFailure(Exception("Forced failure."))

    assertThrows<Exception> { exceptionLogger.logException(exception1) }
    val exception2 = assertThrows<Exception> { exceptionLogger.logException(exception2) }

    assertThat(exception2).hasMessageThat().isEqualTo("Forced failure.")
  }

  @Test
  fun testFakeExceptionLogger_logExceptionTwice_withNewFailureSet_throwsLatestException() {
    fakeExceptionLogger.setFailure(Exception("Forced failure."))
    fakeExceptionLogger.setFailure(Exception("Forced failure2."))

    val exception = assertThrows<Exception> { exceptionLogger.logException(exception1) }

    assertThat(exception).hasMessageThat().isEqualTo("Forced failure2.")
  }

  @Test
  fun testFakeExceptionLogger_logExceptionTwice_withFailureSetToNull_doesNotThrowException() {
    fakeExceptionLogger.setFailure(Exception("Forced failure."))
    fakeExceptionLogger.setFailure(null) // Reset.

    exceptionLogger.logException(exception1)

    val exception = fakeExceptionLogger.getMostRecentException()
    assertThat(exception).isInstanceOf(IllegalStateException::class.java)
    assertThat(exception).hasMessageThat().contains("First Exception")
  }

  @Test
  fun testFakeExceptionLogger_logException_clearList_logExceptionAgain_returnsLatestException() {
    exceptionLogger.logException(exception1)
    fakeExceptionLogger.clearAllExceptions()
    exceptionLogger.logException(exception2)
    val exception = fakeExceptionLogger.getMostRecentException()

    assertThat(exception).isInstanceOf(NullPointerException::class.java)
    assertThat(exception).hasMessageThat().contains("Second Exception")
  }

  @Test
  fun testFakeExceptionLogger_logNothing_getMostRecent_returnsFailure() {
    val exception = assertThrows<NoSuchElementException>() {
      fakeExceptionLogger.getMostRecentException()
    }

    assertThat(exception).isInstanceOf(NoSuchElementException::class.java)
  }

  @Test
  fun testFakeExceptionLogger_logException_clearAllExceptions_getMostRecent_returnsFailure() {
    exceptionLogger.logException(IllegalStateException("Test Exception"))
    fakeExceptionLogger.clearAllExceptions()

    val exception = assertThrows<NoSuchElementException>() {
      fakeExceptionLogger.getMostRecentException()
    }

    assertThat(exception).isInstanceOf(NoSuchElementException::class.java)
  }

  @Test
  fun testFakeExceptionLogger_clearAllExceptions_returnsEmptyList() {
    fakeExceptionLogger.clearAllExceptions()
    val isEmptyList = fakeExceptionLogger.noExceptionsPresent()

    assertThat(isEmptyList).isTrue()
  }

  @Test
  fun testFakeExceptionLogger_logException_clearAllExceptions_returnsEmptyList() {
    exceptionLogger.logException(IllegalStateException("Test Exception"))
    fakeExceptionLogger.clearAllExceptions()

    val isEmptyList = fakeExceptionLogger.noExceptionsPresent()

    assertThat(isEmptyList).isTrue()
  }

  @Test
  fun testFakeExceptionLogger_logMultipleExceptions_clearAllExceptions_returnsEmptyList() {
    exceptionLogger.logException(exception1)
    exceptionLogger.logException(exception2)
    fakeExceptionLogger.clearAllExceptions()

    val isEmptyList = fakeExceptionLogger.noExceptionsPresent()

    assertThat(isEmptyList).isTrue()
  }

  @Test
  fun testFakeExceptionLogger_logException_returnsNonEmptyList() {
    exceptionLogger.logException(IllegalStateException("Test Exception"))

    val isEmptyList = fakeExceptionLogger.noExceptionsPresent()

    assertThat(isEmptyList).isFalse()
  }

  @Test
  fun testFakeExceptionLogger_logMultipleExceptions_returnsNonEmptyList() {
    exceptionLogger.logException(exception1)
    exceptionLogger.logException(exception2)

    val exceptionLogStatus1 = fakeExceptionLogger.hasExceptionLogged(exception1)
    val exceptionLogStatus2 = fakeExceptionLogger.hasExceptionLogged(exception2)
    val exceptionListStatus = fakeExceptionLogger.noExceptionsPresent()

    assertThat(exceptionListStatus).isFalse()
    assertThat(exceptionLogStatus1).isTrue()
    assertThat(exceptionLogStatus2).isTrue()
  }

  @Test
  fun testFakeExceptionLogger_getMostRecentExceptions_two_noExceptionsLogged_returnsEmptyList() {
    val mostRecentExceptions = fakeExceptionLogger.getMostRecentExceptions(count = 2)

    assertThat(mostRecentExceptions).isEmpty()
  }

  @Test
  fun testFakeExceptionLogger_getMostRecentExceptions_two_oneExceptionLogged_returnsOneItemList() {
    exceptionLogger.logException(exception1)

    val mostRecentExceptions = fakeExceptionLogger.getMostRecentExceptions(count = 2)

    assertThat(mostRecentExceptions).hasSize(1)
    assertThat(mostRecentExceptions.single()).hasMessageThat().isEqualTo("First Exception")
  }

  @Test
  fun testFakeExceptionLogger_getMostRecentExceptions_two_twoExceptionsLogged_returnsInOrder() {
    exceptionLogger.logException(exception2)
    exceptionLogger.logException(exception1)

    val mostRecentExceptions = fakeExceptionLogger.getMostRecentExceptions(count = 2)

    val messages = mostRecentExceptions.map { it.message }
    assertThat(messages).containsExactly("Second Exception", "First Exception").inOrder()
  }

  @Test
  fun testFakeExceptionLogger_getMostRecentExceptions_one_twoExceptionsLogged_returnsLatest() {
    exceptionLogger.logException(exception2)
    exceptionLogger.logException(exception1)

    val mostRecentExceptions = fakeExceptionLogger.getMostRecentExceptions(count = 1)

    assertThat(mostRecentExceptions).hasSize(1)
    assertThat(mostRecentExceptions.single()).hasMessageThat().isEqualTo("First Exception")
  }

  @Test
  fun testFakeExceptionLogger_getMostRecentExceptions_zero_twoExceptionsLogged_returnsEmptyList() {
    exceptionLogger.logException(exception2)
    exceptionLogger.logException(exception1)

    val mostRecentExceptions = fakeExceptionLogger.getMostRecentExceptions(count = 0)

    assertThat(mostRecentExceptions).isEmpty()
  }

  @Test
  fun testFakeExceptionLogger_getMostRecentExceptions_negative_twoExceptionsLogged_throws() {
    exceptionLogger.logException(exception2)
    exceptionLogger.logException(exception1)

    assertThrows<IllegalArgumentException> {
      fakeExceptionLogger.getMostRecentExceptions(count = -1)
    }
  }

  @Test
  fun testFakeExceptionLogger_getMostRecentExceptions_two_twoLogged_cleared_returnsEmpty() {
    exceptionLogger.logException(exception2)
    exceptionLogger.logException(exception1)
    fakeExceptionLogger.clearAllExceptions()

    val mostRecentExceptions = fakeExceptionLogger.getMostRecentExceptions(count = 2)

    assertThat(mostRecentExceptions).isEmpty()
  }

  @Test
  fun testFakeExceptionLogger_getMostRecentExceptions_two_logged_cleared_logged_returnsLatest() {
    exceptionLogger.logException(exception1)
    fakeExceptionLogger.clearAllExceptions()
    exceptionLogger.logException(exception2)

    val mostRecentExceptions = fakeExceptionLogger.getMostRecentExceptions(count = 2)

    assertThat(mostRecentExceptions).hasSize(1)
    assertThat(mostRecentExceptions.single()).hasMessageThat().isEqualTo("Second Exception")
  }

  @Test
  fun testFakeExceptionLogger_nothingLogged_countReturnsZero() {
    val count = fakeExceptionLogger.getExceptionCount()

    assertThat(count).isEqualTo(0)
  }

  @Test
  fun testFakeExceptionLogger_logException_countReturnsOne() {
    exceptionLogger.logException(exception1)

    val count = fakeExceptionLogger.getExceptionCount()

    assertThat(count).isEqualTo(1)
  }

  @Test
  fun testFakeExceptionLogger_logExceptionTwice_countReturnsTwo() {
    exceptionLogger.logException(exception1)
    exceptionLogger.logException(exception2)

    val count = fakeExceptionLogger.getExceptionCount()

    assertThat(count).isEqualTo(2)
  }

  @Test
  fun testFakeExceptionLogger_logExceptionTwice_clear_countReturnsZero() {
    exceptionLogger.logException(exception1)
    exceptionLogger.logException(exception2)
    fakeExceptionLogger.clearAllExceptions()

    val count = fakeExceptionLogger.getExceptionCount()

    assertThat(count).isEqualTo(0)
  }

  @Test
  fun testFakeExceptionLogger_logException_clear_logOneMore_countReturnsOne() {
    exceptionLogger.logException(exception1)
    fakeExceptionLogger.clearAllExceptions()
    exceptionLogger.logException(exception2)

    val count = fakeExceptionLogger.getExceptionCount()

    assertThat(count).isEqualTo(1)
  }

  private fun setUpTestApplicationComponent() {
    DaggerFakeExceptionLoggerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
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
      TestLogReportingModule::class,
      TestModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(fakeExceptionLoggerTest: FakeExceptionLoggerTest)
  }
}
