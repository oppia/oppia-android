package org.oppia.android.domain.locale

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Inject
import javax.inject.Singleton
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.util.caching.testing.AssetTestModule
import org.oppia.android.util.logging.LoggerModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

// TODO(#59): Use a build-time configuration instead of the runtime AssetRepository fake (i.e. by
//  not including the config assets during build time).
/**
 * Tests for [LanguageConfigRetriever]. Unlike [LanguageConfigRetrieverTest], this suite verifies
 * the retriever's behavior when there are no configuration files to include.
 */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class LanguageConfigRetrieverWithoutAssetsTest {
  @Inject
  lateinit var languageConfigRetriever: LanguageConfigRetriever

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  // TODO: finish

  @Test
  fun testCreateLocale_default_throwsException() {
  }

  private fun setUpTestApplicationComponent() {
    DaggerLanguageConfigRetrieverWithoutAssetsTest_TestApplicationComponent.builder()
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
      TestModule::class, LoggerModule::class, TestDispatcherModule::class, RobolectricModule::class,
      AssetTestModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(languageConfigRetrieverWithoutAssetsTest: LanguageConfigRetrieverWithoutAssetsTest)
  }
}
