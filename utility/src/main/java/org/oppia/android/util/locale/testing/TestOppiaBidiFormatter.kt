package org.oppia.android.util.locale.testing

import java.util.Locale
import javax.inject.Inject
import org.oppia.android.util.locale.OppiaBidiFormatter
import org.oppia.android.util.locale.OppiaBidiFormatterImpl

/**
 * Test-only implementation of [OppiaBidiFormatter] for verifying calls to the formatter.
 *
 * See [isTextWrapped] for details on how to validate string wrapping. Further, note that this
 * formatter delegates to the production formatter to ensure that wrapping still happens, and it
 * will validate that strings are not wrapped multiple times.
 *
 * Implementations of this class are available by injecting [OppiaBidiFormatter.Factory] at the test
 * scope & constructing a new formatter. This fake is designed to be shared across multiple locales
 * so that wrapping can be verified without worrying about sharing formatter instances (which is due
 * to the fact that [isTextWrapped] can be called for a result from a completely different formatter
 * instance & return the correct result).
 */
class TestOppiaBidiFormatter private constructor(
  private val prodFormatter: OppiaBidiFormatter
): OppiaBidiFormatter {
  /**
   * Returns whether the specified unicode sequence has been wrapped for formatting by this class
   * (i.e. by a call to [wrapText]).
   *
   * Note that [wrapText] will ensure that already wrapped sequences are not wrapped again. Further,
   * certain operations will break this check (particularly, conversion to a string or anything that
   * returns a different char sequence from the original).
   *
   * Finally, this method will not return true for a string formatted by the original production
   * implementation or Android's bidirectional formatter directly. This is only meant to be used on
   * char sequences returned recently from a call to [wrapText].
   */
  fun isTextWrapped(unicode: CharSequence): Boolean = unicode is WrappedStringMarker

  override fun wrapText(unicode: CharSequence): CharSequence {
    check(unicode !is WrappedStringMarker) {
      "Error: encountered string that's already been wrapped: $unicode"
    }
    return WrappedStringMarker(prodFormatter.wrapText(unicode))
  }

  /** Implementation of [OppiaBidiFormatter.Factory] for test formatters. */
  class FactoryImpl @Inject constructor(
    private val prodFactoryImpl: OppiaBidiFormatterImpl.FactoryImpl
  ): OppiaBidiFormatter.Factory {
    override fun createFormatter(locale: Locale): OppiaBidiFormatter =
      TestOppiaBidiFormatter(prodFactoryImpl.createFormatter(locale))
  }

  private data class WrappedStringMarker(private val wrappedSeq: CharSequence) : CharSequence {
    override val length: Int = wrappedSeq.length

    override fun get(index: Int): Char = wrappedSeq.get(index)

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence =
      wrappedSeq.subSequence(startIndex, endIndex)

    override fun toString(): String = wrappedSeq.toString()
  }
}
