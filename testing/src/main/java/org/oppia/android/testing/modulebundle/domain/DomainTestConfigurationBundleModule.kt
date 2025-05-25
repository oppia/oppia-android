package org.oppia.android.testing.modulebundle.domain

import dagger.Module
import org.oppia.android.domain.exploration.ExplorationProgressModule
import org.oppia.android.domain.exploration.testing.ExplorationStorageTestModule
import org.oppia.android.domain.onboarding.testing.ExpirationMetaDataRetrieverTestModule
import org.oppia.android.testing.firebase.AuthenticationTestModule
import org.oppia.android.testing.modulebundle.data.DataTestConfigurationBundleModule

/**
 * A Dagger bundle [Module] that includes all of the necessary configuration modules for all domain
 * layer tests.
 *
 * Note that there are no configuration varieties for these modules, so in most cases this module
 * can be included as-is.
 */
@Module(
  includes = [
    DataTestConfigurationBundleModule::class, ExpirationMetaDataRetrieverTestModule::class,
    ExplorationProgressModule::class, ExplorationStorageTestModule::class,
    AuthenticationTestModule::class
  ]
)
interface DomainTestConfigurationBundleModule
