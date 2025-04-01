package org.oppia.android.testing.modulebundle.utility

import dagger.Module
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.system.OppiaClockModule

/**
 * A Dagger bundle [Module] that includes all of the necessary modules for using Oppia's custom
 * clock utility.
 *
 * Note that there are no configuration varieties for these modules, so in most cases this module
 * can be included as-is.
 */
@Module(includes = [FakeOppiaClockModule::class, OppiaClockModule::class])
interface ClockBundleModule
