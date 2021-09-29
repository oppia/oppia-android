package org.oppia.android.app.translation.testing

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
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
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [TestActivityRecreator]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = TestActivityRecreatorTest.TestApplication::class)
class TestActivityRecreatorTest {
  @Inject
  lateinit var testActivityRecreator: TestActivityRecreator

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testGetRecreateCount_initialState_returnsZero() {
    val recreateCount = testActivityRecreator.getRecreateCount()

    // The initial recreate count should be zero.
    assertThat(recreateCount).isEqualTo(0)
  }

  @Test
  fun testRecreate_doesEffectivelyNothing() {
    testActivityRecreator.recreate(AppCompatActivity())

    // Recreate shouldn't actually do anything for the test recreator. Unfortunately, this is
    // difficult to directly verify since Robolectric's activity shadow doesn't provide
    // functionality for checking recreate.
  }

  @Test
  fun testGetRecreateCount_afterRecreate_returnsOne() {
    testActivityRecreator.recreate(AppCompatActivity())

    val recreateCount = testActivityRecreator.getRecreateCount()

    // An activity has been requested to be recreated.
    assertThat(recreateCount).isEqualTo(1)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
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
      TestModule::class, TestLogReportingModule::class, TestDispatcherModule::class,
      RobolectricModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(testActivityRecreatorTest: TestActivityRecreatorTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerTestActivityRecreatorTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(testActivityRecreatorTest: TestActivityRecreatorTest) {
      component.inject(testActivityRecreatorTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
