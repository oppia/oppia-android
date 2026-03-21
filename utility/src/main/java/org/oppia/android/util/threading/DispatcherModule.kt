package org.oppia.android.util.threading

import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Singleton

/**
 * Dagger [Module] that provides [CoroutineDispatcher]s that bind to [BackgroundDispatcher] and [BlockingDispatcher]
 * qualifiers.
 */
@Module
class DispatcherModule {
  @Provides
  @BackgroundDispatcher
  @Singleton
  fun provideBackgroundDispatcher(): CoroutineDispatcher {
    return Executors.newFixedThreadPool(
      /* nThreads= */ 4, OppiaThreadFactory("background")
    ).asCoroutineDispatcher()
  }

  @Provides
  @BlockingDispatcher
  @Singleton
  fun provideBlockingDispatcher(): CoroutineDispatcher {
    return Executors.newSingleThreadExecutor(OppiaThreadFactory("blocking")).asCoroutineDispatcher()
  }

  private class OppiaThreadFactory(private val poolName: String): ThreadFactory {
    private val threadFactory by lazy { Executors.defaultThreadFactory() }
    private val counter = AtomicInteger(0)

    override fun newThread(runnable: Runnable): Thread {
      // Use a proxy runnable to force Oppia into the callstack. This is a useful trick to ensure
      // that cases when context may be lost (e.g. due to a coroutine suspend hop) that the trace
      // still points back to an Oppia thread.
      val proxyRunnable = Runnable { runnable.run() }
      return threadFactory.newThread(proxyRunnable).also {
        it.name = "oppia-$poolName-thread-${counter.incrementAndGet()}"
      }
    }
  }
}
