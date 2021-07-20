package org.oppia.android.util.networking

import dagger.Binds
import dagger.Module
import javax.inject.Singleton

/** Provides prod implementation of [NetworkConnectionUtil]. */
@Module
interface NetworkConnectionUtilProdModule {
  @Singleton
  @Binds
  fun bindsNetworkConnectionUtil(impl: NetworkConnectionUtilProdImpl): NetworkConnectionUtil
}
