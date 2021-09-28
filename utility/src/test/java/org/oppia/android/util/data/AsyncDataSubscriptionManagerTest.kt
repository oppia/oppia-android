package org.oppia.android.util.data

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Singleton

/** Tests for [AsyncDataSubscriptionManager]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class AsyncDataSubscriptionManagerTest {
  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  // TODO: finish

  @Test
  fun testSomethingForSomethingToPass() {
  }

  // testNotifyChange_noSubscription_doesNothing
  // testNotifyChange_forSubscription_invokesCallback
  // testNotifyChange_forRemovedSubscription_doesNothing
  // testNotifyChange_forReaddedSubscription_invokesCallback
  // testNotifyChange_forValidSubscription_twice_invokesCallbackTwice
  // testAssociateIds_sameId_throwsException
  // testAssociateIds_validParentChildIds_childCallbacksNotCalled
  // testAssociateIds_unregisteredParentId_notifyParent_childCallbackNotCalled
  // testAssociateIds_registeredParentId_notifyParent_unregisteredChild_callbackNotCalled
  // testAssociateIds_registeredParentChildIds_notifyParent_invokeChildCallback
  // testAssociateIds_disassociatedChildId_notifyParent_doesNotInvokeChildCallback
  // testAssociateIds_circularDependency_throwsException
  // testNotifyChangeAsync_forValidSubscription_doNotRunDispatcher_doesNothing
  // testNotifyChangeAsync_forValidSubscription_runDispatcher_invokesCallback

  private fun setUpTestApplicationComponent() {
    DaggerAsyncDataSubscriptionManagerTest_TestApplicationComponent.builder()
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
      TestModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(asyncDataSubscriptionManagerTest: AsyncDataSubscriptionManagerTest)
  }
}
