package org.oppia.android.util.locale.testing

import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.util.locale.OppiaBidiFormatter
import org.oppia.android.util.locale.OppiaBidiFormatterImpl
import org.oppia.android.util.locale.testing.TestOppiaBidiFormatter.Checker

/**
 * Test-only implementation of [OppiaBidiFormatter] for verifying calls to the formatter.
 *
 * See [Checker.isTextWrapped] for details on how to validate string wrapping. Further, note that
 * this formatter delegates to the production formatter to ensure that wrapping still happens, and
 * it will validate that strings are not wrapped multiple times.
 *
 * Implementations of this class are available by injecting [OppiaBidiFormatter.Factory] at the test
 * scope & constructing a new formatter.
 */
class TestOppiaBidiFormatter private constructor(
  private val prodFormatter: OppiaBidiFormatter,
  private val checker: Checker
): OppiaBidiFormatter {
  override fun wrapText(unicode: CharSequence): CharSequence {
    check(unicode !is WrappedStringMarker) {
      "Error: encountered string that's already been wrapped: $unicode"
    }
    checker.wrappedTexts += unicode
    return WrappedStringMarker(prodFormatter.wrapText(unicode))
  }

  /** Implementation of [OppiaBidiFormatter.Factory] for test formatters. */
  class FactoryImpl @Inject constructor(
    private val prodFactoryImpl: OppiaBidiFormatterImpl.FactoryImpl,
    private val checker: Checker
  ): OppiaBidiFormatter.Factory {
    override fun createFormatter(locale: Locale): OppiaBidiFormatter =
      TestOppiaBidiFormatter(prodFactoryImpl.createFormatter(locale), checker)
  }

  /**
   * Checker utility for determining whether a [CharSequence] has been wrapped for bidirectional
   * formatting.
   *
   * This class can be injected at the test application scope & be used for any strings wrapped by
   * any [TestOppiaBidiFormatter]s.
   */
  @Singleton
  class Checker @Inject constructor() {
    /** The list of texts wrapped using [wrapText] across all formatters. */
    internal val wrappedTexts = mutableListOf<CharSequence>()

    /**
     * Returns whether the specified unicode sequence has been wrapped for formatting by this class
     * (i.e. by a call to [wrapText]).
     *
     * Note that [wrapText] will ensure that already wrapped sequences are not wrapped again.
     * Further, certain operations will break this check (particularly, conversion to a string or
     * anything that returns a different char scequence from the original).
     *
     * Finally, this method will not return true for a string formatted by the original production
     * implementation or Android's bidirectional formatter directly. This is only meant to be used
     * on char sequences returned recently from a call to [wrapText].
     */
    fun isTextWrapped(unicode: CharSequence): Boolean = unicode is WrappedStringMarker

    /**
     * Returns the list of all sequences wrapped using [wrapText] across all formatters. Care should
     * be taken if multiple formatters are used in succession since this list does not necessarily
     * guarantee order.
     */
    fun getAllWrappedUnicodeTexts(): List<CharSequence> = wrappedTexts
  }

  private class WrappedStringMarker(private val wrappedSeq: CharSequence) : CharSequence {
    override val length: Int = wrappedSeq.length

    override fun get(index: Int): Char = wrappedSeq[index]

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence =
      wrappedSeq.subSequence(startIndex, endIndex)

    override fun toString(): String = wrappedSeq.toString()

    override fun hashCode(): Int = wrappedSeq.hashCode()

    override fun equals(other: Any?): Boolean = toString() == other
  }
}
