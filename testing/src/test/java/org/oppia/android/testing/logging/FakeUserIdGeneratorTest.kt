package org.oppia.android.testing.logging

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
import org.oppia.android.util.system.UserIdGenerator
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

private const val TEST_UUID_1 = "test_uuid_1"
private const val TEST_UUID_2 = "test_uuid_2"

/** Tests for [FakeUserIdGenerator]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class FakeUserIdGeneratorTest {
  @Inject lateinit var fakeUserIdGenerator: FakeUserIdGenerator
  @Inject lateinit var userIdGenerator: UserIdGenerator

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testRandomUserId_returnsDefaultValue() {
    val userId = fakeUserIdGenerator.randomUserId

    assertThat(userId).isEqualTo(FakeUserIdGenerator.DEFAULT_USER_ID)
  }

  @Test
  fun testGenerateRandomUserId_returnsDefaultValue() {
    val userId = fakeUserIdGenerator.generateRandomUserId()

    assertThat(userId).isEqualTo(FakeUserIdGenerator.DEFAULT_USER_ID)
  }

  @Test
  fun testRandomUserId_afterSettingItToNewValue_returnsNewValue() {
    fakeUserIdGenerator.randomUserId = TEST_UUID_1

    val userId = fakeUserIdGenerator.randomUserId

    assertThat(userId).isEqualTo(TEST_UUID_1)
    assertThat(userId).isNotEqualTo(FakeUserIdGenerator.DEFAULT_USER_ID)
  }

  @Test
  fun testGenerateRandomUserId_afterSettingNewRandomUserIdValue_returnsNewValue() {
    fakeUserIdGenerator.randomUserId = TEST_UUID_1

    val userId = userIdGenerator.generateRandomUserId()

    assertThat(userId).isEqualTo(TEST_UUID_1)
    assertThat(userId).isNotEqualTo(FakeUserIdGenerator.DEFAULT_USER_ID)
  }

  @Test
  fun testGenerateRandomUserId_twice_afterSettingValue_returnsSameValue() {
    fakeUserIdGenerator.randomUserId = TEST_UUID_1

    val userId1 = userIdGenerator.generateRandomUserId()
    val userId2 = userIdGenerator.generateRandomUserId()

    assertThat(userId1).isEqualTo(TEST_UUID_1)
    assertThat(userId1).isEqualTo(userId2)
  }

  @Test
  fun testGenerateRandomUserId_setNewValueTwice_returnsLatestValue() {
    fakeUserIdGenerator.randomUserId = TEST_UUID_1
    fakeUserIdGenerator.randomUserId = TEST_UUID_2

    val userId = userIdGenerator.generateRandomUserId()

    assertThat(userId).isEqualTo(TEST_UUID_2)
  }

  @Test
  fun testGenerateRandomUserId_setSameValueTwice_returnsSameValue() {
    fakeUserIdGenerator.randomUserId = TEST_UUID_1
    fakeUserIdGenerator.randomUserId = TEST_UUID_1

    val userId = userIdGenerator.generateRandomUserId()

    // The ID can be overwritten with the same value; this doesn't change the random user ID.
    assertThat(userId).isEqualTo(TEST_UUID_1)
  }

  @Test
  fun testGenerateRandomUserId_twice_aroundSettingNewValue_returnsDifferentValues() {
    val userId1 = userIdGenerator.generateRandomUserId()
    fakeUserIdGenerator.randomUserId = TEST_UUID_1
    val userId2 = userIdGenerator.generateRandomUserId()

    // 'Generating' a new ID after overriding the default should result in a different valuye being
    // returned.
    assertThat(userId1).isNotEqualTo(userId2)
  }

  private fun setUpTestApplicationComponent() {
    DaggerFakeUserIdGeneratorTest_TestApplicationComponent.builder()
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

    fun inject(test: FakeUserIdGeneratorTest)
  }
}
