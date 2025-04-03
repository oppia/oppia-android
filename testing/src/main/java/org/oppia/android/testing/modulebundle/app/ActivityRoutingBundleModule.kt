package org.oppia.android.testing.modulebundle.app

import dagger.Module
import org.oppia.android.app.activity.route.ActivityRouterModule

/**
 * A Dagger bundle [Module] that includes all of the necessary modules for activity routing.
 *
 * Note that there are no configuration varieties for these modules, so in most cases this module
 * can be included as-is.
 */
@Module(includes = [ActivityRouterModule::class])
interface ActivityRoutingBundleModule
