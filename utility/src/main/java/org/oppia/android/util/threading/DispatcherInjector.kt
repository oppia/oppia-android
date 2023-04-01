package org.oppia.android.util.threading

import kotlinx.coroutines.CoroutineDispatcher

/** Injector for [CoroutineDispatcher]. Implemented by a generated Dagger application component. */
interface DispatcherInjector {
  /** Returns the [BackgroundDispatcher] [CoroutineDispatcher]. */
  @BackgroundDispatcher fun getBackgroundDispatcher(): CoroutineDispatcher

  /** Returns the [BlockingDispatcher] [CoroutineDispatcher]. */
  @BlockingDispatcher fun getBlockingDispatcher(): CoroutineDispatcher
}
