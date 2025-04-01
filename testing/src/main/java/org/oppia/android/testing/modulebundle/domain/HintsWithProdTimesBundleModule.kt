package org.oppia.android.testing.modulebundle.domain

import dagger.Module
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionProdModule

/**
 * A Dagger bundle [Module] that includes all of the necessary modules for managing hints.
 *
 * Unlike ``HintsWithFastTimesBundleModule`` this configures tests to show hints using the standard
 * production clock time change requirements.
 */
@Module(includes = [HintsAndSolutionConfigModule::class, HintsAndSolutionProdModule::class])
interface HintsWithProdTimesBundleModule
