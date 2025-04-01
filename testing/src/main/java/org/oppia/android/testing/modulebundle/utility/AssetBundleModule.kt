package org.oppia.android.testing.modulebundle.utility

import dagger.Module
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule

/**
 * A Dagger bundle [Module] that includes all of the necessary modules for loading assets.
 *
 * Note that there are no configuration varieties for these modules, so in most cases this module
 * can be included as-is.
 */
@Module(includes = [AssetModule::class, CachingTestModule::class])
interface AssetBundleModule
