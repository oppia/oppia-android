package org.oppia.android.domain.clipboard

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Inject
import javax.inject.Singleton
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.domain.clipboard.ClipboardController.CurrentClip
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.logging.SyncStatusTestModule
import org.oppia.android.testing.logging.UserIdTestModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/** Tests for [ClipboardController]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = ClipboardControllerTest.TestApplication::class)
class ClipboardControllerTest {
  private companion object {
    private const val TEST_LABEL_FROM_OPPIA_1 = "test label from Oppia one"
    private const val TEST_LABEL_FROM_OPPIA_2 = "test label from Oppia two"
    private const val TEST_TEXT_FROM_OPPIA_1 = "test text to copy from Oppia one"
    private const val TEST_TEXT_FROM_OPPIA_2 = "test text to copy from Oppia two"
    private const val TEST_TEXT_FROM_OTHER_APP_1 = "test text to copy from another app one"
    private const val TEST_TEXT_FROM_OTHER_APP_2 = "test text to copy from another app two"
  }

  @Inject lateinit var context: Context
  @Inject lateinit var clipboardController: ClipboardController
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory

  private val clipboardManager by lazy {
    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testGetCurrentClip_initialState_returnsUnknown() {
    val currentClipProvider = clipboardController.getCurrentClip()

    val currentClip = monitorFactory.waitForNextSuccessfulResult(currentClipProvider)
    assertThat(currentClip).isEqualTo(CurrentClip.Unknown)
  }

  @Test
  fun testGetCurrentClip_afterAnotherAppChangesClipboard_returnsUnknown() {
    val currentClipProvider = clipboardController.getCurrentClip()

    updateClipboard(TEST_TEXT_FROM_OTHER_APP_1)

    val currentClip = monitorFactory.waitForNextSuccessfulResult(currentClipProvider)
    assertThat(currentClip).isEqualTo(CurrentClip.Unknown)
  }

  @Test
  fun testGetCurrentClip_afterSettingClip_returnsSetWithAppText() {
    val currentClipProvider = clipboardController.getCurrentClip()

    clipboardController.setCurrentClip(TEST_LABEL_FROM_OPPIA_1, TEST_TEXT_FROM_OPPIA_1)

    val currentClip = monitorFactory.waitForNextSuccessfulResult(currentClipProvider)
    assertThat(currentClip).isEqualTo(
      CurrentClip.SetWithAppText(label = TEST_LABEL_FROM_OPPIA_1, text = TEST_TEXT_FROM_OPPIA_1)
    )
  }

  @Test
  fun testGetCurrentClip_afterSettingClip_again_notifiesNewSetWithAppText() {
    val currentClipProvider = clipboardController.getCurrentClip()
    val monitor = monitorFactory.createMonitor(currentClipProvider)
    clipboardController.setCurrentClip(TEST_LABEL_FROM_OPPIA_1, TEST_TEXT_FROM_OPPIA_1)
    monitor.waitForNextResult()

    clipboardController.setCurrentClip(TEST_LABEL_FROM_OPPIA_2, TEST_TEXT_FROM_OPPIA_2)

    val currentClip = monitor.waitForNextSuccessResult()
    assertThat(currentClip).isEqualTo(
      CurrentClip.SetWithAppText(label = TEST_LABEL_FROM_OPPIA_2, text = TEST_TEXT_FROM_OPPIA_2)
    )
  }

  @Test
  fun testGetCurrentClip_afterSettingClip_again_newSub_returnsNewSetWithAppText() {
    val currentClipProvider = clipboardController.getCurrentClip()
    clipboardController.setCurrentClip(TEST_LABEL_FROM_OPPIA_1, TEST_TEXT_FROM_OPPIA_1)

    clipboardController.setCurrentClip(TEST_LABEL_FROM_OPPIA_2, TEST_TEXT_FROM_OPPIA_2)

    val currentClip = monitorFactory.waitForNextSuccessfulResult(currentClipProvider)
    assertThat(currentClip).isEqualTo(
      CurrentClip.SetWithAppText(label = TEST_LABEL_FROM_OPPIA_2, text = TEST_TEXT_FROM_OPPIA_2)
    )
  }

  @Test
  fun testGetCurrentClip_setClip_otherAppChangesClipboard_notifiesSetWithOtherContent() {
    val currentClipProvider = clipboardController.getCurrentClip()
    val monitor = monitorFactory.createMonitor(currentClipProvider)
    clipboardController.setCurrentClip(TEST_LABEL_FROM_OPPIA_1, TEST_TEXT_FROM_OPPIA_1)
    monitor.waitForNextResult()

    updateClipboard(TEST_TEXT_FROM_OTHER_APP_1)

    // The other app changing the clipboard results in Oppia's content being removed.
    val currentClip = monitor.waitForNextSuccessResult()
    assertThat(currentClip).isEqualTo(CurrentClip.SetWithOtherContent)
  }

  @Test
  fun testGetCurrentClip_setClip_otherAppChangesClipboard_newSub_returnsSetWithOtherContent() {
    val currentClipProvider = clipboardController.getCurrentClip()
    clipboardController.setCurrentClip(TEST_LABEL_FROM_OPPIA_1, TEST_TEXT_FROM_OPPIA_1)

    updateClipboard(TEST_TEXT_FROM_OTHER_APP_1)

    // The other app changing the clipboard results in Oppia's content being removed.
    val currentClip = monitorFactory.waitForNextSuccessfulResult(currentClipProvider)
    assertThat(currentClip).isEqualTo(CurrentClip.SetWithOtherContent)
  }

  @Test
  fun testGetCurrentClip_setClip_otherAppChangesClipboard_again_doesNotRenotify() {
    val currentClipProvider = clipboardController.getCurrentClip()
    val monitor = monitorFactory.createMonitor(currentClipProvider)
    clipboardController.setCurrentClip(TEST_LABEL_FROM_OPPIA_1, TEST_TEXT_FROM_OPPIA_1)
    updateClipboard(TEST_TEXT_FROM_OTHER_APP_1)
    monitor.waitForNextResult()

    // Copy different text from another app.
    updateClipboard(TEST_TEXT_FROM_OTHER_APP_2)

    // There shouldn't actually be a notification in this case since the state didn't technically
    // change (since the controller never exposes clipboard content from other apps).
    monitor.verifyProviderIsNotUpdated()
  }

  @Test
  fun testGetCurrentClip_setClip_otherAppChanges_setClipAgain_notifiesNewSetWithAppText() {
    val currentClipProvider = clipboardController.getCurrentClip()
    val monitor = monitorFactory.createMonitor(currentClipProvider)
    clipboardController.setCurrentClip(TEST_LABEL_FROM_OPPIA_1, TEST_TEXT_FROM_OPPIA_1)
    updateClipboard(TEST_TEXT_FROM_OTHER_APP_1)
    monitor.waitForNextResult()

    // Copy the same Oppia text again.
    clipboardController.setCurrentClip(TEST_LABEL_FROM_OPPIA_1, TEST_TEXT_FROM_OPPIA_1)

    // The clip should be updated with the new Oppia-sourced text.
    val currentClip = monitor.waitForNextSuccessResult()
    assertThat(currentClip).isEqualTo(
      CurrentClip.SetWithAppText(label = TEST_LABEL_FROM_OPPIA_1, text = TEST_TEXT_FROM_OPPIA_1)
    )
  }

  @Test
  fun testGetCurrentClip_setClip_otherAppChanges_twice_notifiesSetWithOtherContent() {
    val currentClipProvider = clipboardController.getCurrentClip()
    val monitor = monitorFactory.createMonitor(currentClipProvider)
    clipboardController.setCurrentClip(TEST_LABEL_FROM_OPPIA_1, TEST_TEXT_FROM_OPPIA_1)
    updateClipboard(TEST_TEXT_FROM_OTHER_APP_1)
    clipboardController.setCurrentClip(TEST_LABEL_FROM_OPPIA_2, TEST_TEXT_FROM_OPPIA_2)
    monitor.waitForNextResult()

    updateClipboard(TEST_TEXT_FROM_OTHER_APP_2)

    // Ending with copying text from another app should result in the clipboard containing 'other
    // content'.
    val currentClip = monitor.waitForNextSuccessResult()
    assertThat(currentClip).isEqualTo(CurrentClip.SetWithOtherContent)
  }

  private fun updateClipboard(text: String) {
    // Simulate copying text from another app by directly modifying the clipboard outside
    // ClipboardController.

    // This must use the setter since property syntax seems to break on SDK 30.
    @Suppress("UsePropertyAccessSyntax")
    clipboardManager.setPrimaryClip(
      ClipData.newPlainText(/* label= */ "label of text from other app", text)
    )
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
      TestModule::class, TestLogReportingModule::class, RobolectricModule::class,
      TestDispatcherModule::class, NetworkConnectionUtilDebugModule::class,
      FakeOppiaClockModule::class, PlatformParameterModule::class,
      PlatformParameterSingletonModule::class, LoggingIdentifierModule::class,
      SyncStatusTestModule::class, UserIdTestModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(test: ClipboardControllerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerClipboardControllerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: ClipboardControllerTest) {
      component.inject(test)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
