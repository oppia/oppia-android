package org.oppia.android.testing.modulebundle.data

import dagger.Module
import org.oppia.android.data.backends.gae.NetworkConfigProdModule
import org.oppia.android.testing.modulebundle.utility.UtilityTestConfigurationBundleModule
import org.oppia.android.testing.network.RetrofitTestModule

/**
 * A Dagger bundle [Module] that includes all of the necessary configuration modules for all data
 * layer tests.
 *
 * Note that there are no configuration varieties for these modules, so in most cases this module
 * can be included as-is.
 */
@Module(includes = [
  RetrofitTestModule::class, NetworkConfigProdModule::class,
  UtilityTestConfigurationBundleModule::class
])
interface DataTestConfigurationBundleModule
