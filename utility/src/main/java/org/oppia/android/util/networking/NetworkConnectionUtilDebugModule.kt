package org.oppia.android.util.networking

import dagger.Binds
import dagger.Module
import javax.inject.Singleton

/** Provides debug implementation of [NetworkConnectionUtil]. */
@Module
interface NetworkConnectionUtilDebugModule {
  @Singleton
  @Binds
  fun bindsNetworkConnectionUtil(impl: NetworkConnectionUtilDebugImpl): NetworkConnectionUtil
}
