package org.oppia.android.testing.modulebundle.app

import dagger.Module
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule

/**
 * A Dagger bundle [Module] that includes all of the necessary modules for showing developer options
 * menus.
 *
 * Note that there are no configuration varieties for these modules, so in most cases this module
 * can be included as-is.
 */
@Module(includes = [DeveloperOptionsModule::class, DeveloperOptionsStarterModule::class])
interface DeveloperOptionsBundleModule
