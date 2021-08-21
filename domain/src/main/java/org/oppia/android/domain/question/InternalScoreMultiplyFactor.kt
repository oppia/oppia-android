package org.oppia.android.domain.question

import javax.inject.Qualifier

/**
 * Qualifier corresponding to the factor by which all the score constants were internally multiplied
 * (relative to Oppia web) with the purpose of maintaining integer representations of constants and
 * scores for internal score calculations.
 */
@Qualifier
annotation class InternalScoreMultiplyFactor