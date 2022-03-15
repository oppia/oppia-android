package org.oppia.android.util.system

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
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [UserIdGeneratorImpl]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class UserIdGeneratorImplTest {
  @Inject lateinit var userIdGenerator: UserIdGenerator

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testGenerateRandomUserId_producesNonEmptyId() {
    val userId = userIdGenerator.generateRandomUserId()

    assertThat(userId).isNotEmpty()
  }

  @Test
  fun testGenerateRandomUserId_producesDifferentIdEachTime() {
    // There's no actual good way to test the generator beyond just verifying that two subsequent
    // IDs are different. Note that this technically has an extremely slim chance to flake, but it
    // realistically should never happen.
    val userId1 = userIdGenerator.generateRandomUserId()
    val userId2 = userIdGenerator.generateRandomUserId()

    assertThat(userId1).isNotEqualTo(userId2)
  }

  private fun setUpTestApplicationComponent() {
    DaggerUserIdGeneratorImplTest_TestApplicationComponent.builder()
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
  @Component(modules = [TestModule::class, UserIdProdModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(test: UserIdGeneratorImplTest)
  }
}
