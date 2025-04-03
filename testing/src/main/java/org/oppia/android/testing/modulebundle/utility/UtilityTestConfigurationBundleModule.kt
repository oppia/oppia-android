package org.oppia.android.testing.modulebundle.utility

import dagger.Module
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.modulebundle.BaseTestModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.locale.testing.LocaleTestModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule

/**
 * A Dagger bundle [Module] that includes all of the necessary configuration modules for all utility
 * layer tests.
 *
 * Note that there are no configuration varieties for these modules, so in most cases this module
 * can be included as-is except for network configuration ([NetworkConnectionUtilDebugModule] is
 * used instead of its prod variant, but the prod variant is essentially never expected to be used
 * in tests).
 */
@Module(
  includes = [
    AccessibilityTestModule::class, LocaleTestModule::class,
    NetworkConnectionDebugUtilModule::class, NetworkConnectionUtilDebugModule::class,
    RobolectricModule::class, TestLogReportingModule::class, TestDispatcherModule::class,
    BaseTestModule::class
  ]
)
interface UtilityTestConfigurationBundleModule
