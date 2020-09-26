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
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.util.logging.ExceptionLogger
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass
import kotlin.reflect.full.cast

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
    val exception = assertThrows(NoSuchElementException::class) {
      fakeExceptionLogger.getMostRecentException()
    }

    assertThat(exception).isInstanceOf(NoSuchElementException::class.java)
  }

  @Test
  fun testFakeExceptionLogger_logException_clearAllExceptions_getMostRecent_returnsFailure() {
    exceptionLogger.logException(IllegalStateException("Test Exception"))
    fakeExceptionLogger.clearAllExceptions()

    val exception = assertThrows(NoSuchElementException::class) {
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

  // TODO(#89): Move to a common test library.
  /** A replacement to JUnit5's assertThrows(). */
  private fun <T : Throwable> assertThrows(type: KClass<T>, operation: () -> Unit): T {
    try {
      operation()
      fail("Expected to encounter exception of $type")
    } catch (t: Throwable) {
      if (type.isInstance(t)) {
        return type.cast(t)
      }
      // Unexpected exception; throw it.
      throw t
    }
    throw AssertionError(
      "Reached an impossible state when verifying that an exception was thrown."
    )
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
  @Component(modules = [TestModule::class, TestLogReportingModule::class])
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
