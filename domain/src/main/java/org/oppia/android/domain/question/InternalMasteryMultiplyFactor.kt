package org.oppia.android.domain.question

import javax.inject.Qualifier

/**
 * Qualifier corresponding to the factor by which all the mastery constants were internally multiplied
 * (relative to Oppia web) with the purpose of maintaining integer representations of constants and
 * masteries for internal mastery calculations.
 */
@Qualifier
annotation class InternalMasteryMultiplyFactor