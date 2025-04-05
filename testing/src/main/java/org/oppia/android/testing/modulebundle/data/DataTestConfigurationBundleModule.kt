package org.oppia.android.testing.modulebundle.data

import dagger.Module
import org.oppia.android.data.backends.gae.RetrofitModule
import org.oppia.android.data.backends.gae.RetrofitServiceModule
import org.oppia.android.data.backends.gae.testing.NetworkConfigTestModule
import org.oppia.android.testing.modulebundle.utility.UtilityTestConfigurationBundleModule

/**
 * A Dagger bundle [Module] that includes all of the necessary configuration modules for all data
 * layer tests.
 *
 * Note that there are no configuration varieties for these modules, so in most cases this module
 * can be included as-is.
 */
@Module(
  includes = [
    NetworkConfigTestModule::class, RetrofitModule::class, RetrofitServiceModule::class,
    UtilityTestConfigurationBundleModule::class
  ]
)
interface DataTestConfigurationBundleModule
