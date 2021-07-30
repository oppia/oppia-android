package org.oppia.android.util.networking

import dagger.Binds
import dagger.Module

/** Provides production implementation of [NetworkConnectionUtil]. */
@Module
interface NetworkConnectionUtilProdModule {
  @Binds
  fun bindsNetworkConnectionUtil(impl: NetworkConnectionUtilProdImpl): NetworkConnectionUtil
}
