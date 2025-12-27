package org.oppia.android.util.networking

import dagger.BindsOptionalOf
import dagger.Module

// TODO(#59): Remove this module once Bazel modularization is complete as then the debug files can
//  be directly excluded and won't require this module.
/** Provides optional dependencies corresponding to the [NetworkConnectionDebugUtil]. */
@Module
interface NetworkConnectionDebugUtilModule {

  /** Provides optional binding for [NetworkConnectionDebugUtil]. */
  @BindsOptionalOf
  fun bindsNetworkConnectionDebugUtil(): NetworkConnectionDebugUtil
}
