package org.oppia.android.testing.modulebundle.app

import dagger.Module
import org.oppia.android.app.activity.ActivityIntentFactoriesModule
import org.oppia.android.app.activity.route.ActivityRouterModule

/**
 * A Dagger bundle [Module] that includes all of the necessary modules for activity navigation and
 * routing functionality.
 *
 * Note that there are no configuration varieties for these modules, so in most cases this module
 * can be included as-is.
 */
@Module(includes = [ActivityIntentFactoriesModule::class, ActivityRouterModule::class])
interface ActivityRoutingBundleModule
