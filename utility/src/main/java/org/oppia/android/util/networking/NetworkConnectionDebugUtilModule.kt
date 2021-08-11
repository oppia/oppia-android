package org.oppia.android.util.networking

import dagger.BindsOptionalOf
import dagger.Module

/** Provides optional dependencies corresponding to the [NetworkConnectionDebugUtil]. */
@Module
interface NetworkConnectionDebugUtilModule {

  /** Provides optional binding for [NetworkConnectionDebugUtil]. */
  @BindsOptionalOf
  fun bindsNetworkConnectionDebugUtil(): NetworkConnectionDebugUtil
}
