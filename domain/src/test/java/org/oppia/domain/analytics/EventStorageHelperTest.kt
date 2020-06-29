package org.oppia.domain.analytics

import android.app.Application
import android.content.Context
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.InternalCoroutinesApi
import org.junit.Before

import org.junit.Assert.*
import org.junit.Rule
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.app.model.TestMessage
import org.oppia.data.persistence.PersistentCacheStore
import org.oppia.testing.TestCoroutineDispatchers
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.util.data.AsyncResult
import org.oppia.util.data.DataProviders
import org.oppia.util.threading.BackgroundDispatcher
import org.robolectric.annotation.Config
import javax.inject.Inject
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class EventStorageHelperTest {

  private companion object {
    private val TEST_MESSAGE_VERSION_1 = TestMessage.newBuilder().setVersion(1).build()
    private val TEST_MESSAGE_VERSION_2 = TestMessage.newBuilder().setVersion(2).build()
  }

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject
  lateinit var cacheFactory: PersistentCacheStore.Factory

  @Inject
  lateinit var dataProviders: DataProviders

  @InternalCoroutinesApi
  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  @field:BackgroundDispatcher
  lateinit var backgroundDispatcher: CoroutineDispatcher

  @Mock
  lateinit var mockUserAppHistoryObserver1: Observer<AsyncResult<TestMessage>>

  @Mock
  lateinit var mockUserAppHistoryObserver2: Observer<AsyncResult<TestMessage>>

  @Captor
  lateinit var userAppHistoryResultCaptor1: ArgumentCaptor<AsyncResult<TestMessage>>

  @Captor
  lateinit var userAppHistoryResultCaptor2: ArgumentCaptor<AsyncResult<TestMessage>>

  private val backgroundDispatcherScope by lazy {
    CoroutineScope(backgroundDispatcher)
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  private fun setUpTestApplicationComponent() {
    DaggerEventStorageHelperTest_TestApplicationComponent.builder()
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
      TestModule::class,
      TestLogReportingModule::class,
      TestDispatcherModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(eventStorageHelperTest: EventStorageHelperTest)
  }
}