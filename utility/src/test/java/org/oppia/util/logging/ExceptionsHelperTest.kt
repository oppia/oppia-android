package org.oppia.util.logging

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
import org.oppia.app.model.ExceptionLog
import org.robolectric.annotation.Config
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class ExceptionsHelperTest {

  private val exceptionLog = ExceptionLog.newBuilder().setMessage("TEST").build()

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testHelper_convertExceptionLogToException_verifyCorrectExceptionFormed() {
    val exception = ExceptionsHelper().convertExceptionLogToException(exceptionLog)
    assertThat(exception.message).isEqualTo("TEST")
    assertThat(exception.cause).isEqualTo(null)
    assertThat(exception.stackTrace).isEqualTo(emptyArray<StackTraceElement>())
  }

  private fun setUpTestApplicationComponent() {
    DaggerExceptionsHelperTest_TestApplicationComponent.builder()
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
  @Component(modules = [TestModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(exceptionsHelperTest: ExceptionsHelperTest)
  }
}
