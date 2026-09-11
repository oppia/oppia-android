package org.oppia.android.domain.util

import org.oppia.android.app.model.NumberUnit
import org.oppia.android.app.model.NumberWithUnits

/**
 * Aggregates the units in a [NumberWithUnits] by summing the exponents of any duplicate units.
 * This is useful for normalizing [NumberWithUnits] instances for comparison, since the order of
 * units is irrelevant and duplicate units are effectively the same as a single unit with the
 * exponent equal to the sum of the exponents of the duplicate units.
 */
fun NumberWithUnits.aggregate(): NumberWithUnits {
  val aggregated = unitList.groupBy { it.unit }
    .map { (unit, units) ->
      NumberUnit.newBuilder()
        .setUnit(unit)
        .setExponent(units.sumOf { it.exponent })
        .build()
    }

  return toBuilder()
    .clearUnit()
    .addAllUnit(aggregated)
    .build()
}
