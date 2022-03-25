package org.oppia.android.app.translation

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.extensions.proto.LiteProtoTruth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.AppLanguageSelection
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.OppiaLanguage.BRAZILIAN_PORTUGUESE
import org.oppia.android.app.model.OppiaLanguage.ENGLISH
import org.oppia.android.app.model.ProfileId
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.assertThrows
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.locale.OppiaLocale
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.util.logging.SyncStatusModule

/** Tests for [AppLanguageLocaleHandler]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = AppLanguageLocaleHandlerTest.TestApplication::class)
class AppLanguageLocaleHandlerTest {
  @Inject
  lateinit var context: Context

  @Inject
  lateinit var appLanguageLocaleHandler: AppLanguageLocaleHandler

  @Inject
  lateinit var translationController: TranslationController

  @Inject
  lateinit var monitorFactory: DataProviderTestMonitor.Factory

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testGetDisplayLocale_initialState_throwsException() {
    val exception = assertThrows(IllegalStateException::class) {
      appLanguageLocaleHandler.getDisplayLocale()
    }

    assertThat(exception).hasMessageThat().contains("Expected locale to be initialized")
  }

  @Test
  fun testInitializeLocale_doesNotThrowException() {
    setAppLanguage(ENGLISH)

    appLanguageLocaleHandler.initializeLocale(retrieveAppLanguageLocale())

    // Initializing the locale should not throw an exception.
  }

  @Test
  fun testGetDisplayLocale_afterInitialization_returnsInitializedDisplayLocale() {
    appLanguageLocaleHandler.initializeLocale(computeNewAppLanguageLocale(ENGLISH))

    val displayLocale = appLanguageLocaleHandler.getDisplayLocale()

    assertThat(displayLocale.localeContext).isNotEqualToDefaultInstance()
    assertThat(displayLocale.localeContext.languageDefinition.language).isEqualTo(ENGLISH)
  }

  @Test
  fun testInitializeLocale_twice_throwsException() {
    appLanguageLocaleHandler.initializeLocale(computeNewAppLanguageLocale(ENGLISH))

    val exception = assertThrows(IllegalStateException::class) {
      appLanguageLocaleHandler.initializeLocale(retrieveAppLanguageLocale())
    }

    // Trying to initialize a second time should result in an exception.
    assertThat(exception)
      .hasMessageThat()
      .contains("Expected to initialize the locale for the first time")
  }

  @Test
  fun testUpdateLocale_uninitialized_throwsException() {
    setAppLanguage(ENGLISH)

    val exception = assertThrows(IllegalStateException::class) {
      appLanguageLocaleHandler.updateLocale(retrieveAppLanguageLocale())
    }

    // The handler can't be updated before it's initialized.
    assertThat(exception).hasMessageThat().contains("Expected locale to be initialized")
  }

  @Test
  fun testUpdateLocale_initialized_sameLocale_returnsFalse() {
    appLanguageLocaleHandler.initializeLocale(computeNewAppLanguageLocale(ENGLISH))

    val updated = appLanguageLocaleHandler.updateLocale(retrieveAppLanguageLocale())

    // The locale never changed, so there's nothing to update.
    assertThat(updated).isFalse()
  }

  @Test
  fun testUpdateLocale_initialized_newLocale_returnsTrue() {
    appLanguageLocaleHandler.initializeLocale(computeNewAppLanguageLocale(ENGLISH))

    val updated =
      appLanguageLocaleHandler.updateLocale(computeNewAppLanguageLocale(BRAZILIAN_PORTUGUESE))

    // A new locale should result in an update.
    assertThat(updated).isTrue()
  }

  @Test
  fun testUpdateLocale_afterUpdate_sameLocale_returnsFalse() {
    appLanguageLocaleHandler.initializeLocale(computeNewAppLanguageLocale(ENGLISH))
    appLanguageLocaleHandler.updateLocale(computeNewAppLanguageLocale(BRAZILIAN_PORTUGUESE))

    val updated = appLanguageLocaleHandler.updateLocale(retrieveAppLanguageLocale())

    // Trying to update twice without changing the locale shouldn't change anything.
    assertThat(updated).isFalse()
  }

  @Test
  fun testUpdateLocale_afterUpdate_newLocale_returnsTrue() {
    appLanguageLocaleHandler.initializeLocale(computeNewAppLanguageLocale(ENGLISH))
    appLanguageLocaleHandler.updateLocale(computeNewAppLanguageLocale(BRAZILIAN_PORTUGUESE))

    // Change language back.
    val updated = appLanguageLocaleHandler.updateLocale(computeNewAppLanguageLocale(ENGLISH))

    // Updating twice with a new locale should lead to an update.
    assertThat(updated).isTrue()
  }

  @Test
  fun testGetDisplayLocale_afterUpdating_returnsUpdatedDisplayLocale() {
    appLanguageLocaleHandler.initializeLocale(computeNewAppLanguageLocale(ENGLISH))
    appLanguageLocaleHandler.updateLocale(computeNewAppLanguageLocale(BRAZILIAN_PORTUGUESE))

    val displayLocale = appLanguageLocaleHandler.getDisplayLocale()

    // The returned locale should be the updated one.
    val localeContext = displayLocale.localeContext
    assertThat(localeContext).isNotEqualToDefaultInstance()
    assertThat(localeContext.languageDefinition.language).isEqualTo(BRAZILIAN_PORTUGUESE)
  }

  @Test
  fun testInitializeLocaleForActivity_uninitialized_throwsException() {
    val configuration = Configuration()

    val exception = assertThrows(IllegalStateException::class) {
      appLanguageLocaleHandler.initializeLocaleForActivity(configuration)
    }

    // The handler can't be updated before it's initialized.
    assertThat(exception).hasMessageThat().contains("Expected locale to be initialized")
  }

  @Test
  fun testInitializeLocaleForActivity_initialized_updatesSystemLocale() {
    forceDefaultLocale(Locale.ROOT)
    val configuration = Configuration()
    appLanguageLocaleHandler.initializeLocale(computeNewAppLanguageLocale(BRAZILIAN_PORTUGUESE))

    appLanguageLocaleHandler.initializeLocaleForActivity(configuration)

    // Verify that the system locale has actually changed.
    assertThat(Locale.getDefault().language).isEqualTo("pt")
    assertThat(Locale.getDefault().country).isEqualTo("BR")
  }

  @Test
  fun testInitializeLocaleForActivity_initialized_updatesConfigurationLocale() {
    val configuration = Configuration()
    appLanguageLocaleHandler.initializeLocale(computeNewAppLanguageLocale(BRAZILIAN_PORTUGUESE))

    appLanguageLocaleHandler.initializeLocaleForActivity(configuration)

    // Verify that the locale actually changed in the configuration.
    val locales = configuration.locales
    assertThat(locales.size()).isEqualTo(1)
    assertThat(locales[0].language).isEqualTo("pt")
    assertThat(locales[0].country).isEqualTo("BR")
  }

  @Test
  fun testInitializeLocaleForActivity_initedAndUpdated_updatesSystemLocaleWithNewLocale() {
    forceDefaultLocale(Locale.ROOT)
    val configuration = Configuration()
    appLanguageLocaleHandler.initializeLocale(computeNewAppLanguageLocale(BRAZILIAN_PORTUGUESE))
    appLanguageLocaleHandler.updateLocale(computeNewAppLanguageLocale(ENGLISH))

    appLanguageLocaleHandler.initializeLocaleForActivity(configuration)

    // Verify that the system locale changed to the updated version.
    assertThat(Locale.getDefault().language).isEqualTo("en")
  }

  @Test
  fun testInitializeLocaleForActivity_initedAndUpdated_updatesConfigurationLocaleWithNewLocale() {
    val configuration = Configuration()
    appLanguageLocaleHandler.initializeLocale(computeNewAppLanguageLocale(BRAZILIAN_PORTUGUESE))
    appLanguageLocaleHandler.updateLocale(computeNewAppLanguageLocale(ENGLISH))

    appLanguageLocaleHandler.initializeLocaleForActivity(configuration)

    // Verify that the configuration locale matches the updated version.
    val locales = configuration.locales
    assertThat(locales.size()).isEqualTo(1)
    assertThat(locales[0].language).isEqualTo("en")
  }

  @Test
  fun testIsInitialized_initialState_returnsFalse() {
    val isInitialized = appLanguageLocaleHandler.isInitialized()

    // initializeLocale() hasn't yet been called.
    assertThat(isInitialized).isFalse()
  }

  @Test
  fun testIsInitialized_afterInitialization_returnsTrue() {
    setAppLanguage(ENGLISH)
    appLanguageLocaleHandler.initializeLocale(retrieveAppLanguageLocale())

    val isInitialized = appLanguageLocaleHandler.isInitialized()

    // The handler should now (& hereafter) be initialized.
    assertThat(isInitialized).isTrue()
  }

  @Test
  fun testIsInitialized_afterInitialization_andUpdate_returnsTrue() {
    appLanguageLocaleHandler.initializeLocale(computeNewAppLanguageLocale(ENGLISH))
    appLanguageLocaleHandler.updateLocale(computeNewAppLanguageLocale(BRAZILIAN_PORTUGUESE))

    val isInitialized = appLanguageLocaleHandler.isInitialized()

    // Updating the locale should keep the handler initialized.
    assertThat(isInitialized).isTrue()
  }

  private fun forceDefaultLocale(locale: Locale) {
    context.applicationContext.resources.configuration.setLocale(locale)
    Locale.setDefault(locale)
  }

  private fun setAppLanguage(language: OppiaLanguage) {
    val updateProvider =
      translationController.updateAppLanguage(
        ProfileId.getDefaultInstance(),
        AppLanguageSelection.newBuilder().apply {
          selectedLanguage = language
        }.build()
      )
    monitorFactory.waitForNextSuccessfulResult(updateProvider)
  }

  /**
   * Returns a [OppiaLocale.DisplayLocale] based on the current app language which is configured
   * either via [setAppLanguage] or [forceDefaultLocale], the latter case only if the app language
   * hasn't been explicitly set (since it then defaults to the system's language).
   */
  private fun retrieveAppLanguageLocale(): OppiaLocale.DisplayLocale {
    val localeProvider = translationController.getAppLanguageLocale(ProfileId.getDefaultInstance())
    return monitorFactory.waitForNextSuccessfulResult(localeProvider)
  }

  private fun computeNewAppLanguageLocale(language: OppiaLanguage): OppiaLocale.DisplayLocale {
    setAppLanguage(language)
    return retrieveAppLanguageLocale()
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
      RobolectricModule::class, LoggerModule::class, LogStorageModule::class,
      NetworkConnectionUtilDebugModule::class, NetworkConnectionDebugUtilModule::class,
      AssetModule::class, LocaleProdModule::class, FakeOppiaClockModule::class,
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class, PlatformParameterModule::class,
      PlatformParameterSingletonModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(appLanguageLocaleHandlerTest: AppLanguageLocaleHandlerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerAppLanguageLocaleHandlerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(appLanguageLocaleHandlerTest: AppLanguageLocaleHandlerTest) {
      component.inject(appLanguageLocaleHandlerTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
