package org.oppia.android.scripts.common

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.android.testing.assertThrows
import java.util.concurrent.TimeUnit

/** Tests for [ScriptBackgroundCoroutineDispatcher]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
class ScriptBackgroundCoroutineDispatcherTest {
  @field:[Rule JvmField] val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Mock lateinit var mockRunnable: Runnable

  @Test
  fun testDispatchTask_taskIsRun() {
    val dispatcher = ScriptBackgroundCoroutineDispatcher()

    runBlocking { withContext(dispatcher) { mockRunnable.run() } }

    verify(mockRunnable).run()
  }

  @Test
  fun testClose_noExceptionThrown() {
    val dispatcher = ScriptBackgroundCoroutineDispatcher()

    dispatcher.close()

    // The verification is that no exception is thrown (otherwise the test should fail).
  }

  @Test
  fun testDispatch_afterClosing_throwsException() {
    val dispatcher = ScriptBackgroundCoroutineDispatcher()
    dispatcher.close()

    // The task should fail to schedule since the dispatcher has been closed.
    assertThrows<CancellationException>() {
      runBlocking { withContext(dispatcher) { mockRunnable.run() } }
    }
  }

  @Test
  fun testClose_pendingTaskLongerThanCloseTimeout_taskIsNotRun() {
    val dispatcher =
      ScriptBackgroundCoroutineDispatcher(
        closeTimeout = 50L, closeTimeoutUnit = TimeUnit.MILLISECONDS
      )
    val taskStartedChannel = Channel<Boolean>()
    // Schedule a task but make sure that the attempt to close the dispatcher happens exactly
    // between the task starting and ending (to verify close timeout flows).
    @Suppress("DeferredResultUnused")
    CoroutineScope(dispatcher).async {
      taskStartedChannel.send(true)
      delay(1_000L)
      mockRunnable.run()
    }
    runBlocking { taskStartedChannel.receive() }

    dispatcher.close()
    // This slows down the test, but provides assurance that the task was definitely cancelled.
    runBlocking { delay(2_000L) }

    // The task should not have run since it was cancelled, but no exception will be thrown.
    verifyNoMoreInteractions(mockRunnable)
  }
}
