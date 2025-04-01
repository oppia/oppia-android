package org.oppia.android.testing.modulebundle.domain

import dagger.Module
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigFastShowTestModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionProdModule

/**
 * A Dagger bundle [Module] that includes all of the necessary modules for managing hints.
 *
 * Unlike ``HintsWithProdTimesBundleModule`` this configures tests to show hints quickly rather than
 * needing to wait for the normal clock time changes.
 */
@Module(includes = [
  HintsAndSolutionConfigFastShowTestModule::class, HintsAndSolutionProdModule::class
])
interface HintsWithFastTimesBundleModule
