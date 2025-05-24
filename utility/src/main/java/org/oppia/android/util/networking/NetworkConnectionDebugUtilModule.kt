package org.oppia.android.util.networking

import dagger.Binds
import dagger.Module

// TODO(#59): Remove this module once we completely migrate to Bazel from Gradle as we can then
//  directly exclude debug files from the build and thus won't be requiring this module.
/** Provides optional dependencies corresponding to the [NetworkConnectionDebugUtil]. */
@Module
interface NetworkConnectionDebugUtilModule {

  @Binds
  fun bindsNetworkConnectionDebugUtil(
    impl: NetworkConnectionUtilDebugImpl
  ): NetworkConnectionDebugUtil
}
