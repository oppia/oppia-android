package org.oppia.android.util.networking

import dagger.Binds
import dagger.Module

/** Provides debug implementation of [NetworkConnectionUtil]. */
@Module
interface NetworkConnectionUtilDebugModule {
  /** Binds [NetworkConnectionUtilDebugImpl] to [NetworkConnectionUtil]. */
  @Binds
  fun bindsNetworkConnectionUtil(impl: NetworkConnectionUtilDebugImpl): NetworkConnectionUtil
}
