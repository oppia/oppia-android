package org.oppia.android.domain.devoptions

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
import org.oppia.android.testing.robolectric.RobolectricModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.system.UserIdProdModule

/** Tests for [ShowAllHintsAndSolutionController]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = ShowAllHintsAndSolutionControllerTest.TestApplication::class)
class ShowAllHintsAndSolutionControllerTest {

  @Inject
  lateinit var showAllHintsAndSolutionController: ShowAllHintsAndSolutionController

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testGetShowAllHintsAndSolution_initialState_returnsFalse() {
    val showAllHintsAndSolution = showAllHintsAndSolutionController.getShowAllHintsAndSolution()
    assertThat(showAllHintsAndSolution).isFalse()
  }

  @Test
  fun testGetShowAllHintsAndSolution_setToShowAll_returnsTrue() {
    showAllHintsAndSolutionController.setShowAllHintsAndSolution(isEnabled = true)
    val showAllHintsAndSolution = showAllHintsAndSolutionController.getShowAllHintsAndSolution()
    assertThat(showAllHintsAndSolution).isTrue()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    fun provideContext(application: Application): Context = application
  }

  @Singleton
  @Component(
    modules = [
      TestModule::class, RobolectricModule::class,
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class, UserIdProdModule::class, PlatformParameterModule::class,
      PlatformParameterSingletonModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(showAllHintsAndSolutionControllerTest: ShowAllHintsAndSolutionControllerTest)
  }

  class TestApplication : Application() {
    private val component: TestApplicationComponent by lazy {
      DaggerShowAllHintsAndSolutionControllerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(showAllHintsAndSolutionControllerTest: ShowAllHintsAndSolutionControllerTest) {
      component.inject(showAllHintsAndSolutionControllerTest)
    }
  }
}
