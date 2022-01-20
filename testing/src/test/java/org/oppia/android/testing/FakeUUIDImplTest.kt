package org.oppia.android.testing

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.util.system.UUIDWrapper
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

private const val TEST_UUID = "test_uuid"

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class FakeUUIDImplTest {

  @Inject
  lateinit var fakeUUIDImpl: FakeUUIDImpl

  @Inject
  lateinit var uuidWrapper: UUIDWrapper

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testFakeUUIDImpl_getRandomUUID_returnsDefaultValue() {
    val expectedValue = fakeUUIDImpl.getUUIDValue()
    val returnedValue = uuidWrapper.randomUUIDString()

    assertThat(returnedValue).isEqualTo(expectedValue)
  }

  @Test
  fun testFakeUUIDImpl_setRandomUUID_returnsNewValue() {
    fakeUUIDImpl.setUUIDValue(TEST_UUID)

    val returnedValue = uuidWrapper.randomUUIDString()
    assertThat(returnedValue).isEqualTo(TEST_UUID)
  }

  @Test
  fun testFakeUUIDImpl_getRandomUUID_updateUUIDValue_getRandomUUID_returnsUpdatedValue() {
    val defaultValue = fakeUUIDImpl.getUUIDValue()
    val initialValue = uuidWrapper.randomUUIDString()
    fakeUUIDImpl.setUUIDValue(TEST_UUID)
    val updatedValue = uuidWrapper.randomUUIDString()

    assertThat(initialValue).isEqualTo(defaultValue)
    assertThat(updatedValue).isEqualTo(TEST_UUID)
  }

  private fun setUpTestApplicationComponent() {
    DaggerFakeUUIDImplTest_TestApplicationComponent.builder()
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

  @Module
  interface TestUUIDModule {
    @Binds
    fun bindUUIDWrapper(fakeUUIDImpl: FakeUUIDImpl): UUIDWrapper
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(modules = [TestModule::class, TestUUIDModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(fakeUUIDImplTest: FakeUUIDImplTest)
  }
}
