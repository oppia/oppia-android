package org.oppia.android.testing.modulebundle.app

import dagger.Module
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.application.testing.TestingBuildFlavorModule
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.testing.modulebundle.domain.DomainTestConfigurationBundleModule

/**
 * A Dagger bundle [Module] that includes all of the necessary configuration modules for all app
 * layer tests.
 *
 * Note that there are no configuration varieties for these modules, so in most cases this module
 * can be included as-is.
 */
@Module(
  includes = [
    ActivityRecreatorTestModule::class, ApplicationStartupListenerModule::class,
    DomainTestConfigurationBundleModule::class, SplitScreenInteractionModule::class,
    TestingBuildFlavorModule::class
  ]
)
interface AppTestConfigurationBundleModule
