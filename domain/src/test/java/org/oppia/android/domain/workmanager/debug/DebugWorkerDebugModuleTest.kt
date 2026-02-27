package org.oppia.android.domain.workmanager.debug

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.domain.workmanager.OppiaWorker
import org.oppia.android.domain.workmanager.StartupWorkerScheduleReadinessListener
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [DebugWorkerDebugModule]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = DebugWorkerDebugModuleTest.TestApplication::class)
@Suppress("FunctionName") // FunctionName: test names are conventionally named with underscores.
class DebugWorkerDebugModuleTest {
  @Inject lateinit var context: Context
  @Inject lateinit var readyLists: Set<@JvmSuppressWildcards StartupWorkerScheduleReadinessListener>
  @Inject lateinit var factories: Map<String, @JvmSuppressWildcards OppiaWorker.Factory<*>>

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testInjection_bindsDebugWorkerSchedulerIntoReadinessListenerSet() {
    // The main test is that the test suite builds.
    val debugWorkerSchedulers = readyLists.filterIsInstance<DebugWorkerScheduler>()
    assertThat(readyLists).isNotEmpty()
    assertThat(debugWorkerSchedulers).hasSize(1)
  }

  @Test
  fun testInjection_bindsDebugWorkerFactoryByWorkerName() {
    assertThat(factories).containsKey(DebugWorker.WORKER_NAME)
    assertThat(factories[DebugWorker.WORKER_NAME]).isInstanceOf(DebugWorker.Factory::class.java)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Module
  interface TestModule {
    @Binds fun bindApplicationContext(application: Application): Context
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class,
      DebugWorkerDebugModule::class,
      LocaleProdModule::class,
      FakeOppiaClockModule::class,
      LoggerModule::class,
      RobolectricModule::class, TestDispatcherModule::class,
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(test: DebugWorkerDebugModuleTest)
  }

  class TestApplication : Application() {
    private val component: TestApplicationComponent by lazy {
      DaggerDebugWorkerDebugModuleTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: DebugWorkerDebugModuleTest) {
      component.inject(test)
    }
  }
}
