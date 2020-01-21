package org.oppia.domain.profile

import android.app.Application
import android.content.Context
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.app.model.Profile
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import org.robolectric.annotation.Config
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlin.coroutines.EmptyCoroutineContext

/** Tests for [ProfileManagementControllerTest]. */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class ProfileTestHelperTest {
  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject lateinit var context: Context

  @Inject lateinit var profileTestHelper: ProfileTestHelper

  @Inject lateinit var profileManagementController: ProfileManagementController

  @Mock
  lateinit var mockProfilesObserver: Observer<AsyncResult<List<Profile>>>
  @Captor
  lateinit var profilesResultCaptor: ArgumentCaptor<AsyncResult<List<Profile>>>

  @Mock
  lateinit var mockUpdateResultObserver: Observer<AsyncResult<Any?>>
  @Captor
  lateinit var updateResultCaptor: ArgumentCaptor<AsyncResult<Any?>>

  @Inject
  @field:TestDispatcher
  lateinit var testDispatcher: CoroutineDispatcher

  private val coroutineContext by lazy {
    EmptyCoroutineContext + testDispatcher
  }

  // https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-test/
  @ObsoleteCoroutinesApi
  private val testThread = newSingleThreadContext("TestMain")

  @Before
  @ExperimentalCoroutinesApi
  @ObsoleteCoroutinesApi
  fun setUp() {
    Dispatchers.setMain(testThread)
    setUpTestApplicationComponent()
  }

  @After
  @ExperimentalCoroutinesApi
  @ObsoleteCoroutinesApi
  fun tearDown() {
    Dispatchers.resetMain()
    testThread.close()
  }

  private fun setUpTestApplicationComponent() {
    DaggerProfileTestHelperTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testInitializeProfiles_initializeProfiles_checkProfilesAreAddedAndCurrentIsSet() = runBlockingTest(coroutineContext) {
    profileTestHelper.initializeProfiles().observeForever(mockUpdateResultObserver)
    profileManagementController.getProfiles().observeForever(mockProfilesObserver)
    advanceUntilIdle()

    verify(mockProfilesObserver, atLeastOnce()).onChanged(profilesResultCaptor.capture())
    verify(mockUpdateResultObserver, atLeastOnce()).onChanged(updateResultCaptor.capture())
    assertThat(profilesResultCaptor.value.isSuccess()).isTrue()
    assertThat(updateResultCaptor.value.isSuccess()).isTrue()
    val profiles = profilesResultCaptor.value.getOrThrow()
    assertThat(profiles[0].name).isEqualTo("Sean")
    assertThat(profiles[0].isAdmin).isTrue()
    assertThat(profiles[1].name).isEqualTo("Ben")
    assertThat(profiles[1].isAdmin).isFalse()
    assertThat(profileManagementController.getCurrentProfileId().internalId).isEqualTo(0)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testAddMoreProfiles_addMoreProfiles_checkProfilesAreAdded() = runBlockingTest(coroutineContext) {
    profileTestHelper.addMoreProfiles(10)
    advanceUntilIdle()
    profileManagementController.getProfiles().observeForever(mockProfilesObserver)
    advanceUntilIdle()

    verify(mockProfilesObserver, atLeastOnce()).onChanged(profilesResultCaptor.capture())
    assertThat(profilesResultCaptor.value.isSuccess()).isTrue()
    assertThat(profilesResultCaptor.value.getOrThrow().size).isEqualTo(10)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testLoginToAdmin_initializeProfiles_loginToAdmin_checkIsSuccessful() = runBlockingTest(coroutineContext) {
    profileTestHelper.initializeProfiles()

    profileTestHelper.loginToAdmin().observeForever(mockUpdateResultObserver)
    advanceUntilIdle()

    verify(mockUpdateResultObserver, atLeastOnce()).onChanged(updateResultCaptor.capture())
    assertThat(updateResultCaptor.value.isSuccess()).isTrue()
    assertThat(profileManagementController.getCurrentProfileId().internalId).isEqualTo(0)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testLoginToUser_initializeProfiles_loginToUser_checkIsSuccessful() = runBlockingTest(coroutineContext) {
    profileTestHelper.initializeProfiles()

    profileTestHelper.loginToUser().observeForever(mockUpdateResultObserver)
    advanceUntilIdle()

    verify(mockUpdateResultObserver, atLeastOnce()).onChanged(updateResultCaptor.capture())
    assertThat(updateResultCaptor.value.isSuccess()).isTrue()
    assertThat(profileManagementController.getCurrentProfileId().internalId).isEqualTo(1)
  }

  @Qualifier
  annotation class TestDispatcher

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    @ExperimentalCoroutinesApi
    @Singleton
    @Provides
    @TestDispatcher
    fun provideTestDispatcher(): CoroutineDispatcher {
      return TestCoroutineDispatcher()
    }

    @Singleton
    @Provides
    @BackgroundDispatcher
    fun provideBackgroundDispatcher(@TestDispatcher testDispatcher: CoroutineDispatcher): CoroutineDispatcher {
      return testDispatcher
    }

    @Singleton
    @Provides
    @BlockingDispatcher
    fun provideBlockingDispatcher(@TestDispatcher testDispatcher: CoroutineDispatcher): CoroutineDispatcher {
      return testDispatcher
    }

    // TODO(#59): Either isolate these to their own shared test module, or use the real logging
    // module in tests to avoid needing to specify these settings for tests.
    @EnableConsoleLog
    @Provides
    fun provideEnableConsoleLog(): Boolean = true

    @EnableFileLog
    @Provides
    fun provideEnableFileLog(): Boolean = false

    @GlobalLogLevel
    @Provides
    fun provideGlobalLogLevel(): LogLevel = LogLevel.VERBOSE
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

    fun inject(profileTestHelperTest: ProfileTestHelperTest)
  }
}
