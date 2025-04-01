package org.oppia.android.testing.modulebundle.domain

import dagger.Module
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule

/**
 * A Dagger bundle [Module] that includes all of the necessary modules for using platform parameters
 * and feature flags.
 *
 * Note that there are no configuration varieties for these modules, so in most cases this module
 * can be included as-is. There's a production version of [TestPlatformParameterModule] that can be
 * used instead, but the test version is meant to act the same way but supports overriding.
 */
@Module(includes = [PlatformParameterSingletonModule::class, TestPlatformParameterModule::class])
interface PlatformParameterBundleModule
