package org.oppia.android.util.caching.testing

import android.app.Application
import android.content.Context
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
import org.oppia.android.app.model.TestMessage
import org.oppia.android.testing.assertThrows
import org.oppia.android.util.caching.AssetRepository
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [TestNoOpAssetRepository]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class TestNoOpAssetRepositoryTest {
  @Inject
  lateinit var assetRepository: AssetRepository

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testLoadTextFileFromLocalAssets_throwsException() {
    val exception = assertThrows(IllegalStateException::class) {
      assetRepository.loadTextFileFromLocalAssets("asset.json")
    }

    assertThat(exception).hasMessageThat().contains("Local text asset doesn't exist: asset.json")
  }

  @Test
  fun testPrimeTextFileFromLocalAssets_doesNotThrowException() {
    assetRepository.primeTextFileFromLocalAssets("asset.json")

    // Nothing happens since priming no-ops.
  }

  // TODO: add test for new method.

  @Test
  fun testPrimeTextFileFromLocalAssets_thenLoadAsset_throwsException() {
    assetRepository.primeTextFileFromLocalAssets("asset.json")

    // Priming doesn't do anything, so the exception is still thrown.
    val exception = assertThrows(IllegalStateException::class) {
      assetRepository.loadTextFileFromLocalAssets("asset.json")
    }

    assertThat(exception).hasMessageThat().contains("Local text asset doesn't exist: asset.json")
  }

  @Test
  fun testLoadProtoFromLocalAssets_throwsException() {
    val exception = assertThrows(IllegalStateException::class) {
      assetRepository.loadProtoFromLocalAssets("test", TestMessage.getDefaultInstance())
    }

    assertThat(exception).hasMessageThat().contains("Local proto asset doesn't exist: test")
  }

  @Test
  fun testTryLoadProtoFromLocalAssets_returnsDefaultProto() {
    val testMessage = TestMessage.newBuilder().apply {
      intValue = 12
    }.build()

    val result = assetRepository.tryLoadProtoFromLocalAssets("test", testMessage)

    // tryLoad() will always return the default message provided since no local assets exist.
    assertThat(result).isEqualTo(testMessage)
  }

  @Test
  fun testGetLocalAssetProtoSize_returnsNegativeOne() {
    val size = assetRepository.getLocalAssetProtoSize("test")

    // The size is always -1 since no local assets exist.
    assertThat(size).isEqualTo(-1)
  }

  @Test
  fun testLoadRemoteBinaryAsset_throwsException() {
    val exception = assertThrows(IllegalStateException::class) {
      assetRepository.loadRemoteBinaryAsset("https://example.com/test.pb")
    }

    assertThat(exception).hasMessageThat()
      .contains("Remote asset doesn't exist: https://example.com/test.pb")
  }

  @Test
  fun testLoadImageAssetFromLocalAssets_throwsException() {
    val exception = assertThrows(IllegalStateException::class) {
      assetRepository.loadImageAssetFromLocalAssets("https://example.com/test.png")
    }

    assertThat(exception).hasMessageThat()
      .contains("Local image asset doesn't exist: https://example.com/test.png")
  }

  @Test
  fun testPrimeRemoteBinaryAsset_doesNotThrowException() {
    assetRepository.primeRemoteBinaryAsset("https://example.com/test.pb")

    // Nothing happens since priming no-ops.
  }

  @Test
  fun testPrimeRemoteBinaryAsset_thenLoad_throwsException() {
    assetRepository.primeRemoteBinaryAsset("https://example.com/test.pb")

    // Priming doesn't do anything, so the exception is still thrown.
    val exception = assertThrows(IllegalStateException::class) {
      assetRepository.loadRemoteBinaryAsset("https://example.com/test.pb")
    }

    assertThat(exception).hasMessageThat()
      .contains("Remote asset doesn't exist: https://example.com/test.pb")
  }

  @Test
  fun testIsRemoteBinaryAssetDownloaded_returnsFalse() {
    val exists = assetRepository.isRemoteBinaryAssetDownloaded("https://example.com/test.pb")

    assertThat(exists).isFalse() // Nothing exists.
  }

  @Test
  fun testIsRemoteBinaryAssetDownloaded_afterPriming_returnsFalse() {
    assetRepository.primeRemoteBinaryAsset("https://example.com/test.pb")

    val exists = assetRepository.isRemoteBinaryAssetDownloaded("https://example.com/test.pb")

    assertThat(exists).isFalse() // Nothing still exists since priming does nothing.
  }

  private fun setUpTestApplicationComponent() {
    DaggerTestNoOpAssetRepositoryTest_TestApplicationComponent.builder()
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
      TestModule::class, AssetTestNoOpModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(testNoOpAssetRepositoryTest: TestNoOpAssetRepositoryTest)
  }
}
