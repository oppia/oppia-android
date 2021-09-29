package org.oppia.android.util.data

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.android.testing.assertThrows
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.util.data.AsyncDataSubscriptionManagerTest.SubscriptionCallback.Companion.toAsyncChange
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [AsyncDataSubscriptionManager]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class AsyncDataSubscriptionManagerTest {
  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject
  lateinit var asyncDataSubscriptionManager: AsyncDataSubscriptionManager

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Mock
  lateinit var mockSubscriptionCallback1: SubscriptionCallback

  @Mock
  lateinit var mockSubscriptionCallback2: SubscriptionCallback

  @Mock
  lateinit var mockSubscriptionCallback3: SubscriptionCallback

  private lateinit var notifierDispatcher: CoroutineDispatcher

  @Before
  fun setUp() {
    setUpTestApplicationComponent()

    // Create a real dispatcher for calling notifyChange() since it must be called within a
    // coroutine.
    notifierDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
  }

  @Test
  fun testNotifyChange_noSubscription_doesNothing() {
    runBlocking(notifierDispatcher) { asyncDataSubscriptionManager.notifyChange("test_sub") }

    // Verify that no callback was called (since nothing was registered). Also, no exceptions should
    // be thrown.
    verify(mockSubscriptionCallback1, never()).callback()
  }

  @Test
  fun testSubscribe_withoutNotification_doesNothing() {
    asyncDataSubscriptionManager.subscribe("test_sub", mockSubscriptionCallback1.toAsyncChange())

    // Verify that just subscribing is insufficient for the callback to be called.
    verify(mockSubscriptionCallback1, never()).callback()
  }

  @Test
  fun testSubscribe_repeatCallback_withoutNotification_doesNothing() {
    asyncDataSubscriptionManager.subscribe("test_sub", mockSubscriptionCallback1.toAsyncChange())

    // Subscribe a second time.
    asyncDataSubscriptionManager.subscribe("test_sub", mockSubscriptionCallback1.toAsyncChange())

    // Verify that just subscribing is insufficient for the callback to be called.
    verify(mockSubscriptionCallback1, never()).callback()
  }

  @Test
  fun testNotifyChange_forSubscription_invokesCallback() {
    asyncDataSubscriptionManager.subscribe("test_sub", mockSubscriptionCallback1.toAsyncChange())

    runBlocking(notifierDispatcher) { asyncDataSubscriptionManager.notifyChange("test_sub") }

    // Verify that the callback was called from the notification.
    verify(mockSubscriptionCallback1).callback()
  }

  @Test
  fun testNotifyChange_forMultipleSubscriptions_sameId_invokesAllCallbacks() {
    asyncDataSubscriptionManager.subscribe("test_sub", mockSubscriptionCallback1.toAsyncChange())
    asyncDataSubscriptionManager.subscribe("test_sub", mockSubscriptionCallback2.toAsyncChange())

    runBlocking(notifierDispatcher) { asyncDataSubscriptionManager.notifyChange("test_sub") }

    // Both callbacks should be called.
    verify(mockSubscriptionCallback1).callback()
    verify(mockSubscriptionCallback2).callback()
  }

  @Test
  fun testNotifyChange_forDifferentSubscription_doesNotInvokeCallback() {
    asyncDataSubscriptionManager.subscribe("test_sub1", mockSubscriptionCallback1.toAsyncChange())
    asyncDataSubscriptionManager.subscribe("test_sub2", mockSubscriptionCallback2.toAsyncChange())

    runBlocking(notifierDispatcher) { asyncDataSubscriptionManager.notifyChange("test_sub2") }

    // A different subscription was notified.
    verify(mockSubscriptionCallback1, never()).callback()
  }

  @Test
  fun testNotifyChange_forOneOfTwoSubscriptions_callsCorrectSubscription() {
    asyncDataSubscriptionManager.subscribe("test_sub1", mockSubscriptionCallback1.toAsyncChange())
    asyncDataSubscriptionManager.subscribe("test_sub2", mockSubscriptionCallback2.toAsyncChange())

    runBlocking(notifierDispatcher) { asyncDataSubscriptionManager.notifyChange("test_sub2") }

    // Verify that the correct subscription was called.
    verify(mockSubscriptionCallback1, never()).callback()
    verify(mockSubscriptionCallback2).callback()
  }

  @Test
  fun testNotifyChange_forSubscription_multipleNotifies_invokesCallbackForEach() {
    asyncDataSubscriptionManager.subscribe("test_sub", mockSubscriptionCallback1.toAsyncChange())

    runBlocking(notifierDispatcher) {
      // Notify 3 times.
      asyncDataSubscriptionManager.notifyChange("test_sub")
      asyncDataSubscriptionManager.notifyChange("test_sub")
      asyncDataSubscriptionManager.notifyChange("test_sub")
    }

    // Verify that the callback was called for each.
    verify(mockSubscriptionCallback1, times(3)).callback()
  }

  @Test
  fun testUnsubscribe_forUnregisteredSubscription_returnsFalse() {
    val asyncChange = mockSubscriptionCallback1.toAsyncChange()

    val result = asyncDataSubscriptionManager.unsubscribe("test_sub", asyncChange)

    // There's nothing to unsubscribe.
    assertThat(result).isFalse()
  }

  @Test
  fun testUnsubscribe_forRegisteredSubscription_returnsTrue() {
    val asyncChange = mockSubscriptionCallback1.toAsyncChange()
    asyncDataSubscriptionManager.subscribe("test_sub", asyncChange)

    val result = asyncDataSubscriptionManager.unsubscribe("test_sub", asyncChange)

    // The subscription is now unsubscribed.
    assertThat(result).isTrue()
  }

  @Test
  fun testUnsubscribe_forPreviouslyRegisteredSubscription_returnsFalse() {
    val asyncChange = mockSubscriptionCallback1.toAsyncChange()
    asyncDataSubscriptionManager.subscribe("test_sub", asyncChange)
    asyncDataSubscriptionManager.unsubscribe("test_sub", asyncChange)

    val result = asyncDataSubscriptionManager.unsubscribe("test_sub", asyncChange)

    // Trying to unsubscribe twice won't do anything.
    assertThat(result).isFalse()
  }

  @Test
  fun testUnsubscribe_forReRegisteredSubscription_returnsTrue() {
    val asyncChange = mockSubscriptionCallback1.toAsyncChange()
    asyncDataSubscriptionManager.subscribe("test_sub", asyncChange)
    asyncDataSubscriptionManager.unsubscribe("test_sub", asyncChange)
    asyncDataSubscriptionManager.subscribe("test_sub", asyncChange)

    val result = asyncDataSubscriptionManager.unsubscribe("test_sub", asyncChange)

    // Removing a re-subscription should work.
    assertThat(result).isTrue()
  }

  @Test
  fun testNotifyChange_forRemovedSubscription_doesNothing() {
    val asyncChange = mockSubscriptionCallback1.toAsyncChange()
    asyncDataSubscriptionManager.subscribe("test_sub", asyncChange)
    asyncDataSubscriptionManager.unsubscribe("test_sub", asyncChange)

    runBlocking(notifierDispatcher) { asyncDataSubscriptionManager.notifyChange("test_sub") }

    // The callback should not be called since the subscription is no longer active.
    verify(mockSubscriptionCallback1, never()).callback()
  }

  @Test
  fun testNotifyChange_forOneOfMultipleSubscriptions_removed_invokesCallbackForRemainingSub() {
    val asyncChange1 = mockSubscriptionCallback1.toAsyncChange()
    asyncDataSubscriptionManager.subscribe("test_sub", asyncChange1)
    asyncDataSubscriptionManager.subscribe("test_sub", mockSubscriptionCallback2.toAsyncChange())
    asyncDataSubscriptionManager.unsubscribe("test_sub", asyncChange1)

    runBlocking(notifierDispatcher) { asyncDataSubscriptionManager.notifyChange("test_sub") }

    // Only the second callback should be called since the first was unregistered.
    verify(mockSubscriptionCallback1, never()).callback()
    verify(mockSubscriptionCallback2).callback()
  }

  @Test
  fun testNotifyChange_beforeAndAfterRemovingSubscription_invokesCallbackOnce() {
    val asyncChange = mockSubscriptionCallback1.toAsyncChange()

    asyncDataSubscriptionManager.subscribe("test_sub", asyncChange)
    runBlocking(notifierDispatcher) { asyncDataSubscriptionManager.notifyChange("test_sub") }
    asyncDataSubscriptionManager.unsubscribe("test_sub", asyncChange)
    runBlocking(notifierDispatcher) { asyncDataSubscriptionManager.notifyChange("test_sub") }

    // The callback should only be called once (for the notification before unregistering).
    verify(mockSubscriptionCallback1).callback()
  }

  @Test
  fun testNotifyChange_forReaddedSubscription_invokesCallback() {
    val asyncChange = mockSubscriptionCallback1.toAsyncChange()
    asyncDataSubscriptionManager.subscribe("test_sub", asyncChange)
    asyncDataSubscriptionManager.unsubscribe("test_sub", asyncChange)
    asyncDataSubscriptionManager.subscribe("test_sub", asyncChange)

    runBlocking(notifierDispatcher) { asyncDataSubscriptionManager.notifyChange("test_sub") }

    // The callback should be received for a re-registered subscription.
    verify(mockSubscriptionCallback1).callback()
  }

  @Test
  fun testAssociateIds_sameId_throwsException() {
    val error = assertThrows(IllegalStateException::class) {
      asyncDataSubscriptionManager.associateIds("same_id", "same_id")
    }

    assertThat(error).hasMessageThat().contains("Encountered cycle")
  }

  @Test
  fun testAssociateIds_validParentChildIds_childCallbacksNotCalled() {
    asyncDataSubscriptionManager.associateIds("child_id", "parent_id")

    // Nothing should be called since neither ID actually corresponds to a subscription.
    verify(mockSubscriptionCallback1, never()).callback()
    verify(mockSubscriptionCallback2, never()).callback()
  }

  @Test
  fun testAssociateIds_unregisteredParentId_notifyParent_invokeChildCallback() {
    asyncDataSubscriptionManager.subscribe("child_id", mockSubscriptionCallback1.toAsyncChange())
    asyncDataSubscriptionManager.associateIds("child_id", "parent_id")

    runBlocking(notifierDispatcher) { asyncDataSubscriptionManager.notifyChange("parent_id") }

    // The parent doesn't need to have subscriptions for its children to receive notifications.
    verify(mockSubscriptionCallback1).callback()
  }

  @Test
  fun testAssociateIds_registeredParentId_notifyParent_unregisteredChild_callbackNotCalled() {
    asyncDataSubscriptionManager.subscribe("parent_id", mockSubscriptionCallback1.toAsyncChange())
    asyncDataSubscriptionManager.associateIds("child_id", "parent_id")

    runBlocking(notifierDispatcher) { asyncDataSubscriptionManager.notifyChange("parent_id") }

    // The child subscription shouldn't be called since it isn't subscribed.
    verify(mockSubscriptionCallback2, never()).callback()
  }

  @Test
  fun testAssociateIds_registeredParentChildIds_notifyParent_invokeChildCallback() {
    asyncDataSubscriptionManager.subscribe("parent_id", mockSubscriptionCallback1.toAsyncChange())
    asyncDataSubscriptionManager.subscribe("child_id", mockSubscriptionCallback2.toAsyncChange())
    asyncDataSubscriptionManager.associateIds("child_id", "parent_id")

    runBlocking(notifierDispatcher) { asyncDataSubscriptionManager.notifyChange("parent_id") }

    // The child subscription should be called since the parent was notified & there's an
    // association.
    verify(mockSubscriptionCallback2).callback()
  }

  @Test
  fun testAssociateIds_multipleChildrenRegistered_notifyParent_invokeAllChildCallbacks() {
    asyncDataSubscriptionManager.subscribe("parent_id", mockSubscriptionCallback1.toAsyncChange())
    asyncDataSubscriptionManager.subscribe("child_id1", mockSubscriptionCallback2.toAsyncChange())
    asyncDataSubscriptionManager.subscribe("child_id2", mockSubscriptionCallback3.toAsyncChange())
    asyncDataSubscriptionManager.associateIds("child_id1", "parent_id")
    asyncDataSubscriptionManager.associateIds("child_id2", "parent_id")

    runBlocking(notifierDispatcher) { asyncDataSubscriptionManager.notifyChange("parent_id") }

    // Verify that both children are notified.
    verify(mockSubscriptionCallback2).callback()
    verify(mockSubscriptionCallback3).callback()
  }

  @Test
  fun testAssociateIds_repeatedChildRegs_multipleCallbacks_notifyParent_notifiesChildOnce() {
    asyncDataSubscriptionManager.subscribe("parent_id", mockSubscriptionCallback1.toAsyncChange())
    asyncDataSubscriptionManager.subscribe("child_id", mockSubscriptionCallback2.toAsyncChange())
    // Associate the child twice.
    asyncDataSubscriptionManager.associateIds("child_id", "parent_id")
    asyncDataSubscriptionManager.associateIds("child_id", "parent_id")

    runBlocking(notifierDispatcher) { asyncDataSubscriptionManager.notifyChange("parent_id") }

    // Verify that the child is only notified once despite multiple association calls.
    verify(mockSubscriptionCallback2).callback()
  }

  @Test
  fun testAssociateIds_childRegistration_childWithMultipleSubs_notifyParent_notifiesAllChildSubs() {
    asyncDataSubscriptionManager.subscribe("parent_id", mockSubscriptionCallback1.toAsyncChange())
    asyncDataSubscriptionManager.subscribe("child_id", mockSubscriptionCallback2.toAsyncChange())
    asyncDataSubscriptionManager.subscribe("child_id", mockSubscriptionCallback3.toAsyncChange())
    asyncDataSubscriptionManager.associateIds("child_id", "parent_id")

    runBlocking(notifierDispatcher) { asyncDataSubscriptionManager.notifyChange("parent_id") }

    // All of the children's subscriptions should be called upon notification of the parent.
    verify(mockSubscriptionCallback2).callback()
    verify(mockSubscriptionCallback3).callback()
  }

  @Test
  fun testAssociateIds_disassociatedChildId_notifyParent_doesNotInvokeChildCallback() {
    asyncDataSubscriptionManager.subscribe("parent_id", mockSubscriptionCallback1.toAsyncChange())
    asyncDataSubscriptionManager.subscribe("child_id", mockSubscriptionCallback2.toAsyncChange())
    asyncDataSubscriptionManager.associateIds("child_id", "parent_id")

    asyncDataSubscriptionManager.dissociateIds("child_id", "parent_id")
    runBlocking(notifierDispatcher) { asyncDataSubscriptionManager.notifyChange("parent_id") }

    // The child shouldn't be notified since it was dissociated.
    verify(mockSubscriptionCallback2, never()).callback()
  }

  @Test
  fun testAssociateIds_disassociatedChildId_multipleChildren_notifyParent_invokesRemainingChild() {
    asyncDataSubscriptionManager.subscribe("parent_id", mockSubscriptionCallback1.toAsyncChange())
    asyncDataSubscriptionManager.subscribe("child_id1", mockSubscriptionCallback2.toAsyncChange())
    asyncDataSubscriptionManager.subscribe("child_id2", mockSubscriptionCallback3.toAsyncChange())
    asyncDataSubscriptionManager.associateIds("child_id1", "parent_id")
    asyncDataSubscriptionManager.associateIds("child_id2", "parent_id")

    asyncDataSubscriptionManager.dissociateIds("child_id1", "parent_id")
    runBlocking(notifierDispatcher) { asyncDataSubscriptionManager.notifyChange("parent_id") }

    // Only one child should be notified since the other one was dissociated.
    verify(mockSubscriptionCallback2, never()).callback()
    verify(mockSubscriptionCallback3).callback()
  }

  @Test
  fun testAssociateIds_notifyParent_multipleTimes_withAssociatedChild_notifiesChildMultipleTimes() {
    asyncDataSubscriptionManager.subscribe("parent_id", mockSubscriptionCallback1.toAsyncChange())
    asyncDataSubscriptionManager.subscribe("child_id", mockSubscriptionCallback2.toAsyncChange())
    asyncDataSubscriptionManager.associateIds("child_id", "parent_id")

    // Notify the parent twice.
    runBlocking(notifierDispatcher) { asyncDataSubscriptionManager.notifyChange("parent_id") }
    runBlocking(notifierDispatcher) { asyncDataSubscriptionManager.notifyChange("parent_id") }

    // The child should be called twice.
    verify(mockSubscriptionCallback2, times(2)).callback()
  }

  @Test
  fun testAssociateIds_notifyChild_withAssociatedChild_notifiesChildAndNotParent() {
    asyncDataSubscriptionManager.subscribe("parent_id", mockSubscriptionCallback1.toAsyncChange())
    asyncDataSubscriptionManager.subscribe("child_id", mockSubscriptionCallback2.toAsyncChange())
    asyncDataSubscriptionManager.associateIds("child_id", "parent_id")

    // Notify the parent twice.
    runBlocking(notifierDispatcher) { asyncDataSubscriptionManager.notifyChange("child_id") }

    // The association should be one-way & not reciprocated.
    verify(mockSubscriptionCallback1, never()).callback()
    verify(mockSubscriptionCallback2).callback()
  }

  @Test
  fun testAssociateIds_notifyParent_betweenAssociationAndDisassociation_notifiesOnce() {
    asyncDataSubscriptionManager.subscribe("parent_id", mockSubscriptionCallback1.toAsyncChange())
    asyncDataSubscriptionManager.subscribe("child_id", mockSubscriptionCallback2.toAsyncChange())

    asyncDataSubscriptionManager.associateIds("child_id", "parent_id")
    runBlocking(notifierDispatcher) { asyncDataSubscriptionManager.notifyChange("parent_id") }
    asyncDataSubscriptionManager.dissociateIds("child_id", "parent_id")
    runBlocking(notifierDispatcher) { asyncDataSubscriptionManager.notifyChange("parent_id") }

    // The child should only be notified once since the second notification was after
    // disassociation.
    verify(mockSubscriptionCallback2).callback()
  }

  @Test
  fun testAssociateIds_mutualCircularDependency_throwsException() {
    asyncDataSubscriptionManager.subscribe("parent_id", mockSubscriptionCallback1.toAsyncChange())
    asyncDataSubscriptionManager.subscribe("child_id", mockSubscriptionCallback2.toAsyncChange())
    asyncDataSubscriptionManager.associateIds("child_id", "parent_id")

    val error = assertThrows(IllegalStateException::class) {
      asyncDataSubscriptionManager.associateIds("parent_id", "child_id")
    }

    // Can't create a relationship between an already associated link.
    assertThat(error).hasMessageThat().contains("Encountered cycle")
  }

  @Test
  fun testAssociateIds_indirectCircularDependency_throwsException() {
    asyncDataSubscriptionManager.subscribe("parent_id", mockSubscriptionCallback1.toAsyncChange())
    asyncDataSubscriptionManager.subscribe("child_id1", mockSubscriptionCallback2.toAsyncChange())
    asyncDataSubscriptionManager.subscribe("child_id2", mockSubscriptionCallback3.toAsyncChange())
    asyncDataSubscriptionManager.associateIds("child_id1", "parent_id")
    asyncDataSubscriptionManager.associateIds("child_id2", "child_id1")

    val error = assertThrows(IllegalStateException::class) {
      asyncDataSubscriptionManager.associateIds("parent_id", "child_id1")
    }

    // Indirect cycles should also fail.
    assertThat(error).hasMessageThat().contains("Encountered cycle")
  }

  @Test
  fun testAssociateIds_indirectChildSubscription_notifyGrandparent_invokesChildCallback() {
    asyncDataSubscriptionManager.subscribe("parent_id", mockSubscriptionCallback1.toAsyncChange())
    asyncDataSubscriptionManager.subscribe("child_id1", mockSubscriptionCallback2.toAsyncChange())
    asyncDataSubscriptionManager.subscribe("child_id2", mockSubscriptionCallback3.toAsyncChange())

    // Parent -> child1, child1 -> child2
    asyncDataSubscriptionManager.associateIds("child_id1", "parent_id")
    asyncDataSubscriptionManager.associateIds("child_id2", "child_id1")
    runBlocking(notifierDispatcher) { asyncDataSubscriptionManager.notifyChange("parent_id") }

    // The grandparent being notified should result in the derived child also being notified.
    verify(mockSubscriptionCallback3).callback()
  }

  @Test
  fun testAssociateIds_directAndIndirectChildSub_notifyGrandparent_invokesChildCallbackOnce() {
    asyncDataSubscriptionManager.subscribe("parent_id", mockSubscriptionCallback1.toAsyncChange())
    asyncDataSubscriptionManager.subscribe("child_id1", mockSubscriptionCallback2.toAsyncChange())
    asyncDataSubscriptionManager.subscribe("child_id2", mockSubscriptionCallback3.toAsyncChange())

    // Parent -> child1, child1 -> child2, parent -> child2
    asyncDataSubscriptionManager.associateIds("child_id1", "parent_id")
    asyncDataSubscriptionManager.associateIds("child_id2", "child_id1")
    asyncDataSubscriptionManager.associateIds("child_id2", "parent_id")
    runBlocking(notifierDispatcher) { asyncDataSubscriptionManager.notifyChange("parent_id") }

    // In this case, the child is subscribed both directly & indirectly to the notified ID. Ensure
    // that its callback is only called once since it doesn't need to be called multiple times.
    verify(mockSubscriptionCallback3).callback()
  }

  @Test
  fun testNotifyChangeAsync_forValidSubscription_doNotRunDispatcher_doesNothing() {
    asyncDataSubscriptionManager.subscribe("test_sub", mockSubscriptionCallback1.toAsyncChange())

    asyncDataSubscriptionManager.notifyChangeAsync("test_sub")

    // The subscription shouldn't be called if the dispatcher doesn't run.
    verify(mockSubscriptionCallback1, never()).callback()
  }

  @Test
  fun testNotifyChangeAsync_forValidSubscription_runDispatcher_invokesCallback() {
    asyncDataSubscriptionManager.subscribe("test_sub", mockSubscriptionCallback1.toAsyncChange())
    asyncDataSubscriptionManager.notifyChangeAsync("test_sub")

    testCoroutineDispatchers.runCurrent()

    // Running the coroutine should trigger the notification.
    verify(mockSubscriptionCallback1).callback()
  }

  @Test
  fun testNotifyChangeAsync_multipleTimes_forValidSub_runDispatcher_invokesCallbackForAll() {
    asyncDataSubscriptionManager.subscribe("test_sub", mockSubscriptionCallback1.toAsyncChange())

    // Call notify twice, then run the dispatcher.
    asyncDataSubscriptionManager.notifyChangeAsync("test_sub")
    asyncDataSubscriptionManager.notifyChangeAsync("test_sub")
    testCoroutineDispatchers.runCurrent()

    // The subscription should be called for each deferred notification. Note that this test is sort
    // of more testing test behaviors (since explicit thread synchronization is needed in tests),
    // but having multiple notifications stack up before they have a chance to execute could certain
    // happen in some production cases.
    verify(mockSubscriptionCallback1, times(2)).callback()
  }

  private fun setUpTestApplicationComponent() {
    DaggerAsyncDataSubscriptionManagerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  interface TestModule {
    @Binds
    fun provideContext(application: Application): Context
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestDispatcherModule::class, RobolectricModule::class
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

  interface SubscriptionCallback {
    fun callback()

    companion object {
      fun SubscriptionCallback.toAsyncChange(): ObserveAsyncChange = { callback() }
    }
  }
}
