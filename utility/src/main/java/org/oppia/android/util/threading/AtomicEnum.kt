package org.oppia.android.util.threading

import java.util.concurrent.atomic.AtomicInteger

// TODO: Remove this class in favor of AtomicReference? Remove regex pattern for this file's test, too.
/**
 * TODO: Finish.
 */
class AtomicEnum<E : Enum<E>> private constructor(
  private val initialValue: E,
  private val constants: Map<Int, E>
) {
  private val backingAtomic by lazy { AtomicInteger(initialValue.toValidOrdinal()) }

  /** TODO: Finish. */
  var value: E
    set(value) = backingAtomic.set(value.toValidOrdinal())
    get() = backingAtomic.get().toValidConstant()

  /** TODO: Finish. */
  fun getAndSet(value: E): E = backingAtomic.getAndSet(value.toValidOrdinal()).toValidConstant()

  private fun E.toValidOrdinal(): Int {
    return ordinal.also {
      require(it in constants) {
        "Constant is not in the supported list of constants: $this (per $constants)."
      }
    }
  }

  // It's safe to use getValue() since enum constants are verified when set.
  private fun Int.toValidConstant(): E = constants.getValue(this)

  companion object {
    /** TODO: Finish. */
    inline fun <reified E : Enum<E>> create(initial: E): AtomicEnum<E> =
      create(initial, enumValues<E>().asIterable())

    /** TODO: Finish. */
    fun <E : Enum<E>> create(initial: E, possibilities: Iterable<E>): AtomicEnum<E> {
      require(initial in possibilities) { "Possibilities don't include initial value: $initial." }
      return AtomicEnum(initial, possibilities.associateBy { it.ordinal })
    }
  }
}
