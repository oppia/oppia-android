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
  fun testGetMostRecentException_oneExceptionLogged_returnsException() {
    exceptionLogger.logException(IllegalStateException("Test Exception"))

    val exception = fakeExceptionLogger.getMostRecentException()

    assertThat(exception).isInstanceOf(IllegalStateException::class.java)
    assertThat(exception).hasMessageThat().contains("Test Exception")
  }

  @Test
  fun testGetMostRecentException_twoExceptionsLogged_returnsLatestException() {
    exceptionLogger.logException(exception1)
    exceptionLogger.logException(exception2)

    val exception = fakeExceptionLogger.getMostRecentException()

    assertThat(exception).isInstanceOf(NullPointerException::class.java)
    assertThat(exception).hasMessageThat().contains("Second Exception")
  }

  @Test
  fun testSetFailure_tryLogExceptionTwice_throwsExceptionForBoth() {
    fakeExceptionLogger.setFailure(Exception("Forced failure."))

    assertThrows<Exception> { exceptionLogger.logException(exception1) }
    val exception2 = assertThrows<Exception> { exceptionLogger.logException(exception2) }
    assertThat(exception2).hasMessageThat().isEqualTo("Forced failure.")
  }

  @Test
  fun testSetFailure_tryLogExceptionTwice_withNewFailureSet_throwsLatestException() {
    fakeExceptionLogger.setFailure(Exception("Forced failure."))
    fakeExceptionLogger.setFailure(Exception("Forced failure2."))

    val exception = assertThrows<Exception> { exceptionLogger.logException(exception1) }

    assertThat(exception).hasMessageThat().isEqualTo("Forced failure2.")
  }

  @Test
  fun testSetFailure_tryLogExceptionTwice_withFailureSetToNull_doesNotThrowException() {
    fakeExceptionLogger.setFailure(Exception("Forced failure."))
    fakeExceptionLogger.setFailure(null) // Reset.

    exceptionLogger.logException(exception1)

    // No exception is thrown, and the logged exception can be retrieved.
    val exception = fakeExceptionLogger.getMostRecentException()
    assertThat(exception).isInstanceOf(IllegalStateException::class.java)
    assertThat(exception).hasMessageThat().contains("First Exception")
  }

  @Test
  fun testGetMostRecentException_loggedException_allCleared_loggedAnother_returnsLatestException() {
    exceptionLogger.logException(exception1)
    fakeExceptionLogger.clearAllExceptions()
    exceptionLogger.logException(exception2)

    val exception = fakeExceptionLogger.getMostRecentException()

    assertThat(exception).isInstanceOf(NullPointerException::class.java)
    assertThat(exception).hasMessageThat().contains("Second Exception")
  }

  @Test
  fun testGetMostRecentException_nothingLogged_throwsNoSuchElementException() {
    val exception = assertThrows<NoSuchElementException>() {
      fakeExceptionLogger.getMostRecentException()
    }

    assertThat(exception).isInstanceOf(NoSuchElementException::class.java)
  }

  @Test
  fun testGetMostRecentException_loggedException_allCleared_throwsNoSuchElementException() {
    exceptionLogger.logException(IllegalStateException("Test Exception"))
    fakeExceptionLogger.clearAllExceptions()

    val exception = assertThrows<NoSuchElementException>() {
      fakeExceptionLogger.getMostRecentException()
    }

    assertThat(exception).isInstanceOf(NoSuchElementException::class.java)
  }

  @Test
  fun testNoExceptionsPresent_noExceptionsLogged_allCleared_returnsTrue() {
    fakeExceptionLogger.clearAllExceptions()

    val isEmptyList = fakeExceptionLogger.noExceptionsPresent()

    assertThat(isEmptyList).isTrue()
  }

  @Test
  fun testNoExceptionsPresent_loggedException_allCleared_returnsTrue() {
    exceptionLogger.logException(IllegalStateException("Test Exception"))
    fakeExceptionLogger.clearAllExceptions()

    val isEmptyList = fakeExceptionLogger.noExceptionsPresent()

    assertThat(isEmptyList).isTrue()
  }

  @Test
  fun testNoExceptionsPresent_twoExceptionsLogged_allCleared_returnsTrue() {
    exceptionLogger.logException(exception1)
    exceptionLogger.logException(exception2)
    fakeExceptionLogger.clearAllExceptions()

    val isEmptyList = fakeExceptionLogger.noExceptionsPresent()

    assertThat(isEmptyList).isTrue()
  }

  @Test
  fun testNoExceptionsPresent_loggedException_returnsFalse() {
    exceptionLogger.logException(IllegalStateException("Test Exception"))

    val isEmptyList = fakeExceptionLogger.noExceptionsPresent()

    assertThat(isEmptyList).isFalse()
  }

  @Test
  fun testHasExceptionLogged_twoExceptionedLogged_returnsTrueForBoth() {
    exceptionLogger.logException(exception1)
    exceptionLogger.logException(exception2)

    val exception1IsLogged = fakeExceptionLogger.hasExceptionLogged(exception1)
    val exception2IsLogged = fakeExceptionLogger.hasExceptionLogged(exception2)

    assertThat(exception1IsLogged).isTrue()
    assertThat(exception2IsLogged).isTrue()
  }

  @Test
  fun testGetMostRecentExceptions_twoMostRecent_noExceptionsLogged_returnsEmptyList() {
    val mostRecentExceptions = fakeExceptionLogger.getMostRecentExceptions(count = 2)

    assertThat(mostRecentExceptions).isEmpty()
  }

  @Test
  fun testGetMostRecentExceptions_twoMostRecent_oneExceptionLogged_returnsOneItemList() {
    exceptionLogger.logException(exception1)

    val mostRecentExceptions = fakeExceptionLogger.getMostRecentExceptions(count = 2)

    assertThat(mostRecentExceptions).hasSize(1)
    assertThat(mostRecentExceptions.single()).hasMessageThat().isEqualTo("First Exception")
  }

  @Test
  fun testGetMostRecentExceptions_twoMostRecent_twoExceptionsLogged_returnsBothInLoggingOrder() {
    exceptionLogger.logException(exception2)
    exceptionLogger.logException(exception1)

    val mostRecentExceptions = fakeExceptionLogger.getMostRecentExceptions(count = 2)

    val messages = mostRecentExceptions.map { it.message }
    assertThat(messages).containsExactly("Second Exception", "First Exception").inOrder()
  }

  @Test
  fun testGetMostRecentExceptions_oneMostRecent_twoExceptionsLogged_returnsLatestException() {
    exceptionLogger.logException(exception2)
    exceptionLogger.logException(exception1)

    val mostRecentExceptions = fakeExceptionLogger.getMostRecentExceptions(count = 1)

    assertThat(mostRecentExceptions).hasSize(1)
    assertThat(mostRecentExceptions.single()).hasMessageThat().isEqualTo("First Exception")
  }

  @Test
  fun testGetMostRecentExceptions_zeroMostRecent_twoExceptionsLogged_returnsEmptyList() {
    exceptionLogger.logException(exception2)
    exceptionLogger.logException(exception1)

    val mostRecentExceptions = fakeExceptionLogger.getMostRecentExceptions(count = 0)

    assertThat(mostRecentExceptions).isEmpty()
  }

  @Test
  fun testGetMostRecentExceptions_negativeOneMostRecent_twoExceptionsLogged_throwsException() {
    exceptionLogger.logException(exception2)
    exceptionLogger.logException(exception1)

    assertThrows<IllegalArgumentException> {
      fakeExceptionLogger.getMostRecentExceptions(count = -1)
    }
  }

  @Test
  fun testGetMostRecentExceptions_twoMostRecent_twoExceptionsLogged_allCleared_returnsEmptyList() {
    exceptionLogger.logException(exception2)
    exceptionLogger.logException(exception1)
    fakeExceptionLogger.clearAllExceptions()

    val mostRecentExceptions = fakeExceptionLogger.getMostRecentExceptions(count = 2)

    assertThat(mostRecentExceptions).isEmpty()
  }

  @Test
  fun testGetMostRecentExceptions_twoMostRecent_oneLogged_allCleared_anotherLogged_returnsLatest() {
    exceptionLogger.logException(exception1)
    fakeExceptionLogger.clearAllExceptions()
    exceptionLogger.logException(exception2)

    val mostRecentExceptions = fakeExceptionLogger.getMostRecentExceptions(count = 2)

    assertThat(mostRecentExceptions).hasSize(1)
    assertThat(mostRecentExceptions.single()).hasMessageThat().isEqualTo("Second Exception")
  }

  @Test
  fun testGetExceptionCount_nothingLogged_returnsZero() {
    val count = fakeExceptionLogger.getExceptionCount()

    assertThat(count).isEqualTo(0)
  }

  @Test
  fun testGetExceptionCount_oneExceptionLogged_returnsOne() {
    exceptionLogger.logException(exception1)

    val count = fakeExceptionLogger.getExceptionCount()

    assertThat(count).isEqualTo(1)
  }

  @Test
  fun testGetExceptionCount_twoExceptionsLogged_returnsTwo() {
    exceptionLogger.logException(exception1)
    exceptionLogger.logException(exception2)

    val count = fakeExceptionLogger.getExceptionCount()

    assertThat(count).isEqualTo(2)
  }

  @Test
  fun testGetExceptionCount_twoExceptionsLogged_allCleared_returnsZero() {
    exceptionLogger.logException(exception1)
    exceptionLogger.logException(exception2)
    fakeExceptionLogger.clearAllExceptions()

    val count = fakeExceptionLogger.getExceptionCount()

    assertThat(count).isEqualTo(0)
  }

  @Test
  fun testGetExceptionCount_oneExceptionLogged_allCleared_anotherLogged_returnsOne() {
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
