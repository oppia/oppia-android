package org.oppia.android.util.profile

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
import org.oppia.android.testing.RobolectricModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [DirectoryManagementUtil]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class DirectoryManagementUtilTest {

  @Inject lateinit var directoryManagementUtil: DirectoryManagementUtil

  @Inject lateinit var context: Context

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  private val TEST_DIRECTORY_1 = "TEST_DIRECTORY_1"
  private val TEST_DIRECTORY_2 = "TEST_DIRECTORY_2"
  private val TEST_FILE_1 = "TEST_FILE_1"
  private val TEST_FILE_2 = "TEST_FILE_2"

  private fun setUpTestApplicationComponent() {
    DaggerDirectoryManagementUtilTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Test
  fun testGetOrCreateDir_forCreatedDir_dirIsCreated() {
    directoryManagementUtil.getOrCreateDir(TEST_DIRECTORY_1)

    val dir = File(getAbsoluteDirPath(TEST_DIRECTORY_1))

    assertThat(dir.exists()).isTrue()
  }

  @Test
  fun testGetOrCreateDir_forCreatedDir_getSameDir_checkDirsAreEqual() {
    val dir = directoryManagementUtil.getOrCreateDir(TEST_DIRECTORY_1)

    val sameDir = directoryManagementUtil.getOrCreateDir(TEST_DIRECTORY_1)

    assertThat(dir).isEqualTo(sameDir)
  }

  @Test
  fun testGetOrCreateDir_forCreatedDir_createDifferentDir_checkDirsAreNotEqual() {
    val dir = directoryManagementUtil.getOrCreateDir(TEST_DIRECTORY_1)

    val diffDir = directoryManagementUtil.getOrCreateDir(TEST_DIRECTORY_2)

    assertThat(dir).isNotEqualTo(diffDir)
  }

  @Test
  fun testGetOrCreateDir_forCreatedDir_restartApplication_dirIsCreated() {
    directoryManagementUtil.getOrCreateDir(TEST_DIRECTORY_1)

    setUpTestApplicationComponent()

    val dir = File(getAbsoluteDirPath(TEST_DIRECTORY_1))
    assertThat(dir.exists()).isTrue()
  }

  @Test
  fun testDeleteDir_forCreatedDir_deleteDir_checkDirDoesNotExist() {
    directoryManagementUtil.getOrCreateDir(TEST_DIRECTORY_1)

    val success = directoryManagementUtil.deleteDir(TEST_DIRECTORY_1)

    val dir = File(getAbsoluteDirPath(TEST_DIRECTORY_1))
    assertThat(dir.exists()).isFalse()
    assertThat(success).isTrue()
  }

  @Test
  fun testDeleteDir_forCreatedDir_withContainedFiles_dirIsDeleted() {
    val dir = directoryManagementUtil.getOrCreateDir(TEST_DIRECTORY_1)
    val file1 = File(dir, TEST_FILE_1)
    file1.createNewFile()
    val file2 = File(dir, TEST_FILE_2)
    file2.createNewFile()

    val success = directoryManagementUtil.deleteDir(TEST_DIRECTORY_1)

    assertThat(dir.exists()).isFalse()
    assertThat(success).isTrue()
    assertThat(File(getAbsoluteDirPath(TEST_DIRECTORY_1) + "/" + TEST_FILE_1).exists()).isFalse()
    assertThat(File(getAbsoluteDirPath(TEST_DIRECTORY_1) + "/" + TEST_FILE_2).exists()).isFalse()
  }

  @Test
  fun testDeleteDir_forCreatedDir_withContainedFiles_deleteDir_createSameDir_checkDirIsEmpty() {
    val dir = directoryManagementUtil.getOrCreateDir(TEST_DIRECTORY_1)
    val file1 = File(dir, TEST_FILE_1)
    file1.createNewFile()
    val file2 = File(dir, TEST_FILE_2)
    file2.createNewFile()

    val success = directoryManagementUtil.deleteDir(TEST_DIRECTORY_1)
    directoryManagementUtil.getOrCreateDir(TEST_DIRECTORY_1)

    assertThat(success).isTrue()
    assertThat(dir.exists()).isTrue()
    assertThat(dir.listFiles().isEmpty()).isTrue()
  }

  private fun getAbsoluteDirPath(path: String): String {
    /**
     * context.filesDir.toString() looks like /tmp/robolectric-Method_test_name/org.oppia.android.util.test-dataDir/files
     * dropLast(5) removes files from the path and then it appends the real path with "app_" as a prefix
     */
    return context.filesDir.toString().dropLast(5) + "app_" + path
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
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
  @Component(
    modules = [
      TestModule::class,
      RobolectricModule::class, FakeOppiaClockModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(directoryManagementUtilTest: DirectoryManagementUtilTest)
  }
}
